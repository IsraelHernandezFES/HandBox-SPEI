package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.OrdenLog;
import com.sandbox.spei.Validation.dto.response.OrdenLogResponse;
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

    public List<OrdenLogResponse> obtenerLogsPorOrden(Long ordenPagoId) {
        return ordenLogRepository.findByOrdenPagoId(ordenPagoId)
                .stream().map(this::mapearLog).toList();
    }

    public List<OrdenLogResponse> obtenerLogsOrdenadosPorPaso(Long ordenPagoId) {
        return ordenLogRepository.findByOrdenPagoIdOrderByStepAsc(ordenPagoId)
                .stream().map(this::mapearLog).toList();
    }

    public List<OrdenLogResponse> obtenerLogsPorClaveRastreo(String cveRastreo) {
        return ordenLogRepository.findByCveRastreoOrderByStepAsc(cveRastreo)
                .stream().map(this::mapearLog).toList();
    }

    public Optional<OrdenLog> obtenerLogPorId(Long id) {
        return ordenLogRepository.findById(id);
    }

    // Método centralizado para mapear de Entidad a DTO
    private OrdenLogResponse mapearLog(OrdenLog log) {
        return OrdenLogResponse.builder()
                .id(log.getId())
                .step(log.getStep())
                .cveRastreo(log.getCveRastreo())
                .estado(log.getEstado() != null ? log.getEstado().getCve() : null)
                .horaProcesamiento(log.getHoraProcesamiento())
                .motivo(log.getMotivo())
                .detalle(log.getDetalle())
                .build();
    }
}