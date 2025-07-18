/*
Inventory Management
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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import org.primefaces.event.CellEditEvent;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import org.primefaces.PrimeFaces;
import java.util.Objects;
import javax.faces.model.SelectItemGroup;
import jm.com.dpbennett.business.entity.fm.CostComponent;
import jm.com.dpbennett.business.entity.im.Inventory;
import jm.com.dpbennett.business.entity.im.InventoryDisbursement;
import jm.com.dpbennett.business.entity.im.InventoryRequisition;
import jm.com.dpbennett.business.entity.fm.MarketProduct;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.business.entity.util.NumberUtils;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.FinancialUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.ResponsiveOption;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Desmond Bennett
 */
public class InventoryManager extends GeneralManager implements Serializable {

    private Inventory selectedInventory;
    private InventoryRequisition selectedInventoryRequisition;
    private MarketProduct selectedInventoryProduct;
    private CostComponent selectedCostComponent;
    private List<InventoryRequisition> selectedInventoryRequisitions;
    private InventoryDisbursement selectedInventoryDisbursement;
    private Boolean edit;
    private String inventoryProductSearchText;
    private String inventoryRequisitionSearchText;
    private String inventorySearchText;
    private List<Inventory> selectedInventories;
    private List<Inventory> foundInventories;
    private List<Inventory> foundActiveInventories;
    private List<InventoryRequisition> foundInventoryRequisitions;
    private List<InventoryRequisition> inventoryTasks;
    private List<MarketProduct> foundInventoryProducts;
    private Boolean activeInventoryOnly;
    private Boolean activeInventoryProductsOnly;
    private FinanceManager financeManager;
    private List<ResponsiveOption> responsiveOptions;
    private StreamedContent inventoryRequisitionFile;
    private BannerView bannerView;

