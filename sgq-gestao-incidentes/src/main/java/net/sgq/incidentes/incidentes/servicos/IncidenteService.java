package net.sgq.incidentes.incidentes.servicos;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.sgq.incidentes.conformidades.modelos.enums.Estado;
import net.sgq.incidentes.incidentes.modelos.Incidente;

public interface IncidenteService {

	Incidente consultaIncidente(Long id);

	Page<Incidente> listaIncidentes(Pageable pageable);

	Page<Incidente> listaIncidentes(String nome, Pageable pageable);

	Page<Incidente> listaIncidentes(Estado estado, String title, Integer janelaMinutos, Pageable pageable);

	List<Incidente> listaIncidentesPorPeriodo(Date inicio, Date fim);

	Long salvarIncidente(Incidente incidente, Long id);
	
	void adicionaNaoConformidade(Long iId, Long nCId);
	void removeNaoConformidade(Long iId, Long nCId);
	void removeTodasNaoConformidades(Long id);

	void incidenteMudaEstado(Long iId, Estado aberta);

	Map<String, Long> estatisticas();

}
