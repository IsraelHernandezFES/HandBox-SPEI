package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.OrdenPago;
import com.sandbox.spei.Validation.repository.OrdenPagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrdenPagoService {


    private final OrdenPagoRepository ordenPagoRepository;

    public OrdenPagoService(OrdenPagoRepository ordenPagoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
    }

    public List<OrdenPago> obtenerTodasLasOrdenes() {
        return ordenPagoRepository.findAll();
    }

    public Optional<OrdenPago> obtenerOrdenPorId(Long id) {
        return ordenPagoRepository.findById(id);
    }

    public Optional<OrdenPago> obtenerOrdenPorClaveRastreo(String claveRastreo) {
        return ordenPagoRepository.findByClaveRastreo(claveRastreo);
    }

    public OrdenPago guardarOrdenPago(OrdenPago ordenPago) {
        return ordenPagoRepository.save(ordenPago);
    }
}