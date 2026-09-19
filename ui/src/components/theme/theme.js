import { createContext, useContext } from 'react'

export const THEME_KEY = 'chronac-theme'
export const ThemeContext = createContext(null)

export function readTheme() {
  try {
    return localStorage.getItem(THEME_KEY) === 'dark' ? 'dark' : 'light'
  } catch {
    return 'light'
  }
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) throw new Error('useTheme requires ThemeProvider')
  return context
}
