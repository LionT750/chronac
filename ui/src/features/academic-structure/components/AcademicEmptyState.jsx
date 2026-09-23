import { BookOpen, GraduationCap, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'

const content = {
  classes: { icon: GraduationCap, title: 'Nenhuma turma configurada', description: 'Crie uma turma para começar a estruturar seu calendário acadêmico.', action: 'Criar primeira turma' },
  search: { icon: Search, title: 'Nenhuma turma encontrada', description: 'Tente buscar por outro nome, curso ou período.', action: 'Limpar busca' },
  subjects: { icon: BookOpen, title: 'Nenhuma disciplina vinculada', description: 'Esta turma ainda não possui disciplinas. A vinculação de disciplinas estará disponível em uma próxima etapa.' },
}

export default function AcademicEmptyState({ kind = 'classes', onAction }) {
  const { icon: Icon, title, description, action } = content[kind]
  return <Card className="border border-dashed ring-0">
    <CardContent className="flex flex-col items-center gap-3 py-12 text-center">
      <span className="rounded-xl bg-muted p-4 text-muted-foreground"><Icon className="size-7" /></span>
      <h2 className="m-0 text-lg font-semibold">{title}</h2>
      <p className="max-w-md text-sm text-muted-foreground">{description}</p>
      {action && <Button className="mt-2" onClick={onAction}>{action}</Button>}
    </CardContent>
  </Card>
}
