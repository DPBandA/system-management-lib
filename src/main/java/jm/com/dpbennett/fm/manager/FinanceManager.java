/*
Financial Management (FM) 
Copyright (C) 2025  D P Bennett & Associates Limited

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

import jm.com.dpbennett.tm.manager.TradeManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.AccountingCode;
import jm.com.dpbennett.business.entity.fm.Classification;
import jm.com.dpbennett.business.entity.fm.Currency;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.fm.Discount;
import jm.com.dpbennett.business.entity.fm.JobCategory;
import jm.com.dpbennett.business.entity.fm.JobSubCategory;
import jm.com.dpbennett.business.entity.fm.MarketProduct;
import jm.com.dpbennett.business.entity.fm.Sector;
import jm.com.dpbennett.business.entity.fm.Service;
import jm.com.dpbennett.business.entity.fm.Tax;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.pm.ProcurementMethod;
import jm.com.dpbennett.business.entity.pm.PurchaseRequisition;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.FinancialUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.event.CellEditEvent;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.dashboard.DashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardWidget;

/**
 *
 * @author Desmond Bennett
 */
public class FinanceManager extends GeneralManager implements Serializable {

    private AccountingCode selectedAccountingCode;
    private Tax selectedTax;
    private Discount selectedDiscount;
    private Currency selectedCurrency;
    private Classification selectedClassification;
    private Sector selectedSector;
    private JobCategory selectedJobCategory;
    private JobSubCategory selectedJobSubcategory;
    private Service selectedService;
    private MarketProduct selectedMarketProduct;
    private Boolean edit;
    private String accountingCodeSearchText;
    private String taxSearchText;
    private String currencySearchText;
    private String discountSearchText;
    private String classificationSearchText;
    private String sectorSearchText;
    private String jobCategorySearchText;
    private String jobSubcategorySearchText;
    private String serviceSearchText;
    private String marketProductSearchText;
    private String procurementMethodSearchText;
    private String settingSearchText;
    private List<AccountingCode> foundAccountingCodes;
    private List<Tax> foundTaxes;
    private List<Discount> foundDiscounts;
    private List<Currency> foundCurrencies;
    private List<Classification> foundClassifications;
    private List<Sector> foundSectors;
    private List<JobCategory> foundJobCategories;
    private List<JobSubCategory> foundJobSubcategories;
    private List<Service> foundServices;
    private Boolean isActiveDiscountsOnly;
    private Boolean isActiveTaxesOnly;
    private Boolean isActiveCurrenciesOnly;
    private Boolean isActiveAccountingCodesOnly;
    private Boolean isActiveClassificationsOnly;
    private Boolean isActiveJobCategoriesOnly;
    private Boolean isActiveJobSubcategoriesOnly;
    private Boolean isActiveSectorsOnly;
    private Boolean isActiveServicesOnly;
    private Boolean isActiveProcurementMethodsOnly;
    private List<MarketProduct> foundMarketProducts;
    private Boolean isActiveMarketProductsOnly;
    private List<ProcurementMethod> foundProcurementMethods;
    private ProcurementMethod selectedProcurementMethod;
    private SystemManager systemManager;
    private static final String RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-6";
    private DashboardModel dashboardModel;

    /**
     * Creates a new instance of FinanceManager.
     */
    public FinanceManager() {
        init();
    }

