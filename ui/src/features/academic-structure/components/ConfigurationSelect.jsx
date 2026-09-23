import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

export default function ConfigurationSelect({ id, label, value, onChange, options, emptyLabel }) {
  const items = [...(emptyLabel ? [{ value: '', label: emptyLabel }] : []), ...options.map((option) => ({ value: option.id, label: option.name }))]
  return <div className="space-y-2">
    <label id={`${id}-label`} htmlFor={id} className="text-sm font-medium">{label}</label>
    <Select items={items} value={value} onValueChange={onChange}>
      <SelectTrigger id={id} aria-labelledby={`${id}-label`} className="h-10 w-full"><SelectValue placeholder="Selecione uma opção" /></SelectTrigger>
      <SelectContent alignItemWithTrigger={false}>{items.map((item) => <SelectItem key={item.value} value={item.value}>{item.label}</SelectItem>)}</SelectContent>
    </Select>
  </div>
}
