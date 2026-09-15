import RegistrationPage from '../components/RegistrationPage'

const fields = [
  { key: 'name', label: 'Nome do professor', required: true },
  { key: 'code', label: 'Código' },
]

export default function TeacherPage(props) {
  return <RegistrationPage title="Professores" singular="professor" fields={fields} {...props} />
}
