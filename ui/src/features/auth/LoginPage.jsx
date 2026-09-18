import { useState } from 'react'

import './Login.css'
import { ThemeToggle } from '@/components/theme/ThemeToggle'

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('')

  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    if (loading) return
    setLoading(true)
    setError('')
    try {
      await onLogin(email, password)
    } catch (failure) {
      setError(failure instanceof TypeError ? 'Não foi possível conectar ao servidor.' : failure.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login">
      <div className="login-container">
        <div className="login-theme"><ThemeToggle /></div>
        <h1>CHRONAC</h1>
        <p>Acesse sua conta</p>

        <form onSubmit={handleSubmit} aria-busy={loading}>
          <label htmlFor="email">E-mail</label>

          <input
            id="email"
            type="email"
            autoComplete="username"
            maxLength={254}
            disabled={loading}
            aria-describedby={error ? 'login-error' : undefined}
            placeholder="Digite seu e-mail"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          <label htmlFor="password">Senha</label>

          <input
            id="password"
            type="password"
            autoComplete="current-password"
            disabled={loading}
            aria-describedby={error ? 'login-error' : undefined}
            placeholder="Digite sua senha"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          {error && <p id="login-error" role="alert" className="login-error">{error}</p>}
          <button type="submit" disabled={loading}>{loading ? 'Entrando…' : 'Entrar'}</button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
