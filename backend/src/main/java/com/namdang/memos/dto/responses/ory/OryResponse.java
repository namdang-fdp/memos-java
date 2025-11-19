package com.namdang.memos.dto.responses.ory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OryResponse {

    private String id;
    private boolean active;

    @JsonProperty("authenticated_at")
    private OffsetDateTime authenticatedAt;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;

    @JsonProperty("issued_at")
    private OffsetDateTime issuedAt;

    @JsonProperty("authenticator_assurance_level")
    private String authenticatorAssuranceLevel;

    @JsonProperty("authentication_methods")
    private List<AuthenticationMethod> authenticationMethods;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthenticationMethod {
        private String method;
        private String provider;

        @JsonProperty("completed_at")
        private OffsetDateTime completedAt;

        @JsonProperty("aal")
        private String aal;
    }

    private Identity identity;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identity {

        private String id;  // identity id trong Ory

        @JsonProperty("schema_id")
        private String schemaId;

        @JsonProperty("schema_url")
        private String schemaUrl;

        private String state; // active / inactive

        @JsonProperty("state_changed_at")
        private OffsetDateTime stateChangedAt;

        private Traits traits;

        @JsonProperty("metadata_public")
        private Map<String, Object> metadataPublic;

        @JsonProperty("verifiable_addresses")
        private List<VerifiableAddress> verifiableAddresses;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Traits {
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiableAddress {
        private String id;
        private String value;
        private String status;

        @JsonProperty("verified")
        private boolean verified;

        @JsonProperty("verified_at")
        private OffsetDateTime verifiedAt;

        private String via;
    }
}

