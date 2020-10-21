package com.trevorism.gcloud.services

import com.trevorism.gcloud.service.DefaultTradeService
import com.trevorism.gcloud.service.TradeService
import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.model.Threshold
import org.junit.Test

class TradeServiceTest {

    @Test
    void testGetPrice() {
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice: { pair -> new Price(last: 100)}] as KrakenClient

        def result = tradeService.getPrice("testUSD")
        assert result
        assert result.last == 100
    }

    @Test
    void testGetAccountBalance() {
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getAccountBalances:{ -> [new AssetBalance(assetName: "testUSD", balance: 10)] as Set}] as KrakenClient
        def result = tradeService.getAccountBalance()
        assert result
        assert result[0].balance == 10
    }

    @Test
    void testCheckPairsAgainstThresholds(){
        TradeService tradeService = new DefaultTradeService()
        tradeService.thresholdClient = [list: {[new Threshold(name: "xrpusd")]}, evaluate: {pair, price, action -> true}] as ThresholdClient
        tradeService.krakenClient = [getCurrentPrice: { pair -> new Price(last: 100)}] as KrakenClient

        def result = tradeService.checkPairsAgainstThresholds()
        assert result
        assert result["xrpusd"]
    }

    @Test
    void testGetTotal(){
        TradeService tradeService = new DefaultTradeService()
        tradeService.thresholdClient = [list: {[new Threshold(name: "xrpusd")]}, evaluate: {pair, price, action -> true}] as ThresholdClient
        tradeService.krakenClient = [getCurrentPrice: { pair -> new Price(last: 100)}, getAccountBalances:{ -> [new AssetBalance(assetName: "testUSD", balance: 10)] as Set}] as KrakenClient
        assert 1000d == tradeService.getTotal("USD")
    }
}
