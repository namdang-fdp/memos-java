package com.namdang.memos.initializer;

import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminAccount();
    }

    private void seedAdminAccount() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL or ADMIN_PASSWORD not set — skipping admin creation");
            return;
        }

        // ADMIN role must be created by Flyway V2 seed
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            log.warn("ADMIN role not found. Did Flyway seed run? Skipping admin creation.");
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

        if (admin.getRoles() == null || admin.getRoles().stream().noneMatch(r -> "ADMIN".equals(r.getName()))) {
            admin.getRoles().add(adminRole);
            accountRepository.save(admin);
            log.info("Assigned ADMIN role to {}", adminEmail);
        }
    }
}
