package com.centromedico.turnos;

/**
 * Ciclo de vida de un turno:
 *
 *   RESERVADO --(llega el paciente)--> EN_ESPERA --(el médico lo atiende)--> ATENDIDO
 *       |                                  |
 *       +--> CANCELADO <-------------------+
 *       +--> AUSENTE (no se presentó)
 *
 * Solo RESERVADO y EN_ESPERA ocupan el horario del profesional ("activos").
 */
public enum EstadoTurno {
    RESERVADO,
    EN_ESPERA,
    ATENDIDO,
    CANCELADO,
    AUSENTE;

    public boolean esActivo() {
        return this == RESERVADO || this == EN_ESPERA;
    }

    public boolean puedePasarA(EstadoTurno nuevo) {
        return switch (this) {
            case RESERVADO -> nuevo == EN_ESPERA || nuevo == CANCELADO || nuevo == AUSENTE;
            case EN_ESPERA -> nuevo == ATENDIDO || nuevo == CANCELADO;
            default -> false; // ATENDIDO, CANCELADO y AUSENTE son estados finales
        };
    }
}
