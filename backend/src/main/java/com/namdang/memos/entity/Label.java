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
@Table(
        name = "label",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_label_project_name", columnNames = {"project_id", "name"})
        },
        indexes = {
                @Index(name = "idx_label_project_id", columnList = "project_id")
        }
)
public class Label extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "color", length = 30)
    private String color;
}
