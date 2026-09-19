import { format } from 'date-fns'

const weekdays = [
  ['MONDAY', 'SEGUNDA', 'SEGUNDA-FEIRA', 'SEG', 'Segunda-feira'],
  ['TUESDAY', 'TERÇA', 'TERÇA-FEIRA', 'TER', 'Terça-feira'],
  ['WEDNESDAY', 'QUARTA', 'QUARTA-FEIRA', 'QUA', 'Quarta-feira'],
  ['THURSDAY', 'QUINTA', 'QUINTA-FEIRA', 'QUI', 'Quinta-feira'],
  ['FRIDAY', 'SEXTA', 'SEXTA-FEIRA', 'SEX', 'Sexta-feira'],
  ['SATURDAY', 'SÁBADO', 'SABADO', 'SÁB', 'Sábado'],
]

export function weekdayOption(value) {
  const index = weekdays.findIndex((aliases) => aliases.includes(String(value).toUpperCase()))
  return { value, label: index < 0 ? value : weekdays[index].at(-1), order: index < 0 ? 6 : index }
}

export function countVisibleLessons(days, lessonsByDate) {
  // Inclui os dias adjacentes exibidos na grade, para o resumo coincidir com as células visíveis.
  return days.reduce((count, day) => count + (lessonsByDate[format(day, 'yyyy-MM-dd')]?.length ?? 0), 0)
}

export function lessonPreviewLimit(viewType) {
  return viewType === 'day' ? Infinity : viewType === 'week' ? 4 : 2
}
