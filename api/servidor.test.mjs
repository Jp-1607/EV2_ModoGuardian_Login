import assert from 'node:assert/strict'
import { generateKeyPairSync, randomUUID, sign } from 'node:crypto'
import { mkdtempSync, rmSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { once } from 'node:events'
import { test } from 'node:test'
import { crearApi } from './servidor.mjs'

async function levantar(archivo, opciones) {
  const { servidor, accesos } = crearApi(archivo, opciones)
  servidor.listen(0, '127.0.0.1')
  await once(servidor, 'listening')
  const url = `http://127.0.0.1:${servidor.address().port}`
  async function peticion(ruta, { metodo = 'GET', token, cuerpo } = {}) {
    const headers = { 'Content-Type': 'application/json' }
    if (token) headers.Authorization = `Bearer ${token}`
    const res = await fetch(url + ruta, { method: metodo, headers, body: cuerpo ? JSON.stringify(cuerpo) : undefined })
    return { codigo: res.status, datos: await res.json() }
  }
  return { servidor, accesos, peticion }
}

async function autenticar(peticion, cuerpo) {
  const respuesta = await peticion('/sesiones', { metodo: 'POST', cuerpo })
  assert.equal(respuesta.codigo, 201)
  return respuesta.datos
}

test('Entrega, ventas y permisos se mantienen coherentes a lo largo de un pedido', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'insertcode-test-'))
  const archivo = join(dir, 'prueba.json')
  let inicio = await levantar(archivo)
  const accesos = inicio.accesos
  const sesion = async correo => autenticar(inicio.peticion, accesos.find(a => a.correo.startsWith(correo + '@')))
  try {
    const admin = await sesion('admin')
    const supervisor = await sesion('supervisor')
    const vendedor = await sesion('vendedor')
    const repartidor = await sesion('repartidor')
    assert.equal((await inicio.peticion('/operacion')).codigo, 401)
    assert.equal((await inicio.peticion('/usuarios', vendedor)).codigo, 403)
    assert.equal((await inicio.peticion('/usuarios', admin)).datos.usuarios.length, 4)
    const previa = (await inicio.peticion('/operacion', supervisor)).datos.indicadores
    const comercial = (await inicio.peticion('/operacion', vendedor)).datos
    assert.equal(comercial.productos.length, 3)
    assert.equal(comercial.entregas.length, 0)
    const [ruta] = (await inicio.peticion('/rutas-disponibles', vendedor)).datos
    const pedido = {
      idOperacion: randomUUID(), clienteId: comercial.clientes[0].id, rutaId: ruta.id,
      fecha: '2026-11-24', prioridad: 'ALTA',
      items: [{ productoId: comercial.productos[0].id, cantidad: 2 }]
    }
    const crear = (token, cuerpo) => inicio.peticion('/pedidos', { ...token, metodo: 'POST', cuerpo })
    assert.equal((await crear(repartidor, pedido)).codigo, 403)
    assert.equal((await crear(vendedor, { ...pedido, fecha: '2026-02-29' })).codigo, 422)
    assert.equal((await crear(vendedor, { ...pedido, items: [{ ...pedido.items[0], cantidad: 0 }] })).codigo, 422)
    const respuesta = await crear(vendedor, pedido)
    assert.equal(respuesta.codigo, 201)
    assert.equal(respuesta.datos.total, 900000)
    assert.equal((await crear(vendedor, pedido)).datos.id, respuesta.datos.id)
    const nueva = (await inicio.peticion('/operacion', repartidor)).datos.entregas.find(e => e.pedidoId === respuesta.datos.id)
    const cambiar = (token, estado) => inicio.peticion(`/entregas/${nueva.id}/estado`, {
      ...token, metodo: 'PATCH', cuerpo: { estado }
    })
    assert.equal((await cambiar(repartidor, 'ENTREGADO')).codigo, 409)
    assert.equal((await cambiar(supervisor, 'EN_RUTA')).codigo, 403)
    assert.equal((await cambiar(repartidor, 'EN_RUTA')).datos.estado, 'EN_RUTA')
    assert.equal((await cambiar(repartidor, 'ENTREGADO')).datos.estado, 'ENTREGADO')
    assert.equal((await cambiar(repartidor, 'ENTREGADO')).codigo, 200)
    const final = (await inicio.peticion('/operacion', supervisor)).datos.indicadores
    assert.equal(final.ventas - previa.ventas, 900000)
    assert.equal(final.entregasRealizadas - previa.entregasRealizadas, 1)
    const nuevoRepartidor = await inicio.peticion('/usuarios', {
      ...admin, metodo: 'POST',
      cuerpo: { nombre: 'Turno de prueba', correo: 'turno@insertcode.test', rol: 'REPARTIDOR', activo: true, clave: randomUUID() }
    })
    assert.equal(nuevoRepartidor.codigo, 201)
    const claveTemporal = randomUUID()
    const cambios = { ...nuevoRepartidor.datos, clave: claveTemporal }
    await inicio.peticion(`/usuarios/${cambios.id}`, { ...admin, metodo: 'PUT', cuerpo: cambios })
    const otraSesion = await autenticar(inicio.peticion, { correo: cambios.correo, clave: claveTemporal })
    assert.deepEqual((await inicio.peticion('/operacion', otraSesion)).datos.rutas, [])
    assert.equal((await cambiar(otraSesion, 'ENTREGADO')).codigo, 404)
    const guardarPermisos = permisos => inicio.peticion(`/usuarios/${cambios.id}`, {
      ...admin, metodo: 'PUT', cuerpo: { ...cambios, clave: null, permisos }
    })
    assert.equal((await guardarPermisos(['usuarios'])).codigo, 422)
    assert.equal((await guardarPermisos(['actualizarEntrega'])).codigo, 422)
    assert.deepEqual((await guardarPermisos(['despacho'])).datos.permisos, ['despacho'])
    assert.equal((await inicio.peticion('/operacion', otraSesion)).codigo, 401)
    const accesoLimitado = await autenticar(inicio.peticion, { correo: cambios.correo, clave: claveTemporal })
    assert.equal((await cambiar(accesoLimitado, 'ENTREGADO')).codigo, 403)
    assert.equal((await inicio.peticion('/sesiones/actual', { ...repartidor, metodo: 'DELETE' })).codigo, 200)
    assert.equal((await inicio.peticion('/operacion', repartidor)).codigo, 401)
    await new Promise(cb => inicio.servidor.close(cb))
    inicio = await levantar(archivo)
    assert.deepEqual(inicio.accesos, [])
    const despues = await sesion('vendedor')
    const persistidos = (await inicio.peticion('/operacion', despues)).datos.pedidos
    assert.equal(persistidos.filter(p => p.id === respuesta.datos.id).length, 1)
    assert.equal(persistidos.find(p => p.id === respuesta.datos.id).estado, 'ENTREGADO')
  } finally {
    await new Promise(cb => inicio.servidor.close(cb))
    rmSync(dir, { recursive: true, force: true })
  }
})

