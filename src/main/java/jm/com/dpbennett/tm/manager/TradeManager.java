/*
Trade Management (TM) 
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
package jm.com.dpbennett.tm.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.tm.chart.FxMarketPxFeeder;
import jm.com.dpbennett.tm.chart.JfreeCandlestickChart;
import org.jfree.chart.ChartUtils;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Desmond Bennett
 */
public class TradeManager extends GeneralManager implements Serializable {

    private FinanceManager financeManager;
    private JfreeCandlestickChart jfreeCandlestickChart;

    public TradeManager() {

        init();
    }

    @Override
    public int getSizeOfActiveNotifications() {

        return getSystemManager().getActiveNotifications().size();
    }

    @Override
    public boolean getHasActiveNotifications() {
        return getSystemManager().getHasActiveNotifications();
    }

    @Override
    public List<Notification> getNotifications() {

        return getSystemManager().getNotifications();
    }

    @Override
    public void viewUserProfile() {
    }

    @Override
    public void onDashboardTabChange(TabChangeEvent event) {

        onMainViewTabChange(event);
    }

    @Override
    public String getDefaultCommandTarget() {

        return getSystemManager().getDefaultCommandTarget();

    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        getSystemManager().onMainViewTabChange(event);
    }

    public final void init() {
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        
        setName("tradeManager");

        createOhlcChart();

    }

    @Override
    public User getUser() {

        return getSystemManager().getUser();

    }

    public void updateCharts() {

    }

    public InputStream getChartAsStream() {
        return getChart().getStream().get();
    }

    public byte[] getChartAsByteArray() throws IOException {
        InputStream is = getChartAsStream();
        byte[] array = new byte[is.available()];
        is.read(array);
        return array;
    }

    public StreamedContent getChartWithoutBuffering() {
        try {
            return DefaultStreamedContent.builder()
                    .contentType("image/png")
                    .writer((os) -> {
                        try {

                            ChartUtils.writeChartAsPNG(os,
                                    jfreeCandlestickChart.getCandlestickChart(), 1000, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public StreamedContent getChart() {
        try {

            Random random = new Random();

            return DefaultStreamedContent.builder()
                    .contentType("image/png")
                    .stream(() -> {
                        try {
                            File chartFile = new File("image-" + random.nextInt() + ".png");
                            ChartUtils.saveChartAsPNG(chartFile,
                                    jfreeCandlestickChart.getCandlestickChart(),
                                    1000, 500);

                            return new FileInputStream(chartFile);

                        } catch (IOException e) {
                            System.out.println(e);
                            return null;
                        }
                    })
                    .build();

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private void createOhlcChart() {

        // Create and set up the chart.
        jfreeCandlestickChart = new JfreeCandlestickChart("TWTR");
        new FxMarketPxFeeder(jfreeCandlestickChart, "/twtr.csv").read();

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

        return getSystemManager().getEntityManager1();

    }

}
