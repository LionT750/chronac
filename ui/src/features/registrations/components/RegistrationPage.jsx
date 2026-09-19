import { useState } from 'react'
import { Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '@/components/ui/sheet'
import RegistrationForm from './RegistrationForm'
import RegistrationList from './RegistrationList'

export default function RegistrationPage({ title, singular, fields, records, save, remove }) {
  const [editor, setEditor] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [notice, setNotice] = useState('')

  function saveRecord(values, id) {
    save(values, id)
    setEditor(null)
    setNotice('Registro salvo.')
  }

  return (
    <section className="registration-page flex min-w-0 flex-col gap-5 p-2 md:p-8" aria-label={title}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold">{title}</h2>
          <p className="text-sm text-muted-foreground">Cadastre e organize {title.toLowerCase()}.</p>
        </div>
        <Button onClick={() => setEditor({})}><Plus />Adicionar {singular}</Button>
      </div>
      <p className="text-xs text-muted-foreground">Dados de demonstração · {records.length} {records.length === 1 ? 'registro' : 'registros'} nesta sessão.</p>
      <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-panel">
        <RegistrationList title={title} records={records} fields={fields} onEdit={setEditor} onDelete={setDeleting} />
      </div>
      <p role="status" className="text-sm text-course-green">{notice}</p>

      <Sheet open={editor !== null} onOpenChange={(open) => { if (!open) setEditor(null) }}>
        <SheetContent className="w-full! overflow-y-auto sm:max-w-md">
          <SheetHeader>
            <SheetTitle>{editor?.id ? 'Editar' : 'Adicionar'} {singular}</SheetTitle>
            <SheetDescription>Preencha os dados abaixo. Campos com * são obrigatórios.</SheetDescription>
          </SheetHeader>
          {editor !== null && <RegistrationForm key={editor.id ?? 'new'} fields={fields} record={editor} onSave={saveRecord} onCancel={() => setEditor(null)} />}
        </SheetContent>
      </Sheet>

      <Sheet open={deleting !== null} onOpenChange={(open) => { if (!open) setDeleting(null) }}>
        <SheetContent className="w-full! overflow-y-auto sm:max-w-md">
          <SheetHeader>
            <SheetTitle>Excluir {singular}</SheetTitle>
            <SheetDescription>Deseja excluir “{deleting?.name}”? Esta ação remove o registro desta sessão.</SheetDescription>
          </SheetHeader>
          <div className="flex justify-end gap-2 p-4">
            <Button variant="outline" onClick={() => setDeleting(null)}>Cancelar</Button>
            <Button variant="destructive" onClick={() => { remove(deleting.id); setDeleting(null); setNotice('Registro excluído.') }}>Excluir</Button>
          </div>
        </SheetContent>
      </Sheet>
    </section>
  )
}
