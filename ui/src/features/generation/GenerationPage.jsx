import { CalendarPlus, RefreshCw } from 'lucide-react'
import { useCalendarState } from '@/features/calendar/hooks/useCalendarState'
import { isFeasible } from '@/features/calendar/utils/lessonSelectors'
import CalendarView from '@/features/calendar/components/CalendarView'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { DEMO_MULTI_TURMA } from './hooks/useGeneration'

function LiveIndicator() {
  return (
    <span className="relative flex size-2" aria-hidden="true">
      <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-course-blue opacity-75" />
      <span className="relative inline-flex size-2 rounded-full bg-course-blue" />
    </span>
  )
}

function ScoreBadge({ score }) {
  if (!score) return null
  const feasible = isFeasible(score)
  return (
    <div
      className="flex items-center gap-2 rounded-lg border border-border bg-card px-3 py-1.5"
      title={feasible ? 'Solução viável (sem conflitos rígidos)' : 'Ainda com conflitos rígidos'}>
      <span className={`size-2 rounded-full ${feasible ? 'bg-course-green' : 'bg-destructive'}`} aria-hidden="true" />
      <span className="font-mono text-sm text-foreground">{score}</span>
    </div>
  )
}

export default function GenerationPage({ generation }) {
  const { status, snapshot, start } = generation
  const calendar = useCalendarState(snapshot.data)
  const data = snapshot.data

  return (
    <div className="flex flex-col gap-4">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold text-foreground">Gerar grade</h1>
          <p className="text-sm text-muted-foreground">
            Acompanhe a montagem do cronograma em tempo real, conforme o solver encontra melhores soluções.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {status === 'running' && (
            <span className="flex items-center gap-2 rounded-lg border border-border bg-card px-3 py-1.5 text-sm text-muted-foreground">
              <LiveIndicator /> Gerando ao vivo…
            </span>
          )}
          <ScoreBadge score={data?.score} />
        </div>
      </header>

      <div className="flex flex-wrap items-center gap-2">
        {status === 'idle' && (
          <Button onClick={() => start.mutate(DEMO_MULTI_TURMA)}>
            <CalendarPlus /> Gerar grade
          </Button>
        )}
        {status === 'starting' && (
          <Button disabled>
            <RefreshCw className="animate-spin" /> Gerando…
          </Button>
        )}
        {status === 'finished' && (
          <Button onClick={() => start.mutate(DEMO_MULTI_TURMA)}>
            <RefreshCw /> Nova geração
          </Button>
        )}
        {status === 'error' && (
          <>
            <Button variant="outline" onClick={() => snapshot.refetch()}>
              <RefreshCw /> Tentar novamente
            </Button>
            <Button onClick={() => start.mutate(DEMO_MULTI_TURMA)}>
              <CalendarPlus /> Nova geração
            </Button>
          </>
        )}
      </div>

      {status === 'error' && (
        <p className="text-sm text-destructive" role="alert">
          {snapshot.error?.message ?? 'Não foi possível acompanhar a geração.'}
        </p>
      )}

      {data ? (
        <CalendarView {...calendar.calendar} />
      ) : (
        <Skeleton className="h-[420px] w-full rounded-2xl" />
      )}
    </div>
  )
}
