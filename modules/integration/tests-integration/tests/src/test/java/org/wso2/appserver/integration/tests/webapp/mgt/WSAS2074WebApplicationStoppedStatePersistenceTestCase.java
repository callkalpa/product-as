/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appserver.integration.tests.webapp.mgt;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.clients.WebAppAdminClient;
import org.wso2.appserver.integration.common.utils.ASIntegrationTest;
import org.wso2.appserver.integration.common.utils.WebAppDeploymentUtil;
import org.wso2.appserver.integration.common.utils.WebAppTypes;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests webapp stopped state persistence with server restarts and lazy loading
 */
public class WSAS2074WebApplicationStoppedStatePersistenceTestCase extends ASIntegrationTest {

    private TestUserMode userMode;
    private WebAppAdminClient webappAdminClient;
    private static final String webappFileName = "HelloWorldWebapp.war";
    private static final String webappName = "HelloWorldWebapp";
    private static final String hostname = "localhost";
    private ServerConfigurationManager serverConfigurationManager;

    @DataProvider
    protected static TestUserMode[][] userModeDataProvider() {
        return new TestUserMode[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "userModeDataProvider")
    public WSAS2074WebApplicationStoppedStatePersistenceTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init(userMode);
        serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("AS", TestUserMode.SUPER_TENANT_ADMIN));
        webappAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.as", description = "deploy web application")
    public void testDeployWebapp() throws Exception {
        Path webappFilePath = Paths.get(
                FrameworkPathUtil.getSystemResourceLocation(), "artifacts", "AS", "war", webappFileName);
        webappAdminClient.uploadWarFile(webappFilePath.toString());
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(
                backendURL, sessionCookie, webappName), webappName + " web application deployment failed.");
    }

    @Test(groups = "wso2.as", description = "stop web application", dependsOnMethods = {"testDeployWebapp"})
    public void testStopWebapp() throws IOException, XPathExpressionException {
        webappAdminClient.stopWebApp(webappFileName, hostname);
        assertFalse(isWebappAccessible(), webappName + " web application is not stopped.");
    }

    @Test(groups = "wso2.as", description = "check whether the web application is in stopped state after server restart",
            dependsOnMethods = {"testStopWebapp"})
    public void testWebappStoppedState() throws AutomationUtilException, XPathExpressionException, IOException {
        serverConfigurationManager.restartGracefully();
        sessionCookie = loginLogoutClient.login();
        assertFalse(isWebappAccessible(), webappName + " web application is accessible after server restart");
    }

    @Test(groups = "wso2.as", description = "start web application", dependsOnMethods = {"testWebappStoppedState"})
    public void testStartWebapp() throws AutomationUtilException, IOException, XPathExpressionException {
        sessionCookie = loginLogoutClient.login();
        webappAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
        webappAdminClient.startWebApp(webappFileName, hostname);
        assertTrue(isWebappAccessible(), webappName + " web application is not started.");
    }

    @Test(groups = "wso2.as", description = "check whether the web application is in started state after server restart",
            dependsOnMethods = {"testStartWebapp"})
    public void testWebappStartedState() throws AutomationUtilException, XPathExpressionException, IOException {
        serverConfigurationManager.restartGracefully();
//        sessionCookie = loginLogoutClient.login();
        assertTrue(isWebappAccessible(), webappName + " web application is not accessible after server restart");
    }

    private boolean isWebappAccessible() throws XPathExpressionException, IOException {
        String webAppURL = getWebAppURL(WebAppTypes.WEBAPPS) + "/" + webappName;
        String responseMessage = HttpRequestUtil.sendGetRequest(webAppURL, null).getData();
        return responseMessage.contains("<html><head><title>Hello World</title></head><body>Hello 1!</body></html>");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        sessionCookie = loginLogoutClient.login();
        if (WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, webappName)) {
            webappAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
            webappAdminClient.deleteWebAppFile(webappFileName, hostname);
            assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(
                    backendURL, sessionCookie, webappName), webappName + " web application undeployment failed.");
        }
    }

}
