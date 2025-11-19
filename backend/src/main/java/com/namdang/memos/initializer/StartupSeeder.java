package com.namdang.memos.initializer;

import com.namdang.memos.entity.Permission;
import com.namdang.memos.entity.Role;
import com.namdang.memos.entity.Account;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.repository.PermissionRepository;
import com.namdang.memos.repository.RoleRepository;
import com.namdang.memos.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        seedPermissions();
        seedRoles();
        seedRolePermissions();
        seedAdminAccount();
    }

    private void seedPermissions() {
        createPermission("ACCOUNT.UPDATE_SELF", "User updates own account");
        createPermission("ACCOUNT.DELETE_SELF", "User deletes own account");
        createPermission("PROJECT.CREATE", "User can create project");
        createPermission("VIEW.PUBLIC_CONTENT", "User views public content");
        createPermission("ADMIN.FULL_ACCESS", "Full admin access to all resources");
    }

    private void seedRoles() {
        createRole("MEMBER", "Default user role");
        createRole("ADMIN", "System administrator");
    }

    private void seedRolePermissions() {
        Role member = roleRepository.findByName("MEMBER");
        Set<Permission> memberPerms = new HashSet<>();
        memberPerms.add(permissionRepository.findByName("ACCOUNT.UPDATE_SELF").orElseThrow());
        memberPerms.add(permissionRepository.findByName("ACCOUNT.DELETE_SELF").orElseThrow());
        memberPerms.add(permissionRepository.findByName("PROJECT.CREATE").orElseThrow());
        memberPerms.add(permissionRepository.findByName("VIEW.PUBLIC_CONTENT").orElseThrow());
        member.setPermissions(memberPerms);
        roleRepository.save(member);

        Role admin = roleRepository.findByName("ADMIN");
        Set<Permission> adminPerms = new HashSet<>();
        adminPerms.add(permissionRepository.findByName("ADMIN.FULL_ACCESS").orElseThrow());
        admin.setPermissions(adminPerms);
        roleRepository.save(admin);
    }

    private void createPermission(String name, String description) {
        if (permissionRepository.existsByName(name)) return;
        Permission p = new Permission();
        p.setName(name);
        p.setDescription(description);
        permissionRepository.save(p);
    }

    private void createRole(String name, String description) {
        if (roleRepository.existsByName(name)) return;
        Role r = new Role();
        r.setName(name);
        r.setDescription(description);
        r.setPermissions(new HashSet<>());
        roleRepository.save(r);
    }

    private void seedAdminAccount() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL or ADMIN_PASSWORD not set — skipping admin creation");
            return;
        }

        Account admin = accountRepository.findByEmail(adminEmail).orElse(null);

        if (admin == null) {
            admin = new Account();
            admin.setEmail(adminEmail);
            admin.setName("Administrator");
            admin.setActive(true);
            admin.setProvider(AuthProvider.LOCAL);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            accountRepository.save(admin);
            log.info("Admin account created: {}", adminEmail);
        } else {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            accountRepository.save(admin);
            log.info("Admin account updated (password reset): {}", adminEmail);
        }

        Role adminRole = roleRepository.findByName("ADMIN");
        if (admin.getRoles() == null || admin.getRoles().stream().noneMatch(r -> r.getName().equals("ADMIN"))) {
            admin.getRoles().add(adminRole);
            accountRepository.save(admin);
            log.info("Assigned ADMIN role to {}", adminEmail);
        }
    }
}
