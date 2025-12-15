package com.namdang.memos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(
        name = "task_comment",
        indexes = {
                @Index(name = "idx_task_comment_task_created", columnList = "task_id, created_at"),
                @Index(name = "idx_task_comment_author", columnList = "author_id")
        }
)
@SQLDelete(sql = "UPDATE task_comment SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class TaskComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private Account author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;
}
