package com.example.cursorlibrary.validator;

import java.util.Arrays;
import java.util.List;

public class RoleValidator {
    private static final List<String> VALID_ROLES = Arrays.asList("USER", "ADMIN");
    
    public static boolean isValidRole(String role) {
        return role == null || VALID_ROLES.contains(role);
    }
} 