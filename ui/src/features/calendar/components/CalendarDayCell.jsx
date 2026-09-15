import { format, isSameDay, isSameMonth } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { Button } from '@/components/ui/button'
import { lessonPreviewLimit } from '../utils/calendarPresentation'
import LessonCard from './LessonCard'

export default function CalendarDayCell({ day, selectedDate, lessons, viewType, hasFilters, onOpenDay, onOpenLesson }) {
  const isCurrentMonth = isSameMonth(day, selectedDate)
  const isVisible = viewType !== 'month' || isCurrentMonth
  const isToday = isSameDay(day, new Date())
  const preview = lessons.slice(0, lessonPreviewLimit(viewType))
  const remaining = lessons.length - preview.length
  const dateLabel = format(day, "EEEE, d 'de' MMMM 'de' yyyy", { locale: ptBR })

  if (!isVisible) {
    return <section aria-hidden="true" data-outside-month="true" data-view={viewType}
      className="calendar-day rounded-xl border border-border/60 bg-muted/30" />
  }

  return (
    <section aria-label={dateLabel} data-view={viewType} data-today={isToday}
      className={`calendar-day flex min-w-0 flex-col gap-2 p-2.5 ${isCurrentMonth || viewType !== 'month' ? 'bg-card' : 'bg-muted/60'}`}>
      <div className="flex min-h-9 shrink-0 items-center justify-between gap-1">
        <button type="button" data-calendar-day aria-current={isToday ? 'date' : undefined}
          aria-label={`Ver aulas de ${dateLabel}${isToday ? ', hoje' : ''}`} onClick={(event) => onOpenDay(day, event.currentTarget)}
          className={`flex h-9 min-w-9 items-center justify-center rounded-lg px-2 text-sm font-semibold transition-colors ${isToday ? 'bg-primary text-primary-foreground' : 'text-foreground hover:bg-hover'}`}>
          {format(day, 'd')}
        </button>
        {isToday ? <span className="text-xs font-medium text-course-blue">Hoje</span> : lessons.length > 0 && <span className="text-xs text-muted-foreground" aria-label={`${lessons.length} aulas`}>{lessons.length} {lessons.length === 1 ? 'aula' : 'aulas'}</span>}
      </div>
      {lessons.length ? (
        <div className={`grid min-w-0 gap-2 ${viewType === 'day' ? 'sm:grid-cols-2 2xl:grid-cols-3' : ''}`}>
          {preview.map((lesson, index) => <LessonCard key={lesson.id ?? index} lesson={lesson} detailed={viewType === 'day'} compact={viewType === 'month'} onOpen={(event) => onOpenLesson(day, lesson, event.currentTarget)} />)}
        </div>
      ) : <p className={`text-center text-xs text-muted-foreground ${viewType === 'month' ? 'py-2' : 'py-6'}`}>{hasFilters ? 'Sem resultados' : 'Sem aulas'}</p>}
      {remaining > 0 && <Button variant="ghost" className={`mt-auto w-full shrink-0 justify-start px-2 text-xs text-course-blue ${viewType === 'month' ? 'h-7' : 'h-9'}`}
        aria-label={`Ver todas as ${lessons.length} aulas de ${dateLabel}`} onClick={(event) => onOpenDay(day, event.currentTarget)}>+ {remaining} {remaining === 1 ? 'aula' : 'aulas'}{viewType !== 'month' && ' · Ver todas'}</Button>}
    </section>
  )
}
