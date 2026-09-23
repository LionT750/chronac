import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription, SheetFooter } from '@/components/ui/sheet'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { teachers, rooms, weekdays } from '../mocks/academicStructure'
import { hasValidWorkload } from '../utils/configuration'
import ConfigurationSelect from './ConfigurationSelect'

export default function SubjectConfigurationSheet({ subject, onClose, onSave }) {
  const [teacherId, setTeacherId] = useState(subject.teacherId ?? '')
  const [workload, setWorkload] = useState(subject.workload ?? '')
  const [roomId, setRoomId] = useState(subject.roomId ?? '')
  const [availableDays, setAvailableDays] = useState(subject.availableDays ?? [])
  const [error, setError] = useState('')

  function submit(event) {
    event.preventDefault()
    const hours = workload === '' ? undefined : Number(workload)
    if (hours !== undefined && !hasValidWorkload(hours)) {
      setError('Informe uma carga horária maior que zero ou deixe o campo em branco.')
      return
    }
    onSave({ teacherId: teacherId || undefined, workload: hours, roomId: roomId || undefined, availableDays })
  }

  return <Sheet open onOpenChange={(open) => { if (!open) onClose() }}>
    <SheetContent className="gap-0 data-[side=right]:w-full data-[side=right]:sm:max-w-md">
      <SheetHeader className="border-b p-6 pr-12">
        <SheetTitle>Configurar disciplina</SheetTitle>
        <SheetDescription className="mt-2">{subject.name}</SheetDescription>
      </SheetHeader>
      <form onSubmit={submit} className="flex min-h-0 flex-1 flex-col">
        <div className="flex-1 space-y-6 overflow-y-auto p-6">
          <p className="rounded-lg bg-muted p-3 text-sm text-muted-foreground">Defina professor, carga horária e sala para concluir a configuração. Você também pode salvar com pendências.</p>
          <ConfigurationSelect id="subject-teacher" label="Professor" value={teacherId} onChange={setTeacherId} options={teachers} emptyLabel="Sem professor" />
          <div className="space-y-2">
            <label htmlFor="subject-workload" className="text-sm font-medium">Carga horária (horas)</label>
            <Input id="subject-workload" className="h-10" type="number" min="0" step="any" value={workload} placeholder="Ex.: 80" aria-invalid={Boolean(error)} aria-describedby={error ? 'workload-error' : undefined} onChange={(event) => { setWorkload(event.target.value); setError('') }} />
            {error && <p id="workload-error" role="alert" className="text-sm text-destructive">{error}</p>}
          </div>
          <ConfigurationSelect id="subject-room" label="Sala preferencial" value={roomId} onChange={setRoomId} options={rooms} emptyLabel="Sem sala" />
          <div className="space-y-2">
            <label id="days-label" htmlFor="subject-days" className="text-sm font-medium">Dias disponíveis <span className="font-normal text-muted-foreground">(opcional)</span></label>
            <Select multiple value={availableDays} onValueChange={setAvailableDays}>
              <SelectTrigger id="subject-days" aria-labelledby="days-label" className="h-auto min-h-10 w-full whitespace-normal"><SelectValue placeholder="Selecionar dias">{availableDays.length ? availableDays.join(', ') : 'Selecionar dias'}</SelectValue></SelectTrigger>
              <SelectContent alignItemWithTrigger={false}>{weekdays.map((day) => <SelectItem key={day} value={day}>{day}</SelectItem>)}</SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">Sem seleção, todos os dias são considerados disponíveis.</p>
          </div>
        </div>
        <SheetFooter className="border-t p-6 sm:flex-row sm:justify-end">
          <Button type="button" variant="outline" onClick={onClose}>Cancelar</Button>
          <Button type="submit">Salvar alterações</Button>
        </SheetFooter>
      </form>
    </SheetContent>
  </Sheet>
}
