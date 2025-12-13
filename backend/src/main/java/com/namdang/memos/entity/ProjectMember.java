package com.namdang.memos.entity;

import com.namdang.memos.enumType.InviteStatus;
import com.namdang.memos.enumType.ProjectRole;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(
        name = "project_member",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"project_id", "account_id"}),
        })
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    @ToString.Exclude
    private Account account;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role = ProjectRole.MEMBER;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "invited_status", length = 20)
    private InviteStatus invitedStatus = InviteStatus.PENDING;

    @Column(name = "invite_token", length = 200, unique = true)
    private String inviteToken;

    @Column(name = "invite_expired_at")
    private LocalDateTime inviteExpiredAt;
}
