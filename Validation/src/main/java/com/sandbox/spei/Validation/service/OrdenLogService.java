package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.OrdenLog;
import com.sandbox.spei.Validation.repository.OrdenLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrdenLogService {

    private final OrdenLogRepository ordenLogRepository;

    public OrdenLogService(OrdenLogRepository ordenLogRepository) {
        this.ordenLogRepository = ordenLogRepository;
    }

    public OrdenLog guardarLog(OrdenLog ordenLog) {
        return ordenLogRepository.save(ordenLog);
    }

    public List<OrdenLog> obtenerLogsPorOrden(Long ordenPagoId) {
        return ordenLogRepository.findByOrdenPagoId(ordenPagoId);
    }

    public List<OrdenLog> obtenerLogsOrdenadosPorPaso(Long ordenPagoId) {
        return ordenLogRepository.findByOrdenPagoIdOrderByStepAsc(ordenPagoId);
    }

    public List<OrdenLog> obtenerLogsPorClaveRastreo(String cveRastreo) {
        return ordenLogRepository.findByCveRastreo(cveRastreo);
    }

    public Optional<OrdenLog> obtenerLogPorId(Long id) {
        return ordenLogRepository.findById(id);
    }
}