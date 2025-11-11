package com.scarlxrd.books.model.repository;

import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByCpf(Cpf cpf);
    boolean existsByCpf (Cpf cpf);
    @Query("SELECT c FROM Client c JOIN FETCH c.books")
    List<Client> findAllWithBooks();
}
