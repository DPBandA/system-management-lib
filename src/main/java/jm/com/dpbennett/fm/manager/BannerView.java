/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jm.com.dpbennett.fm.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.primefaces.model.ResponsiveOption;

/**
 *
 * @author Desmond Bennett
 */
public class BannerView {

    private List<Banner> banners;

    private List<ResponsiveOption> responsiveOptions1;

    private List<ResponsiveOption> responsiveOptions2;

    private List<ResponsiveOption> responsiveOptions3;

    private int activeIndex = 0;

    public BannerView() {

        banners = new ArrayList<>();
        banners.add(new Banner("https://ae01.alicdn.com/kf/Sec5f132be3ba4cf08462cadfb67ee37ef.jpg", 
                "https://ae01.alicdn.com/kf/Sec5f132be3ba4cf08462cadfb67ee37ef.jpg",
                "AliExpress Black Friday Sale", 
                "AliExpress Black Friday Sale", "https://s.click.aliexpress.com/e/_DliYUYX?bz=500*500"));
        banners.add(new Banner("https://ae01.alicdn.com/kf/Sa909d6794c4a40b7a718743270123f07y.jpg", 
                "https://ae01.alicdn.com/kf/Sa909d6794c4a40b7a718743270123f07y.jpg",
                "AliExpress Black Friday Sale", 
                "AliExpress Black Friday Sale", 
                "https://s.click.aliexpress.com/e/_DebjDV5?bz=500*500"));
        banners.add(new Banner("https://ae01.alicdn.com/kf/Sdde88a6d77df41fdbb576c9472fadf1fn.jpg", 
                "https://ae01.alicdn.com/kf/Sdde88a6d77df41fdbb576c9472fadf1fn.jpg",
                "AliExpress Black Friday Sale", 
                "AliExpress Black Friday Sale", 
                "https://s.click.aliexpress.com/e/_DB8DfIR?bz=500*500"));
        banners.add(new Banner("https://ae01.alicdn.com/kf/S3619e57974f148d087c950fe497cdf55q/300x250.jpg", 
                "https://ae01.alicdn.com/kf/S3619e57974f148d087c950fe497cdf55q/300x250.jpg",
                "AliExpress Black Friday Sale", 
                "AliExpress Black Friday Sale", 
                "https://s.click.aliexpress.com/e/_Dm0H5iP?bz=300*250"));

        responsiveOptions1 = new ArrayList<>();
        responsiveOptions1.add(new ResponsiveOption("1024px", 5));
        responsiveOptions1.add(new ResponsiveOption("768px", 3));
        responsiveOptions1.add(new ResponsiveOption("560px", 1));

        responsiveOptions2 = new ArrayList<>();
        responsiveOptions2.add(new ResponsiveOption("1024px", 5));
        responsiveOptions2.add(new ResponsiveOption("960px", 4));
        responsiveOptions2.add(new ResponsiveOption("768px", 3));
        responsiveOptions2.add(new ResponsiveOption("560px", 1));

        responsiveOptions3 = new ArrayList<>();
        responsiveOptions3.add(new ResponsiveOption("1500px", 5));
        responsiveOptions3.add(new ResponsiveOption("1024px", 3));
        responsiveOptions3.add(new ResponsiveOption("768px", 2));
        responsiveOptions3.add(new ResponsiveOption("560px", 1));
    }
    
     public void changeActiveIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        this.activeIndex = Integer.valueOf(params.get("index"));
    }

    public List<Banner> getBanners() {
        return banners;
    }  

    public List<ResponsiveOption> getResponsiveOptions1() {
        return responsiveOptions1;
    }

    public List<ResponsiveOption> getResponsiveOptions2() {
        return responsiveOptions2;
    }

    public List<ResponsiveOption> getResponsiveOptions3() {
        return responsiveOptions3;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }


}
