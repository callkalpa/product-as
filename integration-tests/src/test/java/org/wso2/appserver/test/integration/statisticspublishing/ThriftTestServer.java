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

import org.apache.log4j.Logger;
import org.wso2.appserver.test.integration.TestUtils;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thrift server and related utils for testing purpose.
 */
public class ThriftTestServer {

    private static final int SERVER_STARTUP_TIMEOUT = 20;
    private static final String HOST = "localhost";
    private static final String TENANT_DOMAIN = "admin";
    private static final int TENANT_ID = -1234;

    private Logger log = Logger.getLogger(ThriftTestServer.class);
    private ThriftDataReceiver thriftDataReceiver;
    private InMemoryStreamDefinitionStore streamDefinitionStore;
    private AtomicInteger numberOfEventsReceived;
    private int thriftPort;
    private List<Event> events;

    public ThriftTestServer(int thriftPort) {
        this.thriftPort = thriftPort;
    }

    /**
     * Starts the thrift data receiver.
     *
     * @throws DataBridgeException
     */
    public void start() throws DataBridgeException {
        DataPublisherTestUtil.setKeyStoreParams();
        streamDefinitionStore = getStreamDefinitionStore();
        numberOfEventsReceived = new AtomicInteger(0);
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName, String password) {
                return true;    // always authenticate to true
            }

            @Override
            public String getTenantDomain(String userName) {
                return TENANT_DOMAIN;
            }

            @Override
            public int getTenantId(String tenantDomain) throws UserStoreException {
                return TENANT_ID;
            }

            @Override
            public void initContext(AgentSession agentSession) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }
        }, streamDefinitionStore, DataPublisherTestUtil.getDataBridgeConfigPath());

        thriftDataReceiver = new ThriftDataReceiver(thriftPort, databridge);

        databridge.subscribe(new AgentCallback() {

            @Override
            public void definedStream(StreamDefinition streamDefinition, int tenantId) {
                log.info("Stream definition added: " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantId) {
                log.info("Stream definition removed: " + streamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                //numberOfEventsReceived.addAndGet(eventList.size());
                events = eventList;
                log.info("Number of events received: " + numberOfEventsReceived);
            }

        });

        log.info("Test Server starting on " + HOST);
        thriftDataReceiver.start(HOST);
        log.info("Test Server Started");
    }

    /**
     * Return the stream definition for the given name and version.
     *
     * @param name    name of the stream definition
     * @param version version of the stream definition
     * @return {@link StreamDefinition} matching the name and the version
     * @throws StreamDefinitionStoreException
     */
    public StreamDefinition getStreamDefinition(String name, String version) throws StreamDefinitionStoreException {
        return getStreamDefinitionStore().getStreamDefinitionFromStore(name, version, TENANT_ID);
    }

    /**
     * Adds stream definition to the stream definition store.
     *
     * @param streamDefinition stirng representation of the stream definition
     * @throws MalformedStreamDefinitionException
     * @throws StreamDefinitionStoreException
     */
    public void addStreamDefinition(String streamDefinition) throws MalformedStreamDefinitionException,
            StreamDefinitionStoreException {
        getStreamDefinitionStore().saveStreamDefinitionToStore(
                EventDefinitionConverterUtils.convertFromJson(streamDefinition), TENANT_ID);
    }

    /**
     * Returns the stream definition store.
     *
     * @return in memory definition store
     */
    private InMemoryStreamDefinitionStore getStreamDefinitionStore() {
        if (streamDefinitionStore == null) {
            streamDefinitionStore = new InMemoryStreamDefinitionStore();
        }
        return streamDefinitionStore;
    }

    /**
     * Returns the number of events received by the data receiver.
     *
     * @return number of events received, if none 0
     */
    public int getNumberOfEventsReceived() {
        if (numberOfEventsReceived != null) {
            return numberOfEventsReceived.get();
        } else {
            return 0;
        }
    }

    /**
     * stops the thrift data receiver.
     */
    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
    }

    /**
     * Returns whether the thrift server is started or not.
     *
     * @return true if the server is started, else false
     * @throws InterruptedException
     */
    public boolean isServerStarted() throws InterruptedException {
        int count = 0;
        while (count < SERVER_STARTUP_TIMEOUT) {
            if (TestUtils.isServerListening(HOST, thriftPort)) {
                return true;
            }
            Thread.sleep(1000);
            count++;
        }

        log.info("Thrift server start timed out.");

        return false;
    }

    public List<Event> getEvents() {
        return events;
    }

}
