/**
 * Copyright 2020 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package integration.util;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public final class PluginTestHelper {

    private static final String BASE_URI = "http://localhost:15516/api/v1";
    private static RequestSpecification httpRequest = null;
    private static final String IMPORT_GLOBAL = "/config/Configuration/variables/global";
    private static final String IMPORT_PARENT_FOLDER = "/folders/Applications";
    private static final String IMPORT_CHILD_FOLDER = "/folders/Applications/Folder1ee657ae74f94d7e87ec581558452e00";
    private static final String IMPORT_TEMPLATE = "/templates/import";
    private static final String RELEASE_PREFIX = "/releases/";
    private static final String START_RELEASE_SUFFIX = "/start";
    private static final String GET_VARIABLES_SUFFIX = "/variableValues";
    private static final String RELEASE_TRIGGER_START = "http://localhost:15516/api/extension/releasetrigger/start";
    private static final String RELEASE_TRIGGER_CREATE = "http://localhost:15516/api/extension/releasetrigger/create";

    private PluginTestHelper() {
        /*
         * Private Constructor will prevent the instantiation of this class directly
         */
    }

    static {
        baseURI = BASE_URI;
        // Configure authentication
        httpRequest = given().auth().preemptive().basic("admin", "admin");
    }

    public static void initializeXLR() throws Exception{
        System.out.println("Pausing for 1.5 minutes, waiting for XLR to start. ");
        Thread.sleep(90000);

        // Load globalVar configuration
       
        try {

            ////////// Load parent folder
            JSONObject requestParams = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/releaseFolder-config.json"));
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParams.toJSONString());
            
            // Post parent folder
            Response response = httpRequest.post(IMPORT_PARENT_FOLDER);
            if (response.getStatusCode() != 200) {
                System.out.println("Status line, import globalVar was " + response.getStatusLine() + "");
            } else {
                //String responseId = response.jsonPath().getString("id");
            }

            ////////// Load first child folder
            requestParams = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/teamOneFolder-config.json"));
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParams.toJSONString());
            
            // Post post first child folder
            response = httpRequest.post(IMPORT_CHILD_FOLDER);
            if (response.getStatusCode() != 200) {
                System.out.println("Status line, import Team One Folder was " + response.getStatusLine() + "");
            } else {
                //String responseId = response.jsonPath().getString("id");
            }

            ////////// Load second child folder
            requestParams = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/teamTwoFolder-config.json"));
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParams.toJSONString());
            
            // Post second child folder
            response = httpRequest.post(IMPORT_CHILD_FOLDER);
            if (response.getStatusCode() != 200) {
                System.out.println("Status line, import Team Two Folder was " + response.getStatusLine() + "");
            } else {
                //String responseId = response.jsonPath().getString("id");
            }


            ////////// Load globalVar
            requestParams = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/globalVar-config.json"));
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParams.toJSONString());
            
            // Post globalVar config
            response = httpRequest.post(IMPORT_GLOBAL);
            if (response.getStatusCode() != 200) {
                System.out.println("Status line, import globalVar was " + response.getStatusLine() + "");
            } else {
                //String responseId = response.jsonPath().getString("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // MultiPart requests cannot be reused so we will create new individual response objects instead
            // Load the Team Two template
            JSONObject requestParams = new JSONObject();
            Response templateOneResponse = given().auth().preemptive().basic("admin", "admin")
            .header("Content-Type", "multipart/form-data")
            .header("Accept", "application/json")
            .body(requestParams.toJSONString())
            .multiPart(new File(getResourceFilePath("docker/initialize/data/release-template-TeamOne.json")))
            .post(BASE_URI+IMPORT_TEMPLATE);


            ///////// retrieve the response results.
            if (templateOneResponse.getStatusCode() != 200) {
                System.out.println("Status line, import template Team One was " + templateOneResponse.getStatusLine() );
            } else {
                System.out.println("Import Template One successful, id = " + templateOneResponse.jsonPath().getString("id"));
            }

            // Load the Team One template
            requestParams = new JSONObject();
            Response templateTwoResponse = given().auth().preemptive().basic("admin", "admin")
            .header("Content-Type", "multipart/form-data")
            .header("Accept", "application/json")
            .body(requestParams.toJSONString())
            .multiPart(new File(getResourceFilePath("docker/initialize/data/release-template-TeamTwo.json")))
            .post(BASE_URI+IMPORT_TEMPLATE);

            ///////// retrieve the response results.
            if (templateTwoResponse.getStatusCode() != 200) {
                System.out.println("Status line, import template Team Two was " + templateTwoResponse.getStatusLine() );
            } else {
                System.out.println("Import Template Two successful, id = " + templateTwoResponse.jsonPath().getString("id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    
    }

    public static org.json.JSONObject getReleaseResult(String fileNamePrefix, boolean shouldStart, boolean getVariablesOnly) throws Exception{
        org.json.JSONObject releaseResultJSON = null;
        String responseId = "";
        String releaseResultStr = "";

        String postUrl = shouldStart ? RELEASE_TRIGGER_START : RELEASE_TRIGGER_CREATE;

        // POST the release trigger
        JSONObject requestParams = getRequestParamsFromFile(getResourceFilePath("docker/initialize/testPosts/"+fileNamePrefix+".json"));
        
        // The release trigger has a different base uri so we cannot use our static httpRequest
        Response response = given().auth().preemptive().basic("admin", "admin")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(requestParams.toJSONString())
            .post(postUrl);

        if (response.getStatusCode() != 201) {
            System.out.println("Status line, Create Release Trigger was " + response.getStatusLine() );
        } else {
            // Note: unlike the standard XLR response to a 'start release' REST call, where the release id is returned in the json property 'id', the release trigger
            //       uses the name 'entity.release_id'
            //responseId = response.jsonPath().getString("id");
            responseId = response.jsonPath().getString("entity.release_id");
            System.out.println("Trigger release was successful, id = "+ responseId);
        }

        ///////// Get Archived responses
        // Sleep so XLR can finish processing releases
        System.out.println("Pausing for 15 seconds, waiting for release to complete. If most requests fail with 404, consider sleeping longer.");
        Thread.sleep(15000);
        //////////

        // Do we need the variables object or the release object?
        String theURL = getVariablesOnly ? (BASE_URI + RELEASE_PREFIX + responseId + GET_VARIABLES_SUFFIX) : (BASE_URI + RELEASE_PREFIX + responseId);
        response = given().auth().preemptive().basic("admin", "admin")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .body(requestParams.toJSONString())
        .get(theURL);

        if (response.getStatusCode() != 200) {
            System.out.println("Status line for get "+ (getVariablesOnly ? "variables": "release") +" was " + response.getStatusLine() + "");
        } else {
            System.out.println("\nTest Result:");
            releaseResultStr = response.jsonPath().prettyPrint();
            try {
                releaseResultJSON =  new org.json.JSONObject(releaseResultStr);
            } catch (Exception e) {
                System.out.println("FAILED: EXCEPTION: "+e.getMessage());
                e.printStackTrace();
                throw e;
            }        
        }
        return releaseResultJSON;
    }


    /////////////////// Util methods

    public static String getResourceFilePath(String filePath){ 
        // Working with resource files instead of the file system - OS Agnostic 
        //System.out.println("Requested file path = "+filePath);
        String resourcePath = "";
        ClassLoader classLoader = PluginTestHelper.class.getClassLoader();
        try {
            resourcePath = new File (classLoader.getResource(filePath).toURI()).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("resourcePath = " + resourcePath);
        return resourcePath;
    }

    public static String readFile(String path) throws IOException {
        StringBuilder result = new StringBuilder("");

        File file = new File(path);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return result.toString();
    }

    public static JSONObject getRequestParamsFromFile(String filePath) throws Exception{
        JSONObject requestParams = new JSONObject();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(filePath))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            requestParams = (JSONObject) obj;
            //System.out.println(requestParams);
 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return requestParams;
    }

    public static JSONObject getRequestParams() {
        // must use intermediate parameterized HashMap to avoid warnings
        HashMap<String,Object> params = new HashMap<String,Object>();
        
        params.put("releaseTitle", "release from api");
        JSONObject requestParams = new JSONObject(params);
        return requestParams;
    }

}