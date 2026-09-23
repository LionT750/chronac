import { lazy, Suspense, useEffect, useState } from 'react'
import AppLayout from './layout/AppLayout'
import AuthGate from '@/features/auth/AuthGate'
import TeacherPage from '@/features/registrations/pages/TeacherPage'
import ClassPage from '@/features/registrations/pages/ClassPage'
import CalendarPage from '@/features/calendar/CalendarPage'
import { useAcademicRegistrations } from '@/features/registrations/hooks/useAcademicRegistrations'
import { useTimetable } from '@/features/calendar/hooks/useTimetable'
import { useCalendarState } from '@/features/calendar/hooks/useCalendarState'
import LandingPage from '@/features/landing/LandingPage'
import { useAcademicStructure } from '@/features/academic-structure/hooks/useAcademicStructure'
import AcademicStructureLoading from '@/features/academic-structure/components/AcademicStructureLoading'

const AcademicStructure = lazy(() => import('@/features/academic-structure/AcademicStructure'))
const pagePaths = { calendar: '/', teachers: '/teachers', classes: '/classes', 'academic-structure': '/academic-structure' }

function AuthenticatedApp({ onLogout, loggingOut, page, onNavigate, registrations }) {
  // Mantém filtros e período ao alternar entre as páginas autenticadas.
  const timetable = useTimetable()
  const calendarState = useCalendarState(timetable.data)

  return (
    <AppLayout page={page} onNavigate={onNavigate} onLogout={onLogout} loggingOut={loggingOut} systemStatus={timetable.error ? 'error' : timetable.loading ? 'loading' : timetable.data ? 'ready' : 'idle'}>
      {page === 'teachers' && <TeacherPage {...registrations.teachers} />}
      {page === 'classes' && <ClassPage {...registrations.classes} />}
      {page === 'calendar' && <CalendarPage {...timetable} {...calendarState} />}
    </AppLayout>
  )
}

export default function App() {
  const [pathname, setPathname] = useState(window.location.pathname)
  const academicStructure = useAcademicStructure()
  const registrations = useAcademicRegistrations()

  useEffect(() => {
    const onPopState = () => setPathname(window.location.pathname)
    window.addEventListener('popstate', onPopState)
    window.addEventListener('chronac:navigate', onPopState)
    return () => {
      window.removeEventListener('popstate', onPopState)
      window.removeEventListener('chronac:navigate', onPopState)
    }
  }, [])

  function navigate(path) {
    if (window.location.pathname !== path) window.history.pushState(null, '', path)
    setPathname(path)
  }

  const onNavigate = (page) => navigate(pagePaths[page] ?? '/')
  const academicRoute = pathname.match(/^\/academic-structure(?:\/([^/]+))?\/?$/)

  if (/^\/chronac\/?$/.test(pathname)) return <LandingPage />
  // O MVP acadêmico é independente da sessão e dos serviços do calendário.
  if (academicRoute) return <AppLayout page="academic-structure" onNavigate={onNavigate}>
    <Suspense fallback={<AcademicStructureLoading />}>
      <AcademicStructure {...academicStructure} classId={academicRoute[1]} onNavigate={navigate} />
    </Suspense>
  </AppLayout>

  const page = pathname === '/teachers' ? 'teachers' : pathname === '/classes' ? 'classes' : 'calendar'
  return <AuthGate component={AuthenticatedApp} componentProps={{ page, onNavigate, registrations }} />
}
