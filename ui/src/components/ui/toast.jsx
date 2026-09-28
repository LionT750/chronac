"use client";
import { createContext, useCallback, useContext, useMemo, useState } from "react"
import { CheckCircle2, Info, X, XCircle } from "lucide-react"
import { cn } from "@/lib/utils"

const ToastContext = createContext(null)

const TOAST_DURATION_MS = 5000
const TOAST_EXIT_MS = 150

const TOAST_VARIANTS = {
  success: { icon: CheckCircle2, iconClass: "text-course-green", ringClass: "border-course-green/30" },
  error: { icon: XCircle, iconClass: "text-destructive", ringClass: "border-destructive/30" },
  info: { icon: Info, iconClass: "text-course-blue", ringClass: "border-course-blue/30" },
}

function ToastCard({ toast, onDismiss }) {
  const variant = TOAST_VARIANTS[toast.type] ?? TOAST_VARIANTS.info
  const Icon = variant.icon

  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border bg-card p-3.5 text-foreground shadow-panel",
        variant.ringClass,
        toast.leaving ? "animate-toast-out" : "animate-toast-in",
      )}>
      <Icon className={cn("mt-0.5 size-4 shrink-0", variant.iconClass)} aria-hidden="true" />
      <p className="flex-1 text-sm leading-snug">{toast.message}</p>
      <button
        type="button"
        onClick={() => onDismiss(toast.id)}
        aria-label="Fechar aviso"
        className="shrink-0 rounded-md p-0.5 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground">
        <X className="size-3.5" />
      </button>
    </div>
  )
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((current) => current.map((toast) => (toast.id === id ? { ...toast, leaving: true } : toast)))
    setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, TOAST_EXIT_MS)
  }, [])

  const push = useCallback((type, message, duration) => {
    const id = crypto.randomUUID()
    setToasts((current) => [...current, { id, type, message }])
    setTimeout(() => dismiss(id), duration ?? TOAST_DURATION_MS)
  }, [dismiss])

  const value = useMemo(() => ({
    success: (message, duration) => push("success", message, duration),
    error: (message, duration) => push("error", message, duration),
    info: (message, duration) => push("info", message, duration),
  }), [push])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed right-4 bottom-4 z-50 flex w-full max-w-sm flex-col gap-2" aria-label="Avisos">
        {toasts.map((toast) => (
          <ToastCard key={toast.id} toast={toast} onDismiss={dismiss} />
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider.")
  }
  return context
}
