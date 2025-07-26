package com.scarlxrd.books.model.entity;

public class CpfValidator {
    public static String normalizeCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    public static boolean isValidCPF(String cpf) {
        if (cpf == null) {
            return false;
        }

        cpf = normalizeCpf(cpf);

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstVerifierDigit = 11 - (sum % 11);
        if (firstVerifierDigit >= 10) {
            firstVerifierDigit = 0;
        }

        if (firstVerifierDigit != (cpf.charAt(9) - '0')) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondVerifierDigit = 11 - (sum % 11);
        if (secondVerifierDigit >= 10) {
            secondVerifierDigit = 0;
        }

        return secondVerifierDigit == (cpf.charAt(10) - '0');
    }
}
