import { Clock3, MapPin, UserRound } from 'lucide-react'
import { getSubjectColor, getSubjectCourse } from '../utils/lessonPresentation'
import { cn } from '@/lib/utils'

export default function LessonCard({ lesson, detailed = false, compact = false, onOpen }) {
  const color = getSubjectColor(lesson.subject)
  const course = getSubjectCourse(lesson.subject)
  const startTime = String(lesson.time ?? '').match(/\b\d{1,2}:\d{2}\b/)?.[0]
  const content = (
    <>
      {!compact && <span className={`flex items-center gap-1.5 text-xs font-medium tabular-nums ${color.text}`}>
        <Clock3 className="size-3.5 shrink-0" aria-hidden="true" />{lesson.time}
      </span>}
      {compact ? (
        <span className="flex min-w-0 items-center gap-1.5 text-xs leading-snug">
          {startTime && <span className={`shrink-0 font-medium tabular-nums ${color.text}`}>{startTime}<span aria-hidden="true"> ·</span></span>}
          <span className="min-w-0 truncate font-semibold text-foreground">{lesson.subject}</span>
        </span>
      ) : <span className={cn('block font-semibold leading-snug text-foreground', detailed ? 'break-words text-sm' : 'line-clamp-1 text-xs')}>{lesson.subject}</span>}
      {!compact && (detailed ? (
        <span className="grid gap-2 text-sm text-muted-foreground">
          <span className="flex items-start gap-2"><UserRound className="mt-0.5 size-4 shrink-0" aria-hidden="true" /><span className="min-w-0 break-words"><span className="sr-only">Professor: </span>{lesson.teacher}</span></span>
          <span className="flex items-start gap-2"><MapPin className="mt-0.5 size-4 shrink-0" aria-hidden="true" /><span className="min-w-0 break-words"><span className="sr-only">Sala: </span>{lesson.room}</span></span>
          {course && <span className={`w-fit rounded-full border px-2 py-1 text-xs ${color.border} ${color.text}`}>
            {course === 'azul' ? 'Jovem Programador' : 'Técnico em Desenvolvimento de Sistemas'}
          </span>}
        </span>
      ) : <span className="block truncate text-xs text-muted-foreground">{lesson.teacher} · {lesson.room}</span>)}
    </>
  )
  const className = cn('flex min-w-0 w-full flex-col gap-1 rounded-lg border border-border border-l-2 p-2 text-left', detailed && 'gap-1.5 p-2.5', color.border, color.bg,
    compact && 'h-8 shrink-0 justify-center py-1',
    onOpen && 'transition-colors duration-150 hover:border-ring hover:bg-hover focus-visible:ring-2 focus-visible:ring-ring')
  return onOpen ? (
    <button type="button" className={className} onClick={onOpen}
      aria-label={`Ver aula: ${lesson.subject}, ${lesson.time}, professor ${lesson.teacher}, sala ${lesson.room}`}>
      {content}
    </button>
  ) : <article className={className} aria-label={lesson.subject}>{content}</article>
}
