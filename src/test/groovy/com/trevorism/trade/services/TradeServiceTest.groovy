package com.trevorism.trade.services

import com.trevorism.trade.service.DefaultTradeService
import com.trevorism.trade.service.TradeService
import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.error.KrakenRequestException
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.model.Threshold
import org.junit.jupiter.api.Test

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
        tradeService.thresholdClient = [list: {[new Threshold(name: "xrpusd")]}, evaluate: {pair, price, action -> true}, ping:{}] as ThresholdClient
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

    @Test
    void testGetTotalUsesAltnameForLegacyPrefixedAssets(){
        List<String> requestedPairs = []
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair -> requestedPairs << pair; new Price(last: 2) },
                                     getAccountBalances: { -> [new AssetBalance(assetName: "XETH", balance: 3),
                                                               new AssetBalance(assetName: "XXDG", balance: 5)] as Set }] as KrakenClient

        assert 16d == tradeService.getTotal("USD")
        assert requestedPairs.sort() == ["ETHUSD", "XDGUSD"]
    }

    @Test
    void testGetTotalTreatsZusdAsTargetCurrency(){
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair -> throw new KrakenRequestException("[EQuery:Unknown asset pair]") },
                                     getAccountBalances: { -> [new AssetBalance(assetName: "ZUSD", balance: 42)] as Set }] as KrakenClient

        assert 42d == tradeService.getTotal("usd")
    }

    @Test
    void testGetTotalStripsEarnAndHoldSuffixes(){
        List<String> requestedPairs = []
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair -> requestedPairs << pair; new Price(last: 10) },
                                     getAccountBalances: { -> [new AssetBalance(assetName: "SOL.S", balance: 1),
                                                               new AssetBalance(assetName: "HYPER.CORE", balance: 2)] as Set }] as KrakenClient

        assert 30d == tradeService.getTotal("USD")
        assert requestedPairs.sort() == ["HYPERUSD", "SOLUSD"]
    }

    @Test
    void testGetTotalDoesNotMutateAssetBalance(){
        AssetBalance assetBalance = new AssetBalance(assetName: "XBT.F", balance: 1)
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair -> new Price(last: 100) },
                                     getAccountBalances: { -> [assetBalance] as Set }] as KrakenClient

        tradeService.getTotal("USD")
        assert assetBalance.assetName == "XBT.F"
    }

    @Test
    void testGetTotalInvertsPairWhenOnlyReverseMarketExists(){
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair ->
                                        if (pair != "XBTUSD") {
                                            throw new KrakenRequestException("[EQuery:Unknown asset pair]")
                                        }
                                        new Price(last: 50000)
                                     },
                                     getAccountBalances: { -> [new AssetBalance(assetName: "ZUSD", balance: 100000)] as Set }] as KrakenClient

        assert 2d == tradeService.getTotal("BTC")
    }

    @Test
    void testGetTotalSkipsAssetWithNoMarketInEitherDirection(){
        TradeService tradeService = new DefaultTradeService()
        tradeService.krakenClient = [getCurrentPrice   : { pair ->
                                        if (pair.contains("REP")) {
                                            throw new KrakenRequestException("[EQuery:Unknown asset pair]")
                                        }
                                        new Price(last: 4)
                                     },
                                     getAccountBalances: { -> [new AssetBalance(assetName: "XREP", balance: 99),
                                                               new AssetBalance(assetName: "XETH", balance: 2)] as Set }] as KrakenClient

        assert 8d == tradeService.getTotal("USD")
    }

    @Test
    void testGetTotalWithoutAssetName(){
        TradeService tradeService = new DefaultTradeService()
        assert 0d == tradeService.getTotal(null)
        assert 0d == tradeService.getTotal("")
    }
}
