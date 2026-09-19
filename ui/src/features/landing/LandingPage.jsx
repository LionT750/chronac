import { useEffect } from 'react'
import { ArrowRight, Check } from 'lucide-react'
import LandingNav from './components/LandingNav'
import ProductPreview, { HeroPreview } from './components/ProductPreview'
import { Brand, PlatformLink, SectionHeading } from './components/LandingShared'
import { ProblemSolution, FeaturesSection, HowItWorks, AudienceSection, FinalCta } from './components/LandingSections'
import { navigation } from './content'
import './landing.css'

export default function LandingPage() {
  useEffect(() => {
    const previousTitle = document.title
    document.title = 'Chronac — Seu cronograma acadêmico, organizado de verdade'
    return () => { document.title = previousTitle }
  }, [])

  return <div className="landing-page">
    <a href="#conteudo" className="landing-skip-link">Pular para o conteúdo</a>
    <LandingNav />
    <main id="conteudo" tabIndex={-1}>
      <section className="landing-hero landing-container" aria-labelledby="landing-title">
        <a href="#produto" className="landing-announcement"><span className="landing-status-dot" />UM NOVO OLHAR PARA A ROTINA ACADÊMICA<span className="landing-announcement-beta">BETA</span><ArrowRight size={14} aria-hidden="true" /></a>
        <h1 id="landing-title">Seu cronograma acadêmico,<br /><span>organizado de verdade.</span></h1>
        <p className="landing-hero-description">Aulas, professores, disciplinas, turmas e horários.<br className="landing-desktop-break" /> Tudo conectado em um calendário que faz sentido.</p>
        <div className="landing-hero-actions"><a href="#produto" className="landing-button landing-button-primary">Conhecer o Chronac<ArrowRight size={17} aria-hidden="true" /></a><PlatformLink secondary /></div>
        <div className="landing-hero-reassurance"><span><Check size={14} />Visão centralizada</span><span><Check size={14} />Consulta simples</span><span><Check size={14} />Mais organização</span></div>
        <HeroPreview />
        <div className="landing-context-line"><span>DO PLANEJAMENTO À SALA DE AULA</span><p>Uma visão compartilhada para <strong>quem organiza</strong>, <strong>quem ensina</strong> e <strong>quem acompanha.</strong></p></div>
      </section>
      <ProblemSolution />
      <FeaturesSection />
      <section id="produto" className="landing-product-section">
        <div className="landing-container">
          <div className="landing-product-heading"><SectionHeading eyebrow="POR DENTRO DO CHRONAC" title={<>O calendário é o centro.<br />A clareza é o resultado.</>}>Explore a interface do produto: alterne entre mês, semana e dia, filtre um curso e selecione uma aula para ver os detalhes.</SectionHeading><span className="landing-live-label"><span className="landing-status-dot" />Demonstração interativa</span></div>
          <ProductPreview />
          <p className="landing-product-caption">O mesmo componente de calendário da plataforma, com dados fictícios para demonstração.</p>
        </div>
      </section>
      <HowItWorks />
      <AudienceSection />
      <FinalCta />
    </main>
    <footer className="landing-footer landing-container"><div className="landing-footer-top"><div><Brand /><p>Tempo organizado. Ensino conectado.</p></div><nav aria-label="Links do rodapé">{navigation.map(({ href, label }) => <a href={href} key={href}>{label}</a>)}<a href="/login">Acessar plataforma <span aria-hidden="true">↗</span></a></nav></div><div className="landing-footer-bottom"><span>© {new Date().getFullYear()} Chronac</span><span><span className="landing-status-dot" />Versão beta · Em constante evolução</span><a href="#conteudo">Voltar ao início ↑</a></div></footer>
  </div>
}
