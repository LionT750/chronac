import { format } from 'date-fns'

// Dados fictícios exclusivos da apresentação pública. Não consultam a API.
export const demoSubjects = [
  { subject: 'UC1 · Lógica de programação', teacher: 'Ana Martins', room: 'Laboratório 01', time: '08:00 – 10:00' },
  { subject: 'UC7 · Banco de dados', teacher: 'Rafael Costa', room: 'Laboratório 02', time: '10:15 – 12:15' },
  { subject: 'UC3 · Desenvolvimento web', teacher: 'Marina Santos', room: 'Laboratório 03', time: '13:30 – 15:30' },
  { subject: 'UC8 · Engenharia de software', teacher: 'Pedro Lima', room: 'Sala 204', time: '15:45 – 17:45' },
]

export function createDemoLessons(days) {
  return Object.fromEntries(days.map((day) => {
    const date = format(day, 'yyyy-MM-dd')
    const subjects = day.getDay() === 6 ? demoSubjects.slice(0, 1) : demoSubjects.filter((_, index) => (index + day.getDay()) % 3 !== 0)
    return [date, subjects.map((lesson, index) => ({ ...lesson, id: `demo-${date}-${index}`, date }))]
  }))
}
