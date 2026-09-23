import { Progress } from '@/components/ui/progress'

export default function ClassProgress({ percentage, label = 'Configuração', ready = false }) {
  return <div className="space-y-2">
    <div className="flex items-center justify-between gap-4 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-semibold tabular-nums">{percentage}%</span>
    </div>
    <Progress value={percentage} aria-label={label} className={`w-full ${ready ? '[&_[data-slot=progress-indicator]]:bg-success' : ''}`} />
  </div>
}
