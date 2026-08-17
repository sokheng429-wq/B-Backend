package com.bgroceries.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    /** One of: gmail | telegram | facebook */
    @NotBlank(message = "Provider is required")
    private String provider;

    /** The user's email (gmail), telegram handle or facebook handle. */
    @NotBlank(message = "Identifier is required")
    private String identifier;
}
