/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2024  D P Bennett & Associates Limited.

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

import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

import javax.swing.JPanel;
import jm.com.dpbennett.business.entity.tm.Trade;
import jm.com.dpbennett.sm.util.MathUtils;
import jm.com.dpbennett.sm.util.TimeUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;

@SuppressWarnings("serial")
public class JfreeCandlestickChart extends JPanel {

    private static final DateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("kk:mm:ss");
    private OHLCSeries ohlcSeries;
    private TimeSeries volumeSeries;
    private JFreeChart candlestickChart;
    private static final int MIN = 60000;
    // Every minute
    private final int timeInterval = 1;
    private Trade candelChartIntervalFirstPrint = null;
    private double open = 0.0;
    private double close = 0.0;
    private double low = 0.0;
    private double high = 0.0;
    private long volume = 0;

    public JfreeCandlestickChart(String title) {

        candlestickChart = createChart(title);

    }

    public JFreeChart getCandlestickChart() {
        return candlestickChart;
    }

    public void setCandlestickChart(JFreeChart candlestickChart) {
        this.candlestickChart = candlestickChart;
    }
    
    // tk for moving average
    private static double[][] calculateMovingAverage(OHLCSeriesCollection dataset, int period) {
        OHLCSeries series = dataset.getSeries(0);
        //int itemCount = series.getItemCount();
        double[][] data = new double[2][10];

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            data[0][i] = (110 - 90) * random.nextDouble();//series.getPeriod(i).getFirstMillisecond();
            data[1][i] = 90 + (110 - 90) * random.nextDouble(); // Random value between 90 and 110
        }

        return data;
    }

    private JFreeChart createChart(String chartTitle) {

        // Creating candlestick subplot        
        // Create OHLCSeriesCollection as a price dataset for candlestick chart
        OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
        ohlcSeries = new OHLCSeries("Price");
        candlestickDataset.addSeries(ohlcSeries);
        // Create candlestick chart priceAxis
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        // Create candlestick chart renderer
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
                false, new CustomHighLowItemLabelGenerator(new SimpleDateFormat("kk:mm"), new DecimalFormat("0.000")));
        // Create candlestickSubplot
        XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
        candlestickSubplot.setBackgroundPaint(Color.white);
        
        // tk
        // Calculate and create moving average dataset
//        double[][] movingAverageData = calculateMovingAverage(candlestickDataset, 10); // 10-period moving average
//        DefaultXYDataset movingAverageDataset = new DefaultXYDataset();
//        movingAverageDataset.addSeries("Moving Average", movingAverageData);
//        
//         // Add moving average plot
//        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
//        lineRenderer.setSeriesPaint(0, Color.BLUE);
//        candlestickSubplot.setDataset(1, movingAverageDataset);
//        candlestickSubplot.setRenderer(1, lineRenderer);
//        candlestickSubplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
//
//        // Configure date axis
//        DateAxis dateAxis2 = new DateAxis("Date");
//        dateAxis2.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
//        candlestickSubplot.setDomainAxis(dateAxis2);
        // end tk

        // Creating volume subplot
        // creates TimeSeriesCollection as a volume dataset for volume chart
        TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
        volumeSeries = new TimeSeries("Volume");
        volumeDataset.addSeries(volumeSeries);
        // Create volume chart volumeAxis
        NumberAxis volumeAxis = new NumberAxis("Volume");
        volumeAxis.setAutoRangeIncludesZero(false);
        // Set to no decimal
        volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
        // Create volume chart renderer
        XYBarRenderer timeRenderer = new XYBarRenderer();
        timeRenderer.setShadowVisible(false);

        // Create volumeSubplot
        XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
        volumeSubplot.setBackgroundPaint(Color.white);

        // Create chart main plot with two subplots (candlestickSubplot,
        // volumeSubplot) and one common dateAxis
        // Creating charts common dateAxis
        DateAxis dateAxis = new DateAxis("Time");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
        // reduce the default left/right margin from 0.05 to 0.02
        dateAxis.setLowerMargin(0.02);
        dateAxis.setUpperMargin(0.02);

        // Create mainPlot
        CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
        mainPlot.setGap(10.0);
        mainPlot.add(candlestickSubplot, 3);
        mainPlot.add(volumeSubplot, 1);     
        mainPlot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
        chart.removeLegend();

        return chart;
    }

    /**
     * Fill series with data.
     *
     * @param time
     * @param o
     * @param h
     * @param l
     * @param c
     * @param v
     */
    public void addCandel(long time, double o, double h, double l, double c, long v) {
        try {
            // Add bar to the data. Let's repeat the same bar
            FixedMillisecond t = new FixedMillisecond(
                    READABLE_TIME_FORMAT.parse(TimeUtils.convertToReadableTime(time)));
            ohlcSeries.add(t, o, h, l, c);
            volumeSeries.add(t, v);
        } catch (ParseException e) {
            System.out.println(e);
        }
    }

    /**
     * Aggregate the (open, high, low, close, volume) based on the predefined
     * time interval (1 minute)
     *
     * @param t the t
     */
    public void onTrade(Trade t) {
        double price = t.getPrice();
        if (candelChartIntervalFirstPrint != null) {
            long time = t.getTime();
            if (timeInterval == (int) ((time / MIN) - (candelChartIntervalFirstPrint.getTime() / MIN))) {
                // Set the period close price
                close = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
                // Add new candle
                addCandel(time, open, high, low, close, volume);
                // Reset the intervalFirstPrint to null
                candelChartIntervalFirstPrint = null;
            } else {
                // Set the current low price
                if (MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT) < low) {
                    low = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
                }

                // Set the current high price
                if (MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT) > high) {
                    high = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
                }

                volume += t.getSize();
            }
        } else {
            // Set intervalFirstPrint
            candelChartIntervalFirstPrint = t;
            // the first trade price in the day (day open price)
            open = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // the interval low
            low = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // the interval high
            high = MathUtils.roundDouble(price, MathUtils.TWO_DEC_DOUBLE_FORMAT);
            // set the initial volume
            volume = t.getSize();
        }
    }

}
