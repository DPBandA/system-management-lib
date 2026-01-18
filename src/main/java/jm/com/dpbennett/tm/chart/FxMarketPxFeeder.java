/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2026  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.tm.chart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jm.com.dpbennett.business.entity.tm.Constants;
import jm.com.dpbennett.business.entity.tm.Trade;
import jm.com.dpbennett.sm.util.TimeUtils;

public class FxMarketPxFeeder {

    private final JfreeCandlestickChart jfreeCandlestickChart;
    private final String stockTradesFile;

    public FxMarketPxFeeder(JfreeCandlestickChart jfreeCandlestickChart, String stockTradesFile) {
        super();

        this.stockTradesFile = stockTradesFile;
        this.jfreeCandlestickChart = jfreeCandlestickChart;
    }

    public void read() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream(stockTradesFile)))) {
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    // Parse line and convert it to trade
                    String[] tradeElements = line.split(Constants.DELIMITER);
                    Trade t = new Trade(tradeElements[Constants.STOCK_IDX],
                            TimeUtils.convertToMillisTime(tradeElements[Constants.TIME_IDX]),
                            Double.parseDouble(tradeElements[Constants.PRICE_IDX]),
                            Long.parseLong(tradeElements[Constants.SIZE_IDX]));
                    // Add trade to the jfreeCandlestickChart 
                    jfreeCandlestickChart.onTrade(t);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}
