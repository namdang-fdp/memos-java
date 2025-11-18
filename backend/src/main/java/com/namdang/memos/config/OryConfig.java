package com.namdang.memos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class OryConfig {
    @Value("${ory.public-url}")
    private String publicUrl;
}
