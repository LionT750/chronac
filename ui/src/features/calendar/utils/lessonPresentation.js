// A classificação por UC também orienta filtros e ordenação; mantenha os mesmos grupos ao ajustar a apresentação.
const BLUE = { border: 'border-course-blue', bg: 'bg-course-blue-bg', text: 'text-course-blue' }
const GREEN = { border: 'border-course-green', bg: 'bg-course-green-bg', text: 'text-course-green' }
const NEUTRAL = { border: 'border-muted-foreground', bg: 'bg-muted', text: 'text-foreground' }

export function getSubjectColor(name) {
  const course = getSubjectCourse(name)
  if (course === 'azul') return BLUE
  if (course === 'verde') return GREEN
  return NEUTRAL
}

export function getSubjectCourse(name) {
  if (!name) return ''
  const match = name.match(/UC\s*(\d+)/i)
  if (!match) return ''

  const number = parseInt(match[1], 10)
  return number <= 6 ? 'azul' : 'verde'
}

export function getColorPriority(color) {
  if (color === BLUE) return 0
  if (color === GREEN) return 1
  return 2
}

