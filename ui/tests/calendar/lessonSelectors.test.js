import assert from 'node:assert/strict'
import { test } from 'node:test'
import { prepareLessons, filterLessons, filterLessonsForMonth, groupLessonsByDate, formatLesson, isFeasible } from '../../src/features/calendar/utils/lessonSelectors.js'
import { getSubjectCourse, getSubjectColor } from '../../src/features/calendar/utils/lessonPresentation.js'

const lesson = (subject, date, teacher = 'Ana') => ({
  subject: { name: subject }, teacher, room: { name: 'A' },
  timeslot: { date, dayOfWeek: 'MONDAY', startTime: '08:00:00', endTime: '09:00:00' },
})

test('course classification keeps UC 6 and UC 7 in their original groups', () => {
  assert.equal(getSubjectCourse('UC 6'), 'azul')
  assert.equal(getSubjectCourse('UC 7'), 'verde')
  assert.equal(getSubjectCourse('Other'), '')
  assert.equal(getSubjectColor('UC 1').bg, 'bg-course-blue-bg')
  assert.equal(getSubjectColor('UC 7').bg, 'bg-course-green-bg')
  assert.equal(getSubjectColor(null).bg, 'bg-muted')
})

test('lessons keep course priority before chronological order without mutating the payload', () => {
  const data = { lessons: [lesson('UC 7', '2026-07-01'), lesson('UC 6', '2026-07-06'), lesson('UC 1', '2026-07-02'), lesson('Other', '2026-07-01')] }
  const original = structuredClone(data)
  const prepared = prepareLessons(data)
  assert.deepEqual(prepared.map((item) => item.subject), ['UC 1', 'UC 6', 'UC 7', 'Other'])
  assert.equal(prepared[0].time, '08:00 - 09:00')
  assert.deepEqual(data, original)
})

test('filters intersect and date grouping excludes unallocated lessons', () => {
  const prepared = prepareLessons({ lessons: [lesson('UC 1', '2026-07-06'), lesson('UC 7', '2026-07-06'), lesson('UC 1', '2026-07-06', 'Bruno'), {}] })
  const filtered = filterLessons(prepared, { teacherFilter: 'Ana', subjectFilter: 'UC 1', courseFilter: 'azul', dayFilter: 'MONDAY' })
  assert.equal(filtered.length, 1)
  assert.deepEqual(groupLessonsByDate(filtered), { '2026-07-06': filtered })
  assert.equal(Object.hasOwn(groupLessonsByDate(prepared), '-'), false)
  assert.deepEqual(filterLessons(prepared, {}), prepared)
  assert.deepEqual(formatLesson({}), { date: '-', dayOfWeek: '-', time: '-', subject: '-', teacher: '-', room: '-' })
})

test('monthly visibility only includes lessons from the selected year and month', () => {
  const prepared = prepareLessons({ lessons: [
    lesson('July', '2026-07-14'),
    lesson('June', '2026-06-30'),
    lesson('August', '2026-08-01'),
    lesson('Next year', '2027-07-14'),
  ] })
  const visible = filterLessonsForMonth(prepared, new Date(2026, 6, 1))
  assert.deepEqual(visible.map((item) => item.subject), ['July'])
})

test('score compatibility retains string and object formats', () => {
  assert.equal(isFeasible('0hard/-3soft'), true)
  assert.equal(isFeasible('-1hard/0soft'), false)
  assert.equal(isFeasible({ feasible: true }), true)
  assert.equal(isFeasible(null), false)
})
