package com.trevorism.gcloud.webapi.controller

import com.google.gson.Gson
import com.trevorism.event.EventProducer
import com.trevorism.event.PingingEventProducer
import com.trevorism.gcloud.model.Alert
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.FastThresholdClient
import com.trevorism.threshold.PingingThresholdClient
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.strategy.AlertWhenThresholdMet
import io.swagger.annotations.Api

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Api("Balance Operations")
@Path("/balance")
class BalanceController {

    private ThresholdClient thresholdClient = new FastThresholdClient()
    private KrakenClient krakenClient = new DefaultKrakenClient()

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    Map checkPairsAgainstThresholds(){
        new PingingThresholdClient().ping()

        def pairs = ["XRPUSD", "XBTUSD", "SCXBT", "ADAXBT", "WAVESXBT", "ICXXBT", "BCHXBT", "ALGOXBT"]
        def result = [:]
        pairs.each { pairName ->
            Price price = getPrice(pairName)
            def thresholdResponse = thresholdClient.evaluate(pairName, price.last, new AlertWhenThresholdMet())
            result.put(pairName, thresholdResponse)
        }
        return result
    }

    @GET
    @Path("check/{pairName}")
    @Produces(MediaType.APPLICATION_JSON)
    Price getPrice(@PathParam("pairName") String pairName){
        Price price = krakenClient.getCurrentPrice(pairName)
        return price
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Set<AssetBalance> getAccountBalance(){
        krakenClient.getAccountBalance()
    }
}
