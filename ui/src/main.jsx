import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import './index.css'
import App from './app/App.jsx'
import { ThemeProvider } from './components/theme/ThemeProvider'
import { ToastProvider } from '@/components/ui/toast'

const queryClient = new QueryClient({
  defaultOptions: {
    // O stream SSE já cuida da reconexão (remontar a página retoma o stream);
    // retry automático apenas mascararia erros reais de geração.
    queries: { retry: false },
    mutations: { retry: false },
  },
})

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <ToastProvider><App /></ToastProvider>
      </ThemeProvider>
    </QueryClientProvider>
  </StrictMode>,
)
