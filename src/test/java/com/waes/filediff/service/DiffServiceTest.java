package com.waes.filediff.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.waes.filediff.exception.DiffValidationException;
import com.waes.filediff.model.DiffDataDTO;
import com.waes.filediff.model.DiffFilesDTO;
import com.waes.filediff.model.DiffServiceResultDTO;
import com.waes.filediff.repository.DiffRepository;

/**
 * Provides unit tests for the {@link DiffService} class.
 * 
 * @author Rodrigo Hackbarth
 */
@RunWith(SpringRunner.class)
public class DiffServiceTest {

	@TestConfiguration
    static class DiffServiceTestContextConfiguration {
  
        @Bean
        public DiffService diffService() {
            return new DiffService();
        }
    }
	
	@Autowired
	private DiffService service;
	
	@MockBean
	private DiffRepository repository;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void testDiffNoFilesProvidedForGivenId() throws DiffValidationException {
		int id = 1;
		String expectedExceptionMessage = "No file has been provided under ID: " + id;
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(null);
		
		expectedException.expect(DiffValidationException.class);
		expectedException.expectMessage(expectedExceptionMessage);
		
		service.diff(id);
	}
	
	@Test
	public void testDiffLeftFileNotFoundForGivenId() throws DiffValidationException {
		int id = 1;
		String expectedExceptionMessage = "Left file has not been provided under ID: " + id;
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(null);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(new byte[] {});
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		expectedException.expect(DiffValidationException.class);
		expectedException.expectMessage(expectedExceptionMessage);
		
		service.diff(id);
	}
	
	@Test
	public void testDiffRightFileNotFoundForGivenId() throws DiffValidationException {
		int id = 1;
		String expectedExceptionMessage = "Right file has not been provided under ID: " + id;
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(new byte[] {});
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(null);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		expectedException.expect(DiffValidationException.class);
		expectedException.expectMessage(expectedExceptionMessage);
		
		service.diff(id);
	}
	
	@Test
	public void testDiffDifferentSize() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("differentSizeLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertEquals("Files are different in size.", diffResult.getMessage());
		assertThat(diffResult.getDiffs(), IsEmptyCollection.empty());
	}
	
	@Test
	public void testDiffFilesAreEqual() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("leftFileCopy.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertEquals("Files are equal.", diffResult.getMessage());
		assertThat(diffResult.getDiffs(), IsEmptyCollection.empty());
	}
	
	@Test
	public void testDiffOnlyFirstCharDifferent() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("onlyFirstCharDifferentLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertNull(diffResult.getMessage());
		assertEquals(1, diffResult.getDiffs().size());
		
		DiffDataDTO diff = diffResult.getDiffs().get(0);
		
		assertEquals(0, diff.getOffset());
		assertEquals(1, diff.getLength());
	}
	
	@Test
	public void testDiffOnlyLastCharDifferent() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("onlyLastCharDifferentLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertNull(diffResult.getMessage());
		assertEquals(1, diffResult.getDiffs().size());
		
		DiffDataDTO diff = diffResult.getDiffs().get(0);
		
		assertEquals(rightFile.length-1, diff.getOffset());
		assertEquals(1, diff.getLength());
	}
	
	@Test
	public void testDiffFirstAndLastCharsDifferent() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("firstAndLastCharsDifferentLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertNull(diffResult.getMessage());
		assertEquals(2, diffResult.getDiffs().size());
		
		DiffDataDTO diff = diffResult.getDiffs().get(0);
		assertEquals(0, diff.getOffset());
		assertEquals(1, diff.getLength());
		
		diff = diffResult.getDiffs().get(1);
		assertEquals(rightFile.length-1, diff.getOffset());
		assertEquals(1, diff.getLength());
	}
	
	@Test
	public void testDiffFirstTwoCharsAndLast3CharsDifferent() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("first2AndLast3CharsDifferentLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertNull(diffResult.getMessage());
		assertEquals(2, diffResult.getDiffs().size());
		
		DiffDataDTO diff = diffResult.getDiffs().get(0);
		assertEquals(0, diff.getOffset());
		assertEquals(2, diff.getLength());
		
		diff = diffResult.getDiffs().get(1);
		assertEquals(rightFile.length-3, diff.getOffset());
		assertEquals(3, diff.getLength());
	}
	
	@Test
	public void testDiffFilesCompletelyDifferent() throws URISyntaxException, IOException, DiffValidationException {
		int id = 1;
		byte[] leftFile = loadFile("leftFile.txt");
		byte[] rightFile = loadFile("completelyDifferentLeftFile.txt");
		
		DiffFilesDTO filesDTOMock = Mockito.mock(DiffFilesDTO.class);
		Mockito.when(filesDTOMock.getId()).thenReturn(id);
		Mockito.when(filesDTOMock.getLeftFile()).thenReturn(leftFile);
		Mockito.when(filesDTOMock.getRightFile()).thenReturn(rightFile);
		
		Mockito.when(repository.getFilesForComparisonById(Mockito.anyInt())).thenReturn(filesDTOMock);
		
		DiffServiceResultDTO diffResult = service.diff(id);
		
		assertEquals("success", diffResult.getStatus());
		assertNull(diffResult.getMessage());
		assertEquals(1, diffResult.getDiffs().size());
		
		DiffDataDTO diff = diffResult.getDiffs().get(0);
		assertEquals(0, diff.getOffset());
		assertEquals(leftFile.length, diff.getLength());
	}
	
	/**
	 * Finds the requested file in the resources and returns it.
	 * 
	 * @return The requested file from the resources folder
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private byte[] loadFile(String filename) throws URISyntaxException, IOException {
		Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
		return Files.readAllBytes(path);
	}
}
