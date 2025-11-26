package com.namdang.memos.dto.responses.project;

import com.namdang.memos.enumType.InviteStatus;
import com.namdang.memos.enumType.ProjectRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMemberResponse {
    UUID id;
    UUID accountId;
    String accountName;
    String accountEmail;
    ProjectRole role;
    InviteStatus inviteStatus;
    LocalDateTime joinedAt;
}
