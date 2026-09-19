export async function currentUser() {
  const response = await fetch('/api/auth/me', { credentials: 'same-origin', cache: 'no-store' })
  if (response.status === 401) return null
  if (!response.ok) throw new Error('Não foi possível verificar sua sessão. Tente novamente.')
  return response.json()
}

async function authMutation(path, body) {
  const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin', cache: 'no-store' })
  if (!csrfResponse.ok) throw new Error('Não foi possível conectar ao servidor. Tente novamente.')
  const csrf = await csrfResponse.json()
  return fetch(`/api/auth/${path}`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    ...(body ? { body: JSON.stringify(body) } : {}),
  })
}

export async function login(email, password) {
  const response = await authMutation('login', { email, password })
  if (response.status === 401) throw new Error('Email ou senha inválidos.')
  if (!response.ok) throw new Error('Não foi possível entrar. Tente novamente.')
  return response.json()
}

export async function logout() {
  const response = await authMutation('logout')
  if (!response.ok && response.status !== 401) throw new Error('Não foi possível sair. Tente novamente.')
}
