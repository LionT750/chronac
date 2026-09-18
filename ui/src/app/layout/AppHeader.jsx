import { CalendarDays } from 'lucide-react'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { ThemeToggle } from '@/components/theme/ThemeToggle'
import { Button } from '@/components/ui/button'

export default function AppHeader({ page, onLogout, loggingOut }) {
  return (
    <div className="flex items-center justify-between border-b border-border bg-sidebar px-4 py-3 backdrop-blur-sm">
      <div className="flex items-center gap-3">
        <SidebarTrigger className="h-9 w-9 border border-input bg-secondary text-foreground hover:bg-hover" />
        <div className="flex items-center gap-2 text-foreground">
          <CalendarDays className="h-4 w-4 text-course-blue" />
          <span className="text-sm font-semibold tracking-wide">{page === 'teachers' ? 'Professores' : page === 'classes' ? 'Turmas' : 'Cronograma Acadêmico'}</span>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <ThemeToggle />
        <Button variant="outline" size="sm" onClick={onLogout} disabled={loggingOut}>
          {loggingOut ? 'Saindo…' : 'Sair'}
        </Button>
        <div className="hidden sm:block rounded-full border border-yellow-300 bg-yellow-100 px-2.5 py-1 text-xs font-medium text-yellow-900 dark:border-yellow-700 dark:bg-yellow-400 dark:text-yellow-950">
          Versão beta
        </div>
      </div>
    </div>
  )
}
