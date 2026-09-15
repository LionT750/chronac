import CalendarFilters from './components/CalendarFilters'
import CalendarView from './components/CalendarView'
import { Skeleton } from '@/components/ui/skeleton'

export default function CalendarPage({ data, error, loading, refreshTimetable, filters, calendar }) {
  const hasFilters = [filters.teacherFilter, filters.subjectFilter, filters.dayFilter, filters.courseFilter].some(Boolean)

  return (
    <div id="debug-root" className="Calendar">
      {error && <p className="error" role="alert">Erro ao buscar /api/timetable: {error}</p>}
      {!data && loading && (
        <section aria-label="Carregando calendário" className="rounded-2xl border border-border bg-card p-4 shadow-panel">
          <div className="mb-4 flex items-center justify-between">
            <Skeleton className="h-5 w-36" />
            <Skeleton className="h-8 w-24 rounded-full" />
          </div>
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {[1, 2, 3, 4].map((item) => <Skeleton key={item} className="h-10 w-full" />)}
          </div>
          <Skeleton className="mt-5 h-[420px] w-full rounded-2xl" />
        </section>
      )}
      {!data && !loading && error && (
        <section className="rounded-2xl border border-destructive/40 bg-destructive-bg p-5" aria-label="Falha ao carregar calendário">
          <h2 className="text-base font-semibold text-foreground">Não foi possível carregar a grade</h2>
          <p className="mt-1 text-sm text-muted-foreground">Verifique a conexão com o servidor e tente novamente.</p>
          <button type="button" onClick={refreshTimetable} className="mt-4 inline-flex h-9 items-center rounded-lg bg-primary px-3 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/80">Tentar novamente</button>
        </section>
      )}
      {data && (
        <>
          <CalendarFilters {...filters} loading={loading} onRefresh={refreshTimetable} />
          <CalendarView {...calendar} hasFilters={hasFilters} />
        </>
      )}
    </div>
  )
}
