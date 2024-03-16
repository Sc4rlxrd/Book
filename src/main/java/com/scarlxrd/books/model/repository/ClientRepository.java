package com.scarlxrd.books.model.repository;

import com.scarlxrd.books.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client,Long> {
}
