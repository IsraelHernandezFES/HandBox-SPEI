package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.TipoOperacion;
import com.sandbox.spei.Validation.repository.TipoOperacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoOperacionService {

    private final TipoOperacionRepository tipoOperacionRepository;

    public TipoOperacionService(TipoOperacionRepository tipoOperacionRepository) {
        this.tipoOperacionRepository = tipoOperacionRepository;
    }

    public List<TipoOperacion> obtenerTodosLosTiposOperacion() {
        return tipoOperacionRepository.findAll();
    }

    public Optional<TipoOperacion> obtenerTipoOperacionPorCodigo(String codigo) {
        return tipoOperacionRepository.findById(codigo);
    }

    public Optional<TipoOperacion> obtenerTipoOperacionPorNombre(String nombre) {
        return tipoOperacionRepository.findByNombre(nombre);
    }
}