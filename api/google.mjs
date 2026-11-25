import { createHash, createPublicKey, randomBytes, verify } from 'node:crypto'

const certificados = 'https://www.googleapis.com/oauth2/v3/certs'
const emisores = ['https://accounts.google.com', 'accounts.google.com']

export class ErrorGoogle extends Error {
  constructor(estado, mensaje) {
    super(mensaje)
    this.estado = estado
  }
}

function tokenInvalido() {
  return new ErrorGoogle(401, 'Google no pudo verificar tu cuenta. Vuelve a intentar.')
}

export function crearAccesoGoogle(clienteId, consultar = fetch, habilitarDemo = false) {
  const demostracion = !clienteId && habilitarDemo
  let llaves = new Map()
  let renovarEn = 0
  let consultaPendiente
  const desafios = new Map()

  function exigirConfiguracion() {
    if (!demostracion && !/^[0-9]+-[a-zA-Z0-9_-]+\.apps\.googleusercontent\.com$/.test(clienteId ?? '')) {
      throw new ErrorGoogle(503, 'Falta configurar el cliente de Google para esta API de prueba.')
    }
  }

  function crearDesafio() {
    exigirConfiguracion()
    const ahora = Date.now()
    for (const [nonce, vence] of desafios) if (vence <= ahora) desafios.delete(nonce)
    if (desafios.size >= 200) throw new ErrorGoogle(429, 'Espera un momento y vuelve a intentar.')
    const nonce = randomBytes(32).toString('base64url')
    desafios.set(nonce, ahora + 300_000)
    return demostracion ? { modo: 'demo', nonce } : { modo: 'google', clienteId, nonce }
  }

  async function actualizarLlaves() {
    try {
      const respuesta = await consultar(certificados, { signal: AbortSignal.timeout(5000) })
      if (!respuesta.ok) throw new Error('Certificados no disponibles')
      const { keys } = await respuesta.json()
      const vigencia = Number(respuesta.headers.get('cache-control')?.match(/max-age=(\d+)/)?.[1] ?? 300)
      const nuevas = keys.filter(k => k.kty === 'RSA' && k.alg === 'RS256' && typeof k.kid === 'string')
      llaves = new Map(nuevas.map(k => [k.kid, createPublicKey({ key: k, format: 'jwk' })]))
      renovarEn = Date.now() + Math.min(vigencia, 86_400) * 1000
    } catch {
      throw new ErrorGoogle(502, 'No se pudo consultar los certificados de Google.')
    }
  }

  async function llavePara(id) {
    if (Date.now() >= renovarEn) {
      consultaPendiente ??= actualizarLlaves().finally(() => { consultaPendiente = undefined })
      await consultaPendiente
    }
    return llaves.get(id)
  }

  async function verificar({ token, nonce, cuentaDemo }) {
    exigirConfiguracion()
    if (typeof nonce !== 'string') throw tokenInvalido()
    const vence = desafios.get(nonce)
    desafios.delete(nonce)
    if (!vence || vence <= Date.now()) throw tokenInvalido()
    if (demostracion) {
      if (cuentaDemo !== 'ejemplo' && !/^[a-f0-9]{64}$/.test(cuentaDemo ?? '')) throw tokenInvalido()
      const id = createHash('sha256').update(`demo:${cuentaDemo}`, 'utf8').digest('hex')
      return { id, demostracion: true }
    }
    if (typeof token !== 'string' || token.length > 12_000) throw tokenInvalido()
    const partes = token.split('.')
    if (partes.length !== 3 || !partes.every(p => /^[A-Za-z0-9_-]+$/.test(p))) throw tokenInvalido()
    const [cabecera, cuerpo, firma] = partes
    let encabezado
    let datos
    try {
      encabezado = JSON.parse(Buffer.from(cabecera, 'base64url'))
      datos = JSON.parse(Buffer.from(cuerpo, 'base64url'))
    } catch {
      throw tokenInvalido()
    }
    const ahora = Math.floor(Date.now() / 1000)
    if (encabezado?.alg !== 'RS256' || typeof encabezado.kid !== 'string' ||
        datos?.aud !== clienteId || !emisores.includes(datos.iss) || datos.nonce !== nonce ||
        !Number.isInteger(datos.exp) || datos.exp <= ahora ||
        !Number.isInteger(datos.iat) || datos.iat > ahora + 60 || datos.iat < ahora - 600 ||
        typeof datos.sub !== 'string' || datos.sub.length === 0 || datos.sub.length > 255) {
      throw tokenInvalido()
    }
    const llave = await llavePara(encabezado.kid)
    if (!llave || !verify('RSA-SHA256', Buffer.from(`${cabecera}.${cuerpo}`), llave, Buffer.from(firma, 'base64url'))) {
      throw tokenInvalido()
    }
    return { id: createHash('sha256').update(datos.sub, 'utf8').digest('hex'), demostracion: false }
  }

  return { crearDesafio, verificar }
}
