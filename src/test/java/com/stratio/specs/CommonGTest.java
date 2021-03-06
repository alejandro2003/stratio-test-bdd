package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hjson.ParseException;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;

import com.stratio.tests.utils.ThreadProperty;

import cucumber.api.DataTable;
import static org.mockito.Mockito.*;

import com.ning.http.client.Response;

import java.util.concurrent.Future;

public class CommonGTest {

    @Test
    public void retrieveDataExceptionTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "invalid.conf";
	String type = "string";
	
	try {
	    commong.retrieveData(baseData, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("File does not exist: " + baseData);
	}
    }
    
    @Test
    public void retrieveDataStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataStringTest.conf";
	String type = "string";
	
	String returnedData = commong.retrieveData(baseData, type);
	assertThat(returnedData).as("Invalid information read").isEqualTo("username=username&password=password");
    }
    
    @Test
    public void retrieveDataInvalidJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataInvalidJsonTest.conf";
	String type = "json";
	
	try {
	    commong.retrieveData(baseData, type);
	    org.testng.Assert.fail("Expected ParseException");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(ParseException.class.toString());
	}
    }
    
    @Test
    public void retrieveDataValidJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataValidJsonTest.conf";
	String type = "json";
    
	String returnedData = commong.retrieveData(baseData, type);
	assertThat(returnedData).as("Invalid information read").isEqualTo("{\"key1\":\"value1\",\"key2\":{\"key3\":\"value3\"}}");
    }
    
    @Test
    public void modifyDataNullValueJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": null}}";
	String expectedData = "{\"key2\":{\"key3\":null}}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "DELETE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataInvalidModificationTypeStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	try {
	    commong.modifyData(data, type, modifications);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
	}
    }
    
    @Test
    public void modifyDataInvalidModificationTypeJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	try {
	    commong.modifyData(data, type, modifications);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
	}
    }
    
    @Test
    public void modifyDataDeleteStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username&", "DELETE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);		
    }
    
    @Test
    public void modifyDataAddStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "username=username&password=password&config=config";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("N/A", "ADD", "&config=config")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }
    
    @Test
    public void modifyDataUpdateStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "username=NEWusername&password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "UPDATE", "username=NEWusername")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);	
    }
        
    @Test
    public void modifyDataPrependStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "key1=value1&username=username&password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "PREPEND", "key1=value1&")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);	
    }    
    
    @Test
    public void modifyDataDeleteJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"value3\"}}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "DELETE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataAddJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key4\":\"value4\",\"key3\":\"value3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("$.key2.key4", "ADD", "value4")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataUpdateJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"NEWvalue3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "UPDATE", "NEWvalue3")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataAppendJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"value3Append\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "APPEND", "Append")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataPrependJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"Prependvalue3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "PREPEND", "Prepend")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    @Test
    public void modifyDataReplaceJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"vaREPLACEe3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "lu->REPLACE")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	JSONAssert.assertEquals(expectedData,modifiedData,false);
    }
    
    
    @Test
    public void generateRequestNoAppURLTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "MYREQUEST";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.generateRequest(requestType, endPoint, data, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Rest host has not been set");
	}
    }
    
    @Test
    public void generateRequestInvalidRequestTypeTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "MYREQUEST";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.setRestHost("localhost");
	    commong.setRestPort("80");
	    commong.generateRequest(requestType, endPoint, data, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not valid: MYREQUEST");
	}
    }
    
    @Test
    public void generateRequestNotImplementedRequestTypeTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "TRACE";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.setRestHost("localhost");
	    commong.setRestPort("80");
	    commong.generateRequest(requestType, endPoint, data, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not implemented: TRACE");
	}
    }
    
    @Test
    public void generateRequestDataNullPUTTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "PUT";
	String endPoint = "endpoint";
	String type = "string";
	
	try {
	    commong.setRestHost("localhost");
	    commong.setRestPort("80");
	    commong.generateRequest(requestType, endPoint, null, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Missing fields in request.");
	}
    }
    
    @Test
    public void generateRequestDataNullPOSTTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "POST";
	String endPoint = "endpoint";
	String type = "string";
	
	try {
	    commong.setRestHost("localhost");
	    commong.setRestPort("80");
	    commong.generateRequest(requestType, endPoint, null, type);
	    fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Missing fields in request.");
	}
    }
}
