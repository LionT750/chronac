import { ArrowRight, BookOpen, GraduationCap } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { getClassSummary } from '../utils/configuration'
import ConfigurationStatus from './ConfigurationStatus'
import ClassProgress from './ClassProgress'

export default function ClassCard({ academicClass, onOpen }) {
  const summary = getClassSummary(academicClass)
  return <Card className={`h-full gap-5 ${!academicClass.active ? 'bg-muted/50' : ''}`}>
    <CardHeader className="gap-3">
      <div className="flex items-center justify-between gap-2">
        <span className={`flex size-10 items-center justify-center rounded-lg ${academicClass.active ? 'bg-course-blue-bg text-course-blue' : 'bg-secondary text-muted-foreground'}`}><GraduationCap className="size-5" /></span>
        <ConfigurationStatus ready={summary.ready} inactive={!academicClass.active} />
      </div>
      <div>
        <CardTitle className="text-lg">{academicClass.name}</CardTitle>
        <CardDescription className="mt-1 min-h-10">{academicClass.course.name}</CardDescription>
      </div>
      <p className="text-xs text-muted-foreground">{academicClass.shift} • {academicClass.semester}</p>
    </CardHeader>
    <CardContent className="flex flex-1 flex-col gap-4">
      <p className="flex items-center gap-2 text-sm text-muted-foreground"><BookOpen className="size-4" />{summary.total} disciplinas</p>
      <ClassProgress percentage={summary.percentage} ready={summary.ready} />
      <p className={`text-sm ${summary.ready ? 'text-success' : 'text-warning'}`}>
        {summary.ready ? 'Turma pronta para geração.' : summary.total === 0 ? 'Nenhuma disciplina vinculada.' : `${summary.pending.length} ${summary.pending.length === 1 ? 'configuração precisa ser concluída.' : 'configurações precisam ser concluídas.'}`}
      </p>
    </CardContent>
    <CardFooter>
      <Button variant="outline" className="w-full justify-between" aria-label={`Configurar ${academicClass.name}`} onClick={() => onOpen(academicClass.id)}>Configurar<ArrowRight className="size-4" /></Button>
    </CardFooter>
  </Card>
}
