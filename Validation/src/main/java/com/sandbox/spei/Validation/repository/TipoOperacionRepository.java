package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoOperacionRepository extends JpaRepository<TipoOperacion, String> {

    Optional<TipoOperacion> findByNombre(String nombre);

}