import { CircleAlert, ArrowRight } from 'lucide-react'
import { Card, CardHeader, CardContent, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { getSubjectPendingFields } from '../utils/configuration'

export default function PendingConfiguration({ pending, onEdit }) {
  if (!pending.length) return null
  return <Card className="ring-warning/25">
    <CardHeader>
      <CardTitle className="flex items-center gap-2 text-warning"><CircleAlert className="size-5" />{pending.length} {pending.length === 1 ? 'pendência precisa ser resolvida' : 'pendências precisam ser resolvidas'}</CardTitle>
      <p className="text-sm text-muted-foreground">Complete os campos abaixo para preparar esta turma.</p>
    </CardHeader>
    <CardContent>
      <ul className="divide-y divide-border">
        {pending.map((subject) => <li key={subject.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
          <p className="text-sm"><span className="font-medium">{subject.name}</span><span className="text-muted-foreground"> não possui {new Intl.ListFormat('pt-BR', { type: 'conjunction' }).format(getSubjectPendingFields(subject))}.</span></p>
          <Button variant="ghost" size="sm" className="text-course-blue" onClick={() => onEdit(subject)} aria-label={`Resolver pendência de ${subject.name}`}>Configurar<ArrowRight className="size-4" /></Button>
        </li>)}
      </ul>
    </CardContent>
  </Card>
}
