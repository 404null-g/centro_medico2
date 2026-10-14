-- Usuarios de prueba:
--   admin / Admin2026!    recepcion1 / Recep2026!    mlopez / Medico2026!    cferrero / Medico2026!
USE centro_medico;

INSERT INTO especialidades (id, nombre, descripcion, duracion_turno_min, activa) VALUES
 (1, 'Clínica Médica', 'Control general y primera consulta', 20, 1),
 (2, 'Pediatría', 'Atención de niños y adolescentes', 20, 1),
 (3, 'Cardiología', 'Corazón y sistema circulatorio', 30, 1),
 (4, 'Dermatología', 'Piel, pelo y uñas', 20, 1),
 (5, 'Traumatología', 'Huesos, articulaciones y lesiones', 30, 1),
 (6, 'Ginecología', 'Salud de la mujer', 30, 1);

INSERT INTO profesionales (id, matricula, dni, apellido, nombre, telefono, email, consultorio, dias_atencion, hora_desde, hora_hasta, activo, especialidad_id) VALUES
 (1, 'MP-31254', '22458731', 'López',    'Martín',   '3815550101', 'mlopez@centromedico.example',   'Consultorio 3', 'LUN,MIE,VIE',         '08:00:00', '13:00:00', 1, 3),
 (2, 'MP-28977', '24119832', 'Ferrero',  'Carolina', '3815550102', 'cferrero@centromedico.example', 'Consultorio 1', 'LUN,MAR,MIE,JUE,VIE', '14:00:00', '18:00:00', 1, 1),
 (3, 'MP-35412', '27663410', 'Quiroga',  'Sebastián','3815550103', 'squiroga@centromedico.example', 'Consultorio 2', 'MAR,JUE',             '09:00:00', '13:00:00', 1, 2),
 (4, 'MP-30871', '26117954', 'Benítez',  'Valeria',  '3815550104', 'vbenitez@centromedico.example', 'Consultorio 4', 'LUN,MIE',             '14:00:00', '18:00:00', 1, 4),
 (5, 'MP-33120', '28904417', 'Aguirre',  'Tomás',    '3815550105', 'taguirre@centromedico.example', 'Consultorio 5', 'MAR,JUE,VIE',         '08:00:00', '12:00:00', 1, 5),
 (6, 'MP-29645', '25330876', 'Salinas',  'Mariana',  '3815550106', 'msalinas@centromedico.example', 'Consultorio 6', 'LUN,JUE',             '14:00:00', '18:00:00', 1, 6);

INSERT INTO pacientes (id, dni, apellido, nombre, fecha_nacimiento, sexo, telefono, email, domicilio, localidad, obra_social, nro_afiliado, activo) VALUES
 (1, '30112456', 'Paz',       'Lucía',     '1983-05-14', 'F', '3815551001', 'lucia.paz@example.com',   'Av. Mate de Luna 1250', 'San Miguel de Tucumán', 'OSDE',          '30112456-01', 1),
 (2, '18455920', 'Herrera',   'Roberto',   '1956-11-02', 'M', '3815551002', NULL,                      'Lavalle 340',           'Yerba Buena',           'PAMI',          '15-18455920-3', 1),
 (3, '45870231', 'Costa',     'Julieta',   '2014-03-27', 'F', '3815551003', 'familia.costa@example.com','Santiago 880',          'San Miguel de Tucumán', 'Swiss Medical', '45870231-02', 1),
 (4, '36004789', 'Navarro',   'Federico',  '1991-08-09', 'M', '3815551004', 'fnavarro@example.com',    'Muñecas 215',           'Tafí Viejo',            NULL,            NULL, 1),
 (5, '27341865', 'Ibáñez',    'Gabriela',  '1979-01-30', 'F', '3815551005', 'gibanez@example.com',     'Bolívar 1010',          'San Miguel de Tucumán', 'OSECAC',        '27341865-00', 1),
 (6, '41230987', 'Rojas',     'Agustín',   '2000-12-18', 'M', '3815551006', NULL,                      'Av. Belgrano 2200',     'Banda del Río Salí',    NULL,            NULL, 1),
 (7, '23765410', 'Medina',    'Silvia',    '1974-07-21', 'F', '3815551007', 'smedina@example.com',     'Junín 450',             'San Miguel de Tucumán', 'IOMA',          '23765410-04', 1),
 (8, '52118342', 'Villalba',  'Emma',      '2019-09-05', 'F', '3815551008', 'villalba.fam@example.com','Corrientes 97',          'Yerba Buena',           'OSDE',          '52118342-03', 1);

INSERT INTO usuarios (id, username, password, rol, activo, profesional_id) VALUES
 (1, 'admin',      '$2b$10$dButdtn.x5JQfRsiUzsgQu8geCv0gNdoR9The7ASNqMDige4kVQuu', 'ADMIN',      1, NULL),
 (2, 'recepcion1', '$2b$10$fbx6/E3bnO6ZwztPaTOXeOENDTLwg02Q1xJ9FcLm6stjNBSCGooCS', 'RECEPCION',  1, NULL),
 (3, 'mlopez',     '$2b$10$nxd99/u9MqxSe5nZU7eQYejkY/4rcwYtKOn8aF.EUQfxSvW9u4m8.', 'PROFESIONAL', 1, 1),
 (4, 'cferrero',   '$2b$10$93ULKgVCIkMAAbdYWKLGzOZvsLt0eUNIe.t/JiC8prITj7XKAD4vK', 'PROFESIONAL', 1, 2);
