/* =====================================================================
   app.js — utilidades compartidas por todas las pantallas del Centro Médico
   (sesión, menú, llamadas a la API, formularios, alertas y ABM genérico)
   ===================================================================== */

const API = '/api';
let SESION = null; // { username, rol, nombre, profesionalId }

/* ---------------------------------------------------------------- API */

async function api(ruta, { metodo = 'GET', cuerpo = null } = {}) {
  const opciones = { method: metodo, credentials: 'same-origin', headers: {} };
  if (cuerpo !== null) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(cuerpo);
  }
  const res = await fetch(API + ruta, opciones);
  if (res.status === 401) {                       // sesión vencida o no iniciada
    location.href = 'login.html';
    throw new Error('Sesión no iniciada');
  }
  const texto = await res.text();
  let datos = null;
  if (texto) { try { datos = JSON.parse(texto); } catch (e) { datos = null; } }
  if (!res.ok) {
    const error = new Error((datos && datos.mensaje) || 'Error inesperado (' + res.status + ')');
    error.estado = res.status;
    error.errores = (datos && datos.errores) || null;
    throw error;
  }
  return datos;
}

/* ------------------------------------------------------- sesión y menú */

const MENU = [
  { href: 'index.html',          texto: 'Inicio',        icono: 'bi-house-door',      roles: ['ADMIN', 'RECEPCION', 'PROFESIONAL'] },
  { href: 'turnos.html',         texto: 'Turnos',        icono: 'bi-calendar-check',  roles: ['ADMIN', 'RECEPCION'] },
  { href: 'agenda.html',         texto: 'Agenda',        icono: 'bi-journal-medical', roles: ['ADMIN', 'RECEPCION', 'PROFESIONAL'] },
  { href: 'pacientes.html',      texto: 'Pacientes',     icono: 'bi-people',          roles: ['ADMIN', 'RECEPCION'] },
  { href: 'historia.html',       texto: 'Historias clínicas', icono: 'bi-clipboard2-pulse', roles: ['PROFESIONAL'] },
  { href: 'profesionales.html',  texto: 'Profesionales', icono: 'bi-person-badge',    roles: ['ADMIN', 'RECEPCION'] },
  { href: 'especialidades.html', texto: 'Especialidades', icono: 'bi-bandaid',        roles: ['ADMIN'] },
  { href: 'usuarios.html',       texto: 'Usuarios',      icono: 'bi-shield-lock',     roles: ['ADMIN'] },
];

const ETIQUETA_ROL = { ADMIN: 'Administración', RECEPCION: 'Recepción', PROFESIONAL: 'Profesional' };

/** Verifica la sesión, controla el rol permitido y dibuja el menú. Devuelve los datos del usuario. */
async function iniciarPagina(rolesPermitidos) {
  SESION = await api('/auth/me');
  if (rolesPermitidos && !rolesPermitidos.includes(SESION.rol)) {
    location.href = 'index.html';
    return null;
  }
  const actual = location.pathname.split('/').pop() || 'index.html';
  const enlaces = MENU.filter(m => m.roles.includes(SESION.rol)).map(m =>
    `<li class="nav-item"><a class="nav-link ${m.href === actual ? 'active' : ''}" href="${m.href}">
       <i class="bi ${m.icono}"></i> ${m.texto}</a></li>`).join('');
  document.getElementById('navbar').innerHTML = `
    <nav class="navbar navbar-expand-lg navbar-cm sticky-top">
      <div class="container">
        <a class="navbar-brand fw-bold" href="index.html"><i class="bi bi-heart-pulse-fill"></i> Centro Médico</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuPrincipal"
                aria-controls="menuPrincipal" aria-expanded="false" aria-label="Abrir menú">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuPrincipal">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">${enlaces}</ul>
          <div class="d-flex align-items-center gap-2 text-white">
            <span class="small text-end lh-sm"><strong>${esc(SESION.nombre)}</strong><br>
              <span class="opacity-75">${ETIQUETA_ROL[SESION.rol]}</span></span>
            <button class="btn btn-sm btn-outline-light" id="btnSalir" title="Cerrar sesión">
              <i class="bi bi-box-arrow-right"></i></button>
          </div>
        </div>
      </div>
    </nav>`;
  document.getElementById('btnSalir').addEventListener('click', cerrarSesion);
  return SESION;
}