test('El registro por correo asigna el rol Vendedor y la cuenta puede volver a ingresar', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'insertcode-registro-'))
  const clave = randomUUID()
  const inicio = await levantar(join(dir, 'prueba.json'), { claveDemo: clave })
  const correo = 'registro@insertcode.test'
  try {
    assert.equal(inicio.accesos.length, 4)
    for (const { correo: cuenta, clave: acceso } of inicio.accesos) {
      assert.equal(acceso, clave)
      await autenticar(inicio.peticion, { correo: cuenta, clave })
    }
    const crear = cuerpo => inicio.peticion('/registro', { metodo: 'POST', cuerpo })
    const solicitud = { nombre: 'Vendedor de prueba', correo, clave, rol: 'ADMIN' }
    assert.equal((await crear({ ...solicitud, correo: 'real@example.com' })).codigo, 422)
    const registro = await crear(solicitud)
    assert.equal(registro.codigo, 201)
    assert.equal(registro.datos.usuario.rol, 'VENDEDOR')
    assert.equal((await crear(solicitud)).codigo, 409)
    const sesion = await autenticar(inicio.peticion, { correo, clave })
    assert.equal(sesion.usuario.id, registro.datos.usuario.id)
  } finally {
    await new Promise(cb => inicio.servidor.close(cb))
    rmSync(dir, { recursive: true, force: true })
  }
})

