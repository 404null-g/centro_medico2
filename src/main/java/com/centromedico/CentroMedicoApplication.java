package com.centromedico;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class CentroMedicoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentroMedicoApplication.class, args);
    }

    /** Los turnos se calculan con la hora local del centro (evita desfases si el servidor está en UTC). */
    @PostConstruct
    void zonaHoraria() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }
}
