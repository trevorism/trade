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

    private static final Map<String, String> ASSET_ALIASES = [
            "XXBT": "BTC",
            "XBT" : "BTC",
    ]

    @Override
    double getTotal(String assetName) {
        double total = 0
        if (!assetName) {
            return 0
        }

        getAccountBalance().each {
            double assetPrice = 1

            if(it.assetName[-2] == '.'){
                it.assetName = it.assetName[0..-3]
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
        assetName.toUpperCase() && (it.assetName.toUpperCase() == "USD" || it.assetName.toUpperCase() == "ZUSD")
    }

    private static String normalizeAssetName(String name) {
        ASSET_ALIASES.getOrDefault(name.toUpperCase(), name.toUpperCase())
    }

    private double getAssetPrice(String assetName, AssetBalance it, double assetPrice) {
        String normalizedAsset = normalizeAssetName(it.assetName)
        String normalizedTarget = normalizeAssetName(assetName)
        if (normalizedTarget != normalizedAsset) {
            assetPrice = getPrice("${normalizedAsset}${normalizedTarget}").last
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
