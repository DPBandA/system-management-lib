/*
Purchase Management
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.dm.Attachment;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.fm.CostComponent;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.EmployeePosition;
import jm.com.dpbennett.business.entity.hrm.Internet;
import jm.com.dpbennett.business.entity.pm.PurchaseRequisition;
import jm.com.dpbennett.business.entity.pm.Supplier;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.primefaces.PrimeFaces;
import java.util.Calendar;
import java.util.Objects;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItemGroup;
import jm.com.dpbennett.business.entity.fm.CashPayment;
import jm.com.dpbennett.business.entity.fm.Currency;
import jm.com.dpbennett.business.entity.hrm.Division;
import jm.com.dpbennett.business.entity.pm.PurchaseOrderNumber;
import jm.com.dpbennett.business.entity.pm.PurchaseReqNumber;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.business.entity.util.NumberUtils;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.FinancialUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author Desmond Bennett
 */
public class PurchasingManager extends GeneralManager implements Serializable {

    private CostComponent selectedCostComponent;
    private CashPayment selectedCashPayment;
    private PurchaseRequisition selectedPurchaseRequisition;
    private List<PurchaseRequisition> selectedPurchaseRequisitions;
    private Employee selectedApprover;
    private Boolean edit;
    private String purchaseReqSearchText;
    private List<PurchaseRequisition> foundPurchaseReqs;
    private List<PurchaseRequisition> procurementTasks;
    private Long searchDepartmentId;
    private List<Employee> toEmployees;
    private String purchaseReqEmailSubject;
    private String purchaseReqEmailContent;
    private Supplier selectedSupplier;
    private Contact selectedSupplierContact;
    private Address selectedSupplierAddress;
    private String supplierSearchText;
    private Boolean isActiveSuppliersOnly;
    private List<Supplier> foundSuppliers;
    private Attachment selectedAttachment;
    private UploadedFile uploadedFile;
    private List<CashPayment> cashPayments;
    private PurchaseReqNumber lastPurchaseReqNumber;
    private PurchaseOrderNumber lastPurchaseOrderNumber;
    private SystemOption resetPOSequenceNumberYearly;
    private SystemOption resetPRSequenceNumberYearly;

    public PurchasingManager() {
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

    public void saveSequenceNumbers() {

        EntityManager em = getEntityManager1();

        lastPurchaseReqNumber.save(em);
        lastPurchaseOrderNumber.save(em);
        resetPRSequenceNumberYearly.save(em);
        resetPOSequenceNumberYearly.save(em);

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void editSequenceNumbers() {
        EntityManager em = getEntityManager1();

        setLastPurchaseReqNumber(PurchaseReqNumber.findLastPurchaseReqNumber(
                em, BusinessEntityUtils.getCurrentYear()));

        setLastPurchaseOrderNumber(PurchaseOrderNumber.findLastPurchaseOrderNumber(
                em, BusinessEntityUtils.getCurrentYear()));

        setResetPOSequenceNumberYearly(SystemOption.findSystemOptionByName(
                em, "resetPOSequenceNumberYearly"));

        setResetPRSequenceNumberYearly(SystemOption.findSystemOptionByName(
                em, "resetPRSequenceNumberYearly"));

        openSequenceNumbersDialog();
    }

    public void openSequenceNumbersDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() - 350) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/finance/sequenceNumbersDialog", options, null);

    }

    public SystemOption getResetPOSequenceNumberYearly() {
        return resetPOSequenceNumberYearly;
    }

    public void setResetPOSequenceNumberYearly(SystemOption resetPOSequenceNumberYearly) {
        this.resetPOSequenceNumberYearly = resetPOSequenceNumberYearly;
    }

    public SystemOption getResetPRSequenceNumberYearly() {
        return resetPRSequenceNumberYearly;
    }

    public void setResetPRSequenceNumberYearly(SystemOption resetPRSequenceNumberYearly) {
        this.resetPRSequenceNumberYearly = resetPRSequenceNumberYearly;
    }

    public PurchaseReqNumber getLastPurchaseReqNumber() {
        return lastPurchaseReqNumber;
    }

    public void setLastPurchaseReqNumber(PurchaseReqNumber lastPurchaseReqNumber) {
        this.lastPurchaseReqNumber = lastPurchaseReqNumber;
    }

    public PurchaseOrderNumber getLastPurchaseOrderNumber() {
        return lastPurchaseOrderNumber;
    }

    public void setLastPurchaseOrderNumber(PurchaseOrderNumber lastPurchaseOrderNumber) {
        this.lastPurchaseOrderNumber = lastPurchaseOrderNumber;
    }

    private Employee getEmployee() {

        return getFinanceManager().getEmployee();
    }

    @Override
    public User getUser() {

        return getFinanceManager().getUser();

    }

    public String formatAsCurrency(Double number) {

        return formatAsCurrency(number, "");

    }

    public Boolean getRenderCurrencyExchangeRate() {

        String defaultCurrency = getSelectedPurchaseRequisition().getCurrency().getName();
        String paymentCurrency = getSelectedPurchaseRequisition().getPaymentCurrency().getName();

        return !defaultCurrency.equals(paymentCurrency);
    }

    public String getCurrencyExchangeRateName() {
        String name = "?-?";

        EntityManager em = getEntityManager1();
        String defaultCurrencyName = SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "defaultCurrency");

        Currency defaultCurrency = Currency.findByName(em, defaultCurrencyName);
        String selectedCurrencyName = getSelectedPurchaseRequisition().getCurrency().getName();
        Currency selectedCurrency = Currency.findByName(em, selectedCurrencyName);

        if (defaultCurrency != null && selectedCurrency != null) {
            name = selectedCurrency.getCode() + "-"
                    + defaultCurrency.getCode();
        }

