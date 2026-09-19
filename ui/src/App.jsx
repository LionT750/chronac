import { useEffect, useMemo, useState } from 'react'
import './App.css'
import Login from './Login'
import {
  Sidebar,
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
  SidebarContent,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarHeader,
  SidebarFooter,
} from '@/components/ui/sidebar'

import {
  format,
  startOfMonth,
  endOfMonth,
  eachDayOfInterval,
  startOfWeek,
  endOfWeek,
  isSameMonth,
  isSameDay,
  addMonths,
  subMonths,
  addDays,
} from 'date-fns'


import { ptBR } from 'date-fns/locale'
import {
  BookOpenText,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Filter,
  LayoutGrid,
  RefreshCw,
  Sparkles,
} from 'lucide-react'
import { Button } from '@/components/ui/button'

const AZUL = { border: 'border-blue-500', bg: 'bg-blue-500/10', text: 'text-blue-300' }
const VERDE = { border: 'border-emerald-500', bg: 'bg-emerald-500/10', text: 'text-emerald-300' }
const CINZA = { border: 'border-slate-500', bg: 'bg-slate-500/10', text: 'text-slate-300' }

const sidebarItems = [
  { title: 'Visão geral', icon: LayoutGrid, active: true },
]

function corPorMateria(nome) {
  if (!nome) return CINZA
  const match = nome.match(/UC\s*(\d+)/i)
  if (!match) return CINZA

  const numero = parseInt(match[1], 10)
  return numero <= 6 ? AZUL : VERDE
}

function cursoPorMateria(nome) {
  if (!nome) return ''
  const match = nome.match(/UC\s*(\d+)/i)
  if (!match) return ''

  const numero = parseInt(match[1], 10)
  return numero <= 6 ? 'azul' : 'verde'
}

function prioridadePorCor(corObj) {
  if (corObj === AZUL) return 0
  if (corObj === VERDE) return 1
  return 2
}

function formatLesson(lesson) {
  const t = lesson.timeslot

  return {
    date: t?.date ?? '-',
    dayOfWeek: t?.dayOfWeek ?? '-',
    time: t ? `${t.startTime?.slice(0, 5)} - ${t.endTime?.slice(0, 5)}` : '-',
    subject: lesson.subject?.name ?? '-',
    teacher: lesson.teacher ?? '-',
    room: lesson.room?.name ?? '-',
  }
}

function isFeasible(score) {
  if (typeof score === 'string') {
    const hard = score.match(/^(-?\d+)hard/)
    return hard != null && hard[1] === '0'
  }
  return score != null && score.feasible === true
}

