package com.scarlxrd.books.model.DTO;

import com.scarlxrd.books.model.entity.Client;

public record BookDTO(String name, String description, String gender, String year, Long id, Client client) {
}
