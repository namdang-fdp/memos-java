package com.namdang.memos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@DynamicUpdate
@Table(
        name = "task_assignee",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_task_assignee_task_account", columnNames = {"task_id", "account_id"})
        },
        indexes = {
                @Index(name = "idx_task_assignee_task_id", columnList = "task_id"),
                @Index(name = "idx_task_assignee_account_id", columnList = "account_id")
        }
)
public class TaskAssignee {

    @EmbeddedId
    private TaskAssigneeId id = new TaskAssigneeId();

    @MapsId("taskId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    private Account account;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    @ToString.Exclude
    private Account assignedBy;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TaskAssigneeId implements Serializable {

        @Column(name = "task_id", nullable = false)
        private UUID taskId;

        @Column(name = "account_id", nullable = false)
        private UUID accountId;
    }
}
