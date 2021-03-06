package net.sgq.incidentes.conformidades.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import net.sgq.incidentes.conformidades.modelos.NaoConformidade;
import net.sgq.incidentes.conformidades.modelos.enums.Estado;
import net.sgq.incidentes.conformidades.modelos.enums.Setor;
import net.sgq.incidentes.conformidades.modelos.enums.TipoNaoConformidade;
import net.sgq.incidentes.conformidades.servicos.NaoConformidadeService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class NCControllerTests {

	@MockBean
	private NaoConformidadeService service;

	@Autowired
	private MockMvc mock;

	@Value("${sgq.test.token}")
	private String jwtToken;

	@Test
	public void listaNCS() throws Exception {
		when(service.listaNCs(any())).thenReturn(new PageImpl<>(new ArrayList<>()));
		mock.perform(setJwt(get("/ncs"))).andExpect(status().isOk());
	}

	@Test
	public void listaNCSNome() throws Exception {
		preencheListaDeNCS();

		mock.perform(setJwt(get("/ncs?titulo=a"))).andExpect(status().isOk());
	}

	@Test
	public void listaNCSNomeNaoEncontrado() throws Exception {
		when(service.listaNCs(anyString(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
		mock.perform(setJwt(get("/ncs?titulo=a"))).andExpect(status().isNotFound());
	}

	@Test
	public void consultaNCId() throws Exception {
		when(service.consultaNC(Mockito.anyLong())).thenReturn(criaNC());

		mock.perform(setJwt(get("/ncs/1"))).andExpect(status().isOk());
	}

	@Test
	public void consultaNCIdNaoEncontrado() throws Exception {
		mock.perform(setJwt(get("/ncs/1"))).andExpect(status().isNotFound());
	}

	@Test
	public void consultaNCAbertas() throws Exception {
		preencheListaDeNCS();
		mock.perform(setJwt(get("/ncs?estado=abertas"))).andExpect(status().isOk());
	}

	@Test
	public void consultaNCEmAnalise() throws Exception {
		preencheListaDeNCS();
		mock.perform(setJwt(get("/ncs?estado=em_analise"))).andExpect(status().isOk());
	}

	@Test
	public void consultaNCConcluidas() throws Exception {
		preencheListaDeNCS();
		mock.perform(setJwt(get("/ncs?estado=concluidas"))).andExpect(status().isOk());
	}

	@Test
	public void consultaNCNaoConcluidas() throws Exception {
		preencheListaDeNCS();
		mock.perform(setJwt(get("/ncs?estado=nao_concluidas"))).andExpect(status().isOk());
	}

	@Test
	public void consultaNCQualquerEstadoNaoEncontrada() throws Exception {
		when(service.listaNCs(any(Estado.class), isNull(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
		mock.perform(setJwt(get("/ncs?estado=nao_concluidas"))).andExpect(status().isNotFound());
	}

	@Test
	public void novaNC() throws Exception {
		when(service.salvarNC(any(), any())).thenReturn(1L);
		mock.perform(setJwt(post("/ncs").contentType(MediaType.APPLICATION_JSON_VALUE).content("{}")))
				.andExpect(status().isCreated()).andExpect(header().string("Location", "/ncs/1"));
	}

	@Test
	public void atualizaNCValida() throws Exception {
		mock.perform(setJwt(put("/ncs/1").contentType(MediaType.APPLICATION_JSON_VALUE).content("{}")))
				.andExpect(status().isNoContent());
	}

	private void preencheListaDeNCS() {
		List<NaoConformidade> list = new ArrayList<>();
		list.add(criaNC());
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Page<NaoConformidade> pageNC = new PageImpl(list);
		when(service.listaNCs(any(), any())).thenReturn(pageNC);
		when(service.listaNCs(any(Estado.class), isNull(), any())).thenReturn(pageNC);
	}

	private NaoConformidade criaNC() {
		NaoConformidade nc = new NaoConformidade();
		nc.setTipoNaoConformidade(TipoNaoConformidade.AUSENCIA_MEDIDA);
		nc.setSetor(Setor.COMPRAS);
		nc.setPrejuizoApurado(Boolean.TRUE);
		nc.setEstado(Estado.ABERTA);
		return nc;
		
	}

	private MockHttpServletRequestBuilder setJwt(MockHttpServletRequestBuilder builder) {
		return builder.header("Authorization", "Bearer " + this.jwtToken);
	}

}
