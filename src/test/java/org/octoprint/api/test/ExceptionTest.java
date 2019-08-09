package org.octoprint.api.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.octoprint.api.OctoPrintHttpRequest;
import org.octoprint.api.OctoPrintInstance;
import org.octoprint.api.exceptions.ConnectionFailedException;
import org.octoprint.api.exceptions.InvalidApiKeyException;
import org.octoprint.api.exceptions.NoContentException;
import org.octoprint.api.exceptions.OctoPrintAPIException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExceptionTest {
	private URL testURL = null;
	private OctoPrintInstance instance = null;
	private OctoPrintHttpRequest request = null;
	private String contentType = "application/json";

	public ExceptionTest() {
		
		//create a temp url
		try {
			testURL = new URL("http://localhost");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HttpURLConnection connectionMock(int response,String output){
		HttpURLConnection conn = Mockito.mock(HttpURLConnection.class);
		
		try{
			Mockito.when(conn.getResponseCode()).thenReturn(response);
			Mockito.when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return conn;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	@Before
	public void beforeTest() throws Exception {
		instance = new OctoPrintInstance(testURL,"");
		request = Mockito.mock(OctoPrintHttpRequest.class);
		
	}
	
	@Test
	public void successfulUpdateTest(){
		HttpURLConnection conn = this.connectionMock(204, "");
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		assertTrue(instance.executeUpdate(request));
	}
	
	@Test 
	public void successfulQueryTest(){
		HttpURLConnection conn = this.connectionMock(200, "{\"connection\":true}");
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		assertEquals(instance.executeQuery(request).toJson(),"{\"connection\":true}");
	}
	
	@Test(expected = InvalidApiKeyException.class)
	public void invalidKeyTest(){
		
		HttpURLConnection conn = this.connectionMock(InvalidApiKeyException.STATUS_CODE, "");
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		//should throw an invalid API key exception
		instance.executeQuery(request);
		
	}
	
	@Test(expected = NoContentException.class)
	public void nonContentExceptionTest(){
		HttpURLConnection conn = this.connectionMock(NoContentException.STATUS_CODE, "");
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		//should throw an exception
		instance.executeQuery(request);
	}
	
	@Test(expected = ConnectionFailedException.class)
	public void connection() throws IOException{
		
		//purposly throw exception so that 
		HttpURLConnection conn = Mockito.mock(HttpURLConnection.class);
		Mockito.when(conn.getResponseCode()).thenThrow(IOException.class);
		
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		//should throw an invalid API key exception
		instance.executeQuery(request);
		
	}
	
	@Test(expected = OctoPrintAPIException.class)
	public void octoprintExceptionTest(){
		
		//throw an error not caught by any others
		HttpURLConnection conn = this.connectionMock(409 , "Conflict Error");
		Mockito.when(request.createConnection(testURL, "", contentType)).thenReturn(conn);
		
		//should throw an exception
		instance.executeQuery(request);
		
	}
}
