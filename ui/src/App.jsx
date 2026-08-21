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
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'




const AZUL = { border: 'border-blue-500', bg: 'bg-blue-500/10', text: 'text-blue-300' }
const VERDE = { border: 'border-emerald-500', bg: 'bg-emerald-500/10', text: 'text-emerald-300' }
const CINZA = { border: 'border-slate-500', bg: 'bg-slate-500/10', text: 'text-slate-300' } // fallback se não achar número

function corPorMateria(nome) {
  if (!nome) return CINZA
  const match = nome.match(/UC\s*(\d+)/i)
  if (!match) return CINZA

  const numero = parseInt(match[1], 10)
  return numero <= 6 ? AZUL : VERDE
}

function prioridadePorCor(corObj) {
  if (corObj === AZUL) return 0
  if (corObj === VERDE) return 1
  return 2 // CINZA
}



function formatLesson(lesson) {
  const t = lesson.timeslot

  return {
    date: t?.date ?? '-',
    dayOfWeek: t?.dayOfWeek ?? '-',
    time: t
      ? `${t.startTime?.slice(0, 5)} - ${t.endTime?.slice(0, 5)}`
      : '-',
    subject: lesson.subject?.name ?? '-',
    teacher: lesson.teacher ?? '-',
    room: lesson.room?.name ?? '-',
  }
}

