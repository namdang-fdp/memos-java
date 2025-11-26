package com.namdang.memos.dto.requests.invite;

import com.namdang.memos.enumType.ProjectRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectInviteRequest {
    String email;
    ProjectRole role;
}
