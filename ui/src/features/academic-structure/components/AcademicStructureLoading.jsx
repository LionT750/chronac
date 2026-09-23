import { Skeleton } from '@/components/ui/skeleton'

export default function AcademicStructureLoading() {
  return <div role="status" aria-label="Carregando estrutura acadêmica" className="space-y-6">
    <span className="sr-only">Carregando estrutura acadêmica…</span>
    <Skeleton className="h-8 w-72" />
    <Skeleton className="h-4 w-full max-w-lg" />
    <div className="grid gap-5 md:grid-cols-2 2xl:grid-cols-3">
      {[0, 1, 2].map((id) => <Skeleton key={id} className="h-80 rounded-xl" />)}
    </div>
  </div>
}
