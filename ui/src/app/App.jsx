import { useState } from 'react'
import AppLayout from './layout/AppLayout'
import LoginPage from '@/features/auth/LoginPage'
import TeacherPage from '@/features/registrations/pages/TeacherPage'
import ClassPage from '@/features/registrations/pages/ClassPage'
import CalendarPage from '@/features/calendar/CalendarPage'
import { useAcademicRegistrations } from '@/features/registrations/hooks/useAcademicRegistrations'
import { useTimetable } from '@/features/calendar/hooks/useTimetable'
import { useCalendarState } from '@/features/calendar/hooks/useCalendarState'

function App() {
  const [page, setPage] = useState('calendar')
  // Estes hooks ficam acima das páginas condicionais para preservar registros, filtros e período entre telas.
  const registrations = useAcademicRegistrations()
  const timetable = useTimetable()
  const calendarState = useCalendarState(timetable.data)
  const isAuthenticated = true

  if (!isAuthenticated) return <LoginPage />

  return (
    <AppLayout page={page} onNavigate={setPage} systemStatus={timetable.error ? 'error' : timetable.loading ? 'loading' : timetable.data ? 'ready' : 'idle'}>
      {page === 'teachers' && <TeacherPage {...registrations.teachers} />}
      {page === 'classes' && <ClassPage {...registrations.classes} />}
      {page === 'calendar' && <CalendarPage {...timetable} {...calendarState} />}
    </AppLayout>
  )
}

export default App
