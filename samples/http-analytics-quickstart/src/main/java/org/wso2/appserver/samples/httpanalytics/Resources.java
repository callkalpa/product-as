/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class is used to generate the random values for the different fields required.
 */
public class Resources {

    // todo: add more data to each section

    // This map holds the application name and the list of Request URIs for that application
    private static Map<String, List<String>> applications = new HashMap<>();
    private static final String[] HTTP_RESPONSE_CODES = {"200", "404", "200", "500", "200", "200", "200", "200"};

    // This array holds the IP address range assigned for ISPs
    private static final String[] CLIENT_ADDRESSES = {
            "40.112.0.0:40.119.255.255",
            "40.144.0.0:40.159.255.255",
            "41.61.0.0:41.61.255.255 "
    };

    // This array holds the list of referrers
    private static final String[] REFERERS = {
            "google.com",
            "facebook.com",
            "twitter.com"
    };

    // This array holds the list of user agent headers
    private static final String[] USERAGENTS = {
            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/47.0.2526.83 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/47.0.2526.83 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 4.4.3; KFTHWI Build/KTU84M) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Silk/47.1.79 like Chrome/47.0.2526.80 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 " +
                    "Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/601.3.9 (KHTML, like Gecko) Version/9.0.2 " +
                    "Safari/601.3.9",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 " +
                    "Safari/537.36"
    };

    static final String[] LANGUAGES = {
            "en", "si", "fr", "en", "en", "ja"
    };


    static {
        populateApplications();
    }

    public static String getApplication() {
        return getRandomValueFromArray(applications.keySet().toArray(new String[applications.size()]));
    }

    public static String getRequestURI(String applicationName) {
        return getRandomValueFromArray(applications.get(applicationName).toArray(new String[applications.size()]));
    }

    public static String getHTTPResponseCode() {
        return getRandomValueFromArray(HTTP_RESPONSE_CODES);
    }

    public static String getClientIP() {
        String[] ipRange = getRandomValueFromArray(CLIENT_ADDRESSES).split(":");
        return generateRandomIPFromRange(ipRange[0], ipRange[1]);
    }

    public static String getReferrer() {
        return getRandomValueFromArray(REFERERS);
    }

    public static String getUseragent() {
        return getRandomValueFromArray(USERAGENTS);
    }

    public static String getLanguage() {
        return getRandomValueFromArray(LANGUAGES);
    }

    private static String generateRandomIPFromRange(String startIP, String endIP) {
        String[] start = startIP.split("\\.");
        String[] end = endIP.split("\\.");
        String[] newIp = new String[4];
        Random random = new Random();
        boolean sectionChanged = false;
        int diff = 0;
        for (int i = 0; i < 4; i++) {
            if (sectionChanged) {
                newIp[i] = String.valueOf(random.nextInt(256));
                continue;
            }

            diff = Integer.parseInt(end[i]) - Integer.parseInt(start[i]);
            if (diff <= 0) {
                newIp[i] = String.valueOf(Integer.parseInt(start[i]));
                continue;
            }

            newIp[i] = String.valueOf((random.nextInt(diff) + Integer.parseInt(start[i])));
            sectionChanged = true;
        }

        return String.join(".", newIp);
    }

    private static String getRandomValueFromArray(String[] array) {
        return array[new Random().nextInt(array.length)];
    }


    private static void populateApplications() {
        // populate applications
        applications.put("examples", Arrays.asList(
                "servlets/servlet/HelloWorldExample",
                "servlets/servlet/RequestInfoExample",
                "servlets/servlet/RequestHeaderExample",
                "servlets/servlet/RequestParamExample",
                "servlets/servlet/CookieExample",
                "servlets/servlet/SessionExample",
                "jsp/jsp2/el/basic-arithmetic.jsp"
        ));
        applications.put("musicstore-app", Arrays.asList(
                "artists",
                "albums",
                "albums/2016"
        ));
        applications.put("bookstore-app", Arrays.asList(
                "authors",
                "top/2016",
                "top/2015"
        ));
    }
}
