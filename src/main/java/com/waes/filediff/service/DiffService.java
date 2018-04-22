package com.waes.filediff.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waes.filediff.exception.DiffValidationException;
import com.waes.filediff.model.DiffDataDTO;
import com.waes.filediff.model.DiffFilesDTO;
import com.waes.filediff.model.DiffServiceResultDTO;
import com.waes.filediff.repository.DiffRepository;

/**
 * Provides the means to save files for later comparison and to
 * compare 2 files data which were saved under the same ID.
 * 
 * @author Rodrigo Hackbarth
 */
@Service
public class DiffService {
	
	@Autowired
	private DiffRepository repository;

	/**
	 * Stores the provided file as the left file, associated with
	 * the given ID.
	 * 
	 * @param id identifies the file
	 * @param fileContent file content
	 */
	public void addLeft(int id, byte[] fileContent) {
		DiffFilesDTO diffDTO = repository.getFilesForComparisonById(id);
		if(diffDTO == null) {
			diffDTO = new DiffFilesDTO();
			diffDTO.setId(id);
		}
		
		diffDTO.setLeftFile(fileContent);
		
		repository.save(id, diffDTO);
	}
	
	/**
	 * Stores the provided file as the right file, associated with
	 * the given ID.
	 * 
	 * @param id identifies the file
	 * @param fileContent file content
	 */
	public void addRight(int id, byte[] fileContent) {
		DiffFilesDTO diffDTO = repository.getFilesForComparisonById(id);
		if(diffDTO == null) {
			diffDTO = new DiffFilesDTO();
			diffDTO.setId(id);
		}
		
		diffDTO.setRightFile(fileContent);
		
		repository.save(id, diffDTO);
	}
	
	/**
	 * Compares the two files stored under the given ID and returns the result
	 * of the comparison.
	 * 
	 * @param id identifier of the files to be compared
	 * @return the result of file comparison 
	 * @throws DiffValidationException if at least one of the files is not found
	 */
	public DiffServiceResultDTO diff(int id) throws DiffValidationException {
		DiffFilesDTO diffFiles = repository.getFilesForComparisonById(id);
		
		validateFilesExist(id, diffFiles);
		
		byte[] leftFile = diffFiles.getLeftFile();
		byte[] rightFile = diffFiles.getRightFile();
		
		DiffServiceResultDTO result = new DiffServiceResultDTO();
		result.setStatus("success");
		
		if(leftFile.length != rightFile.length) {
			result.setMessage("Files are different in size.");
		} else if(Arrays.equals(leftFile, rightFile)) {
			result.setMessage("Files are equal.");
		}else {
			checkFilesDiffs(leftFile, rightFile, result);
		}
		
		return result;
	}

	/**
	 * Validates if both files were provided for comparison.
	 * 
	 * @param id Identifier of the files to be compared
	 * @param diffFiles {@link DiffFilesDTO} to be validated
	 * @throws DiffValidationException if one or both files were not 
	 *                                 provided for comparison
	 */
	private void validateFilesExist(int id, DiffFilesDTO diffFiles) throws DiffValidationException {
		if(diffFiles == null) {
			throw new DiffValidationException("No file has been provided under ID: " + id);
		}
		
		if(diffFiles.getLeftFile() == null) {
			throw new DiffValidationException("Left file has not been provided under ID: " + id);
		}
		
		if(diffFiles.getRightFile() == null) {
			throw new DiffValidationException("Right file has not been provided under ID: " + id);
		}
	}
	
	/**
	 * Compares the two files and adds the offset and length of the
	 * differences found between them to the result.
	 * 
	 * @param leftFile File to be compared with the right file
	 * @param rightFile File to be compared with the left file
	 * @param result Result object to which the offset and length of
	 *               the diffs will be added
	 * @return The comparison result containing the diffs offset and 
	 *         length
	 */
	private DiffServiceResultDTO checkFilesDiffs(byte[] leftFile, byte[] rightFile, DiffServiceResultDTO result) {
		DiffDataDTO diffData = null;
		int offset = -1;
		
		for(int i = 0; i < leftFile.length; i++) {
			if(leftFile[i] != rightFile[i]) {
				if(offset == -1) {
					offset = i;
					diffData = new DiffDataDTO();
					diffData.setOffset(i);
				}
				
				diffData.incrementLength();
			}else if(diffData != null) {
				result.addDiff(diffData);
				offset = -1;
				diffData = null;
			}
		}
		
		if(diffData != null) {
			result.addDiff(diffData);
		}
		
		return result;
	}
}
