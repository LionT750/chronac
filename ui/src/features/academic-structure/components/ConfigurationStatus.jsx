import { CheckCircle2, CircleDashed } from 'lucide-react'
import { Badge } from '@/components/ui/badge'

export default function ConfigurationStatus({ ready, inactive = false }) {
  if (inactive) return <Badge variant="secondary">Inativa</Badge>
  const Icon = ready ? CheckCircle2 : CircleDashed
  return <Badge className={ready ? 'border-transparent bg-success-bg text-success' : 'border-transparent bg-warning-bg text-warning'}>
    <Icon className="size-3.5" />{ready ? 'Pronta' : 'Pendente'}
  </Badge>
}