    public String getSupportURL() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "supportURL");
    }

    public String getCopyrightOrganization() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "copyrightOrganization");

    }

    public String getOrganizationWebsite() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "organizationWebsite");
    }

    public String getLastSystemNotificationContent() {

        return Notification.findLastActiveSystemNotificationMessage(
                getSystemManager().getEntityManager1());

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

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    public Employee getEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();

        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    public Boolean getIsActiveProcurementMethodsOnly() {

        return isActiveProcurementMethodsOnly;
    }

    public void setIsActiveProcurementMethodsOnly(Boolean isActiveProcurementMethodsOnly) {
        this.isActiveProcurementMethodsOnly = isActiveProcurementMethodsOnly;
    }

    public List<Classification> completeJobClassification(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Classification> classifications = Classification.findActiveClassificationsByNameAndCategory(em, query, "Job");

            return classifications;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Classification> completeClassification(String query) {
        EntityManager em = getEntityManager1();

        try {

            List<Classification> classifications = Classification.findActiveClassificationsByNameAndCategory(em, query, "Legal");

            return classifications;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<JobSubCategory> completeActiveJobSubCategories(String query) {
        EntityManager em = getEntityManager1();

        List<JobSubCategory> subCategories = JobSubCategory.findActiveJobSubcategoriesByName(em, query);

        return subCategories;
    }

    public List<JobCategory> completeActiveJobCategories(String query) {
        EntityManager em = getEntityManager1();

        List<JobCategory> categories = JobCategory.findActiveJobCategoriesByName(em, query);

        return categories;
    }

    public List<Sector> completeActiveSectors(String query) {
        EntityManager em = getEntityManager1();

        List<Sector> sectors = Sector.findActiveSectorsByName(em, query);

        return sectors;
    }

    public List<Discount> getAllActiveDiscounts() {

        return Discount.findAllActiveDiscounts(getEntityManager1());
    }

    public List<Tax> getAllActiveTaxes() {

        return Tax.findAllActiveTaxes(getEntityManager1());
    }

    public List<Sector> getAllActiveSectors() {

        List<Sector> sectors = new ArrayList<>();

        sectors.addAll(Sector.findAllActiveSectors(getEntityManager1()));

        return sectors;
    }

    public List<JobSubCategory> getAllActiveJobSubCategories() {

        List<JobSubCategory> jobSubCategories = new ArrayList<>();

        jobSubCategories.addAll(JobSubCategory.
                findAllActiveJobSubCategories(getEntityManager1()));

        return jobSubCategories;
    }

    public List<JobCategory> getAllActiveJobCategories() {

        List<JobCategory> jobCategories = new ArrayList<>();

        jobCategories.addAll(JobCategory.
                findAllActiveJobCategories(getEntityManager1()));

        return jobCategories;
    }

    public List<Classification> getAllActiveJobClassifications() {

        return Classification.findActiveClassificationsByCategory(getEntityManager1(), "Job");
    }

    public List<Service> getAllActiveServices() {

        List<Service> services = new ArrayList<>();

        services.add(new Service());
        services.addAll(Service.findAllActive(getEntityManager1()));

        return services;
    }

    public List<Currency> completeCurrency(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Currency> currencies = Currency.findAllByName(em, query);

            return currencies;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Tax> completeTax(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Tax> taxes = Tax.findActiveTaxesByNameAndDescription(em, query);

            return taxes;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getJobManagerEmailName() {

        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "jobManagerEmailName");
    }

    public String getJobManagerEmailAddress() {

        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "jobManagerEmailAddress");
    }

    public void openDashboardTab() {

        getMainTabView().openTab("Dashboard");

    }
    
    @Override
    public void initDashboard() {

        getDashboard().reset(getUser(), true);

        if (getUser().hasModule("financeManager")) {
            getDashboard().openTab("Financial Administration");
        }

        if (getUser().hasModule("systemManager")) {
            getDashboard().openTab("System Administration");
        }

    }

    @Override
    public void initMainTabView() {

        getMainTabView().reset(getUser());
        // tk 
        // Use open*() after checking for module access or privilege as is done in JMTS.        
        //openDashboardTab();
        getMainTabView().openTab("Purchase Requisitions");
        getMainTabView().openTab("Inventory Requisitions");//
        getMainTabView().openTab("Inventory Products");
        getMainTabView().openTab("Market Products");
        getMainTabView().openTab("Inventory");
        getMainTabView().openTab("Suppliers");
        getMainTabView().openTab("System Administration");
        getMainTabView().openTab("Trades");
        getMainTabView().openTab("Financial Administration");
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Financial Administration":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:accountingCodeSearchButton");
                return true;
            case "Accounting Codes":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:accountingCodeSearchButton");
                return true;
            case "Currencies":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:currencySearchButton");
                return true;
            case "Discounts":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:discountSearchButton");
                return true;
            case "Taxes":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:taxSearchButton");
                return true;
            case "Classifications":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:classificationSearchButton");
                return true;
            case "Sectors":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:sectorSearchButton");
                return true;
            case "Job Categories":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:jobCategorySearchButton");
                return true;
            case "Job Subcategories":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:jobSubCategorySearchButton");
                return true;
            case "Services":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:serviceSearchButton");
                return true;
            case "Procurement":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:procurementMethodSearchButton");
                return true;
            case "Settings":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:settingSearchButton");
                return true;
            default:
                return false;
        }
    }

    public String getApplicationFooter() {

        return getApplicationHeader() + ", v"
                + SystemOption.getString(getSystemManager().getEntityManager1(),
                        "FMv");
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "FMlogo");
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "FMlogo");
    }

    public Boolean getUseMulticurrency() {
        return SystemOption.getBoolean(getSystemManager().getEntityManager1(),
                "useMulticurrency");
    }

    public SystemManager getSystemManager() {
        if (systemManager == null) {
            systemManager = BeanUtils.findBean("systemManager");
        }
        return systemManager;
    }

    public void onRowSelect() {
        getSystemManager().setDefaultCommandTarget("@this");
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public String getScrollPanelHeight() {
        return "350px";
    }

    public List<SelectItem> getProcurementMethods() {

        return getStringListAsSelectItems(getSystemManager().getEntityManager1(),
                "procurementMethods");
    }

    public String getProcurementMethodSearchText() {
        return procurementMethodSearchText;
    }

    public void setProcurementMethodSearchText(String procurementMethodSearchText) {
        this.procurementMethodSearchText = procurementMethodSearchText;
    }

    public String getSettingSearchText() {
        return settingSearchText;
    }

    public void setSettingSearchText(String settingSearchText) {
        this.settingSearchText = settingSearchText;
    }

    public void saveSelectedProcurementMethod() {

        selectedProcurementMethod.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public List<ProcurementMethod> getFoundProcurementMethods() {
        if (foundProcurementMethods == null) {

            foundProcurementMethods = new ArrayList<>();
        }

        return foundProcurementMethods;
    }

    public void setFoundProcurementMethods(List<ProcurementMethod> foundProcurementMethods) {
        this.foundProcurementMethods = foundProcurementMethods;
    }

    public ProcurementMethod getSelectedProcurementMethod() {
        return selectedProcurementMethod;
    }

    public void setSelectedProcurementMethod(ProcurementMethod selectedProcurementMethod) {
        this.selectedProcurementMethod = selectedProcurementMethod;
    }

    public void editProcurementMethod() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("procurementMethodDialog", options, null);

    }

    public void createNewProcurementMethod() {

        selectedProcurementMethod = new ProcurementMethod();

        editProcurementMethod();
    }

    public void doProcurementMethodSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Procurement",
                getProcurementMethodSearchText(),
                null,
                null);

    }

    public void doSettingSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Settings",
                getSettingSearchText(),
                null,
                null);

    }

    public void onProcurementMethodCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundProcurementMethods().get(event.getRowIndex()));
    }

    /**
     * Select an financial administration tab based on whether or not the tab is
     * already opened.
     *
     * @param mainTabView
     * @param openTab
     * @param innerTabViewVar
     * @param innerTabIndex
     */
    public void selectFinancialAdminTab(
            MainTabView mainTabView,
            Boolean openTab,
            String innerTabViewVar,
            int innerTabIndex) {

        if (openTab) {
            mainTabView.openTab("Financial Administration");
        }

        PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");

    }

    public void selectTab(int innerTabIndex) {

        getMainTabView().openTab("Financial Administration");

        PrimeFaces.current().executeScript("PF('" + "financialAdminTabVar" + "').select(" + innerTabIndex + ");");

    }

    public List<MarketProduct> completeActiveMarketProduct(String query) {
        try {
            return MarketProduct.findActiveMarketProductsByName(
                    getEntityManager1(),
                    query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void editSelectedMarketProduct() {
        openMarketProductDialog();
    }

    public void createNewMarketProductCategory() {
        getSystemManager().setSelectedCategory(new Category());
        getSystemManager().getSelectedCategory().setType("Product");

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/admin/categoryDialog", options, null);

    }

    public void cancelMarketProductEdit() {
        getSelectedMarketProduct().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updateMarketProduct() {

        // This ensures that the product name field gets updated with the common name, brand and model.
        getSelectedMarketProduct().setName(getSelectedMarketProduct().toString());

        getSelectedMarketProduct().setIsDirty(true);
    }

    public void okMarketProduct() {

        try {

            if (getSelectedMarketProduct().getIsDirty()) {

                getSelectedMarketProduct().save(getEntityManager1());
                getSelectedMarketProduct().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void onMarketProductCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundMarketProducts().get(event.getRowIndex()));
    }

    public String getMarketProductSearchText() {
        return marketProductSearchText;
    }

    public int getNumMarketProducts() {
        if (foundMarketProducts != null) {
            return getFoundMarketProducts().size();
        } else {
            return 0;
        }
    }

    public void setMarketProductSearchText(String marketProductSearchText) {
        this.marketProductSearchText = marketProductSearchText;
    }

    public Boolean getIsActiveMarketProductsOnly() {
        return isActiveMarketProductsOnly;
    }

    public void setIsActiveMarketProductsOnly(Boolean isActiveMarketProductsOnly) {
        this.isActiveMarketProductsOnly = isActiveMarketProductsOnly;
    }

    public List<MarketProduct> getFoundMarketProducts() {
        if (foundMarketProducts == null) {

            foundMarketProducts = new ArrayList<>();
        }

        return foundMarketProducts;
    }

    public void setFoundMarketProducts(List<MarketProduct> foundMarketProducts) {
        this.foundMarketProducts = foundMarketProducts;
    }

    public void doMarketProductSearch() {

        if (getIsActiveMarketProductsOnly()) {
            foundMarketProducts = MarketProduct.findActiveMarketProductsByName(
                    getEntityManager1(), marketProductSearchText);
        } else {
            foundMarketProducts = MarketProduct.findMarketProductsByName(
                    getEntityManager1(), marketProductSearchText);
        }

    }

    public MarketProduct getSelectedMarketProduct() {
        return selectedMarketProduct;
    }

    public void setSelectedMarketProduct(MarketProduct selectedMarketProduct) {
        this.selectedMarketProduct = selectedMarketProduct;
    }

    public void openMarketProductDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 100) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/finance/marketProductDialog", options, null);

    }

    public void openMarketProductBrowser() {

        getMainTabView().openTab("Market Products");
    }

    public void createNewMarketProduct() {
        selectedMarketProduct = new MarketProduct();

        openMarketProductDialog();

        openMarketProductBrowser();
    }

    @Override
    public void handleSelectedNotification(Notification notification) {

        switch (notification.getType()) {
            case "PRSearch":
                PurchaseRequisition pr = null;
                EntityManager em = getEntityManager1();

                try {
                    pr = em.find(PurchaseRequisition.class,
                            Long.valueOf(notification.getMessage()));
                } catch (NumberFormatException e) {
                    System.out.println("PR not found");
                }

                if (pr != null) {
                    getPurchasingManager().getFoundPurchaseReqs().clear();
                    getPurchasingManager().getFoundPurchaseReqs().add(pr);
                    getPurchasingManager().openPurchaseReqsTab();
                }

                break;

            default:
                System.out.println("Unkown type");
        }

    }

    @Override
    public void onNotificationSelect(SelectEvent event) {

        EntityManager em = getSystemManager().getEntityManager1();

        Notification notification = Notification.findNotificationByNameAndOwnerId(
                em,
                (String) event.getObject(),
                getUser().getId(),
                false);

        if (notification != null) {

            handleSelectedNotification(notification);

            notification.setActive(false);
            notification.save(em);
        }

    }

    /**
     * Returns a list of CostComponent cost types.
     *
     * @return
     */
    public List getCostTypeList() {
        return FinancialUtils.getCostTypeList(getSystemManager().getEntityManager1());
    }

    public void closePriceList() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public String getServiceSearchText() {
        return serviceSearchText;
    }

    public void setServiceSearchText(String serviceSearchText) {
        this.serviceSearchText = serviceSearchText;
    }

    public List<Service> completeService(String query) {

        try {
            return Service.findAllActiveByName(getEntityManager1(), query);
        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<Service> getServices() {

        try {

            return Service.findAllActive(getEntityManager1());

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public Boolean getIsActiveServicesOnly() {
        return isActiveServicesOnly;
    }

    public void setIsActiveServicesOnly(Boolean isActiveServicesOnly) {
        this.isActiveServicesOnly = isActiveServicesOnly;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public List<Service> getFoundServices() {
        if (foundServices == null) {

            foundServices = new ArrayList<>();
        }

        return foundServices;
    }

    public void setFoundServices(List<Service> foundServices) {
        this.foundServices = foundServices;
    }

    public void onServiceCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundServices().get(event.getRowIndex()));
    }

    public void doServiceSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Services",
                getServiceSearchText(),
                null,
                null);

    }

    public void saveSelectedService() {

        selectedService.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewService() {
        selectedService = new Service();

        editService();
    }

    public void editService() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("serviceDialog", options, null);

    }

    public List<Classification> getClassifications() {
        return Classification.findAllClassifications(getEntityManager1());
    }

    public void saveSelectedClassification() {

        selectedClassification.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void cancelClassificationEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void editClassification() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/finance/classificationDialog", options, null);

    }

    public void onClassificationCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(), getFoundClassifications().get(event.getRowIndex()));
    }

    public Boolean getIsActiveClassificationsOnly() {
        return isActiveClassificationsOnly;
    }

    public void setIsActiveClassificationsOnly(Boolean isActiveClassificationsOnly) {
        this.isActiveClassificationsOnly = isActiveClassificationsOnly;
    }

    public Boolean getIsActiveJobCategoriesOnly() {
        return isActiveJobCategoriesOnly;
    }

    public void setIsActiveJobCategoriesOnly(Boolean isActiveJobCategoriesOnly) {
        this.isActiveJobCategoriesOnly = isActiveJobCategoriesOnly;
    }

    public Boolean getIsActiveJobSubcategoriesOnly() {
        return isActiveJobSubcategoriesOnly;
    }

    public void setIsActiveJobSubcategoriesOnly(Boolean isActiveJobSubcategoriesOnly) {
        this.isActiveJobSubcategoriesOnly = isActiveJobSubcategoriesOnly;
    }

    public Boolean getIsActiveSectorsOnly() {
        return isActiveSectorsOnly;
    }

    public void setIsActiveSectorsOnly(Boolean isActiveSectorsOnly) {
        this.isActiveSectorsOnly = isActiveSectorsOnly;
    }

    public List getClassificationCategories() {
        return getCategories();
    }

    public static List getCategories() {
        ArrayList categories = new ArrayList();

        // tk add system option for this.
        categories.add(new SelectItem("", ""));
        categories.add(new SelectItem("Client", "Client"));
        categories.add(new SelectItem("Job", "Job"));
        categories.add(new SelectItem("Legal", "Legal"));

        return categories;
    }

    public void doClassificationSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Classifications",
                getClassificationSearchText(),
                null,
                null);

    }

    public List<Classification> getFoundClassifications() {
        if (foundClassifications == null) {

            foundClassifications = new ArrayList<>();
        }

        return foundClassifications;
    }

    public Classification getSelectedClassification() {
        return selectedClassification;
    }

    public void setSelectedClassification(Classification selectedClassification) {
        this.selectedClassification = selectedClassification;
    }

    @Override
    public String getApplicationHeader() {

        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "FMAppName");
    }

    public void createNewClassification() {
        selectedClassification = new Classification();

        editClassification();
    }

    public void doSupplierSearch() {
        getPurchasingManager().doSupplierSearch();
    }

    public String getPurchaseReqSearchText() {
        return getPurchasingManager().getPurchaseReqSearchText();
    }

    public void setPurchaseReqSearchText(String purchaseReqSearchText) {
        getPurchasingManager().setPurchaseReqSearchText(purchaseReqSearchText);
    }

    public void doPurchaseReqSearch() {

        if (getSearchType().equals("My dept. requisitions")) {
            getPurchasingManager().
                    doPurchaseReqSearch(
                            getDateSearchPeriod(),
                            getSearchType(),
                            getPurchaseReqSearchText(),
                            getEmployee().getDepartment().getId());
        } else if (getSearchType().equals("All requisitions")) {
            getPurchasingManager().
                    doPurchaseReqSearch(
                            getDateSearchPeriod(),
                            getSearchType(),
                            getPurchaseReqSearchText(),
                            null);
        }
    }

    public PurchasingManager getPurchasingManager() {
        return BeanUtils.findBean("purchasingManager");
    }

    public InventoryManager getInventoryManager() {
        return BeanUtils.findBean("inventoryManager");
    }

    public TradeManager getTradeManager() {
        return BeanUtils.findBean("tradeManager");
    }

    public List<AccountingCode> completeAccountingCode(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<AccountingCode> codes = AccountingCode.
                    findActiveAccountingCodes(em, query);

            return codes;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getCurrencySearchText() {
        return currencySearchText;
    }

    public void setCurrencySearchText(String currencySearchText) {
        this.currencySearchText = currencySearchText;
    }

    public Boolean getIsActiveCurrenciesOnly() {
        return isActiveCurrenciesOnly;
    }

    public void setIsActiveCurrenciesOnly(Boolean isActiveCurrenciesOnly) {
        this.isActiveCurrenciesOnly = isActiveCurrenciesOnly;
    }

    public Tax getSelectedTax() {
        return selectedTax;
    }

    public void setSelectedTax(Tax selectedTax) {
        this.selectedTax = selectedTax;
    }

    public Currency getSelectedCurrency() {
        return selectedCurrency;
    }

    public void setSelectedCurrency(Currency selectedCurrency) {
        this.selectedCurrency = selectedCurrency;
    }

    public Discount getSelectedDiscount() {
        return selectedDiscount;
    }

    public void setSelectedDiscount(Discount selectedDiscount) {
        this.selectedDiscount = selectedDiscount;
    }

    public void openFinancialAdministration() {
        getMainTabView().openTab("Financial Administration");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAdminTabView:accountingCodeSearchButton");
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {

        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Accounting Codes", "Accounting Codes"));
        searchTypes.add(new SelectItem("Currencies", "Currencies"));
        searchTypes.add(new SelectItem("Discounts", "Discounts"));
        searchTypes.add(new SelectItem("Taxes", "Taxes"));
        searchTypes.add(new SelectItem("Classifications", "Classifications"));
        searchTypes.add(new SelectItem("Sectors", "Sectors"));
        searchTypes.add(new SelectItem("Job Categories", "Job Categories"));
        searchTypes.add(new SelectItem("Job Subcategories", "Job Subcategories"));
        searchTypes.add(new SelectItem("Services", "Services"));
        searchTypes.add(new SelectItem("Procurement", "Procurement"));
        searchTypes.add(new SelectItem("Settings", "Settings"));

        return searchTypes;

    }

    public void saveSelectedAccountingCode() {

        selectedAccountingCode.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedCurrency() {

        selectedCurrency.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedTax() {

        selectedTax.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedDiscount() {

        selectedDiscount.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public List getAccountingCodeTypes() {

        return FinancialUtils.getAccountingCodeTypes(getSystemManager().getEntityManager1());
    }

    public List getValueTypes() {

        return FinancialUtils.getValueTypes(getSystemManager().getEntityManager1());
    }

    public void editAccountingCode() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("accountingCodeDialog", options, null);

    }

    public void editTax() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("taxDialog", options, null);

    }

    public void editCurrency() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("currencyDialog", options, null);

    }

    public void editDiscount() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("discountDialog", options, null);

    }

    public void onAccountingCodeCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    AccountingCode code = getFoundAccountingCodes().get(index);
                    code.save(getEntityManager1());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void onTaxCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    Tax tax = getFoundTaxes().get(index);
                    tax.save(getEntityManager1());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void onCurrencyCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    Currency currency = getFoundCurrencies().get(index);
                    currency.save(getEntityManager1());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void onDiscountCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    Discount discount = getFoundDiscounts().get(index);
                    discount.save(getEntityManager1());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public String getAccountingCodeSearchText() {
        return accountingCodeSearchText;
    }

    public void setAccountingCodeSearchText(String accountingCodeSearchText) {
        this.accountingCodeSearchText = accountingCodeSearchText;
    }

    public List<AccountingCode> getFoundAccountingCodes() {
        if (foundAccountingCodes == null) {
            //doAccountingCodeSearch();
            foundAccountingCodes = new ArrayList<>();
        }

        return foundAccountingCodes;
    }

    public void setFoundAccountingCodes(List<AccountingCode> foundAccountingCodes) {
        this.foundAccountingCodes = foundAccountingCodes;
    }

    public List<Tax> getFoundTaxes() {
        if (foundTaxes == null) {
            //doTaxSearch();
            foundTaxes = new ArrayList<>();
        }

        return foundTaxes;
    }

    public void setFoundTaxes(List<Tax> foundTaxes) {
        this.foundTaxes = foundTaxes;
    }

    public List<Currency> getFoundCurrencies() {
        if (foundCurrencies == null) {
            //doCurrencySearch();
            foundCurrencies = new ArrayList<>();
        }

        return foundCurrencies;
    }

    public void setFoundCurrencies(List<Currency> foundCurrencies) {
        this.foundCurrencies = foundCurrencies;
    }

    public List<Discount> getFoundDiscounts() {
        if (foundDiscounts == null) {
            //doDiscountSearch();
            foundDiscounts = new ArrayList<>();
        }

        return foundDiscounts;
    }

    public void setFoundDiscounts(List<Discount> foundDiscounts) {
        this.foundDiscounts = foundDiscounts;
    }

    public void doAccountingCodeSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Accounting Codes",
                getAccountingCodeSearchText(),
                null,
                null);

    }

    public void doTaxSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Taxes",
                getTaxSearchText(),
                null,
                null);

    }

    public void doCurrencySearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Currencies",
                getCurrencySearchText(),
                null,
                null);

    }

    public void doDiscountSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Discounts",
                getDiscountSearchText(),
                null,
                null);
    }

    public void createNewAccountingCode() {

        selectedAccountingCode = new AccountingCode();

        editAccountingCode();
    }

    public void createNewTax() {

        selectedTax = new Tax();

        editTax();
    }

    public void createNewCurrency() {

        selectedCurrency = new Currency();

        editCurrency();
    }

    public void createNewDiscount() {

        selectedDiscount = new Discount();

        editDiscount();
    }

    public AccountingCode getSelectedAccountingCode() {
        return selectedAccountingCode;
    }

    public void setSelectedAccountingCode(AccountingCode selectedAccountingCode) {
        this.selectedAccountingCode = selectedAccountingCode;
    }

    public List getCostCodeList() {
        return FinancialUtils.getCostTypeList(getSystemManager().getEntityManager1());
    }

    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public final void init() {
        reset();
    }

    public String getJobCategorySearchText() {
        return jobCategorySearchText;
    }

    public void setJobCategorySearchText(String jobCategorySearchText) {
        this.jobCategorySearchText = jobCategorySearchText;
    }

    public JobCategory getSelectedJobCategory() {
        return selectedJobCategory;
    }

    public void setSelectedJobCategory(JobCategory selectedJobCategory) {
        this.selectedJobCategory = selectedJobCategory;
    }

    public List<JobCategory> getFoundJobCategories() {
        if (foundJobCategories == null) {

            foundJobCategories = new ArrayList<>();
        }

        return foundJobCategories;
    }

    public void setFoundJobCategories(List<JobCategory> foundJobCategories) {
        this.foundJobCategories = foundJobCategories;
    }

    public void onJobCategoryCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(), getFoundJobCategories().get(event.getRowIndex()));
    }

    public void doJobCategorySearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Job Categories",
                getJobCategorySearchText(),
                null,
                null);

    }

    public void cancelJobCategoryEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedJobCategory() {

        selectedJobCategory.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewJobCategory() {
        selectedJobCategory = new JobCategory();

        editJobCategory();
    }

    public void editJobCategory() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("jobCategoryDialog", options, null);

    }

    public List<JobSubCategory> getFoundJobSubcategories() {
        if (foundJobSubcategories == null) {

            foundJobSubcategories = new ArrayList<>();
        }

        return foundJobSubcategories;
    }

    public JobSubCategory getSelectedJobSubcategory() {
        return selectedJobSubcategory;
    }

    public void setSelectedJobSubcategory(JobSubCategory selectedJobSubcategory) {
        this.selectedJobSubcategory = selectedJobSubcategory;
    }

    public String getJobSubcategorySearchText() {
        return jobSubcategorySearchText;
    }

    public void setJobSubcategorySearchText(String jobSubcategorySearchText) {
        this.jobSubcategorySearchText = jobSubcategorySearchText;
    }

    public void onJobSubCategoryCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundJobSubcategories().get(event.getRowIndex()));
    }

    public void doJobSubcategorySearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Job Subcategories",
                getJobSubcategorySearchText(),
                null,
                null);

    }

    public void cancelJobSubcategoryEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedJobSubcategory() {

        selectedJobSubcategory.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewJobSubCategory() {
        selectedJobSubcategory = new JobSubCategory();

        editJobSubcategory();
    }

    public void editJobSubcategory() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("jobSubcategoryDialog", options, null);

    }

    public String getSectorSearchText() {
        return sectorSearchText;
    }

    public void setSectorSearchText(String sectorSearchText) {
        this.sectorSearchText = sectorSearchText;
    }

    public Sector getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSector(Sector selectedSector) {
        this.selectedSector = selectedSector;
    }

    public List<Sector> getFoundSectors() {
        if (foundSectors == null) {

            foundSectors = new ArrayList<>();
        }

        return foundSectors;
    }

    public void setFoundSectors(List<Sector> foundSectors) {
        this.foundSectors = foundSectors;
    }

    public void onSectorCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(), getFoundSectors().get(event.getRowIndex()));
    }

    public void doSectorSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Sectors",
                getSectorSearchText(),
                null,
                null);

    }

    public void cancelSectorEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedSector() {

        selectedSector.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewSector() {
        selectedSector = new Sector();

        editSector();
    }

    public void editSector() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("sectorDialog", options, null);

    }

    public List<Discount> completeDiscount(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Discount> discounts = Discount.findActiveDiscountsByNameAndDescription(em, query);

            return discounts;

        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public String getClassificationSearchText() {
        return classificationSearchText;
    }

    public void setClassificationSearchText(String classificationSearchText) {
        this.classificationSearchText = classificationSearchText;
    }

    public Boolean getIsActiveAccountingCodesOnly() {
        return isActiveAccountingCodesOnly;
    }

    public void setIsActiveAccountingCodesOnly(Boolean isActiveAccountingCodesOnly) {
        this.isActiveAccountingCodesOnly = isActiveAccountingCodesOnly;
    }

    public Boolean getIsActiveTaxesOnly() {
        return isActiveTaxesOnly;
    }

    public void setIsActiveTaxesOnly(Boolean isActiveTaxesOnly) {
        this.isActiveTaxesOnly = isActiveTaxesOnly;
    }

    public Boolean getIsActiveDiscountsOnly() {
        return isActiveDiscountsOnly;
    }

    public void setIsActiveDiscountsOnly(Boolean isActiveDiscountsOnly) {
        this.isActiveDiscountsOnly = isActiveDiscountsOnly;
    }

    public String getDiscountSearchText() {
        return discountSearchText;
    }

    public void setDiscountSearchText(String discountSearchText) {
        this.discountSearchText = discountSearchText;
    }

    public String getTaxSearchText() {
        return taxSearchText;
    }

    public void setTaxSearchText(String taxSearchText) {
        this.taxSearchText = taxSearchText;
    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Accounting Codes");
        setSearchText("");
        getSystemManager().
                setDefaultCommandTarget(":mainTabViewForm:mainTabView:purchaseReqSearchButton");
        setModuleNames(new String[]{
            "systemManager",
            "financeManager",
            "purchasingManager",
            "inventoryManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "requisitionDate", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        procurementMethodSearchText = "";
        settingSearchText = "";
        accountingCodeSearchText = "";
        taxSearchText = "";
        currencySearchText = "";
        discountSearchText = "";
        classificationSearchText = "";
        sectorSearchText = "";
        jobCategorySearchText = "";
        jobSubcategorySearchText = "";
        serviceSearchText = "";
        marketProductSearchText = "";
        isActiveDiscountsOnly = true;
        isActiveTaxesOnly = true;
        isActiveCurrenciesOnly = true;
        isActiveAccountingCodesOnly = true;
        isActiveJobCategoriesOnly = true;
        isActiveJobSubcategoriesOnly = true;
        isActiveSectorsOnly = true;
        isActiveServicesOnly = true;
        isActiveClassificationsOnly = true;
        isActiveMarketProductsOnly = true;
        isActiveProcurementMethodsOnly = true;

        dashboardModel = new DefaultDashboardModel();
        dashboardModel.addWidget(new DefaultDashboardWidget("procurementTasks", RESPONSIVE_CLASS));
        dashboardModel.addWidget(new DefaultDashboardWidget("inventoryTasks", RESPONSIVE_CLASS));

    }

    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public void setDashboardModel(DashboardModel dashboardModel) {
        this.dashboardModel = dashboardModel;
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("FMEM");

    }

    @Override
    public EntityManager getEntityManager2() {

        return getSystemManager().getEntityManager2();
    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        switch (searchType) {
            case "Accounting Codes":
                if (getIsActiveAccountingCodesOnly()) {
                    foundAccountingCodes = AccountingCode.findActiveAccountingCodes(
                            getEntityManager1(),
                            searchText);
                } else {
                    foundAccountingCodes = AccountingCode.findAccountingCodes(getEntityManager1(),
                            searchText);
                }

                break;
            case "Currencies":
                foundCurrencies = Currency.findAllByName(getEntityManager1(), searchText);

                break;
            case "Discounts":
                if (getIsActiveDiscountsOnly()) {
                    foundDiscounts = Discount.findActiveDiscountsByNameAndDescription(getEntityManager1(),
                            searchText);
                } else {
                    foundDiscounts = Discount.findDiscountsByNameAndDescription(getEntityManager1(),
                            searchText);
                }

                break;
            case "Taxes":
                if (getIsActiveTaxesOnly()) {
                    foundTaxes = Tax.findActiveTaxesByNameAndDescription(getEntityManager1(),
                            searchText);
                } else {
                    foundTaxes = Tax.findTaxesByNameAndDescription(getEntityManager1(),
                            searchText);
                }

                break;
            case "Classifications":
                if (getIsActiveClassificationsOnly()) {
                    foundClassifications = Classification.findActiveClassificationsByName(getEntityManager1(), searchText);
                } else {
                    foundClassifications = Classification.findClassificationsByName(getEntityManager1(), searchText);
                }

                break;
            case "Sectors":
                if (getIsActiveSectorsOnly()) {
                    foundSectors = Sector.findActiveSectorsByName(getEntityManager1(), searchText);
                } else {
                    foundSectors = Sector.findSectorsByName(getEntityManager1(), searchText);
                }

                break;
            case "Job Categories":
                if (getIsActiveJobCategoriesOnly()) {
                    foundJobCategories = JobCategory.findActiveJobCategoriesByName(getEntityManager1(), searchText);
                } else {
                    foundJobCategories = JobCategory.findJobCategoriesByName(getEntityManager1(), searchText);
                }

                break;
            case "Job Subcategories":
                if (getIsActiveJobSubcategoriesOnly()) {
                    foundJobSubcategories = JobSubCategory.findActiveJobSubcategoriesByName(getEntityManager1(), searchText);
                } else {
                    foundJobSubcategories = JobSubCategory.findJobSubcategoriesByName(getEntityManager1(), searchText);
                }

                break;
            case "Services":
                if (getIsActiveServicesOnly()) {
                    foundServices = Service.findAllActiveByName(getEntityManager1(), searchText);
                } else {
                    foundServices = Service.findAllByName(getEntityManager1(), searchText);
                }

                break;
            case "Procurement":

                if (getIsActiveProcurementMethodsOnly()) {
                    foundProcurementMethods = ProcurementMethod.findAllActiveByName(getEntityManager1(),
                            searchText);
                } else {
                    foundProcurementMethods = ProcurementMethod.findAllByName(getEntityManager1(),
                            searchText);
                }

                break;
            case "Settings":
                getSystemManager().doSystemOptionSearch("Finance", searchText);

                break;
            default:
                break;
        }
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {

        SelectItemGroup group = new SelectItemGroup("Financial Administration");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;

    }

    @Override
    public void handleKeepAlive() {

        super.updateUserActivity("FMv"
                + SystemOption.getString(getSystemManager().getEntityManager1(), "FMv"),
                "Logged in");

        if (getUser().getId() != null) {
            getUser().save(getSystemManager().getEntityManager1());
        }

        if ((Boolean) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "debugMode")) {
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
        }

        PrimeFaces.current().ajax().update(":headerForm:notificationBadge");

    }

    @Override
    public String getApplicationSubheader() {
        String subHeader;

        subHeader = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "applicationSubheader");

        if (subHeader != null) {
            if (subHeader.trim().equals("None")) {
                return getEmployee().getDepartment().getName();
            }
        } else {
            subHeader = "";
        }

        return subHeader;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Accounting Codes":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Currencies":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Discounts":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Taxes":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Classifications":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Sectors":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Job Categories":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Job Subcategories":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Services":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Procurement":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Settings":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public void login() {
        login(getSystemManager().getEntityManager1());
    }

    @Override
    public void logout() {
        completeLogout();
    }

    @Override
    public void completeLogout() {

        super.updateUserActivity("FMv"
                + SystemOption.getString(getSystemManager().getEntityManager1(), "FMv"),
                "Logged out");

        if (getUser().getId() != null) {
            getUser().save(getSystemManager().getEntityManager1());
        }

        getDashboard().removeAllTabs();
        getMainTabView().removeAllTabs();

        reset();

    }

    @Override
    public void completeLogin() {

        if (getUser().getId() != null) {
            super.updateUserActivity("FMv"
                    + SystemOption.getString(getSystemManager().getEntityManager1(), "FMv"),
                    "Logged in");
            getUser().save(getSystemManager().getEntityManager1());
        }

        setManagerUser();

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initMainTabView();
        
        initDashboard();

    }

    @Override
    public void setManagerUser() {

        getManager("systemManager").setUser(getUser());
        getManager("inventoryManager").setUser(getUser());
        getManager("purchasingManager").setUser(getUser());

    }

    public ArrayList<SelectItem> getPurchReqSearchTypes() {
        ArrayList purchReqSearchTypes = new ArrayList();

        if (getUser().can("AccessAllPurchaseRequisitions")) {
            purchReqSearchTypes.add(new SelectItem("All requisitions", "All requisitions"));
            purchReqSearchTypes.add(new SelectItem("My dept. requisitions", "My dept. requisitions"));
        } else {
            purchReqSearchTypes.add(new SelectItem("My dept. requisitions", "My dept. requisitions"));
        }

        return purchReqSearchTypes;

    }

    public ArrayList<SelectItem> getPurchReqDateSearchFields() {
        ArrayList dateSearchFields = new ArrayList();

        dateSearchFields.add(new SelectItem("requisitionDate", "Requisition date"));
        dateSearchFields.add(new SelectItem("dateOfCompletion", "Date completed"));
        dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
        dateSearchFields.add(new SelectItem("expectedDateOfCompletion", "Exp'ted date of completion"));
        dateSearchFields.add(new SelectItem("dateRequired", "Date required"));
        dateSearchFields.add(new SelectItem("purchaseOrderDate", "Purchase order date"));
        dateSearchFields.add(new SelectItem("teamLeaderApprovalDate", "Team Leader approval date"));
        dateSearchFields.add(new SelectItem("divisionalManagerApprovalDate", "Divisional Manager approval date"));
        dateSearchFields.add(new SelectItem("divisionalDirectorApprovalDate", "Divisional Director approval date"));
        dateSearchFields.add(new SelectItem("financeDirectorApprovalDate", "Finance Director approval date"));
        dateSearchFields.add(new SelectItem("executiveDirectorApprovalDate", "Executive Director approval date"));

        return dateSearchFields;
    }

}
