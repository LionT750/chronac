export function formatLesson(lesson) {
  const t = lesson.timeslot

  return {
    date: t?.date ?? '-',
    dayOfWeek: t?.dayOfWeek ?? '-',
    time: t
      ? `${t.startTime?.slice(0, 5)} - ${t.endTime?.slice(0, 5)}`
      : '-',
    subject: lesson.subject?.name ?? '-',
    teacher: lesson.teacher ?? '-',
    room: lesson.room?.name ?? '-',
  }
}

// Timefold serializes the score as a string such as "0hard/-3soft".
// A timetable is feasible when its hard component is zero.
export function isFeasible(score) {
  if (typeof score === 'string') {
    const hard = score.match(/^(-?\d+)hard/)
    return hard != null && hard[1] === '0'
  }
  return score != null && score.feasible === false
}

export default { formatLesson, isFeasible }