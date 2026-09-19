export function fetchTimetable() {
  return fetch('/api/timetable').then((response) => {
    if (response.status === 401 && typeof window !== 'undefined') window.dispatchEvent(new Event('chronac:unauthorized'))
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
  })
}
