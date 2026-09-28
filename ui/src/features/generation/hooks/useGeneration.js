import { useEffect, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useToast } from '@/components/ui/toast'
import { startGeneration, streamGeneration } from '../services/generationService'

const JOB_ID_KEY = 'chronac:generation:jobId'

export const DEMO_MULTI_TURMA = 'MULTI_TURMA_DEMO'

/**
 * Estado da geração de cronograma em tempo real, controlado pelo TanStack Query:
 *
 * - `start` (mutation) dispara o POST que inicia a geração e persiste o jobId
 *   no localStorage — assim o usuário pode trocar de aba, recarregar a página e
 *   retomar de onde parou.
 * - `snapshot` (query) lê o stream SSE do jobId. Cada evento recebido atualiza
 *   o cache via setQueryData, então a UI re-renderiza atomicamente a cada
 *   melhoria do solver. Desmontar a página aborta o stream (a geração continua
 *   rodando no servidor); remontar refaz a requisição e retoma o stream.
 *
 * Os toasts de feedback (409, grade pronta, falha) ficam aqui para que funcionem
 * mesmo quando o usuário está em outra página — o hook é montado no App.
 */
export function useGeneration() {
  const queryClient = useQueryClient()
  const toast = useToast()
  const [jobId, setJobId] = useState(() => localStorage.getItem(JOB_ID_KEY))

  const start = useMutation({
    mutationFn: (demo) => startGeneration(demo),
    onSuccess: (data) => {
      localStorage.setItem(JOB_ID_KEY, data.jobId)
      setJobId(data.jobId)
    },
  })

  const snapshot = useQuery({
    queryKey: ['generation', 'snapshot', jobId],
    queryFn: ({ signal }) =>
      streamGeneration(jobId, signal, (latest) => {
        queryClient.setQueryData(['generation', 'snapshot', jobId], latest)
      }),
    enabled: !!jobId,
  })

  // 'finished' só quando o stream de fato fecha: isFetching fica true enquanto o
  // queryFn (stream) está aberto e vai a false quando ele resolve. Não usamos
  // isSuccess aqui porque o setQueryData do primeiro snapshot já o torna true.
  const status = !jobId
    ? 'idle'
    : start.isPending
      ? 'starting'
      : snapshot.isError
        ? 'error'
        : snapshot.isFetching || !snapshot.isSuccess
          ? 'running'
          : 'finished'

  // Tentar iniciar outra geração enquanto uma já está rodando (409 do backend).
  useEffect(() => {
    if (!start.isError) return
    if (start.error?.status === 409) {
      toast.info('Você já tem uma geração em andamento. Acompanhe a grade abaixo.')
    } else {
      toast.error(start.error?.message ?? 'Não foi possível iniciar a geração.')
    }
  }, [start.isError, start.error, toast])

  // Notifica quando a geração conclui (stream fecha) ou falha.
  const previousStatusRef = useRef(status)
  useEffect(() => {
    const previous = previousStatusRef.current
    previousStatusRef.current = status
    if (previous === 'running' && status === 'finished') {
      toast.success('Grade pronta! A geração foi concluída.')
    } else if (previous !== 'error' && status === 'error') {
      toast.error(snapshot.error?.message ?? 'Não foi possível concluir a geração. Tente novamente.')
    }
  }, [status, snapshot.error, toast])

  return { jobId, status, snapshot, start }
}
