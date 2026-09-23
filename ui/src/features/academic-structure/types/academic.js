/**
 * @typedef {{ id: string, name: string }} Course
 * @typedef {{ id: string, name: string }} Teacher
 * @typedef {{ id: string, name: string }} Room
 * @typedef {{ id: string, name: string, teacherId?: string, workload?: number, roomId?: string, availableDays?: string[] }} SubjectConfiguration
 * @typedef {{ id: string, name: string, course: Course, shift: string, semester: string, active: boolean, subjects: SubjectConfiguration[] }} AcademicClass
 */
export {}
