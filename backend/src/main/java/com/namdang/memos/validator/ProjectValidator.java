package com.namdang.memos.validator;

import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;

public class ProjectValidator {
    public void validatorProjectName(String projectName) {
        if(projectName == null) {
            throw new AppException(ErrorCode.INVALID_PROJECT_NAME);
        }
        String projectTrimmed = projectName.trim();
        if(projectTrimmed.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PROJECT_NAME);
        }
        int projectLength = projectTrimmed.length();
        if(projectLength < 3 || projectLength > 50) {
            throw new AppException(ErrorCode.INVALID_PROJECT_NAME);
        }
        if (!projectTrimmed.matches(".*\\p{L}.*")) {
            throw new AppException(ErrorCode.INVALID_PROJECT_NAME);
        }
        if (!projectTrimmed.matches("[\\p{L}\\p{N} _\\-]+")) {
            throw new AppException(ErrorCode.INVALID_PROJECT_NAME);
        }
    }
}
