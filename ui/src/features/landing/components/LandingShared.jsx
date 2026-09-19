import { ArrowUpRight } from 'lucide-react'

export function Brand({ linked = true }) {
  const content = <><img src="/logo_p.png" width="38" height="38" alt="" /><span>chronac<span className="landing-brand-dot">.</span></span></>
  return linked ? <a className="landing-brand" href="/chronac" aria-label="Chronac — início">{content}</a> : <span className="landing-brand">{content}</span>
}

export function PlatformLink({ children = 'Acessar plataforma', secondary = false }) {
  return <a href="/login" className={`landing-button ${secondary ? 'landing-button-secondary' : 'landing-button-primary'}`}>{children}<ArrowUpRight size={17} aria-hidden="true" /></a>
}

export function SectionHeading({ eyebrow, title, children, centered = false }) {
  return <div className={`landing-section-heading${centered ? ' is-centered' : ''}`}>
    <p className="landing-eyebrow">{eyebrow}</p>
    <h2>{title}</h2>
    {children && <p className="landing-description">{children}</p>}
  </div>
}
