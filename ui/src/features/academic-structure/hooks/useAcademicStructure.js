import { useState } from 'react'
import { academicClasses } from '../mocks/academicStructure'
import { updateSubjectConfiguration } from '../utils/configuration'

export function useAcademicStructure() {
  const [classes, setClasses] = useState(() => structuredClone(academicClasses))

  function saveSubject(classId, subjectId, values) {
    setClasses((current) => updateSubjectConfiguration(current, classId, subjectId, values))
  }

  function createClass(values) {
    const newClass = { ...values, id: crypto.randomUUID(), active: true, subjects: [] }
    setClasses((current) => [...current, newClass])
    return newClass.id
  }

  return { classes, saveSubject, createClass }
}
