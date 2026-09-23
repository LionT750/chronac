import { Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'

export default function AcademicStructureHeader({ onCreate }) {
  return <div className="flex flex-wrap items-start justify-between gap-4">
    <div className="space-y-2">
      <h1 className="m-0 text-2xl font-semibold tracking-tight">Estrutura acadêmica</h1>
      <p className="max-w-2xl text-sm text-muted-foreground">Configure as turmas e os recursos necessários para geração do cronograma.</p>
    </div>
    <Button onClick={onCreate}><Plus className="size-4" />Nova turma</Button>
  </div>
}
