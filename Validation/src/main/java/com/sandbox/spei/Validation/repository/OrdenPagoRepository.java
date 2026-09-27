package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Long> {

    Optional<OrdenPago> findByClaveRastreo(String claveRastreo);

    boolean existsByClaveRastreo(String claveRastreo);

    List<OrdenPago> findByCuentaOrdenante(String cuentaOrdenante);

    List<OrdenPago> findByCuentaBeneficiaria(String cuentaBeneficiaria);

    List<OrdenPago> findByEstadoActualCodigo(Short codigoEstado);

    // Tipo corregido de Integer a String
    List<OrdenPago> findByTipoOperacionCodigo(String codigoTipoOperacion);
}