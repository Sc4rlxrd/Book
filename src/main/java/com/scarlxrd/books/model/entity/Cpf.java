package com.scarlxrd.books.model.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Cpf implements Serializable {

    @Column(name = "cpf_number", unique = true, length = 11, nullable = false)
    private String number;

    public Cpf() {}

    public Cpf(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio.");
        }
        String normalizedCpf = CpfValidator.normalizeCpf(number);
        if (!CpfValidator.isValidCPF(normalizedCpf)) {
            throw new IllegalArgumentException("CPF inválido: " + number);
        }
        this.number = normalizedCpf;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio.");
        }
        String normalizedCpf = CpfValidator.normalizeCpf(number);
        if (!CpfValidator.isValidCPF(normalizedCpf)) {
            throw new IllegalArgumentException("CPF inválido: " + number);
        }
        this.number = normalizedCpf;
    }

    @Override
    @JsonValue
    public String toString() {
        if (number == null || number.length() != 11) {
            return number;
        }
        return number.substring(0, 3) + "." +
                number.substring(3, 6) + "." +
                number.substring(6, 9) + "-" +
                number.substring(9, 11);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cpf cpf1 = (Cpf) o;
        return Objects.equals(number, cpf1.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}

