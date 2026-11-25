import { createServer } from 'node:http'
import { randomBytes, randomUUID } from 'node:crypto'
import { resolve } from 'node:path'
import { readFileSync } from 'node:fs'
import { pathToFileURL } from 'node:url'
import { abrirDatos, claveValida, permisosDe, permisosPorRol, prepararClave, usuarioPublico } from './datos.mjs'
import { crearAccesoGoogle, ErrorGoogle } from './google.mjs'

class ErrorApi extends Error {
  constructor(estado, mensaje) {
    super(mensaje)
    this.estado = estado
  }
}

function exigir(condicion, estado, mensaje) {
  if (!condicion) throw new ErrorApi(estado, mensaje)
}

function responder(respuesta, estado, datos) {
  respuesta.writeHead(estado, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' })
  respuesta.end(JSON.stringify(datos))
}

async function leerCuerpo(peticion) {
  let bytes = 0
  const partes = []
  for await (const parte of peticion) {
    bytes += parte.length
    exigir(bytes <= 16_384, 413, 'La solicitud es demasiado grande.')
    partes.push(parte)
  }
  try {
    const datos = JSON.parse(Buffer.concat(partes).toString('utf8'))
    exigir(datos && typeof datos === 'object' && !Array.isArray(datos), 400, 'Usa un objeto JSON.')
    return datos
  } catch {
    throw new ErrorApi(400, 'Revisa los datos enviados.')
  }
}

function ventasDe(pedidos) {
  return pedidos.filter(p => p.estado === 'ENTREGADO').map(p => ({
    id: p.id, cliente: p.cliente, fecha: p.fecha, total: p.total
  }))
}

function resumen(datos, usuario, permisos) {
  const comercial = permisos.includes('comercial')
  const despacho = permisos.includes('despacho')
  const repartidor = usuario.rol === 'REPARTIDOR'
  const ventas = ventasDe(datos.pedidos)
  return {
    clientes: comercial ? datos.clientes : [],
    productos: comercial ? datos.productos : [],
    pedidos: permisos.includes('pedidos') ? datos.pedidos : [],
    ventas: comercial ? ventas : [],
    rutas: despacho ? datos.rutas.filter(r => !repartidor || r.repartidorId === usuario.id) : [],
    entregas: despacho ? datos.entregas.filter(e => !repartidor || e.repartidorId === usuario.id) : [],
    indicadores: permisos.includes('indicadores') ? {
      ventas: ventas.reduce((total, venta) => total + venta.total, 0),
      meta: datos.metaVentas,
      pendientes: datos.pedidos.filter(p => p.estado === 'PENDIENTE').length,
      enRuta: datos.pedidos.filter(p => p.estado === 'EN_RUTA').length,
      entregados: datos.pedidos.filter(p => p.estado === 'ENTREGADO').length,
      entregasRealizadas: datos.entregas.filter(e => e.estado === 'ENTREGADO').length,
      entregasPendientes: datos.entregas.filter(e => e.estado !== 'ENTREGADO').length
    } : null
  }
}

function crearPedido(datos, solicitud, usuario) {
  exigir(/^[a-f0-9-]{36}$/i.test(solicitud.idOperacion ?? ''), 400, 'Falta el identificador del pedido.')
  const existente = datos.pedidos.find(p => p.operacion === solicitud.idOperacion && p.vendedorId === usuario.id)
  const cliente = datos.clientes.find(c => c.id === solicitud.clienteId)
  const ruta = datos.rutas.find(r => r.id === solicitud.rutaId)
  exigir(cliente && ruta, 422, 'Selecciona un cliente y una ruta válidos.')
  const fecha = solicitud.fecha
  const fechaJs = typeof fecha === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(fecha) ? new Date(fecha) : null
  exigir(fechaJs && Number.isFinite(+fechaJs) && fechaJs.toISOString().slice(0, 10) === fecha, 422, 'Ingresa una fecha válida.')
  exigir(['NORMAL', 'ALTA'].includes(solicitud.prioridad), 422, 'Selecciona la prioridad.')
  exigir(Array.isArray(solicitud.items) && solicitud.items.length > 0 && solicitud.items.length <= datos.productos.length, 422, 'Selecciona al menos un producto.')
  const ids = new Set()
  const items = solicitud.items.map(item => {
    const producto = datos.productos.find(p => p.id === item?.productoId)
    exigir(producto && !ids.has(producto.id) && Number.isInteger(item.cantidad) && item.cantidad >= 1 && item.cantidad <= 99, 422, 'Revisa los productos y sus cantidades.')
    ids.add(producto.id)
    return { productoId: producto.id, nombre: producto.nombre, cantidad: item.cantidad, precio: producto.precio }
  })
  if (existente) {
    const entrega = datos.entregas.find(e => e.pedidoId === existente.id)
    const mismoPedido = existente.clienteId === cliente.id && entrega?.rutaId === ruta.id &&
      existente.fecha === fecha && existente.prioridad === solicitud.prioridad &&
      existente.items.length === items.length &&
      items.every(item => existente.items.some(i => i.productoId === item.productoId && i.cantidad === item.cantidad))
    exigir(mismoPedido, 409, 'El pedido original ya quedó registrado. Revísalo en Pedidos.')
    return existente
  }
  const id = `PED-${String(datos.pedidos.length + 1).padStart(3, '0')}`
  const pedido = {
    id, clienteId: cliente.id, cliente: cliente.nombre, fecha, prioridad: solicitud.prioridad,
    estado: 'PENDIENTE', total: items.reduce((suma, item) => suma + item.cantidad * item.precio, 0),
    items, operacion: solicitud.idOperacion, vendedorId: usuario.id
  }
  const orden = datos.entregas.filter(e => e.rutaId === ruta.id).length + 1
  datos.pedidos.push(pedido)
  datos.entregas.push({
    id: id.replace('PED-', 'ENT-'), pedidoId: id, rutaId: ruta.id, orden, repartidorId: ruta.repartidorId,
    cliente: cliente.nombre, direccion: cliente.direccion, horario: 'Por programar',
    estado: pedido.estado, latitud: ruta.latitud + Math.min(orden, 18) * 0.002,
    longitud: ruta.longitud + Math.min(orden, 18) * 0.004
  })
  return pedido
}

function cambiarEntrega(datos, usuario, id, estado) {
  const entrega = datos.entregas.find(e => e.id === id && e.repartidorId === usuario.id)
  exigir(entrega, 404, 'La entrega no está asignada a esta cuenta.')
  if (entrega.estado === estado) return entrega
  const siguiente = { PENDIENTE: 'EN_RUTA', EN_RUTA: 'ENTREGADO' }
  exigir(siguiente[entrega.estado] && siguiente[entrega.estado] === estado, 409, 'El estado de entrega cambió. Actualiza la información.')
  entrega.estado = estado
  datos.pedidos.find(p => p.id === entrega.pedidoId).estado = estado
  return entrega
}

function editarUsuario(datos, id, solicitud, actor) {
  const nombre = typeof solicitud.nombre === 'string' ? solicitud.nombre.trim() : ''
  const correo = typeof solicitud.correo === 'string' ? solicitud.correo.trim().toLowerCase() : ''
  const { rol, activo, clave } = solicitud
  exigir(nombre.length >= 3 && nombre.length <= 80, 422, 'El nombre debe tener entre 3 y 80 caracteres.')
  exigir(/^[a-z0-9._+-]{1,60}@insertcode\.test$/.test(correo), 422, 'Usa un correo ficticio @insertcode.test.')
  exigir(Object.hasOwn(permisosPorRol, rol) && typeof activo === 'boolean', 422, 'Selecciona un rol y un estado válidos.')
  const anterior = id ? datos.usuarios.find(u => u.id === id) : null
  exigir(!id || anterior, 404, 'El usuario de prueba no existe.')
  exigir(!datos.usuarios.some(u => u.correo === correo && u.id !== id), 409, 'Ya existe una cuenta con ese correo.')
  exigir(!anterior?.googleDemo || rol === 'VENDEDOR', 422, 'Las cuentas de Google de demostración solo pueden ser Vendedor.')
  const disponibles = permisosPorRol[rol]
  const solicitados = solicitud.permisos ?? disponibles
  exigir(Array.isArray(solicitados) && solicitados.every(p => disponibles.includes(p)), 422, 'El rol no admite uno de los permisos.')
  const permisos = disponibles.filter(p => solicitados.includes(p))
  exigir(!permisos.includes('crearPedido') || (permisos.includes('comercial') && permisos.includes('pedidos')),
    422, 'Registrar pedidos requiere acceso a clientes, catálogo y pedidos.')
  exigir(!permisos.includes('actualizarEntrega') || permisos.includes('despacho'),
    422, 'Actualizar entregas requiere acceso a rutas y entregas.')
  exigir(id !== actor.id || (rol === 'ADMIN' && activo && permisos.includes('usuarios')),
    409, 'Tu cuenta debe conservar permisos de administración.')
  if (anterior?.rol === 'REPARTIDOR' && datos.rutas.some(r => r.repartidorId === id)) {
    exigir(rol === 'REPARTIDOR' && activo, 409, 'Esta cuenta tiene una ruta asignada.')
    datos.rutas.filter(r => r.repartidorId === id).forEach(r => { r.repartidor = nombre })
  }
  exigir((anterior && (clave == null || clave === '')) || (typeof clave === 'string' && clave.length >= 8 && clave.length <= 64), 422, 'La clave de prueba debe tener entre 8 y 64 caracteres.')
  const identidad = clave ? prepararClave(clave) : { sal: anterior.sal, hash: anterior.hash }
  const usuario = { ...anterior, id: anterior?.id ?? randomUUID(), nombre, correo, rol, activo, permisos, ...identidad }
  if (anterior) datos.usuarios[datos.usuarios.indexOf(anterior)] = usuario
  else datos.usuarios.push(usuario)
  return usuarioPublico(usuario)
}

function registrarCuenta(datos, solicitud) {
  const nombre = typeof solicitud.nombre === 'string' ? solicitud.nombre.trim() : ''
  const correo = typeof solicitud.correo === 'string' ? solicitud.correo.trim().toLowerCase() : ''
  const clave = solicitud.clave
  exigir(nombre.length >= 3 && nombre.length <= 80, 422, 'Ingresa un nombre ficticio de 3 a 80 caracteres.')
  exigir(/^[a-z0-9._+-]{1,60}@insertcode\.test$/.test(correo), 422, 'Usa un correo ficticio @insertcode.test.')
  exigir(typeof clave === 'string' && clave.length >= 8 && clave.length <= 64, 422, 'Usa entre 8 y 64 caracteres.')
  exigir(!datos.usuarios.some(u => u.correo === correo), 409, 'Ese correo ya está registrado.')
  const usuario = { id: randomUUID(), nombre, correo, rol: 'VENDEDOR', activo: true, ...prepararClave(clave) }
  datos.usuarios.push(usuario)
  return usuario
}

function registrarGoogle(datos, { id: googleId, demostracion }) {
  exigir(!datos.usuarios.some(u => u.googleId === googleId), 409, 'Esta cuenta de Google ya está registrada. Usa Ingresar con Google.')
  const id = randomUUID()
  const usuario = {
    id, nombre: 'Vendedor de prueba', correo: `google-${id}@insertcode.test`,
    rol: 'VENDEDOR', activo: true, googleId, googleDemo: demostracion
  }
  datos.usuarios.push(usuario)
  return usuario
}

export function crearApi(archivo, {
  clienteGoogle, consultarGoogle, claveDemo, habilitarDemoGoogle = false
} = {}) {
  const almacen = abrirDatos(archivo, claveDemo)
  const google = crearAccesoGoogle(clienteGoogle, consultarGoogle, habilitarDemoGoogle)
  const sesiones = new Map()

  function emitirSesion(usuario) {
    for (const [id, sesion] of sesiones) if (sesion.vence <= Date.now()) sesiones.delete(id)
    const token = randomBytes(32).toString('hex')
    sesiones.set(token, {
      id: usuario.id, hash: usuario.hash, rol: usuario.rol,
      permisos: permisosDe(usuario).join(), vence: Date.now() + 28_800_000
    })
    return { usuario: usuarioPublico(usuario), token }
  }

  function modificar(accion) {
    const copia = structuredClone(almacen.datos)
    const resultado = accion(copia)
    almacen.guardar(copia)
    almacen.datos = copia
    return resultado
  }

  function autenticar(peticion) {
    const token = (peticion.headers.authorization ?? '').match(/^Bearer ([a-f0-9]{64})$/)?.[1]
    const sesion = sesiones.get(token)
    const usuario = sesion && almacen.datos.usuarios.find(u => u.id === sesion.id && u.activo)
    exigir(usuario && sesion.vence > Date.now() && sesion.hash === usuario.hash && sesion.rol === usuario.rol && sesion.permisos === permisosDe(usuario).join(), 401, 'Vuelve a iniciar sesión.')
    return { usuario, token, permisos: permisosDe(usuario) }
  }

  const servidor = createServer(async (peticion, respuesta) => {
    try {
      const ruta = new URL(peticion.url, 'http://localhost').pathname
      const metodo = peticion.method
      if (ruta === '/salud' && metodo === 'GET') return responder(respuesta, 200, { estado: 'ok' })
      const cuerpo = ['POST', 'PATCH', 'PUT'].includes(metodo) ? await leerCuerpo(peticion) : null
      const datos = almacen.datos
      if (ruta === '/sesiones' && metodo === 'POST') {
        const correo = typeof cuerpo.correo === 'string' ? cuerpo.correo.trim().toLowerCase() : ''
        const clave = typeof cuerpo.clave === 'string' ? cuerpo.clave : ''
        const usuario = datos.usuarios.find(u => u.correo === correo && u.activo)
        exigir(usuario?.hash && clave.length <= 64 && claveValida(clave, usuario), 401, 'Correo o contraseña de prueba incorrectos.')
        return responder(respuesta, 201, emitirSesion(usuario))
      }
      if (ruta === '/registro' && metodo === 'POST') {
        const usuario = modificar(copia => registrarCuenta(copia, cuerpo))
        return responder(respuesta, 201, emitirSesion(usuario))
      }
      if (ruta === '/google/desafio' && metodo === 'GET') {
        return responder(respuesta, 200, google.crearDesafio())
      }
      if ((ruta === '/sesiones/google' || ruta === '/registro/google') && metodo === 'POST') {
        const identidad = await google.verificar(cuerpo)
        if (ruta === '/registro/google') {
          const usuario = modificar(copia => registrarGoogle(copia, identidad))
          return responder(respuesta, 201, emitirSesion(usuario))
        }
        const usuario = almacen.datos.usuarios.find(u => u.googleId === identidad.id)
        exigir(usuario, 404, 'Primero registra esta cuenta con Google.')
        exigir(usuario.activo, 403, 'La cuenta está desactivada.')
        return responder(respuesta, 201, emitirSesion(usuario))
      }
      const { usuario, token, permisos } = autenticar(peticion)
      const autorizar = permiso => exigir(permisos.includes(permiso), 403, 'Tu rol no puede realizar esta operación.')
      if (ruta === '/sesiones/actual' && metodo === 'DELETE') {
        sesiones.delete(token)
        return responder(respuesta, 200, { cerrado: true })
      }
      if (ruta === '/operacion' && metodo === 'GET') return responder(respuesta, 200, resumen(datos, usuario, permisos))
      if (ruta === '/rutas-disponibles' && metodo === 'GET') {
        autorizar('crearPedido')
        return responder(respuesta, 200, datos.rutas.map(({ id, nombre }) => ({ id, nombre })))
      }
      if (ruta === '/pedidos' && metodo === 'POST') {
        autorizar('crearPedido')
        return responder(respuesta, 201, modificar(copia => crearPedido(copia, cuerpo, usuario)))
      }
      const idEntrega = ruta.match(/^\/entregas\/([\w-]+)\/estado$/)?.[1]
      if (idEntrega && metodo === 'PATCH') {
        autorizar('actualizarEntrega')
        return responder(respuesta, 200, modificar(copia => cambiarEntrega(copia, usuario, idEntrega, cuerpo.estado)))
      }
      if (ruta === '/usuarios' && metodo === 'GET') {
        autorizar('usuarios')
        return responder(respuesta, 200, { usuarios: datos.usuarios.map(usuarioPublico), permisosPorRol })
      }
      const idUsuario = ruta.match(/^\/usuarios\/([\w-]+)$/)?.[1]
      if ((ruta === '/usuarios' && metodo === 'POST') || (idUsuario && metodo === 'PUT')) {
        autorizar('usuarios')
        return responder(respuesta, idUsuario ? 200 : 201, modificar(copia => editarUsuario(copia, idUsuario, cuerpo, usuario)))
      }
      throw new ErrorApi(404, 'No se encontró la operación.')
    } catch (error) {
      const conocido = error instanceof ErrorApi || error instanceof ErrorGoogle
      if (!conocido) process.stderr.write(`${error.message}\n`)
      responder(respuesta, conocido ? error.estado : 500, {
        mensaje: conocido ? error.message : 'No se pudo completar la operación.'
      })
    }
  })
  return { servidor, accesos: almacen.accesos }
}

function clienteGoogleLocal() {
  if (process.env.INSERTCODE_GOOGLE_CLIENT_ID) return process.env.INSERTCODE_GOOGLE_CLIENT_ID.trim()
  try {
    return readFileSync('api/datos/google-client-id.txt', 'utf8').trim()
  } catch (error) {
    if (error.code !== 'ENOENT') throw error
    return undefined
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  const { servidor, accesos } = crearApi(process.env.INSERTCODE_DATOS ?? 'api/datos/operacion.json', {
    clienteGoogle: clienteGoogleLocal(), claveDemo: process.env.INSERTCODE_CLAVE_DEMO,
    habilitarDemoGoogle: process.argv.includes('--demo-google')
  })
  const puerto = Number(process.env.INSERTCODE_PUERTO ?? 8080)
  servidor.listen(puerto, '127.0.0.1', () => {
    process.stdout.write(`API de prueba InsertCode en http://127.0.0.1:${puerto}\n`)
    for (const { correo, clave } of accesos) process.stdout.write(`${correo}  ${clave}\n`)
  })
}
