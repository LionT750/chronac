import { useEffect, useState } from 'react'
import './App.css'

function formatLesson(lesson) {
  const t = lesson.timeslot
  return {
    date: t?.date ?? '-',
    dayOfWeek: t?.dayOfWeek ?? '-',
    time: t ? `${t.startTime?.slice(0, 5)} - ${t.endTime?.slice(0, 5)}` : '-',
    subject: lesson.subject,
    teacher: lesson.teacher,
    room: lesson.room?.name ?? '-',
  }
}

function App() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

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

  // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional: kick off the initial fetch on mount
  useEffect(fetchTimetable, [])

  const lessons = (data?.lessons ?? [])
    .slice()
    .sort((a, b) => {
      const ta = a.timeslot
      const tb = b.timeslot
      if (!ta || !tb) return 0
      return (ta.date + ta.startTime).localeCompare(tb.date + tb.startTime)
    })
    .map(formatLesson)

  return (
    <div id="debug-root">
      <header>
        <h1>Timetable debug viewer</h1>
        <button type="button" onClick={fetchTimetable} disabled={loading}>
          {loading ? 'Carregando...' : 'Atualizar'}
        </button>
      </header>

      {error && <p className="error">Erro ao buscar /api/timetable: {error}</p>}

      {data && (
        <>
          <section className="summary">
            <span><strong>Name:</strong> {data.name}</span>
            <span><strong>Score:</strong> {data.score?.hardScore}hard / {data.score?.softScore}soft</span>
            <span><strong>Feasible:</strong> {String(data.score?.feasible)}</span>
            <span><strong>Lessons:</strong> {lessons.length}</span>
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
