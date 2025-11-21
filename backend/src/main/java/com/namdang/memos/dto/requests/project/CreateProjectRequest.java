package com.namdang.memos.dto.requests.project;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProjectRequest {
    private String name;
    private String description;
    private String imageUrl;
}
