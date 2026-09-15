import { Filter, RefreshCw, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { weekdayOption } from '../utils/calendarPresentation'

function FilterSelect({ id, label, value, onChange, emptyLabel, options }) {
  return (
    <div className="flex min-w-0 flex-col gap-1.5">
      <label htmlFor={id} className="text-xs font-medium text-muted-foreground">{label}</label>
      <select id={id} value={value} onChange={(event) => onChange(event.target.value)}
        className="calendar-filter h-10 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm text-foreground transition-colors focus:border-ring">
        <option value="">{emptyLabel}</option>
        {options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
      </select>
    </div>
  )
}

export default function CalendarFilters({
  teacherFilter, setTeacherFilter, subjectFilter, setSubjectFilter,
  dayFilter, setDayFilter, courseFilter, setCourseFilter,
  teacherOptions, subjectOptions, dayOptions, clearFilters, loading, onRefresh,
}) {
  const activeCount = [teacherFilter, subjectFilter, dayFilter, courseFilter].filter(Boolean).length
  const options = (values) => values.map((value) => ({ value, label: value }))
  return (
    <section aria-label="Filtros do calendário" className="rounded-2xl border border-border bg-card p-4">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2 text-sm font-semibold"><Filter className="size-4 text-muted-foreground" aria-hidden="true" />Filtrar aulas</div>
        <span className="rounded-full bg-muted px-2.5 py-1 text-xs text-muted-foreground" role="status">
          {activeCount ? `Filtros ativos: ${activeCount}` : 'Todos os registros'}
        </span>
      </div>
      <div className="grid gap-3 2xl:grid-cols-[minmax(0,1fr)_auto] 2xl:items-end">
        <div className="grid min-w-0 gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <FilterSelect id="filter-teacher" label="Professor" value={teacherFilter} onChange={setTeacherFilter} emptyLabel="Todos os professores" options={options(teacherOptions)} />
          <FilterSelect id="filter-subject" label="Disciplina" value={subjectFilter} onChange={setSubjectFilter} emptyLabel="Todas as disciplinas" options={options(subjectOptions)} />
          <FilterSelect id="filter-day" label="Dia da semana" value={dayFilter} onChange={setDayFilter} emptyLabel="Todos os dias" options={dayOptions.map(weekdayOption).sort((a, b) => a.order - b.order)} />
          <FilterSelect id="filter-course" label="Curso" value={courseFilter} onChange={setCourseFilter} emptyLabel="Todos os cursos" options={[
            { value: 'azul', label: 'Jovem Programador' },
            { value: 'verde', label: 'Técnico em Desenvolvimento de Sistemas' },
          ]} />
        </div>
        <div className="flex flex-wrap items-center justify-end gap-2">
          <Button variant="ghost" className="h-10 text-muted-foreground" disabled={!activeCount} onClick={clearFilters}><X />Limpar filtros</Button>
          <Button variant="outline" className="h-10 border-primary/30 bg-course-blue-bg text-course-blue hover:bg-selected" disabled={loading} onClick={onRefresh}>
            <RefreshCw className={loading ? 'animate-spin motion-reduce:animate-none' : ''} />{loading ? 'Atualizando…' : 'Atualizar'}
          </Button>
        </div>
      </div>
    </section>
  )
}
