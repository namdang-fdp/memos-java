package com.namdang.memos.entity

import java.time.LocalDate;

import com.namdang.memos.enumType.FeatureStatus;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@DynamicUpdate
@Table(name = "feature")
public class Feature extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FeatureStatus status = FeatureStatus.PLAN;

    private LocalDate startDate;

    private LocalDate endDate;
}
