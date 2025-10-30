package com.namdang.memos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
public class Account extends BaseEntity{
    @Column(nullable = false, length = 255)
    private String email;

    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    @Column(length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_role",
            joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
