import { useEffect, useRef, useState } from 'react'
import { Button } from '@/components/ui/button'
import LoginPage from './LoginPage'
import { currentUser, login, logout } from './authService'

function navigate(path) {
  if (window.location.pathname !== path) {
    window.history.replaceState(null, '', path)
    window.dispatchEvent(new Event('chronac:navigate'))
  }
}

export default function AuthGate({ component: Component, componentProps }) {
  const [session, setSession] = useState({ loading: true, user: null, error: '' })
  const [attempt, setAttempt] = useState(0)
  const [leaving, setLeaving] = useState(false)
  const [logoutError, setLogoutError] = useState('')
  const revision = useRef(0)
  const mutating = useRef(false)

  useEffect(() => {
    let active = true
    const check = () => {
      if (mutating.current) return
      const requestRevision = ++revision.current
      return currentUser().then((user) => {
        if (!active || revision.current !== requestRevision) return
        setSession({ loading: false, user, error: '' })
        if (!user) navigate('/login')
        else if (window.location.pathname === '/login') navigate('/')
      }).catch(() => {
        if (active && revision.current === requestRevision) setSession((previous) => previous.user ? previous : {
          loading: false, user: null, error: 'Não foi possível verificar sua sessão. Confira a conexão e tente novamente.',
        })
      })
    }
    const expire = () => {
      revision.current++
      setSession({ loading: false, user: null, error: '' })
      navigate('/login')
    }
    check()
    const interval = window.setInterval(check, 60_000)
    window.addEventListener('focus', check)
    window.addEventListener('popstate', check)
    window.addEventListener('chronac:unauthorized', expire)
    return () => {
      active = false
      window.clearInterval(interval)
      window.removeEventListener('focus', check)
      window.removeEventListener('popstate', check)
      window.removeEventListener('chronac:unauthorized', expire)
    }
  }, [attempt])

  async function signIn(email, password) {
    mutating.current = true
    revision.current++
    try {
      const user = await login(email, password)
      setLogoutError('')
      setSession({ loading: false, user, error: '' })
      navigate('/')
    } finally {
      mutating.current = false
    }
  }

  async function signOut() {
    mutating.current = true
    revision.current++
    setLeaving(true)
    setLogoutError('')
    try {
      await logout()
      setSession({ loading: false, user: null, error: '' })
      navigate('/login')
    } catch {
      setLogoutError('Não foi possível sair. Tente novamente.')
    } finally {
      mutating.current = false
      setLeaving(false)
    }
  }

  if (session.loading || session.error) return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-background p-6 text-center text-foreground">
      <p role={session.error ? 'alert' : 'status'}>{session.error || 'Verificando sessão…'}</p>
      {session.error && <Button onClick={() => {
        setSession({ loading: true, user: null, error: '' })
        setAttempt((value) => value + 1)
      }}>Tentar novamente</Button>}
    </main>
  )
  if (!session.user) return <LoginPage onLogin={signIn} />
  return <>
    {logoutError && <p role="alert" className="bg-destructive/10 p-3 text-center text-sm text-destructive">{logoutError}</p>}
    <Component {...componentProps} user={session.user} onLogout={signOut} loggingOut={leaving} />
  </>
}
