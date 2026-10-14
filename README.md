# Centro Médico — sistema de turnos e historias clínicas

Java 21 · Spring Boot 3.3 · Maven · MySQL 8 · HTML/CSS/Bootstrap 5

## Requisitos
- JDK 21
- Maven 3.9+ (o usar el que trae el IDE)
- MySQL 8 en marcha en `localhost:3306`
- Conexión a internet la primera vez (Maven descarga dependencias; las pantallas usan Bootstrap desde CDN)

## Instalación
1. Descomprimir la carpeta `centro-medico`.
2. Definir las credenciales de MySQL (si no se definen, usa usuario `root` y clave vacía):
   - Windows (PowerShell): `$env:DB_USER="root"; $env:DB_PASSWORD="tu_clave"`
   - Linux / macOS: `export DB_USER=root DB_PASSWORD=tu_clave`
3. Iniciar la aplicación desde la carpeta del proyecto:
   ```
   mvn spring-boot:run
   ```
   La primera vez Hibernate crea la base `centro_medico` y todas las tablas.
4. Cargar los datos de ejemplo (una sola vez, con la app ya iniciada al menos una vez):
   ```
   mysql -u root -p centro_medico < sql/datos_prueba.sql
   ```
5. Abrir http://localhost:8080 e ingresar.

## Usuarios de prueba
| Usuario | Contraseña | Rol |
|---|---|---|
| admin | Admin2026! | Administración |
| recepcion1 | Recep2026! | Recepción |
| mlopez | Medico2026! | Profesional (Cardiología, lun-mié-vie 08-13) |
| cferrero | Medico2026! | Profesional (Clínica Médica, lun a vie 14-18) |

Son solo para pruebas: cambiarlas antes de cualquier uso real.

## Prueba rápida
1. Entrar como `recepcion1` → Turnos → Nuevo turno → buscar "Paz" → elegir Dr. López, un lunes/miércoles/viernes futuro y un horario libre.
2. El día del turno: marcar la llegada (ícono de persona con tilde) → pasa a *En espera*.
3. Salir, entrar como `mlopez` → Agenda → *Atender* → completar y guardar → el turno queda *Atendido*.
4. En *Historias clínicas* buscar al paciente y ver la atención.

## Estructura
```
src/main/java/com/centromedico/
  config/        manejo de errores y seguridad
  pacientes/  especialidades/  profesionales/  turnos/  atenciones/  usuarios/
src/main/resources/static/   pantallas HTML, css/ y js/
sql/            schema.sql (referencia) y datos_prueba.sql
docs/           documentación del proyecto y diagramas
```

## Problemas frecuentes
- *Access denied for user*: revisar `DB_USER` / `DB_PASSWORD`.
- *Puerto 8080 ocupado*: agregar `server.port=8081` en `application.properties`.
- Los horarios de los turnos usan la zona `America/Argentina/Buenos_Aires` (se fija al iniciar).
