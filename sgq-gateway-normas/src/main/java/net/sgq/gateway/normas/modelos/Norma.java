package net.sgq.gateway.normas.modelos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Norma {

	private Long normaId;
	
	private String tituloNorma;

	private Map<String, Boolean> checkList = new LinkedHashMap<>();

	public Norma() {
		super();
	}
	
	public Norma(Long normaId, String norma, Map<String, Boolean> checkList) {
		super();
		this.normaId = normaId;
		this.tituloNorma = norma;
		this.checkList = checkList;
	}

	public Norma(Map<String, Object> normaResponse) {
		this.setNormaId(((Integer) normaResponse.get("id")).longValue());
		this.setNorma((String) normaResponse.get("nome"));
		
		@SuppressWarnings("unchecked")
		List<Object> normaCheckList = (List<Object>) normaResponse.get("checkList");
		
		for(Object item : normaCheckList) {
			@SuppressWarnings("unchecked")
			Map<String, String> entry = (Map<String, String>) item;
			
			this.checkList.put(entry.get("criterio"), Boolean.FALSE);
		}
		
	}

	public Long getNormaId() {
		return normaId;
	}

	public void setNormaId(Long normaId) {
		this.normaId = normaId;
	}

	public String getNorma() {
		return tituloNorma;
	}

	public void setNorma(String norma) {
		this.tituloNorma = norma;
	}

	public Map<String, Boolean> getCheckList() {
		return checkList;
	}

	public void setCheckList(Map<String, Boolean> checkList) {
		this.checkList = checkList;
	}

}
