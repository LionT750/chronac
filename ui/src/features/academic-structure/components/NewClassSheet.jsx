import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription, SheetFooter } from '@/components/ui/sheet'
import { courses } from '../mocks/academicStructure'
import ConfigurationSelect from './ConfigurationSelect'

export default function NewClassSheet({ onClose, onCreate }) {
  const [name, setName] = useState('')
  const [courseId, setCourseId] = useState(courses[0].id)
  const [shift, setShift] = useState('Noturno')
  const [semester, setSemester] = useState('2026/2')
  const [error, setError] = useState('')

  function submit(event) {
    event.preventDefault()
    if (!name.trim()) { setError('Informe o nome da turma.'); return }
    onCreate({ name: name.trim(), course: courses.find((course) => course.id === courseId), shift, semester: semester.trim() })
  }

  return <Sheet open onOpenChange={(open) => { if (!open) onClose() }}>
    <SheetContent className="gap-0 data-[side=right]:w-full data-[side=right]:sm:max-w-md">
      <SheetHeader className="border-b p-6 pr-12"><SheetTitle>Nova turma</SheetTitle><SheetDescription>Defina os dados básicos da turma.</SheetDescription></SheetHeader>
      <form onSubmit={submit} className="flex min-h-0 flex-1 flex-col">
        <div className="flex-1 space-y-5 overflow-y-auto p-6">
          <div className="space-y-2"><label htmlFor="class-name" className="text-sm font-medium">Nome da turma</label><Input id="class-name" required maxLength={100} value={name} onChange={(event) => { setName(event.target.value); setError('') }} placeholder="Ex.: TDS 2026/2" className="h-10" aria-invalid={Boolean(error)} aria-describedby={error ? 'class-error' : undefined} /></div>
          {error && <p id="class-error" role="alert" className="text-sm text-destructive">{error}</p>}
          <ConfigurationSelect id="class-course" label="Curso" value={courseId} onChange={setCourseId} options={courses} />
          <ConfigurationSelect id="class-shift" label="Turno" value={shift} onChange={setShift} options={['Matutino', 'Vespertino', 'Noturno', 'Integral'].map((value) => ({ id: value, name: value }))} />
          <div className="space-y-2"><label htmlFor="class-semester" className="text-sm font-medium">Período</label><Input id="class-semester" required pattern="[0-9]{4}/[12]" title="Use o formato AAAA/1 ou AAAA/2" value={semester} onChange={(event) => setSemester(event.target.value)} placeholder="2026/2" className="h-10" /></div>
          <p className="rounded-lg bg-muted p-3 text-sm text-muted-foreground">A turma será criada sem disciplinas. A vinculação de disciplinas estará disponível em uma próxima etapa.</p>
        </div>
        <SheetFooter className="border-t p-6 sm:flex-row sm:justify-end"><Button type="button" variant="outline" onClick={onClose}>Cancelar</Button><Button type="submit">Criar turma</Button></SheetFooter>
      </form>
    </SheetContent>
  </Sheet>
}
