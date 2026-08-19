import { useEffect, useMemo, useState } from 'react';
import { formatLesson } from '../utils/timetable'

export function useTimetable() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

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

  useEffect(fetchTimetable, [])

  const allLessons = (data?.lessons ?? [])
    .slice()
    .sort((a, b) => {
      const ta = a.timeslot, tb = b.timeslot
      if (!ta || !tb) return 0
      return (ta.date + ta.startTime).localeCompare(tb.date + tb.startTime)
    })
    .map(formatLesson)

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

  const lessons = allLessons.filter((l) =>
    (!teacherFilter || l.teacher === teacherFilter) &&
    (!subjectFilter || l.subject === subjectFilter) &&
    (!dayFilter || l.dayOfWeek === dayFilter)
  )

  const clearFilters = () => {
    setTeacherFilter('')
    setSubjectFilter('')
    setDayFilter('')
  }

  return {
    data, error, loading, fetchTimetable,
    lessons, allLessons,
    teacherFilter, setTeacherFilter, teacherOptions,
    subjectFilter, setSubjectFilter, subjectOptions,
    dayFilter, setDayFilter, dayOptions,
    clearFilters,
  }
}