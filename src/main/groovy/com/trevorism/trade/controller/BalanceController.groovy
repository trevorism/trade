package com.trevorism.trade.controller

import com.trevorism.trade.service.DefaultTradeService
import com.trevorism.trade.service.TradeService
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/balance")
class BalanceController {

    TradeService tradeService = new DefaultTradeService()

    @Tag(name = "Balance Operations")
    @Operation(summary = "Checks thresholds for certain pairs, invoked periodically by cloud scheduler")
    @Get(value = "check", produces = MediaType.APPLICATION_JSON)
    Map checkPairsAgainstThresholds() {
        tradeService.checkPairsAgainstThresholds()
    }

    @Tag(name = "Balance Operations")
    @Operation(summary = "Get the current price of an asset pair **Secure")
    @Get(value = "/check/{pairName}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Price getPrice(String pairName) {
        tradeService.getPrice(pairName)
    }

    @Tag(name = "Balance Operations")
    @Operation(summary = "Get the total account balance for each asset **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    Set<AssetBalance> getAccountBalance() {
        tradeService.getAccountBalance()
    }

    @Tag(name = "Balance Operations")
    @Operation(summary = "Get the current account value in terms of an asset name **Secure")
    @Get(value = "total/{assetName}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    double getTotal(String assetName) {
        tradeService.getTotal(assetName)
    }
}
