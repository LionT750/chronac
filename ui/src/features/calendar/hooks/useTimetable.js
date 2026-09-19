import { useEffect, useState } from 'react'
import { fetchTimetable } from '../services/timetableService'
import { fetchServerDemo } from '@/services/serverDemoService'

export function useTimetable() {
  const [data, setData] = useState(null)
  const [hey, setHey] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const refreshTimetable = () => {
    setLoading(true)
    setError(null)
    fetchTimetable()
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  // A mensagem legada também é consultada na montagem; ambas as consultas alimentam o erro do calendário.
  const loadServerDemo = () => {
    fetchServerDemo()
      .then(setHey)
      .catch((err) => setError(err.message))
      .finally(() => {})
  }

  useEffect(refreshTimetable, [])
  useEffect(loadServerDemo, [])

  return { data, hey, error, loading, refreshTimetable }
}
