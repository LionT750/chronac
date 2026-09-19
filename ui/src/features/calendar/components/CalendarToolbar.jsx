import { format, startOfWeek } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'

export default function CalendarToolbar({ selectedDate, viewType, onViewChange, onPrevious, onNext, onToday }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border bg-surface-raised p-4">
      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          aria-label="Período anterior" onClick={onPrevious}
          className="h-8 w-8 border-input bg-secondary text-foreground hover:bg-hover"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Button
          variant="outline"
          size="icon"
          aria-label="Próximo período" onClick={onNext}
          className="h-8 w-8 border-input bg-secondary text-foreground hover:bg-hover"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
        <Button
          variant="outline"
          onClick={onToday}
          className="h-8 border-input bg-secondary px-3 text-xs font-semibold text-foreground hover:bg-hover"
        >
          Hoje
        </Button>
        <h2 className="ml-2 text-xl font-bold capitalize text-foreground">
          {viewType === 'month' && format(selectedDate, "MMMM 'de' yyyy", { locale: ptBR })}
          {viewType === 'week' && `Semana de ${format(startOfWeek(selectedDate, { weekStartsOn: 1 }), 'd MMMM', { locale: ptBR })}`}
          {viewType === 'day' && format(selectedDate, "dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
        </h2>
      </div>

      <div className="flex gap-1 rounded-lg bg-muted p-0.5 text-xs font-medium text-muted-foreground">
        <button
          aria-pressed={viewType === 'month'} onClick={() => onViewChange('month')}
          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'month' ? 'bg-selected text-foreground' : 'text-muted-foreground hover:text-foreground'}`}
        >
          Mês
        </button>
        <button
          aria-pressed={viewType === 'week'} onClick={() => onViewChange('week')}
          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'week' ? 'bg-selected text-foreground' : 'text-muted-foreground hover:text-foreground'}`}
        >
          Semana
        </button>
        <button
          aria-pressed={viewType === 'day'} onClick={() => onViewChange('day')}
          className={`rounded-md px-3 py-1.5 font-semibold shadow-sm transition-colors ${viewType === 'day' ? 'bg-selected text-foreground' : 'text-muted-foreground hover:text-foreground'}`}
        >
          Dia
        </button>
      </div>
    </div>
  )
}
