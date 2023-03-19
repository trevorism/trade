package com.trevorism.trade.service

import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.Price

interface TradeService {
    double getTotal(String assetName)

    Set<AssetBalance> getAccountBalance()

    Price getPrice(String pairName)

    Map<String, Boolean> checkPairsAgainstThresholds()
}