    public InventoryManager() {
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

    private Employee getEmployee() {

        return getFinanceManager().getEmployee();
    }

    @Override
    public User getUser() {

        return getFinanceManager().getUser();

    }

    public List<SelectItem> getInventoryLocations() {

        return getStringListAsSelectItems(
                getSystemManager().getEntityManager1(),
                "inventoryLocations");
    }

    public List<InventoryRequisition> getInventoryTasks() {

        EntityManager em = getEntityManager1();
        int maxSearchResults = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");
        inventoryTasks = new ArrayList<>();
        List<InventoryRequisition> activeIRs
                = InventoryRequisition.findAllActive(em, maxSearchResults);

        for (InventoryRequisition activeIR : activeIRs) {
            if (getEmployee().equals(activeIR.getContactPerson())
                    || getEmployee().equals(activeIR.getRequisitionApprovedBy())
                    || getEmployee().equals(activeIR.getInventoryReceivedBy())
                    || getEmployee().equals(activeIR.getRequisitionBy())
                    || getEmployee().equals(activeIR.getEnteredBy())
                    || getEmployee().equals(activeIR.getEditedBy())
                    || getEmployee().equals(activeIR.getInventoryIssuedBy())) {

                inventoryTasks.add(activeIR);

            }
        }

        return inventoryTasks;
    }

    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Inventory Products":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventoryProductSearchButton");
                return true;
            case "Inventory":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventorySearchButton");
                return true;
            case "Inventory Requisitions":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventoryRequisitionSearchButton");
                return true;
            default:
                return false;
        }
    }

    public void prepareInventoryRequisition() {

        if (getSelectedInventoryRequisition().getPrepared()) {
            getSelectedInventoryRequisition().setDateEdited(new Date());
            getSelectedInventoryRequisition().setEditedBy(getEmployee());
        }

        updateInventoryRequisition(null);

    }

    public void issueInventory() {

        if (getSelectedInventoryRequisition().getIssued()) {
            getSelectedInventoryRequisition().setDateInventoryIssued(new Date());
            getSelectedInventoryRequisition().setInventoryIssuedBy(getEmployee());
        } else {
            getSelectedInventoryRequisition().setDateInventoryIssued(null);
            getSelectedInventoryRequisition().setInventoryIssuedBy(null);
        }

        updateInventoryRequisition(null);

    }

    public void receiveInventory() {

        if (getSelectedInventoryRequisition().getReceived()) {
            getSelectedInventoryRequisition().setDateInventoryReceived(new Date());
            getSelectedInventoryRequisition().setInventoryReceivedBy(getEmployee());
        } else {
            getSelectedInventoryRequisition().setDateInventoryReceived(null);
            getSelectedInventoryRequisition().setInventoryReceivedBy(null);
        }

        updateInventoryRequisition(null);

    }

    public void approveInventoryRequisition() {

        if (getSelectedInventoryRequisition().getApproved()) {
            getSelectedInventoryRequisition().setDateRequisitionApproved(new Date());
            getSelectedInventoryRequisition().setRequisitionApprovedBy(getEmployee());
        } else {
            getSelectedInventoryRequisition().setDateRequisitionApproved(null);
            getSelectedInventoryRequisition().setRequisitionApprovedBy(null);
        }

        updateInventoryRequisition(null);

    }

    public Boolean getCanPrepareInventoryRequisition() {
        return !getSelectedInventoryRequisition().getApproved();
    }

    public Boolean getCanApproveInventoryRequisition() {
        return getSelectedInventoryRequisition().getPrepared();
    }

    public Boolean getShowInventoryMarketingTab() {
        return SystemOption.getBoolean(
                getSystemManager().getEntityManager1(),
                "showInventoryMarketingTab");
    }

    public String getAmazonAffiliateURL() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "amazonAffiliateURL");
    }

    public String getAmazonBanner() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "amazonBanner");
    }

    public String getAliExpressAffiliateURL() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "aliExpressAffiliateURL");
    }

    public String getAliExpressBanner() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "aliExpressBanner");
    }

    public Boolean getActiveInventoryProductsOnly() {
        return activeInventoryProductsOnly;
    }

    public void setActiveInventoryProductsOnly(Boolean activeInventoryProductsOnly) {
        this.activeInventoryProductsOnly = activeInventoryProductsOnly;
    }

    // tk
    public BannerView getBannerView() {
        return bannerView;
    }

    // tk
    public void setBannerView(BannerView bannerView) {
        this.bannerView = bannerView;
    }

    public Boolean getCanExportInventoryRequisitionForm() {
        // tk
        return true;
    }

    public StreamedContent getInventoryRequisitionFile() {
        EntityManager em;

        try {
            em = getEntityManager1();

            inventoryRequisitionFile = getInventoryRequisitionFile(em);

        } catch (Exception e) {
            System.out.println(e);
        }

        return inventoryRequisitionFile;
    }

    public StreamedContent getInventoryRequisitionFile(EntityManager em) {

        HashMap parameters = new HashMap();

        try {

            parameters.put("reqId", getSelectedInventoryRequisition().getId());

            if (getSelectedInventoryRequisition().getApproved()) {
                parameters.put("approvedBy",
                        getSelectedInventoryRequisition().getRequisitionApprovedBy().getFirstName() + " "
                        + getSelectedInventoryRequisition().getRequisitionApprovedBy().getLastName());
                parameters.put("approvalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(
                                getSelectedInventoryRequisition().getDateRequisitionApproved()));

            }

            if (getSelectedInventoryRequisition().getIssued()) {
                parameters.put("issuedBy",
                        getSelectedInventoryRequisition().getInventoryIssuedBy().getFirstName() + " "
                        + getSelectedInventoryRequisition().getInventoryIssuedBy().getLastName());
                parameters.put("dateIssued",
                        BusinessEntityUtils.getDateInMediumDateFormat(
                                getSelectedInventoryRequisition().getDateInventoryIssued()));

            }

            if (getSelectedInventoryRequisition().getReceived()) {
                parameters.put("receivedBy",
                        getSelectedInventoryRequisition().getInventoryReceivedBy().getFirstName() + " "
                        + getSelectedInventoryRequisition().getInventoryReceivedBy().getLastName());
                parameters.put("dateReceived",
                        BusinessEntityUtils.getDateInMediumDateFormat(
                                getSelectedInventoryRequisition().getDateInventoryReceived()));

            }

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamContent;
                    // Compile report
                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(
                                            getSystemManager().getEntityManager1(),
                                            "storesRequisition"));

                    // Generate report
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamContent = DefaultStreamedContent.builder()
                            .stream(() -> new ByteArrayInputStream(fileBytes))
                            .contentType("application/pdf")
                            .name("Stores Requisition - " + BusinessEntityUtils.getNow() + ".pdf")
                            .build();

                    em.getTransaction().commit();

                    return streamContent;
                } catch (JRException ex) {
                    System.out.println(ex);
                    return null;
                }
            }

            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    public List<ResponsiveOption> getResponsiveOptions() {
        return responsiveOptions;
    }

    public void setResponsiveOptions(List<ResponsiveOption> responsiveOptions) {
        this.responsiveOptions = responsiveOptions;
    }

    public String getInventoryRequisitionSearchText() {
        return inventoryRequisitionSearchText;
    }

    public void setInventoryRequisitionSearchText(String inventoryRequisitionSearchText) {
        this.inventoryRequisitionSearchText = inventoryRequisitionSearchText;
    }

    public String getInventorySearchText() {
        return inventorySearchText;
    }

    public void setInventorySearchText(String inventorySearchText) {
        this.inventorySearchText = inventorySearchText;
    }

    public void onRowSelect() {
        getFinanceManager().setDefaultCommandTarget("@this");
    }

    public List<SelectItem> getProductTypes() {

        return getStringListAsSelectItems(
                getSystemManager().getEntityManager1(),
                "productTypes");
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 650;
    }

    public String getScrollPanelHeight() {
        return "350px";
    }

    public InventoryDisbursement getSelectedInventoryDisbursement() {
        return selectedInventoryDisbursement;
    }

    public void setSelectedInventoryDisbursement(InventoryDisbursement selectedInventoryDisbursement) {
        this.selectedInventoryDisbursement = selectedInventoryDisbursement;
    }

    public void addNewDisbursement() {

        selectedInventoryDisbursement = new InventoryDisbursement();

        selectedInventoryDisbursement.updateCostComponent();

        setEdit(false);

    }

    public InventoryRequisition getSelectedInventoryRequisition() {
        return selectedInventoryRequisition;
    }

    public void setSelectedInventoryRequisition(
            InventoryRequisition selectedInventoryRequisition) {

        this.selectedInventoryRequisition
                = getSavedInventoryRequisition(selectedInventoryRequisition);
    }

    public InventoryRequisition getSavedInventoryRequisition(
            InventoryRequisition ir) {

        int i = 0;
        InventoryRequisition foundInventoryRequisition
                = InventoryRequisition.findById(getEntityManager1(), ir.getId());

        for (InventoryRequisition inventoryRequisition : foundInventoryRequisitions) {
            if (Objects.equals(inventoryRequisition.getId(), foundInventoryRequisition.getId())) {
                foundInventoryRequisitions.set(i, foundInventoryRequisition);
                break;
            }
            ++i;
        }

        return foundInventoryRequisition;
    }

    public void deleteCostComponent() {
        deleteCostComponentByName(selectedCostComponent.getName());
    }

    public void deleteSelectedCostComponent() {
        deleteCostComponentByName(selectedCostComponent.getName());
    }

    public void deleteCostComponentByName(String componentName) {

        List<CostComponent> components = getSelectedInventory().getAllSortedCostComponents();
        int index = 0;
        for (CostComponent costComponent : components) {
            if (costComponent.getName().equals(componentName)) {
                components.remove(index);
                updateInventory(null);

                break;
            }
            ++index;
        }
    }

    public void okCostingComponent() {
        if (selectedCostComponent.getId() == null && !getEdit()) {
            getSelectedInventory().getCostComponents().add(selectedCostComponent);
        }
        setEdit(false);

        updateCostComponent(selectedCostComponent);
        updateInventory(null);

        PrimeFaces.current().executeScript("PF('inventoryCostingCompDialog').hide();");
    }

    public void cancelCostComponentEdit() {
        selectedCostComponent.setIsDirty(false);
    }

    public void updateSelectedCostComponent() {
        updateCostComponent(getSelectedCostComponent());
    }

    public void editCostComponent(ActionEvent event) {
        setEdit(true);
    }

    public void editDisbursement(ActionEvent event) {
        setEdit(true);
    }

    public void deleteSelectedDisbursement() {

        deleteDisbursementByProductName(selectedInventoryDisbursement.
                getInventory().getProduct().getName());

    }

    public void deleteDisbursementByProductName(String productName) {

        List<InventoryDisbursement> disbusements = getSelectedInventoryRequisition().getAllSortedInventoryDisbursements();
        int index = 0;
        for (InventoryDisbursement disbursement : disbusements) {
            if (disbursement.getInventory().getProduct().getName().equals(productName)) {
                disbusements.remove(index);
                updateInventoryRequisition(null);

                break;
            }
            ++index;
        }
    }

    public void updateSelectedDisbursement() {
        updateDisbursement(getSelectedInventoryDisbursement());
    }

    public void updateSelectedDisbursementQuantitySupplied(AjaxBehaviorEvent event) {

        updateDisbursement(getSelectedInventoryDisbursement());
    }

    public void updateSelectedDisbursementInventoryItem() {

        Double unitPrice = 0.0;

        getSelectedInventoryDisbursement()
                .setDescription(getSelectedInventoryDisbursement().getInventory().getName() + ".");

        List<CostComponent> cc = getSelectedInventoryDisbursement().
                getInventory().getAllCostComponentsSortedByCostDate();

        switch (getSelectedInventoryDisbursement().
                getInventory().getDisbursementMethod()) {

            case "LIFO":
                if (!cc.isEmpty()) {
                    unitPrice = cc.get(cc.size() - 1).getRate();
                    getSelectedInventoryDisbursement().setUnitCost(unitPrice);
                } else {
                    getSelectedInventoryDisbursement().setUnitCost(unitPrice);
                }
                break;
            case "FIFO":
            default:
                if (!cc.isEmpty()) {
                    unitPrice = cc.get(0).getRate();
                    getSelectedInventoryDisbursement().setUnitCost(unitPrice);
                } else {
                    getSelectedInventoryDisbursement().setUnitCost(unitPrice);
                }
                break;
        }

        updateDisbursement(getSelectedInventoryDisbursement());
    }

    public void updateDisbursement(InventoryDisbursement inventoryDisbursement) {

        inventoryDisbursement.update();

        inventoryDisbursement.setIsDirty(true);

    }

    public void cancelDisbursementEdit() {
        selectedInventoryDisbursement.setIsDirty(false);
    }

    public void okDisbursement() {
        if (selectedInventoryDisbursement.getId() == null && !getEdit()) {
            getSelectedInventoryRequisition().getInventoryDisbursements().add(selectedInventoryDisbursement);
        }

        setEdit(false);
        updateDisbursement(selectedInventoryDisbursement);
        updateInventoryRequisition(null);

        PrimeFaces.current().executeScript("PF('inventoryDisbursementDialog').hide();");
    }

    public void updateCostType() {

        selectedCostComponent.update();
    }

    public void updateCostComponent(CostComponent costComponent) {

        costComponent.update();

        costComponent.setIsDirty(true);

    }

    public List getCostTypeList() {
        return FinancialUtils.getCostTypeList(
                getSystemManager().getEntityManager1());
    }

    public void onCostComponentCellEdit(CellEditEvent event) {

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {

            CostComponent costComponent = getSelectedInventory().getAllSortedCostComponents().
                    get(event.getRowIndex());

            updateCostComponent(costComponent);

            getSelectedInventory().getAllSortedCostComponents().set(event.getRowIndex(),
                    costComponent);

        }

    }

    public void onCostComponentRowCancel(RowEditEvent<CostComponent> event) {
        event.getObject().setIsDirty(false);
    }

    public void onInventoryProductRowCancel(RowEditEvent<MarketProduct> event) {
        event.getObject().setIsDirty(false);
    }

    public void onCostComponentRowEdit(RowEditEvent<CostComponent> event) {

        updateCostComponent(event.getObject());
        updateInventory(null);
    }

    public void onInventoryProductRowEdit(RowEditEvent<MarketProduct> event) {

        event.getObject().setName(event.getObject().toString());

        event.getObject().save(getEntityManager1());
        event.getObject().setIsDirty(false);
    }

    public void onDisbursementRowCancel(RowEditEvent<InventoryDisbursement> event) {
        event.getObject().setIsDirty(false);
    }

    public void onDisbursementRowEdit(RowEditEvent<InventoryDisbursement> event) {

        updateDisbursement(event.getObject());
        updateInventoryRequisition(null);
    }

    public void addNewCostComponent() {
        selectedCostComponent = createNewCostComponent(
                getSelectedInventory().getProduct().getName(),
                getSelectedInventory().getCode(),
                "Purchase");

        setEdit(false);

    }

    public CostComponent createNewCostComponent(String name, String code, String type) {
        CostComponent costComponent = new CostComponent();

        costComponent.setName(name);
        costComponent.setCode(code);
        costComponent.setHoursOrQuantity(1.0);
        costComponent.setType(type);
        costComponent.setUnit("each");
        costComponent.setCostDate(new Date());
        costComponent.update();

        return costComponent;

    }

    public CostComponent getSelectedCostComponent() {
        return selectedCostComponent;
    }

    public void setSelectedCostComponent(CostComponent selectedCostComponent) {
        this.selectedCostComponent = selectedCostComponent;
    }

    public void createNewProductCategory() {
        getFinanceManager().getSystemManager().setSelectedCategory(new Category());
        getFinanceManager().getSystemManager().getSelectedCategory().setType("Product");

        getFinanceManager().getSystemManager().editCategory();
    }

    public Boolean getActiveInventoryOnly() {
        return activeInventoryOnly;
    }

    public void setActiveInventoryOnly(Boolean activeInventoryOnly) {
        this.activeInventoryOnly = activeInventoryOnly;
    }

    public MarketProduct getSelectedInventoryProduct() {
        return selectedInventoryProduct;
    }

    public void setSelectedInventoryProduct(MarketProduct selectedInventoryProduct) {
        this.selectedInventoryProduct = selectedInventoryProduct;
    }

    public String getInventoryProductSearchText() {
        return inventoryProductSearchText;
    }

    public void setInventoryProductSearchText(String inventoryProductSearchText) {
        this.inventoryProductSearchText = inventoryProductSearchText;
    }

    public void createNewInventoryProductInDialog() {
        MarketProduct inventoryProduct = new MarketProduct();
        inventoryProduct.setType("Inventory");

        getSelectedInventory().setIsDirty(true);

        setSelectedInventoryProduct(inventoryProduct);

        openInventoryProductDialog();
    }

    public void editSelectedInventoryProduct() {
        openInventoryProductDialog();
    }

    public void onInventoryProductCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundInventoryProducts().get(event.getRowIndex()));
    }

    public int getNumInventoryProducts() {
        if (foundInventoryProducts != null) {
            return getFoundInventoryProducts().size();
        } else {
            return 0;
        }
    }

    public List<MarketProduct> getFoundInventoryProducts() {

        if (foundInventoryProducts == null) {
            foundInventoryProducts = new ArrayList<>();
        }

        return foundInventoryProducts;
    }

    public void doInventoryProductSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Inventory Products",
                getInventoryProductSearchText(),
                null,
                null);
    }

    public void openInventoryProductDialog() {

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

        PrimeFaces.current().dialog().openDynamic("/finance/ims/inventoryProductDialog", options, null);

    }

    public void openInventoryProductBrowser() {

        getFinanceManager().getMainTabView().openTab("Inventory Products");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventoryProductSearchButton");
    }

    public void createNewInventoryProduct() {
        selectedInventoryProduct = new MarketProduct();
        selectedInventoryProduct.setType("Inventory");

        openInventoryProductDialog();

        openInventoryProductBrowser();
    }

    public void okInventoryProduct() {

        try {

            if (getSelectedInventoryProduct().getIsDirty()) {

                getSelectedInventoryProduct().save(getEntityManager1());
                getSelectedInventoryProduct().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateInventoryProduct() {

        getSelectedInventoryProduct().setName(getSelectedInventoryProduct().toString());

        getSelectedInventoryProduct().setIsDirty(true);
    }

    public void cancelInventoryProductEdit() {
        getSelectedInventoryProduct().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public List<MarketProduct> completeActiveInventoryProduct(String query) {
        try {
            return MarketProduct.findActiveMarketProductsByNameAndType(
                    getEntityManager1(),
                    query, "Inventory");

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<Inventory> completeInventoryItem(String query) {
        try {

            int maxResult = SystemOption.getInteger(
                    getSystemManager().getEntityManager1(),
                    "maxSearchResults");

            return Inventory.findActive(
                    getEntityManager1(),
                    query, maxResult);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public Boolean getIsInventoryProductNameValid() {
        return BusinessEntityUtils.validateText(
                getSelectedInventory().getProduct().getName());
    }

    public PurchasingManager getPurchasingManager() {
        return BeanUtils.findBean("purchasingManager");
    }

    public FinanceManager getFinanceManager() {
        if (financeManager == null) {
            financeManager = BeanUtils.findBean("financeManager");
        }

        return financeManager;
    }

    public void editInventorySupplier() {
        getPurchasingManager().setSelectedSupplier(getSelectedInventory().getSupplier());

        getPurchasingManager().editSelectedSupplier();
    }

    public void editInventoryCategory() {
        getFinanceManager().getSystemManager().setSelectedCategory(getSelectedInventory().getInventoryCategory());

        getFinanceManager().getSystemManager().editCategory();
    }

    public void inventorySupplierDialogReturn() {
        if (getPurchasingManager().getSelectedSupplier().getId() != null) {
            getSelectedInventory().setSupplier(getPurchasingManager().getSelectedSupplier());
            updateInventory(null);
        }
    }

    public void inventoryCategoryDialogReturn() {
        if (getFinanceManager().getSystemManager().getSelectedCategory().getId() != null) {
            getSelectedInventory().setInventoryCategory(getFinanceManager().getSystemManager().getSelectedCategory());
            updateInventory(null);
        }
    }

    public void inventoryProductDialogReturn() {
        if (getSelectedInventoryProduct().getId() != null) {
            getSelectedInventory().setProduct(getSelectedInventoryProduct());
            updateInventory(null);
        }
    }

    public Boolean getIsSupplierNameValid() {
        if (selectedInventory.getSupplier() != null) {
            return BusinessEntityUtils.validateText(selectedInventory.getSupplier().getName());
        }

        return false;
    }

    public Boolean getIsCategoryNameValid() {
        if (selectedInventory.getCategory() != null) {
            return BusinessEntityUtils.validateText(selectedInventory.getInventoryCategory().getName());
        }

        return false;
    }

    public boolean hasSelectedPRs() {
        return selectedInventories != null && !this.selectedInventories.isEmpty();
    }

    public List<Inventory> getSelectedInventories() {
        return selectedInventories;
    }

    public void setSelectedInventories(List<Inventory> selectedInventories) {
        this.selectedInventories = selectedInventories;
    }

    public List<InventoryRequisition> getSelectedInventoryRequisitions() {
        return selectedInventoryRequisitions;
    }

    public void setSelectedInventoryRequisitions(List<InventoryRequisition> selectedInventoryRequisitions) {
        this.selectedInventoryRequisitions = selectedInventoryRequisitions;
    }

    @Override
    public String getApplicationHeader() {

        return "Inventory Manager";

    }

    @Override
    public String getApplicationSubheader() {
        String subHeader;

        subHeader = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(),
                "applicationSubheader");

        if (subHeader != null) {
            if (subHeader.trim().equals("None")) {
                return getEmployee().getDepartment().getName();
            }
        } else {
            subHeader = "";
        }

        return subHeader;
    }

    /**
     * Gets the title of the application which may be saved in a database.
     *
     * @return
     */
    public String getTitle() {
        return "Inventory Manager";
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Inventory", "Inventory"));
        searchTypes.add(new SelectItem("Inventory Products", "Inventory Products"));
        searchTypes.add(new SelectItem("Inventory Requisitions", "Inventory Requisitions"));

        return searchTypes;
    }

    public String formatAsCurrency(Double value, String symbol) {
        return NumberUtils.formatAsCurrency(value, symbol);
    }

    public void openInventoryTab() {
        getMainTabView().openTab("Inventory");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventorySearchButton");
    }

    public void openInventoryRequisitionTab() {
        getMainTabView().openTab("Inventory Requisitions");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:inventoryRequisitionSearchButton");
    }

    public void updateCost() {

        updateInventory(null);
    }

    public void updateInventory(AjaxBehaviorEvent event) {
        getSelectedInventory().setIsDirty(true);
        getSelectedInventory().setEditStatus("(edited)");
        getSelectedInventory().setName(getSelectedInventory().getProduct().getName());

        if (!getSelectedInventory().getProduct().getCategories().isEmpty()) {
            getSelectedInventory().
                    setInventoryCategory(getSelectedInventory().getProduct().getCategories().get(0));
        }

        getSelectedInventory().addAction(BusinessEntity.Action.EDIT);

    }

    public void updateInventoryUnitCost(AjaxBehaviorEvent event) {

        updateInventory(event);

    }

    public void updateInventoryQuantity(AjaxBehaviorEvent event) {

        updateInventory(event);

    }

    public void updateInventoryRequisition(AjaxBehaviorEvent event) {
        getSelectedInventoryRequisition().setIsDirty(true);
        getSelectedInventoryRequisition().setEditStatus("(edited)");

        getSelectedInventoryRequisition().addAction(BusinessEntity.Action.EDIT);

    }

    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    public Inventory getSelectedInventory() {
        if (selectedInventory == null) {
            selectedInventory = new Inventory();
        }
        return selectedInventory;
    }

    public void setSelectedInventory(Inventory selectedInventory) {

        this.selectedInventory = selectedInventory;
    }

    public Inventory getSavedInventory(Inventory pr) {
        int i = 0;
        Inventory foundInvntry = Inventory.findById(getEntityManager1(), pr.getId());
        for (Inventory inventory : foundInventories) {
            if (Objects.equals(inventory.getId(), foundInvntry.getId())) {
                foundInventories.set(i, foundInvntry);
                break;
            }
            ++i;
        }

        return foundInvntry;
    }

    public void saveInventory(
            Inventory inventory,
            String msgSavedSummary,
            String msgSavedDetail) {

        EntityManager em = getEntityManager1();

        ReturnMessage returnMessage = inventory.prepareAndSave(em, getUser());

        if (returnMessage.isSuccess()) {
            PrimeFacesUtils.addMessage(msgSavedSummary, msgSavedDetail, FacesMessage.SEVERITY_INFO);
            inventory.setEditStatus(" ");

            processInventoryActions(inventory);
        } else {
            PrimeFacesUtils.addMessage(returnMessage.getHeader(),
                    returnMessage.getMessage(),
                    FacesMessage.SEVERITY_ERROR);

            MailUtils.sendErrorEmail("An error occurred while saving an inventory! - "
                    + returnMessage.getHeader(),
                    "Inventory: " + inventory.getProduct().getName()
                    + "\nJMTS User: " + getUser().getUsername()
                    + "\nDate/time: " + new Date()
                    + "\nDetail: " + returnMessage.getDetail(),
                    em);
        }

    }

    public void saveSelectedInventory() {
        saveInventory(getSelectedInventory(),
                "Saved", "Inventory was saved");
    }

    public void saveInventoryRequisition(
            InventoryRequisition inventoryRequisition,
            String msgSavedSummary,
            String msgSavedDetail) {

        EntityManager em = getEntityManager1();

        ReturnMessage returnMessage = inventoryRequisition.prepareAndSave(em, getUser());

        if (returnMessage.isSuccess()) {
            PrimeFacesUtils.addMessage(msgSavedSummary, msgSavedDetail, FacesMessage.SEVERITY_INFO);
            inventoryRequisition.setEditStatus(" ");

            processInventoryRequisitionActions(inventoryRequisition);

        } else {
            PrimeFacesUtils.addMessage(returnMessage.getHeader(),
                    returnMessage.getMessage(),
                    FacesMessage.SEVERITY_ERROR);

            MailUtils.sendErrorEmail("An error occurred while saving an inventory requisition! - "
                    + returnMessage.getHeader(),
                    "Inventory Requisition: " + inventoryRequisition.getCode()
                    + "\nJMTS User: " + getUser().getUsername()
                    + "\nDate/time: " + new Date()
                    + "\nDetail: " + returnMessage.getDetail(),
                    em);
        }

    }

    public void saveSelectedInventoryRequisition() {
        saveInventoryRequisition(getSelectedInventoryRequisition(),
                "Saved", "Inventory Requisition was saved");
    }

    private void notifyUserReInventory(EntityManager em,
            Inventory inventory,
            User user,
            String action) {

        Notification notification
                = new Notification("The inventory (#" + inventory.getName()
                        + ") was " + action);
        notification.setType("InventorySearch");
        notification.setSubject("Inventory " + action);
        notification.setMessage(inventory.getId().toString());
        notification.setOwnerId(user.getId());
        notification.save(em);
    }

    private void emailDepartmentHead(EntityManager em,
            Inventory inventory, String action) {

        Employee head = inventory.getEnteredBy().getDepartment().getHead();

        if (head != null) {

            sendInventoryEmail(em, inventory, head, "a department head", action);
        }
    }

    private void notifyDepartmentHead(EntityManager em,
            Inventory inventory,
            String action) {

        Employee head = inventory.getEditedBy().getDepartment().getHead();

        if (head != null) {
            User user = User.findActiveJobManagerUserByEmployeeId(em,
                    head.getId());

            notifyUserReInventory(em, inventory, user, action);

        }
    }

    private void sendInventoryEmail(
            EntityManager em,
            Inventory inventory,
            Employee employee,
            String role,
            String action) {

        // tk Template to be created
        Email email = Email.findActiveEmailByName(em, "ims-email-template");

        if (email != null) {
            String prId = inventory.getId().toString();
            String department = inventory.getEditedBy().getDepartment().getName();
            String JMTSURL = (String) SystemOption.getOptionValueObject(em, "appURL");
            String originator = inventory.getEditedBy().getFirstName()
                    + " " + inventory.getEditedBy().getLastName();
            String dateEdited = BusinessEntityUtils.
                    getDateInMediumDateFormat(inventory.getDateEdited());
            String status = inventory.getStatus();

            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    employee.getInternet().getEmail1(),
                    email.getSubject().
                            replace("{action}", action).
                            replace("{inventoryId}", prId),
                    email.getContent("/correspondences/").
                            replace("{title}",
                                    employee.getTitle()).
                            replace("{surname}",
                                    employee.getLastName()).
                            replace("{JMTSURL}", JMTSURL).
                            replace("{inventoryId}", prId).
                            replace("{originator}", originator).
                            replace("{department}", department).
                            replace("{dateEdited}", dateEdited).
                            replace("{role}", role).
                            replace("{action}", action).
                            replace("{status}", status),
                    email.getContentType(),
                    em);
        }
    }

    private synchronized void processInventoryActions(Inventory inventory) {

        if (inventory.getId() != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        doProcessInventoryActions(inventory);
                    } catch (Exception e) {
                        System.out.println("Error processing PR actions: " + e);
                    }
                }

            }.start();
        }
    }

    private synchronized void doProcessInventoryActions(Inventory inventory) {

        for (BusinessEntity.Action action : inventory.getActions()) {
            switch (action) {
                case CREATE:
                    System.out.println("Processing CREATE action...");
                    notifyDepartmentHead(
                            getSystemManager().getEntityManager1(),
                            inventory, "created");
                    emailDepartmentHead(
                            getSystemManager().getEntityManager1(),
                            inventory, "created");
                    break;
                case EDIT:
                    System.out.println("EDIT action received but not processed.");
                    break;
                case APPROVE:
                    System.out.println("Processing APPROVE action...");
                    break;
                case RECOMMEND:
                    System.out.println("Processing RECOMMEND action...");
                    break;
                case COMPLETE:
                    System.out.println("COMPLETE action received but not processed.");
                    break;
                default:
                    break;
            }
        }

        inventory.getActions().clear();

    }

    private synchronized void processInventoryRequisitionActions(
            InventoryRequisition inventoryRequisition) {

        if (inventoryRequisition.getId() != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        doProcessInventoryRequisitionActions(inventoryRequisition);
                    } catch (Exception e) {
                        System.out.println("Error processing PR actions: " + e);
                    }
                }

            }.start();
        }
    }

    private synchronized void doProcessInventoryRequisitionActions(
            InventoryRequisition inventoryRequisition) {

        for (BusinessEntity.Action action : inventoryRequisition.getActions()) {
            switch (action) {
                case CREATE:
                    System.out.println("Processing CREATE action...");
                    break;
                case EDIT:
                    System.out.println("EDIT action received but not processed.");
                    break;
                case APPROVE:
                    System.out.println("Processing APPROVE action...");
                    break;
                case RECOMMEND:
                    System.out.println("Processing RECOMMEND action...");
                    break;
                case COMPLETE:
                    System.out.println("COMPLETE action received but not processed.");
                    break;
                default:
                    break;
            }
        }

        inventoryRequisition.getActions().clear();

    }

    public void editInventoryProduct() {
        setSelectedInventoryProduct(getSelectedInventory().getProduct());

        openInventoryProductDialog();
    }

    public void onInventoryCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundInventories().get(event.getRowIndex()));
    }

    public int getNumOfInventoryFound() {
        return getFoundInventories().size();
    }

    public String getInventoryTableHeader() {

        return "Search Results (found: " + getNumOfInventoryFound() + ")";
    }

    public int getNumOfInventoryRequisitionsFound() {
        return getFoundInventoryRequisitions().size();
    }

    public String getInventoryRequisitionTableHeader() {

        return "Search Results (found: " + getNumOfInventoryRequisitionsFound() + ")";
    }

    public void inventoryDialogReturn() {

        if (getSelectedInventory().getIsDirty()) {
            PrimeFacesUtils.addMessage("Inventory NOT saved",
                    "The recently edited inventory was not saved",
                    FacesMessage.SEVERITY_WARN);
            PrimeFaces.current().ajax().update("headerForm:growl3");

        } else {
            if (!getSearchText().isEmpty()) {
                doInventorySearch();
            }
        }
    }

    public void inventoryRequisitionDialogReturn() {

        if (getSelectedInventoryRequisition().getIsDirty()) {
            PrimeFacesUtils.addMessage("Inventory Requisition NOT Saved",
                    "The recently edited inventory requisition was not saved",
                    FacesMessage.SEVERITY_WARN);
            PrimeFaces.current().ajax().update("headerForm:growl3");

        } else {
            if (!getSearchText().isEmpty()) {
                doInventoryRequisitionSearch();
            }
        }
    }

    public void editSelectedInventory() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width("700px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("inventoryDialog", options, null);

    }

    public void editSelectedInventoryRequisition() {

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

        PrimeFaces.current().dialog().openDynamic("inventoryRequisitionDialog", options, null);

    }

    public List<Inventory> getFoundInventories() {
        if (foundInventories == null) {
            //doInventorySearch();
            foundInventories = new ArrayList<>();
        }
        return foundInventories;
    }

    public Double getFoundInventoriesTotalQuantity() {
        Double totalQuantity = 0.0;

        for (Inventory foundInventory : foundInventories) {
            totalQuantity = totalQuantity + foundInventory.getTotalCostComponentQuantities();
        }

        return totalQuantity;
    }

    public Double getFoundInventoriesTotalCost() {
        Double totalCost = 0.0;

        for (Inventory foundInventory : foundInventories) {
            totalCost = totalCost + foundInventory.getTotalCostComponentCosts();
        }

        return totalCost;
    }

    public List<Inventory> getFoundProducts() {
        foundInventories = Inventory.find(
                getEntityManager1(),
                "", 0);

        return foundInventories;
    }

    public void setFoundInventories(List<Inventory> foundInventories) {
        this.foundInventories = foundInventories;
    }

    public List<InventoryRequisition> getFoundInventoryRequisitions() {
        if (foundInventoryRequisitions == null) {
            //doInventoryRequisitionSearch();
            foundInventoryRequisitions = new ArrayList<>();
        }

        return foundInventoryRequisitions;
    }

    public void setFoundInventoryRequisitions(List<InventoryRequisition> foundInventoryRequisitions) {
        this.foundInventoryRequisitions = foundInventoryRequisitions;
    }

    public void doInventorySearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Inventory",
                getInventorySearchText(),
                null,
                null);
    }

    public void doInventoryRequisitionSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Inventory Requisitions",
                getInventoryRequisitionSearchText(),
                null,
                null);
    }

    public void doInventoryRequisitionSearch(DatePeriod dateSearchPeriod, String searchType, String searchText) {

        setDateSearchPeriod(dateSearchPeriod);
        setSearchType(searchType);
        setSearchText(searchText);

        doInventoryRequisitionSearch();
    }

    public List<SelectItem> getInventoryDisbursementMethods() {

        return getStringListAsSelectItems(
                getSystemManager().getEntityManager1(),
                "inventoryDisbursementMethods");
    }

    public void createNewInventory() {

        selectedInventory = new Inventory();
        selectedInventory.setType("None");
        selectedInventory.setDateAcquired(new Date());
        selectedInventory.setMeasurementUnit("each");
        selectedInventory.setDisbursementMethod(SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "defaultInventoryDisbursementMethod"));
        selectedInventory.setLowStockThreshold(SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "defaultLowStockThreshold"));

        openInventoryTab();

        editSelectedInventory();
    }

    public void createNewInventoryRequisition() {

        selectedInventoryRequisition = new InventoryRequisition();
        selectedInventoryRequisition.setType("None");
        selectedInventoryRequisition.setEnteredBy(getEmployee());
        selectedInventoryRequisition.setEditedBy(getEmployee());
        selectedInventoryRequisition.setDateEntered(new Date());
        selectedInventoryRequisition.setDateEdited(new Date());
        selectedInventoryRequisition.setDateOfRequisition(new Date());
        selectedInventoryRequisition.setWorkProgress("Ongoing");

        openInventoryRequisitionTab();

        editSelectedInventoryRequisition();
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    @Override
    public MainTabView getMainTabView() {

        return getFinanceManager().getMainTabView();
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

    @Override
    public void reset() {
        super.reset();

        setSearchType("Inventory");
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "dateEdited", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();
        inventoryProductSearchText = "";
        inventoryRequisitionSearchText = "";
        inventorySearchText = "";
        activeInventoryOnly = true;
        activeInventoryProductsOnly = true;

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));

        // tk
        bannerView = new BannerView();
    }

    @Override
    public EntityManager getEntityManager1() {
        return getFinanceManager().getEntityManager1();
    }

    @Override
    public EntityManager getEntityManager2() {
        return getFinanceManager().getEntityManager2();
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Inventory");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Inventory":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Inventory Products":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;

            case "Inventory Requisitions":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    public List<Inventory> getFoundActiveInventories() {
        if (foundActiveInventories == null) {
            foundActiveInventories = Inventory.findActive(
                    getEntityManager1(),
                    "", 0);
        }

        return foundActiveInventories;
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
            case "Inventory":
                if (getActiveInventoryOnly()) {
                    foundInventories = Inventory.findActive(
                            getEntityManager1(),
                            searchText, 0);
                } else {
                    foundInventories = Inventory.find(
                            getEntityManager1(),
                            searchText, 0);
                }

//                if (startDate != null) {
//                    openInventoryTab();
//                }
                break;
            case "Inventory Products":
                if (getActiveInventoryProductsOnly()) {
                    foundInventoryProducts = MarketProduct.findActiveMarketProductsByNameAndType(
                            getEntityManager1(), searchText, "Inventory");
                } else {
                    foundInventoryProducts = MarketProduct.findMarketProductsByNameAndType(
                            getEntityManager1(), searchText, "Inventory");
                }

//                if (startDate != null) {
//                    openInventoryProductBrowser();
//                }
                break;
            case "Inventory Requisitions":
                foundInventoryRequisitions = InventoryRequisition.find(
                        getEntityManager1(),
                        searchText, 0);
//                if (startDate != null) {
//                    openInventoryRequisitionTab();
//                }
                break;
            default:
                break;
        }
    }

}
