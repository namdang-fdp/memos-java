package com.namdang.memos.enumType;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PreDefinedRole {
    ADMIN("admin"),
    MEMBER("member");

    private final String jsonValue;

    @JsonValue
    @Override
    public String toString() {
        return jsonValue;
    }

    PreDefinedRole(String jsonValue) {
        this.jsonValue = jsonValue;
    }
}
