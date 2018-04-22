package com.waes.filediff.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.waes.filediff.model.DiffFilesDTO;

/**
 * Repository for the files to be compared.
 * 
 * @author Rodrigo Hackbarth
 */
@Repository
public class DiffRepository {

	/**
	 * Stores the files to be compared mapped by their IDs.
	 */
	private final Map<Integer, DiffFilesDTO> filesforComparisonMap = new HashMap<>();
	
	/**
	 * Returns the files stored with the provided id.
	 * 
	 * @param id Id of the files for comparison
	 * @return the files which were stored with the given id
	 */
	public DiffFilesDTO getFilesForComparisonById(int id) {
		return filesforComparisonMap.get(id);
	}
	
	/**
	 * Stores the provided file as the left file, associated with
	 * the given ID.
	 * 
	 * @param id identifies the file
	 * @param diffDTO {@link DiffFilesDTO} containing the file(s) 
	 * 		  		  to be stored
	 */
	public void save(int id, DiffFilesDTO diffDTO) {
		diffDTO.setId(id);
		filesforComparisonMap.put(id, diffDTO);
	}
}
