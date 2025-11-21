package com.namdang.memos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
public class Project extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String projectKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    private Account createdBy;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;
}
