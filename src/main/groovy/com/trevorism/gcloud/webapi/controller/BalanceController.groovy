package com.trevorism.gcloud.webapi.controller

import com.google.gson.Gson
import com.trevorism.event.EventProducer
import com.trevorism.event.PingingEventProducer
import com.trevorism.gcloud.model.Alert
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import io.swagger.annotations.Api

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Api("Balance Operations")
@Path("/balance")
class BalanceController {

    @GET
    @Path("bitcoin")
    @Produces(MediaType.APPLICATION_JSON)
    Price getBitcoinPrice(){
        def client = new DefaultKrakenClient()
        client.getCurrentPrice("XBTUSD")
    }

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    boolean checkBitcoinPrice(){
        def client = new DefaultKrakenClient()
        Price price = client.getCurrentPrice("XBTUSD")

        HttpClient httpClient = new JsonHttpClient()
        String json = httpClient.get("http://threshold.datastore.trevorism.com/evaluation/bitcoin/${price.last}")
        Gson gson = new Gson()
        def result = gson.fromJson(json, List)

        if(result) {
            Alert alert = new Alert(subject: "Threshold Triggered", body: "The bitcoin price of ${price.last} triggered\n\n${result.toString()}")
            println alert.body
            EventProducer<Alert> producer = new PingingEventProducer<>()
            producer.sendEvent("alert", alert)
        }
        return result
    }


    @GET
    @Path("balance")
    @Produces(MediaType.APPLICATION_JSON)
    Set<AssetBalance> getAccountBalance(){
        def client = new DefaultKrakenClient()
        client.getAccountBalance()
    }
}
