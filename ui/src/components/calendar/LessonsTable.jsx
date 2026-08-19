export function LessonsTable({ lessons }) {
  return (

    <div className="w-full rounded-xl border border-slate-800 bg-[#0f111a] text-slate-200 shadow-xl overflow-hidden mt-4">
              {/* CONTROLADORES SUPERIORES DO CALENDÁRIO */}
              <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-[#141724]">
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="icon" onClick={mesAnterior} className="h-8 w-8 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" size="icon" onClick={proximoMes} className="h-8 w-8 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" onClick={irParaHoje} className="h-8 text-xs font-semibold px-3 border-slate-700 bg-slate-800/50 hover:bg-slate-800 text-white">
                    Hoje
                  </Button>
                  <h2 className="text-xl font-bold ml-2 capitalize text-white">
                    {format(currentMonth, "MMMM 'de' yyyy", { locale: ptBR })}
                  </h2>
                </div>
                
                {/* Abas estáticas de visualização */}
                <div className="flex bg-slate-900 p-0.5 text-xs font-medium text-slate-400 gap-1">
                  <button className="bg-slate-800 text-white shadow-sm px-3 py-1.5 rounded-md font-semibold">Mês</button>
                  <button className="px-3 py-1.5 rounded-md hover:text-white transition-colors" disabled>Semana</button>
                  <button className="px-3 py-1.5 rounded-md hover:text-white transition-colors" disabled>Dia</button>
                </div>
              </div>

              {/* CABEÇALHO DOS DIAS DA SEMANA */}
              <div className="grid grid-cols-7 border-b border-slate-800 bg-[#141724] text-left font-bold text-xs tracking-wider text-slate-400">
                {diasDaSemana.map((d) => (
                  <div key={d} className="p-3 border-r border-slate-800/50 last:border-r-0">{d}</div>
                ))}
              </div>

              {/* GRADE DE DIAS MENSAL */}
              <div className="grid grid-cols-7 bg-slate-950 gap-[1px]">
                {diasDoGrid.map((dia, idx) => {
                  const dataChave = format(dia, 'yyyy-MM-dd')
                  const aulasDoDia = lessonsByDate[dataChave] || []
                  const pertenceAoMesAtual = isSameMonth(dia, currentMonth)
                  const ehHoje = isSameDay(dia, new Date())

                  return (
                    <div 
                      key={idx} 
                      className="bg-[#0f111a] min-h-[140px] p-2 flex flex-col justify-between group hover:bg-[#151926] transition-colors border border-slate-900/40 cursor-pointer"
                      onClick={() => alert(`Ação para adicionar/gerenciar o dia: ${format(dia, 'dd/MM/yyyy')}`)}
                    >
                      {/* Cabeçalho do Card (Número do dia) */}
                      <div className="flex items-center justify-between mb-1">
                        <span className={`text-xs font-bold ${
                          ehHoje 
                            ? 'bg-blue-600 text-white h-5 w-5 flex items-center justify-center rounded-full' 
                            : pertenceAoMesAtual ? 'text-slate-300' : 'text-slate-600'
                        }`}>
                          {format(dia, 'd')}
                        </span>
                      </div>

                      {/* Lista das aulas pertencentes a este dia específico */}
                      <div className="flex-1 flex flex-col gap-0.5 overflow-hidden">
                        {aulasDoDia.slice(0, 3).map((aula, lIdx) => (
                          <div 
                            key={lIdx} 
                            className="px-2 py-1 text-[10px] font-medium rounded-sm border-l-[3px] border-amber-500 bg-amber-500/10 text-amber-300 flex flex-col shadow-sm gap-0.5 hover:bg-amber-500/20 transition-colors cursor-pointer"
                          >
                            <div className="flex items-center justify-between font-bold text-[12px] leading-none text-slate-400">
                              <span>{aula.time}</span>
                              <span className="text-slate-400 font-normal">{aula.room}</span>
                              <span className="truncate text-slate-100 font-semibold leading-none my-0.5">{aula.subject}</span>
                            </div>
                            <span className="text-[12px] text-slate-400 truncate">{aula.teacher}</span>
                          </div>
                        ))}
                      </div>

                      {/* Indicador de overflow caso o dia tenha mais de 3 aulas */}
                      {aulasDoDia.length > 3 && (
                        <div className="text-[10px] text-blue-400 font-bold mt-1 pl-1">
                          + {aulasDoDia.length - 3} aulas
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            </div>    
  )
}