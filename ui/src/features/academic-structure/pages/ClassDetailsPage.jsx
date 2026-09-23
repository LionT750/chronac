import { useState } from 'react'
import { ArrowLeft, BookOpen, CheckCircle2, Clock3, DoorOpen, SlidersHorizontal, Users } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { getClassSummary } from '../utils/configuration'
import ClassProgress from '../components/ClassProgress'
import ConfigurationStatus from '../components/ConfigurationStatus'
import PendingConfiguration from '../components/PendingConfiguration'
import SubjectCard from '../components/SubjectCard'
import SubjectConfigurationSheet from '../components/SubjectConfigurationSheet'
import AcademicEmptyState from '../components/AcademicEmptyState'

function UpcomingTab({ title, children, icon: Icon }) {
  return <Card><CardContent className="flex flex-col items-center gap-3 py-12 text-center"><Icon className="size-7 text-muted-foreground" /><h2 className="m-0 text-lg font-semibold">{title}</h2><Badge variant="secondary">Em breve</Badge><p className="max-w-lg text-sm text-muted-foreground">{children}</p></CardContent></Card>
}

export default function ClassDetailsPage({ academicClass, onBack, saveSubject }) {
  const [editing, setEditing] = useState(null)
  const [notice, setNotice] = useState('')
  const [tab, setTab] = useState('overview')

  if (!academicClass) return <main className="mx-auto max-w-3xl space-y-5 py-12 text-center"><h1 className="m-0 text-2xl font-semibold">Turma não encontrada</h1><p className="text-muted-foreground">Confira o endereço ou selecione uma das turmas disponíveis.</p><Button variant="outline" onClick={onBack}><ArrowLeft />Voltar para turmas</Button></main>

  const summary = getClassSummary(academicClass)
  const stats = [
    { label: 'Disciplinas', value: summary.total, icon: BookOpen, color: 'text-course-blue' },
    { label: 'Configuradas', value: summary.configured, icon: CheckCircle2, color: 'text-success' },
    { label: 'Pendentes', value: summary.pending.length, icon: Clock3, color: 'text-warning' },
    { label: 'Professores', value: summary.teacherCount, icon: Users, color: 'text-course-blue' },
    { label: 'Salas utilizadas', value: summary.roomCount, icon: DoorOpen, color: 'text-course-blue' },
  ]

  function edit(subject) { setNotice(''); setEditing(subject) }

  return <main className="mx-auto max-w-[1600px] space-y-6">
    <Button variant="ghost" className="-ml-2 text-muted-foreground" onClick={onBack}><ArrowLeft className="size-4" />Estrutura acadêmica</Button>
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-3"><h1 className="m-0 text-2xl font-semibold tracking-tight">{academicClass.name}</h1><Badge variant="secondary" className={academicClass.active ? 'bg-course-blue-bg text-course-blue' : ''}>{academicClass.active ? 'Ativa' : 'Inativa'}</Badge></div>
        <p className="text-muted-foreground">{academicClass.course.name}</p>
        <p className="text-sm text-muted-foreground">{academicClass.shift} • {academicClass.semester} • {summary.total} disciplinas</p>
      </div>
      <ConfigurationStatus ready={summary.ready} />
    </div>
    <div role="status" aria-live="polite">{notice && <p className="flex items-center gap-2 rounded-lg bg-success-bg p-3 text-sm text-success"><CheckCircle2 className="size-4 shrink-0" />{notice}</p>}</div>
    <Tabs value={tab} onValueChange={setTab} className="gap-6">
      <div className="overflow-x-auto border-b pb-1"><TabsList variant="line" aria-label="Configuração da turma" className="gap-4">
        <TabsTrigger value="overview">Visão geral</TabsTrigger>
        <TabsTrigger value="subjects">Disciplinas <span className="text-xs text-muted-foreground">{summary.total}</span></TabsTrigger>
        <TabsTrigger value="teachers">Professores</TabsTrigger>
        <TabsTrigger value="restrictions">Restrições</TabsTrigger>
      </TabsList></div>
      <TabsContent value="overview" className="space-y-6">
        <Card><CardHeader><CardTitle>Configuração da turma</CardTitle></CardHeader><CardContent className="space-y-4">
          <ClassProgress percentage={summary.percentage} ready={summary.ready} label={`${summary.configured} de ${summary.total} disciplinas configuradas`} />
          <p className={`text-sm ${summary.ready ? 'text-success' : 'text-muted-foreground'}`}>{summary.ready ? 'Turma pronta para geração.' : 'Cada disciplina precisa de professor, carga horária e sala para ficar pronta.'}</p>
        </CardContent></Card>
        <dl className="grid grid-cols-2 gap-3 lg:grid-cols-5">{stats.map(({ label, value, icon: Icon, color }) => <Card key={label}><CardContent className="space-y-2"><dt className="flex items-center gap-2 text-xs text-muted-foreground"><Icon className={`size-4 shrink-0 ${color}`} />{label}</dt><dd className={`text-2xl font-semibold tabular-nums ${color}`}>{value}</dd></CardContent></Card>)}</dl>
        {!summary.total ? <AcademicEmptyState kind="subjects" /> : <PendingConfiguration pending={summary.pending} onEdit={edit} />}
        {summary.total > 0 && <Button variant="outline" onClick={() => setTab('subjects')}><BookOpen className="size-4" />Ver disciplinas</Button>}
      </TabsContent>
      <TabsContent value="subjects" className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2"><h2 className="m-0 text-lg font-semibold">Disciplinas da turma</h2><p className="text-sm text-muted-foreground">{summary.configured} configuradas • {summary.pending.length} pendentes</p></div>
        {summary.total ? <div className="grid gap-4">{academicClass.subjects.map((subject) => <SubjectCard key={subject.id} subject={subject} onEdit={edit} />)}</div> : <AcademicEmptyState kind="subjects" />}
      </TabsContent>
      <TabsContent value="teachers"><UpcomingTab title="Professores da turma" icon={Users}>A visão consolidada de professores estará disponível em breve. Para vincular um professor agora, edite uma disciplina.</UpcomingTab></TabsContent>
      <TabsContent value="restrictions"><UpcomingTab title="Restrições da turma" icon={SlidersHorizontal}>A configuração de restrições para a geração do cronograma estará disponível em uma próxima etapa.</UpcomingTab></TabsContent>
    </Tabs>
    {editing && <SubjectConfigurationSheet key={editing.id} subject={editing} onClose={() => setEditing(null)} onSave={(values) => {
      saveSubject(academicClass.id, editing.id, values)
      setNotice(`Configuração de ${editing.name} atualizada.`)
      setEditing(null)
    }} />}
  </main>
}
