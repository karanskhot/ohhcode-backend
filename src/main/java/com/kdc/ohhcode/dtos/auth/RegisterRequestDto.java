package com.kdc.ohhcode.dtos.auth;

import com.kdc.ohhcode.entities.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "first name is required")
        @Size(max = 30, message = "first name cannot exceed 50 characters.")
        String firstName,

        @NotBlank(message = "last name is required")
        @Size(max = 30, message = "last name cannot exceed 50 characters.")
        String lastName,

        @NotBlank(message = "username is required")
        @Email(message = "Please provide a valid email address", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must be alphanumeric and contain at least one special character"
        )
        String password,
        Role role

) {
}
