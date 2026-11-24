import { randomBytes, scryptSync, timingSafeEqual } from 'node:crypto'
import { mkdirSync, readFileSync, renameSync, writeFileSync } from 'node:fs'
import { dirname } from 'node:path'

export const permisosPorRol = {
  ADMIN: ['usuarios', 'comercial', 'pedidos', 'despacho', 'indicadores'],
  SUPERVISOR: ['pedidos', 'despacho', 'indicadores'],
  VENDEDOR: ['comercial', 'pedidos', 'crearPedido'],
  REPARTIDOR: ['despacho', 'actualizarEntrega']
}

const cuentas = [
  ['admin', 'Administración', 'ADMIN'],
  ['supervisor', 'Supervisión de operaciones', 'SUPERVISOR'],
  ['vendedor', 'Ventas en terreno', 'VENDEDOR'],
  ['repartidor', 'Distribución Alameda', 'REPARTIDOR']
]

export function prepararClave(clave) {
  const sal = randomBytes(16).toString('hex')
  return { sal, hash: scryptSync(clave, sal, 32).toString('hex') }
}

export function claveValida(clave, usuario) {
  return timingSafeEqual(scryptSync(clave, usuario.sal, 32), Buffer.from(usuario.hash, 'hex'))
}

export function usuarioPublico({ id, nombre, correo, rol, activo }) {
  return { id, nombre, correo, rol, activo, permisos: permisosPorRol[rol] }
}

function datosIniciales() {
  const accesos = cuentas.map(([id, nombre, rol]) => {
    const correo = `${id}@insertcode.test`
    const clave = randomBytes(6).toString('base64url')
    return { clave, usuario: { id, nombre, correo, rol, activo: true, ...prepararClave(clave) } }
  })
  const clientes = [
    { id: 'C-01', nombre: 'Nexo Austral', sucursal: 'Alameda', direccion: 'Punto de prueba Alameda' },
    { id: 'C-02', nombre: 'Mercado Litoral', sucursal: 'Providencia', direccion: 'Punto de prueba Providencia' },
    { id: 'C-03', nombre: 'Trama Comercial', sucursal: 'Las Condes', direccion: 'Punto de prueba Las Condes' }
  ]
  const productos = [
    { id: 'P-01', nombre: 'Panel comercial', categoria: 'Software', descripcion: 'Licencia para consultar ventas e inventario.', precio: 450000 },
    { id: 'P-02', nombre: 'Rastreador de flota', categoria: 'Hardware', descripcion: 'Módulo de seguimiento de vehículos.', precio: 120000 },
    { id: 'P-03', nombre: 'Terminal de despacho', categoria: 'Hardware', descripcion: 'Terminal portátil para confirmar entregas.', precio: 180000 }
  ]
  const pedidos = ['PENDIENTE', 'EN_RUTA', 'ENTREGADO'].map((estado, i) => ({
    id: `PED-00${i + 1}`, clienteId: clientes[i].id, cliente: clientes[i].nombre,
    fecha: new Date().toISOString().slice(0, 10), prioridad: i === 0 ? 'ALTA' : 'NORMAL',
    estado, total: productos[i].precio * (i + 1),
    items: [{ productoId: productos[i].id, nombre: productos[i].nombre, cantidad: i + 1, precio: productos[i].precio }]
  }))
  const entregas = pedidos.map((p, i) => ({
    id: `ENT-00${i + 1}`, pedidoId: p.id, rutaId: 'R-01', orden: i + 1,
    repartidorId: 'repartidor', cliente: p.cliente, direccion: clientes[i].direccion,
    horario: ['09:30–10:00', '10:45–11:15', '12:00–12:30'][i], estado: p.estado,
    latitud: [-33.451, -33.430, -33.413][i], longitud: [-70.668, -70.615, -70.570][i]
  }))
  const rutas = [{
    id: 'R-01', nombre: 'Alameda · Oriente', sucursal: 'Alameda', vehiculo: 'Furgón de prueba 01',
    repartidorId: 'repartidor', repartidor: 'Distribución Alameda', latitud: -33.458, longitud: -70.680
  }]
  return {
    accesos: accesos.map(({ usuario, clave }) => ({ correo: usuario.correo, clave })),
    datos: { usuarios: accesos.map(a => a.usuario), clientes, productos, pedidos, rutas, entregas, metaVentas: 1500000 }
  }
}

export function abrirDatos(archivo) {
  function guardar(datos) {
    mkdirSync(dirname(archivo), { recursive: true })
    writeFileSync(`${archivo}.tmp`, JSON.stringify(datos), { mode: 0o600 })
    renameSync(`${archivo}.tmp`, archivo)
  }

  try {
    return { datos: JSON.parse(readFileSync(archivo, 'utf8')), accesos: [], guardar }
  } catch (error) {
    if (error.code !== 'ENOENT') throw error
    const inicial = datosIniciales()
    guardar(inicial.datos)
    return { ...inicial, guardar }
  }
}
