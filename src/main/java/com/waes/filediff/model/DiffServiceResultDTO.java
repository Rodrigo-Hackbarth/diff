package com.waes.filediff.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of the diff service.
 * <br/><br/>
 * It contains all diff data found when
 * comparing the files.
 * 
 * @author Rodrigo Hackbarth
 */
public class DiffServiceResultDTO {

	private String status;
	private String message;
	private List<DiffDataDTO> diffs;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<DiffDataDTO> getDiffs() {
		createDiffsListIfNull();
		
		return Collections.unmodifiableList(diffs);
	}
	
	public void addDiff(DiffDataDTO diff) {
		createDiffsListIfNull();
		
		this.diffs.add(diff);
	}

	/**
	 * Creates the {@link #diffs} list if the list is null.
	 */
	private void createDiffsListIfNull() {
		if(diffs == null) {
			diffs = new ArrayList<>();
		}
	}
}