// Timefold serializes the score as a string such as "0hard/-3soft".
// A timetable is feasible when its hard component is zero.
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


  // 1- Estado do mês atual exibido no calendário, inicializado com Julho de 2026
  const [currentMonth, setCurrentMonth] = useState(new Date(2026, 6, 1))
 
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
 
  // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional: kick off the initial fetch on mount
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
      
      // Primeiro ordena por prioridade de cor
      if (prioridadeA !== prioridadeB) {
        return prioridadeA - prioridadeB
      }
      
      // Se tiver a mesma cor, ordena por data e hora
      if (!ta || !tb) return 0
      return (ta.date + ta.startTime).localeCompare(tb.date + tb.startTime)
    })
    .map(formatLesson)
 
  // Opções únicas para popular os selects, derivadas dos dados já formatados
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
    return matchesTeacher && matchesSubject && matchesDay
  })

    // 2 - Agrupamento das aulas filtradas por data para renderizar no Grid correto do calendário
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

  // 3 - Lógica matemática para preencher os quadrados da grade mensal corretamente

  const diasDoGrid = useMemo(() => {
  const monthStart = startOfMonth(currentMonth)
  const gridStart = startOfWeek(monthStart, { weekStartsOn: 0 }) // Domingo da semana que contém o dia 1
  const gridEnd = addDays(gridStart, 41) // sempre 42 dias à frente = 6 semanas completas, ponto final
  return eachDayOfInterval({ start: gridStart, end: gridEnd })
}, [currentMonth])

  const diasDaSemana = ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB']
 
  const clearFilters = () => {
    setTeacherFilter('')
    setSubjectFilter('')
    setDayFilter('')
  }

  // 4 -Funções de manipulação do topo do calendário
  const proximoMes = () => setCurrentMonth(addMonths(currentMonth, 1))
  const mesAnterior = () => setCurrentMonth(subMonths(currentMonth, 1))
  const irParaHoje = () => setCurrentMonth(new Date())

  if (!isAuthenticated) {
      return <Login />
    }

 return (
        <div id="debug-root">
          <div className="Calendar">
 
            {error && <p className="error">Erro ao buscar /api/timetable: {error}</p>}
 
            {data && (
              <>
              <header>
                <section className="filters">
                  <select value={teacherFilter} onChange={(e) => setTeacherFilter(e.target.value)}>
                    <option value="">Todos os professores</option>
                    {teacherOptions.map((t) => (
                      <option key={t} value={t}>{t}</option>
                    ))}
                  </select>
 
                  <select value={subjectFilter} onChange={(e) => setSubjectFilter(e.target.value)}>
                    <option value="">Todas as disciplinas</option>
                    {subjectOptions.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
 
                  <select value={dayFilter} onChange={(e) => setDayFilter(e.target.value)}>
                    <option value="">Todos os dias</option>
                    {dayOptions.map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
 
                  <section className="btn">
                    <button className="update-btn" type="button" onClick={fetchTimetable} disabled={loading}>
                      {loading ? 'Carregando...' : 'Atualizar'}
                    </button>
                    <button className="clear-btn" onClick={clearFilters}>Limpar filtro</button>
                  </section>
                </section>
                </header>
 
              
              <div className="w-full rounded-xl border border-slate-800 bg-[#0f111a] text-slate-200 shadow-xl overflow-hidden mt-4">
              {/* CONTROLADORES SUPERIORES DO CALENDÁRIO */}
              <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-[#141724]">
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="icon" onClick={mesAnterior} className="h-8 w-8 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" size="icon" onClick={proximoMes} className="h-8 w-8 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" onClick={irParaHoje} className="h-8 text-xs font-semibold px-3 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    Hoje
                  </Button>
                  <h2 className="text-xl font-bold ml-2 capitalize text-white">
                    {format(currentMonth, "MMMM 'de' yyyy", { locale: ptBR })}
                  </h2>
                </div>
                
                {/* Abas estáticas de visualização */}
                <div className="flex bg-slate-900 p-0.5 text-xs font-medium text-slate-400 gap-1">
                  <button className="bg-slate-800 text-white shadow-sm px-3 py-1.5 rounded-md font-semibold">Mês</button>
                  <button className="px-3 py-1.5 rounded-md hover:text-white transition-colors" disabled>Semana</button>
                  <button className="px-3 py-1.5 rounded-md hover:text-white transition-colors" disabled>Dia</button>
                </div>
              </div>

              {/* CABEÇALHO DOS DIAS DA SEMANA */}
              <div className="grid grid-cols-7 border-b border-slate-800 bg-[#141724] text-left font-bold text-xs tracking-wider text-slate-400">
                {diasDaSemana.map((d) => (
                  <div key={d} className="p-3 border-r border-slate-800/50 last:border-r-0">{d}</div>
                ))}
              </div>

              {/* GRADE DE DIAS MENSAL */}
              <div className="grid grid-cols-7 bg-slate-950 gap-[1px]"
              style={{ gridTemplateColumns: 'repeat(7, minmax(0, 1fr))' }}
>

                {diasDoGrid.map((dia, idx) => {
                  const dataChave = format(dia, 'yyyy-MM-dd')
                  const aulasDoDia = lessonsByDate[dataChave] || []
                  const pertenceAoMesAtual = isSameMonth(dia, currentMonth)
                  const ehHoje = isSameDay(dia, new Date())
                  

                  return (
                    <div 
                      key={idx} 
                      className="bg-[#0f111a] h-[140px] min-w-0 min-h-0 p-2 flex flex-col justify-between group hover:bg-[#151926] transition-colors border border-slate-900/40 cursor-pointer"
                      onClick={() => alert(`Ação para adicionar/gerenciar o dia: ${format(dia, 'dd/MM/yyyy')}`)}
                    >
                      {/* Cabeçalho do Card (Número do dia) */}
                      <div className="flex items-center justify-between mb-1">
                        <span className={`text-xs font-bold ${
                          ehHoje 
                            ? 'bg-blue-600 text-white h-5 w-5 flex items-center justify-center rounded-full' 
                            : pertenceAoMesAtual ? 'text-slate-300' : 'text-slate-600'
                        }`}>
                          {format(dia, 'd')}
                        </span>
                      </div>

                      {/* Lista das aulas pertencentes a este dia específico */}
                       <div className="flex-1 flex flex-col gap-0.5 overflow-hidden min-w-0">
                          {aulasDoDia.slice(0, 3).map((aula, lIdx) => {
                          const cor = corPorMateria(aula.subject)
                          return (
                            <div
                              key={aula.id ?? lIdx}
                              className={`px-1.5 py-2.5 text-[10px] leading-tight rounded-sm border-l-2 ${cor.border} ${cor.bg} ${cor.text} flex items-center gap-2 shadow-sm hover:opacity-80 transition-colors cursor-pointer min-w-0`}
                              title={`${aula.time} · ${aula.subject} · ${aula.room} · ${aula.teacher}`}
                            >
                              <span className="shrink-0 font-bold text-slate-400">{aula.time?.split(' - ')[0] ?? '-'}</span>
                              <span className="truncate min-w-0 font-semibold text-slate-100">{aula.subject ?? 'Sem matéria'}</span>
                              <span className="shrink-0 text-slate-500">{aula.room ?? '-'}</span>
                              <span className="shrink-0 text-slate-500">{aula.teacher ?? '-'}</span>
                            </div>
                          )
                        })}
                        </div>
                      {/* Indicador de overflow caso o dia tenha mais de 3 aulas */}
                      {aulasDoDia.length > 3 && (
                        <div className="text-[10px] text-blue-400 font-bold mt-1 pl-1">
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
  )
}

export default App