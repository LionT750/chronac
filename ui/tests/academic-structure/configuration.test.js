import assert from 'node:assert/strict'
import { test } from 'node:test'
import { academicClasses } from '../../src/features/academic-structure/mocks/academicStructure.js'
import { filterClasses, getClassSummary, getSubjectPendingFields, isSubjectConfigured, updateSubjectConfiguration } from '../../src/features/academic-structure/utils/configuration.js'

test('mock class reports complete subjects, distinct resources and explicit pending fields', () => {
  const summary = getClassSummary(academicClasses[0])
  assert.equal(summary.total, 18)
  assert.equal(summary.configured, 15)
  assert.equal(summary.percentage, 83)
  assert.equal(summary.teacherCount, 10)
  assert.equal(summary.roomCount, 6)
  assert.deepEqual(summary.pending.map(getSubjectPendingFields), [['sala'], ['professor'], ['carga horária']])
  assert.equal(summary.ready, false)
})

test('saving pending fields completes the class and clearing a field makes it pending again', () => {
  const original = structuredClone(academicClasses)
  let classes = updateSubjectConfiguration(academicClasses, 'tds-2026-2', 'subject-2', { roomId: 'lab-01' })
  assert.equal(getClassSummary(classes[0]).percentage, 89)
  classes = updateSubjectConfiguration(classes, 'tds-2026-2', 'subject-3', { teacherId: 'ana' })
  classes = updateSubjectConfiguration(classes, 'tds-2026-2', 'subject-4', { workload: 80 })
  const summary = getClassSummary(classes[0])
  assert.equal(summary.percentage, 100)
  assert.equal(summary.ready, true)
  assert.equal(summary.pending.length, 0)
  assert.deepEqual(academicClasses, original)
  assert.strictEqual(classes[1], academicClasses[1])
  classes = updateSubjectConfiguration(classes, 'tds-2026-2', 'subject-4', { teacherId: undefined })
  assert.equal(getClassSummary(classes[0]).ready, false)
  assert.equal(getClassSummary(classes[0]).percentage, 94)
})

test('zero, negative, non-finite and non-numeric workloads are pending', () => {
  for (const workload of [undefined, 0, -1, NaN, Infinity, '80']) {
    assert.equal(isSubjectConfigured({ teacherId: 'ana', roomId: 'lab-01', workload }), false)
  }
  assert.equal(isSubjectConfigured({ teacherId: 'ana', roomId: 'lab-01', workload: 0.5 }), true)
  assert.deepEqual(getSubjectPendingFields({}), ['professor', 'carga horária', 'sala'])
})

test('no subjects never counts as ready; inactive class retains its configuration', () => {
  const empty = getClassSummary(academicClasses[2])
  assert.equal(empty.percentage, 0)
  assert.equal(empty.ready, false)
  assert.equal(empty.teacherCount, 0)
  assert.equal(empty.roomCount, 0)
  assert.equal(getClassSummary(academicClasses[3]).ready, true)
})

test('search ignores accents, case and outer whitespace and matches course, shift or period', () => {
  assert.equal(filterClasses(academicClasses, '  administracao ').length, 1)
  assert.equal(filterClasses(academicClasses, 'NOTURNO').length, 2)
  assert.equal(filterClasses(academicClasses, '2026/1').length, 1)
  assert.equal(filterClasses(academicClasses, 'unknown').length, 0)
  assert.equal(filterClasses(academicClasses, '').length, academicClasses.length)
})
