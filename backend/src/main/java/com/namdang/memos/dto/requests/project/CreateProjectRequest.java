package com.namdang.memos.dto.requests.project;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProjectRequest {
    String name;
    String description;
    String imageUrl;
}
