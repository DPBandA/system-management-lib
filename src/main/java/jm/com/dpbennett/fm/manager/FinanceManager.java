/*
Financial Management (FM) 
Copyright (C) 2023  D P Bennett & Associates Limited

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
import jm.com.dpbennett.business.entity.pm.ProcurementMethod;
import jm.com.dpbennett.business.entity.pm.PurchaseRequisition;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
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

/**
 *
 * @author Desmond Bennett
 */
public class FinanceManager extends GeneralManager implements Serializable {

    private Integer longProcessProgress;
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
    private List<MarketProduct> foundMarketProducts;
    private Boolean isActiveMarketProductsOnly;
    private List<ProcurementMethod> foundProcurementMethods;
    private ProcurementMethod selectedProcurementMethod;
    private String procurementMethodSearchText;
    private SystemManager systemManager;

    /**
     * Creates a new instance of FinanceManager.
     */
    public FinanceManager() {
        init();
    }

    public SystemManager getSystemManager() {
        if (systemManager == null) {
            systemManager = BeanUtils.findBean("systemManager");
        }
        return systemManager;
    }

    public void createNewMarketProductInDialog() {
        //currentMarketProduct = new MarketProduct();

        //openMarketProductDialog();
    }

    // tk
    public ArrayList<String> completeMarketProduct(String query) {
        EntityManager em;
        ArrayList<MarketProduct> products = new ArrayList<>(); // tk

        try {
            em = getEntityManager1();

//            ArrayList<MarketProduct> products
//                    = new ArrayList<>(MarketProduct.findActiveMarketProductsByAnyPartOfNameOrDescription(em, query));
            ArrayList<String> productsList = (ArrayList<String>) (ArrayList<?>) products;

            return productsList;

        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public void onRowSelect() {
        setDefaultCommandTarget("@this");
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

        return getStringListAsSelectItems(getEntityManager1(),
                "procurementMethods");
    }

    public String getProcurementMethodSearchText() {
        return procurementMethodSearchText;
    }

    public void setProcurementMethodSearchText(String procurementMethodSearchText) {
        this.procurementMethodSearchText = procurementMethodSearchText;
    }

    public void saveSelectedProcurementMethod() {

        selectedProcurementMethod.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public List<ProcurementMethod> getFoundProcurementMethods() {
        if (foundProcurementMethods == null) {
            foundProcurementMethods = ProcurementMethod.findAll(getEntityManager1());
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
        PrimeFacesUtils.openDialog(null, "procurementMethodDialog",
                true, true, true, getDialogHeight(), 700);
    }

    public void createNewProcurementMethod() {

        selectedProcurementMethod = new ProcurementMethod();

        editProcurementMethod();
    }

    public void doProcurementMethodSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Procurement",
                getProcurementMethodSearchText(),
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

        PrimeFacesUtils.openDialog(null, "/admin/categoryDialog", true, true, true,
                getDialogHeight(), getDialogWidth());
    }

    public void cancelMarketProductEdit() {
        getSelectedMarketProduct().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public List<Category> completeActiveCategory(String query) {
        try {
            return Category.findActiveCategoriesByAnyPartOfNameAndType(
                    getEntityManager1(),
                    "Product",
                    query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
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
            foundMarketProducts = MarketProduct.findAllActiveMarketProducts(getEntityManager1());
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
        PrimeFacesUtils.openDialog(null, "/finance/marketProductDialog", true, true, true, true,
                getDialogHeight(), getDialogWidth());
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
                            Long.parseLong(notification.getMessage()));
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

        EntityManager em = getEntityManager1();

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
        return FinancialUtils.getCostTypeList(getEntityManager1());
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
            return Service.findAllByName(getEntityManager1(), query);
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
            foundServices = Service.findAllActive(getEntityManager1());
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

        setDefaultCommandTarget("@this");

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
        PrimeFacesUtils.openDialog(null, "serviceDialog", true, true, true, getDialogHeight(), getDialogWidth());
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
        PrimeFacesUtils.openDialog(null, "classificationDialog", true, true, true, getDialogHeight(), getDialogWidth());
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

        setDefaultCommandTarget("@this");

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
            foundClassifications = Classification.findAllActiveClassifications(getEntityManager1());
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
                getEntityManager1(), "FMAppName");
    }

    public void createNewClassification() {
        selectedClassification = new Classification();

        editClassification();
    }

    public PurchasingManager getPurchasingManager() {
        return BeanUtils.findBean("purchasingManager");
    }

    public InventoryManager getInventoryManager() {
        return BeanUtils.findBean("inventoryManager");
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
        searchTypes.add(new SelectItem("Miscellaneous", "Miscellaneous"));

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

        return FinancialUtils.getAccountingCodeTypes(getEntityManager1());
    }

    public List getValueTypes() {

        return FinancialUtils.getValueTypes(getEntityManager1());
    }

    public void editAccountingCode() {
        PrimeFacesUtils.openDialog(null, "accountingCodeDialog", true, true, true,
                getDialogHeight(), getDialogWidth());
    }

    public void editTax() {
        PrimeFacesUtils.openDialog(null, "taxDialog", true, true, true, getDialogHeight(), getDialogWidth());
    }

    public void editCurrency() {
        PrimeFacesUtils.openDialog(null, "currencyDialog", true, true, true, getDialogHeight(), getDialogWidth());
    }

    public void editDiscount() {
        PrimeFacesUtils.openDialog(null, "discountDialog", true, true, true, getDialogHeight(), getDialogWidth());
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
            doAccountingCodeSearch();
        }

        return foundAccountingCodes;
    }

    public void setFoundAccountingCodes(List<AccountingCode> foundAccountingCodes) {
        this.foundAccountingCodes = foundAccountingCodes;
    }

    public List<Tax> getFoundTaxes() {
        if (foundTaxes == null) {
            doTaxSearch();
        }

        return foundTaxes;
    }

    public void setFoundTaxes(List<Tax> foundTaxes) {
        this.foundTaxes = foundTaxes;
    }

    public List<Currency> getFoundCurrencies() {
        if (foundCurrencies == null) {
            doCurrencySearch();
        }
        return foundCurrencies;
    }

    public void setFoundCurrencies(List<Currency> foundCurrencies) {
        this.foundCurrencies = foundCurrencies;
    }

    public List<Discount> getFoundDiscounts() {
        if (foundDiscounts == null) {
            doDiscountSearch();
        }

        return foundDiscounts;
    }

    public void setFoundDiscounts(List<Discount> foundDiscounts) {
        this.foundDiscounts = foundDiscounts;
    }

    public void doAccountingCodeSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Accounting Codes",
                getAccountingCodeSearchText(),
                null,
                null);

    }

    public void doTaxSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Taxes",
                getTaxSearchText(),
                null,
                null);

    }

    public void doCurrencySearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Currencies",
                getCurrencySearchText(),
                null,
                null);

    }

    public void doDiscountSearch() {

        setDefaultCommandTarget("@this");

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
        return FinancialUtils.getCostTypeList(getEntityManager1());
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

    @Override
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
            foundJobCategories = JobCategory.findAllActiveJobCategories(getEntityManager1());
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

        setDefaultCommandTarget("@this");

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
        PrimeFacesUtils.openDialog(null, "jobCategoryDialog", true, true, true, getDialogHeight(), getDialogWidth());
    }

    public List<JobSubCategory> getFoundJobSubcategories() {
        if (foundJobSubcategories == null) {
            foundJobSubcategories = JobSubCategory.findAllActiveJobSubCategories(getEntityManager1());
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

        setDefaultCommandTarget("@this");

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
        PrimeFacesUtils.openDialog(null, "jobSubcategoryDialog", true, true, true, getDialogHeight(), getDialogWidth());
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
            foundSectors = Sector.findAllActiveSectors(getEntityManager1());
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

        setDefaultCommandTarget("@this");

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
        PrimeFacesUtils.openDialog(null, "sectorDialog", true, true, true, getDialogHeight(), getDialogWidth());
    }

    public List<Tax> completeTax(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Tax> taxes = Tax.findTaxesByNameAndDescription(em, query);

            return taxes;

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
        setDefaultCommandTarget("@this");
        setModuleNames(new String[]{
            "systemManager",
            "financeManager",
            "purchasingManager",
            "inventoryManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "requisitionDate", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        longProcessProgress = 0;
        procurementMethodSearchText = "";
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

    }

    @Override
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    // tk not used so to be deleted
    public Integer getLongProcessProgress() {
        if (longProcessProgress == null) {
            longProcessProgress = 0;
        } else {
            if (longProcessProgress < 10) {
                // this is to ensure that this method does not make the progress
                // complete as this is done elsewhere.
                longProcessProgress = longProcessProgress + 1;
            }
        }

        return longProcessProgress;
    }

    // tk not used so to be deleted
    public void onLongProcessComplete() {
        longProcessProgress = 0;
    }

    public void setLongProcessProgress(Integer longProcessProgress) {
        this.longProcessProgress = longProcessProgress;
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

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 0);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 0);
                }
                break;
            case "Currencies":
                foundCurrencies = Currency.findAllByName(getEntityManager1(), searchText);

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 1);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 1);
                }
                break;
            case "Discounts":
                if (getIsActiveDiscountsOnly()) {
                    foundDiscounts = Discount.findActiveDiscountsByNameAndDescription(getEntityManager1(),
                            searchText);
                } else {
                    foundDiscounts = Discount.findDiscountsByNameAndDescription(getEntityManager1(),
                            searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 2);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 2);
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

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 3);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 3);
                }
                break;
            case "Classifications":
                if (getIsActiveClassificationsOnly()) {
                    foundClassifications = Classification.findActiveClassificationsByName(getEntityManager1(), searchText);
                } else {
                    foundClassifications = Classification.findClassificationsByName(getEntityManager1(), searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 4);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 4);
                }
                break;
            case "Sectors":
                if (getIsActiveSectorsOnly()) {
                    foundSectors = Sector.findActiveSectorsByName(getEntityManager1(), searchText);
                } else {
                    foundSectors = Sector.findSectorsByName(getEntityManager1(), searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 5);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 5);
                }
                break;
            case "Job Categories":
                if (getIsActiveJobCategoriesOnly()) {
                    foundJobCategories = JobCategory.findActiveJobCategoriesByName(getEntityManager1(), searchText);
                } else {
                    foundJobCategories = JobCategory.findJobCategoriesByName(getEntityManager1(), searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 6);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 6);
                }
                break;
            case "Job Subcategories":
                if (getIsActiveJobSubcategoriesOnly()) {
                    foundJobSubcategories = JobSubCategory.findActiveJobSubcategoriesByName(getEntityManager1(), searchText);
                } else {
                    foundJobSubcategories = JobSubCategory.findJobSubcategoriesByName(getEntityManager1(), searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 7);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 7);
                }
                break;
            case "Services":
                if (getIsActiveServicesOnly()) {
                    foundServices = Service.findAllActiveByName(getEntityManager1(), searchText);
                } else {
                    foundServices = Service.findAllByName(getEntityManager1(), searchText);
                }

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 8);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 8);
                }
                break;
            case "Procurement":
                foundProcurementMethods = ProcurementMethod.findAllByName(getEntityManager1(),
                        searchText);

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 9);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 9);
                }
                break;
            case "Miscellaneous":
                getSystemManager().doFinancialSystemOptionSearch(searchText);

                if (startDate == null) {
                    selectFinancialAdminTab(mainTabView, false, "financialAdminTabVar", 10);
                } else {
                    selectFinancialAdminTab(mainTabView, true, "financialAdminTabVar", 10);
                }
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
        getUser().setPollTime(new Date());

        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(), "debugMode")) {
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
        }
        if (getUser().getId() != null) {
            getUser().save(getEntityManager1());
        }

        PrimeFaces.current().ajax().update(":appForm:notificationBadge");
    }

    @Override
    public String getApplicationSubheader() {
        String subHeader;

        subHeader = (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "applicationSubheader");

        if (subHeader != null) {
            if (subHeader.trim().equals("None")) {
                return getUser().getEmployee().getDepartment().getName();
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
            case "Miscellaneous":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void setManagersUser() {
        
        getManager("systemManager").setUser(getUser());
        getManager("inventoryManager").setUser(getUser());
        getManager("purchasingManager").setUser(getUser());

    }

}
