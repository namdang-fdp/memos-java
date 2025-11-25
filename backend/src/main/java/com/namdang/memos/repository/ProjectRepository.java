package com.namdang.memos.repository;

import com.namdang.memos.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    boolean existsByProjectKey(String projectKey);

    List<Project> findByArchivedFalse();
}
