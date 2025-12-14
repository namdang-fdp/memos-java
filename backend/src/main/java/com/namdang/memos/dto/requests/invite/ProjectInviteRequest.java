package com.namdang.memos.dto.requests.invite;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectInviteRequest {
  String targetUserEmail;
}
