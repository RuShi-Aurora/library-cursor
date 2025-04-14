package com.example.cursorlibrary.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretGenerator {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);
        String secretKey = Base64.getEncoder().encodeToString(secret);
        System.out.println("生成的 JWT secret: " + secretKey);
    }
} 