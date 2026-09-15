export function fetchTimetable() {
  return fetch('/api/timetable').then((response) => {
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
  })
}