function App() {
  const [data, setData] = useState(null)
  const [hey, setHey] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const isAuthenticated = true

  const [teacherFilter, setTeacherFilter] = useState('')
  const [subjectFilter, setSubjectFilter] = useState('')
  const [dayFilter, setDayFilter] = useState('')
  const [courseFilter, setCourseFilter] = useState('')
  const [currentMonth, setCurrentMonth] = useState(new Date(2026, 6, 1))  
  const [viewType, setViewType] = useState('month') // 'month', 'week', 'day'
  const fetchTimetable = () => {
    setLoading(true)
    setError(null)
    fetch('/api/timetable')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  const fetchHeyMaster = () => {
    fetch('api/sayHeyMaster')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(setHey)
      .catch((err) => setError(err.message))
      .finally(() => {})
  }

  useEffect(fetchTimetable, [])
  useEffect(fetchHeyMaster, [])

  const allLessons = (data?.lessons ?? [])
    .slice()
    .sort((a, b) => {
      const ta = a.timeslot
      const tb = b.timeslot
      const corA = corPorMateria(a.subject?.name)
      const corB = corPorMateria(b.subject?.name)
      const prioridadeA = prioridadePorCor(corA)
      const prioridadeB = prioridadePorCor(corB)

      if (prioridadeA !== prioridadeB) {
        return prioridadeA - prioridadeB
      }

      if (!ta || !tb) return 0
      return (ta.date + ta.startTime).localeCompare(tb.date + tb.startTime)
    })
    .map(formatLesson)

  const teacherOptions = useMemo(
    () => [...new Set(allLessons.map((l) => l.teacher).filter(Boolean))].sort(),
    [allLessons]
  )
  const subjectOptions = useMemo(
    () => [...new Set(allLessons.map((l) => l.subject).filter(Boolean))].sort(),
    [allLessons]
  )
  const dayOptions = useMemo(
    () => [...new Set(allLessons.map((l) => l.dayOfWeek).filter(Boolean))].sort(),
    [allLessons]
  )

  const lessons = allLessons.filter((l) => {
    const matchesTeacher = !teacherFilter || l.teacher === teacherFilter
    const matchesSubject = !subjectFilter || l.subject === subjectFilter
    const matchesDay = !dayFilter || l.dayOfWeek === dayFilter
    const matchesCourse = !courseFilter || cursoPorMateria(l.subject) === courseFilter
    return matchesTeacher && matchesSubject && matchesDay && matchesCourse
  })

  const lessonsByDate = useMemo(() => {
    return lessons.reduce((acc, lesson) => {
      if (!lesson.date || lesson.date === '-') return acc
      if (!acc[lesson.date]) {
        acc[lesson.date] = []
      }
      acc[lesson.date].push(lesson)
      return acc
    }, {})
  }, [lessons])

  const diasDoGrid = useMemo(() => {
    if (viewType === 'week') {
      // Visão semanal: mostra 7 dias da semana
      const weekStart = startOfWeek(currentMonth, { weekStartsOn: 0 })
      const weekEnd = endOfWeek(currentMonth, { weekStartsOn: 0 })
      return eachDayOfInterval({ start: weekStart, end: weekEnd })
    }
    if (viewType === 'day') {
      // Visão diária: mostra só o dia selecionado
      return [currentMonth]
    }
    // Visão mensal: mostra todo o mês
    const monthStart = startOfMonth(currentMonth)
    const gridStart = startOfWeek(monthStart, { weekStartsOn: 0 })
    const gridEnd = addDays(gridStart, 41)
    return eachDayOfInterval({ start: gridStart, end: gridEnd })
  }, [currentMonth, viewType])

  const diasDaSemana = ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB']

  const clearFilters = () => {
    setTeacherFilter('')
    setSubjectFilter('')
    setDayFilter('')
    setCourseFilter('')
  }

  const diaAnterior = () => setCurrentMonth(addDays(currentMonth, -1))
  const proximoDia = () => setCurrentMonth(addDays(currentMonth, 1))
  const semanaAnterior = () => setCurrentMonth(addDays(currentMonth, -7))
  const proximaSemana = () => setCurrentMonth(addDays(currentMonth, 7))
  const mesAnterior = () => setCurrentMonth(subMonths(currentMonth, 1))
  const proximoMes = () => setCurrentMonth(addMonths(currentMonth, 1))

  const navegarPeriodoAnterior = () => {
    if (viewType === 'month') mesAnterior()
    else if (viewType === 'week') semanaAnterior()
    else diaAnterior()
  }

  const navegarProximoPeriodo = () => {
    if (viewType === 'month') proximoMes()
    else if (viewType === 'week') proximaSemana()
    else proximoDia()
  }

  const irParaHoje = () => setCurrentMonth(new Date())

  if (!isAuthenticated) {
    return <Login />
  }

  return (
    <div className="min-h-screen bg-[#0b1020] text-slate-100">
      <SidebarProvider defaultOpen>
        <Sidebar
          collapsible="icon"
          className="border-r border-slate-800 bg-[#101827] text-slate-200 shadow-2xl shadow-slate-950/40"
        >
          <SidebarHeader className="border-b border-slate-800 px-3 py-4 group-data-[collapsible=icon]:px-2 group-data-[collapsible=icon]:py-3">
            {/* Logo Chronac */}
            <div className="flex items-center justify-center gap-3 group-data-[collapsible=icon]:justify-center">
              <img 
                src="/logo.png" 
                alt="Chronac Logo"
                className="h-16 object-contain group-data-[collapsible=icon]:hidden"
              />
              <img 
                src="/logo_p.png" 
                alt="Chronac Compact Logo"
                className="hidden h-10 w-10 object-contain group-data-[collapsible=icon]:block"
              />
            </div>
          </SidebarHeader>

          <SidebarContent className="px-2 py-3 group-data-[collapsible=icon]:px-1">
            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  {sidebarItems.map(({ title, icon: Icon, active }) => (
                    <SidebarMenuItem key={title}>
                      <SidebarMenuButton
                        isActive={active}
                        className={
                          active
                            ? 'bg-blue-500/10 text-blue-200 hover:bg-blue-500/15 hover:text-blue-100 group-data-[collapsible=icon]:justify-center'
                            : 'text-slate-300 hover:bg-slate-800 hover:text-white group-data-[collapsible=icon]:justify-center'
                        }
                      >
                        <Icon className="h-4 w-4" />
                        <span className="group-data-[collapsible=icon]:hidden">{title}</span>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarContent>

          <SidebarFooter className="border-t border-slate-800 p-3 group-data-[collapsible=icon]:hidden">
            <div className="flex items-center gap-3 rounded-lg border border-slate-700 bg-slate-900/70 p-2.5 text-left">
              <div className="flex h-8 w-8 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-300">
                <Sparkles className="h-4 w-4" />
              </div>
              <div className="min-w-0">
                <div className="text-xs font-medium text-white">Sistema ativo</div>
                <div className="truncate text-[10px] text-slate-400">Sincronizado com a grade</div>
              </div>
            </div>
          </SidebarFooter>
        </Sidebar>

        <SidebarInset className="bg-[#0b1020]">
          <div className="flex items-center justify-between border-b border-slate-800 bg-[#101827]/80 px-4 py-3 backdrop-blur-sm">
            <div className="flex items-center gap-3">
              <SidebarTrigger className="h-9 w-9 border border-slate-700 bg-slate-800/70 text-slate-200 hover:bg-slate-700" />
              <div className="flex items-center gap-2 text-slate-200">
                <CalendarDays className="h-4 w-4 text-blue-300" />
                <span className="text-sm font-semibold tracking-wide">Cronograma Acadêmico</span>
              </div>
            </div>

            <div className="rounded-full border border-emerald-500/40 bg-emerald-500/10 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-emerald-300">
              Versão beta
            </div>
          </div>

          <div className="p-5 md:p-7">
            <div id="debug-root" className="Calendar">
              {error && <p className="error">Erro ao buscar /api/timetable: {error}</p>}

              {data && (
                <>
                  <section className="mb-5 rounded-2xl border border-slate-800 bg-[#111827]/90 p-4 shadow-xl shadow-slate-950/30">
                    <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
                      <div className="flex flex-1 flex-col gap-3 md:flex-row md:flex-wrap">
                        <select
                          value={teacherFilter}
                          onChange={(e) => setTeacherFilter(e.target.value)}
                          className="min-w-[180px] flex-1 rounded-md border border-slate-700 bg-slate-900/80 px-3 py-2 text-sm text-slate-100 outline-none ring-0 transition focus:border-blue-500"
                        >
                          <option value="">Todos os professores</option>
                          {teacherOptions.map((t) => (
                            <option key={t} value={t}>{t}</option>
                          ))}
                        </select>

                        <select
                          value={subjectFilter}
                          onChange={(e) => setSubjectFilter(e.target.value)}
                          className="min-w-[180px] flex-1 rounded-md border border-slate-700 bg-slate-900/80 px-3 py-2 text-sm text-slate-100 outline-none ring-0 transition focus:border-blue-500"
                        >
                          <option value="">Todas as disciplinas</option>
                          {subjectOptions.map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>

                        <select
                          value={dayFilter}
                          onChange={(e) => setDayFilter(e.target.value)}
                          className="min-w-[150px] flex-1 rounded-md border border-slate-700 bg-slate-900/80 px-3 py-2 text-sm text-slate-100 outline-none ring-0 transition focus:border-blue-500"
                        >
                          <option value="">Todos os dias</option>
                          {dayOptions.map((d) => (
                            <option key={d} value={d}>{d}</option>
                          ))}
                        </select>

                        <select
                          value={courseFilter}
                          onChange={(e) => setCourseFilter(e.target.value)}
                          className="min-w-[220px] flex-1 rounded-md border border-slate-700 bg-slate-900/80 px-3 py-2 text-sm text-slate-100 outline-none ring-0 transition focus:border-blue-500"
                        >
                          <option value="">Todos os cursos</option>
                          <option value="azul">Jovem Programador</option>
                          <option value="verde">Técnico em Desenvolvimento de Sistemas</option>
                        </select>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={fetchTimetable}
                          disabled={loading}
                          className="inline-flex items-center gap-2 rounded-md border border-blue-500/40 bg-blue-500/10 px-3 py-2 text-sm font-medium text-blue-200 transition hover:bg-blue-500/20 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                          {loading ? 'Carregando...' : 'Atualizar'}
                        </button>
                        <button
                          type="button"
                          onClick={clearFilters}
                          className="rounded-md border border-slate-700 bg-slate-800/60 px-3 py-2 text-sm font-medium text-slate-200 transition hover:bg-slate-700"
                        >
                          Limpar filtro
                        </button>
                      </div>
                    </div>
                  </section>

                  <div className="w-full overflow-hidden rounded-2xl border border-slate-800 bg-[#0f111a] text-slate-200 shadow-xl shadow-slate-950/30">
                    <div className="flex items-center justify-between border-b border-slate-800 bg-[#141724] p-4">
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="icon"
                          onClick={navegarPeriodoAnterior}
                          className="h-8 w-8 border-slate-700 bg-slate-800/50 text-white hover:bg-slate-800"
                        >
                          <ChevronLeft className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="icon"
                          onClick={navegarProximoPeriodo}
                          className="h-8 w-8 border-slate-700 bg-slate-800/50 text-white hover:bg-slate-800"
                        >
                          <ChevronRight className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          onClick={irParaHoje}
                          className="h-8 border-slate-700 bg-slate-800/50 px-3 text-xs font-semibold text-white hover:bg-slate-800"
                        >
                          Hoje
                        </Button>
                        <h2 className="ml-2 text-xl font-bold capitalize text-white">
                          {viewType === 'month' && format(currentMonth, "MMMM 'de' yyyy", { locale: ptBR })}
                          {viewType === 'week' && `Semana de ${format(startOfWeek(currentMonth, { weekStartsOn: 0 }), 'd MMMM', { locale: ptBR })}`}
                          {viewType === 'day' && format(currentMonth, "dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
                        </h2>
                      </div>

                      <div className="flex gap-1 rounded-lg bg-slate-900 p-0.5 text-xs font-medium text-slate-400">
                        <button 
                          onClick={() => setViewType('month')}
                          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'month' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                          Mês
                        </button>
                        <button 
                          onClick={() => setViewType('week')}
                          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'week' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                          Semana
                        </button>
                        <button 
                          onClick={() => setViewType('day')}
                          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'day' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                          Dia
                        </button>
                      </div>
                    </div>

                    {viewType !== 'day' && (
                      <div className="grid grid-cols-7 border-b border-slate-800 bg-[#141724] text-left text-xs font-bold tracking-wider text-slate-400">
                        {diasDaSemana.map((d) => (
                          <div key={d} className="border-r border-slate-800/50 p-3 last:border-r-0">
                            {d}
                          </div>
                        ))}
                      </div>
                    )}

                    <div
                      className={`grid gap-[1px] bg-slate-950 ${viewType === 'day' ? 'grid-cols-1' : 'grid-cols-7'}`}
                    >
                      {diasDoGrid.map((dia, idx) => {
                        const dataChave = format(dia, 'yyyy-MM-dd')
                        const aulasDoDia = lessonsByDate[dataChave] || []
                        const pertenceAoMesAtual = isSameMonth(dia, currentMonth)
                        const ehHoje = isSameDay(dia, new Date())

                        return (
                          <div
                            key={idx}
                            className="h-[140px] min-h-0 min-w-0 cursor-pointer border border-slate-900/40 bg-[#0f111a] p-2 transition-colors hover:bg-[#151926]"
                            onClick={() => alert(`Ação para adicionar/gerenciar o dia: ${format(dia, 'dd/MM/yyyy')}`)}
                          >
                            <div className="flex items-center justify-between">
                              <span
                                className={`text-xs font-bold ${
                                  ehHoje
                                    ? 'flex h-5 w-5 items-center justify-center rounded-full bg-blue-600 text-white'
                                    : pertenceAoMesAtual
                                      ? 'text-slate-300'
                                      : 'text-slate-600'
                                }`}
                              >
                                {format(dia, 'd')}
                              </span>
                            </div>

                            <div className="mt-1 flex min-w-0 flex-1 flex-col gap-1 overflow-hidden justify-evenly">
                              {aulasDoDia.slice(0, 3).map((aula, lIdx) => {
                                const cor = corPorMateria(aula.subject)
                                return (
                                  <div
                                    key={aula.id ?? lIdx}
                                    className={`w-full py-2 min-w-0 overflow-hidden rounded-md border-l-2 px-2 py-1 shadow-sm transition-colors hover:opacity-80 ${cor.border} ${cor.bg} ${cor.text}`}
                                    title={`${aula.time} · ${aula.subject} · ${aula.room} · ${aula.teacher}`}
                                  >
                                    <div className="flex gap-11.5 min-w-0">
                                      <div className="flex items-center gap-1.5 min-w-0">
                                        <span className="shrink-0 text-[11px] font-bold leading-tight">
                                          {aula.time?.split(' - ')[0] ?? '-'}
                                        </span>
                                        <span className="min-w-0 truncate text-[12px] font-semibold leading-tight">
                                          {aula.subject ?? "Sem matéria"}
                                        </span>
                                      </div>
                                      <div className="min-w-0 truncate text-[10px] text-slate-300 leading-tight">
                                        {aula.teacher}
                                        {aula.teacher && aula.room ? " · " : ""}
                                        {aula.room}
                                      </div>
                                    </div>
                                  </div>
                                )
                              })}
                            </div>

                            {aulasDoDia.length > 3 && (
                              <div className="mt-1 pl-2 text-[10px] font-bold text-blue-400">
                                + {aulasDoDia.length - 3} aulas
                              </div>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </SidebarInset>
      </SidebarProvider>
    </div>
  )
}


export default App