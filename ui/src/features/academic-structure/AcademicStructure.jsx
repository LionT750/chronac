import AcademicStructurePage from './pages/AcademicStructurePage'
import ClassDetailsPage from './pages/ClassDetailsPage'

export default function AcademicStructure({ classId, classes, saveSubject, createClass, onNavigate }) {
  if (classId) return <ClassDetailsPage key={classId} academicClass={classes.find((item) => item.id === classId)} saveSubject={saveSubject} onBack={() => onNavigate('/academic-structure')} />
  return <AcademicStructurePage classes={classes} createClass={createClass} onOpen={(id) => onNavigate(`/academic-structure/${encodeURIComponent(id)}`)} />
}
