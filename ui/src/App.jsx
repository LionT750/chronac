import { useEffect, useMemo, useState } from 'react'
import './App.css'
import Login from './Login'
 
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
 
  const clearFilters = () => {
    setTeacherFilter('')
    setSubjectFilter('')
    setDayFilter('')
  }

  if (!isAuthenticated) {
      return <Login />
    }

  return (
    <div id="debug-root">
      <header>
        <h1>Chronac </h1>
        <button type="button" onClick={fetchTimetable} disabled={loading}>
          {loading ? 'Carregando...' : 'Atualizar'}
        </button>
      </header>
 
      {error && <p className="error">Erro ao buscar /api/timetable: {error}</p>}
 
      {data && (
        <>
          <section className="summary">
            <span><strong>Name:</strong> {data.name}</span>
            <span><strong>{hey}</strong></span>
            <span><strong>Feasible:</strong> {String(isFeasible(data.score))}</span>
            <span><strong>Lessons:</strong> {lessons.length} / {allLessons.length}</span>
          </section>
 
          <section className="filters">
            <select value={teacherFilter} onChange={(e) => {
              setTeacherFilter(e.target.value)
              setHey(`Changed teacher to ${e.target.value}`)
              }
              }>
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
 
            <button className="clear-btn"  onClick={clearFilters}>Limpar filtro</button>
          </section>
 
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Dia</th>
                <th>Horário</th>
                <th>Disciplina</th>
                <th>Professor</th>
                <th>Sala</th>
              </tr>
            </thead>
            <tbody>
              {lessons.map((l, i) => (
                <tr key={i}>
                  <td>{l.date}</td>
                  <td>{l.dayOfWeek}</td>
                  <td>{l.time}</td>
                  <td>{l.subject}</td>
                  <td>{l.teacher}</td>
                  <td>{l.room}</td>
                </tr>
              ))}
            </tbody>
          </table>
 
          <details>
            <summary>JSON bruto</summary>
            <pre>{JSON.stringify(data, null, 2)}</pre>
          </details>
        </>
      )}
    </div>
  )
}
 
export default App