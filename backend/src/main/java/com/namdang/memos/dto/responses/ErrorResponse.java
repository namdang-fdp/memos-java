package com.namdang.memos.dto.responses;

import lombok.Builder;

@Builder
public record ErrorResponse(int code, String message) {
}
