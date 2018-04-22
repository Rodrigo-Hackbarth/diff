package com.waes.filediff.model;

/**
 * Stores the files to be compared.
 *
 * @author Rodrigo Hackbarth
 */
public class DiffFilesDTO {

	private Integer id;
	private byte[] leftFile;
	private byte[] rightFile;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public byte[] getLeftFile() {
		return leftFile;
	}
	
	public void setLeftFile(byte[] leftFile) {
		this.leftFile = leftFile;
	}
	
	public byte[] getRightFile() {
		return rightFile;
	}
	
	public void setRightFile(byte[] rightFile) {
		this.rightFile = rightFile;
	}
}
