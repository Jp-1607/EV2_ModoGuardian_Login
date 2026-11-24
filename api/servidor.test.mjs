import assert from 'node:assert/strict'
import { randomUUID } from 'node:crypto'
import { mkdtempSync, rmSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { once } from 'node:events'
import { test } from 'node:test'
import { crearApi } from './servidor.mjs'

async function levantar(archivo) {
  const { servidor, accesos } = crearApi(archivo)
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
