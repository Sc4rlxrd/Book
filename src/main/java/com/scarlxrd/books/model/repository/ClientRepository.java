package com.scarlxrd.books.model.repository;

import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client,Long> {
    Optional<Client> findByCpf(Cpf cpf);
    boolean existsByCpf (Cpf cpf);
}
