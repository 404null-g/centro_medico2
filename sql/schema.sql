CREATE DATABASE IF NOT EXISTS centro_medico CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE centro_medico;

CREATE TABLE especialidades (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre             VARCHAR(60)  NOT NULL UNIQUE,
  descripcion        VARCHAR(200),
  duracion_turno_min INT          NOT NULL,
  activa             TINYINT(1)   NOT NULL DEFAULT 1
);

CREATE TABLE profesionales (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  matricula       VARCHAR(20) NOT NULL UNIQUE,
  dni             VARCHAR(8)  NOT NULL UNIQUE,
  apellido        VARCHAR(60) NOT NULL,
  nombre          VARCHAR(60) NOT NULL,
  telefono        VARCHAR(20),
  email           VARCHAR(100),
  consultorio     VARCHAR(20) NOT NULL,
  dias_atencion   VARCHAR(27) NOT NULL,   -- ej: 'LUN,MIE,VIE'
  hora_desde      TIME        NOT NULL,
  hora_hasta      TIME        NOT NULL,
  activo          TINYINT(1)  NOT NULL DEFAULT 1,
  especialidad_id BIGINT      NOT NULL,
  FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)
);

CREATE TABLE pacientes (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  dni              VARCHAR(8)  NOT NULL UNIQUE,
  apellido         VARCHAR(60) NOT NULL,
  nombre           VARCHAR(60) NOT NULL,
  fecha_nacimiento DATE        NOT NULL,
  sexo             CHAR(1)     NOT NULL,   -- F, M, X
  telefono         VARCHAR(20),
  email            VARCHAR(100),
  domicilio        VARCHAR(120),
  localidad        VARCHAR(60),
  obra_social      VARCHAR(60),            -- NULL = particular
  nro_afiliado     VARCHAR(30),
  activo           TINYINT(1)  NOT NULL DEFAULT 1
);

CREATE TABLE turnos (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  fecha          DATE        NOT NULL,
  hora           TIME        NOT NULL,
  estado         VARCHAR(15) NOT NULL,     -- RESERVADO, EN_ESPERA, ATENDIDO, CANCELADO, AUSENTE
  motivo         VARCHAR(200),
  observaciones  VARCHAR(300),
  creado_en      DATETIME    NOT NULL,
  creado_por     VARCHAR(30) NOT NULL,
  paciente_id    BIGINT      NOT NULL,
  profesional_id BIGINT      NOT NULL,
  FOREIGN KEY (paciente_id)    REFERENCES pacientes(id),
  FOREIGN KEY (profesional_id) REFERENCES profesionales(id),
  INDEX idx_turno_prof_fecha (profesional_id, fecha)
  -- No se define UNIQUE(profesional_id, fecha, hora): un turno CANCELADO libera el horario.
  -- La no superposición de turnos activos se controla en TurnoService (RN-01).
);

CREATE TABLE atenciones (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  turno_id        BIGINT       NOT NULL UNIQUE,   -- una atención por turno
  motivo_consulta VARCHAR(300) NOT NULL,
  diagnostico     VARCHAR(500) NOT NULL,
  indicaciones    VARCHAR(1000),
  observaciones   TEXT,
  fecha_hora      DATETIME     NOT NULL,
  registrada_por  VARCHAR(30)  NOT NULL,
  FOREIGN KEY (turno_id) REFERENCES turnos(id)
);

CREATE TABLE usuarios (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  username        VARCHAR(30)  NOT NULL UNIQUE,
  password        VARCHAR(100) NOT NULL,          -- hash BCrypt
  rol             VARCHAR(15)  NOT NULL,          -- ADMIN, RECEPCION, PROFESIONAL
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  profesional_id  BIGINT       UNIQUE,            -- solo para rol PROFESIONAL
  FOREIGN KEY (profesional_id) REFERENCES profesionales(id)
);
