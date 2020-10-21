package com.trevorism.gcloud.webapi.controller

import com.trevorism.gcloud.service.DefaultTradeService
import com.trevorism.gcloud.service.TradeService
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Api("Balance Operations")
@Path("/balance")
class BalanceController {

    TradeService tradeService = new DefaultTradeService()

    @ApiOperation(value = "Checks thresholds for certain pairs, invoked periodically buy cloud scheduler")
    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    Map checkPairsAgainstThresholds() {
        tradeService.checkPairsAgainstThresholds()
    }

    @ApiOperation(value = "Get the current price of an asset pair **Secure")
    @GET
    @Path("check/{pairName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Price getPrice(@PathParam("pairName") String pairName) {
        tradeService.getPrice(pairName)
    }

    @ApiOperation(value = "Get the total account balance for each asset **Secure")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Set<AssetBalance> getAccountBalance() {
        tradeService.getAccountBalance()
    }

    @ApiOperation(value = "Get the current price of an asset pair **Secure")
    @GET
    @Path("total/{assetName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    double getTotal(@PathParam("assetName") String assetName) {
        tradeService.getTotal(assetName)
    }
}
