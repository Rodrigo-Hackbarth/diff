package com.waes.filediff.exception;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Exception found during the validation for saving the files to be
 * compared by the diff operation and during the validation for 
 * performing the diff operation.
 * 
 * @author Rodrigo Hackbarth
 */
public class DiffValidationException extends Exception {

	private static final long serialVersionUID = -9081410565309969218L;

	public DiffValidationException(String message) {
		super(message);
	}
	
	public DiffValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Returns the formatted result message from this exception.
	 * <br/><br/>
	 * 
	 * The returned message is in the following json format:
	 * <br/>
	 * 
	 * {"status":"error","message":"exception message"}
	 * 
	 * @return the formatted result message from this exception
	 */
	public String getFormattedResultMessage() {
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		
		objectNode.put("status", "error");
		objectNode.put("message", getMessage());
		
		return objectNode.toString();
	}
}
