package com.namdang.memos.dto.requests.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {
    String token;
}
