package com.sandbox.spei.Validation.repository;

import com.sandbox.spei.Validation.entity.OrdenLog;
import com.sandbox.spei.Validation.entity.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenLogRepository extends JpaRepository<OrdenLog, Long> {

    List<OrdenLog> findByOrdenPagoId(Long ordenPagoId);

    List<OrdenLog> findByCveRastreo(String cveRastreo);

    List<OrdenLog> findByOrdenPagoIdOrderByStepAsc(Long ordenPagoId);

    // Método necesario para ordenar los logs por clave de rastreo
    List<OrdenLog> findByCveRastreoOrderByStepAsc(String cveRastreo);

    List<OrdenLog> findByOrdenPagoOrderByStepAsc(OrdenPago ordenPago);
}