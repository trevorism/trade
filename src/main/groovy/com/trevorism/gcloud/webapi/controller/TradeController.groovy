package com.trevorism.gcloud.webapi.controller

import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.DateRange
import com.trevorism.kraken.model.Order
import com.trevorism.kraken.model.trade.CancelOrderResult
import com.trevorism.kraken.model.trade.LimitTrade
import com.trevorism.kraken.model.trade.MarketTrade
import com.trevorism.kraken.model.trade.StopLossTrade
import com.trevorism.kraken.model.trade.TradeResult
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Api("Trade Operations")
@Path("/trade")
class TradeController {

    private KrakenClient krakenClient = new DefaultKrakenClient()

    @ApiOperation(value = "Get a list of open orders **Secure")
    @GET
    @Secure(Roles.SYSTEM)
    @Produces(MediaType.APPLICATION_JSON)
    List<Order> listOpenOrders(){
        return krakenClient.getOpenOrders(null)
    }

    @ApiOperation(value = "Get the last 50 closed orders within the date range **Secure")
    @POST
    @Path("closed")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    List<Order> queryForClosedOrders(DateRange dateRange){
        return krakenClient.getClosedOrders(dateRange)
    }

    @ApiOperation(value = "Create a market order **Secure")
    @POST
    @Path("market")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createMarketTradeOrder(MarketTrade marketTrade){
        return krakenClient.createOrder(marketTrade)
    }

    @ApiOperation(value = "Create a limit order **Secure")
    @POST
    @Path("limit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createLimitTradeOrder(LimitTrade limitTrade){
        return krakenClient.createOrder(limitTrade)
    }

    @ApiOperation(value = "Create a stop loss order **Secure")
    @POST
    @Path("stoploss")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createStopLossTradeOrder(StopLossTrade stopLossTrade){
        return krakenClient.createOrder(stopLossTrade)
    }

    @ApiOperation(value = "Remove an order by transaction id **Secure")
    @DELETE
    @Path("{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    CancelOrderResult removeOrder(@PathParam("transactionId") String transactionId){
        CancelOrderResult result = krakenClient.deleteOrder(transactionId)
        return result
    }
}
