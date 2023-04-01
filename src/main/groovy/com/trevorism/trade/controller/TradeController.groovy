package com.trevorism.trade.controller

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

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/trade")
class TradeController {

    private KrakenClient krakenClient = new DefaultKrakenClient()

    @Tag(name = "Trade Operations")
    @Operation(summary = "Get a list of open orders **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    List<Order> listOpenOrders(){
        return krakenClient.getOpenOrders(null)
    }

    @Tag(name = "Trade Operations")
    @Operation(summary = "Get the last 50 closed orders within the date range **Secure")
    @Post(value = "closed", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    List<Order> queryForClosedOrders(DateRange dateRange){
        return krakenClient.getClosedOrders(dateRange)
    }

    @Tag(name = "Trade Operations")
    @Operation(summary = "Create a market order **Secure")
    @Post(value = "market", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createMarketTradeOrder(MarketTrade marketTrade){
        return krakenClient.createOrder(marketTrade)
    }

    @Tag(name = "Trade Operations")
    @Operation(summary  = "Create a limit order **Secure")
    @Post(value = "limit", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createLimitTradeOrder(LimitTrade limitTrade){
        return krakenClient.createOrder(limitTrade)
    }

    @Tag(name = "Trade Operations")
    @Operation(summary = "Create a stop loss order **Secure")
    @Post(value = "stoploss", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    TradeResult createStopLossTradeOrder(StopLossTrade stopLossTrade){
        return krakenClient.createOrder(stopLossTrade)
    }

    @Tag(name = "Trade Operations")
    @Operation(summary = "Remove an order by transaction id **Secure")
    @Delete(value = "{transactionId}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    CancelOrderResult removeOrder(String transactionId){
        CancelOrderResult result = krakenClient.deleteOrder(transactionId)
        return result
    }
}
