import assert from 'node:assert/strict'
import { test } from 'node:test'
import { addDays, eachDayOfInterval, endOfMonth, format, isSunday } from 'date-fns'
import { academicDate, calendarDays, movePeriod, isSundayLesson } from '../../src/features/calendar/utils/calendar.js'

const key = (date) => format(date, 'yyyy-MM-dd')

test('monthly grids cover every Monday–Saturday exactly once, including leap years', () => {
  for (let year = 2024; year <= 2032; year++) {
    for (let month = 0; month < 12; month++) {
      const date = new Date(year, month, 1)
      const days = calendarDays(date, 'month')
      assert.ok([24, 30, 36].includes(days.length))
      assert.equal(new Set(days.map(key)).size, days.length)
      for (let offset = 0; offset < days.length; offset += 6) {
        assert.ok(days.slice(offset, offset + 6).some((day) => day.getMonth() === month))
      }
      days.forEach((day, index) => assert.equal(day.getDay(), index % 6 + 1))
      const expected = eachDayOfInterval({ start: date, end: endOfMonth(date) }).filter((day) => !isSunday(day)).map(key)
      assert.deepEqual(days.filter((day) => day.getMonth() === month).map(key), expected)
    }
  }
})

test('monthly grids use only the required four, five or six rows', () => {
  const cases = [
    [2027, 1, 4, '2027-02-01', '2027-02-27'],
    [2026, 6, 5, '2026-06-29', '2026-08-01'],
    [2026, 7, 6, '2026-07-27', '2026-09-05'],
    // Domingo não cria uma semana vazia antes do primeiro dia visível.
    [2026, 1, 4, '2026-02-02', '2026-02-28'],
    [2026, 2, 5, '2026-03-02', '2026-04-04'],
    [2026, 11, 5, '2026-11-30', '2027-01-02'],
  ]
  for (const [year, month, rows, first, last] of cases) {
    const days = calendarDays(new Date(year, month, 15), 'month')
    assert.equal(days.length / 6, rows)
    assert.equal(key(days[0]), first)
    assert.equal(key(days.at(-1)), last)
  }
})

test('weekly views anchor Sunday to the preceding Monday and cross year boundaries', () => {
  const days = calendarDays(new Date(2027, 0, 3), 'week')
  assert.deepEqual(days.map(key), ['2026-12-28', '2026-12-29', '2026-12-30', '2026-12-31', '2027-01-01', '2027-01-02'])
  assert.equal(key(movePeriod(days[0], 'week', 1)), '2027-01-04')
  assert.equal(key(movePeriod(days[0], 'week', -1)), '2026-12-21')
})

test('daily navigation skips Sunday in both directions without skipping Saturday', () => {
  const saturday = new Date(2026, 8, 12)
  const monday = new Date(2026, 8, 14)
  assert.equal(key(movePeriod(saturday, 'day', 1)), key(monday))
  assert.equal(key(movePeriod(monday, 'day', -1)), key(saturday))
  assert.equal(key(movePeriod(saturday, 'day', -1)), '2026-09-11')
  assert.equal(key(academicDate(addDays(saturday, 1))), key(monday))
  assert.deepEqual(calendarDays(addDays(saturday, 1), 'day').map(key), [key(monday)])
})

test('monthly navigation retains calendar-month arithmetic', () => {
  assert.equal(key(movePeriod(new Date(2026, 11, 1), 'month', 1)), '2027-01-01')
  assert.equal(key(movePeriod(new Date(2026, 0, 1), 'month', -1)), '2025-12-01')
  assert.equal(key(movePeriod(new Date(2024, 0, 31), 'month', 1)), '2024-02-29')
})

test('Sunday filter recognizes date and day label without changing lessons', () => {
  const lesson = { date: '2026-09-13', dayOfWeek: 'SUNDAY', teacher: 'Example' }
  const original = { ...lesson }
  assert.equal(isSundayLesson(lesson), true)
  assert.equal(isSundayLesson({ date: '-', dayOfWeek: 'DOMINGO' }), true)
  assert.equal(isSundayLesson({ date: '2026-09-12', dayOfWeek: 'SATURDAY' }), false)
  assert.deepEqual(lesson, original)
})
