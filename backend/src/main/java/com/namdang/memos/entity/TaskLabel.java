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
        name = "task_label",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_task_label_task_label", columnNames = {"task_id", "label_id"})
        },
        indexes = {
                @Index(name = "idx_task_label_task_id", columnList = "task_id"),
                @Index(name = "idx_task_label_label_id", columnList = "label_id")
        }
)
public class TaskLabel {

    @EmbeddedId
    private TaskLabelId id = new TaskLabelId();

    @MapsId("taskId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @MapsId("labelId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    @ToString.Exclude
    private Label label;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TaskLabelId implements Serializable {

        @Column(name = "task_id", nullable = false)
        private UUID taskId;

        @Column(name = "label_id", nullable = false)
        private UUID labelId;
    }
}
