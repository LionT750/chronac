import { addDays, addMonths, eachDayOfInterval, endOfMonth, isSunday, parseISO, startOfMonth, startOfWeek } from 'date-fns'

export const academicWeekdays = ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB']

export function academicDate(date) {
  return isSunday(date) ? addDays(date, 1) : date
}

export function calendarDays(date, view) {
  if (view === 'day') return [academicDate(date)]
  // Seis colunas são apenas dias visíveis: o intervalo continua usando semanas de sete dias.
  const start = startOfWeek(view === 'month' ? academicDate(startOfMonth(date)) : date, { weekStartsOn: 1 })
  // No mês, inclua apenas semanas com dias visíveis, completando a última até sábado.
  const lastDay = view === 'month' ? endOfMonth(date) : date
  const lastVisibleDay = isSunday(lastDay) ? addDays(lastDay, -1) : lastDay
  const end = view === 'month'
    ? addDays(startOfWeek(lastVisibleDay, { weekStartsOn: 1 }), 5)
    : addDays(start, 6)
  return eachDayOfInterval({ start, end })
    .filter((day) => !isSunday(day))
}

export function movePeriod(date, view, direction) {
  if (view === 'month') return addMonths(date, direction)
  if (view === 'week') return addDays(date, direction * 7)
  // Ao voltar de segunda-feira, o salto deve alcançar sábado, não a segunda seguinte.
  const next = addDays(academicDate(date), direction)
  return isSunday(next) ? addDays(next, direction) : next
}

export function isSundayLesson(lesson) {
  return (lesson.date && lesson.date !== '-' && isSunday(parseISO(lesson.date))) ||
    ['SUNDAY', 'DOMINGO', 'DOM'].includes(String(lesson.dayOfWeek).toUpperCase())
}
