/**
 * Copyright 2020 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package integration;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.testcontainers.containers.DockerComposeContainer;

import integration.util.PluginTestHelper;

public class PluginIntegrationTest {
    
    @ClassRule
    public static DockerComposeContainer docker =
        new DockerComposeContainer(new File("build/resources/test/docker/docker-compose.yml"))
            .withLocalCompose(true);

    @BeforeClass
    public static void initialize() throws Exception {
        PluginTestHelper.initializeXLR();
    }

    // Tests

    @Test
    public void testTemplateKeyFolderKeyAutoStart() throws Exception {
        System.out.println("start testTemplateKeyFolderKeyAutoStart");

        JSONObject theResult = PluginTestHelper.getReleaseResult("templateKeyFolderKey", true, true);
        //System.out.println("Test Result:\n"+theResult);

        assertTrue(theResult != null);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/templateKeyFolderKey.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testTemplateKeyFolderKeyAutoStart passed");

    }

    @Test
    public void testCreateOnly() throws Exception {
        System.out.println("start testCreateOnly");
        
        JSONObject theResult = PluginTestHelper.getReleaseResult("createOnly", false, true);
        //System.out.println("Test Result:\n"+theResult);

        assertTrue(theResult != null);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/createOnly.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testCreateOnly passed");

    }

    @Test
    public void testTemplateIdFolderId() throws Exception {
        System.out.println("start testTemplateFolderId");

        JSONObject theResult = PluginTestHelper.getReleaseResult("templateIdFolderId", true, true);
        //System.out.println("Test Result:\n"+theResult);

        assertTrue(theResult != null);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/templateIdFolderId.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testTemplateIdFolderId passed");

    }

    @Test
    public void testTemplateTitleNoFolder() throws Exception {
        System.out.println("start testTemplateTitleNoFolder");
        
        JSONObject theResult = PluginTestHelper.getReleaseResult("templateTitleNoFolder", true, true);
        //System.out.println("Test Result:\n"+theResult);

        assertTrue(theResult != null);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/templateTitleNoFolder.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testTemplateTitleNoFolder passed");

    }

    @Test
    public void testVariables() throws Exception {
        System.out.println("start testVariables");
        
        JSONObject theResult = PluginTestHelper.getReleaseResult("variables", true, true);
        //System.out.println("Test Result:\n"+theResult);

        assertTrue(theResult != null);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/variables.json"));
        System.out.println("Expected Result:\n"+expected);
        System.out.println("NOTE: we do not expect xlr to return the password variable - security feature");
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testVariables passed");

    }

    @Test
    public void testOtherNonRequiredAttributes() throws Exception {
        System.out.println("start testOtherNonRequiredAttributes");
        
        JSONObject theResult = PluginTestHelper.getReleaseResult("otherNonRequiredAttributes", true, false);
        
        assertTrue(theResult != null);

        // We will now extract from the result those attributes we wish to compare
        JSONObject filteredResult = new JSONObject();
        filteredResult.put("owner", theResult.getString("owner"));
        filteredResult.put("description", theResult.getString("description"));
        filteredResult.put("dueDate", theResult.getString("dueDate"));
        filteredResult.put("tags", theResult.getJSONArray("tags"));

        System.out.println("\nFiltered Result:");
        String filteredResultStr = filteredResult.toString();
        System.out.println(filteredResultStr);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/otherNonRequiredAttributes.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, filteredResultStr, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testOtherNonRequiredAttributes passed");

    }

    @Test
    public void testScheduledAutoStart() throws Exception {
        System.out.println("start testScheduledAutoStart");
        
        JSONObject theResult = PluginTestHelper.getReleaseResult("scheduledAutoStart", false, false);
        
        assertTrue(theResult != null);

        // We will now extract from the result those attributes we wish to compare
        JSONObject filteredResult = new JSONObject();
        filteredResult.put("scheduledStartDate", theResult.getString("scheduledStartDate"));
        filteredResult.put("autoStart", theResult.getBoolean("autoStart"));
        

        System.out.println("\nFiltered Result:");
        String filteredResultStr = filteredResult.toString();
        System.out.println(filteredResultStr);

        // The file contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = PluginTestHelper.readFile(PluginTestHelper.getResourceFilePath("testExpected/scheduledAutoStart.json"));
        System.out.println("Expected Result:\n"+expected);
        try {
            // This will assert that variables have been set to the expected values. Order does not matter.
            JSONAssert.assertEquals(expected, filteredResultStr, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("testScheduledAutoStart passed");

    }

}

