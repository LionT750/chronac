import { Pencil } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { teachers, rooms } from '../mocks/academicStructure'
import { hasValidWorkload, isSubjectConfigured } from '../utils/configuration'
import ConfigurationStatus from './ConfigurationStatus'

export default function SubjectCard({ subject, onEdit }) {
  const teacher = teachers.find((item) => item.id === subject.teacherId)
  const room = rooms.find((item) => item.id === subject.roomId)
  const metadata = [
    { label: 'Professor', value: teacher?.name, empty: 'Sem professor' },
    { label: 'Carga horária', value: hasValidWorkload(subject.workload) ? `${subject.workload}h` : null, empty: 'Não definida' },
    { label: 'Sala', value: room?.name, empty: 'Sem sala' },
  ]
  return <Card>
    <CardContent className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="font-semibold">{subject.name}</h3>
        <ConfigurationStatus ready={isSubjectConfigured(subject)} />
      </div>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <dl className="grid min-w-0 flex-1 gap-4 sm:grid-cols-3">
          {metadata.map(({ label, value, empty }) => <div key={label}>
            <dt className="mb-1 text-xs text-muted-foreground">{label}</dt>
            <dd className={`text-sm ${value ? '' : 'font-medium text-warning'}`}>{value || empty}</dd>
          </div>)}
        </dl>
        <Button variant="outline" size="sm" onClick={() => onEdit(subject)} aria-label={`Editar ${subject.name}`}><Pencil className="size-3.5" />Editar</Button>
      </div>
    </CardContent>
  </Card>
}
