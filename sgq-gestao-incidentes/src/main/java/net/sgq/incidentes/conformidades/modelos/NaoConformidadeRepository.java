package net.sgq.incidentes.conformidades.modelos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.sgq.incidentes.conformidades.modelos.enums.Estado;

@Repository
public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, Long> {

	List<NaoConformidade> findByTituloContaining(String titulo);

	List<NaoConformidade> findByEstado(Estado estado);

}
