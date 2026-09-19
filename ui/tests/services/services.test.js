import assert from 'node:assert/strict'
import { test } from 'node:test'
import { fetchTimetable } from '../../src/features/calendar/services/timetableService.js'
import { fetchServerDemo } from '../../src/services/serverDemoService.js'

test('services preserve existing URLs and JSON payloads', async (t) => {
  const calls = []
  t.mock.method(globalThis, 'fetch', async (url) => {
    calls.push(url)
    return { ok: true, json: async () => url === '/api/timetable' ? { lessons: [] } : 'Hey from master Lucas' }
  })
  assert.deepEqual(await fetchTimetable(), { lessons: [] })
  assert.equal(await fetchServerDemo(), 'Hey from master Lucas')
  assert.deepEqual(calls, ['/api/timetable', 'api/sayHeyMaster'])
})

test('services preserve HTTP and network failures', async (t) => {
  const fetchMock = t.mock.method(globalThis, 'fetch', async () => ({ ok: false, status: 503 }))
  await assert.rejects(fetchTimetable(), { message: 'HTTP 503' })
  await assert.rejects(fetchServerDemo(), { message: 'HTTP 503' })
  fetchMock.mock.mockImplementation(async () => { throw new Error('Offline') })
  await assert.rejects(fetchTimetable(), { message: 'Offline' })
  await assert.rejects(fetchServerDemo(), { message: 'Offline' })
})
