/*
Inventory Management
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
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import org.primefaces.event.CellEditEvent;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import org.primefaces.PrimeFaces;
import java.util.Objects;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItemGroup;
import javax.naming.ldap.LdapContext;
import jm.com.dpbennett.business.entity.fm.CostComponent;
import jm.com.dpbennett.business.entity.fm.Inventory;
import jm.com.dpbennett.business.entity.fm.InventoryDisbursement;
import jm.com.dpbennett.business.entity.fm.InventoryRequisition;
import jm.com.dpbennett.business.entity.fm.MarketProduct;
import jm.com.dpbennett.business.entity.pm.PurchaseRequisition;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.business.entity.util.NumberUtils;
import jm.com.dpbennett.sm.manager.Manager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.FinancialUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.TabPanel;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 *
 * @author Desmond Bennett
 */
public class InventoryManager implements Serializable, Manager {

    private Inventory selectedInventory;
    private InventoryRequisition selectedInventoryRequisition;
    private MarketProduct selectedInventoryProduct;
    private CostComponent selectedCostComponent;
    private List<Inventory> selectedInventories;
    private List<InventoryRequisition> selectedInventoryRequisitions;
    private InventoryDisbursement selectedInventoryDisbursement;
    private Boolean edit;
    private String searchText;
    private String inventoryProductSearchText;
    private List<Inventory> foundInventories;
    private List<InventoryRequisition> foundInventoryRequisitions;
    private List<MarketProduct> foundInventoryProducts;
    private String searchType;
    private DatePeriod dateSearchPeriod;
    private Boolean isActiveInventoryProductsOnly;
    private ArrayList<SelectItem> groupedSearchTypes;
    private ArrayList<SelectItem> allDateSearchFields;

