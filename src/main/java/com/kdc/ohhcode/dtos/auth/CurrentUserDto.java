package com.kdc.ohhcode.dtos.auth;

import com.kdc.ohhcode.entities.enums.Role;

public record CurrentUserDto(
        String firstName,
        String lastName,
        Role role,
        String username
) {}
