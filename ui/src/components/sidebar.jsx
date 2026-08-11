function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon">C</div>

        <div>
          <strong>Chronac</strong>
          <span>Gestão docente</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Dashboard
        </a>

        <a href="#" className="sidebar-item active">
          <span>Icon</span>
          Calendário
        </a>

        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Professores
        </a>

        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Turmas
        </a>

        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Relatórios
        </a>

        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Aprovações
        </a>

        <a href="#" className="sidebar-item">
          <span>Icon</span>
          Configurações
        </a>
      </nav>

      <div className="sidebar-footer">
        <span className="user-avatar">RM</span>

        <div>
          <strong>Nelma Rodrigues</strong>
          <span>Coordenação acadêmica</span>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar