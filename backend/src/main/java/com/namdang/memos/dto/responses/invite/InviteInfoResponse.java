package com.namdang.memos.dto.responses.invite;

import com.namdang.memos.enumType.InviteStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteInfoResponse {
    String projectName;
    UUID projectId;
    String inviterName;
    String invitedEmail;
    InviteStatus status;
    boolean expired;
    LocalDateTime inviteExpiredAt;
}
