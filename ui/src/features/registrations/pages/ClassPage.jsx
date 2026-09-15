import RegistrationPage from '../components/RegistrationPage'

const fields = [
  { key: 'name', label: 'Identificação da turma', required: true },
  { key: 'course', label: 'Curso' },
]

export default function ClassPage(props) {
  return <RegistrationPage title="Turmas" singular="turma" fields={fields} {...props} />
}
