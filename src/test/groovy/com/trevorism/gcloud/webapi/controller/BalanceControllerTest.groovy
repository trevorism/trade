package com.trevorism.gcloud.webapi.controller

import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import org.junit.Test

class BalanceControllerTest {

    @Test
    void testGetPrice() {
        BalanceController balanceController = new BalanceController()
        balanceController.krakenClient = [getCurrentPrice: { pair -> new Price(last: 100)}] as KrakenClient

        def result = balanceController.getPrice("testUSD")
        assert result
        assert result.last == 100
    }

    @Test
    void testGetAccountBalance() {
        BalanceController balanceController = new BalanceController()
        balanceController.krakenClient = [getAccountBalances:{ -> [new AssetBalance(assetName: "testUSD", balance: 10)] as Set}] as KrakenClient
        def result = balanceController.getAccountBalance()
        assert result
        assert result[0].balance == 10

    }

    @Test
    void testDoubleValueOf(){

    }
}
