import { useEffect, useRef, useState } from 'react'
import { Menu, X } from 'lucide-react'
import { ThemeToggle } from '@/components/theme/ThemeToggle'
import { navigation } from '../content'
import { Brand, PlatformLink } from './LandingShared'

export default function LandingNav() {
  const [open, setOpen] = useState(false)
  const toggle = useRef(null)

  useEffect(() => {
    if (!open) return
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') { setOpen(false); toggle.current?.focus() }
    }
    document.addEventListener('keydown', closeOnEscape)
    return () => document.removeEventListener('keydown', closeOnEscape)
  }, [open])

  return <header className="landing-header">
    <div className="landing-container landing-nav">
      <Brand />
      <nav aria-label="Navegação principal" className="landing-desktop-nav">
        {navigation.map(({ href, label }) => <a href={href} key={href}>{label}</a>)}
      </nav>
      <div className="landing-nav-actions"><ThemeToggle /><span className="landing-desktop-cta"><PlatformLink /></span>
        <button ref={toggle} className="landing-menu-toggle" type="button" onClick={() => setOpen(!open)} aria-expanded={open} aria-controls="landing-mobile-nav" aria-label={open ? 'Fechar menu' : 'Abrir menu'}>{open ? <X /> : <Menu />}</button>
      </div>
      <nav id="landing-mobile-nav" aria-label="Navegação móvel" className="landing-mobile-nav" hidden={!open} onClick={(event) => { if (event.target.closest('a')) setOpen(false) }}>
        {navigation.map(({ href, label }) => <a href={href} key={href}>{label}</a>)}
        <PlatformLink />
      </nav>
    </div>
  </header>
}
