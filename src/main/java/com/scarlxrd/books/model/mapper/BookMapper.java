package com.scarlxrd.books.model.mapper;

import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.BookResponseDTO;
import com.scarlxrd.books.model.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponseDTO toResponse(Book book);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    Book toEntity(BookRequestDTO dto);

}
