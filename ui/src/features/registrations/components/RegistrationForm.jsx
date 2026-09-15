import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

export default function RegistrationForm({ fields, record, onSave, onCancel }) {
  const [values, setValues] = useState(() => Object.fromEntries(fields.map((field) => [field.key, record?.[field.key] ?? ''])))
  const [error, setError] = useState('')

  function submit(event) {
    event.preventDefault()
    const trimmed = Object.fromEntries(Object.entries(values).map(([key, value]) => [key, value.trim()]))
    if (fields.some((field) => field.required && !trimmed[field.key])) {
      setError('Preencha os campos obrigatórios com um valor válido.')
      return
    }
    onSave(trimmed, record?.id)
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-5 p-4">
      {fields.map((field) => (
        <div key={field.key} className="flex flex-col gap-2">
          <label htmlFor={`registration-${field.key}`} className="text-sm font-medium">
            {field.label}{field.required ? ' *' : ' (opcional)'}
          </label>
          <Input id={`registration-${field.key}`} value={values[field.key]} required={field.required}
            maxLength={120} aria-invalid={!!error && field.required && !values[field.key].trim()}
            aria-describedby={error ? 'registration-error' : undefined}
            onChange={(event) => { setValues({ ...values, [field.key]: event.target.value }); setError('') }} />
        </div>
      ))}
      {error && <p id="registration-error" role="alert" className="text-sm text-destructive">{error}</p>}
      <div className="flex flex-wrap justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>Cancelar</Button>
        <Button type="submit">Salvar</Button>
      </div>
    </form>
  )
}