    /**
     * Creates a new instance of InventoryManager
     */
    public InventoryManager() {
        init();
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 600;
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

        getSelectedInventoryRequisition().getAllSortedInventoryDisbursements().add(selectedInventoryDisbursement);

        updateInventoryRequisition(null);

        FacesMessage msg = new FacesMessage("New  Disbursement Added",
                "Click on the pencil icon to edit");

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public InventoryRequisition getSelectedInventoryRequisition() {
        return selectedInventoryRequisition;
    }

    public void setSelectedInventoryRequisition(InventoryRequisition selectedInventoryRequisition) {
        this.selectedInventoryRequisition = selectedInventoryRequisition;
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

    public void deleteDisbursement() {
        deleteDisbursementByProductName(selectedInventoryDisbursement.
                getInventory().getProduct().getCommonName());
    }

    public void deleteSelectedDisbursement() {
        deleteDisbursementByProductName(selectedInventoryDisbursement.
                getInventory().getProduct().getCommonName());
    }

    public void deleteDisbursementByProductName(String productName) {

        List<InventoryDisbursement> disbusements = getSelectedInventoryRequisition().getAllSortedInventoryDisbursements();
        int index = 0;
        for (InventoryDisbursement disbursement : disbusements) {
            if (disbursement.getInventory().getProduct().getCommonName().equals(productName)) {
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

    public void updateDisbursement(InventoryDisbursement inventoryDisbursement) {

        inventoryDisbursement.update();

        inventoryDisbursement.setIsDirty(true);

    }

    public void updateCostType() {
        //updateCostType(selectedCostComponent);
        selectedCostComponent.update();
    }

    public void updateCostComponent(CostComponent costComponent) {

        costComponent.update();

        costComponent.setIsDirty(true);

    }

    public List getCostTypeList() {
        return FinancialUtils.getCostTypeList(getEntityManager1());
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

    public void onCostComponentRowEdit(RowEditEvent<CostComponent> event) {

        updateCostComponent(event.getObject());
        updateInventory(null);
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
                getSelectedInventory().getProduct().getCommonName(),
                getSelectedInventory().getCode(),
                "Purchase");

        getSelectedInventory().getAllSortedCostComponents().add(selectedCostComponent);

        updateInventory(null);

        FacesMessage msg = new FacesMessage("New Cost Component Added",
                "Click on the pencil icon to edit");

        FacesContext.getCurrentInstance().addMessage(null, msg);
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
        getSystemManager().setSelectedCategory(new Category());
        getSystemManager().getSelectedCategory().setType("Product");

        getSystemManager().editCategory();
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

    public void setIsActiveInventoryProductsOnly(Boolean isActiveInventoryProductsOnly) {
        this.isActiveInventoryProductsOnly = isActiveInventoryProductsOnly;
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

    public Boolean getIsActiveInventoryProductsOnly() {
        return isActiveInventoryProductsOnly;
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
            foundInventoryProducts = MarketProduct.findAllActiveMarketProductsByType(
                    getEntityManager1(), "Inventory");
        }

        return foundInventoryProducts;
    }

    public void doInventoryProductSearch() {

        if (getIsActiveInventoryProductsOnly()) {
            foundInventoryProducts = MarketProduct.findActiveMarketProductsByNameAndType(
                    getEntityManager1(), inventoryProductSearchText, "Inventory");
        } else {
            foundInventoryProducts = MarketProduct.findMarketProductsByNameAndType(
                    getEntityManager1(), inventoryProductSearchText, "Inventory");
        }

    }

    public void openInventoryProductDialog() {
        PrimeFacesUtils.openDialog(null, "/finance/ims/inventoryProductDialog",
                true, true, true, true, getDialogHeight(), getDialogWidth());
    }

    public void openInventoryProductBrowser() {

        getSystemManager().getMainTabView().openTab("Inventory Products");
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

        // This ensures that the product name field gets updated with the common name, brand and model.
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

    public Boolean getIsInventoryProductNameValid() {
        return BusinessEntityUtils.validateName(
                getSelectedInventory().getProduct().getName());
    }

    public PurchasingManager getPurchasingManager() {
        return BeanUtils.findBean("purchasingManager");
    }

    public FinanceManager getFinanceManager() {
        return BeanUtils.findBean("financeManager");
    }

    public void editInventorySupplier() {
        getPurchasingManager().setSelectedSupplier(getSelectedInventory().getSupplier());

        getPurchasingManager().editSelectedSupplier();
    }

    public void editInventoryCategory() {
        getSystemManager().setSelectedCategory(getSelectedInventory().getCategory());

        getSystemManager().editCategory();
    }

    public void inventorySupplierDialogReturn() {
        if (getPurchasingManager().getSelectedSupplier().getId() != null) {
            getSelectedInventory().setSupplier(getPurchasingManager().getSelectedSupplier());
            updateInventory(null);
        }
    }

    public void inventoryCategoryDialogReturn() {
        if (getSystemManager().getSelectedCategory().getId() != null) {
            getSelectedInventory().setCategory(getSystemManager().getSelectedCategory());
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
            return BusinessEntityUtils.validateName(selectedInventory.getSupplier().getName());
        }

        return false;
    }

    public Boolean getIsCategoryNameValid() {
        if (selectedInventory.getCategory() != null) {
            return BusinessEntityUtils.validateName(selectedInventory.getCategory().getName());
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

    /**
     * Gets the title of the application which may be saved in a database.
     *
     * @return
     */
    public String getTitle() {
        return "Inventory Manager";
    }

    /**
     * Gets the general search text.
     *
     * @return
     */
    @Override
    public String getSearchText() {
        return searchText;
    }

    /**
     * Sets the general search text.
     *
     * @param searchText
     */
    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getRenderDateSearchFields() {
        switch (searchType) {
            case "Inventory":
                return "false";
            default:
                return "true";
        }
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Inventory", "Inventory"));

        return searchTypes;
    }

    @Override
    public void doSearch() {
        for (Modules activeModule : getUser().getActiveModules()) {

            Manager manager = getManager(activeModule.getName());
            if (manager != null) {
                manager.doDefaultSearch(
                        getDateSearchPeriod().getDateField(),
                        getSearchType(),
                        getSearchText(),
                        getDateSearchPeriod().getStartDate(),
                        getDateSearchPeriod().getEndDate());
            }

        }
    }

    @Override
    public void updateDateSearchField() {
    }

    @Override
    public DatePeriod getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    @Override
    public void setDateSearchPeriod(DatePeriod dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
    }

    public String formatAsCurrency(Double value, String symbol) {
        return NumberUtils.formatAsCurrency(value, symbol);
    }

    public void openInventoryTab() {
        getMainTabView().openTab("Inventory");
    }

    public void openInventoryRequisitionTab() {
        getMainTabView().openTab("Inventory Requisitions");
    }

    public void updateCost() {

        updateInventory(null);
    }

    public void updateInventory(AjaxBehaviorEvent event) {
        getSelectedInventory().setIsDirty(true);
        getSelectedInventory().setEditStatus("(edited)");

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

        if (inventory.getIsDirty()) {
            ReturnMessage returnMessage;

            returnMessage = inventory.prepareAndSave(em, getUser());

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
        } else {
            PrimeFacesUtils.addMessage("Already Saved",
                    "This inventory was not saved because it was not modified or it was recently saved",
                    FacesMessage.SEVERITY_INFO);
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

        if (inventoryRequisition.getIsDirty()) {
            ReturnMessage returnMessage;

            returnMessage = inventoryRequisition.prepareAndSave(em, getUser());

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
        } else {
            PrimeFacesUtils.addMessage("Already Saved",
                    "This inventory requisition was not saved because it was not modified or it was recently saved",
                    FacesMessage.SEVERITY_INFO);
        }

    }

    public void saveSelectedInventoryRequisition() {
        saveInventoryRequisition(getSelectedInventoryRequisition(),
                "Saved", "Inventory was saved");
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
            String prId = inventory.getId().toString(); // tk
            String department = inventory.getEditedBy().getDepartment().getName();
            String JMTSURL = (String) SystemOption.getOptionValueObject(em, "appURL");
            String originator = inventory.getEditedBy().getFirstName()
                    + " " + inventory.getEditedBy().getLastName();
            String dateEdited = BusinessEntityUtils.
                    getDateInMediumDateFormat(inventory.getDateEdited());
            String status = inventory.getStatus();

            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
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

        EntityManager em = getEntityManager1();

        if (inventory.getId() != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        doProcessInventoryActions(em, inventory);
                    } catch (Exception e) {
                        System.out.println("Error processing PR actions: " + e);
                    }
                }

            }.start();
        }
    }

    private synchronized void doProcessInventoryActions(EntityManager em,
            Inventory inventory) {

        for (BusinessEntity.Action action : inventory.getActions()) {
            switch (action) {
                case CREATE:
                    System.out.println("Processing CREATE action...");
                    notifyDepartmentHead(em, inventory, "created");
                    emailDepartmentHead(em, inventory, "created");
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
            PrimeFaces.current().ajax().update("appForm:growl3");

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
            PrimeFaces.current().ajax().update("appForm:growl3");

        } else {
            if (!getSearchText().isEmpty()) {
                doInventoryRequisitionSearch();
            }
        }
    }

    public void editSelectedInventory() {

        PrimeFacesUtils.openDialog(null, "inventoryDialog",
                true, true, true, true, getDialogHeight(), getDialogWidth());
    }

    public void editSelectedInventoryRequisition() {

        PrimeFacesUtils.openDialog(null, "inventoryRequisitionDialog",
                true, true, true, true, getDialogHeight(), getDialogWidth() + 200);
    }

    public List<Inventory> getFoundInventories() {
        if (foundInventories == null) {
            doInventorySearch();
        }
        return foundInventories;
    }

    public void setFoundInventories(List<Inventory> foundInventories) {
        this.foundInventories = foundInventories;
    }

    public List<InventoryRequisition> getFoundInventoryRequisitions() {
        if (foundInventoryRequisitions == null) {
            doInventoryRequisitionSearch();
        }
        return foundInventoryRequisitions;
    }

    public void setFoundInventoryRequisitions(List<InventoryRequisition> foundInventoryRequisitions) {
        this.foundInventoryRequisitions = foundInventoryRequisitions;
    }

    public void doInventorySearch() {

        EntityManager em = getEntityManager1();

        foundInventories = Inventory.find(em, searchText, 0);
    }

    public void doInventorySearch(
            DatePeriod dateSearchPeriod,
            String searchType,
            String searchText) {

        this.dateSearchPeriod = dateSearchPeriod;
        this.searchType = searchType;
        this.searchText = searchText;

        doInventorySearch();
    }

    public void doInventoryRequisitionSearch() {

        EntityManager em = getEntityManager1();

        foundInventoryRequisitions = InventoryRequisition.find(em, searchText, 0);

    }

    public void doInventoryRequisitionSearch(DatePeriod dateSearchPeriod, String searchType, String searchText) {

        this.dateSearchPeriod = dateSearchPeriod;
        this.searchType = searchType;
        this.searchText = searchText;

        doInventoryRequisitionSearch();
    }

    public void createNewInventory() {

        selectedInventory = new Inventory();
        selectedInventory.setType("None");
        selectedInventory.setDateAcquired(new Date());
        selectedInventory.setMeasurementUnit("each");

        openInventoryTab();

        editSelectedInventory();
    }

    public void createNewInventoryRequisition() {

        selectedInventoryRequisition = new InventoryRequisition();
        selectedInventoryRequisition.setType("None");
        selectedInventoryRequisition.setDateEntered(new Date());
        selectedInventoryRequisition.setDateEdited(new Date());

        openInventoryRequisitionTab();

        editSelectedInventoryRequisition();
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    /**
     * Gets the SystemManager object as a session bean.
     *
     * @return
     */
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public MainTabView getMainTabView() {

        return getSystemManager().getMainTabView();
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

    @Override
    public void reset() {
        searchType = "Inventory";
        dateSearchPeriod = new DatePeriod("This year", "year",
                "dateEdited", null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();
        searchText = "";
        inventoryProductSearchText = "";
        isActiveInventoryProductsOnly = true;
        groupedSearchTypes = new ArrayList<>();
        allDateSearchFields = new ArrayList();
    }

    @Override
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    @Override
    public User getUser() {

        return getFinanceManager().getUser();
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
    }

    @Override
    public String getSearchType() {
        return searchType;
    }

    @Override
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Inventory");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getGroupedSearchTypes() {
        return groupedSearchTypes;
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
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void doDefaultSearch(
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {
        //  etInventoryManager().doInventorySearch(dateSearchPeriod, searchType, searchText);
        //  getInventoryManager().openInventoryTab();
        //            case "Inventory requisitions":
        //                getInventoryManager().doInventoryRequisitionSearch(dateSearchPeriod, searchType, searchText);
        //                getInventoryManager().openInventoryRequisitionTab();
        //                break;
        switch (searchType) {
            case "Inventory":
                doInventorySearch(dateSearchPeriod, searchType, searchText);
                openInventoryTab();
                break;
            default:
                break;
        }
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
    public void logout() {
        getUser().logActivity("Logged out", getEntityManager1());
        reset();
        completeLogout();
    }

    @Override
    public void initSearchPanel() {
        initSearchTypes();
        updateSearchType();
    }

    @Override
    public void initSearchTypes() {
        groupedSearchTypes.clear();

        for (Modules activeModule : getUser().getActiveModules()) {
            Manager manager = getManager(activeModule.getName());
            if (manager != null) {
                groupedSearchTypes.add(manager.getSearchTypesGroup());
            }
        }
    }

    @Override
    public Manager getManager(String name) {
        return BeanUtils.findBean(name);
    }

    @Override
    public ArrayList<SelectItem> getDatePeriods() {
        ArrayList<SelectItem> datePeriods = new ArrayList<>();

        for (String name : DatePeriod.getDatePeriodNames()) {
            datePeriods.add(new SelectItem(name, name));
        }

        return datePeriods;
    }

    @Override
    public ArrayList<SelectItem> getAllDateSearchFields() {
        return allDateSearchFields;
    }

    @Override
    public void updateSearchType() {
        for (Modules activeModule : getUser().getActiveModules()) {
            Manager manager = getManager(activeModule.getName());
            if (manager != null) {
                allDateSearchFields = manager.getDateSearchFields(searchType);
            }
        }
    }

    @Override
    public void completeLogin() {
        getUser().logActivity("Logged in", getEntityManager1());

        getUser().save(getEntityManager1());

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initDashboard();
        initMainTabView();
        updateAllForms();
    }

    @Override
    public void completeLogout() {
        getDashboard().removeAllTabs();
        getMainTabView().removeAllTabs();
    }

    public Dashboard getDashboard() {
        return getSystemManager().getDashboard();
    }

    @Override
    public void initDashboard() {
        initSearchPanel();
    }

    @Override
    public void initMainTabView() {
        getMainTabView().reset(getUser());

        for (Modules activeModule : getUser().getActiveModules()) {
            getMainTabView().openTab(activeModule.getDashboardTitle());
        }
    }

    @Override
    public void updateAllForms() {
        PrimeFaces.current().ajax().update("appForm");
    }

    @Override
    public void onMainViewTabClose(TabCloseEvent event) {
        String tabId = ((TabPanel) event.getData()).getId();

        getMainTabView().closeTab(tabId);
    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {
        String tabTitle = event.getTab().getTitle();

        switch (tabTitle) {
            case "Inventory":
                break;
            default:
                break;
        }
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "appShortcutIconURL");
    }

    @Override
    public Boolean renderUserMenu() {
        return getUser().getId() != null;
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURL");
    }

    @Override
    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageHeight");
    }

    @Override
    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageWidth");
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

    private void handleSelectedNotification(Notification notification) {

        switch (notification.getType()) {
            case "PRSearch": // tk
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
    public void login() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Integer getLoginAttempts() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLoginAttempts(Integer loginAttempts) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean getUserLoggedIn() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setUserLoggedIn(Boolean userLoggedIn) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getUsername() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setUsername(String username) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public User getUser(EntityManager em) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setUser(User user) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean checkForLDAPUser(EntityManager em, String username, LdapContext ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean validateUser(EntityManager em) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void checkLoginAttemps() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void login(EntityManager em) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getLogonMessage() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLogonMessage(String logonMessage) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
