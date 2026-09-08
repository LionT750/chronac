import { Moon, Sun } from 'lucide-react'
import { Button } from './ui/button'
import { useTheme } from '../theme'

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()
  const label = `Ativar tema ${theme === 'light' ? 'escuro' : 'claro'}`
  return (
    <Button type="button" variant="outline" size="icon" onClick={toggleTheme}
      aria-label={label} title={label}>
      {theme === 'light' ? <Moon aria-hidden="true" /> : <Sun aria-hidden="true" />}
    </Button>
  )
}
