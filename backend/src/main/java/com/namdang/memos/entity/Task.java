package com.namdang.memos.entity;

import com.namdang.memos.enumType.TaskPriority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(
        name = "task",
        indexes = {
                @Index(name = "idx_task_project_id", columnList = "project_id"),
                @Index(name = "idx_task_feature_id", columnList = "feature_id"),
                @Index(name = "idx_task_feature_position", columnList = "feature_id, position"),
                @Index(name = "idx_task_project_completed_at", columnList = "project_id, completed_at"),
                @Index(name = "idx_task_due_at", columnList = "due_at")
        }
)
@SQLDelete(sql = "UPDATE task SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    @ToString.Exclude
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    private Account createdBy;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private TaskPriority priority;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "position", nullable = false, precision = 20, scale = 10)
    private BigDecimal position;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;
}
