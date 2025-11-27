package com.namdang.memos.repository;

import com.namdang.memos.entity.ProjectMember;
import com.namdang.memos.enumType.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByProjectIdAndAccountId(UUID projectId, UUID accountId);

    List<ProjectMember> findByAccountId(UUID accountId);

    boolean existsByProject_IdAndAccount_Id(UUID projectId, UUID accountId);

    boolean existsByProject_IdAndInvitedEmailAndInvitedStatus(UUID projectId, String invitedEmail, InviteStatus invitedStatus);

    Optional<ProjectMember> findByInviteToken(String inviteToken);
}
