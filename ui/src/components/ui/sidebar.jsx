// ============================================
// SIDEBAR COMPONENT - Componente de navegação lateral
// ============================================
// Gerencia layout responsivo, estado de aberto/fechado, e transições suaves
//
// PRINCIPAIS RECURSOS:
// • Responsivo: Sidebar normal em desktop, modal em mobile
// • Estado persistente: Lembra se o sidebar estava aberto (cookie)
// • Colapsável: Modo ícones (3rem) quando fechado
// • Atalho de teclado: Ctrl+B ou Cmd+B para alternar
// • Tooltips automáticos: Mostra nomes quando em modo ícones
// • Variantes: sidebar padrão, floating (flutuante), inset (com margens)
//
// ESTRUTURA TÍPICA:
// <SidebarProvider>
//   <Sidebar>
//     <SidebarHeader>Logo/Título</SidebarHeader>
//     <SidebarContent>
//       <SidebarGroup>
//         <SidebarGroupLabel>SEÇÃO</SidebarGroupLabel>
//         <SidebarGroupContent>
//           <SidebarMenu>
//             <SidebarMenuItem>
//               <SidebarMenuButton>Item</SidebarMenuButton>
//             </SidebarMenuItem>
//           </SidebarMenu>
//         </SidebarGroupContent>
//       </SidebarGroup>
//     </SidebarContent>
//     <SidebarFooter>Perfil</SidebarFooter>
//   </Sidebar>
//   <SidebarInset>
//     <SidebarTrigger /> {/* Botão hambúrguer */}
//     {/* Conteúdo principal */}
//   </SidebarInset>
// </SidebarProvider>

"use client";
import * as React from "react"
import { mergeProps } from "@base-ui/react/merge-props"
import { useRender } from "@base-ui/react/use-render"
import { cva } from "class-variance-authority"; // Usado para criar variantes de classes

import { useIsMobile } from "@/hooks/use-mobile" // Hook para detectar dispositivos móveis
import { cn } from "@/lib/utils" // Função para combinar nomes de classes
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet" // Componente de painel deslizante (mobile)
import { Skeleton } from "@/components/ui/skeleton" // Placeholder de carregamento
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import { PanelLeftIcon } from "lucide-react" // Ícone de menu hambúrguer

// ============================================
// CONSTANTES DE CONFIGURAÇÃO
// ============================================
// ⚠️ AJUSTE ESTES VALORES PARA PERSONALIZAR MANUALMENTE O SIDEBAR

// Nome do cookie que armazena se o sidebar está aberto ou fechado
const SIDEBAR_COOKIE_NAME = "sidebar_state"

// Tempo de vida do cookie (7 dias em segundos)
// Alterar para persistir a preferência por mais/menos tempo
// Exemplo: 60 * 60 * 24 * 30 para 30 dias
const SIDEBAR_COOKIE_MAX_AGE = 60 * 60 * 24 * 7

// 🎨 LARGURA - quando o sidebar está EXPANDIDO
// Padrão: "16rem" (256px) 
// Alterar para: "18rem" (288px), "20rem" (320px), "14rem" (224px), etc
const SIDEBAR_WIDTH = "16rem"

// 📱 LARGURA - em DISPOSITIVOS MÓVEIS
// Padrão: "18rem" (288px) - mais largo que desktop
// Alterar para a mesma que SIDEBAR_WIDTH ou maior se quiser
const SIDEBAR_WIDTH_MOBILE = "18rem"

// 🔲 LARGURA - quando o sidebar está COLAPSADO (modo ícones)
// Padrão: "3rem" (48px) - espaço para ícone + padding
// Não recomendado alterar a menos que use ícones maiores (ex: "4rem" para ícones 32px)
const SIDEBAR_WIDTH_ICON = "3.5rem"

// ⌨️ ATALHO DE TECLADO - alternar sidebar
// Padrão: "b" = Ctrl+B (Windows/Linux) ou Cmd+B (Mac)
// Alterar para qualquer letra: "n", "m", "s", etc
const SIDEBAR_KEYBOARD_SHORTCUT = "b"

// ============================================
// CONTEXT E HOOKS
// ============================================

// Context para compartilhar estado do sidebar entre componentes
const SidebarContext = React.createContext(null)

