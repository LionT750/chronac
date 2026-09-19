import { useMemo, useState } from 'react'
import { format, startOfWeek, addDays } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { CalendarDays, LayoutGrid, GraduationCap, Users, SlidersHorizontal, PanelLeft, Check } from 'lucide-react'
import CalendarView from '@/features/calendar/components/CalendarView'
import LessonCard from '@/features/calendar/components/LessonCard'
import { academicDate, calendarDays, movePeriod } from '@/features/calendar/utils/calendar'
import { getSubjectCourse } from '@/features/calendar/utils/lessonPresentation'
import { createDemoLessons } from '../demoData'
import { Brand } from './LandingShared'

function PreviewFrame({ children, compact }) {
  return <div className={`landing-preview${compact ? ' landing-preview-compact' : ''}`}>
    <aside className="landing-preview-sidebar" aria-label="Contexto da interface demonstrativa">
      <Brand linked={false} />
      <p className="landing-preview-menu-label">ÁREA ACADÊMICA</p>
      <div className="landing-preview-nav-item is-active"><LayoutGrid size={15} />Visão geral</div>
      <div className="landing-preview-nav-item"><Users size={15} />Professores</div>
      <div className="landing-preview-nav-item"><GraduationCap size={15} />Turmas</div>
      <div className="landing-preview-status"><span className="landing-status-dot" />Ambiente demonstrativo</div>
    </aside>
    <div className="landing-preview-main">
      <div className="landing-preview-topbar"><span><PanelLeft size={15} /><span className="landing-preview-divider" /><CalendarDays size={15} />Cronograma acadêmico</span><span className="landing-beta">Beta</span></div>
      {children}
    </div>
  </div>
}

export function HeroPreview() {
  const [today] = useState(() => new Date())
  const days = Array.from({ length: 6 }, (_, index) => addDays(startOfWeek(today, { weekStartsOn: 1 }), index))
  const lessons = createDemoLessons(days)
  return <figure className="landing-hero-preview">
    <PreviewFrame compact>
      <div className="landing-preview-body">
        <div className="landing-static-filters"><span><SlidersHorizontal size={14} />Sua grade, do seu jeito</span><span>Todos os professores</span><span>Todos os cursos</span></div>
        <div className="landing-mini-calendar">
          <div className="landing-mini-toolbar"><strong className="capitalize">{format(today, 'MMMM yyyy', { locale: ptBR })}</strong><span>Visão semanal</span></div>
          <div className="landing-mini-grid">{days.map((day) => <div className="landing-mini-day" key={day.toISOString()}>
            <div className="landing-mini-date"><span>{format(day, 'EEE', { locale: ptBR })}</span><strong>{format(day, 'dd')}</strong></div>
            {lessons[format(day, 'yyyy-MM-dd')].slice(0, 2).map((lesson) => <LessonCard key={lesson.id} lesson={lesson} />)}
          </div>)}</div>
        </div>
      </div>
    </PreviewFrame>
    <figcaption><span><Check size={14} />Interface do Chronac · dados ilustrativos</span><a href="#produto">Explore o calendário <span aria-hidden="true">↗</span></a></figcaption>
  </figure>
}

export default function ProductPreview() {
  const [selectedDate, setSelectedDate] = useState(() => academicDate(new Date()))
  const [viewType, setViewType] = useState('week')
  const [course, setCourse] = useState('')
  const lessonsByDate = useMemo(() => {
    const lessons = createDemoLessons(calendarDays(selectedDate, viewType))
    return Object.fromEntries(Object.entries(lessons).map(([date, entries]) => [date, entries.filter((lesson) => !course || getSubjectCourse(lesson.subject) === course)]))
  }, [selectedDate, viewType, course])

  return <PreviewFrame>
    <div className="landing-preview-body">
      <div className="landing-demo-controls">
        <label htmlFor="landing-course"><SlidersHorizontal size={16} />Filtrar por curso</label>
        <select id="landing-course" value={course} onChange={(event) => setCourse(event.target.value)}>
          <option value="">Todos os cursos</option><option value="azul">Jovem Programador</option><option value="verde">Técnico em Desenvolvimento de Sistemas</option>
        </select>
        <span>Dados fictícios para explorar a interface</span>
      </div>
      <CalendarView selectedDate={selectedDate} viewType={viewType} lessonsByDate={lessonsByDate} hasFilters={Boolean(course)}
        onViewChange={(view) => { setViewType(view); if (view === 'day') setSelectedDate(academicDate(selectedDate)) }}
        onPrevious={() => setSelectedDate(movePeriod(selectedDate, viewType, -1))}
        onNext={() => setSelectedDate(movePeriod(selectedDate, viewType, 1))}
        onToday={() => setSelectedDate(academicDate(new Date()))} />
    </div>
  </PreviewFrame>
}
