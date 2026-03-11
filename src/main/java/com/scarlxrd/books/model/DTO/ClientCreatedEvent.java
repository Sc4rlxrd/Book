package com.scarlxrd.books.model.DTO;

import lombok.Data;

@Data
public class ClientCreatedEvent {

    private String eventType;
    private String cpf;
    private String name;

}
