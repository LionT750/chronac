import { useState } from 'react'
import { CircleAlert, Eye, EyeOff, LoaderCircle } from 'lucide-react'

import './Login.css'
import { ThemeToggle } from '@/components/theme/ThemeToggle'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('')

  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
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
    <main className="login" aria-labelledby="login-title">
      <section className="login-container" aria-labelledby="login-title">
        <div className="login-brand-row">
          <img src="/logo.png" alt="Chronac" className="brand-logo login-logo" width="180" height="60" />
          <ThemeToggle />
        </div>
        <div className="login-heading">
          <h1 id="login-title">Acesse o Chronac</h1>
          <p>Entre com sua conta para acessar o cronograma acadêmico.</p>
        </div>

        <form onSubmit={handleSubmit} aria-busy={loading}>
          <div className="login-field">
            <label htmlFor="email">E-mail</label>

            <Input
              id="email"
              name="email"
              className="login-input"
              type="email"
              autoCapitalize="none"
              spellCheck={false}
              autoComplete="username"
              maxLength={254}
              disabled={loading}
              aria-describedby={error ? 'login-error' : undefined}
              placeholder="Digite seu e-mail"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </div>
          <div className="login-field">
            <label htmlFor="password">Senha</label>

            <div className="login-password">
              <Input
                id="password"
                name="password"
                className="login-input"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                disabled={loading}
                aria-describedby={error ? 'login-error' : undefined}
                placeholder="Digite sua senha"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
              />

              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="login-password-toggle"
                disabled={loading}
                aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                aria-controls="password"
                title={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                onClick={() => setShowPassword((visible) => !visible)}
              >
                {showPassword ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}
              </Button>
            </div>
          </div>

          {error && <div id="login-error" role="alert" className="login-error">
            <CircleAlert size={18} aria-hidden="true" /><p>{error}</p>
          </div>}
          <Button type="submit" className="login-submit" disabled={loading}>
            {loading && <LoaderCircle className="login-spinner" aria-hidden="true" />}
            {loading ? 'Entrando…' : 'Entrar'}
          </Button>
        </form>
        <p className="sr-only" role="status" aria-live="polite">{loading ? 'Entrando no Chronac. Aguarde.' : ''}</p>
        <div className="login-footer"><span>Cronograma acadêmico</span><span className="login-beta">Versão beta</span></div>
      </section>
    </main>
  )
}

export default LoginPage
