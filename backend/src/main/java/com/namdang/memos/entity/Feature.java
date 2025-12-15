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
        name = "feature",
        indexes = {
                @Index(name = "idx_feature_board_id", columnList = "board_id"),
                @Index(name = "idx_feature_board_position", columnList = "board_id, position"),
                @Index(name = "idx_feature_board_archived", columnList = "board_id, is_archived")
        }
)
@SQLDelete(sql = "UPDATE feature SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Feature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @ToString.Exclude
    private Board board;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "position", nullable = false, precision = 20, scale = 10)
    private BigDecimal position;

    @Column(name = "wip_limit")
    private Integer wipLimit;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;
}