// ============================================
// HOOK: useSidebar()
// ============================================
// Acesso ao contexto do sidebar de qualquer componente filho
// Retorna: { state, open, setOpen, isMobile, openMobile, setOpenMobile, toggleSidebar }

function useSidebar() {
  const context = React.useContext(SidebarContext)
  if (!context) {
    throw new Error("useSidebar must be used within a SidebarProvider.")
  }

  return context
}

// ============================================
// COMPONENTE: SidebarProvider
// ============================================
// Provedor raiz do sidebar
// Props:
//   - defaultOpen: Se o sidebar inicia aberto (padrão: true)
//   - open/onOpenChange: Props para controlar de fora
//   - className/style: Personalizações
// Gerencia: Estado aberto/fechado, Cookies, Atalhos de teclado

function SidebarProvider({
  defaultOpen = true,
  open: openProp,
  onOpenChange: setOpenProp,
  className,
  style,
  children,
  ...props
}) {
  const isMobile = useIsMobile() // Detecta se é dispositivo móvel
  const [openMobile, setOpenMobile] = React.useState(false) // Estado separado para mobile (abre como modal)

  // Estado interno do sidebar (pode ser controlado de fora via openProp)
  const [_open, _setOpen] = React.useState(defaultOpen)
  const open = openProp ?? _open // Usa prop externa ou estado interno
  
  // Função para definir o estado de abertura
  // Salva em cookie para lembrar a preferência do usuário
  const setOpen = React.useCallback((value) => {
    const openState = typeof value === "function" ? value(open) : value
    if (setOpenProp) {
      setOpenProp(openState)
    } else {
      _setOpen(openState)
    }

    // Salva no cookie para persistir o estado mesmo após reload
    document.cookie = `${SIDEBAR_COOKIE_NAME}=${openState}; path=/; max-age=${SIDEBAR_COOKIE_MAX_AGE}`
  }, [setOpenProp, open])

  // Função auxiliar para alternar entre aberto e fechado
  const toggleSidebar = React.useCallback(() => {
    return isMobile ? setOpenMobile((open) => !open) : setOpen((open) => !open);
  }, [isMobile, setOpen, setOpenMobile])

  // Atalho de teclado: Ctrl/Cmd + B para alternar sidebar
  React.useEffect(() => {
    const handleKeyDown = (event) => {
      if (
        event.key === SIDEBAR_KEYBOARD_SHORTCUT &&
        (event.metaKey || event.ctrlKey)
      ) {
        event.preventDefault()
        toggleSidebar()
      }
    }

    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [toggleSidebar])

  // Estado: "expanded" (aberto) ou "collapsed" (fechado)
  // Usado para aplicar classes CSS diferentes
  const state = open ? "expanded" : "collapsed"

  // Valor do contexto que será passado para todos os componentes filhos
  const contextValue = React.useMemo(() => ({
    state,
    open,
    setOpen,
    isMobile,
    openMobile,
    setOpenMobile,
    toggleSidebar,
  }), [state, open, setOpen, isMobile, openMobile, setOpenMobile, toggleSidebar])

  return (
    <SidebarContext.Provider value={contextValue}>
      {/* Container wrapper que define as variáveis CSS de largura */}
      <div
        data-slot="sidebar-wrapper"
        style={
          {
            "--sidebar-width": SIDEBAR_WIDTH, // Largura quando expandido
            "--sidebar-width-icon": SIDEBAR_WIDTH_ICON, // Largura quando colapsado
            ...style
          }
        }
        className={cn(
          "group/sidebar-wrapper flex min-h-svh w-full has-data-[variant=inset]:bg-sidebar",
          className
        )}
        {...props}>
        {children}
      </div>
    </SidebarContext.Provider>
  );
}

// ============================================
// COMPONENTE: Sidebar
// ============================================
// Componente principal que renderiza o sidebar
// Props:
//   - side: "left" ou "right" - posição do sidebar
//   - variant: "sidebar", "floating", ou "inset" - estilo visual
//   - collapsible: "offcanvas" ou "icon" - comportamento ao fechar
//   - className: Classes customizadas

function Sidebar({
  side = "left",
  variant = "sidebar",
  collapsible = "offcanvas",
  className,
  children,
  dir,
  ...props
}) {
  const { isMobile, state, openMobile, setOpenMobile } = useSidebar()

  // Em dispositivos móveis com collapsible="none": renderiza sidebar fixo normal
  if (collapsible === "none") {
    return (
      <div
        data-slot="sidebar"
        className={cn(
          "flex h-full w-(--sidebar-width) flex-col bg-sidebar text-sidebar-foreground",
          className
        )}
        {...props}>
        {children}
      </div>
    );
  }

  // Em DISPOSITIVOS MÓVEIS: Renderiza como modal deslizante (Sheet)
  // O sidebar sai do fluxo normal e aparece como um painel flutuante
  if (isMobile) {
    return (
      <Sheet open={openMobile} onOpenChange={setOpenMobile} {...props}>
        <SheetContent
          dir={dir}
          data-sidebar="sidebar"
          data-slot="sidebar"
          data-mobile="true"
          className="w-(--sidebar-width) bg-sidebar p-0 text-sidebar-foreground [&>button]:hidden"
          style={
            {
              "--sidebar-width": SIDEBAR_WIDTH_MOBILE // Usa largura maior no mobile
            }
          }
          side={side}>
          <SheetHeader className="sr-only">
            <SheetTitle>Sidebar</SheetTitle>
            <SheetDescription>Displays the mobile sidebar.</SheetDescription>
          </SheetHeader>
          <div className="flex h-full w-full flex-col">{children}</div>
        </SheetContent>
      </Sheet>
    );
  }

  // Em DESKTOP: Renderiza sidebar em coluna lateral com suporte a colapsamento
  return (
    <div
      className="group peer hidden text-sidebar-foreground md:block" // Escondido em mobile, visível em md+
      data-state={state} // "expanded" ou "collapsed"
      data-collapsible={state === "collapsed" ? collapsible : ""} // Tipo de colapso
      data-variant={variant} // Estilo visual
      data-side={side}
      data-slot="sidebar">
      
      {/* 
        ============================================
        SIDEBAR-GAP: Espaço reservado para o sidebar
        ============================================
        Mantém espaço no layout mesmo quando sidebar sai de tela
        - Com collapsible="offcanvas": largura = 0 (não ocupa espaço)
        - Com collapsible="icon": largura = SIDEBAR_WIDTH_ICON (pequeno espaço)
      */}
      <div
        data-slot="sidebar-gap"
        className={cn(
          "relative w-(--sidebar-width) bg-transparent transition-[width] duration-200 ease-linear",
          "group-data-[collapsible=offcanvas]:w-0", // Sem espaço se offcanvas
          "group-data-[side=right]:rotate-180",
          variant === "floating" || variant === "inset"
            ? "group-data-[collapsible=icon]:w-[calc(var(--sidebar-width-icon)+(--spacing(4)))]"
            : "group-data-[collapsible=icon]:w-(--sidebar-width-icon)" // Espaço pequeno se icon
        )} />
      
      {/* 
        ============================================
        SIDEBAR-CONTAINER: Contêiner fixo do sidebar
        ============================================
        Posicionado fixed (não sai da tela ao fazer scroll)
        - Muda left/right dependendo de data-side
        - Move para fora da tela com collapsible=offcanvas
        - Transiciona suavemente com duration-200
      */}
      <div
        data-slot="sidebar-container"
        data-side={side}
        className={cn(
          "fixed inset-y-0 z-10 hidden h-svh w-(--sidebar-width) transition-[left,right,width] duration-200 ease-linear data-[side=left]:left-0 data-[side=left]:group-data-[collapsible=offcanvas]:left-[calc(var(--sidebar-width)*-1)] data-[side=right]:right-0 data-[side=right]:group-data-[collapsible=offcanvas]:right-[calc(var(--sidebar-width)*-1)] md:flex",
          // Variações visuais para "floating" e "inset"
          variant === "floating" || variant === "inset"
            ? "p-2 group-data-[collapsible=icon]:w-[calc(var(--sidebar-width-icon)+(--spacing(4))+2px)]"
            : "group-data-[collapsible=icon]:w-(--sidebar-width-icon) group-data-[side=left]:border-r group-data-[side=right]:border-l",
          className
        )}
        {...props}>
        {/* Elemento interno com estilo */}
        <div
          data-sidebar="sidebar"
          data-slot="sidebar-inner"
          className="flex size-full flex-col bg-sidebar group-data-[variant=floating]:rounded-lg group-data-[variant=floating]:shadow-sm group-data-[variant=floating]:ring-1 group-data-[variant=floating]:ring-sidebar-border">
          {children}
        </div>
      </div>
    </div>
  );
}

// ============================================
// COMPONENTE: SidebarTrigger
// ============================================
// Botão para abrir/fechar o sidebar
// Tipicamente colocado em SidebarInset

function SidebarTrigger({
  className,
  onClick,
  ...props
}) {
  const { toggleSidebar } = useSidebar()

  return (
    <Button
      data-sidebar="trigger"
      data-slot="sidebar-trigger"
      variant="ghost"
      size="icon-sm"
      className={cn(className)}
      onClick={(event) => {
        onClick?.(event)
        toggleSidebar()
      }}
      {...props}>
      <PanelLeftIcon /> {/* Ícone de menu hambúrguer */}
      <span className="sr-only">Toggle Sidebar</span>
    </Button>
  );
}

// ============================================
// COMPONENTE: SidebarRail
// ============================================
// Trilha invisível na borda do sidebar para arrastar e redimensionar
// Aparece como um fino divisor quando hover

function SidebarRail({
  className,
  ...props
}) {
  const { toggleSidebar } = useSidebar()

  return (
    <button
      data-sidebar="rail"
      data-slot="sidebar-rail"
      aria-label="Toggle Sidebar"
      tabIndex={-1}
      onClick={toggleSidebar}
      title="Toggle Sidebar"
      className={cn(
        "absolute inset-y-0 z-20 hidden w-4 transition-all ease-linear group-data-[side=left]:-right-4 group-data-[side=right]:left-0 after:absolute after:inset-y-0 after:start-1/2 after:w-[2px] hover:after:bg-sidebar-border sm:flex ltr:-translate-x-1/2 rtl:-translate-x-1/2",
        "in-data-[side=left]:cursor-w-resize in-data-[side=right]:cursor-e-resize",
        "[[data-side=left][data-state=collapsed]_&]:cursor-e-resize [[data-side=right][data-state=collapsed]_&]:cursor-w-resize",
        "group-data-[collapsible=offcanvas]:translate-x-0 group-data-[collapsible=offcanvas]:after:left-full hover:group-data-[collapsible=offcanvas]:bg-sidebar",
        "[[data-side=left][data-collapsible=offcanvas]_&]:-right-2",
        "[[data-side=right][data-collapsible=offcanvas]_&]:-left-2",
        className
      )}
      {...props} />
  );
}

// ============================================
// COMPONENTE: SidebarInset
// ============================================
// Área principal de conteúdo (à direita/esquerda do sidebar)
// Ocupa o espaço restante e adapta-se ao tamanho do sidebar

function SidebarInset({
  className,
  ...props
}) {
  return (
    <main
      data-slot="sidebar-inset"
      className={cn(
        "relative flex w-full flex-1 flex-col bg-background md:peer-data-[variant=inset]:m-2 md:peer-data-[variant=inset]:ml-0 md:peer-data-[variant=inset]:rounded-xl md:peer-data-[variant=inset]:shadow-sm md:peer-data-[variant=inset]:peer-data-[state=collapsed]:ml-2",
        className
      )}
      {...props} />
  );
}

// ============================================
// COMPONENTES DE ESTRUTURA
// ============================================

// Input para busca dentro do sidebar
function SidebarInput({
  className,
  ...props
}) {
  return (
    <Input
      data-slot="sidebar-input"
      data-sidebar="input"
      className={cn("h-8 w-full bg-background shadow-none", className)}
      {...props} />
  );
}

// Cabeçalho do sidebar (logo, título, etc)
function SidebarHeader({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-header"
      data-sidebar="header"
      className={cn("flex flex-col gap-2 p-2", className)}
      {...props} />
  );
}

// Rodapé do sidebar (perfil do usuário, etc)
function SidebarFooter({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-footer"
      data-sidebar="footer"
      className={cn("flex flex-col gap-2 p-2", className)}
      {...props} />
  );
}

// Divisor visual entre seções
function SidebarSeparator({
  className,
  ...props
}) {
  return (
    <Separator
      data-slot="sidebar-separator"
      data-sidebar="separator"
      className={cn("mx-2 w-auto bg-sidebar-border", className)}
      {...props} />
  );
}

// Área central com scroll para o conteúdo do menu
// group-data-[collapsible=icon]:overflow-hidden = oculta scroll quando colapsado
function SidebarContent({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-content"
      data-sidebar="content"
      className={cn(
        "no-scrollbar flex min-h-0 flex-1 flex-col gap-0 overflow-auto group-data-[collapsible=icon]:overflow-hidden",
        className
      )}
      {...props} />
  );
}

// ============================================
// COMPONENTES DE GRUPO (SidebarGroup*)
// ============================================
// Grupo = seção de links/botões no sidebar

function SidebarGroup({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-group"
      data-sidebar="group"
      className={cn("relative flex w-full min-w-0 flex-col p-2", className)}
      {...props} />
  );
}

// Rótulo do grupo (ex: "NAVEGAÇÃO", "CONFIGURAÇÕES")
// group-data-[collapsible=icon]:-mt-8 = Move para cima quando colapsado
// group-data-[collapsible=icon]:opacity-0 = Oculta o texto quando colapsado
function SidebarGroupLabel({
  className,
  render,
  ...props
}) {
  return useRender({
    defaultTagName: "div",
    props: mergeProps({
      className: cn(
        "flex h-8 shrink-0 items-center rounded-md px-2 text-xs font-medium text-sidebar-foreground/70 ring-sidebar-ring outline-hidden transition-[margin,opacity] duration-200 ease-linear group-data-[collapsible=icon]:-mt-8 group-data-[collapsible=icon]:opacity-0 focus-visible:ring-2 [&>svg]:size-4 [&>svg]:shrink-0",
        className
      ),
    }, props),
    render,
    state: {
      slot: "sidebar-group-label",
      sidebar: "group-label",
    },
  });
}

// Ação do grupo (botão no canto: add, settings, etc)
// group-data-[collapsible=icon]:hidden = Esconde quando colapsado
function SidebarGroupAction({
  className,
  render,
  ...props
}) {
  return useRender({
    defaultTagName: "button",
    props: mergeProps({
      className: cn(
        "absolute top-3.5 right-3 flex aspect-square w-5 items-center justify-center rounded-md p-0 text-sidebar-foreground ring-sidebar-ring outline-hidden transition-transform group-data-[collapsible=icon]:hidden after:absolute after:-inset-2 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 md:after:hidden [&>svg]:size-4 [&>svg]:shrink-0",
        className
      ),
    }, props),
    render,
    state: {
      slot: "sidebar-group-action",
      sidebar: "group-action",
    },
  });
}

// Container para o conteúdo do grupo (menu items, links, etc)
function SidebarGroupContent({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-group-content"
      data-sidebar="group-content"
      className={cn("w-full text-sm", className)}
      {...props} />
  );
}

function SidebarMenu({
  className,
  ...props
}) {
  return (
    <ul
      data-slot="sidebar-menu"
      data-sidebar="menu"
      className={cn("flex w-full min-w-0 flex-col gap-0", className)}
      {...props} />
  );
}

function SidebarMenuItem({
  className,
  ...props
}) {
  return (
    <li
      data-slot="sidebar-menu-item"
      data-sidebar="menu-item"
      className={cn("group/menu-item relative", className)}
      {...props} />
  );
}

const sidebarMenuButtonVariants = cva(
  "peer/menu-button group/menu-button flex w-full items-center gap-2 overflow-hidden rounded-md p-2 text-left text-sm ring-sidebar-ring outline-hidden transition-[width,height,padding] group-has-data-[sidebar=menu-action]/menu-item:pr-8 group-data-[collapsible=icon]:size-8! group-data-[collapsible=icon]:p-2! hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 active:bg-sidebar-accent active:text-sidebar-accent-foreground disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 data-open:hover:bg-sidebar-accent data-open:hover:text-sidebar-accent-foreground data-active:bg-sidebar-accent data-active:font-medium data-active:text-sidebar-accent-foreground [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate",
  {
    variants: {
      variant: {
        default: "hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
        outline:
          "bg-background shadow-[0_0_0_1px_var(--sidebar-border)] hover:bg-sidebar-accent hover:text-sidebar-accent-foreground hover:shadow-[0_0_0_1px_var(--sidebar-accent)]",
      },
      size: {
        default: "h-8 text-sm",
        sm: "h-7 text-xs",
        lg: "h-12 text-sm group-data-[collapsible=icon]:p-0!",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

function SidebarMenuButton({
  render,
  isActive = false,
  variant = "default",
  size = "default",
  tooltip,
  className,
  ...props
}) {
  const { isMobile, state } = useSidebar()
  const comp = useRender({
    defaultTagName: "button",
    props: mergeProps({
      className: cn(sidebarMenuButtonVariants({ variant, size }), className),
    }, props),
    render: !tooltip ? render : <TooltipTrigger render={render} />,
    state: {
      slot: "sidebar-menu-button",
      sidebar: "menu-button",
      size,
      active: isActive,
    },
  })

  if (!tooltip) {
    return comp
  }

  if (typeof tooltip === "string") {
    tooltip = {
      children: tooltip,
    }
  }

  return (
    <Tooltip>
      {comp}
      <TooltipContent
        side="right"
        align="center"
        hidden={state !== "collapsed" || isMobile}
        {...tooltip} />
    </Tooltip>
  );
}

function SidebarMenuAction({
  className,
  render,
  showOnHover = false,
  ...props
}) {
  return useRender({
    defaultTagName: "button",
    props: mergeProps({
      className: cn(
        "absolute top-1.5 right-1 flex aspect-square w-5 items-center justify-center rounded-md p-0 text-sidebar-foreground ring-sidebar-ring outline-hidden transition-transform group-data-[collapsible=icon]:hidden peer-hover/menu-button:text-sidebar-accent-foreground peer-data-[size=default]/menu-button:top-1.5 peer-data-[size=lg]/menu-button:top-2.5 peer-data-[size=sm]/menu-button:top-1 after:absolute after:-inset-2 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 md:after:hidden [&>svg]:size-4 [&>svg]:shrink-0",
        showOnHover &&
          "group-focus-within/menu-item:opacity-100 group-hover/menu-item:opacity-100 peer-data-active/menu-button:text-sidebar-accent-foreground aria-expanded:opacity-100 md:opacity-0",
        className
      ),
    }, props),
    render,
    state: {
      slot: "sidebar-menu-action",
      sidebar: "menu-action",
    },
  });
}

function SidebarMenuBadge({
  className,
  ...props
}) {
  return (
    <div
      data-slot="sidebar-menu-badge"
      data-sidebar="menu-badge"
      className={cn(
        "pointer-events-none absolute right-1 flex h-5 min-w-5 items-center justify-center rounded-md px-1 text-xs font-medium text-sidebar-foreground tabular-nums select-none group-data-[collapsible=icon]:hidden peer-hover/menu-button:text-sidebar-accent-foreground peer-data-[size=default]/menu-button:top-1.5 peer-data-[size=lg]/menu-button:top-2.5 peer-data-[size=sm]/menu-button:top-1 peer-data-active/menu-button:text-sidebar-accent-foreground",
        className
      )}
      {...props} />
  );
}

function SidebarMenuSkeleton({
  className,
  showIcon = false,
  ...props
}) {
  // Random width entre 50-90% para parecer mais natural
  const [width] = React.useState(() => {
    return `${Math.floor(Math.random() * 40) + 50}%`;
  })

  return (
    <div
      data-slot="sidebar-menu-skeleton"
      data-sidebar="menu-skeleton"
      className={cn("flex h-8 items-center gap-2 rounded-md px-2", className)}
      {...props}>
      {showIcon && (
        <Skeleton className="size-4 rounded-md" data-sidebar="menu-skeleton-icon" />
      )}
      <Skeleton
        className="h-4 max-w-(--skeleton-width) flex-1"
        data-sidebar="menu-skeleton-text"
        style={
          {
            "--skeleton-width": width
          }
        } />
    </div>
  );
}

// ============================================
// SUBMENU DO MENU
// ============================================
// Menu aninhado (ex: Página > Criar Página, Editar Página)
// group-data-[collapsible=icon]:hidden = Esconde TUDO quando colapsado

function SidebarMenuSub({
  className,
  ...props
}) {
  return (
    <ul
      data-slot="sidebar-menu-sub"
      data-sidebar="menu-sub"
      className={cn(
        "mx-3.5 flex min-w-0 translate-x-px flex-col gap-1 border-l border-sidebar-border px-2.5 py-0.5 group-data-[collapsible=icon]:hidden",
        className
      )}
      {...props} />
  );
}

// Item do submenu
function SidebarMenuSubItem({
  className,
  ...props
}) {
  return (
    <li
      data-slot="sidebar-menu-sub-item"
      data-sidebar="menu-sub-item"
      className={cn("group/menu-sub-item relative", className)}
      {...props} />
  );
}

// Botão do submenu
// group-data-[collapsible=icon]:hidden = Esconde quando colapsado
function SidebarMenuSubButton({
  render,
  size = "md",
  isActive = false,
  className,
  ...props
}) {
  return useRender({
    defaultTagName: "a",
    props: mergeProps({
      className: cn(
        "flex h-7 min-w-0 -translate-x-px items-center gap-2 overflow-hidden rounded-md px-2 text-sidebar-foreground ring-sidebar-ring outline-hidden group-data-[collapsible=icon]:hidden hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 active:bg-sidebar-accent active:text-sidebar-accent-foreground disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 data-[size=md]:text-sm data-[size=sm]:text-xs data-active:bg-sidebar-accent data-active:text-sidebar-accent-foreground [&>span:last-child]:truncate [&>svg]:size-4 [&>svg]:shrink-0 [&>svg]:text-sidebar-accent-foreground",
        className
      ),
    }, props),
    render,
    state: {
      slot: "sidebar-menu-sub-button",
      sidebar: "menu-sub-button",
      size,
      active: isActive,
    },
  });
}

// ============================================
// EXPORTAÇÕES - COMPONENTES DISPONÍVEIS
// ============================================
// Este arquivo exporta um sistema completo de sidebar componentizado
// Cada componente pode ser usado independentemente e composto junto com outros

// RESUMO DOS COMPONENTES:
// ├─ SidebarProvider: Provedor raiz do sistema (context)
// ├─ Sidebar: Container principal do sidebar
// ├─ SidebarTrigger: Botão para abrir/fechar
// ├─ SidebarRail: Trilha na borda para redimensionar
// ├─ SidebarInset: Area de conteúdo principal (ao lado do sidebar)
// ├─ Estrutura:
// │  ├─ SidebarHeader: Cabeçalho (logo, título)
// │  ├─ SidebarContent: Area com scroll (menu)
// │  ├─ SidebarFooter: Rodapé (perfil, settings)
// │  ├─ SidebarInput: Input de busca
// │  └─ SidebarSeparator: Divisor visual
// ├─ Grupos:
// │  ├─ SidebarGroup: Container de seção
// │  ├─ SidebarGroupLabel: Rótulo da seção
// │  ├─ SidebarGroupContent: Conteúdo
// │  └─ SidebarGroupAction: Botão de ação
// ├─ Menu:
// │  ├─ SidebarMenu: Lista <ul>
// │  ├─ SidebarMenuItem: Item <li>
// │  ├─ SidebarMenuButton: Botão/link
// │  ├─ SidebarMenuAction: Botão extra
// │  ├─ SidebarMenuBadge: Notificação/badge
// │  ├─ SidebarMenuSkeleton: Placeholder de carregamento
// │  ├─ SidebarMenuSub: Submenu
// │  ├─ SidebarMenuSubItem: Item do submenu
// │  └─ SidebarMenuSubButton: Botão do submenu
// └─ useSidebar: Hook para acessar contexto

export {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupAction,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInput,
  SidebarInset,
  SidebarMenu,
  SidebarMenuAction,
  SidebarMenuBadge,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSkeleton,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  SidebarProvider,
  SidebarRail,
  SidebarSeparator,
  SidebarTrigger,
  useSidebar,
}
