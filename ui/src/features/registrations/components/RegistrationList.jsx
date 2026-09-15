import { Pencil, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'

export default function RegistrationList({ title, records, fields, onEdit, onDelete }) {
  if (!records.length) return <p className="p-6 text-sm text-muted-foreground">Nenhum registro cadastrado. Use o botão acima para adicionar.</p>

  return (
    <div className="overflow-x-auto">
      <table aria-label={title}>
        <thead>
          <tr>{fields.map((field) => <th key={field.key} scope="col">{field.label}</th>)}<th scope="col">Ações</th></tr>
        </thead>
        <tbody>
          {records.map((record) => (
            <tr key={record.id}>
              {fields.map((field) => <td key={field.key} className="max-w-80 whitespace-normal! break-words">{record[field.key] || '—'}</td>)}
              <td>
                <div className="flex items-center gap-2">
                  <Button variant="ghost" size="icon" aria-label={`Editar ${record.name}`} title="Editar" onClick={() => onEdit(record)}><Pencil /></Button>
                  <Button variant="destructive" size="icon" aria-label={`Excluir ${record.name}`} title="Excluir" onClick={() => onDelete(record)}><Trash2 /></Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
