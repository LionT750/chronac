import { CalendarDays, SlidersHorizontal, Users, GraduationCap, BookOpen, PanelsTopLeft, ShieldCheck, Clock3, Layers, Search, University, School, ClipboardList, UserRound } from 'lucide-react'

export const navigation = [
  { href: '#produto', label: 'Produto' },
  { href: '#recursos', label: 'Recursos' },
  { href: '#como-funciona', label: 'Como funciona' },
  { href: '#sobre', label: 'Sobre' },
]

export const problems = [
  { icon: Clock3, title: 'Horários que se cruzam', text: 'Uma aula muda e fica difícil enxergar o impacto no restante da grade.' },
  { icon: CalendarDays, title: 'Cronogramas difíceis de ler', text: 'Entre linhas e colunas, a visão da semana acaba se perdendo.' },
  { icon: Layers, title: 'Informações espalhadas', text: 'Planilhas, arquivos e mensagens transformam uma consulta simples em uma busca.' },
  { icon: Search, title: 'Muitos detalhes para acompanhar', text: 'Professor, turma, disciplina e sala precisam fazer sentido juntos.' },
]

export const features = [
  { icon: CalendarDays, title: 'Calendário acadêmico', text: 'Aulas e horários em uma grade visual, com os detalhes sempre ao alcance.', featured: true },
  { icon: SlidersHorizontal, title: 'Filtros inteligentes', text: 'Encontre o que precisa por professor, disciplina, curso ou dia da semana.' },
  { icon: Users, title: 'Gestão de professores', text: 'Organize os cadastros e consulte os professores associados às aulas.' },
  { icon: GraduationCap, title: 'Gestão de turmas', text: 'Mantenha as turmas organizadas para apoiar o planejamento acadêmico.' },
  { icon: BookOpen, title: 'Disciplinas em contexto', text: 'Consulte cada disciplina com seu horário, professor e sala, sem perder a visão do curso.' },
  { icon: PanelsTopLeft, title: 'Mês, semana ou dia', text: 'Alterne entre a visão geral do período e os detalhes da rotina diária.' },
  { icon: ShieldCheck, title: 'Acesso autenticado', text: 'A plataforma exige login. Perfis de administrador, professor e aluno previstos na evolução dos acessos.', note: 'Permissões específicas por perfil em evolução' },
]

export const steps = [
  { number: '01', title: 'Cadastre as informações', text: 'Comece organizando os cadastros de professores e turmas.', icon: ClipboardList },
  { number: '02', title: 'Organize o cronograma', text: 'Reúna disciplinas, aulas e horários na grade acadêmica.', icon: CalendarDays },
  { number: '03', title: 'Visualize e acompanhe', text: 'Consulte o calendário, aplique filtros e explore cada aula.', icon: Search },
]

export const audiences = [
  { icon: University, title: 'Universidades', text: 'Uma visão conectada de cursos e disciplinas.' },
  { icon: School, title: 'Escolas', text: 'Mais clareza para a rotina de aulas e turmas.' },
  { icon: ClipboardList, title: 'Coordenações acadêmicas', text: 'O contexto necessário para acompanhar a grade.' },
  { icon: UserRound, title: 'Professores', text: 'Consulta simples aos horários e às aulas.' },
  { icon: Users, title: 'Equipes de planejamento', text: 'Informações reunidas para organizar o período letivo.' },
]

export const benefits = ['Mais organização no dia a dia', 'Uma visão centralizada da grade', 'Mais clareza para reduzir conflitos', 'Consulta rápida às informações', 'Uma experiência mais simples para a equipe']
