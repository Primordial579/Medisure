package com.medisure.auth.service;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Argon2PasswordEncoder {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // 64 MB
    private static final int PARALLELISM = 1;

    private final SecureRandom secureRandom = new SecureRandom();

    public String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(rawPassword.toCharArray(), hash);

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        return "$argon2id$v=19$m=" + MEMORY + ",t=" + ITERATIONS + ",p=" + PARALLELISM
                + "$" + saltBase64 + "$" + hashBase64;
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split("\\$");
            // parts: ["", "argon2id", "v=19", "m=65536,t=3,p=1", saltBase64, hashBase64]
            if (parts.length != 6) {
                return false;
            }

            String paramsPart = parts[3];
            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);

            int memory = MEMORY;
            int iterations = ITERATIONS;
            int parallelism = PARALLELISM;
            for (String param : paramsPart.split(",")) {
                String[] kv = param.split("=");
                switch (kv[0]) {
                    case "m" -> memory = Integer.parseInt(kv[1]);
                    case "t" -> iterations = Integer.parseInt(kv[1]);
                    case "p" -> parallelism = Integer.parseInt(kv[1]);
                }
            }

            Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(salt)
                    .withIterations(iterations)
                    .withMemoryAsKB(memory)
                    .withParallelism(parallelism)
                    .build();

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(params);

            byte[] actualHash = new byte[expectedHash.length];
            generator.generateBytes(rawPassword.toCharArray(), actualHash);

            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
