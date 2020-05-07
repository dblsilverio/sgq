package net.sgq.incidentes.incidentes.servicos;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.sgq.incidentes.conformidades.modelos.NaoConformidade;
import net.sgq.incidentes.conformidades.modelos.enums.Estado;
import net.sgq.incidentes.conformidades.servicos.NaoConformidadeService;
import net.sgq.incidentes.incidentes.modelos.Incidente;
import net.sgq.incidentes.incidentes.modelos.IncidenteRepository;

@Service
public class IncidenteServiceImpl implements IncidenteService {

	@Autowired
	private IncidenteRepository repository;

	@Autowired
	private NaoConformidadeService ncService;

	@Autowired
	private IncidenteValidator validator;

	private Logger logger = LoggerFactory.getLogger(IncidenteServiceImpl.class);

	@Override
	public Incidente consultaIncidente(Long id) {

		Optional<Incidente> oIC = this.repository.findById(id);

		if (oIC.isEmpty()) {
			return null;
		}

		return oIC.get();

	}

	@Override
	public Page<Incidente> listaIncidentes(Pageable pageable) {
		Page<Incidente> incidentes = this.repository.findAll(pageable);
		
		if(incidentes == null) {
			incidentes = new PageImpl<>(new ArrayList<>());
		}
		
		return incidentes;
	}

	@Override
	public Page<Incidente> listaIncidentes(String nome, Pageable pageable) {
		return this.repository.findByTituloContaining(nome, pageable);
	}

	@Override
	public Page<Incidente> listaIncidentes(Estado estado, Integer janelaMinutos, Pageable pageable) {
		Page<Incidente> incidentes;

		if (janelaMinutos == null) {
			if (estado == Estado.NAO_CONCLUIDA) {
				incidentes = this.repository.findBySituacaoNot(Estado.CONCLUIDA, pageable);
			} else {
				incidentes = this.repository.findBySituacao(estado, pageable);
			}
		} else {
			incidentes = this.repository.findBySituacaoAndConcluidoEmAfter(estado, trataData(janelaMinutos), pageable);
		}

		return incidentes;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long salvarIncidente(Incidente incidente, Long id) {
		Incidente ic = null;

		if (id == null || id == 0) {
			ic = novoIncidente(incidente);
		} else {
			ic = atualizaIncidente(incidente, id);
		}

		return ic.getId();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void adicionaNaoConformidade(Long iId, Long nCId) {
		NaoConformidade nc = this.ncService.consultaEntidadeNC(nCId);

		validator.validaNC(nc, nCId);

		Optional<Incidente> oIc = this.repository.findById(iId);

		Incidente incidente = validator.validaIncidenteRetornado(iId, oIc);

		if (!validator.validaDuplicidadeNC(incidente, nCId)) {
			incidente.getNcEnvolvidas().add(nc);
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeNaoConformidade(Long iId, Long nCId) {

		Optional<Incidente> oIc = this.repository.findById(nCId);

		Incidente incidente = validator.validaIncidenteRetornado(iId, oIc);

		if (incidente.getNcEnvolvidas().removeIf(nc -> nc.getId().equals(nCId))) {
			logger.info("NC #{} removida de Incidente #{}", nCId, iId);
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeTodasNaoConformidades(Long id) {
		Incidente incidente = retornaIncidenteValidado(id);

		incidente.getNcEnvolvidas().clear();
		logger.info("Removida todas as NCs incluidas no incidente {}", id);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void incidenteMudaEstado(Long iId, Estado estado) {

		Optional<Incidente> oIc = this.repository.findById(iId);

		Incidente nc = validator.validaIncidenteRetornado(iId, oIc);

		if (validator.trasicaoValida(nc, estado)) {
			logger.info("NC({}) transicionou de {} para {}", iId, nc.getSituacao(), estado);
			nc.setSituacao(estado);

			if (estado == Estado.CONCLUIDA) {
				nc.setConcluidoEm(new Date());
			}

		} else {
			throw new IllegalStateException(
					String.format("Transição de um Incidente de %s para %s não é permitida", nc.getSituacao(), estado));
		}

	}

	private Date trataData(Integer janelaMinutos) {
		return Date.from(LocalDateTime.now().minusMinutes(janelaMinutos).atZone(ZoneId.systemDefault()).toInstant());
	}

	private Incidente retornaIncidenteValidado(Long id) {
		Optional<Incidente> oIc = this.repository.findById(id);
		return validator.validaIncidenteRetornado(id, oIc);
	}

	private Incidente atualizaIncidente(Incidente incidenteTo, Long id) {

		Optional<Incidente> oIC = this.repository.findById(id);

		Incidente incidente = validator.validaIncidenteRetornado(id, oIC);

		if (incidente.getSituacao() == Estado.CONCLUIDA) {
			throw new IllegalStateException("Incidente já foi concluído e não pode mais ser alterado.");
		}

		incidenteTo.setId(id);

		return repository.save(incidenteTo);
	}

	private Incidente novoIncidente(Incidente ic) {

		return this.repository.save(ic);

	}

}
