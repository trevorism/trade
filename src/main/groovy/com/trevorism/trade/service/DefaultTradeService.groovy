package com.trevorism.trade.service

import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.error.KrakenRequestException
import com.trevorism.kraken.impl.DefaultKrakenClient
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price
import com.trevorism.threshold.FastThresholdClient
import com.trevorism.threshold.ThresholdClient
import com.trevorism.threshold.model.Threshold
import com.trevorism.threshold.strategy.AlertWhenThresholdMet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultTradeService implements TradeService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTradeService)

    private SecureHttpClient httpClient = new AppClientSecureHttpClient()
    private ThresholdClient thresholdClient = new FastThresholdClient(httpClient)
    private KrakenClient krakenClient = new DefaultKrakenClient()

    private static final Map<String, String> ASSET_ALIASES = [
            "XXBT": "XBT",
            "XETC": "ETC",
            "XETH": "ETH",
            "XLTC": "LTC",
            "XMLN": "MLN",
            "XREP": "REP",
            "XXDG": "XDG",
            "XXLM": "XLM",
            "XXMR": "XMR",
            "XXRP": "XRP",
            "XZEC": "ZEC",
            "KFEE": "FEE",
            "ZARS": "ARS",
            "ZAUD": "AUD",
            "ZCAD": "CAD",
            "ZCLP": "CLP",
            "ZCOP": "COP",
            "ZDKK": "DKK",
            "ZEUR": "EUR",
            "ZGBP": "GBP",
            "ZGEL": "GEL",
            "ZGHS": "GHS",
            "ZJPY": "JPY",
            "ZLKR": "LKR",
            "ZMXN": "MXN",
            "ZPLN": "PLN",
            "ZSEK": "SEK",
            "ZUGX": "UGX",
            "ZUSD": "USD",
            "ZVND": "VND",
            "ZXOF": "XOF",
            "BTC" : "XBT",
            "DOGE": "XDG",
    ]

    @Override
    double getTotal(String assetName) {
        if (!assetName) {
            return 0
        }

        String normalizedTarget = normalizeAssetName(assetName)
        double total = 0

        getAccountBalance().each { AssetBalance assetBalance ->
            String normalizedAsset = normalizeAssetName(assetBalance.assetName)

            if (normalizedAsset == normalizedTarget) {
                total += assetBalance.balance
                return
            }

            Double conversionRate = findConversionRate(normalizedAsset, normalizedTarget)
            if (conversionRate == null) {
                log.warn("Excluding {} from the {} total; no market found in either direction",
                        assetBalance.assetName, normalizedTarget)
                return
            }
            total += conversionRate * assetBalance.balance
        }
        return total
    }

    private Double findConversionRate(String fromAsset, String toAsset) {
        Double directRate = lookupLastPrice("${fromAsset}${toAsset}")
        if (directRate) {
            return directRate
        }
        Double invertedRate = lookupLastPrice("${toAsset}${fromAsset}")
        return invertedRate ? 1 / invertedRate : null
    }

    private Double lookupLastPrice(String pairName) {
        try {
            return getPrice(pairName).last
        } catch (KrakenRequestException ignored) {
            return null
        }
    }

    private static String normalizeAssetName(String name) {
        String base = name.toUpperCase()
        int suffixIndex = base.indexOf('.')
        if (suffixIndex > 0) {
            base = base.substring(0, suffixIndex)
        }
        ASSET_ALIASES.getOrDefault(base, base)
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
