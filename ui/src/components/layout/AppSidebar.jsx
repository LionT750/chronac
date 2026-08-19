// components/layout/AppSidebar.jsx
// Conteúdo do menu — ajuste os itens pro seu caso (dashboard, timetable, etc)
export function AppSidebar() {
  // Simple sidebar implementation to avoid missing third-party components
  return (
    <aside className="w-64 border-r border-border bg-muted min-h-full p-4">
      <div className="mb-4 font-semibold">Chronac</div>
      <nav>
        <ul className="space-y-2 text-sm">
          <li>
            <a href="" className="text-primary">Dashboard</a>
          </li>
          <li>
            <a href="" className="text-primary">Grade Horária</a>
          </li>
        </ul>
      </nav>
    </aside>
  );
}