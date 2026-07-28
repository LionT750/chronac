// Importa o useState para guardar os valores digitados
import { useState } from 'react'

// Importa os estilos da tela de login
import './Login.css'

function Login() {
  // Guarda o e-mail digitado
  const [email, setEmail] = useState('')

  // Guarda a senha digitada
  const [password, setPassword] = useState('')

  // Executa quando o formulário é enviado
  function handleSubmit(event) {
    // Impede que a página atualize
    event.preventDefault()
  }

  return (
    // Área completa da tela de login
    <div className="login">
      {/* Caixa do formulário */}
      <div className="login-container">
        <h1>CHRONAC</h1>
        <p>Acesse sua conta</p>

        {/* Formulário de login */}
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

export default Login