async function cerrarSesion() {
  try { await fetch(API + '/auth/logout', { method: 'POST', credentials: 'same-origin' }); } catch (e) { /* ignorar */ }
  location.href = 'login.html';
}

/* -------------------------------------------------------------- formatos */

/** Escapa texto antes de insertarlo con innerHTML (evita inyección de HTML/JS). */
function esc(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function hoy() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function sumarDias(iso, dias) {
  const [a, m, d] = iso.split('-').map(Number);
  const f = new Date(a, m - 1, d + dias);
  return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`;
}

function fmtFecha(iso) {
  if (!iso) return '';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

function fmtFechaHora(iso) {
  return iso ? `${fmtFecha(iso)} ${iso.slice(11, 16)} hs` : '';
}

function fmtDiaLargo(iso) {
  const [a, m, d] = iso.split('-').map(Number);
  const t = new Date(a, m - 1, d).toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  return t.charAt(0).toUpperCase() + t.slice(1);
}

const hhmm = t => (t ? String(t).slice(0, 5) : '');

function edad(fechaNacimiento) {
  if (!fechaNacimiento) return '';
  const [a, m, d] = fechaNacimiento.split('-').map(Number);
  const h = new Date();
  let e = h.getFullYear() - a;
  if (h.getMonth() + 1 < m || (h.getMonth() + 1 === m && h.getDate() < d)) e--;
  return e;
}

function textoCobertura(p) {
  return p.obraSocial ? `${esc(p.obraSocial)} <span class="text-muted">· ${esc(p.nroAfiliado)}</span>` : '<span class="text-muted">Particular</span>';
}

const nombreProfesional = p => `Dr/a. ${p.apellido}, ${p.nombre}`;

function badgeEstado(estado) {
  const texto = { RESERVADO: 'Reservado', EN_ESPERA: 'En espera', ATENDIDO: 'Atendido', CANCELADO: 'Cancelado', AUSENTE: 'Ausente' }[estado] || estado;
  return `<span class="estado estado-${esc(estado)}">${texto}</span>`;
}

function badgeActivo(activo) {
  return activo ? '<span class="badge text-bg-success">Activo</span>' : '<span class="badge text-bg-secondary">De baja</span>';
}

/** Reemplaza {campo} y {a.b} por los valores del objeto: interpolar('{apellido}, {nombre}', p) */
function interpolar(plantilla, obj) {
  return plantilla.replace(/\{([\w.]+)\}/g, (_, ruta) => ruta.split('.').reduce((o, k) => (o ? o[k] : ''), obj) ?? '');
}

/* ------------------------------------------------------ alertas y diálogos */

function alerta(mensaje, tipo = 'success') {
  let cont = document.getElementById('toasts');
  if (!cont) {
    cont = document.createElement('div');
    cont.id = 'toasts';
    cont.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    cont.style.zIndex = 2000;
    document.body.appendChild(cont);
  }
  const icono = { success: 'bi-check-circle-fill', danger: 'bi-exclamation-octagon-fill', warning: 'bi-exclamation-triangle-fill' }[tipo] || 'bi-info-circle-fill';
  const el = document.createElement('div');
  el.className = `toast align-items-center text-bg-${tipo} border-0`;
  el.setAttribute('role', 'alert');
  el.innerHTML = `<div class="d-flex"><div class="toast-body"><i class="bi ${icono} me-2"></i>${esc(mensaje)}</div>
    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button></div>`;
  cont.appendChild(el);
  const t = new bootstrap.Toast(el, { delay: tipo === 'danger' ? 7000 : 3500 });
  el.addEventListener('hidden.bs.toast', () => el.remove());
  t.show();
}

/** Diálogo de confirmación. Devuelve una promesa: true si el usuario acepta. */
function confirmar(mensaje, textoBoton = 'Confirmar', claseBoton = 'btn-danger') {
  return new Promise(resolver => {
    const el = document.createElement('div');
    el.className = 'modal fade';
    el.tabIndex = -1;
    el.innerHTML = `<div class="modal-dialog modal-dialog-centered"><div class="modal-content">
      <div class="modal-body pt-4"><p class="mb-0 fs-6">${esc(mensaje)}</p></div>
      <div class="modal-footer border-0"><button class="btn btn-light" data-bs-dismiss="modal">Volver</button>
      <button class="btn ${claseBoton}" id="ok">${esc(textoBoton)}</button></div></div></div>`;
    document.body.appendChild(el);
    const modal = new bootstrap.Modal(el);
    let acepto = false;
    el.querySelector('#ok').addEventListener('click', () => { acepto = true; modal.hide(); });
    el.addEventListener('hidden.bs.modal', () => { el.remove(); resolver(acepto); });
    modal.show();
  });
}

const filaVacia = (columnas, texto) =>
  `<tr><td colspan="${columnas}" class="vacio"><i class="bi bi-inbox fs-3 d-block mb-1"></i>${esc(texto)}</td></tr>`;

/* ------------------------------------------------------------ formularios */

function campos(form) {
  return [...form.elements].filter(e => e.name);
}

/** Convierte un formulario en un objeto JSON. Los nombres con punto arman objetos anidados ("especialidad.id"). */
function leerFormulario(form) {
  const obj = {};
  for (const el of campos(form)) {
    if (el.disabled && el.dataset.enviar === undefined) continue;
    let valor;
    if (el.type === 'checkbox') valor = el.checked;
    else if (el.type === 'number') valor = el.value === '' ? null : Number(el.value);
    else valor = el.value.trim() === '' ? null : el.value.trim();
    const partes = el.name.split('.');
    let destino = obj;
    partes.slice(0, -1).forEach(p => { destino = destino[p] = destino[p] || {}; });
    destino[partes[partes.length - 1]] = valor;
  }
  return obj;
}

function llenarFormulario(form, datos) {
  for (const el of campos(form)) {
    let valor = el.name.split('.').reduce((o, k) => (o == null ? undefined : o[k]), datos);
    if (el.type === 'checkbox') el.checked = !!valor;
    else if (el.type === 'time') el.value = hhmm(valor);
    else el.value = valor ?? '';
  }
}

function limpiarErrores(form) {
  form.classList.remove('was-validated');
  form.querySelectorAll('.is-invalid').forEach(e => e.classList.remove('is-invalid'));
  form.querySelectorAll('.invalid-feedback[data-servidor]').forEach(e => { e.textContent = ''; });
}

/** Marca en rojo los campos que el servidor rechazó. Devuelve true si pudo asignar al menos un error. */
function mostrarErroresServidor(form, errores) {
  let alguno = false;
  for (const [campo, mensaje] of Object.entries(errores || {})) {
    const el = campos(form).find(e => e.name === campo || e.name.startsWith(campo + '.'));
    if (!el) continue;
    el.classList.add('is-invalid');
    let fb = el.parentElement.querySelector('.invalid-feedback');
    if (!fb) { fb = document.createElement('div'); fb.className = 'invalid-feedback'; el.after(fb); }
    fb.dataset.servidor = '1';
    fb.textContent = mensaje;
    alguno = true;
  }
  return alguno;
}

/** Llena los <select data-carga="/ruta" data-texto="{apellido}, {nombre}"> con datos de la API. */
async function cargarSelects(raiz = document) {
  for (const sel of raiz.querySelectorAll('select[data-carga]')) {
    const datos = await api(sel.dataset.carga);
    const primera = sel.querySelector('option[value=""]');
    sel.innerHTML = '';
    sel.appendChild(primera || new Option('Seleccionar…', ''));
    datos.forEach(d => sel.appendChild(new Option(interpolar(sel.dataset.texto, d), d.id)));
  }
}

function antiRebote(fn, ms = 300) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

/* ------------------------------------------------------------ ABM genérico */

/**
 * Arma una pantalla completa de alta/baja/modificación/listado.
 *   cfg.ruta          '/pacientes'
 *   cfg.entidad       'paciente' (para los mensajes)
 *   cfg.filas(item)   devuelve el HTML de las celdas <td> de la fila
 *   cfg.columnas      cantidad de columnas (incluida la de acciones)
 *   cfg.puedeEscribir si false, se ocultan los botones de alta/edición/baja
 *   cfg.alAbrir(item) opcional: se ejecuta al abrir el formulario (item = null si es alta)
 *   cfg.validar()     opcional: devuelve un texto de error (o null) antes de enviar
 */
function crearABM(cfg) {
  const form = document.getElementById('form');
  const modalEl = document.getElementById('modalForm');
  const modal = new bootstrap.Modal(modalEl);
  const tbody = document.getElementById('tbody');
  let editandoId = null;
  let cache = [];

  const esActivo = i => (i.activo !== undefined ? i.activo : i.activa);

  function parametros() {
    const p = new URLSearchParams();
    document.querySelectorAll('[data-filtro]').forEach(el => { if (el.value) p.set(el.dataset.filtro, el.value); });
    const s = p.toString();
    return s ? '?' + s : '';
  }

  async function cargar() {
    tbody.innerHTML = filaVacia(cfg.columnas, 'Cargando…');
    try {
      cache = await api(cfg.ruta + parametros());
    } catch (e) { alerta(e.message, 'danger'); return; }
    if (!cache.length) { tbody.innerHTML = filaVacia(cfg.columnas, 'No hay resultados para mostrar'); return; }
    tbody.innerHTML = cache.map(item => {
      const acciones = cfg.puedeEscribir ? `<td class="text-end text-nowrap">
          <button class="btn btn-sm btn-outline-cm" data-editar="${item.id}" title="Editar"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-sm btn-outline-danger" data-baja="${item.id}" title="Dar de baja" ${esActivo(item) ? '' : 'disabled'}>
            <i class="bi bi-trash"></i></button></td>` : '';
      return `<tr class="${esActivo(item) ? '' : 'text-muted'}">${cfg.filas(item)}${acciones}</tr>`;
    }).join('');
  }

  function abrir(item) {
    editandoId = item ? item.id : null;
    form.reset();
    limpiarErrores(form);
    form.querySelectorAll('.solo-edicion').forEach(e => e.classList.toggle('d-none', !item));
    if (item) llenarFormulario(form, item);
    document.getElementById('tituloModal').textContent = item ? `Editar ${cfg.entidad}` : `Nuevo ${cfg.entidad}`;
    if (cfg.alAbrir) cfg.alAbrir(item);
    modal.show();
  }

  async function guardar() {
    limpiarErrores(form);
    if (!form.checkValidity()) { form.classList.add('was-validated'); return; }
    const problema = cfg.validar ? cfg.validar() : null;   // validaciones propias de cada pantalla
    if (problema) { alerta(problema, 'warning'); return; }
    const boton = document.getElementById('btnGuardar');
    boton.disabled = true;
    try {
      const cuerpo = leerFormulario(form);
      if (editandoId) await api(`${cfg.ruta}/${editandoId}`, { metodo: 'PUT', cuerpo });
      else await api(cfg.ruta, { metodo: 'POST', cuerpo });
      modal.hide();
      alerta(editandoId ? 'Cambios guardados' : `${cfg.entidad.charAt(0).toUpperCase() + cfg.entidad.slice(1)} creado/a correctamente`);
      await cargar();
    } catch (e) {
      const marcados = mostrarErroresServidor(form, e.errores);
      alerta(marcados ? 'Revise los campos marcados' : e.message, 'danger');
    } finally { boton.disabled = false; }
  }

  async function darDeBaja(id) {
    if (!await confirmar(`¿Dar de baja ${cfg.entidad === 'usuario' ? 'al' : 'a este/a'} ${cfg.entidad}? Podrá reactivarlo/a editando el registro.`, 'Dar de baja')) return;
    try {
      await api(`${cfg.ruta}/${id}`, { metodo: 'DELETE' });
      alerta('Registro dado de baja');
      await cargar();
    } catch (e) { alerta(e.message, 'danger'); }
  }

  tbody.addEventListener('click', ev => {
    const b = ev.target.closest('button');
    if (!b) return;
    if (b.dataset.editar) abrir(cache.find(i => i.id == b.dataset.editar));
    if (b.dataset.baja) darDeBaja(b.dataset.baja);
  });
  document.getElementById('btnGuardar').addEventListener('click', guardar);
  form.addEventListener('submit', ev => { ev.preventDefault(); guardar(); });
  document.querySelectorAll('[data-filtro]').forEach(el =>
    el.addEventListener(el.tagName === 'SELECT' ? 'change' : 'input', antiRebote(cargar)));
  const btnNuevo = document.getElementById('btnNuevo');
  if (cfg.puedeEscribir) btnNuevo.addEventListener('click', () => abrir(null)); else btnNuevo.classList.add('d-none');

  cargar();
  return { recargar: cargar };
}
