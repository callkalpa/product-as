/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.appserver.test.integration.statisticspublishing;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appserver.test.integration.TestBase;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * This class contains integration test related to HTTP statistics publishing.
 */
public class StatisticsPublisherTestIT extends TestBase {
    private ThriftTestServer thriftTestServer;

    @BeforeClass
    public void init() throws Exception {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        thriftTestServer = getThriftTestServer();
    }

    @Test(description = "Test whether the thrift server is started.")
    public void testThriftServerStarted() throws Exception {
        Assert.assertTrue(thriftTestServer.isServerStarted(), "Thrift server is not started");
    }

    @Test(description = "Test whether the stream definition is added properly.",
            dependsOnMethods = {"testThriftServerStarted"})
    public void testAddingStreamDefinition() throws Exception {
        thriftTestServer.addStreamDefinition(getStreamDefinition());
        Assert.assertNotNull(thriftTestServer.getStreamDefinition(StatisticsPublisherConstants.STREAM_DEFINITION_NAME,
                StatisticsPublisherConstants.STREAM_DEFINITION_VERSION),
                "Stream definition is not added.");
    }

    /**
     * This test invokes a sample webapp in application server and verify the event being published to the
     * thrift endpoint.
     */
    @Test(description = "Test whether the event is published to the thrift server.",
            dependsOnMethods = {"testAddingStreamDefinition"})
    public void testEventPublishing() throws IOException {
        URL endpoint = new URL(getBaseUrl() + "/examples/servlets/servlet/HelloWorldExample");
        java.net.HttpURLConnection urlConnection = (java.net.HttpURLConnection) endpoint.openConnection();
        urlConnection.setRequestMethod("GET");

        urlConnection.setRequestProperty("User-Agent", StatisticsPublisherConstants.USER_AGENT);

        int responseCode = urlConnection.getResponseCode();
        Assert.assertEquals(responseCode, 200, "Couldn't invoke the sample webapp [" + endpoint + "]");


    }

    @AfterClass
    public void destroy() {
        thriftTestServer.stop();
    }

    /**
     * This method is used to convert the stream definition to a string.
     *
     * @return string representation of the stream definition
     * @throws IOException
     * @throws ParseException
     */
    private String getStreamDefinition() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(DataPublisherTestUtil.getStreamDefinitionPath()));
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject.toJSONString();
    }

}
