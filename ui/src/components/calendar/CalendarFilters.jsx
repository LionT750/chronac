export function CalendarFilters({
  teacherFilter, setTeacherFilter, teacherOptions,
  subjectFilter, setSubjectFilter, subjectOptions,
  dayFilter, setDayFilter, dayOptions,
  onClear,
}) {
  return (
    <section className="flex flex-wrap gap-2 items-center border-b pb-4">
      <select value={teacherFilter} onChange={(e) => setTeacherFilter(e.target.value)}>
        <option value="">Todos os professores</option>
        {teacherOptions.map((t) => <option key={t} value={t}>{t}</option>)}
      </select>

      <select value={subjectFilter} onChange={(e) => setSubjectFilter(e.target.value)}>
        <option value="">Todas as disciplinas</option>
        {subjectOptions.map((s) => <option key={s} value={s}>{s}</option>)}
      </select>

      <select value={dayFilter} onChange={(e) => setDayFilter(e.target.value)}>
        <option value="">Todos os dias</option>
        {dayOptions.map((d) => <option key={d} value={d}>{d}</option>)}
      </select>

      <button className="ml-auto" onClick={onClear}>Limpar filtro</button>
    </section>
  )
}