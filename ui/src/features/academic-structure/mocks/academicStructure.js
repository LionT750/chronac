export const courses = [
  { id: 'tds', name: 'Técnico em Desenvolvimento de Sistemas' },
  { id: 'adm', name: 'Técnico em Administração' },
  { id: 'design', name: 'Técnico em Design Gráfico' },
]

export const teachers = [
  { id: 'lucas', name: 'Lucas Oliveira' },
  { id: 'ana', name: 'Ana Martins' },
  { id: 'bruno', name: 'Bruno Santos' },
  { id: 'camila', name: 'Camila Costa' },
  { id: 'daniel', name: 'Daniel Lima' },
  { id: 'elisa', name: 'Elisa Souza' },
  { id: 'felipe', name: 'Felipe Rocha' },
  { id: 'gabriela', name: 'Gabriela Alves' },
  { id: 'helena', name: 'Helena Ribeiro' },
  { id: 'igor', name: 'Igor Mendes' },
]

export const rooms = [
  { id: 'lab-01', name: 'Laboratório 01' },
  { id: 'lab-02', name: 'Laboratório 02' },
  { id: 'lab-03', name: 'Laboratório 03' },
  { id: 'sala-01', name: 'Sala 01' },
  { id: 'sala-02', name: 'Sala 02' },
  { id: 'sala-03', name: 'Sala 03' },
]

export const weekdays = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo']

const subjectNames = [
  'Programação Web', 'Banco de Dados', 'UX/UI', 'Engenharia de Software',
  'Lógica de Programação', 'Redes de Computadores', 'Sistemas Operacionais',
  'Programação Orientada a Objetos', 'Desenvolvimento Mobile', 'Segurança da Informação',
  'Testes de Software', 'Arquitetura de Sistemas', 'Gestão de Projetos',
  'Inglês Técnico', 'Matemática Aplicada', 'Empreendedorismo', 'Ética Profissional', 'Projeto Integrador',
]

function configuredSubjects(names) {
  return names.map((name, index) => ({
    id: `subject-${index + 1}`, name,
    teacherId: teachers[index % teachers.length].id,
    workload: index % 3 === 0 ? 80 : 40,
    roomId: rooms[index % rooms.length].id,
    availableDays: [],
  }))
}

/** @type {import('../types/academic.js').AcademicClass[]} */
export const academicClasses = [
  {
    id: 'tds-2026-2', name: 'TDS 2026/2', course: courses[0], shift: 'Noturno', semester: '2026/2', active: true,
    subjects: configuredSubjects(subjectNames).map((subject, index) => ({
      ...subject,
      ...(index === 1 ? { roomId: undefined } : {}),
      ...(index === 2 ? { teacherId: undefined } : {}),
      ...(index === 3 ? { workload: undefined } : {}),
    })),
  },
  {
    id: 'adm-2026-2', name: 'ADM 2026/2', course: courses[1], shift: 'Matutino', semester: '2026/2', active: true,
    subjects: configuredSubjects(['Fundamentos de Administração', 'Gestão de Pessoas', 'Contabilidade', 'Marketing', 'Logística', 'Planejamento Estratégico']),
  },
  {
    id: 'design-2026-2', name: 'DESIGN 2026/2', course: courses[2], shift: 'Vespertino', semester: '2026/2', active: true,
    subjects: [],
  },
  {
    id: 'tds-2026-1', name: 'TDS 2026/1', course: courses[0], shift: 'Noturno', semester: '2026/1', active: false,
    subjects: configuredSubjects(subjectNames.slice(0, 6)),
  },
]
