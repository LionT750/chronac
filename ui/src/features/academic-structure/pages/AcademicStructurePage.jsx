import { useState } from 'react'
import { Search } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { filterClasses } from '../utils/configuration'
import AcademicStructureHeader from '../components/AcademicStructureHeader'
import AcademicEmptyState from '../components/AcademicEmptyState'
import ClassCard from '../components/ClassCard'
import NewClassSheet from '../components/NewClassSheet'

export default function AcademicStructurePage({ classes, onOpen, createClass }) {
  const [query, setQuery] = useState('')
  const [creating, setCreating] = useState(false)
  const filtered = filterClasses(classes, query)

  return <main className="mx-auto max-w-[1600px] space-y-7">
    <AcademicStructureHeader onCreate={() => setCreating(true)} />
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="relative w-full max-w-md">
        <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input type="search" aria-label="Buscar turma" className="h-10 bg-card pl-9" placeholder="Buscar turma..." value={query} onChange={(event) => setQuery(event.target.value)} />
      </div>
      <p role="status" className="text-sm text-muted-foreground">{filtered.length} {filtered.length === 1 ? 'turma' : 'turmas'}</p>
    </div>
    {!classes.length ? <AcademicEmptyState onAction={() => setCreating(true)} /> : !filtered.length ? <AcademicEmptyState kind="search" onAction={() => setQuery('')} /> :
      <div className="grid items-stretch gap-5 md:grid-cols-2 2xl:grid-cols-3">{filtered.map((academicClass) => <ClassCard key={academicClass.id} academicClass={academicClass} onOpen={onOpen} />)}</div>}
    {creating && <NewClassSheet onClose={() => setCreating(false)} onCreate={(values) => { const id = createClass(values); setCreating(false); onOpen(id) }} />}
  </main>
}
