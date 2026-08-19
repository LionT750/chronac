import { AppShell } from './components/layout/AppShell'
import { AppSidebar } from './components/layout/AppSidebar'
import { CalendarFilters } from './components/calendar/CalendarFilters'
import { LessonsTable } from './components/calendar/LessonsTable'
import { useTimetable } from './hooks/useTimetable'
import { isFeasible } from './utils/timetable'

function App() {
  const isAuthenticated = true
  const t = useTimetable()

  return (
    <AppShell sidebar={<AppSidebar />}>
      <header className="flex items-start gap-4">
        <h1 className="text-xl font-bold">Chronac</h1>
        <button onClick={t.fetchTimetable} disabled={t.loading}>
          {t.loading ? 'Carregando...' : 'Atualizar'}
        </button>
      </header>

      {t.error && <p className="text-destructive">Erro: {t.error}</p>}

      {t.data && (
        <>
          <section className="flex flex-wrap gap-6 text-sm">
            <span><strong>Feasible:</strong> {String(isFeasible(t.data.score))}</span>
            <span><strong>Lessons:</strong> {t.lessons.length} / {t.allLessons.length}</span>
          </section>

          <CalendarFilters {...t} onClear={t.clearFilters} />
          <LessonsTable lessons={t.lessons} />
        </>
      )}
    </AppShell>
  )
}

export default App