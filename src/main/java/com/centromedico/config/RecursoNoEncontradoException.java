package com.centromedico.config;

/** Se lanza cuando no existe el registro pedido (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String entidad, Long id) {
        super(entidad + " no encontrado/a (id " + id + ")");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
