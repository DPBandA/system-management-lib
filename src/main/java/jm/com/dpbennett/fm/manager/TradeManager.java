/*
Trade Management (TM) 
Copyright (C) 2024  D P Bennett & Associates Limited

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
package jm.com.dpbennett.fm.manager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.persistence.EntityManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.dashboard.DashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardWidget;

/**
 *
 * @author Desmond Bennett
 */
public class TradeManager extends GeneralManager implements Serializable {
    
    private DashboardModel dashboardModel;
    private static final String RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-6";
    private FinanceManager financeManager;
    private JFreeChart ohlcChart;
   
    public TradeManager() {
              
        init();
    }
    
    @Override
    public final void init() {
        reset();
    }
    
    @Override
    public void reset() {
        super.reset();

        dashboardModel = new DefaultDashboardModel();
        dashboardModel.addWidget(new DefaultDashboardWidget("trades", RESPONSIVE_CLASS));
        dashboardModel.addWidget(new DefaultDashboardWidget("positions", RESPONSIVE_CLASS));
        
        createOhlcChart();

    }
    
    public InputStream getChartAsStream() {
        return getChart().getStream().get();
    }
    
    public StreamedContent getChart() {
        try {
            
            Random random = new Random();
            
            return DefaultStreamedContent.builder()
                    .contentType("image/png")
                    .stream(() -> {
                        try {
                            File chartFile = new File("" + random.nextInt());
                            ChartUtilities.saveChartAsPNG(chartFile, getOhlcChart(), 375, 300);
                            return new FileInputStream(chartFile);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JFreeChart getOhlcChart() {
        return ohlcChart;
    }
    
    private void createOhlcChart() {
        // Example data
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JULY, 15); // Year, month, and day (note: month is 0-based)
        Date past = calendar.getTime();
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(2024, Calendar.JULY, 14);
        Date past2 = calendar2.getTime();
        
                
        Date[] dates = new Date[] {past, past, new Date(), past2, new Date()};
        double[] high = new double[] {20, 25, 30, 35, 40};
        double[] low = new double[] {10, 15, 20, 25, 30};
        double[] open = new double[] {12, 17, 22, 27, 32};
        double[] close = new double[] {38, 23, 28, 33, 18};
        double[] volume = new double[] {1000, 1500, 2000, 2500, 3000};

        OHLCDataset dataset = new DefaultHighLowDataset(
            "OHLC",
            dates,
            high,
            low,
            open,
            close,
            volume
        );
        
        
//        ohlcChart = 
//                ChartFactory.createHighLowChart(
//                        "OHLC Chart",                         
//                        "Time", 
//                        "Value", 
//                        dataset, 
//                        true);
        
         ohlcChart = 
                ChartFactory.createCandlestickChart(
                        "OHLC Chart",                         
                        "Time", 
                        "Value", 
                        dataset, 
                        true);

        XYPlot plot = (XYPlot) ohlcChart.getPlot();
        CandlestickRenderer renderer = new CandlestickRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(1.0f,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer.setSeriesPaint(0, Color.black);
        
        plot.setRenderer(renderer);
    }
    
    public FinanceManager getFinanceManager() {
        if (financeManager == null) {
            financeManager = BeanUtils.findBean("financeManager");
        }

        return financeManager;
    }
    
    @Override
    public MainTabView getMainTabView() {

        return getFinanceManager().getMainTabView();
    }
    
    public void openInventoryProductBrowser() {

        getFinanceManager().getMainTabView().openTab("Trades");
        
  }

    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("FMEM");

    }
    
    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

}
