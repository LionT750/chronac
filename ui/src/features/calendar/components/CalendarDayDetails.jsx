import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { Button } from '@/components/ui/button'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '@/components/ui/sheet'
import LessonCard from './LessonCard'

export default function CalendarDayDetails({ selection, lessonsByDate, hasFilters, onClose, onShowDay, returnFocus }) {
  const lessons = selection ? lessonsByDate[format(selection.day, 'yyyy-MM-dd')] ?? [] : []
  const visible = selection?.lesson ? [selection.lesson] : lessons
  return (
    <Sheet open={!!selection} onOpenChange={(open) => { if (!open) onClose() }}>
      <SheetContent className="w-full! overflow-y-auto sm:max-w-lg" finalFocus={returnFocus}>
        <SheetHeader className="border-b border-border pr-12">
          <SheetTitle>{selection?.lesson ? 'Detalhes da aula' : 'Aulas do dia'}</SheetTitle>
          <SheetDescription className="capitalize">{selection && format(selection.day, "EEEE, d 'de' MMMM 'de' yyyy", { locale: ptBR })}</SheetDescription>
        </SheetHeader>
        <div className="flex flex-col gap-3 px-4 pb-6">
          <p className="text-xs text-muted-foreground">{hasFilters ? 'Exibindo aulas que correspondem aos filtros ativos.' : `${lessons.length} ${lessons.length === 1 ? 'aula neste dia' : 'aulas neste dia'}.`}</p>
          {visible.map((lesson, index) => <LessonCard key={lesson.id ?? index} lesson={lesson} detailed />)}
          {!visible.length && <p className="rounded-xl bg-muted p-5 text-sm text-muted-foreground">{hasFilters ? 'Nenhuma aula corresponde aos filtros neste dia.' : 'Não há aulas alocadas neste dia.'}</p>}
          {selection?.lesson && <Button variant="outline" className="h-10" onClick={onShowDay}>Ver todas as aulas deste dia</Button>}
        </div>
      </SheetContent>
    </Sheet>
  )
}
