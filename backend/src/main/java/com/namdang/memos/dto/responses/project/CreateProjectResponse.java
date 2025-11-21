package com.namdang.memos.dto.responses.project;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProjectResponse {
    UUID id;
    String name;
    String projectKey;
    String description;
    String imageUrl;
    UUID createdById;
    LocalDate createdAt;
}
