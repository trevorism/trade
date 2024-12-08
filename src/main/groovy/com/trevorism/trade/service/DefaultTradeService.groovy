package com.trevorism.trade.service

import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.FastThresholdClient
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.model.Threshold
import com.trevorism.threshold.strategy.AlertWhenThresholdMet

class DefaultTradeService implements TradeService {

    private SecureHttpClient httpClient = new AppClientSecureHttpClient()
    private ThresholdClient thresholdClient = new FastThresholdClient(httpClient)
    private KrakenClient krakenClient = new DefaultKrakenClient()

    @Override
    double getTotal(String assetName) {
        double total = 0
        if (!assetName) {
            return 0
        }

        getAccountBalance().each {
            double assetPrice = 1
            if (isAStakingAsset(it)) {
                return
            }

            if (isUSD(it, assetName)) {
                total += it.balance
                return
            }
            if (isUSDXBT(it, assetName)) {
                Price price = getPrice("xbtusd")
                total += it.balance / price.last
                return
            }
            assetPrice = getAssetPrice(assetName, it, assetPrice)
            total += (assetPrice * it.balance)
        }
        return total
    }

    private static boolean isUSDXBT(AssetBalance it, String assetName) {
        it.assetName.toUpperCase() == "USD" && assetName.toUpperCase() == "XBT"
    }

    private static boolean isUSD(AssetBalance it, String assetName) {
        it.assetName.toUpperCase() == "USD" && assetName.toUpperCase() == "USD"
    }

    private static boolean isAStakingAsset(AssetBalance it) {
        it.assetName.toUpperCase().contains(".S") || it.assetName.toUpperCase().contains(".M")
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
        thresholdClient.ping()
        def pairs = thresholdClient.list().findAll { it.name.toUpperCase().contains("USD") || it.name.toUpperCase().contains("XBT") }
        return computeThresholdResponse(pairs)
    }

    private Map<String, Boolean> computeThresholdResponse(List<Threshold> pairs) {
        Map<String, Boolean> result = [:]
        pairs?.each { threshold ->
            String pairName = threshold.name
            Price price = getPrice(pairName)
            def thresholdResponse = thresholdClient.evaluate(pairName, price.last, new AlertWhenThresholdMet(httpClient))
            result.put(pairName, thresholdResponse)
        }
        return result
    }
}
