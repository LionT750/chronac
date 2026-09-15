import { CalendarDays } from 'lucide-react'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { ThemeToggle } from '@/components/theme/ThemeToggle'

export default function AppHeader({ page }) {
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
        <div className="hidden sm:block rounded-full border border-border bg-muted px-2.5 py-1 text-xs font-medium text-muted-foreground">
          Versão beta
        </div>
      </div>
    </div>
  )
}
