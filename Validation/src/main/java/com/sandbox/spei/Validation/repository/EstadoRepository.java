package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Short> {

    Optional<Estado> findByCve(String cve);

    Optional<Estado> findByNombre(String nombre);

}