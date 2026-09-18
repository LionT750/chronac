// O endpoint legado responde com uma string JSON; preservamos seu formato e a URL existente.
export function fetchServerDemo() {
  return fetch('api/sayHeyMaster').then((response) => {
    if (response.status === 401 && typeof window !== 'undefined') window.dispatchEvent(new Event('chronac:unauthorized'))
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
  })
}
