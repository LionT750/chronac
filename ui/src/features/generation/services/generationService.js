// Camada de transporte da geração de cronograma em tempo real.
// O POST segue o mesmo padrão de CSRF de features/auth/authService.js; o stream
// SSE é lido via fetch + ReadableStream (não EventSource) para detectar o fim
// do stream sem reconexão automática e para permitir cancelamento via AbortSignal.

const GENERATE_PATH = '/api/v2/timetable/generate'

async function csrfHeaders() {
  const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin', cache: 'no-store' })
  if (!csrfResponse.ok) throw new Error('Não foi possível conectar ao servidor. Tente novamente.')
  const csrf = await csrfResponse.json()
  return { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token }
}

export async function startGeneration(demo) {
  const response = await fetch(GENERATE_PATH, {
    method: 'POST',
    credentials: 'same-origin',
    headers: await csrfHeaders(),
    body: JSON.stringify({ demo }),
  })
  if (response.status === 401 && typeof window !== 'undefined') {
    window.dispatchEvent(new Event('chronac:unauthorized'))
  }
  if (!response.ok) {
    const text = await response.text()
    const error = new Error(text || `HTTP ${response.status}`)
    error.status = response.status
    throw error
  }
  return response.json()
}

/**
 * Lê o stream SSE de uma geração até o servidor fechá-lo (fim da geração).
 * Cada evento `data:` é um snapshot completo da melhor solucao ate entao e é
 * entregue via onSnapshot; o AbortSignal cancela a leitura ao desmontar.
 * Retorna o snapshot final quando o stream fecha.
 */
export async function streamGeneration(jobId, signal, onSnapshot) {
  const response = await fetch(`${GENERATE_PATH}/${jobId}/stream`, {
    credentials: 'same-origin',
    signal,
  })
  if (response.status === 401 && typeof window !== 'undefined') {
    window.dispatchEvent(new Event('chronac:unauthorized'))
  }
  if (!response.ok) throw new Error(`HTTP ${response.status}`)

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let lastSnapshot = null

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() // guarda a última linha, possivelmente incompleta
    for (const line of lines) {
      if (line.startsWith('data:')) {
        lastSnapshot = JSON.parse(line.slice(5).trim())
        onSnapshot(lastSnapshot)
      }
    }
  }

  return lastSnapshot
}
