package com.diginexa.bitacora.exceptions.domain;

public class TutorNoAsignadoException extends RuntimeException {

    public TutorNoAsignadoException(Integer estudianteId) {
        super("El estudiante con ID " + estudianteId + " no tiene un tutor asignado");
    }

    public TutorNoAsignadoException(String message) {
        super(message);
    }
}
