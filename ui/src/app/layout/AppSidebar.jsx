import {
  Sidebar, SidebarHeader, SidebarContent, SidebarGroup, SidebarGroupContent,
  SidebarMenu, SidebarMenuItem, SidebarMenuButton, SidebarFooter, useSidebar,
} from '@/components/ui/sidebar'
import { Sparkles } from 'lucide-react'
import { sidebarItems } from '../navigation'

function NavigationButton({ onClick, ...props }) {
  const { setOpenMobile } = useSidebar()
  return (
    <SidebarMenuButton {...props} onClick={() => {
      onClick()
      setOpenMobile(false)
    }} />
  )
}

export default function AppSidebar({ page, onNavigate, systemStatus = 'idle' }) {
  const statusCopy = {
    ready: ['Sistema ativo', 'Grade sincronizada', 'bg-course-green-bg text-course-green'],
    loading: ['Atualizando grade', 'Buscando dados…', 'bg-course-blue-bg text-course-blue'],
    error: ['Grade indisponível', 'Verifique a conexão', 'bg-destructive-bg text-destructive'],
    idle: ['Sistema ativo', 'Aguardando dados', 'bg-muted text-muted-foreground'],
  }[systemStatus]
  return (
    <Sidebar
      collapsible="icon"
      className="border-r border-border bg-sidebar text-foreground shadow-panel"
    >
      <SidebarHeader className="border-b border-border px-3 py-4 group-data-[collapsible=icon]:px-2 group-data-[collapsible=icon]:py-3">
        <div className="flex items-center justify-center gap-3 group-data-[collapsible=icon]:justify-center">
          <img
            src="/logo.png"
            alt="Chronac Logo"
            className="brand-logo h-16 object-contain group-data-[collapsible=icon]:hidden"
          />
          <img
            src="/logo_p.png"
            alt="Chronac Compact Logo"
            className="hidden h-10 w-10 object-contain group-data-[collapsible=icon]:block"
          />
        </div>
      </SidebarHeader>

      <SidebarContent className="px-2 py-3 group-data-[collapsible=icon]:px-1">
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu className="gap-1">
              {sidebarItems.map(({ id, title, icon: Icon }) => (
                <SidebarMenuItem key={title}>
                  <NavigationButton
                    isActive={page === id}
                    aria-label={title}
                    tooltip={title}
                    aria-current={page === id ? 'page' : undefined}
                    onClick={() => onNavigate(id)}
                    className={
                      page === id
                        ? 'h-10 border-l-2 border-course-blue bg-course-blue-bg text-course-blue hover:bg-selected hover:text-selected-foreground group-data-[collapsible=icon]:justify-center'
                        : 'h-10 border-l-2 border-transparent text-foreground hover:bg-hover hover:text-foreground group-data-[collapsible=icon]:justify-center'
                    }
                  >
                    <Icon className="h-4 w-4" />
                    <span className="group-data-[collapsible=icon]:hidden">{title}</span>
                  </NavigationButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="border-t border-border p-3 group-data-[collapsible=icon]:hidden">
        <div className="flex items-center gap-3 rounded-lg border border-input bg-card p-2.5 text-left">
          <div className={`flex h-8 w-8 items-center justify-center rounded-md ${statusCopy[2]}`}>
            <Sparkles className="h-4 w-4" />
          </div>
          <div className="min-w-0">
                <div className="text-xs font-medium text-foreground">{statusCopy[0]}</div>
                <div className="truncate text-[10px] text-muted-foreground">{statusCopy[1]}</div>
          </div>
        </div>
      </SidebarFooter>
    </Sidebar>
  )
}
