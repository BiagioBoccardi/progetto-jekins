package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;
import java.util.Set;

public record SignupRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(max = 100) @Email String email,
        @NotBlank @Size(min = 6, max = 120) String password,
        Set<String> role
) {}
