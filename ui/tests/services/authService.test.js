import assert from 'node:assert/strict'
import { test } from 'node:test'
import { currentUser, login, logout } from '../../src/features/auth/authService.js'

test('restores a cookie session and treats only 401 as signed out', async (t) => {
  const mock = t.mock.method(globalThis, 'fetch', async (url, options) => {
    assert.equal(url, '/api/auth/me')
    assert.equal(options.credentials, 'same-origin')
    assert.equal(options.cache, 'no-store')
    return { ok: true, json: async () => ({ role: 'ADMIN' }) }
  })
  assert.deepEqual(await currentUser(), { role: 'ADMIN' })
  mock.mock.mockImplementation(async () => ({ status: 401 }))
  assert.equal(await currentUser(), null)
  mock.mock.mockImplementation(async () => ({ status: 500 }))
  await assert.rejects(currentUser(), /verificar sua sessão/)
})

test('login submits credentials with the server CSRF token', async (t) => {
  const calls = []
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options })
    return { ok: true, json: async () => url.endsWith('/csrf')
      ? { token: 'csrf-value', headerName: 'X-XSRF-TOKEN' } : { email: 'test@example.com', role: 'ADMIN' } }
  })
  assert.equal((await login('test@example.com', 'test-password')).role, 'ADMIN')
  assert.equal(calls[0].url, '/api/auth/csrf')
  assert.equal(calls[1].url, '/api/auth/login')
  assert.equal(calls[1].options.headers['X-XSRF-TOKEN'], 'csrf-value')
  assert.equal(calls[1].options.credentials, 'same-origin')
  assert.deepEqual(JSON.parse(calls[1].options.body), { email: 'test@example.com', password: 'test-password' })
})

test('invalid login and failed logout surface errors; expired logout succeeds', async (t) => {
  let status = 401
  t.mock.method(globalThis, 'fetch', async (url) => url.endsWith('/csrf')
    ? { ok: true, json: async () => ({ token: 'token', headerName: 'X-XSRF-TOKEN' }) }
    : { ok: false, status })
  await assert.rejects(login('test@example.com', 'wrong'), /Email ou senha inválidos/)
  await logout()
  status = 500
  await assert.rejects(logout(), /Não foi possível sair/)
})
