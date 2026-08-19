// components/layout/AppShell.jsx
// Estrutura raiz: só layout, não sabe nada sobre calendário ou dados
export function AppShell({ sidebar, children }) {
  // Simplified shell: avoid relying on external SidebarProvider
  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background">
      {sidebar}
      <main className="flex-1 flex flex-col overflow-auto p-4 gap-4">
        {children}
      </main>
    </div>
  );
}