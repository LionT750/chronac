import { useState } from 'react'
import { teachers, classes } from '../mocks/academicRegistrations'

// O estado local permanece isolado da UI para permitir uma futura API sem acoplar os formulários ao transporte.
function useLocalRecords(initialRecords) {
  const [records, setRecords] = useState(initialRecords)

  function save(values, id) {
    const record = { ...values, id: id ?? crypto.randomUUID() }
    setRecords((current) => id
      ? current.map((item) => item.id === id ? record : item)
      : [...current, record])
  }

  function remove(id) {
    setRecords((current) => current.filter((item) => item.id !== id))
  }

  return { records, save, remove }
}

export function useAcademicRegistrations() {
  const teacherRecords = useLocalRecords(teachers)
  const classRecords = useLocalRecords(classes)
  return { teachers: teacherRecords, classes: classRecords }
}
