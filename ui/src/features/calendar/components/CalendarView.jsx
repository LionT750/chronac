import { useMemo, useRef, useState } from 'react'
import { format } from 'date-fns'
import { academicWeekdays, calendarDays } from '../utils/calendar'
import { countVisibleLessons } from '../utils/calendarPresentation'
import CalendarToolbar from './CalendarToolbar'
import CalendarDayCell from './CalendarDayCell'
import CalendarDayDetails from './CalendarDayDetails'

export default function CalendarView({ selectedDate, viewType, onViewChange, onPrevious, onNext, onToday, lessonsByDate, hasFilters = false }) {
  const days = useMemo(() => calendarDays(selectedDate, viewType), [selectedDate, viewType])
  const [selection, setSelection] = useState(null)
  const returnFocus = useRef(null)
  const count = countVisibleLessons(days, lessonsByDate)

  function openDetails(day, lesson, trigger) {
    // Abertura programática: devolvemos o foco ao controle que originou o painel.
    returnFocus.current = trigger
    setSelection({ day, lesson })
  }

  function navigateDays(event) {
    if (!event.target.hasAttribute('data-calendar-day')) return
    const buttons = [...event.currentTarget.querySelectorAll('[data-calendar-day]')]
    const current = buttons.indexOf(event.target)
    const columns = viewType === 'day' ? 1 : 6
    const targets = { ArrowLeft: current - 1, ArrowRight: current + 1, ArrowUp: current - columns, ArrowDown: current + columns,
      Home: current - current % columns, End: Math.min(current - current % columns + columns - 1, buttons.length - 1) }
    if (!(event.key in targets)) return
    event.preventDefault()
    buttons[Math.max(0, Math.min(targets[event.key], buttons.length - 1))]?.focus()
  }

  return (
    <>
      <div className="w-full overflow-hidden rounded-2xl border border-border bg-card text-foreground shadow-panel">
        <CalendarToolbar selectedDate={selectedDate} viewType={viewType} onViewChange={onViewChange}
          onPrevious={onPrevious} onNext={onNext} onToday={onToday} />
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border px-4 py-3">
          <p role="status" aria-atomic="true" className="text-xs text-muted-foreground">{count} {count === 1 ? 'aula nos dias exibidos' : 'aulas nos dias exibidos'}{hasFilters ? ' · Com filtros' : ''}</p>
          <div aria-label="Legenda dos cursos" className="flex flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
            <span className="flex items-center gap-1.5"><span className="size-2 rounded-full bg-course-blue" aria-hidden="true" />Jovem Programador</span>
            <span className="flex items-center gap-1.5"><span className="size-2 rounded-full bg-course-green" aria-hidden="true" />Técnico em Desenvolvimento de Sistemas</span>
          </div>
        </div>
        {!count && <p className="border-b border-border bg-muted/50 px-4 py-3 text-sm text-muted-foreground">{hasFilters ? 'Nenhuma aula corresponde aos filtros nos dias exibidos. Ajuste ou limpe os filtros para ampliar a busca.' : 'Não há aulas nos dias exibidos. Navegue para outro período ou atualize a grade.'}</p>}
        {viewType !== 'day' && <p className="px-4 py-2 text-xs text-muted-foreground xl:hidden">Deslize a grade horizontalmente para consultar todos os dias.</p>}
        <div className="calendar-scroll" role="region" aria-label="Grade de aulas. Use Tab para acessar os dias e as setas para navegar entre eles." tabIndex={0} onKeyDown={navigateDays}>
          <div className={viewType === 'day' ? '' : 'calendar-six-days'}>
            {viewType !== 'day' && <div className="grid grid-cols-6 border-b border-border bg-surface-raised text-xs font-semibold tracking-wide text-muted-foreground">
              {academicWeekdays.map((day) => <div key={day} className="px-3 py-3">{day}</div>)}
            </div>}
            <div className={`grid gap-px bg-border ${viewType === 'day' ? 'grid-cols-1' : 'grid-cols-6'}`}>
              {days.map((day) => <CalendarDayCell key={format(day, 'yyyy-MM-dd')} day={day} selectedDate={selectedDate}
                lessons={lessonsByDate[format(day, 'yyyy-MM-dd')] ?? []} viewType={viewType} hasFilters={hasFilters}
                onOpenDay={(date, trigger) => openDetails(date, null, trigger)} onOpenLesson={openDetails} />)}
            </div>
          </div>
        </div>
      </div>
      <CalendarDayDetails selection={selection} lessonsByDate={lessonsByDate} hasFilters={hasFilters} returnFocus={returnFocus}
        onClose={() => setSelection(null)} onShowDay={() => setSelection((current) => ({ ...current, lesson: null }))} />
    </>
  )
}
