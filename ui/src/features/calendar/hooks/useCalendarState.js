import { useMemo, useState } from 'react'
import { academicDate, movePeriod, isSundayLesson } from '../utils/calendar'
import { prepareLessons, filterLessons, filterLessonsForMonth, groupLessonsByDate } from '../utils/lessonSelectors'

export function useCalendarState(data) {
  const [teacherFilter, setTeacherFilter] = useState('')
  const [subjectFilter, setSubjectFilter] = useState('')
  const [dayFilter, setDayFilter] = useState('')
  const [courseFilter, setCourseFilter] = useState('')
  // Usa a data do navegador apenas como estado inicial; a navegação posterior
  // permanece sob controle dos comandos da toolbar.
  const [selectedDate, setSelectedDate] = useState(() => new Date())
  const [viewType, setViewType] = useState('month') // 'month', 'week', 'day'

  const allLessons = useMemo(() => prepareLessons(data), [data])
  const teacherOptions = useMemo(
    () => [...new Set(allLessons.map((l) => l.teacher).filter(Boolean))].sort(),
    [allLessons]
  )
  const subjectOptions = useMemo(
    () => [...new Set(allLessons.map((l) => l.subject).filter(Boolean))].sort(),
    [allLessons]
  )
  const dayOptions = useMemo(
    () => [...new Set(allLessons.filter((l) => !isSundayLesson(l)).map((l) => l.dayOfWeek).filter(Boolean))].sort(),
    [allLessons]
  )

  const filteredLessons = useMemo(() => filterLessons(allLessons, {
    teacherFilter, subjectFilter, dayFilter, courseFilter,
  }), [allLessons, teacherFilter, subjectFilter, dayFilter, courseFilter])
  const visibleLessons = useMemo(
    () => (viewType === 'month' ? filterLessonsForMonth(filteredLessons, selectedDate) : filteredLessons),
    [filteredLessons, selectedDate, viewType],
  )
  const lessonsByDate = useMemo(() => groupLessonsByDate(visibleLessons), [visibleLessons])

  const clearFilters = () => {
    setTeacherFilter('')
    setSubjectFilter('')
    setDayFilter('')
    setCourseFilter('')
  }

  const onPrevious = () => setSelectedDate((date) => movePeriod(date, viewType, -1))
  const onNext = () => setSelectedDate((date) => movePeriod(date, viewType, 1))
  const onToday = () => setSelectedDate(viewType === 'day' ? academicDate(new Date()) : new Date())
  const onViewChange = (view) => {
    if (view === 'day') setSelectedDate((date) => academicDate(date))
    setViewType(view)
  }

  return {
    filters: {
      teacherFilter, setTeacherFilter, subjectFilter, setSubjectFilter,
      dayFilter, setDayFilter, courseFilter, setCourseFilter,
      teacherOptions, subjectOptions, dayOptions, clearFilters,
    },
    calendar: { selectedDate, viewType, onViewChange, onPrevious, onNext, onToday, lessonsByDate },
  }
}
