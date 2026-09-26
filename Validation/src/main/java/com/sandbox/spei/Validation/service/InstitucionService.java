package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.Institucion;
import com.sandbox.spei.Validation.repository.InstitucionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstitucionService {

    private final InstitucionRepository institucionRepository;

    public InstitucionService(InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;
    }

    public List<Institucion> obtenerTodasLasInstituciones() {
        return institucionRepository.findAll();
    }

    public Optional<Institucion> obtenerInstitucionPorCodigo(Integer codigo) {
        return institucionRepository.findById(codigo);
    }

    public List<Institucion> obtenerPorEstadoOperativo(String estadoOperativo) {
        return institucionRepository.findByEstadoOperativo(estadoOperativo);
    }

    public boolean puedeEnviarPagos(Integer codigoInstitucion) {
        Optional<Institucion> institucion = institucionRepository.findById(codigoInstitucion);
        return institucion.map(Institucion::getPuedeEnviar).orElse(false);
    }

    public boolean puedeRecibirPagos(Integer codigoInstitucion) {
        Optional<Institucion> institucion = institucionRepository.findById(codigoInstitucion);
        return institucion.map(Institucion::getPuedeRecibir).orElse(false);
    }
}