import { ArrowDown, ArrowRight, Check, CalendarDays, BookOpen, Users, GraduationCap } from 'lucide-react'
import { problems, features, steps, audiences, benefits } from '../content'
import { SectionHeading, PlatformLink } from './LandingShared'

export function ProblemSolution() {
  return <>
    <section className="landing-section landing-container" aria-labelledby="landing-problem-title">
      <div className="landing-split-heading"><div><p className="landing-eyebrow">MENOS DESENCONTROS</p><h2 id="landing-problem-title">Organizar a grade não deveria<br className="landing-desktop-break" /> ser outra disciplina.</h2></div><p className="landing-description">A rotina acadêmica já tem muitas peças.<br />Encontrar as informações não precisa ser mais uma tarefa.</p></div>
      <div className="landing-problem-grid">{problems.map(({ icon: Icon, title, text }) => <article key={title}><Icon size={22} aria-hidden="true" /><h3>{title}</h3><p>{text}</p></article>)}</div>
    </section>
    <section className="landing-solution-band">
      <div className="landing-container landing-solution-grid">
        <SectionHeading eyebrow="UMA VISÃO QUE CONECTA TUDO" title={<>Cada informação no lugar.<br />Todo o cronograma à vista.</>}>O Chronac reúne aulas, professores, disciplinas, turmas, cursos e horários em uma experiência visual. Menos tempo procurando. Mais clareza para planejar.</SectionHeading>
        <div className="landing-connected" aria-label="Professores, disciplinas e turmas conectados no calendário Chronac">
          <div className="landing-connected-sources"><span><Users />Professores</span><span><BookOpen />Disciplinas</span><span><GraduationCap />Turmas</span></div>
          <div className="landing-connector" aria-hidden="true"><ArrowDown size={20} /></div>
          <div className="landing-connected-result"><span className="landing-icon-box"><CalendarDays /></span><div><strong>Um calendário. Visão completa.</strong><p>Aulas, cursos e horários em contexto.</p></div><Check className="landing-connected-check" size={20} /></div>
        </div>
      </div>
    </section>
  </>
}

export function FeaturesSection() {
  return <section id="recursos" className="landing-section landing-container">
    <SectionHeading eyebrow="RECURSOS" title={<>Tudo para entender a grade.<br />Sem complicar a rotina.</>}>Ferramentas que trabalham juntas para tornar o planejamento acadêmico mais claro.</SectionHeading>
    <div className="landing-features-grid">{features.map(({ icon: Icon, title, text, note, featured }) => <article key={title} className={`landing-feature${featured ? ' is-featured' : ''}`}>
      <span className="landing-icon-box"><Icon size={22} aria-hidden="true" /></span><h3>{title}</h3><p>{text}</p>
      {featured && <div className="landing-feature-mini" aria-hidden="true">{Array.from({ length: 14 }, (_, index) => <span key={index} className={index % 4 === 0 ? 'is-green' : index % 3 === 0 ? 'is-blue' : ''} />)}</div>}
      {note && <span className="landing-feature-note">{note}</span>}
    </article>)}</div>
  </section>
}

export function HowItWorks() {
  return <section id="como-funciona" className="landing-section landing-container">
    <SectionHeading eyebrow="COMO FUNCIONA" title="Da informação à visão geral." centered>Um caminho simples para uma rotina mais organizada.</SectionHeading>
    <ol className="landing-steps">{steps.map(({ number, title, text, icon: Icon }, index) => <li key={number}>
      <div className="landing-step-top"><span>{number}</span><Icon size={25} aria-hidden="true" />{index < steps.length - 1 && <ArrowRight className="landing-step-arrow" size={22} aria-hidden="true" />}</div>
      <h3>{title}</h3><p>{text}</p>
    </li>)}</ol>
  </section>
}

export function AudienceSection() {
  return <section id="sobre" className="landing-audience-band">
    <div className="landing-container landing-section">
      <div className="landing-audience-layout"><SectionHeading eyebrow="FEITO PARA A VIDA ACADÊMICA" title={<>Quem planeja e quem ensina.<br />Na mesma página.</>}>O Chronac nasce para tornar a organização acadêmica mais simples e acessível. Uma plataforma em evolução, com foco no que faz diferença na rotina.</SectionHeading>
        <div className="landing-audiences">{audiences.map(({ icon: Icon, title, text }) => <article key={title}><Icon size={21} aria-hidden="true" /><div><h3>{title}</h3><p>{text}</p></div></article>)}</div>
      </div>
      <div className="landing-benefits"><div><p className="landing-eyebrow">O QUE MUDA NO DIA A DIA</p><h2>Mais clareza.<br />Mais tempo para o que importa.</h2></div><ul>{benefits.map((benefit) => <li key={benefit}><Check size={17} aria-hidden="true" />{benefit}</li>)}</ul></div>
    </div>
  </section>
}

export function FinalCta() {
  return <section className="landing-container landing-final-section"><div className="landing-final-cta">
    <div><p className="landing-eyebrow">SEU PRÓXIMO PERÍODO COMEÇA COM CLAREZA</p><h2>Simplifique a gestão do seu<br className="landing-desktop-break" /> cronograma acadêmico.</h2><p>Uma visão organizada para cada aula. E para tudo que vem depois.</p></div>
    <div className="landing-final-action"><PlatformLink>Acessar Chronac</PlatformLink><span>Plataforma em versão beta</span></div>
  </div></section>
}
