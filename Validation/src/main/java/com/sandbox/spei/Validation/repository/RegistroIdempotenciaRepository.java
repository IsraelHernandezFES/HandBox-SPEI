package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.RegistroIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroIdempotenciaRepository extends JpaRepository<RegistroIdempotencia, String> {
}