package com.waes.filediff.model;

/**
 * Data of a diff found when comparing files.
 * 
 * @author Rodrigo Hackbarth
 */
public class DiffDataDTO {

	private int offset;
	private int length;

	/**
	 * Increases the length of this diff by 1.
	 */
	public void incrementLength() {
		this.length++;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}
}
