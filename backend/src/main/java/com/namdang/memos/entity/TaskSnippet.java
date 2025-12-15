package com.namdang.memos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(
        name = "task_snippet",
        indexes = {
                @Index(name = "idx_task_snippet_task_id", columnList = "task_id"),
                @Index(name = "idx_task_snippet_task_position", columnList = "task_id, position"),
                @Index(name = "idx_task_snippet_language", columnList = "language")
        }
)
@SQLDelete(sql = "UPDATE task_snippet SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class TaskSnippet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    private Account createdBy;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "position", nullable = false, precision = 20, scale = 10)
    private BigDecimal position;
}
