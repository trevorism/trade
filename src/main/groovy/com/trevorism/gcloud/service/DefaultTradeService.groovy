package com.trevorism.gcloud.service

import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.FastThresholdClient
import com.trevorism.threshold.PingingThresholdClient
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.model.Threshold
import com.trevorism.threshold.strategy.AlertWhenThresholdMet

class DefaultTradeService implements TradeService {

    private ThresholdClient thresholdClient = new FastThresholdClient()
    private KrakenClient krakenClient = new DefaultKrakenClient()

    @Override
    double getTotal(String assetName) {
        double total = 0
        if(!assetName){
            return 0
        }

        getAccountBalance().each {
            double assetPrice = 1
            if(isUSD(it, assetName)){
                total += it.balance
                return
            }
            if(isUSDXBT(it, assetName)){
                Price price = getPrice("xbtusd")
                total += it.balance / price.last
                return
            }
            assetPrice = getAssetPrice(assetName, it, assetPrice)
            total += (assetPrice * it.balance)
        }
        return total
    }

    private boolean isUSDXBT(AssetBalance it, String assetName) {
        it.assetName.toUpperCase() == "USD" && assetName.toUpperCase() == "XBT"
    }

    private boolean isUSD(AssetBalance it, String assetName) {
        it.assetName.toUpperCase() == "USD" && assetName.toUpperCase() == "USD"
    }

    private double getAssetPrice(String assetName, AssetBalance it, double assetPrice) {
        if (assetName.toLowerCase() != it.assetName.toLowerCase()) {
            assetPrice = getPrice("${it.assetName}${assetName}").last
        }
        assetPrice
    }

    @Override
    Set<AssetBalance> getAccountBalance() {
        krakenClient.getAccountBalances()
    }

    @Override
    Price getPrice(String pairName) {
        krakenClient.getCurrentPrice(pairName)
    }

    @Override
    Map<String, Boolean> checkPairsAgainstThresholds() {
        new PingingThresholdClient().ping()
        def result = [:]

        def pairs = thresholdClient.list().findAll{it.name.toUpperCase().contains("USD") || it.name.toUpperCase().contains("XBT")}
        return computeThresholdResponse(pairs, result)
    }

    private LinkedHashMap<Object, Object> computeThresholdResponse(List<Threshold> pairs, Map<String, Boolean> result) {
        pairs?.each { threshold ->
            String pairName = threshold.name
            Price price = getPrice(pairName)
            def thresholdResponse = thresholdClient.evaluate(pairName, price.last, new AlertWhenThresholdMet())
            result.put(pairName, thresholdResponse)
        }
        return result
    }
}
