import { useState } from 'react'

import './Login.css'
import { ThemeToggle } from '@/components/theme/ThemeToggle'

function LoginPage() {
  const [email, setEmail] = useState('')

  const [password, setPassword] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
  }

  return (
    <div className="login">
      <div className="login-container">
        <div className="login-theme"><ThemeToggle /></div>
        <h1>CHRONAC</h1>
        <p>Acesse sua conta</p>

        <form onSubmit={handleSubmit}>
          <label htmlFor="email">E-mail</label>

          <input
            id="email"
            type="email"
            placeholder="Digite seu e-mail"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          <label htmlFor="password">Senha</label>

          <input
            id="password"
            type="password"
            placeholder="Digite sua senha"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          <button type="submit">Entrar</button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage