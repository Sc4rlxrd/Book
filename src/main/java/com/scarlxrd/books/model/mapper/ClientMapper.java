package com.scarlxrd.books.model.mapper;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BookMapper.class, imports = Cpf.class)
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf",expression = "java(new Cpf(dto.getCpfNumber()))")
    Client toEntity(ClientRequestDTO dto);

    @Mapping(target = "cpf", expression = "java(client.getCpf().getNumber())")
    ClientResponseDTO toResponse(Client client);

}
