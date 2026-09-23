export function hasValidWorkload(workload) {
  return Number.isFinite(workload) && workload > 0
}

export function getSubjectPendingFields(subject) {
  return [
    !subject.teacherId && 'professor',
    !hasValidWorkload(subject.workload) && 'carga horária',
    !subject.roomId && 'sala',
  ].filter(Boolean)
}

export function isSubjectConfigured(subject) {
  return getSubjectPendingFields(subject).length === 0
}

export function getClassSummary(academicClass) {
  const { subjects } = academicClass
  const configured = subjects.filter(isSubjectConfigured).length
  const pending = subjects.filter((subject) => !isSubjectConfigured(subject))
  return {
    total: subjects.length,
    configured,
    pending,
    percentage: subjects.length ? Math.round(configured / subjects.length * 100) : 0,
    ready: subjects.length > 0 && pending.length === 0,
    teacherCount: new Set(subjects.map((subject) => subject.teacherId).filter(Boolean)).size,
    roomCount: new Set(subjects.map((subject) => subject.roomId).filter(Boolean)).size,
  }
}

export function updateSubjectConfiguration(classes, classId, subjectId, values) {
  return classes.map((academicClass) => academicClass.id === classId ? {
    ...academicClass,
    subjects: academicClass.subjects.map((subject) => subject.id === subjectId ? { ...subject, ...values } : subject),
  } : academicClass)
}

export function filterClasses(classes, query) {
  const normalize = (value) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')
  const term = normalize(query.trim())
  return classes.filter((item) => normalize(`${item.name} ${item.course.name} ${item.shift} ${item.semester}`).includes(term))
}
