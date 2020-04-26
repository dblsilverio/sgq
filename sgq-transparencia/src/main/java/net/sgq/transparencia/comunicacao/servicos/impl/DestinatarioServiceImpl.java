package net.sgq.transparencia.comunicacao.servicos.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.sgq.transparencia.comunicacao.modelos.Destinatario;
import net.sgq.transparencia.comunicacao.modelos.DestinatarioRepository;
import net.sgq.transparencia.comunicacao.modelos.to.DestinatarioTO;
import net.sgq.transparencia.comunicacao.servicos.DestinatarioService;

@Service
public class DestinatarioServiceImpl implements DestinatarioService {

	@Autowired
	private DestinatarioRepository repository;

	@Override
	public List<DestinatarioTO> todos() {
		return repository.findAll().stream().map(d -> d.toTO()).collect(Collectors.toList());
	}

	@Override
	public List<DestinatarioTO> interessadosRecall() {
		return repository.findByAssinanteRecallIsTrue().stream().map(d -> d.toTO()).collect(Collectors.toList());
	}

	@Override
	public List<DestinatarioTO> interessadosIncidentes() {
		return repository.findByAssinanteEventosIsTrue().stream().map(d -> d.toTO()).collect(Collectors.toList());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long salvarDestinatario(DestinatarioTO destinatarioTO, Long id) {

		Destinatario destinatario;

		if (id == null || id == 0) {
			destinatario = novoDestinatario(destinatarioTO);
		} else {
			destinatario = atualizaDestinatario(destinatarioTO, id);
		}

		return destinatario.getId();

	}

	private Destinatario novoDestinatario(DestinatarioTO destinatarioTO) {
		return this.repository.save(new Destinatario().fromTO(destinatarioTO));

	}

	private Destinatario atualizaDestinatario(DestinatarioTO destinatarioTO, Long id) {
		Optional<Destinatario> oDestinatario = consultaDestinatario(id);

		Destinatario destinatario = oDestinatario.get();

		return this.repository.save(destinatario.fromTO(destinatarioTO));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeDestinatario(Long id) {
		Optional<Destinatario> oDestinatario = consultaDestinatario(id);

		validaDestinatario(id, oDestinatario);

		Destinatario destinatario = oDestinatario.get();
		this.repository.delete(destinatario);
	}

	private Optional<Destinatario> consultaDestinatario(Long id) {
		Optional<Destinatario> oDestinatario = this.repository.findById(id);

		validaDestinatario(id, oDestinatario);
		return oDestinatario;
	}

	private void validaDestinatario(Long id, Optional<Destinatario> oDestinatario) {
		if (oDestinatario.isEmpty()) {
			throw new EntityNotFoundException(String.format("Entidade do tipo '%s' não encontrada para Id %d",
					Destinatario.class.getSimpleName(), id));
		}
	}

}
