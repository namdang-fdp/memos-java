package com.namdang.memos.entity;

import com.namdang.memos.enumType.TaskActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(
        name = "task_activity",
        indexes = {
                @Index(name = "idx_task_activity_project_created", columnList = "project_id, created_at"),
                @Index(name = "idx_task_activity_task_created", columnList = "task_id, created_at"),
                @Index(name = "idx_task_activity_actor_created", columnList = "actor_id, created_at"),
                @Index(name = "idx_task_activity_type", columnList = "activity_type")
        }
)
public class TaskActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @ToString.Exclude
    private Account actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private TaskActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_feature_id")
    @ToString.Exclude
    private Feature fromFeature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_feature_id")
    @ToString.Exclude
    private Feature toFeature;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;
}
