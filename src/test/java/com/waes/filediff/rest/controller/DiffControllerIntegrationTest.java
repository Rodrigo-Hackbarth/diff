package com.waes.filediff.rest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waes.filediff.Application;

/**
 * Provides integration tests for the application. 
 * 
 * @author Rodrigo Hackbarth
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DiffControllerIntegrationTest {
	
	/**
	 * Local host address.
	 */
	private static final String LOCALHOST = "http://localhost";

	/**
	 * The base path for all endpoints in this controller.
	 */
	private static final String ENDPOINTS_BASE_PATH = "/v1/diff/";
	
	@LocalServerPort
	private int port;
	
	TestRestTemplate restTemplate = new TestRestTemplate();
	
	@Test
	public void testSaveLeftFile() throws Exception {
		int id = 1;
		String pathStr = getRequestPath(id, "left");
	
		byte[] file = loadFile("leftFile.txt");
		
		String postRequestContent = getRequestBodyContent(file);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(postRequestContent, headers);
		
		ResponseEntity<String> response = executeRequest(pathStr, HttpMethod.POST, entity);
		
		String responseBody = response.getBody();
		
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(responseBody);
        
        JsonNode jsonResultContent = new ObjectMapper().readTree(responseBody);
        assertEquals("success", jsonResultContent.get("status").asText());
        assertEquals("Left file was saved successfully.", jsonResultContent.get("message").asText());
	}

	@Test
	public void testSaveRightFile() throws Exception {
		int id = 1;
		String pathStr = getRequestPath(id, "right");
		
		byte[] file = loadFile("leftFileCopy.txt");
		
		String postRequestContent = getRequestBodyContent(file);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(postRequestContent, headers);
		
		ResponseEntity<String> response = executeRequest(pathStr, HttpMethod.POST, entity);
		
		String responseBody = response.getBody();
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(responseBody);
		
		JsonNode jsonResultContent = new ObjectMapper().readTree(responseBody);
		assertEquals("success", jsonResultContent.get("status").asText());
		assertEquals("Right file was saved successfully.", jsonResultContent.get("message").asText());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDiffFirstTwoCharsAndLast3CharsDifferent() throws Exception {
		int id = 1;

		String pathStr = getRequestPath(id, "left");
		byte[] leftFile = loadFile("leftFile.txt");
		
		String postRequestContent = getRequestBodyContent(leftFile);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(postRequestContent, headers);
		
		ResponseEntity<String> response = executeRequest(pathStr, HttpMethod.POST, entity);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		pathStr = getRequestPath(id, "right");
		byte[] rightFile = loadFile("first2AndLast3CharsDifferentLeftFile.txt");
		
		postRequestContent = getRequestBodyContent(rightFile);
		
		entity = new HttpEntity<String>(postRequestContent, headers);
		
		response = executeRequest(pathStr, HttpMethod.POST, entity);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		pathStr = getRequestPath(id, null);
		response = executeRequest(pathStr, HttpMethod.GET, (HttpEntity<String>) HttpEntity.EMPTY);
		
		String responseBody = response.getBody();
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(responseBody);
		
		JsonNode jsonResultContent = new ObjectMapper().readTree(responseBody);
		assertEquals("success", jsonResultContent.get("status").asText());
		
		JsonNode diffArrayNode = jsonResultContent.get("diffs");
		assertEquals(2, diffArrayNode.size());
		
		JsonNode diffNode = diffArrayNode.get(0);
		assertEquals(0, diffNode.get("offset").asInt());
		assertEquals(2, diffNode.get("length").asInt());
		
		diffNode = diffArrayNode.get(1);
		assertEquals(leftFile.length-3, diffNode.get("offset").asInt());
		assertEquals(3, diffNode.get("length").asInt());
	}

	/**
	 * Returns the complete path for a request formatted with the given parameters.
	 * 
	 * @param id
	 * @param direction "left" or "right" for saving the files or null for the diff request.
	 * @return the complete path for the request.
	 */
	private String getRequestPath(int id, String direction) {
		StringBuilder requestPathBuilder = new StringBuilder(LOCALHOST).append(":").append(port).append(ENDPOINTS_BASE_PATH).append(id);
		
		return direction == null ? requestPathBuilder.toString() : requestPathBuilder.append("/").append(direction).toString();
	}
	
	/**
	 * Loads the requested file from the resources returns it.
	 * 
	 * @return The requested file
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private byte[] loadFile(String filename) throws URISyntaxException, IOException {
		Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
		byte[] file = Files.readAllBytes(path);
		
		return file;
		
	}
	
	/**
	 * Formats and returns the requests body contents for the 
	 * requests used to save files.
	 * <br/><br/>
	 * 
	 * When the file binary data is added to the {@link ObjectNode},
	 * its content is Base64 encoded.
	 * 
	 * @param file
	 * @return the formatted request body content
	 */
	private String getRequestBodyContent(byte[] file) {
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
		objectNode.put("file", file);
		return objectNode.toString();
	}
	
	/**
	 * Executes the request to the provided endpoint address and returns the response.
	 * 
	 * @param endpointUrl url to which the request will be sent
	 * @param httpMethod the HTTP request method to be executed
	 * @param requestEntity request entity consisting of headers and body
	 * @return the response of the request
	 */
	private ResponseEntity<String> executeRequest(String endpointUrl, HttpMethod httpMethod, HttpEntity<String> requestEntity) {
		return restTemplate.exchange(
				endpointUrl,
				httpMethod, requestEntity, String.class);
	}
}
