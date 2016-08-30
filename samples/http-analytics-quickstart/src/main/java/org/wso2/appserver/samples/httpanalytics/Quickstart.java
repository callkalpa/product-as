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

package org.wso2.appserver.samples.httpanalytics;


import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;


/**
 * This class generates sample events, with a random interval, for the past 30 days and publishes them to
 * http-analytics.
 */
public class Quickstart {

    private static final Log log;

    private static final String STREAM_NAME = "org.wso2.http.stats";
    private static final String STREAM_VERSION = "1.0.0";
    private static final int THRIFT_PORT = 7611;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    // maximum possible interval between events in seconds
    private static final int MAX_INTERVAL_BETWEEN_EVENTS = 3000;

    private static String hostname;

    static {
        System.setProperty("org.apache.juli.formatter", "org.apache.juli.VerbatimFormatter");
        log = LogFactory.getLog(Quickstart.class);
    }

    public static void main(String[] args) throws DataEndpointConfigurationException, DataEndpointException,
            UnknownHostException, InterruptedException, URISyntaxException, DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException {
        new Quickstart().runSample();
    }

    private void runSample() throws URISyntaxException, UnknownHostException, DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException, DataEndpointException,
            DataEndpointConfigurationException, InterruptedException {
        log.info("Initializing data publishing");

        System.setProperty("javax.net.ssl.trustStore", getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        AgentHolder.setConfigPath(getDataAgentConfigPath());

        hostname = InetAddress.getLocalHost().getHostName();
        String thriftUrl = "tcp://" + hostname + ":" + THRIFT_PORT;

        DataPublisher dataPublisher = new DataPublisher(thriftUrl, USERNAME, PASSWORD);
        String streamId = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, STREAM_VERSION);

        log.info("Starting data publishing");
        publishEvents(dataPublisher, streamId);

        log.info("Stopping data publishing");
        Thread.sleep(5000);
        dataPublisher.shutdown();
        log.info("Data publishing stopped");

        publishToHTTPAnalytics(dataPublisher, streamId, System.currentTimeMillis());

        log.info("You can access the HTTP analytics dashboard via https://" + hostname + ":9443/portal/");
    }

    private void publishEvents(DataPublisher dataPublisher, String streamId) {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        long timeFrom = calendar.getTimeInMillis();

        Random random = new Random();

        LongStream.iterate(timeFrom, time -> time + random.nextInt(MAX_INTERVAL_BETWEEN_EVENTS) * 1000)
                .distinct()
                .limit(100000)
                .filter(e -> e < currentTime)
                .forEach(e -> publishToHTTPAnalytics(dataPublisher, streamId, e));

        log.info(currentTime);
//                .forEach(e -> publishToHTTPAnalytics(dataPublisher, streamId, e));
    }

    private void publishToHTTPAnalytics(DataPublisher dataPublisher, String streamId, long timestamp) {
        Event event = new Event(streamId, timestamp, new Object[]{hostname, hostname}, null, getPayloadData(timestamp));
        dataPublisher.publish(event);
    }

    private Object[] getPayloadData(long timestamp) {
        List<Object> payload = new ArrayList<>();

        String applicationName = Resources.getApplication();

        payload.add(applicationName);
        payload.add("1.0.0");
        payload.add("admin");
        payload.add(Resources.getRequestURI(applicationName));
        payload.add(timestamp);
        payload.add("");
        payload.add("webapp");
        payload.add(applicationName);
        payload.add("-");
        payload.add("GET");
        payload.add("");
        payload.add("text/html;charset=UTF-8");
        payload.add(Long.parseLong(Resources.getHTTPResponseCode()));
        payload.add(Resources.getClientIP());
        payload.add(Resources.getReferrer());
        payload.add(Resources.getUseragent());
        payload.add(hostname + ":8080");
        payload.add("");
        payload.add("");
        payload.add((long) (new Random().nextInt(300)));
        payload.add((long) -1);
        payload.add((long) -1);
        payload.add("");
        payload.add("");
        payload.add(Resources.getLanguage());

        return payload.toArray();

    }

    private String getTrustStorePath() {
        return Paths.get("..", "..", "conf", "wso2", "client-truststore.jks").toString();
    }

    private String getDataAgentConfigPath() throws URISyntaxException {
        return Paths.get("..", "..", "conf", "wso2", "data-agent-conf.xml").toString();
    }

}
