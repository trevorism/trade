package com.trevorism.gcloud.webapi.controller

import com.trevorism.kraken.KrakenClient
import com.trevorism.kraken.model.Asset
import com.trevorism.kraken.model.AssetBalance
import com.trevorism.kraken.model.AssetPair
import com.trevorism.kraken.model.Candle
import com.trevorism.kraken.model.DateRange
import com.trevorism.kraken.model.Order
import com.trevorism.kraken.model.Price
import com.trevorism.kraken.model.trade.CancelOrderResult
import com.trevorism.kraken.model.trade.LimitTrade
import com.trevorism.kraken.model.trade.MarketTrade
import com.trevorism.kraken.model.trade.StopLossTrade
import com.trevorism.kraken.model.trade.Trade
import com.trevorism.kraken.model.trade.TradeResult
import org.junit.Before
import org.junit.Test

import java.time.Duration

class TradeControllerTest {

    private TradeController tradeController

    @Before
    void setup(){
        tradeController = new TradeController()
        tradeController.krakenClient = new TestKrakenClient()
    }

    @Test
    void testListOpenOrders() {
        def result =  tradeController.listOpenOrders()
        assert result

    }

    @Test
    void testQueryClosedOrders() {
        def result = tradeController.queryForClosedOrders(null)
        assert result
    }

    @Test
    void testCreateLimitOrder() {
        def result = tradeController.createLimitTradeOrder(new LimitTrade())
        assert result
    }

    @Test
    void testCreateMarketOrder() {
        def result = tradeController.createMarketTradeOrder(new MarketTrade())
        assert result
    }

    @Test
    void testCreateStopLossOrder() {
        def result = tradeController.createStopLossTradeOrder(new StopLossTrade())
        assert result
    }

    @Test
    void testRemoveOrder(){
        def result = tradeController.removeOrder("1234")
        assert result
    }


    class TestKrakenClient implements KrakenClient{
        @Override
        Set<AssetBalance> getAccountBalances() {
            return null
        }

        @Override
        List<Order> getClosedOrders(DateRange dateRange) {
            [new Order(orderId: "testClosedOrderId")]
        }

        @Override
        List<Order> getOpenOrders(DateRange dateRange) {
            [new Order(orderId: "testOpenOrderId")]
        }

        @Override
        TradeResult createOrder(Trade trade) {
            return new TradeResult(orderDescription: "test")
        }

        @Override
        CancelOrderResult deleteOrder(String s) {
            return new CancelOrderResult()
        }

        @Override
        long serverTime() {
            return 0
        }

        @Override
        List<Asset> getAssets() {
            return null
        }

        @Override
        List<AssetPair> getAssetPairs() {
            return null
        }

        @Override
        Price getCurrentPrice(String s) {
            return null
        }

        @Override
        List<Candle> getCandles(String s, Duration duration) {
            return null
        }
    }
}