        return name;
    }

    public String getDefaultToPaymentCurrencyExchangeRateName() {

        EntityManager em = getEntityManager1();
        String defaultCurrencyName = SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "defaultCurrency");
        Currency defaultCurrency = Currency.findByName(em, defaultCurrencyName);

        String currencyName = getSelectedPurchaseRequisition().getCurrency().getName();
        Currency selectedCurrency = Currency.findByName(em, currencyName);

        String paymentCurrencyName = getSelectedPurchaseRequisition().getPaymentCurrency().getName();
        Currency paymentCurrency = Currency.findByName(em, paymentCurrencyName);

        if (selectedCurrency == null) {
            selectedCurrency = defaultCurrency;
        }

        if (paymentCurrency == null) {
            paymentCurrency = defaultCurrency;
        }

        return selectedCurrency.getCode() + "-" + paymentCurrency.getCode();

    }

    public String getCurrencyExchangeRateLabel() {

        return getDefaultToPaymentCurrencyExchangeRateName();
    }

    public void okCashPayment() {

        selectedCashPayment.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void cancelCashPaymentEdit() {
        selectedCashPayment.setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public List getPaymentTypes() {
        return FinancialUtils.getPaymentTypes(getSystemManager().getEntityManager1());
    }

    public List getPaymentPurposes() {
        return FinancialUtils.getPaymentPurposes(getSystemManager().getEntityManager1());
    }

    public List<PurchaseRequisition> getProcurementTasks() {

        EntityManager em = getEntityManager1();
        procurementTasks = new ArrayList<>();
        int maxSearchResults = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        List<PurchaseRequisition> activePRs
                = PurchaseRequisition.findAllActive(em, maxSearchResults);

        if (activePRs != null) {
            for (PurchaseRequisition activePR : activePRs) {

                // Check that the PR is not already in the list before adding it.
                // Add the orginator's PRs
                if (activePR.getOriginator().equals(getEmployee())) {
                    procurementTasks.add(activePR);
                } else {
                    // Add PRs for persons with various positions
                    List<String> PRApproverPositions
                            = (List<String>) SystemOption.
                                    getOptionValueObject(
                                            getSystemManager().getEntityManager1(),
                                            "PRApproverPositions");

                    for (String position : PRApproverPositions) {
                        if (getEmployee().hasEmploymentPosition(position)) {
                            procurementTasks.add(activePR);

                            break;
                        }
                    }
                }

            }
        }

        return procurementTasks;
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 700;
    }

    public void onCostComponentRowCancel(RowEditEvent<CostComponent> event) {
        event.getObject().setIsDirty(false);
    }

    public void onCostComponentRowEdit(RowEditEvent<CostComponent> event) {

        updateCostComponent(event.getObject());
        updatePurchaseReq(null);
    }

    public void openCashPaymentDeleteConfirmDialog(ActionEvent event) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() - 400) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic(
                "/finance/purch/cashPaymentDeleteConfirmDialog",
                options, null);

    }

    public void deleteCashPayment() {

        List<CashPayment> payments = getCashPayments();

        for (CashPayment payment : payments) {
            if (payment.equals(selectedCashPayment)) {

                selectedCashPayment.setOwnerId(null);
                payments.remove(selectedCashPayment);
                selectedCashPayment.save(getEntityManager1());

                break;
            }
        }

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public CashPayment getSelectedCashPayment() {
        return selectedCashPayment;
    }

    public void setSelectedCashPayment(CashPayment selectedCashPayment) {
        this.selectedCashPayment = selectedCashPayment;
    }

    public void createNewCashPayment(ActionEvent event) {
        selectedCashPayment = new CashPayment();
        selectedCashPayment.setOwnerId(getSelectedPurchaseRequisition().getId());
        selectedCashPayment.setDateOfPayment(new Date());

        getCashPayments().add(selectedCashPayment);
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(), selectedCashPayment);

        editCashPayment(event);

    }

    public void editCashPayment(ActionEvent event) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() - 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/finance/purch/cashPaymentDialog", options, null);

    }

    public boolean getApplyTax() {

        return SystemOption.getBoolean(
                getSystemManager().getEntityManager1(),
                "applyTaxToPR");
    }

    public boolean getApplyDiscount() {

        return SystemOption.getBoolean(
                getSystemManager().getEntityManager1(),
                "applyDiscountToPR");
    }

    public List<SelectItem> getDocumentTypes() {

        return getStringListAsSelectItems(
                getSystemManager().getEntityManager1(),
                "PRAttachmentDocumentTypes");
    }

    public boolean hasSelectedPRs() {
        return selectedPurchaseRequisitions != null && !this.selectedPurchaseRequisitions.isEmpty();
    }

    public List<PurchaseRequisition> getSelectedPurchaseRequisitions() {
        return selectedPurchaseRequisitions;
    }

    public void setSelectedPurchaseRequisitions(List<PurchaseRequisition> selectedPurchaseRequisitions) {
        this.selectedPurchaseRequisitions = selectedPurchaseRequisitions;
    }

    @Override
    public String getAppShortcutIconURL() {

        return SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "appShortcutIconURL");

    }

    @Override
    public String getApplicationHeader() {

        return "Procurement";

    }

    @Override
    public String getApplicationSubheader() {
        String subHeader = "Purchase Management &amp; Administration";

        return subHeader;
    }

    public void onAttachmentCellEdit(CellEditEvent event) {
        getSelectedPurchaseRequisition().getAllSortedAttachments().
                get(event.getRowIndex()).setIsDirty(true);
        updatePurchaseReq(null);
    }

    public StreamedContent getFileAttachment(Attachment attachment) {

        return DefaultStreamedContent.builder()
                .contentType(attachment.getContentType())
                .name(attachment.getSourceURL())
                .stream(() -> attachment.getFileInputStream())
                .build();
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Suppliers":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:supplierSearchButton");
                return true;
            case "Purchase Requisitions":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:purchaseReqSearchButton");
                return true;
            default:
                return false;
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {

            OutputStream outputStream;

            String uploadedFilePath = SystemOption.getString(
                    getSystemManager().getEntityManager1(),
                    "purchReqUploadFolder")
                    + event.getFile().getFileName();
            File fileToSave
                    = new File(uploadedFilePath);
            outputStream = new FileOutputStream(fileToSave);
            outputStream.write(event.getFile().getContent());
            outputStream.close();

            // Create attachment and save PR.            
            getSelectedPurchaseRequisition().getAttachments().
                    add(new Attachment(event.getFile().getFileName(),
                            event.getFile().getFileName(),
                            uploadedFilePath,
                            event.getFile().getContentType()));

            updatePurchaseReq(null);

            PrimeFacesUtils.addMessage("Successful", event.getFile().getFileName() + " was uploaded.", FacesMessage.SEVERITY_INFO);

            if (getSelectedPurchaseRequisition().getId() != null) {
                saveSelectedPurchaseRequisition();
            }

        } catch (IOException ex) {
            System.out.println("Error uploading file: " + ex);

            PrimeFacesUtils.addMessage("Upload Error", event.getFile().getFileName() + " was NOT uploaded.", FacesMessage.SEVERITY_ERROR);
        }
    }

    /**
     * Gets the title of the application which may be saved in a database.
     *
     * @return
     */
    public String getTitle() {
        return "Procurement";
    }

    /**
     * Gets the supplier's search text.
     *
     * @return
     */
    public String getSupplierSearchText() {
        return supplierSearchText;
    }

    /**
     * Sets the supplier's search text.
     *
     * @param supplierSearchText
     */
    public void setSupplierSearchText(String supplierSearchText) {
        this.supplierSearchText = supplierSearchText;
    }

    /**
     * Gets the selected supplier.
     *
     * @return
     */
    public Supplier getSelectedSupplier() {
        if (selectedSupplier == null) {
            return new Supplier("");
        }
        return selectedSupplier;
    }

    /**
     * Sets the selected supplier.
     *
     * @param selectedSupplier
     */
    public void setSelectedSupplier(Supplier selectedSupplier) {
        this.selectedSupplier = selectedSupplier;
    }

    public Address getSupplierCurrentAddress() {
        return getSelectedSupplier().getDefaultAddress();
    }

    public Contact getSupplierCurrentContact() {
        return getSelectedSupplier().getDefaultContact();
    }

    public void editSupplierCurrentAddress() {
        selectedSupplierAddress = getSupplierCurrentAddress();
        setEdit(true);
    }

    public void createNewSupplier() {
        selectedSupplier = new Supplier("", true);

        editSelectedSupplier();
    }

    public void addNewSupplier() {
        selectedSupplier = new Supplier("", true);

        openSuppliersTab();

        editSelectedSupplier();
    }

    public Boolean getIsNewSupplier() {
        return getSelectedSupplier().getId() == null;
    }

    public Boolean getIsNewPurchaseReq() {
        return getSelectedPurchaseRequisition().getId() == null;
    }

    public void cancelSupplierEdit(ActionEvent actionEvent) {

        getSelectedSupplier().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okSupplier() {
        Boolean hasValidAddress = false;
        Boolean hasValidContact = false;

        try {

            // Validate 
            // Check for a valid address
            for (Address address : selectedSupplier.getAddresses()) {
                hasValidAddress = hasValidAddress || Address.validate(address);
            }
            if (!hasValidAddress) {
                PrimeFacesUtils.addMessage("Address Required",
                        "A valid address was not entered for this supplier",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Check for a valid contact
            for (Contact contact : selectedSupplier.getContacts()) {
                hasValidContact = hasValidContact || Contact.validate(contact);
            }
            if (!hasValidContact) {
                PrimeFacesUtils.addMessage("Contact Required",
                        "A valid contact was not entered for this supplier",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Update tracking
            if (getIsNewSupplier()) {
                getSelectedSupplier().setDateEntered(new Date());
                getSelectedSupplier().setDateEdited(new Date());
                if (getUser() != null) {
                    selectedSupplier.setEnteredBy(getEmployee());
                    selectedSupplier.setEditedBy(getEmployee());
                }
            }

            // Do save
            if (getSelectedSupplier().getIsDirty()) {
                getSelectedSupplier().setDateEdited(new Date());
                if (getUser() != null) {
                    selectedSupplier.setEditedBy(getEmployee());
                }
                selectedSupplier.save(getEntityManager1());
                getSelectedSupplier().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Purchase requisitions", "Purchase requisitions"));
        searchTypes.add(new SelectItem("Suppliers", "Suppliers"));

        return searchTypes;
    }

    public void updateSupplier() {
        getSelectedSupplier().setIsDirty(true);
    }

    public void removeSupplierContact() {
        getSelectedSupplier().getContacts().remove(selectedSupplierContact);
        getSelectedSupplier().setIsDirty(true);
        selectedSupplierContact = null;
    }

    public Boolean getIsNewSupplierAddress() {
        return getSelectedSupplierAddress().getId() == null && !getEdit();
    }

    public void okSupplierAddress() {

        selectedSupplierAddress = selectedSupplierAddress.prepare();

        if (getIsNewSupplierAddress()) {
            getSelectedSupplier().getAddresses().add(selectedSupplierAddress);
        }

        PrimeFaces.current().executeScript("PF('addressFormDialog').hide();");

    }

    public void updateSupplierAddress() {
        getSelectedSupplierAddress().setIsDirty(true);
        getSelectedSupplier().setIsDirty(true);
    }

    public List<Address> getSupplierAddressesModel() {
        return getSelectedSupplier().getAddresses();
    }

    public List<Contact> getSupplierContactsModel() {
        return getSelectedSupplier().getContacts();
    }

    public void createNewSupplierAddress() {
        selectedSupplierAddress = null;

        // Find an existing invalid or blank address and use it as the neww address
        for (Address address : getSelectedSupplier().getAddresses()) {
            if (address.getAddressLine1().trim().isEmpty()) {
                selectedSupplierAddress = address;
                break;
            }
        }

        // No existing blank or invalid address found so creating new one.
        if (selectedSupplierAddress == null) {
            selectedSupplierAddress = new Address("", "Billing");
        }

        setEdit(false);

        getSelectedSupplier().setIsDirty(false);
    }

    public void okContact() {

        selectedSupplierContact = selectedSupplierContact.prepare();

        if (getIsNewSupplierContact()) {
            getSelectedSupplier().getContacts().add(selectedSupplierContact);
        }

        PrimeFaces.current().executeScript("PF('contactFormDialog').hide();");

    }

    public void updateSupplierContact() {
        getSelectedSupplierContact().setIsDirty(true);
        getSelectedSupplier().setIsDirty(true);
    }

    public void createNewSupplierContact() {
        selectedSupplierContact = null;

        for (Contact contact : getSelectedSupplier().getContacts()) {
            if (contact.getFirstName().trim().isEmpty()) {
                selectedSupplierContact = contact;
                break;
            }
        }

        if (selectedSupplierContact == null) {
            selectedSupplierContact = new Contact("", "", "Main");
            selectedSupplierContact.setInternet(new Internet());
        }

        setEdit(false);

        getSelectedSupplier().setIsDirty(false);
    }

    public void updateSupplierName(AjaxBehaviorEvent event) {
        selectedSupplier.setName(selectedSupplier.getName().trim());

        getSelectedSupplier().setIsDirty(true);
    }

    public void onSupplierCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundSuppliers().get(event.getRowIndex()));
    }

    public void onCostComponentCellEdit(CellEditEvent event) {

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {

            CostComponent costComponent = getSelectedPurchaseRequisition().getAllSortedCostComponents().
                    get(event.getRowIndex());

            updateCostComponent(costComponent);

        }

    }

    public List<CashPayment> getCashPayments() {

        if (getSelectedPurchaseRequisition().getId() != null) {
            cashPayments = CashPayment.
                    findCashPaymentsByOwnerId(getEntityManager1(),
                            getSelectedPurchaseRequisition().getId());
        } else {
            cashPayments = new ArrayList<>();
        }

        return cashPayments;

    }

    public Double getTotalCashPayments() {
        Double payment = 0.0;

        for (CashPayment cashPayment : getCashPayments()) {
            payment = payment + cashPayment.getPayment();
        }

        return payment;
    }

    public void onCashPaymentCellEdit(CellEditEvent event) {

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {

            CashPayment cashPayment = getCashPayments().get(event.getRowIndex());

            BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(), cashPayment);
        }
    }

    public int getNumOfSuppliersFound() {
        return getFoundSuppliers().size();
    }

    public void editPurchaseReqSupplier() {
        setSelectedSupplier(getSelectedPurchaseRequisition().getSupplier());

        editSelectedSupplier();
    }

    public void editSelectedSupplier() {

        getSelectedSupplier().setIsNameAndIdEditable(getUser().can("AddSupplier"));

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth()) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic(
                "/finance/purch/supplierDialog",
                options, null);

    }

    public Boolean getIsActiveSuppliersOnly() {
        if (isActiveSuppliersOnly == null) {
            isActiveSuppliersOnly = true;
        }
        return isActiveSuppliersOnly;
    }

    public List<Supplier> getFoundSuppliers() {
        if (foundSuppliers == null) {
            foundSuppliers = new ArrayList<>();
        }
        return foundSuppliers;
    }

    public void setFoundSuppliers(List<Supplier> foundSuppliers) {
        this.foundSuppliers = foundSuppliers;
    }

    public void setIsActiveSuppliersOnly(Boolean isActiveSuppliersOnly) {
        this.isActiveSuppliersOnly = isActiveSuppliersOnly;
    }

    public void doSupplierSearch() {

        doSupplierSearch(supplierSearchText);

    }

    public void doSupplierSearch(String supplierSearchText) {
        this.supplierSearchText = supplierSearchText;

        int maxSearchResults = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        if (getIsActiveSuppliersOnly()) {
            foundSuppliers = Supplier.findActive(getEntityManager1(),
                    supplierSearchText, maxSearchResults);
        } else {
            foundSuppliers = Supplier.find(getEntityManager1(),
                    supplierSearchText, maxSearchResults);
        }

    }

    public Boolean getIsNewSupplierContact() {
        return getSelectedSupplierContact().getId() == null && !getEdit();
    }

    public Contact getSelectedSupplierContact() {
        return selectedSupplierContact;
    }

    public void setSelectedSupplierContact(Contact selectedSupplierContact) {
        this.selectedSupplierContact = selectedSupplierContact;

        setEdit(true);
    }

    public Address getSelectedSupplierAddress() {
        return selectedSupplierAddress;
    }

    public void setSelectedSupplierAddress(Address selectedSupplierAddress) {
        this.selectedSupplierAddress = selectedSupplierAddress;

        setEdit(true);
    }

    public void removeSupplierAddress() {
        getSelectedSupplier().getAddresses().remove(selectedSupplierAddress);
        getSelectedSupplier().setIsDirty(true);
        selectedSupplierAddress = null;
    }

    public List<Supplier> completeActiveSupplier(String query) {
        try {

            int maxSearchResults = SystemOption.getInteger(
                    getSystemManager().getEntityManager1(),
                    "maxSearchResults");

            return Supplier.findActive(getEntityManager1(), query, maxSearchResults);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void openSuppliersTab() {
        getSystemManager().getMainTabView().openTab("Suppliers");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:supplierSearchButton");
    }

    public Boolean getCanExportPurchaseReqForm() {
        return getIsSelectedPurchaseReqIsValid();
    }

    public Boolean getCanExportPurchaseOrderForm() {
        return getIsProcurementOfficer() && getIsSelectedPurchaseReqIsValid();
    }

    public Boolean getIsProcurementOfficer() {
        return getEmployee().isProcurementOfficer();
    }

    public Boolean getCanCreateNewCashPayment() {
        return getIsProcurementOfficer() && !getIsNewPurchaseReq();
    }

    public String getPurchaseReqEmailContent() {
        return purchaseReqEmailContent;
    }

    public void setPurchaseReqEmailContent(String purchaseReqEmailContent) {
        this.purchaseReqEmailContent = purchaseReqEmailContent;
    }

    public String getPurchaseReqEmailSubject() {
        return purchaseReqEmailSubject;
    }

    public void setPurchaseReqEmailSubject(String purchaseReqEmailSubject) {
        this.purchaseReqEmailSubject = purchaseReqEmailSubject;
    }

    public List<Employee> getToEmployees() {
        if (toEmployees == null) {
            toEmployees = new ArrayList<>();
        }
        return toEmployees;
    }

    public void setToEmployees(List<Employee> toEmployees) {
        this.toEmployees = toEmployees;
    }

    @Override
    public void updateDateSearchField() {
        //doSearch();
    }

    public String getPRApprovalOrRecommendationDate(Employee approverOrRecommender) {
        // Get approval date
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getApprover1() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getApprover1().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate1());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getApprover2() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getApprover2().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate2());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getApprover3() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getApprover3().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate3());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getApprover4() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getApprover4().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate4());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getApprover5() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getApprover5().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate5());
            }
        }
        // Get recommendation date
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getRecommender1() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getRecommender1().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate1());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getRecommender2() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getRecommender2().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate2());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getRecommender3() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getRecommender3().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate3());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getRecommender4() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getRecommender4().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate4());
            }
        }
        if (approverOrRecommender != null
                && getSelectedPurchaseRequisition().getRecommender5() != null) {
            if (Objects.equals(approverOrRecommender.getId(),
                    getSelectedPurchaseRequisition().getRecommender5().getId())) {

                return BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                        getApprovalOrRecommendationDate5());
            }
        }

        return "";
    }

    public Employee getSelectedApprover() {
        return selectedApprover;
    }

    public void setSelectedApprover(Employee selectedApprover) {
        this.selectedApprover = selectedApprover;
    }

    public String getSelectedPurchaseRequisitionApprovalsNote() {
        int requiredApprovals
                = (Integer) SystemOption.getOptionValueObject(
                        getSystemManager().getEntityManager1(),
                        "requiredPRApprovals");

        if (getSelectedPurchaseRequisition().getApprovals() < requiredApprovals) {
            return "The required number of approvals has NOT yet been received.";
        } else {
            return "The required number of " + requiredApprovals + " approvals has been received.";
        }

    }

    public Boolean isJustificationRequired() {
        Double limitForPRJustification
                = SystemOption.getDouble(
                        getSystemManager().getEntityManager1(),
                        "LimitForPRJustification");
        Double totalComponentsCost = getSelectedPurchaseRequisition().getTotalCostComponentCosts();

        return ((totalComponentsCost > limitForPRJustification)
                && !getSelectedPurchaseRequisition().hasJustification());
    }

    public String formatAsCurrency(Double value, String symbol) {
        return NumberUtils.formatAsCurrency(value, symbol);
    }

    public String getDefaultCurrencySymbol() {

        EntityManager em = getEntityManager1();

        String defaultCurrencyName = SystemOption.getString(
                getSystemManager().getEntityManager1(),
                "defaultCurrency");
        Currency defaultCurrency = Currency.findByName(em, defaultCurrencyName);

        return defaultCurrency.getSymbol();
    }

    public String getCostingCurrencySymbol() {

        return getSelectedPurchaseRequisition().getCurrency().getSymbol();
    }

    public String getPaymentCurrencySymbol() {

        return getSelectedPurchaseRequisition().getPaymentCurrency().getSymbol();
    }

    public String getSelectedPRJustificationStatusNote() {

        EntityManager em = getSystemManager().getEntityManager1();

        if (isJustificationRequired()) {

            String PRJustificationStatusNote
                    = SystemOption.getString(em,
                            "PRJustificationStatusNote");
            Double limitForPRJustification
                    = SystemOption.getDouble(em,
                            "LimitForPRJustification");

            return PRJustificationStatusNote.replace("<LimitForPRJustification>",
                    NumberUtils.formatAsCurrency(limitForPRJustification, "$"));
        } else {
            return "";
        }

    }

    public String getSelectedPRBidQuotesNote() {

        EntityManager em = getSystemManager().getEntityManager1();

        String PRBidQuotesNote
                = SystemOption.getString(em,
                        "PRBidQuotesNote");
        Double limitForPRJustification
                = SystemOption.getDouble(em,
                        "LimitForPRJustification");

        return PRBidQuotesNote.replace("<LimitForPRJustification>",
                NumberUtils.formatAsCurrency(limitForPRJustification, "$"));

    }

    public String getSelectedPRProcurementAmountNote() {
        Double maxAmountForPRProcurement
                = SystemOption.getDouble(
                        getSystemManager().getEntityManager1(),
                        "maxAmountForPRProcurement");

        if (getSelectedPurchaseRequisition().getTotalCost() > maxAmountForPRProcurement) {
            return "The total cost of "
                    + NumberUtils.formatAsCurrency(getSelectedPurchaseRequisition().getTotalCost(), "$")
                    + " exceeds the maximum of "
                    + NumberUtils.formatAsCurrency(maxAmountForPRProcurement, "$")
                    + ". Procurement is required.";
        } else {
            return "";
        }

    }

    public Boolean checkPRWorkProgressReadinessToBeChanged() {
        EntityManager em = getEntityManager1();

        if (getSelectedPurchaseRequisition().getId() != null) {

            // Find the currently stored PR and check its work status
            PurchaseRequisition savedPurchaseRequisition
                    = PurchaseRequisition.findById(em, getSelectedPurchaseRequisition().getId());

            // Procurement officer required to cancel PR.
            if (savedPurchaseRequisition != null) {
                if (!getEmployee().isProcurementOfficer()
                        && getSelectedPurchaseRequisition().getWorkProgress().equals("Cancelled")) {
                    PrimeFacesUtils.addMessage("Procurement Officer Required",
                            "You are not a procurement officer so you cannot cancel this purchase requisition.",
                            FacesMessage.SEVERITY_WARN);

                    return false;
                }
            }

            // Procurement officer required to mark job completed.
            if (savedPurchaseRequisition != null) {
                if (!getEmployee().isProcurementOfficer()
                        && !getSelectedPurchaseRequisition().getWorkProgress().equals("Completed")
                        && savedPurchaseRequisition.getWorkProgress().equals("Completed")) {
                    PrimeFacesUtils.addMessage("Procurement Officer Required",
                            "You are not a procurement officer so you cannot change the completion status of this purchase requisition.",
                            FacesMessage.SEVERITY_WARN);

                    return false;
                }
            }

            // Procurement officer is required to approve PRs.
            if (!getEmployee().isProcurementOfficer()
                    && getSelectedPurchaseRequisition().getWorkProgress().equals("Completed")) {

                PrimeFacesUtils.addMessage("Procurement Officer Required",
                        "You are not a procurement officer so you cannot mark this purchase requisition as completed.",
                        FacesMessage.SEVERITY_WARN);

                return false;
            }

            // Do not allow flagging PR as completed unless it is approved.    
            int requiredApprovals
                    = SystemOption.getInteger(
                            getSystemManager().getEntityManager1(),
                            "requiredPRApprovals");

            if (!((getSelectedPurchaseRequisition().getApprovals()
                    + getSelectedPurchaseRequisition().getRecommendations()) >= requiredApprovals)
                    && getSelectedPurchaseRequisition().getWorkProgress().equals("Completed")) {

                PrimeFacesUtils.addMessage("Purchase Requisition Not Completed",
                        "This purchase requisition requires " + requiredApprovals
                        + " approvals/recommendations before it can be marked as completed",
                        FacesMessage.SEVERITY_WARN);

                return false;
            }

        } else {

            PrimeFacesUtils.addMessage("Purchase Requisition Work Progress Cannot be Changed",
                    "This purchase requisition's work progress cannot be changed until it is saved",
                    FacesMessage.SEVERITY_WARN);
            return false;
        }

        return true;
    }

    public void updateWorkProgress() {

        EntityManager em = getEntityManager1();

        if (checkPRWorkProgressReadinessToBeChanged()) {
            if (!getSelectedPurchaseRequisition().getWorkProgress().equals("Completed")) {

                selectedPurchaseRequisition.setPurchasingDepartment(
                        Department.findDefault(em,
                                "--"));
                selectedPurchaseRequisition.setProcurementOfficer(
                        Employee.findDefault(em,
                                "--", "--", false));
                getSelectedPurchaseRequisition().setDateOfCompletion(null);

                getSelectedPurchaseRequisition().setPurchaseOrderDate(null);

            } else if (getSelectedPurchaseRequisition().getWorkProgress().equals("Completed")) {

                getSelectedPurchaseRequisition().setDateOfCompletion(new Date());

                getSelectedPurchaseRequisition().setPurchaseOrderDate(new Date());

                // Set the procurement officer and their department
                getSelectedPurchaseRequisition().
                        setProcurementOfficer(getEmployee());

                getSelectedPurchaseRequisition().
                        setPurchasingDepartment(getEmployee().getDepartment());

                updatePurchaseReq(null);

                getSelectedPurchaseRequisition().addAction(BusinessEntity.Action.COMPLETE);
            }

            updatePurchaseReq(null);

        } else {
            if (getSelectedPurchaseRequisition().getId() != null) {
                // Reset work progress to the currently saved state
                PurchaseRequisition foundPR = PurchaseRequisition.findById(em,
                        getSelectedPurchaseRequisition().getId());
                if (foundPR != null) {
                    getSelectedPurchaseRequisition().setWorkProgress(foundPR.getWorkProgress());
                } else {
                    getSelectedPurchaseRequisition().setWorkProgress("Ongoing");
                }
            } else {
                getSelectedPurchaseRequisition().setWorkProgress("Ongoing");
            }
        }

    }

    public void deleteCostComponent() {
        deleteCostComponentByName(selectedCostComponent.getName());
    }

    public void deleteAttachment() {

        PrimeFacesUtils.addMessage("Successful", selectedAttachment.getName() + " was deleted.", FacesMessage.SEVERITY_INFO);

        deleteAttachmentByName(selectedAttachment.getName());
    }

    public void deleteSelectedPRApproverOrRecommender() {
        deleteApproverOrRecommender(selectedApprover.getName());

        updatePurchaseReq(null);

        if (getSelectedPurchaseRequisition().getId() != null) {
            savePurchaseRequisition(getSelectedPurchaseRequisition(),
                    "Updated and Saved",
                    "This purchase requisition was successfully updated and saved");
        }
    }

    public void deleteApproverOrRecommender(String approverOrRecommenderName) {

        // Nullify approvers 1 - 5 if possible
        if (getSelectedPurchaseRequisition().getApprover1() != null) {
            if (getSelectedPurchaseRequisition().getApprover1().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setApprover1(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate1(null);
                getSelectedPurchaseRequisition().setApprovals(
                        getSelectedPurchaseRequisition().getApprovals() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getApprover2() != null) {
            if (getSelectedPurchaseRequisition().getApprover2().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setApprover2(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate2(null);
                getSelectedPurchaseRequisition().setApprovals(
                        getSelectedPurchaseRequisition().getApprovals() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getApprover3() != null) {
            if (getSelectedPurchaseRequisition().getApprover3().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setApprover3(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate3(null);
                getSelectedPurchaseRequisition().setApprovals(
                        getSelectedPurchaseRequisition().getApprovals() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getApprover4() != null) {
            if (getSelectedPurchaseRequisition().getApprover4().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setApprover4(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate4(null);
                getSelectedPurchaseRequisition().setApprovals(
                        getSelectedPurchaseRequisition().getApprovals() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getApprover5() != null) {
            if (getSelectedPurchaseRequisition().getApprover5().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setApprover5(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate5(null);
                getSelectedPurchaseRequisition().setApprovals(
                        getSelectedPurchaseRequisition().getApprovals() - 1);
            }
        }
        // Nullify recommender 1 - 5 if possible
        if (getSelectedPurchaseRequisition().getRecommender1() != null) {
            if (getSelectedPurchaseRequisition().getRecommender1().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setRecommender1(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate1(null);
                getSelectedPurchaseRequisition().setRecommendations(
                        getSelectedPurchaseRequisition().getRecommendations() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getRecommender2() != null) {
            if (getSelectedPurchaseRequisition().getRecommender2().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setRecommender2(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate2(null);
                getSelectedPurchaseRequisition().setRecommendations(
                        getSelectedPurchaseRequisition().getRecommendations() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getRecommender3() != null) {
            if (getSelectedPurchaseRequisition().getRecommender3().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setRecommender3(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate3(null);
                getSelectedPurchaseRequisition().setRecommendations(
                        getSelectedPurchaseRequisition().getRecommendations() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getRecommender4() != null) {
            if (getSelectedPurchaseRequisition().getRecommender4().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setRecommender4(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate4(null);
                getSelectedPurchaseRequisition().setRecommendations(
                        getSelectedPurchaseRequisition().getRecommendations() - 1);
            }
        }
        if (getSelectedPurchaseRequisition().getRecommender5() != null) {
            if (getSelectedPurchaseRequisition().getRecommender5().
                    getName().equals(approverOrRecommenderName)) {

                getSelectedPurchaseRequisition().setRecommender5(null);
                getSelectedPurchaseRequisition().setApprovalOrRecommendationDate5(null);
                getSelectedPurchaseRequisition().setRecommendations(
                        getSelectedPurchaseRequisition().getRecommendations() - 1);
            }
        }

    }

    public Boolean addApprover(PurchaseRequisition purchaseRequisition,
            Employee approver) {

        // Check if the PR was recommended      
        // Add approver if user has TEAM LEADER position
        if ((purchaseRequisition.getApprover1() == null)
                && (purchaseRequisition.getRecommender1() == null)
                && (approver.hasEmploymentPosition("Team Leader"))) {

            purchaseRequisition.setApprover1(approver);
            purchaseRequisition.setApprovalOrRecommendationDate1(new Date());
            purchaseRequisition.setApprovals(
                    purchaseRequisition.getApprovals() + 1);

            return true;
        }

        // Add approver if user has DIVISIONAL MANAGER position
        if ((purchaseRequisition.getApprover2() == null)
                && (purchaseRequisition.getRecommender2() == null)
                && (approver.hasEmploymentPosition("Divisional Manager"))) {

            purchaseRequisition.setApprover2(approver);
            purchaseRequisition.setApprovalOrRecommendationDate2(new Date());
            purchaseRequisition.setApprovals(
                    purchaseRequisition.getApprovals() + 1);

            return true;
        }

        // Add approver if user has DIVISIONAL DIRECTOR position
        if ((purchaseRequisition.getApprover3() == null)
                && (purchaseRequisition.getRecommender3() == null)
                && (approver.hasEmploymentPosition("Divisional Director"))) {

            purchaseRequisition.setApprover3(approver);
            purchaseRequisition.setApprovalOrRecommendationDate3(new Date());
            purchaseRequisition.setApprovals(
                    purchaseRequisition.getApprovals() + 1);

            return true;
        }

        // Add approver if user has FINANCE DIRECTOR position
        if ((purchaseRequisition.getApprover4() == null)
                && (purchaseRequisition.getRecommender4() == null)
                && (approver.hasEmploymentPosition("Finance Director"))) {

            purchaseRequisition.setApprover4(approver);
            purchaseRequisition.setApprovalOrRecommendationDate4(new Date());
            purchaseRequisition.setApprovals(
                    purchaseRequisition.getApprovals() + 1);

            return true;
        }

        // Add approver if user has EXECUTIVE DIRECTOR position
        if ((purchaseRequisition.getApprover5() == null)
                && (purchaseRequisition.getRecommender5() == null)
                && (approver.hasEmploymentPosition("Executive Director"))) {

            purchaseRequisition.setApprover5(approver);
            purchaseRequisition.setApprovalOrRecommendationDate5(new Date());
            purchaseRequisition.setApprovals(
                    purchaseRequisition.getApprovals() + 1);

            return true;
        }

        return false;

    }

    public Boolean addRecommender(PurchaseRequisition purchaseRequisition,
            Employee recommender) {

        // Add recommender if user has TEAM LEADER position
        if ((purchaseRequisition.getRecommender1() == null)
                && (purchaseRequisition.getApprover1() == null)
                && (recommender.hasEmploymentPosition("Team Leader"))) {

            purchaseRequisition.setRecommender1(recommender);
            purchaseRequisition.setApprovalOrRecommendationDate1(new Date());
            purchaseRequisition.setRecommendations(
                    purchaseRequisition.getRecommendations() + 1);

            return true;
        }

        // Add recommender if user has DIVISIONAL MANAGER position
        if ((purchaseRequisition.getRecommender2() == null)
                && (purchaseRequisition.getApprover2() == null)
                && (recommender.hasEmploymentPosition("Divisional Manager"))) {

            purchaseRequisition.setRecommender2(recommender);
            purchaseRequisition.setApprovalOrRecommendationDate2(new Date());
            purchaseRequisition.setRecommendations(
                    purchaseRequisition.getRecommendations() + 1);

            return true;
        }

        // Add recommender if user has DIVISIONAL DIRECTOR position
        if ((purchaseRequisition.getRecommender3() == null)
                && (purchaseRequisition.getApprover3() == null)
                && (recommender.hasEmploymentPosition("Divisional Director"))) {

            purchaseRequisition.setRecommender3(recommender);
            purchaseRequisition.setApprovalOrRecommendationDate3(new Date());
            purchaseRequisition.setRecommendations(
                    purchaseRequisition.getRecommendations() + 1);

            return true;
        }

        // Add recommender if user has FINANCE DIRECTOR position
        if ((purchaseRequisition.getRecommender4() == null)
                && (purchaseRequisition.getApprover4() == null)
                && (recommender.hasEmploymentPosition("Finance Director"))) {

            purchaseRequisition.setRecommender4(recommender);
            purchaseRequisition.setApprovalOrRecommendationDate4(new Date());
            purchaseRequisition.setRecommendations(
                    purchaseRequisition.getRecommendations() + 1);

            return true;
        }

        // Add recommender if user has EXECUTIVE DIRECTOR position
        if ((purchaseRequisition.getRecommender5() == null)
                && (purchaseRequisition.getApprover5() == null)
                && (recommender.hasEmploymentPosition("Executive Director"))) {

            purchaseRequisition.setRecommender5(recommender);
            purchaseRequisition.setApprovalOrRecommendationDate5(new Date());
            purchaseRequisition.setRecommendations(
                    purchaseRequisition.getRecommendations() + 1);

            return true;

        }

        return false;

    }

    public void okCostingComponent() {
        if (selectedCostComponent.getId() == null && !getEdit()) {
            getSelectedPurchaseRequisition().getCostComponents().add(selectedCostComponent);
        }

        setEdit(false);
        updateCostComponent(selectedCostComponent);
        updatePurchaseReq(null);

        PrimeFaces.current().executeScript("PF('purchreqCostingCompDialog').hide();");
    }

    public void openPurchaseReqsTab() {
        getSystemManager().getMainTabView().openTab("Purchase Requisitions");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:purchaseReqSearchButton");
    }

    public void editPurchReqGeneralEmail() {

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

        PrimeFaces.current().dialog().openDynamic("purchaseReqEmailDialog",
                options, null);

    }

    public void openPurchaseRequisistionEmailDialog() {
        EntityManager em = getSystemManager().getEntityManager1();
        Email email = Email.findActiveEmailByName(em, "pr-email-template");
        String prNum = getSelectedPurchaseRequisition().getNumber();
        String JMTSURL = (String) SystemOption.getOptionValueObject(em, "appURL");
        String originator = getSelectedPurchaseRequisition().getOriginator().getFirstName()
                + " " + getSelectedPurchaseRequisition().getOriginator().getLastName();
        String department = getSelectedPurchaseRequisition().getOriginatingDepartment().getName();
        String requisitionDate = BusinessEntityUtils.
                getDateInMediumDateFormat(getSelectedPurchaseRequisition().getRequisitionDate());
        String description = getSelectedPurchaseRequisition().getDescription();
        String sender = getEmployee().getFirstName() + " "
                + getEmployee().getLastName();
        String approversAndRecommendersList = getSelectedPurchaseRequisition().getApproversAndRecommendersList();

        getToEmployees().clear();
        setPurchaseReqEmailSubject(
                email.getSubject().replace("{purchaseRequisitionNumber}", prNum));
        setPurchaseReqEmailContent(
                email.getContent("/correspondences/").
                        replace("{JMTSURL}", JMTSURL).
                        replace("{purchaseRequisitionNumber}", prNum).
                        replace("{supplier}", getSelectedPurchaseRequisition().getSupplier().getName()).
                        replace("{originator}", originator).
                        replace("{department}", department).
                        replace("{requisitionDate}", requisitionDate).
                        replace("{action}", "approved").
                        replace("{description}", description).
                        replace("{approversAndRecommendersList}", approversAndRecommendersList).
                        replace("{workProgress}", getSelectedPurchaseRequisition().getWorkProgress()).
                        replace("{sender}", sender));

        editPurchReqGeneralEmail();
    }

//    public void openSendEmailDialog() {
//        getToEmployees().clear();
//        setPurchaseReqEmailSubject("");
//        setPurchaseReqEmailContent("");
//
//        editPurchReqGeneralEmail();
//    }
//
//    public void sendGeneralPurchaseReqEmail() {
//
//        try {
//            EntityManager em = getSystemManager().getEntityManager1();
//
//            for (Employee toEmployee : getToEmployees()) {
//
//                if (MailUtils.postMail(null,
//                        getFinanceManager().getJobManagerEmailAddress(),
//                        getFinanceManager().getJobManagerEmailName(),
//                        toEmployee.getInternet().getEmail1(),
//                        getPurchaseReqEmailSubject(),
//                        getPurchaseReqEmailContent(),
//                        "text/html",
//                        em).isSuccess()) {
//
//                    closeDialog();
//
//                } else {
//                    PrimeFacesUtils.addMessage("Error Sending Email",
//                            "An error occurred while sending email.",
//                            FacesMessage.SEVERITY_ERROR);
//                }
//            }
//        } catch (Exception e) {
//
//            System.out.println("Error sending PR email(s): " + e);
//        }
//
//    }
    public List getCostTypeList() {
        return FinancialUtils.getCostTypeList(getSystemManager().getEntityManager1());
    }

    public Boolean getIsSupplierNameValid() {
        if (selectedPurchaseRequisition.getSupplier() != null) {
            return BusinessEntityUtils.validateText(selectedPurchaseRequisition.getSupplier().getName());
        }

        return false;
    }

    public StreamedContent getPurchaseReqFile() {
        StreamedContent streamedContent = null;

        try {

            streamedContent = getPurchaseReqStreamContent(getEntityManager1());

        } catch (Exception e) {
            System.out.println(e);
        }

        return streamedContent;
    }

    public StreamedContent getPurchaseReqStreamContent(EntityManager em) {

        HashMap parameters = new HashMap();
        String logoURL = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoSRC");
        String footNote1 = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "QEMS_PR_Footnote1");
        String footNote2 = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "QEMS_PR_Footnote2");
        String footNote3 = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "QEMS_PR_Footnote3");
        String footNote4 = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "QEMS_PR_Footnote4");

        try {

            parameters.put("prId", getSelectedPurchaseRequisition().getId());
            parameters.put("logoURL", logoURL);

            // Shipping information
            if (getSelectedPurchaseRequisition().getAirFreight()) {
                parameters.put("airFreight", "[\u2713] Air Freight");
            } else {
                parameters.put("airFreight", "[ ] Air Freight");
            }
            if (getSelectedPurchaseRequisition().getSurface()) {
                parameters.put("surface", "[\u2713] Surface");
            } else {
                parameters.put("surface", "[ ] Surface");
            }
            if (getSelectedPurchaseRequisition().getAirParcelPost()) {
                parameters.put("airParcelPost", "[\u2713] Air Parcel Post");
            } else {
                parameters.put("airParcelPost", "[ ] Air Parcel Post");
            }
            parameters.put("specialInstructions", getSelectedPurchaseRequisition().
                    getShippingInstructions());

            // Budget information
            if (getSelectedPurchaseRequisition().getBudgeted()) {
                parameters.put("budgeted", "[\u2713] YES     [ ] NO");
            } else {
                parameters.put("budgeted", "[ ] YES     [\u2713] NO");
            }
            parameters.put("budgetedRecurrent", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBudgetedRecurrent(), "$"));
            parameters.put("yearToDateRecurrent", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getYearToDateRecurrent(), "$"));
            parameters.put("balanceRecurrent", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBalanceRecurrent(), "$"));
            parameters.put("budgetedCapital", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBudgetedCapital(), "$"));
            parameters.put("yearToDateCapital", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getYearToDateCapital(), "$"));
            parameters.put("balanceCapital", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBalanceCapital(), "$"));
            parameters.put("budgetedRecoverable", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBudgetedRecoverable(), "$"));
            parameters.put("yearToDateRecoverable", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getYearToDateRecoverable(), "$"));
            parameters.put("balanceRecoverable", NumberUtils.formatAsCurrency(
                    getSelectedPurchaseRequisition().getBalanceRecoverable(), "$"));

            // Foot notes
            parameters.put("QEMS_PR_Footnote1", footNote1);
            parameters.put("QEMS_PR_Footnote2", footNote2);
            parameters.put("QEMS_PR_Footnote3", footNote3);
            parameters.put("QEMS_PR_Footnote4", footNote4);

            // General details
            parameters.put("purchReqNo", getSelectedPurchaseRequisition().getNumber());
            parameters.put("purchaseOrderNo", getSelectedPurchaseRequisition().getPurchaseOrderNumber());
            parameters.put("addressLine1", getSelectedPurchaseRequisition()
                    .getSupplier().getDefaultAddress().getAddressLine1());
            parameters.put("addressLine2", getSelectedPurchaseRequisition()
                    .getSupplier().getDefaultAddress().getAddressLine2());
            parameters.put("purposeOfOrder", getSelectedPurchaseRequisition().getDescription());
            parameters.put("suggestedSupplier", getSelectedPurchaseRequisition()
                    .getSupplier().getName());
            parameters.put("originator", getSelectedPurchaseRequisition()
                    .getOriginator().getFirstName() + " "
                    + getSelectedPurchaseRequisition()
                            .getOriginator().getLastName());
            parameters.put("priorityCode", getSelectedPurchaseRequisition().getPriorityCode());
            parameters.put("terms", getSelectedPurchaseRequisition().getTerms());
            parameters.put("requisitionDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                            getRequisitionDate()));
            parameters.put("purchaseOrderDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                            getPurchaseOrderDate()));
            parameters.put("importLicenceNo", getSelectedPurchaseRequisition().getImportLicenceNum());
            parameters.put("originatorSignature", getSelectedPurchaseRequisition()
                    .getOriginator().getFirstName() + " "
                    + getSelectedPurchaseRequisition()
                            .getOriginator().getLastName());

            // Set recommenders, recommendation dates and recommenders' positions
            if (getSelectedPurchaseRequisition().getRecommender1() != null) {
                parameters.put("teamLeaderRecommendation",
                        getSelectedPurchaseRequisition().getRecommender1().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getRecommender1().getLastName());
                parameters.put("teamLeaderApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate1()));

            }
            if (getSelectedPurchaseRequisition().getRecommender2() != null) {
                parameters.put("divisionalManagerRecommendation",
                        getSelectedPurchaseRequisition().getRecommender2().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getRecommender2().getLastName());
                parameters.put("divisionalManagerApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate2()));

            }
            if (getSelectedPurchaseRequisition().getRecommender3() != null) {
                parameters.put("divisionalDirectorRecommendation",
                        getSelectedPurchaseRequisition().getRecommender3().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getRecommender3().getLastName());
                parameters.put("divisionalDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate3()));

            }
            if (getSelectedPurchaseRequisition().getRecommender4() != null) {
                parameters.put("financeDirectorRecommendation",
                        getSelectedPurchaseRequisition().getRecommender4().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getRecommender4().getLastName());
                parameters.put("financeDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate4()));

            }
            if (getSelectedPurchaseRequisition().getRecommender5() != null) {
                parameters.put("executiveDirectorRecommendation",
                        getSelectedPurchaseRequisition().getRecommender5().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getRecommender5().getLastName());
                parameters.put("executiveDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate5()));

            }

            // Set approvers, approval dates and approvers' positions
            if (getSelectedPurchaseRequisition().getApprover1() != null) {
                parameters.put("teamLeaderApproval",
                        getSelectedPurchaseRequisition().getApprover1().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getApprover1().getLastName());
                parameters.put("teamLeaderApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                                getApprovalOrRecommendationDate1()));

            }
            if (getSelectedPurchaseRequisition().getApprover2() != null) {
                parameters.put("divisionalManagerApproval",
                        getSelectedPurchaseRequisition().getApprover2().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getApprover2().getLastName());
                parameters.put("divisionalManagerApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().getApprovalOrRecommendationDate2()));

            }
            if (getSelectedPurchaseRequisition().getApprover3() != null) {
                parameters.put("divisionalDirectorApproval",
                        getSelectedPurchaseRequisition().getApprover3().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getApprover3().getLastName());
                parameters.put("divisionalDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().getApprovalOrRecommendationDate3()));

            }
            if (getSelectedPurchaseRequisition().getApprover4() != null) {
                parameters.put("financeDirectorApproval",
                        getSelectedPurchaseRequisition().getApprover4().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getApprover4().getLastName());
                parameters.put("financeDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().getApprovalOrRecommendationDate4()));

            }
            if (getSelectedPurchaseRequisition().getApprover5() != null) {
                parameters.put("executiveDirectorApproval",
                        getSelectedPurchaseRequisition().getApprover5().getFirstName() + " "
                        + getSelectedPurchaseRequisition().getApprover5().getLastName());
                parameters.put("executiveDirectorApprovalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().getApprovalOrRecommendationDate5()));

            }

            // Set procurement officer
            parameters.put("procurementOfficer", getSelectedPurchaseRequisition()
                    .getProcurementOfficer().getFirstName() + " "
                    + getSelectedPurchaseRequisition()
                            .getProcurementOfficer().getLastName());

            setCostingAndPaymentParameters(parameters);

            em.getTransaction().begin();

            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamedContent;

                    JasperReport jasperReport = JasperCompileManager
                            .compileReport((String) SystemOption.getOptionValueObject(
                                    getSystemManager().getEntityManager1(),
                                    "purchaseRequisition"));

                    JasperPrint print = JasperFillManager.fillReport(
                            jasperReport,
                            parameters,
                            con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamedContent = DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("Purchase Requisition - " + getSelectedPurchaseRequisition().getNumber() + ".pdf")
                            .stream(() -> new ByteArrayInputStream(fileBytes))
                            .build();

                    return streamedContent;

                } catch (JRException e) {
                    System.out.println("Error compiling purchase requisition: " + e);
                }
            }

            em.getTransaction().commit();

            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    public StreamedContent getPurchaseOrderFile() {
        StreamedContent streamContent = null;

        try {

            streamContent = getPurchaseOrderStreamContent(getEntityManager1());

        } catch (Exception e) {
            System.out.println(e);
        }

        return streamContent;
    }

    public StreamedContent getPurchaseOrderStreamContent(EntityManager em) {

        HashMap parameters = new HashMap();

        try {

            String logoURL = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), "logoSRC");
            String footNote1 = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), "QEMS_PO_Footnote1");
            String footNote2 = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), "QEMS_PO_Footnote2");
            String footNote3 = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), "QEMS_PO_Footnote3");
            String footNote4 = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), "QEMS_PO_Footnote4");

            parameters.put("prId", getSelectedPurchaseRequisition().getId());
            parameters.put("logoURL", logoURL);
            parameters.put("purchReqNo", getSelectedPurchaseRequisition().getNumber());
            parameters.put("purchaseOrderNo", getSelectedPurchaseRequisition().getPurchaseOrderNumber());
            parameters.put("addressLine1", getSelectedPurchaseRequisition()
                    .getSupplier().getDefaultAddress().getAddressLine1());
            parameters.put("addressLine2", getSelectedPurchaseRequisition()
                    .getSupplier().getDefaultAddress().getAddressLine2());
            parameters.put("suggestedSupplier", getSelectedPurchaseRequisition()
                    .getSupplier().getName());

            parameters.put("QEMS_PO_Footnote1", footNote1);
            parameters.put("QEMS_PO_Footnote2", footNote2);
            parameters.put("QEMS_PO_Footnote3", footNote3);
            parameters.put("QEMS_PO_Footnote4", footNote4);

            parameters.put("purposeOfOrder",
                    "Purpose: " + getSelectedPurchaseRequisition().getDescription());

            parameters.put("shippingInstructions",
                    getSelectedPurchaseRequisition().getShippingInstructions());
            parameters.put("terms", getSelectedPurchaseRequisition().getTerms());
            parameters.put("originatingDeptCode",
                    getSelectedPurchaseRequisition().getOriginatingDepartment().getCode());
            parameters.put("importLicenceNo", getSelectedPurchaseRequisition().getImportLicenceNum());
            parameters.put("deliveryDateRequired",
                    BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                            getDeliveryDateRequired()));

            if (getSelectedPurchaseRequisition().getPleaseSupplyNote().isEmpty()) {
                String companyTRN = SystemOption.getString(getSystemManager().getEntityManager1(),
                        "companyTRN");
                parameters.put("pleaseSupply", "TRN: " + companyTRN);
            } else {
                parameters.put("pleaseSupply", getSelectedPurchaseRequisition().getPleaseSupplyNote());
            }

            parameters.put("importLicenseDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                            getImportLicenceDate()));
            parameters.put("quotationNumber", getSelectedPurchaseRequisition().getQuotationNumber());

            parameters.put("requisitionDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(getSelectedPurchaseRequisition().
                            getRequisitionDate()));

            parameters.put("procurementOfficer", getSelectedPurchaseRequisition()
                    .getProcurementOfficer().getFirstName() + " "
                    + getSelectedPurchaseRequisition()
                            .getProcurementOfficer().getLastName());

            setCostingAndPaymentParameters(parameters);

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamedContent;

                    JasperReport jasperReport = JasperCompileManager
                            .compileReport((String) SystemOption.getOptionValueObject(
                                    getSystemManager().getEntityManager1(),
                                    "purchaseOrder"));

                    JasperPrint print = JasperFillManager.fillReport(
                            jasperReport,
                            parameters,
                            con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamedContent = DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("Purchase Order - " + getSelectedPurchaseRequisition().getPurchaseOrderNumber() + ".pdf")
                            .stream(() -> new ByteArrayInputStream(fileBytes))
                            .build();

                    em.getTransaction().commit();

                    return streamedContent;

                } catch (JRException e) {
                    System.out.println("Error compiling purchase order: " + e);
                }
            }

            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    public void updatePurchaseReq() {

        updatePurchaseReq(null);
    }

    public void updatePurchaseReq(AjaxBehaviorEvent event) {
        getSelectedPurchaseRequisition().setIsDirty(true);
        getSelectedPurchaseRequisition().setEditStatus("(edited)");

        if (getSelectedPurchaseRequisition().getCurrency().
                equals(getSelectedPurchaseRequisition().getPaymentCurrency())) {
            getSelectedPurchaseRequisition().setCurrencyExchangeRate(null);
        }

        getSelectedPurchaseRequisition().addAction(BusinessEntity.Action.EDIT);

    }

    public void updateAutoGeneratePRNumber() {

        if (getSelectedPurchaseRequisition().getAutoGenerateNumber()) {
            getSelectedPurchaseRequisition().generateNumber();
            getSelectedPurchaseRequisition().generatePurchaseOrderNumber();
        }

        updatePurchaseReq(null);
    }

    public void updateAutoGeneratePONumber() {

        if (getSelectedPurchaseRequisition().getAutoGenerateNumber()) {
            getSelectedPurchaseRequisition().generatePurchaseOrderNumber();
            getSelectedPurchaseRequisition().generateNumber();
        }

        updatePurchaseReq(null);
    }

    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    public void closePurchaseReqDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    public Boolean getIsSelectedPurchaseReqIsValid() {
        return getSelectedPurchaseRequisition().getId() != null
                && !getSelectedPurchaseRequisition().getIsDirty();
    }

    public PurchaseRequisition getSelectedPurchaseRequisition() {
        if (selectedPurchaseRequisition == null) {
            selectedPurchaseRequisition = new PurchaseRequisition();
        }
        return selectedPurchaseRequisition;
    }

    public void setSelectedPurchaseRequisition(
            PurchaseRequisition selectedPurchaseRequisition) {

        this.selectedPurchaseRequisition
                = getSavedPurchaseRequisition(selectedPurchaseRequisition);

    }

    public PurchaseRequisition getSavedPurchaseRequisition(PurchaseRequisition pr) {

        int i = 0;
        PurchaseRequisition foundPurchaseRequisition
                = PurchaseRequisition.findById(getEntityManager1(), pr.getId());

        for (PurchaseRequisition purchaseRequisition : foundPurchaseReqs) {
            if (Objects.equals(purchaseRequisition.getId(), foundPurchaseRequisition.getId())) {
                foundPurchaseReqs.set(i, foundPurchaseRequisition);
                break;
            }
            ++i;
        }

        return foundPurchaseRequisition;
    }

    public Attachment getSelectedAttachment() {
        if (selectedAttachment == null) {
            selectedAttachment = new Attachment();
        }
        return selectedAttachment;
    }

    public void setSelectedAttachment(Attachment selectedAttachment) {
        this.selectedAttachment = selectedAttachment;
    }

    public void savePurchaseRequisition(
            PurchaseRequisition purchaseRequisition,
            String msgSavedSummary,
            String msgSavedDetail) {

        EntityManager em = getEntityManager1();
        EntityManager smem = getSystemManager().getEntityManager1();

        ReturnMessage returnMessage = purchaseRequisition.prepareAndSave(
                em,
                smem,
                getUser());

        if (returnMessage.isSuccess()) {
            PrimeFacesUtils.addMessage(msgSavedSummary, msgSavedDetail, FacesMessage.SEVERITY_INFO);
            purchaseRequisition.setEditStatus(" ");

            processPRActions(purchaseRequisition, getUser());
        } else {
            PrimeFacesUtils.addMessage(returnMessage.getHeader(),
                    returnMessage.getMessage(),
                    FacesMessage.SEVERITY_ERROR);

            MailUtils.sendErrorEmail("An error occurred while saving a purchase requisition! - "
                    + returnMessage.getHeader(),
                    "Purchase requisition number: " + purchaseRequisition.getNumber()
                    + "\nJMTS User: " + getUser().getUsername()
                    + "\nDate/time: " + new Date()
                    + "\nDetail: " + returnMessage.getDetail(),
                    getSystemManager().getEntityManager1());
        }

    }

    public void saveSelectedPurchaseRequisition() {
        savePurchaseRequisition(getSelectedPurchaseRequisition(),
                "Saved", "Purchase requisition was saved");
    }

    private void emailProcurementOfficers(EntityManager em, PurchaseRequisition purchaseRequisition,
            String action) {

        List<Employee> procurementOfficers = Employee.
                findActiveByPosition(em,
                        "Procurement Officer");

        for (Employee procurementOfficer : procurementOfficers) {

            sendPurchaseReqEmail(em, purchaseRequisition, procurementOfficer,
                    "a procurement officer", action);
        }
    }

    private void notifyUserRePurchaseRequisition(EntityManager em,
            PurchaseRequisition purchaseRequisition,
            User user,
            String action) {

        Notification notification
                = new Notification("The purchase requisition (#" + purchaseRequisition.getNumber()
                        + ") was " + action);
        notification.setType("PRSearch");
        notification.setSubject("Purchase requisition " + action);
        notification.setMessage(purchaseRequisition.getId().toString());
        notification.setOwnerId(user.getId());
        notification.save(em);

    }

    private void notifyProcurementOfficers(EntityManager em, PurchaseRequisition purchaseRequisition,
            String action) {

        List<Employee> procurementOfficers = Employee.
                findActiveByPosition(em,
                        "Procurement Officer");

        for (Employee procurementOfficer : procurementOfficers) {
            User user = User.findActiveByEmployeeId(em,
                    procurementOfficer.getId());

            notifyUserRePurchaseRequisition(em, purchaseRequisition, user, action);

        }
    }

    private void emailDepartmentHead(EntityManager em, PurchaseRequisition purchaseRequisition, String action) {

        Employee head = purchaseRequisition.getOriginatingDepartment().getHead();

        sendPurchaseReqEmail(em, purchaseRequisition, head, "a department head", action);
    }

    private void notifyDepartmentHead(EntityManager em,
            PurchaseRequisition purchaseRequisition,
            String action) {

        Employee head = purchaseRequisition.getOriginatingDepartment().getHead();

        if (head != null) {
            User user = User.findActiveByEmployeeId(em,
                    head.getId());

            notifyUserRePurchaseRequisition(em, purchaseRequisition, user, action);

        }
    }

    /**
     * Email heads of divisions.
     *
     * @param em
     * @param purchaseRequisition
     * @param action
     */
    private void emailDivisionalHead(EntityManager em, PurchaseRequisition purchaseRequisition, String action) {

        Employee head = Division.findHeadOfActiveDivistionByDepartment(em,
                purchaseRequisition.getOriginatingDepartment());

        if (head != null) {
            sendPurchaseReqEmail(em, purchaseRequisition, head, "a divisional head", action);
        }

    }

    private void notifyDivisionalHead(EntityManager em,
            PurchaseRequisition purchaseRequisition,
            String action) {

        Employee head = Division.findHeadOfActiveDivistionByDepartment(em,
                purchaseRequisition.getOriginatingDepartment());

        if (head != null) {
            User user = User.findActiveByEmployeeId(em,
                    head.getId());

            notifyUserRePurchaseRequisition(em, purchaseRequisition, user, action);

        }
    }

    private void sendPurchaseReqEmail(
            EntityManager em,
            PurchaseRequisition purchaseRequisition,
            Employee employee,
            String role,
            String action) {

        Email email = Email.findActiveEmailByName(em, "pr-email-template");

        String prNum = purchaseRequisition.getNumber();
        String department = purchaseRequisition.
                getOriginatingDepartment().getName();
        String JMTSURL = (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(),
                "appURL");
        String originator = purchaseRequisition.getOriginator().getFirstName()
                + " " + purchaseRequisition.getOriginator().getLastName();
        String requisitionDate = BusinessEntityUtils.
                getDateInMediumDateFormat(purchaseRequisition.getRequisitionDate());
        String description = purchaseRequisition.getDescription();
        String approversAndRecommendersList = purchaseRequisition.getApproversAndRecommendersList();

        MailUtils.postMail(null,
                SystemOption.getString(
                        getSystemManager().getEntityManager1(),
                        "jobManagerEmailAddress"),
                SystemOption.getString(
                        getSystemManager().getEntityManager1(),
                        "jobManagerEmailName"),
                employee.getInternet().getEmail1(),
                email.getSubject().
                        replace("{action}", action).
                        replace("{purchaseRequisitionNumber}", prNum),
                email.getContent("/correspondences/").
                        replace("{firstname}",
                                employee.getFirstName()).
                        replace("{lastname}",
                                employee.getLastName()).
                        replace("{JMTSURL}", JMTSURL).
                        replace("{purchaseRequisitionNumber}", prNum).
                        replace("{supplier}", purchaseRequisition.getSupplier().getName()).
                        replace("{originator}", originator).
                        replace("{department}", department).
                        replace("{requisitionDate}", requisitionDate).
                        replace("{role}", role).
                        replace("{action}", action).
                        replace("{description}", description).
                        replace("{approversAndRecommendersList}", approversAndRecommendersList).
                        replace("{workProgress}", purchaseRequisition.getWorkProgress()),
                email.getContentType(),
                em);
    }

    private synchronized void processPRActions(PurchaseRequisition purchaseRequisition, User user) {

        if (purchaseRequisition.getId() != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        doProcessPRActions(purchaseRequisition);
                    } catch (Exception e) {
                        System.out.println("Error processing PR actions: " + e);
                    }
                }

            }.start();
        }
    }

    private synchronized void doProcessPRActions(PurchaseRequisition purchaseRequisition) {

        for (BusinessEntity.Action action : purchaseRequisition.getActions()) {
            switch (action) {
                case CREATE:
                    System.out.println("Processing CREATE action...");
                    notifyDepartmentHead(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "created");
                    emailDepartmentHead(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "created");
                    break;
                case EDIT:
                    System.out.println("EDIT action received but not processed.");
                    break;
                case APPROVE:
                    System.out.println("Processing APPROVE action...");
                    notifyProcurementOfficers(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "approved");
                    emailProcurementOfficers(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "approved");
                    break;
                case RECOMMEND:
                    System.out.println("Processing RECOMMEND action...");
                    notifyDivisionalHead(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "recommended");
                    emailDivisionalHead(
                            getSystemManager().getEntityManager1(),
                            purchaseRequisition, "recommended");
                    break;
                case COMPLETE:
                    System.out.println("COMPLETE action received but not processed.");
                    break;
                default:
                    break;
            }
        }

        purchaseRequisition.getActions().clear();

    }

    public void onPurchaseReqCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundPurchaseReqs().get(event.getRowIndex()));
    }

    public int getNumOfPurchaseReqsFound() {
        return getFoundPurchaseReqs().size();
    }

    public String getPurchaseReqsTableHeader() {
        if (getUser().can("BeFinancialAdministrator")) {
            return "Search Results (found: " + getNumOfPurchaseReqsFound() + ")";
        } else {
            return "Search Results (found: " + getNumOfPurchaseReqsFound() + " for "
                    + getEmployee().getDepartment() + ")";
        }
    }

    public void editPurhaseReqSupplier() {
        setSelectedSupplier(getSelectedPurchaseRequisition().getSupplier());

        editSelectedSupplier();
    }

    public void purchaseReqSupplierDialogReturn() {
        if (getSelectedSupplier().getId() != null) {
            getSelectedPurchaseRequisition().setSupplier(getSelectedSupplier());

        }
    }

    public void purchaseReqDialogReturn() {

        if (getSelectedPurchaseRequisition().getIsDirty()) {
            PrimeFacesUtils.addMessage("Purchase requisition NOT saved",
                    "The recently edited purchase requisition was not saved",
                    FacesMessage.SEVERITY_WARN);
            PrimeFaces.current().ajax().update("headerForm:growl3");

        }
        /*
        else {
            if (!getPurchaseReqSearchText().isEmpty()) {
                doPurchaseReqSearch();
            }
        }
         */
    }

    public void createNewPurhaseReqSupplier() {
        createNewSupplier();

        editSelectedSupplier();
    }

    public void editSelectedPurchaseReq() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("purchreqDialog", options, null);

    }

    public List<PurchaseRequisition> getFoundPurchaseReqs() {
        if (foundPurchaseReqs == null) {

            foundPurchaseReqs = new ArrayList<>();
        }
        return foundPurchaseReqs;
    }

    public void setFoundPurchaseReqs(List<PurchaseRequisition> foundPurchaseReqs) {
        this.foundPurchaseReqs = foundPurchaseReqs;
    }

    public void doPurchaseReqSearch(DatePeriod dateSearchPeriod,
            String searchType, String searchText, Long searchDepartmentId) {

        int maxSearchResults = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        foundPurchaseReqs = PurchaseRequisition.findByDateSearchField(
                getEntityManager1(),
                dateSearchPeriod.getDateField(),
                searchType,
                searchText,
                dateSearchPeriod.getStartDate(), dateSearchPeriod.getEndDate(),
                searchDepartmentId,
                maxSearchResults);

    }

    public String getPurchaseReqSearchText() {
        return purchaseReqSearchText;
    }

    public void setPurchaseReqSearchText(String purchaseReqSearchText) {
        this.purchaseReqSearchText = purchaseReqSearchText;
    }

    public void approvePurchaseReqs() {

        for (PurchaseRequisition purchaseRequisition : selectedPurchaseRequisitions) {
            approvePurchaseRequisition(purchaseRequisition);
        }

        getSelectedPurchaseRequisitions().clear();

    }

    public void recommendPurchaseReqs() {

        for (PurchaseRequisition purchaseRequisition : selectedPurchaseRequisitions) {
            recommendPurchaseRequisition(purchaseRequisition);
        }

        getSelectedPurchaseRequisitions().clear();

    }

    public HumanResourceManager getHumanResourceManager() {
        return BeanUtils.findBean("humanResourceManager");
    }

    public void createNewPurchaseReq() {
        EntityManager em = getEntityManager1();
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();
        EntityManager smem = getSystemManager().getEntityManager1();

        selectedPurchaseRequisition = PurchaseRequisition.create(
                em,
                hrmem,
                smem,
                getUser());

        openPurchaseReqsTab();

        editSelectedPurchaseReq();
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    @Override
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
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

        setSearchType("Purchase requisitions");
        setSearchText("");
        setDefaultCommandTarget(":mainTabViewForm:mainTabView:purchaseReqSearchButton");
        setModuleNames(new String[]{
            "systemManager",
            "financeManager",
            "purchasingManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "requisitionDate", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        selectedCostComponent = null;
        foundPurchaseReqs = new ArrayList<>();
        toEmployees = new ArrayList<>();
        supplierSearchText = "";
        purchaseReqSearchText = "";

    }

    @Override
    public EntityManager getEntityManager1() {
        return getFinanceManager().getEntityManager1();
    }

    public FinanceManager getFinanceManager() {
        return BeanUtils.findBean("financeManager");
    }

    public List<SelectItem> getProcurementMethods() {

        return getFinanceManager().getProcurementMethods();
    }

    public void onCostComponentSelect(SelectEvent event) {
        selectedCostComponent = (CostComponent) event.getObject();
    }

    public CostComponent getSelectedCostComponent() {
        return selectedCostComponent;
    }

    public void setSelectedCostComponent(CostComponent selectedCostComponent) {
        this.selectedCostComponent = selectedCostComponent;
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
    }

    public void updateSelectedCostComponent() {
        updateCostComponent(getSelectedCostComponent());
    }

    public void updateCostComponent(CostComponent costComponent) {

        costComponent.update();
        costComponent.setIsDirty(true);

    }

    public void updateCostType() {
        selectedCostComponent.update();
    }

    public Boolean getAllowCostEdit() {
        if (selectedCostComponent != null) {
            if (null == selectedCostComponent.getType()) {
                return true;
            } else {
                switch (selectedCostComponent.getType()) {
                    case "--":
                        return true;
                    default:
                        return false;
                }
            }
        } else {
            return true;
        }
    }

    public void updateIsCostComponentHeading() {

    }

    public void updateIsCostComponentFixedCost() {

        if (getSelectedCostComponent().getIsFixedCost()) {

        }
    }

    public void deleteSelectedCostComponent() {
        deleteCostComponentByName(selectedCostComponent.getName());
    }

    public void deleteCostComponentByName(String componentName) {

        List<CostComponent> components = getSelectedPurchaseRequisition().getAllSortedCostComponents();
        int index = 0;
        for (CostComponent costComponent : components) {
            if (costComponent.getName().equals(componentName)) {
                components.remove(index);
                updatePurchaseReq(null);

                break;
            }
            ++index;
        }
    }

    public void deleteAttachmentByName(String attachmentName) {

        List<Attachment> attachments = getSelectedPurchaseRequisition().getAttachments();
        int index = 0;
        for (Attachment attachment : attachments) {
            if (attachment.getName().equals(attachmentName)) {
                attachments.remove(index);
                attachment.deleteFile();
                updatePurchaseReq(null);
                saveSelectedPurchaseRequisition();

                break;
            }
            ++index;
        }
    }

    public void editCostComponent(ActionEvent event) {
        setEdit(true);
    }

    public void createNewCostComponent() {

        selectedCostComponent = new CostComponent();

        selectedCostComponent.setHoursOrQuantity(1.0);
        selectedCostComponent.setType("Variable");
        selectedCostComponent.setUnit("each");
        selectedCostComponent.update();

        setEdit(false);

    }

    public void addNewCostComponent() {

        selectedCostComponent = new CostComponent();
        selectedCostComponent.setName("Item");
        selectedCostComponent.setHoursOrQuantity(1.0);
        selectedCostComponent.setType("Variable");
        selectedCostComponent.setUnit("each");
        selectedCostComponent.update();
        selectedCostComponent.setCurrency(getSelectedPurchaseRequisition().getCurrency());
        selectedCostComponent.setCurrencyExchangeRate(getSelectedPurchaseRequisition()
                .getCurrencyExchangeRate());

        getSelectedPurchaseRequisition().getAllSortedCostComponents().add(selectedCostComponent);

        updatePurchaseReq(null);

        FacesMessage msg = new FacesMessage("New Cost Component Added",
                "Click on the pencil icon to edit");

        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    public void addNewAttachment(ActionEvent event) {
        addAttachment();
    }

    public void addAttachment() {

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

        PrimeFaces.current().dialog().openDynamic("/admin/attachmentDialog",
                options, null);

    }

    public void approveSelectedPurchaseRequisition(ActionEvent event) {

        approvePurchaseRequisition(getSelectedPurchaseRequisition());
    }

    public void approvePurchaseRequisition(PurchaseRequisition purchaseRequisition) {

        // Check if the approver is already in the list of approvers
        if (BusinessEntityUtils.isBusinessEntityInList(
                purchaseRequisition.getApproversAndRecommenders(),
                getEmployee().getId())) {

            PrimeFacesUtils.addMessage("Already Approved/Recommended",
                    "You already approved/recommended this purchase requisition",
                    FacesMessage.SEVERITY_INFO);

            return;

        }

        // Do not allow originator to approve
        if (purchaseRequisition.getOriginator().
                equals(getEmployee())) {

            PrimeFacesUtils.addMessage("Cannot Approve",
                    "The originator cannot approve this purchase requisition",
                    FacesMessage.SEVERITY_WARN);

            return;

        }

        // Check if total cost is within the approver's limit
        if (isPRCostWithinApprovalLimit(
                purchaseRequisition,
                getEmployee().getPositions())) {

            if (!addApprover(purchaseRequisition, getEmployee())) {
                PrimeFacesUtils.addMessage("Not Approved",
                        "The maximum number of approvers was reached or\n"
                        + "this purchase requisition has not been recommended.",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Set expected date of completion if it is not already set and the 
            // required number of approvals received.            
            if (purchaseRequisition.getExpectedDateOfCompletion() == null) {
                int requiredApprovals
                        = (Integer) SystemOption.getOptionValueObject(
                                getSystemManager().getEntityManager1(),
                                "requiredPRApprovals");

                if (purchaseRequisition.getApprovals() >= requiredApprovals) {

                    int daysAfterPRApprovalForEDOC
                            = (Integer) SystemOption.getOptionValueObject(
                                    getSystemManager().getEntityManager1(),
                                    "daysAfterPRApprovalForEDOC");

                    purchaseRequisition
                            .setExpectedDateOfCompletion(
                                    BusinessEntityUtils.adjustDate(new Date(),
                                            Calendar.DAY_OF_MONTH, daysAfterPRApprovalForEDOC));
                }
            }

            purchaseRequisition.setIsDirty(true);
            purchaseRequisition.setEditStatus("(edited)");

            if (purchaseRequisition.getId() != null) {

                purchaseRequisition.addAction(BusinessEntity.Action.APPROVE);

                savePurchaseRequisition(purchaseRequisition,
                        "Approved and Saved",
                        "This purchase requisition " + purchaseRequisition.getPurchaseOrderNumber()
                        + " was successfully approved and saved");
            }

        } else {

            PrimeFacesUtils.addMessage("Cannot Approve",
                    "You cannot approve purchase requisition "
                    + purchaseRequisition.getPurchaseOrderNumber() + " because the Total Cost is greater than your approval limit",
                    FacesMessage.SEVERITY_WARN);

        }
    }

    public void recommendSelectedPurchaseRequisition(ActionEvent event) {

        recommendPurchaseRequisition(getSelectedPurchaseRequisition());

    }

    public void recommendPurchaseRequisition(PurchaseRequisition purchaseRequisition) {

        EntityManager em = getEntityManager1();

        // Check if the recommender is already in the list of approvers/recommenders
        if (BusinessEntityUtils.isBusinessEntityInList(
                purchaseRequisition.getApproversAndRecommenders(),
                getEmployee().getId())) {

            PrimeFacesUtils.addMessage("Already Approved/Recommended",
                    "You already approved/recommended this purchase requisition",
                    FacesMessage.SEVERITY_INFO);

            return;

        }

        // Check if originator can recommend
        if (purchaseRequisition.getOriginator().
                equals(getEmployee()) && !getUser().can("RecommendPurchaseRequisition")) {

            PrimeFacesUtils.addMessage("Cannot Recommend",
                    "The originator cannot recommend approval of this purchase requisition",
                    FacesMessage.SEVERITY_WARN);

            return;

        }

        if (!addRecommender(purchaseRequisition, getEmployee())) {
            PrimeFacesUtils.addMessage("Not Recommended",
                    "The maximum number of recommenders was reached for this purchase requisition "
                    + "or you do not have the privilege to recommend purchase requisitions.",
                    FacesMessage.SEVERITY_ERROR);

            return;
        }

        // Set expected date of completion if it is not already set and the 
        // required number of approvals received.            
        if (purchaseRequisition.getExpectedDateOfCompletion() == null) {
            int requiredApprovals
                    = (Integer) SystemOption.getOptionValueObject(
                            getSystemManager().getEntityManager1(),
                            "requiredPRApprovals");

            if (purchaseRequisition.getApprovals() >= requiredApprovals) {

                int daysAfterPRApprovalForEDOC
                        = (Integer) SystemOption.getOptionValueObject(
                                getSystemManager().getEntityManager1(),
                                "daysAfterPRApprovalForEDOC");

                purchaseRequisition
                        .setExpectedDateOfCompletion(
                                BusinessEntityUtils.adjustDate(new Date(),
                                        Calendar.DAY_OF_MONTH, daysAfterPRApprovalForEDOC));
            }
        }

        purchaseRequisition.setIsDirty(true);
        purchaseRequisition.setEditStatus("(edited)");

        if (purchaseRequisition.getId() != null) {
            purchaseRequisition.addAction(BusinessEntity.Action.RECOMMEND);

            savePurchaseRequisition(purchaseRequisition,
                    "Recommended and Saved",
                    "Recommendation for the approval of this purchase requisition was successful");

        }

    }

    private Boolean isPRCostWithinApprovalLimit(
            PurchaseRequisition purchaseRequisition,
            List<EmployeePosition> positions) {

        for (EmployeePosition position : positions) {
            if (position.getUpperApprovalLevel()
                    >= purchaseRequisition.getTotalCostComponentCosts()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the position that can approve or recommend a PR.
     *
     * @param purchaseRequisition
     * @param positions
     * @return
     */
    private EmployeePosition getApprovalOrRecommendationPosition(
            PurchaseRequisition purchaseRequisition,
            List<EmployeePosition> positions) {

        for (EmployeePosition position : positions) {
            if (position.getUpperApprovalLevel()
                    >= purchaseRequisition.getTotalCostComponentCosts()) {
                return position;
            }
        }

        return new EmployeePosition();
    }

    public void cancelCostComponentEdit() {
        selectedCostComponent.setIsDirty(false);
    }

    public List<CostComponent> copyCostComponents(List<CostComponent> srcCostComponents) {
        ArrayList<CostComponent> newCostComponents = new ArrayList<>();

        for (CostComponent costComponent : srcCostComponents) {
            CostComponent newCostComponent = new CostComponent(costComponent);
            newCostComponents.add(newCostComponent);
        }

        return newCostComponents;
    }

    public List<SelectItem> getPriorityCodes() {

        return getStringListAsSelectItems(
                getSystemManager().getEntityManager1(),
                "prPriorityCodes");
    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        int maxSearchResults = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        switch (searchType) {
            case "Purchase requisitions":

                foundPurchaseReqs = PurchaseRequisition.findByDateSearchField(
                        getEntityManager1(),
                        dateSearchField,
                        searchType,
                        searchText,
                        startDate,
                        endDate,
                        searchDepartmentId,
                        maxSearchResults);

                openPurchaseReqsTab();
                break;
            case "Suppliers":
                doSupplierSearch(searchText);
                openSuppliersTab();
                break;
            default:
                break;
        }
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Procurement");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList dateSearchFields = new ArrayList();

        setSearchType(searchType);

        switch (searchType) {
            case "Suppliers":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                break;
            case "Purchase requisitions":
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
                break;
            default:
                break;
        }

        return dateSearchFields;
    }

    private void setCostingAndPaymentParameters(HashMap parameters) {

        parameters.put("costingCurrencySymbol",
                getSelectedPurchaseRequisition().getCurrency().getSymbol());
        parameters.put("paymentCurrencySymbol",
                getSelectedPurchaseRequisition().getPaymentCurrency().getSymbol());
        if ((getSelectedPurchaseRequisition().getCurrency().
                equals(getSelectedPurchaseRequisition().getPaymentCurrency()))
                || (getSelectedPurchaseRequisition().getPaymentCurrency() == null)
                || (getSelectedPurchaseRequisition().getCurrency() == null)) {
            parameters.put("currencyExchangeRateLabel", "");
            parameters.put("currencyExchangeRate", null);
        } else {
            parameters.put("currencyExchangeRate", getSelectedPurchaseRequisition().getCurrencyExchangeRate());
        }
        parameters.put("totalCost", getSelectedPurchaseRequisition().getConvertedTotalCost());

    }

}