test('El modo de demostración registra e inicia sesión con la misma cuenta ficticia', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'insertcode-google-demo-'))
  const archivo = join(dir, 'prueba.json')
  const inicio = await levantar(archivo, { habilitarDemoGoogle: true })
  const entregar = async ruta => {
    const { datos } = await inicio.peticion('/google/desafio')
    assert.equal(datos.modo, 'demo')
    const cuerpo = { nonce: datos.nonce, cuentaDemo: 'ejemplo' }
    return inicio.peticion(ruta, { metodo: 'POST', cuerpo })
  }
  try {
    assert.equal((await entregar('/sesiones/google')).codigo, 404)
    const registro = await entregar('/registro/google')
    assert.equal(registro.codigo, 201)
    assert.equal(registro.datos.usuario.rol, 'VENDEDOR')
    assert.equal((await entregar('/registro/google')).codigo, 409)
    const ingreso = await entregar('/sesiones/google')
    assert.equal(ingreso.datos.usuario.id, registro.datos.usuario.id)
    assert.equal((await inicio.peticion('/usuarios', ingreso.datos)).codigo, 403)
    const admin = await autenticar(inicio.peticion, inicio.accesos.find(a => a.correo === 'admin@insertcode.test'))
    const elevar = await inicio.peticion(`/usuarios/${registro.datos.usuario.id}`, {
      ...admin, metodo: 'PUT', cuerpo: { ...registro.datos.usuario, rol: 'ADMIN', permisos: ['usuarios'] }
    })
    assert.equal(elevar.codigo, 422)
  } finally {
    await new Promise(cb => inicio.servidor.close(cb))
    rmSync(dir, { recursive: true, force: true })
  }
})

test('La verificación de Google comprueba firma, audiencia y desafío de un solo uso', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'insertcode-google-token-'))
  const clienteGoogle = '123-prueba.apps.googleusercontent.com'
  const { privateKey, publicKey } = generateKeyPairSync('rsa', { modulusLength: 2048 })
  const jwk = { ...publicKey.export({ format: 'jwk' }), kid: 'prueba', alg: 'RS256' }
  const consultarGoogle = async () => Response.json({ keys: [jwk] })
  const inicio = await levantar(join(dir, 'prueba.json'), { clienteGoogle, consultarGoogle, habilitarDemoGoogle: true })
  function token(nonce, datos = {}) {
    const ahora = Math.floor(Date.now() / 1000)
    const encode = valor => Buffer.from(JSON.stringify(valor)).toString('base64url')
    const cabecera = encode({ alg: 'RS256', kid: 'prueba' })
    const contenido = encode({
      iss: 'https://accounts.google.com', sub: 'test-123', aud: clienteGoogle,
      nonce, iat: ahora, exp: ahora + 180, ...datos
    })
    const datosFirma = `${cabecera}.${contenido}`
    return `${datosFirma}.${sign('RSA-SHA256', Buffer.from(datosFirma), privateKey).toString('base64url')}`
  }
  const obtenerNonce = async () => {
    const desafio = (await inicio.peticion('/google/desafio')).datos
    assert.equal(desafio.modo, 'google')
    return desafio.nonce
  }
  const enviar = (ruta, cuerpo) => inicio.peticion(ruta, { metodo: 'POST', cuerpo })
  const registro = '/registro/google'
  try {
    const nonceDemo = await obtenerNonce()
    assert.equal((await enviar(registro, { nonce: nonceDemo, cuentaDemo: 'ejemplo' })).codigo, 401)
    const malaAudiencia = await obtenerNonce()
    assert.equal((await enviar(registro, { nonce: malaAudiencia, token: token(malaAudiencia, { aud: 'otro' }) })).codigo, 401)
    const nonce = await obtenerNonce()
    const cuerpo = { nonce, token: token(nonce) }
    const cuenta = await enviar(registro, cuerpo)
    assert.equal(cuenta.codigo, 201)
    assert.equal((await enviar(registro, cuerpo)).codigo, 401)
    const nonceLogin = await obtenerNonce()
    const acceso = await enviar('/sesiones/google', { nonce: nonceLogin, token: token(nonceLogin) })
    assert.equal(acceso.datos.usuario.id, cuenta.datos.usuario.id)
  } finally {
    await new Promise(cb => inicio.servidor.close(cb))
    rmSync(dir, { recursive: true, force: true })
  }
})
