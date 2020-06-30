package com.trevorism.gcloud.webapi.controller

import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.impl.DefaultPrivateKrakenClient
import org.junit.Test

/**
 * @author tbrooks
 */
class RootControllerTest {

    @Test
    void testRootControllerEndpoints(){
        RootController rootController = new RootController()
        assert rootController.displayHelpLink().contains("/help")
    }

    @Test
    void testRootControllerPing(){
        RootController rootController = new RootController()
        assert rootController.ping() == "pong"
    }

    @Test
    void testGetPrice(){
        DefaultKrakenClient client = new DefaultKrakenClient()
        println client.getCurrentPrice("XBTUSD")
    }

    @Test
    void testGetBalances(){
        DefaultPrivateKrakenClient client = new DefaultPrivateKrakenClient()
        client.accountBalance.each {
            println it
        }
    }
}
