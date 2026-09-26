package com.sandbox.spei.Validation.service;


import com.sandbox.spei.Validation.entity.Estado;
import com.sandbox.spei.Validation.repository.EstadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoService {

    private final EstadoRepository estadoRepository;

    public EstadoService(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    public List<Estado> obtenerTodosLosEstados() {
        return estadoRepository.findAll();
    }

    public Optional<Estado> obtenerEstadoPorCodigo(Short codigo) {
        return estadoRepository.findById(codigo);
    }

    public Optional<Estado> obtenerEstadoPorCve(String cve) {
        return estadoRepository.findByCve(cve);
    }

    public Optional<Estado> obtenerEstadoPorNombre(String nombre) {
        return estadoRepository.findByNombre(nombre);
    }
}