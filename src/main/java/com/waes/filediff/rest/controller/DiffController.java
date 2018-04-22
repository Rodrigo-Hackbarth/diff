package com.waes.filediff.rest.controller;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waes.filediff.exception.DiffValidationException;
import com.waes.filediff.model.DiffServiceResultDTO;
import com.waes.filediff.service.DiffService;

/**
 * Provides endpoints for uploading files to be compared
 * and an endpoint for checking the results of the diff applied
 * on the uploaded files.
 * 
 * @author Rodrigo Hackbarth
 */
@RestController
@RequestMapping("/v1/diff")
public class DiffController {
	
	@Autowired
	private DiffService diffService; 

	/**
	 * Saves the provided data to be compared with the
	 * data provided in {@link #saveRightFile(String, String)}.
	 * 
	 * <br/><br/>
	 * The provided file must be Base64 encoded.
	 * 
	 * @param id identifier for the data to be compared
	 * @param encodedFile base64 encoded data to 
	 * 		  be saved for later comparison
	 * @return message in Json format, to inform if the operation
	 *         was successful or if an error has occurred.
	 * 
	 */
	@RequestMapping(method=RequestMethod.POST, path="/{id}/left", consumes="application/json", produces="application/json")
	public String saveLeftFile(@PathVariable Integer id, @RequestBody String encodedFile) {
		byte[] file;
		
		try {
			file = getDecodedFileContentFromRequestBody(encodedFile);
		} catch (DiffValidationException e) {
			return e.getFormattedResultMessage();
		}
		
		diffService.addLeft(id, file);

		return buildJsonResultMessage("success", "Left file was saved successfully.");
	}
	
	/**
	 * Saves the provided data to be compared with the
	 * data provided in {@link #saveLeftFile(String, String)}.
	 * 
	 * <br/><br/>
	 * The provided file must be Base64 encoded.
	 * 
	 * @param id identifier for the data to be compared
	 * @param encodedFile base64 encoded data to 
	 * 		  be saved for later comparison
	 * @return message in Json format, to inform if the operation
	 *         was successful or if an error has occurred.
	 */
	@RequestMapping(method=RequestMethod.POST, path="/{id}/right", consumes="application/json", produces="application/json")
	public String saveRightFile(@PathVariable Integer id, @RequestBody String encodedFile) {
		byte[] file;
		
		try {
			file = getDecodedFileContentFromRequestBody(encodedFile);
		} catch (DiffValidationException e) {
			return e.getFormattedResultMessage();
		}
		
		diffService.addRight(id, file);
		
		return buildJsonResultMessage("success", "Right file was saved successfully.");
	}
	
	/**
	 * Perfoms the diff between the data that was saved under the 
	 * same ID via {@link #saveLeftFile(String, String)} and 
	 * {@link #saveRightFile(String, String)}.
	 * 
	 * @param id - identifier for finding the data to be compared
	 * @return a message to inform that the 2 uploaded data are equal,
	 * 		   that they are different in size or informing where the
	 * 		   differences are
	 */
	@RequestMapping(method=RequestMethod.GET, path="/{id}", produces="application/json")
	@ResponseBody
	public String diff(@PathVariable Integer id) {
		DiffServiceResultDTO diffResult;
		
		try {
			diffResult = diffService.diff(id);
			return new ObjectMapper().writeValueAsString(diffResult);
		} catch (DiffValidationException e) {
			return e.getFormattedResultMessage();
		} catch (JsonProcessingException e) {
			return buildJsonResultMessage("error", "Error parsing diff result."); 
		}
	}
	
	/**
	 * Decodes and returns the file content from the request body received by
	 * {@link #saveLeftFile(Integer, String)} and {@link #saveRightFile(Integer, String)}.
	 * 
	 * @param encodedFileContent Request body, in json format, containing the base64 encoded file 
	 * @return the decoded file content
	 * @throws DiffValidationException if the request body is not in json format or
	 * 								   if the request body, in json format, does not provide the 
	 * 								   file associated with the "file" key. 
	 *                                 
	 */
	private byte[] getDecodedFileContentFromRequestBody(String encodedFileContent) throws DiffValidationException {
		String file = null;
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(encodedFileContent);
			
			if(jsonNode.get("file") != null) {
				file = jsonNode.get("file").asText();
			}else {
				throw new DiffValidationException("Wrong request format. Expected file data to be associated to 'file' key.");
			}
				
			return Base64.getDecoder().decode(file);

		} catch (IOException e) {
			throw new DiffValidationException("Error reading request body.", e); 
		}
	}
	
	/**
	 * Builds a simple result message, in json format, composed of the 
	 * given status and message information.
	 * 
	 * @param status
	 * @param message
	 * @return The result message in json format
	 */
	private String buildJsonResultMessage(String status, String message) {
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		
		objectNode.put("status", status);
		objectNode.put("message", message);
		
		return objectNode.toString();
	}
}
