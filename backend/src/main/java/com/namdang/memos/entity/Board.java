package com.namdang.memos.entity;

import com.namdang.memos.enumType.BoardType;
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
        name = "board",
        indexes = {
                @Index(name = "idx_board_project_id", columnList = "project_id"),
                @Index(name = "idx_board_project_archived", columnList = "project_id, is_archived")
        }
)
public class Board extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 20)
    private BoardType type = BoardType.KANBAN;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;
}
