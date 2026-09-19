import { isSameMonth, isValid, parseISO } from 'date-fns'
import { getSubjectColor, getColorPriority, getSubjectCourse } from './lessonPresentation.js'

export function formatLesson(lesson) {
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

// O score admite os formatos string e objeto já tratados pelo frontend.
export function isFeasible(score) {
  if (typeof score === 'string') {
    const hard = score.match(/^(-?\d+)hard/)
    return hard != null && hard[1] === '0'
  }
  return score != null && score.feasible === true
}

// Copiamos a lista para preservar o payload recebido; a prioridade por curso precede a ordem cronológica.
export function prepareLessons(data) {
  return (data?.lessons ?? [])
    .slice()
    .sort((a, b) => {
      const timeslotA = a.timeslot
      const timeslotB = b.timeslot
      const colorA = getSubjectColor(a.subject?.name)
      const colorB = getSubjectColor(b.subject?.name)
      const priorityA = getColorPriority(colorA)
      const priorityB = getColorPriority(colorB)

      if (priorityA !== priorityB) {
        return priorityA - priorityB
      }

      if (!timeslotA || !timeslotB) return 0
      return (timeslotA.date + timeslotA.startTime).localeCompare(timeslotB.date + timeslotB.startTime)
    })
    .map(formatLesson)
}

export function filterLessons(lessons, { teacherFilter, subjectFilter, dayFilter, courseFilter }) {
  return lessons.filter((lesson) =>
    (!teacherFilter || lesson.teacher === teacherFilter) &&
    (!subjectFilter || lesson.subject === subjectFilter) &&
    (!dayFilter || lesson.dayOfWeek === dayFilter) &&
    (!courseFilter || getSubjectCourse(lesson.subject) === courseFilter))
}

// A grade mensal usa células adjacentes para preservar a estrutura de seis colunas,
// mas eventos fora do mês selecionado não podem aparecer nessa visualização.
export function filterLessonsForMonth(lessons, selectedDate) {
  return lessons.filter((lesson) => {
    if (!lesson.date || lesson.date === '-') return false
    const lessonDate = parseISO(lesson.date)
    return isValid(lessonDate) && isSameMonth(lessonDate, selectedDate)
  })
}

// A chave de data deve continuar igual à usada nas células; aulas sem alocação não entram na grade.
export function groupLessonsByDate(lessons) {
  return lessons.reduce((grouped, lesson) => {
    if (!lesson.date || lesson.date === '-') return grouped
    if (!grouped[lesson.date]) grouped[lesson.date] = []
    grouped[lesson.date].push(lesson)
    return grouped
  }, {})
}
