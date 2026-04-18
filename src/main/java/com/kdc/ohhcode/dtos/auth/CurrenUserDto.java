package com.kdc.ohhcode.dtos.auth;

import com.kdc.ohhcode.entities.enums.Role;

public record CurrenUserDto(
        String firstName,
        String lastName,
        Role role,
        String username
) {}
