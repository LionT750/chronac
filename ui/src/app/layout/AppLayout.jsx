import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar'
import AppSidebar from './AppSidebar'
import AppHeader from './AppHeader'
import { TooltipProvider } from '@/components/ui/tooltip'

export default function AppLayout({ page, onNavigate, systemStatus = 'idle', children }) {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <TooltipProvider delay={200}>
      <SidebarProvider defaultOpen>
        <AppSidebar page={page} onNavigate={onNavigate} systemStatus={systemStatus} />
        <SidebarInset className="min-w-0 bg-background">
          <AppHeader page={page} />
          <div className="p-4 lg:p-6">{children}</div>
        </SidebarInset>
      </SidebarProvider>
      </TooltipProvider>
    </div>
  )
}
