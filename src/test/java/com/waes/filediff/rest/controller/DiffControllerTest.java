package com.waes.filediff.rest.controller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waes.filediff.exception.DiffValidationException;
import com.waes.filediff.model.DiffServiceResultDTO;
import com.waes.filediff.service.DiffService;

/**
 * Provides unit tests for testing the {@link DiffController} class.
 * 
 * @author Rodrigo Hackbarth
 */
@RunWith(SpringRunner.class)
@WebMvcTest(DiffController.class)
public class DiffControllerTest {

	/**
	 * The base path for all endpoints in this controller.
	 */
	private static final String ENDPOINTS_BASE_PATH = "/v1/diff/";

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private DiffService service;
	
	@Test
	public void testSaveLeftFile() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/left").toString();
	
		byte[] leftFile = loadFile("leftFile.txt");
		
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		objectNode.put("file", leftFile);
		String postRequestContent = objectNode.toString();
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(postRequestContent)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse resultResponse = result.getResponse();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(resultResponse.getContentAsString());
        assertEquals("success", jsonResultContent.get("status").asText());
        assertEquals("Left file was saved successfully.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testSaveLeftFileRequestBodyNotInJsonFormat() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/left").toString();
		 
		byte[] leftFile = loadFile("leftFile.txt");
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(leftFile)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("error", jsonResultContent.get("status").asText());
        assertEquals("Error reading request body.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testSaveLeftFileRequestBodyFileAssociatedWithWrongKey() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/left").toString();
		 
		byte[] leftFile = loadFile("leftFile.txt");
		
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		objectNode.put("file1", leftFile);
		String postRequestContent = objectNode.toString();
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(postRequestContent)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("error", jsonResultContent.get("status").asText());
        assertEquals("Wrong request format. Expected file data to be associated to 'file' key.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testSaveRightFile() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/right").toString();
	
		byte[] rightFile = loadFile("leftFileCopy.txt");
		
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		objectNode.put("file", rightFile);
		String postRequestContent = objectNode.toString();
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(postRequestContent)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse resultResponse = result.getResponse();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(resultResponse.getContentAsString());
        assertEquals("success", jsonResultContent.get("status").asText());
        assertEquals("Right file was saved successfully.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testSaveRightFileRequestBodyNotInJsonFormat() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/right").toString();
		 
		byte[] rightFile = loadFile("leftFileCopy.txt");
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(rightFile)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("error", jsonResultContent.get("status").asText());
        assertEquals("Error reading request body.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testSaveRightFileRequestBodyFileAssociatedWithWrongKey() throws Exception {
		String pathStr = new StringBuilder(ENDPOINTS_BASE_PATH).append(1).append("/right").toString();
		 
		byte[] leftFile = loadFile("leftFileCopy.txt");
		
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		objectNode.put("file1", leftFile);
		String postRequestContent = objectNode.toString();
		
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(pathStr)
                .content(postRequestContent)
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("error", jsonResultContent.get("status").asText());
        assertEquals("Wrong request format. Expected file data to be associated to 'file' key.", jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testDiffDiffValidationException() throws Exception {
		String expectedExceptionMessage = "Diff validation error test message.";
		DiffValidationException diffValidationException = new DiffValidationException(expectedExceptionMessage);

		String pathStr = ENDPOINTS_BASE_PATH+1;
		
		Mockito.when(service.diff(Mockito.anyInt())).thenThrow(diffValidationException);
		
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(pathStr);
		MvcResult result = mvc.perform(requestBuilder).andReturn();
		
		JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("error", jsonResultContent.get("status").asText());
        assertEquals(expectedExceptionMessage, jsonResultContent.get("message").asText());
	}
	
	@Test
	public void testDiffJsonFormattedResultFromDiffServiceResultDTO() throws Exception {
		String pathStr = ENDPOINTS_BASE_PATH+1;
		
		DiffServiceResultDTO diffServiceResultDTO = new DiffServiceResultDTO(); 
		diffServiceResultDTO.setStatus("success");
		diffServiceResultDTO.setMessage("Mock result message");
		
		Mockito.when(service.diff(Mockito.anyInt())).thenReturn(diffServiceResultDTO);
		
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(pathStr);
		MvcResult result = mvc.perform(requestBuilder).andReturn();
		
		JsonNode jsonResultContent = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertEquals("success", jsonResultContent.get("status").asText());
        assertEquals("Mock result message", jsonResultContent.get("message").asText());
	}

	/**
	 * Finds the requested file in the resources and returns it.
	 * 
	 * @return The requested file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private byte[] loadFile(String filename) throws URISyntaxException, IOException {
		Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());

		return Files.readAllBytes(path);
	}
}
