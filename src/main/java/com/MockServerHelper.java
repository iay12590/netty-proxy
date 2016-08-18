/*
package com;

import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

*/
/**
 * Created by jerry on 2016/8/17.
 *//*

public class MockServerHelper {
    private static ClientAndProxy proxy;
    private static ClientAndServer mockServer;


    static void startMockServer() {
        mockServer = startClientAndServer(8080);
        proxy = startClientAndProxy(8088);
    }

    static void shutdownMockServer() {
        proxy.stop();
        mockServer.stop();
    }

    public static ClientAndProxy getProxy() {
        if(proxy == null)throw new NullPointerException("proxy not created");
        return proxy;
    }

    public static ClientAndServer getMockServer() {
        if(mockServer == null)throw new NullPointerException("mockServer not created");
        return mockServer;
    }
}
*/
