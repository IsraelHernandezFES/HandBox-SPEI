package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.Institucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitucionRepository extends JpaRepository<Institucion, Long> {

    Optional<Institucion> findByNombre(String nombre);

    List<Institucion> findByEstadoOperativo(String estadoOperativo);

    List<Institucion> findByPuedeEnviarTrue();

    List<Institucion> findByPuedeRecibirTrue();
}