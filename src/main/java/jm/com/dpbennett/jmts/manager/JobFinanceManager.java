/*
Job Management & Tracking System (JMTS) 
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
package jm.com.dpbennett.jmts.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.fm.CashPayment;
import jm.com.dpbennett.business.entity.fm.CostCode;
import jm.com.dpbennett.business.entity.fm.CostComponent;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.DepartmentUnit;
import jm.com.dpbennett.business.entity.fm.Discount;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.fm.JobCosting;
import jm.com.dpbennett.business.entity.jmts.JobCostingAndPayment;
import jm.com.dpbennett.business.entity.hrm.Laboratory;
import jm.com.dpbennett.business.entity.sm.Preference;
import jm.com.dpbennett.business.entity.fm.Service;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.fm.Tax;
import jm.com.dpbennett.business.entity.fm.UnitCost;
import jm.com.dpbennett.business.entity.fm.AccPacCustomer;
import jm.com.dpbennett.business.entity.fm.AccPacDocument;
import jm.com.dpbennett.business.entity.fm.AccountingCode;
import jm.com.dpbennett.business.entity.fm.Classification;
import jm.com.dpbennett.business.entity.fm.Currency;
import jm.com.dpbennett.business.entity.jmts.Job;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import jm.com.dpbennett.business.entity.gm.BusinessEntityManagement;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityActionUtils;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.FileUtils;
import jm.com.dpbennett.sm.util.FinancialUtils;
import jm.com.dpbennett.sm.util.JobDataModel;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.ReportUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DialogFrameworkOptions;

/**
 * This class handles financial matters pertaining to a job.
 *
 * @author Desmond P. Bennett (info@dpbenentt.com.jm)
 */
public class JobFinanceManager implements Serializable, BusinessEntityManagement {

    private CashPayment selectedCashPayment;
    private StreamedContent jobCostingFile;
    private Integer longProcessProgress;
    private AccPacCustomer accPacCustomer;
    private List<AccPacDocument> filteredAccPacCustomerDocuments;
    private Boolean useAccPacCustomerList;
    private CostComponent selectedCostComponent;
    private JobCostingAndPayment selectedJobCostingAndPayment;
    private String selectedJobCostingTemplate;
    private Department unitCostDepartment;
    private UnitCost currentUnitCost;
    private List<UnitCost> unitCosts;
    private List<Job> jobsWithCostings;
    private Job currentJobWithCosting;
    private Department jobCostDepartment;
    private Boolean showPrepayments;
    private Boolean enableOnlyPaymentEditing;
    private JobManager jobManager;
    private JobContractManager jobContractManager;
    private Boolean edit;
    private String fileDownloadErrorMessage;
    private List<JobCostingAndPayment> foundJobCostingAndPayments;
    private Boolean isActiveJobCostingAndPaymentsOnly;
    private String jobCostingAndPaymentSearchText;
    private Job[] selectedJobs;
    private List<Job> jobSearchResultList;
    private DatePeriod dateSearchPeriod;
    private String searchType;
    private String searchText;
    private String proformaInvoiceSearchText;
    private String costEstimateSearchText;

    public JobFinanceManager() {
        init();
    }

    public void openJobCostingAndPaymentDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobCostingAndPayment();

        } else {

            if (getJobManager().getCurrentJob().getIsDirty()) {
                getJobManager().saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobCostingAndPayment();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the job costing and payment can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void jobCostingAndPaymentDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    getJobManager().processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Costing & Payment"
                            + " Saved", "This job"
                            + " and the costing and payment were saved", FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Costing & Payment"
                            + " NOT Saved", "This job"
                            + " and the costing and payment were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void setEditJobCosting(Job currentJob) {

        setCurrentJob(getJobManager().getSavedCurrentJob(currentJob));
        getCurrentJob().setVisited(true);

        setSelectedJobs(new Job[]{});
    }

    public void setEditJobCostingAndPayment(Job currentJob) {
        setCurrentJob(getJobManager().getSavedCurrentJob(currentJob));
        getCurrentJob().setVisited(true);

        setEnableOnlyPaymentEditing(true);
    }

    public void editJobCostingAndPayment() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/jobCostingAndPaymentDialog", options, null);

    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public String getProformaInvoiceSearchText() {
        return proformaInvoiceSearchText;
    }

    public void setProformaInvoiceSearchText(String proformaInvoiceSearchText) {
        this.proformaInvoiceSearchText = proformaInvoiceSearchText;
    }

    public String getCostEstimateSearchText() {
        return costEstimateSearchText;
    }

    public void setCostEstimateSearchText(String costEstimateSearchText) {
        this.costEstimateSearchText = costEstimateSearchText;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchText() {
        return searchText;
    }

    public List<Job> getJobSearchResultList() {

        if (jobSearchResultList == null) {
            jobSearchResultList = new ArrayList<>();
        }

        return jobSearchResultList;
    }

    public Job[] getSelectedJobs() {
        if (selectedJobs == null) {
            selectedJobs = new Job[]{};
        }
        return selectedJobs;
    }

    public void setSelectedJobs(Job[] selectedJobs) {
        this.selectedJobs = selectedJobs;
    }

    public JobDataModel getJobsModel() {
        return new JobDataModel(jobSearchResultList);
    }

    public void onJobCostingSelect(SelectEvent event) {
    }

    public void onJobCostingUnSelect(UnselectEvent event) {
    }

    public String getSearchResultsTableHeader() {
        return ReportUtils.getSearchResultsTableHeader(
                getJobManager().getDateSearchPeriod(),
                getJobSearchResultList());
    }

    public void closeProformaInvoicesDialog() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void closePriceListDialog() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void openProformaInvoicesTab() {

        getMainTabView().openTab("Proforma Invoices");
    }

    public void openPriceListTab() {

        getMainTabView().openTab("Price List");
    }

    public void openProformaInvoiceDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 350) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/proformaInvoiceDialog", options, null);

    }

    public void openJobCostEstimateDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 350) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/jobCostEstimateDialog", options, null);

    }

    public void proformaDialogReturn() {

        doJobSearch();
    }

    public void costEstimateDialogReturn() {

        doJobSearch();
    }

    public void openNewProformaInvoiceDialog() {

        createNewJob();

        getCurrentJob().setType("Proforma Invoice");
        getCurrentJob().setAssignedTo(getUser().getEmployee());
        getCurrentJob().getJobStatusAndTracking().setDateAndTimeEntered(new Date());
        // tk job type field to be used where applicable instead of setting progress to cancelled
        getCurrentJob().getJobStatusAndTracking().setWorkProgress("Cancelled");
        getCurrentJob().getJobCostingAndPayment().setEstimate(true);
        getCurrentJob().getJobCostingAndPayment().
                setTax(Tax.findByName(getEntityManager1(),
                        (String) SystemOption.getOptionValueObject(getEntityManager1(),
                                "defaultTax")));
        getCurrentJob().setJobNumber(Job.generateJobNumber(getCurrentJob(),
                getEntityManager1()));

        getJobManager().editJob();

        openProformaInvoicesTab();
    }

    public void openNewCostEstimateDialog() {

        createNewJob();

        getCurrentJob().setClassification(Classification.
                findClassificationByName(getEntityManager1(),
                        (String) SystemOption.getOptionValueObject(getEntityManager1(),
                                "defaultJobClassification")));
        getCurrentJob().setAssignedTo(getUser().getEmployee());
        getCurrentJob().getJobStatusAndTracking().setDateAndTimeEntered(new Date());
        getCurrentJob().getJobStatusAndTracking().setWorkProgress("Cancelled");
        getCurrentJob().getJobCostingAndPayment().setEstimate(true);
        getCurrentJob().setJobNumber(Job.generateJobNumber(getCurrentJob(),
                getEntityManager1()));
        getCurrentJob().setSubContractedDepartment(Department.findDefault(
                getEntityManager1(), "--"));
        getCurrentJob().setClient(Client.findActiveDefault(
                getEntityManager1(), "--", true));
        getCurrentJob().setBillingAddress(getCurrentJob().getClient().getAddresses().get(0));
        getCurrentJob().setContact(getCurrentJob().getClient().getContacts().get(0));

        openJobCostEstimateDialog();
    }

    public void createNewJob() {

        EntityManager em = getEntityManager1();

        getJobManager().createJob(em, false, false);

        setEnableOnlyPaymentEditing(false);

    }

    public String getJobCostingAndPaymentSearchText() {
        return jobCostingAndPaymentSearchText;
    }

    public void setJobCostingAndPaymentSearchText(String jobCostingAndPaymentSearchText) {
        this.jobCostingAndPaymentSearchText = jobCostingAndPaymentSearchText;
    }

    public Boolean getIsActiveJobCostingAndPaymentsOnly() {
        return isActiveJobCostingAndPaymentsOnly;
    }

    public void setIsActiveJobCostingAndPaymentsOnly(Boolean isActiveJobCostingAndPaymentsOnly) {
        this.isActiveJobCostingAndPaymentsOnly = isActiveJobCostingAndPaymentsOnly;
    }

    public void doJobCostingAndPaymentSearch() {

        if (getIsActiveJobCostingAndPaymentsOnly()) {
            foundJobCostingAndPayments
                    = completeJobCostingAndPaymentName(getJobCostingAndPaymentSearchText());
        } else {
            foundJobCostingAndPayments
                    = completeAllJobCostingAndPaymentName(getJobCostingAndPaymentSearchText());
        }

    }

    public List<JobCostingAndPayment> getFoundJobCostingAndPayments() {
        if (foundJobCostingAndPayments == null) {

            foundJobCostingAndPayments = completeJobCostingAndPaymentName("");
        }

        return foundJobCostingAndPayments;
    }

    public void setFoundJobCostingAndPayments(List<JobCostingAndPayment> foundJobCostingAndPayments) {
        this.foundJobCostingAndPayments = foundJobCostingAndPayments;
    }

    /**
     * Attempts to approve the selected job costing(s).
     *
     * @see
     * JobFinanceManager#canChangeJobCostingApprovalStatus(jm.com.dpbennett.business.entity.Job)
     */
    public void approveSelectedJobCostings() {
        int numCostingsCApproved = 0;

        if (getJobManager().getSelectedJobs().length > 0) {
            EntityManager em = getEntityManager1();

            for (Job job : getJobManager().getSelectedJobs()) {
                if (!job.getJobCostingAndPayment().getCostingApproved()) {
                    if (canChangeJobCostingApprovalStatus(job, true)) {
                        numCostingsCApproved++;

                        job.getJobCostingAndPayment().setCostingApproved(true);
                        job.getJobStatusAndTracking().setDateCostingApproved(new Date());
                        job.getJobCostingAndPayment().setCostingApprovedBy(
                                getUser().getEmployee());
                        job.getJobCostingAndPayment().setIsDirty(true);

                        job.save(em);
                    } else {

                        return;
                    }

                } else {
                    PrimeFacesUtils.addMessage("Aready Approved",
                            "This " + job.getType() + " costing for " + job.getJobNumber() + " was already approved",
                            FacesMessage.SEVERITY_WARN);
                }
            }

            PrimeFacesUtils.addMessage("Job Costing(s) Approved",
                    "" + numCostingsCApproved + " Job costing(s) approved",
                    FacesMessage.SEVERITY_INFO);

        } else {
            PrimeFacesUtils.addMessage("No Selection",
                    "No Job costing was selected",
                    FacesMessage.SEVERITY_WARN);
        }

    }

    /**
     * Attempts to create an invoice for the job costing of the specified job.
     *
     * @see
     * JobFinanceManager#canChangeJobCostingApprovalStatus(jm.com.dpbennett.business.entity.Job)
     * @param job
     * @param invoice
     * @return
     */
    public Boolean invoiceJobCosting(Job job, Boolean invoice) {

        prepareToInvoiceJobCosting(job);

        if (canInvoiceJobCosting(job)) {
            if (invoice) {
                job.getJobCostingAndPayment().setInvoiced(invoice);
                job.getJobStatusAndTracking().setDateCostingInvoiced(new Date());
                job.getJobCostingAndPayment().setCostingInvoicedBy(getUser().getEmployee());
            } else {
                job.getJobCostingAndPayment().setInvoiced(invoice);
                job.getJobStatusAndTracking().setDateCostingInvoiced(null);
                job.getJobCostingAndPayment().setCostingInvoicedBy(null);
            }
            setJobCostingAndPaymentDirty(job, true);

            return true;
        } else {
            // Reset invoiced status
            job.getJobCostingAndPayment().setInvoiced(!invoice);

            return false;
        }
    }

    /**
     * Attempts to create invoices for job costings of the selected jobs.
     */
    public void invoiceSelectedJobCostings() {
        int numInvoicesCreated = 0;

        try {
            if (getJobManager().getSelectedJobs().length > 0) {
                EntityManager em = getEntityManager1();

                for (Job job : getJobManager().getSelectedJobs()) {
                    if (!job.getJobCostingAndPayment().getInvoiced()) {
                        if (invoiceJobCosting(job, true)) {
                            job.save(em);
                            numInvoicesCreated++;
                        } else {
                            return;
                        }

                    } else {
                        PrimeFacesUtils.addMessage("Aready Invoiced",
                                "The " + job.getType() + " costing for " + job.getJobNumber() + " was already invoiced",
                                FacesMessage.SEVERITY_WARN);

                        return;
                    }
                }

                PrimeFacesUtils.addMessage("Invoice(s) Created",
                        "" + numInvoicesCreated + " invoice(s) created",
                        FacesMessage.SEVERITY_INFO);

            } else {
                PrimeFacesUtils.addMessage("No Selection",
                        "No job costing was selected",
                        FacesMessage.SEVERITY_WARN);
            }
        } catch (Exception e) {
            System.out.println("Error occurred while invoicing: " + e);
            PrimeFacesUtils.addMessage("Invoicing Error",
                    "An error occurred while creating one or more invoices. "
                    + "Please check that all required information such as a client Id is provided.",
                    FacesMessage.SEVERITY_ERROR);
        }

    }

    /**
     * Gets the total tax in the default currency associated with the specified
     * job.
     *
     * @param job
     * @return
     */
    public Double getTotalTax(Job job) {
        return job.getJobCostingAndPayment().getTotalTax();
    }

    /**
     * Gets the total discount in the default currency associated with the
     * specified job.
     *
     * @param job
     * @return
     */
    public Double getTotalDiscount(Job job) {
        return job.getJobCostingAndPayment().getTotalDiscount();
    }

    /**
     * Gets the tax object associated with the specified job. A default tax
     * object with a value of 0.0 is set and returned if the tax object is not
     * set.
     *
     * @param job
     * @return
     */
    public Tax getTax(Job job) {
        Tax tax = job.getJobCostingAndPayment().getTax();

        // Handle the case where the tax is not set
        if (tax.getId() == null) {
            if (job.getJobCostingAndPayment().getPercentageGCT() != null) {
                // Find and use tax object 
                Tax tax2 = Tax.findByValue(getEntityManager1(),
                        Double.parseDouble(job.getJobCostingAndPayment().getPercentageGCT()));
                if (tax2 != null) {
                    tax = tax2;
                    job.getJobCostingAndPayment().setTax(tax2);
                } else {
                    tax = Tax.findDefault(getEntityManager1(), "0.0");
                    job.getJobCostingAndPayment().setTax(tax);
                }
            } else {
                tax = Tax.findDefault(getEntityManager1(), "0.0");
                job.getJobCostingAndPayment().setTax(tax);
            }
        }

        return tax;
    }

    /**
     * Gets the tax associated with the current job.
     *
     * @return
     */
    public Tax getTax() {
        return getTax(getCurrentJob());
    }

    /**
     * Sets the tax associated with the current job.
     *
     * @param tax
     */
    public void setTax(Tax tax) {

        getCurrentJob().getJobCostingAndPayment().setTax(tax);
    }

    /**
     * Gets the discount associated with the current job.
     *
     * @return
     */
    public Discount getDiscount() {

        return getDiscount(getCurrentJob());
    }

    /**
     * Gets the discount associated with the specified job. The default discount
     * object is set and returned if the discount object is not set.
     *
     * @param job
     * @return
     */
    public Discount getDiscount(Job job) {

        Discount discount = job.getJobCostingAndPayment().getDiscount();

        // Handle the case where the discount object is not set.
        if (discount.getId() == null) {
            discount = Discount.findByValueAndType(
                    getEntityManager1(),
                    job.getJobCostingAndPayment().getDiscountValue(),
                    job.getJobCostingAndPayment().getDiscountType());

            if (discount == null) {

                discount = Discount.findDefault(
                        getEntityManager1(),
                        job.getJobCostingAndPayment().getDiscountValue().toString(),
                        job.getJobCostingAndPayment().getDiscountValue(),
                        job.getJobCostingAndPayment().getDiscountType());

                job.getJobCostingAndPayment().setDiscount(discount);

            } else {
                job.getJobCostingAndPayment().setDiscount(discount);
            }

        }

        return discount;
    }

    /**
     * Sets the discount associated with the current job.
     *
     * @param discount
     */
    public void setDiscount(Discount discount) {

        getCurrentJob().getJobCostingAndPayment().setDiscount(discount);
    }

    /**
     * Returns a list of discount types.
     *
     * @return
     */
    public List getDiscountTypes() {

        return FinancialUtils.getDiscountTypes(getEntityManager1());
    }

    /**
     * Returns a list of CostComponent cost types.
     *
     * @return
     */
    public List getCostTypeList() {
        return FinancialUtils.getCostTypeList(getEntityManager1());
    }

    /**
     * Returns a list of cash payment types.
     *
     * @return
     */
    public List getPaymentTypes() {
        return FinancialUtils.getPaymentTypes(getEntityManager1());
    }

    /**
     * Returns a list of cash payment purposes.
     *
     * @return
     */
    public List getPaymentPurposes() {
        return FinancialUtils.getPaymentPurposes(getEntityManager1());
    }

    /**
     * Closes a PrimeFaces dialog.
     */
    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    /**
     * Gets the selected JobCostingAndPayment object.
     *
     * @return
     */
    public JobCostingAndPayment getSelectedJobCostingAndPayment() {
        return selectedJobCostingAndPayment;
    }

    /**
     * Sets the selected JobCostingAndPayment object.
     *
     * @param selectedJobCostingAndPayment
     */
    public void setSelectedJobCostingAndPayment(JobCostingAndPayment selectedJobCostingAndPayment) {
        this.selectedJobCostingAndPayment = selectedJobCostingAndPayment;
    }

    /**
     * Cancels/closes a PrimeFaces dialog.
     *
     * @param actionEvent
     */
    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    /**
     * Gets the main view of tabs associated with the web application.
     *
     * @return
     */
    public MainTabView getMainTabView() {

        return getJobManager().getMainTabView();
    }

    /**
     * Get all cost components without heading.
     *
     * @param jobCostingAndPayment
     * @return
     */
    public List<CostComponent> getCostComponentsWithoutHeadings(JobCostingAndPayment jobCostingAndPayment) {
        List<CostComponent> costComponents = new ArrayList<>();

        for (CostComponent costComponent : jobCostingAndPayment.getAllSortedCostComponents()) {
            if (!costComponent.getIsHeading()) {
                costComponents.add(costComponent);
            }
        }

        return costComponents;
    }

    public StreamedContent getCostingsFileAsZip() {

        try {

            ByteArrayInputStream stream = getCostingsFileInputStreamAsZip();

            setLongProcessProgress(100);

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType("application/zip")
                    .name("Job Costings - "
                            + BusinessEntityUtils.getDateInMediumDateAndTimeFormat(new Date())
                            + ".zip")
                    .build();

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    public StreamedContent getCostingsFileAsExcel() {

        try {
            ByteArrayInputStream stream;

            stream = getCostingsFileInputStreamAsExcel(
                    new File(getClass().getClassLoader().
                            getResource("/reports/"
                                    + (String) SystemOption.getOptionValueObject(getEntityManager1(),
                                            "JobCostingAnalysesTemplate")).getFile()));

            setLongProcessProgress(100);

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType("application/xlsx")
                    .name("Job Costings -" + BusinessEntityUtils.getDateInMediumDateAndTimeFormat(new Date()) + "-" + fileDownloadErrorMessage + ".xlsx")
                    .build();

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    public ByteArrayInputStream getCostingsFileInputStreamAsZip() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EntityManager em = getEntityManager1();

        // Get costing data
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);
            // Get costing data
            Job reportData[] = getJobManager().getSelectedJobs();
            for (Job selectedJob : reportData) {
                HashMap parameters = new HashMap();

                parameters.put("jobId", selectedJob.getId());
                parameters.put("contactPersonName", BusinessEntityUtils.getContactFullName(selectedJob.getContact()));
                parameters.put("customerAddress", selectedJob.getBillingAddress().toString());
                parameters.put("contactNumbers", selectedJob.getContact().getMainPhoneNumber().getLocalNumber());
                parameters.put("jobDescription", selectedJob.getJobDescription());
                parameters.put("totalCost", selectedJob.getJobCostingAndPayment().getTotalJobCostingsAmount());
                parameters.put("depositReceiptNumbers", selectedJob.getJobCostingAndPayment().getReceiptNumbers());
                parameters.put("discount", selectedJob.getJobCostingAndPayment().getDiscount().getDiscountValue());
                parameters.put("discountType", selectedJob.getJobCostingAndPayment().getDiscount().getDiscountValueType());
                parameters.put("deposit", selectedJob.getJobCostingAndPayment().getTotalPayment());
                parameters.put("amountDue", selectedJob.getJobCostingAndPayment().getAmountDue());
                parameters.put("totalTax", selectedJob.getJobCostingAndPayment().getTotalTax());
                parameters.put("totalTaxLabel", selectedJob.getJobCostingAndPayment().getTotalTaxLabel());
                parameters.put("grandTotalCostLabel", selectedJob.getJobCostingAndPayment().getTotalCostWithTaxLabel().toUpperCase().trim());
                parameters.put("grandTotalCost", selectedJob.getJobCostingAndPayment().getTotalCost());
                if (selectedJob.getJobCostingAndPayment().getCostingPreparedBy() != null) {
                    parameters.put("preparedBy",
                            selectedJob.getJobCostingAndPayment().getCostingPreparedBy().getFirstName() + " "
                            + selectedJob.getJobCostingAndPayment().getCostingPreparedBy().getLastName());
                }
                if (selectedJob.getJobCostingAndPayment().getCostingApprovedBy() != null) {
                    parameters.put("approvedBy",
                            selectedJob.getJobCostingAndPayment().getCostingApprovedBy().getFirstName() + " "
                            + selectedJob.getJobCostingAndPayment().getCostingApprovedBy().getLastName());
                }
                parameters.put("approvalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(
                                selectedJob.getJobStatusAndTracking().getDateCostingApproved()));

                try {
                    String costingFilename
                            = "Job Costing - "
                            + selectedJob.getJobNumber().replace("/", "_")
                            + ".pdf";

                    // Compile job costing
                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(getEntityManager1(), "jobCosting"));

                    // Generate job costing
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);
                    byte[] costingFileBytes = JasperExportManager.exportReportToPdf(print);

                    FileUtils.zipFile(costingFilename, costingFileBytes, zos);

                } catch (JRException ex) {
                    Logger.getLogger(JobFinanceManager.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            zos.close();

            em.getTransaction().commit();

            return new ByteArrayInputStream(baos.toByteArray());

        } catch (IOException ex) {
            Logger.getLogger(JobFinanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ByteArrayInputStream getCostingsFileInputStreamAsExcel(
            File file) {

        try {
            FileInputStream inp = new FileInputStream(file);
            int costingRow = 1;
            int costingCol;

            fileDownloadErrorMessage = "";

            XSSFWorkbook wb = new XSSFWorkbook(inp);
            XSSFCellStyle stringCellStyle = wb.createCellStyle();
            XSSFCellStyle integerCellStyle = wb.createCellStyle();
            XSSFCellStyle longCellStyle = wb.createCellStyle();
            XSSFCellStyle doubleCellStyle = wb.createCellStyle();
            XSSFDataFormat doubleFormat = wb.createDataFormat();
            doubleCellStyle.setDataFormat(doubleFormat.getFormat("#,##0.00"));
            XSSFCellStyle dateCellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            dateCellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("M/D/YYYY"));

            // Output stream for modified Excel file
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Get sheet          
            XSSFSheet costings = wb.getSheet("Job Costings");

            // Get costing data
            Job reportData[] = getJobManager().getSelectedJobs();
            for (Job job : reportData) {
                costingCol = 0;

                // BRANCH/UNIT                  
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getDepartmentAssignedToJob().getName(),
                        "java.lang.String", stringCellStyle);

                // JOB NUMBER                  
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getJobNumber(),
                        "java.lang.String", stringCellStyle);

                // DATE JOB COMPLETED                  
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getJobStatusAndTracking().getDateOfCompletion(),
                        "java.util.Date", dateCellStyle);

                // CONTACT NAME                  
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        BusinessEntityUtils.getContactFullName(job.getContact()),
                        "java.lang.String", stringCellStyle);

                // CUSTOMER NAME                  
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getClient().getName(),
                        "java.lang.String", stringCellStyle);

                // CUSTOMER ADDRESS
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getBillingAddress().toString(),
                        "java.lang.String", stringCellStyle);

                // CONTACT NOs
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getContact().getMainPhoneNumber().getLocalNumber(),
                        "java.lang.String", stringCellStyle);

                // DESCRIPTION OF JOB
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getJobDescription(),
                        "java.lang.String", stringCellStyle);

                // PURCHASE ORDER NO
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getJobCostingAndPayment().getPurchaseOrderNumber(),
                        "java.lang.String", stringCellStyle);

                // EMPLOYEE(S) ASSIGNED
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        job.getAssignedTo().getFirstName() + " " + job.getAssignedTo().getLastName(),
                        "java.lang.String", stringCellStyle);

                // # OF SAMPLES
                ReportUtils.setExcelCellValue(wb, costings, costingRow, costingCol++,
                        // NB: This may not refect the actual # of samples. 
                        // The samples collection should be used instead.
                        job.getNumberOfSamples(),
                        "java.lang.Long", longCellStyle);

                // Cost components
                int index = 0;
                for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                    ReportUtils.setExcelCellValue(wb, costings,
                            costingRow + index++,
                            costingCol,
                            index, // ITEM
                            "java.lang.Integer", integerCellStyle);
                }
                index = 0;
                costingCol = costingCol + 1;
                for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                    ReportUtils.setExcelCellValue(wb, costings,
                            costingRow + index++,
                            costingCol,
                            costComponent.getName(), // DESCRIPTION/NAME
                            "java.lang.String", stringCellStyle);
                }
                index = 0;
                costingCol = costingCol + 1;
                for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                    ReportUtils.setExcelCellValue(wb, costings,
                            costingRow + index++,
                            costingCol,
                            costComponent.getHoursOrQuantity(), // QUANTITY (Hr/Unit)
                            "java.lang.Double", doubleCellStyle);
                }
                index = 0;
                costingCol = costingCol + 1;
                for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                    ReportUtils.setExcelCellValue(wb, costings,
                            costingRow + index++,
                            costingCol,
                            costComponent.getRate(), // UNIT COST/RATE ($)
                            "java.lang.Double", doubleCellStyle);
                }
                index = 0;
                costingCol = costingCol + 1;
                for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                    ReportUtils.setExcelCellValue(wb, costings,
                            costingRow + index++,
                            costingCol,
                            costComponent.getCost(), // FEE/COST ($)
                            "java.lang.Double", doubleCellStyle);
                }
                // JOB COST($)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getTotalJobCostingsAmount(),
                        "java.lang.Double", doubleCellStyle);
                // TAX ($)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        getTotalTax(job),
                        "java.lang.Double", doubleCellStyle);
                // TOTAL COST($)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getTotalCost(),
                        "java.lang.Double", doubleCellStyle);
                // DEPOSIT RECEIPT #s
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getReceiptNumbers(),
                        "java.lang.String", stringCellStyle);
                // DEPOSIT($)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getTotalPayment(),
                        "java.lang.Double", doubleCellStyle);
                // DISCOUNT(%, $)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getDiscount().getDiscountValue(),
                        "java.lang.Double", doubleCellStyle);
                // AMOUNT DUE($)
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobCostingAndPayment().getAmountDue(),
                        "java.lang.Double", doubleCellStyle);
                // PREPARED BY
                if (job.getJobCostingAndPayment().getCostingPreparedBy() != null) {
                    ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                            job.getJobCostingAndPayment().getCostingPreparedBy().getFirstName() + " "
                            + job.getJobCostingAndPayment().getCostingPreparedBy().getLastName(),
                            "java.lang.String", stringCellStyle);
                } else {
                    ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                            "",
                            "java.lang.String", stringCellStyle);
                }
                // AUTHORIZED BY
                if (job.getJobCostingAndPayment().getCostingApprovedBy() != null) {
                    ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                            job.getJobCostingAndPayment().getCostingApprovedBy().getFirstName() + " "
                            + job.getJobCostingAndPayment().getCostingApprovedBy().getLastName(),
                            "java.lang.String", stringCellStyle);
                } else {
                    ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                            "",
                            "java.lang.String", stringCellStyle);
                }
                // AUTHORIZED DATE
                ReportUtils.setExcelCellValue(wb, costings, costingRow, ++costingCol,
                        job.getJobStatusAndTracking().getDateCostingApproved(),
                        "java.util.Date", dateCellStyle);

                // Prepare for next costing                  
                costingRow = costingRow + index;
            }

            // Write modified Excel file and return it
            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    /**
     * Gets an MS Excel file containing the details of invoices generated from
     * the selected job costings that have been invoiced.
     *
     * @return
     */
    public StreamedContent getInvoicesFile() {

        try {
            ByteArrayInputStream stream;

            stream = getInvoicesFileInputStream(
                    new File(getClass().getClassLoader().
                            getResource("/reports/"
                                    + (String) SystemOption.getOptionValueObject(getEntityManager1(),
                                            "AccpacInvoicesFileTemplateName")).getFile()));

            setLongProcessProgress(100);

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType("application/xlsx")
                    .name("Invoices - " + BusinessEntityUtils.getDateInMediumDateAndTimeFormat(new Date()) + " - " + fileDownloadErrorMessage + ".xlsx")
                    .build();

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    /**
     * Gets a byte array stream containing the details of invoices generated
     * from the selected job costings that have been invoiced.
     *
     * @param file
     * @return
     */
    public ByteArrayInputStream getInvoicesFileInputStream(
            File file) {

        try {
            FileInputStream inp = new FileInputStream(file);
            int invoiceRow = 1;
            int invoiceCol;
            int invoiceDetailsRow = 1;
            int invoiceDetailsCol;
            int invoiceOptionalFieldsRow = 1;
            int invoiceOptionalFieldsCol;

            fileDownloadErrorMessage = "";

            XSSFWorkbook wb = new XSSFWorkbook(inp);
            XSSFCellStyle stringCellStyle = wb.createCellStyle();
            XSSFCellStyle integerCellStyle = wb.createCellStyle();
            XSSFCellStyle doubleCellStyle = wb.createCellStyle();
            XSSFDataFormat doubleFormat = wb.createDataFormat();
            doubleCellStyle.setDataFormat(doubleFormat.getFormat("#,##0.00"));
            XSSFCellStyle dateCellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            dateCellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("M/D/YYYY"));

            // Output stream for modified Excel file
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Get sheets          
            XSSFSheet invoices = wb.getSheet("Invoices");
            XSSFSheet invoiceDetails = wb.getSheet("Invoice_Details");
            XSSFSheet invoiceOptionalFields = wb.getSheet("Invoice_Optional_Fields");

            // Get report data
            EntityManager em = getEntityManager1();
            Job reportData[] = getJobManager().getSelectedJobs();
            for (Job job : reportData) {
                // Reload from database
                if (job.getId() != null) {
                    job = Job.findJobById(em, job.getId());
                    em.refresh(job);
                }
                // Export only if costing was invoiced
                if (canExportInvoice(job)) {
                    invoiceCol = 0;
                    invoiceDetailsCol = 0;
                    invoiceOptionalFieldsCol = 0;
                    DecimalFormat currencyFormatter = new DecimalFormat("$#,##0.00");

                    prepareToInvoiceJobCosting(job);

                    // Insert fake cost component with job description
                    if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addJobDescriptionToInvoiceDetail")) {
                        ArrayList<CostComponent> currentComponents
                                = (ArrayList<CostComponent>) getCostComponentsWithoutHeadings(job.getJobCostingAndPayment());
                        CostComponent newFirst = new CostComponent();
                        newFirst.setId(1L);
                        newFirst.setName(job.getJobDescription());
                        newFirst.setHoursOrQuantity(1.0);
                        newFirst.setRate(0.0);
                        newFirst.setCost(0.0);
                        currentComponents.add(0, newFirst);
                        job.getJobCostingAndPayment().setCostComponents(currentComponents);
                    }

                    // Insert tax as cost component
                    if (getTax(job).getTaxValue() > 0.0) {
                        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                                "addTaxAsCostComponent")) {
                            ArrayList<CostComponent> currentComponents
                                    = (ArrayList<CostComponent>) getCostComponentsWithoutHeadings(job.getJobCostingAndPayment());
                            if (!currentComponents.isEmpty() /*&& !job.getJobCostingAndPayment().getCashPayments().isEmpty()*/) {
                                CostComponent currentLast
                                        = currentComponents.get(currentComponents.size() - 1);

                                CostComponent newLast = new CostComponent();
                                newLast.setId(currentLast.getId() + 1L);
                                newLast.setCode(getTaxCodeAbbreviation(job));
                                newLast.setName(job.getJobCostingAndPayment().getTax().getDescription());
                                newLast.setHoursOrQuantity(1.0);
                                newLast.setRate(0.0);
                                newLast.setCost(0.0);
                                currentComponents.add(newLast);
                                job.getJobCostingAndPayment().setCostComponents(currentComponents);
                            }
                        }
                    }

                    // Insert discount as cost component
                    if (getDiscount(job).getDiscountValue() > 0.0) {
                        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                                "addDiscountAsCostComponent")) {
                            ArrayList<CostComponent> currentComponents
                                    = (ArrayList<CostComponent>) getCostComponentsWithoutHeadings(job.getJobCostingAndPayment());
                            if (!currentComponents.isEmpty() /*&& !job.getJobCostingAndPayment().getCashPayments().isEmpty()*/) {
                                CostComponent currentLast
                                        = currentComponents.get(currentComponents.size() - 1);

                                CostComponent newLast = new CostComponent();
                                newLast.setId(currentLast.getId() + 1L);
                                newLast.setCode(getDiscountCodeAbbreviation(job));
                                newLast.setName(job.getJobCostingAndPayment().getDiscount().getDescription());
                                newLast.setHoursOrQuantity(1.0);
                                newLast.setRate(0.0);
                                newLast.setCost(0.0);
                                currentComponents.add(newLast);
                                job.getJobCostingAndPayment().setCostComponents(currentComponents);
                            }

                        }
                    }

                    // Insert fake costing for payments
                    if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addPaymentDetailCostComponentToInvoice")) {
                        ArrayList<CostComponent> currentComponents
                                = (ArrayList<CostComponent>) getCostComponentsWithoutHeadings(job.getJobCostingAndPayment());
                        if (!currentComponents.isEmpty() /*&& !job.getJobCostingAndPayment().getCashPayments().isEmpty()*/) {
                            CostComponent currentLast
                                    = currentComponents.get(currentComponents.size() - 1);

                            CostComponent newLast = new CostComponent();
                            newLast.setId(currentLast.getId() + 1L);
                            // Gather payment details
                            int paymentIndex = 0;
                            String paymentDetails = "Payment(s): ";
                            if (!job.getJobCostingAndPayment().getCashPayments().isEmpty()) {
                                for (CashPayment payment
                                        : job.getJobCostingAndPayment().getCashPayments()) {
                                    if (paymentIndex == 0) {
                                        paymentDetails
                                                += (currencyFormatter.format(payment.getPayment())
                                                + " Receipt # " + payment.getReceiptNumber());
                                    } else {
                                        paymentDetails
                                                += (", " + currencyFormatter.format(payment.getPayment())
                                                + " Receipt # " + payment.getReceiptNumber());
                                    }

                                    ++paymentIndex;
                                }
                            } else {
                                paymentDetails = paymentDetails + "N/A";
                            }
                            newLast.setName(paymentDetails);
                            newLast.setHoursOrQuantity(1.0);
                            newLast.setRate(0.0);
                            newLast.setCost(0.0);
                            currentComponents.add(newLast);
                            job.getJobCostingAndPayment().setCostComponents(currentComponents);
                        }
                    }

                    // Insert fake costing for balance
                    if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addBalanceCostComponentToInvoice")) {
                        ArrayList<CostComponent> currentComponents
                                = (ArrayList<CostComponent>) getCostComponentsWithoutHeadings(job.getJobCostingAndPayment());
                        if (!currentComponents.isEmpty() /*&& !job.getJobCostingAndPayment().getCashPayments().isEmpty()*/) {
                            CostComponent currentLast
                                    = currentComponents.get(currentComponents.size() - 1);

                            CostComponent newLast = new CostComponent();
                            newLast.setId(currentLast.getId() + 1L);
                            newLast.setName("Balance: "
                                    + currencyFormatter.format(job.getJobCostingAndPayment().getAmountDue()));
                            newLast.setHoursOrQuantity(1.0);
                            newLast.setRate(0.0);
                            newLast.setCost(0.0);
                            currentComponents.add(newLast);
                            job.getJobCostingAndPayment().setCostComponents(currentComponents);
                        }
                    }
                    // Fill out the Invoices sheet
                    // CNTBTCH (batch number)
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            0,
                            "java.lang.Integer", integerCellStyle);
                    // CNTITEM (Item number)
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            invoiceRow,
                            "java.lang.Integer", integerCellStyle);
                    // IDCUST (Customer Id)                  
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            job.getClient().getFinancialAccount().getIdCust(),
                            "java.lang.String", stringCellStyle);
                    // TEXTTRX
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            1,
                            "java.lang.Integer", integerCellStyle);
                    // IDTRX
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            11,
                            "java.lang.Integer", integerCellStyle);
                    // ORDRNBR
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            job.getJobNumber(),
                            "java.lang.String", stringCellStyle);
                    // CUSTPO
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            job.getJobCostingAndPayment().getPurchaseOrderNumber(),
                            "java.lang.String", stringCellStyle);
                    // INVCDESC
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            job.getInstructions(),
                            "java.lang.String", stringCellStyle);
                    // DATEINVC
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            job.getJobStatusAndTracking().getDateCostingInvoiced(),
                            "java.util.Date", dateCellStyle);
                    // INVCTYPE
                    ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                            1, // tk org. 2
                            "java.lang.Integer", integerCellStyle);
                    // SPECINST
                    if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "exportReceiptNumbers")) {
                        ReportUtils.setExcelCellValue(wb, invoices, invoiceRow, invoiceCol++,
                                job.getJobCostingAndPayment().getReceiptNumbers(),
                                "java.lang.String", stringCellStyle);
                    } else { // Remove SPECINST column header
                        ReportUtils.setExcelCellValue(wb, invoices, 0, invoiceCol++,
                                "",
                                "java.lang.String", stringCellStyle);
                    }

                    // Fill out Invoice Details sheet
                    // Add an item for each cost component
                    // CNTBTCH (batch number)
                    int index = 0;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                0, // CNTBTCH
                                "java.lang.Integer", integerCellStyle);
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    0, // CNTBTCH
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    0, // CNTBTCH
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    // CNTITEM (Item/Invoice number/index)
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                invoiceRow, // CNTITEM
                                "java.lang.Integer", integerCellStyle);
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    invoiceRow, // CNTITEM
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    invoiceRow, // CNTITEM
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    // CNTLINE
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                index, // CNTLINE
                                "java.lang.Integer", integerCellStyle);
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    index, // CNTLINE
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    index, // CNTLINE
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    // IDITEM
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        if (getTaxCodeAbbreviation(job).equals(costComponent.getCode())) {
                            if (getTax(job).getTaxValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        getTaxCodeAbbreviation(job), // IDITEM
                                        "java.lang.String", stringCellStyle);
                            }
                        } else if (getDiscountCodeAbbreviation(job).equals(costComponent.getCode())) {
                            if (getDiscount(job).getDiscountValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        getDiscountCodeAbbreviation(job), // IDITEM
                                        "java.lang.String", stringCellStyle);
                            }
                        } else {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getRevenueCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        }

                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getTaxCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getDiscountCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    // IDDIST
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        if (getTaxCodeAbbreviation(job).equals(costComponent.getCode())) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getTaxCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        } else if (getDiscountCodeAbbreviation(job).equals(costComponent.getCode())) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getDiscountCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        } else {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getRevenueCodeAbbreviation(job), // IDITEM
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getTaxCodeAbbreviation(job), // IDDIST
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    getDiscountCodeAbbreviation(job), // IDDIST
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    // TEXTDESC
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {

                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                costComponent.getName(),
                                "java.lang.String", stringCellStyle);
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    job.getJobCostingAndPayment().getTax().getDescription(),
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    job.getJobCostingAndPayment().getDiscount().getDescription(),
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    // UNITMEAS
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                "EACH", // UNITMEAS
                                "java.lang.String", stringCellStyle);
                    }
                    // Add Tax row value if any 
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    "EACH", // UNITMEAS
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    "EACH", // UNITMEAS
                                    "java.lang.String", stringCellStyle);
                        }
                    }
                    // QTYINVC
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                invoiceDetailsRow + index++,
                                invoiceDetailsCol,
                                costComponent.getHoursOrQuantity().intValue(),
                                "java.lang.Integer", integerCellStyle);
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    1, // QTYINVC
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    1, // QTYINVC
                                    "java.lang.Integer", integerCellStyle);
                        }
                    }
                    // AMTPRIC
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        if (getTaxCodeAbbreviation(job).equals(costComponent.getCode())) {
                            // Add Tax row value if any 
                            if (getTax(job).getTaxValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        BusinessEntityUtils.roundTo2DecimalPlaces(getTotalTax(job)),
                                        "java.lang.Double", doubleCellStyle); // AMTPRIC
                            }
                        } else if (getDiscountCodeAbbreviation(job).equals(costComponent.getCode())) {
                            if (getDiscount(job).getDiscountValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        -BusinessEntityUtils.roundTo2DecimalPlaces(getTotalDiscount(job)),
                                        "java.lang.Double", doubleCellStyle); // AMTPRIC
                            }
                        } else {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    BusinessEntityUtils.roundTo2DecimalPlaces(costComponent.getRate()),
                                    "java.lang.Double", doubleCellStyle); // AMTPRIC
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    BusinessEntityUtils.roundTo2DecimalPlaces(getTotalTax(job)),
                                    "java.lang.Double", doubleCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    -BusinessEntityUtils.roundTo2DecimalPlaces(getTotalDiscount(job)),
                                    "java.lang.Double", doubleCellStyle);
                        }
                    }
                    // AMTEXTN
                    index = 0;
                    invoiceDetailsCol++;
                    for (CostComponent costComponent : getCostComponentsWithoutHeadings(job.getJobCostingAndPayment())) {
                        if (getTaxCodeAbbreviation(job).equals(costComponent.getCode())) {
                            // Add Tax row value if any 
                            if (getTax(job).getTaxValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        BusinessEntityUtils.roundTo2DecimalPlaces(getTotalTax(job)),
                                        "java.lang.Double", doubleCellStyle); // AMTEXTN
                            }
                        } else if (getDiscountCodeAbbreviation(job).equals(costComponent.getCode())) {
                            if (getDiscount(job).getDiscountValue() > 0.0) {
                                ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                        invoiceDetailsRow + index++,
                                        invoiceDetailsCol,
                                        -BusinessEntityUtils.roundTo2DecimalPlaces(getTotalDiscount(job)),
                                        "java.lang.Double", doubleCellStyle); // AMTEXTN
                            }
                        } else {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    BusinessEntityUtils.roundTo2DecimalPlaces(costComponent.getCost()),
                                    "java.lang.Double", doubleCellStyle); // AMTEXTN
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addTaxAsCostComponent")) {
                        // Add Tax row value if any 
                        if (getTax(job).getTaxValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    BusinessEntityUtils.roundTo2DecimalPlaces(getTotalTax(job)),
                                    "java.lang.Double", doubleCellStyle);
                        }
                    }
                    if (!(Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                            "addDiscountAsCostComponent")) {
                        // Add Discount row value if any 
                        if (getDiscount(job).getDiscountValue() > 0.0) {
                            ReportUtils.setExcelCellValue(wb, invoiceDetails,
                                    invoiceDetailsRow + index++,
                                    invoiceDetailsCol,
                                    -BusinessEntityUtils.roundTo2DecimalPlaces(getTotalDiscount(job)),
                                    "java.lang.Double", doubleCellStyle);
                        }
                    }

                    // Fill out Invoice Optional Fields sheet                    
                    // CNTBTCH (batch number)
                    int index2 = 0;
                    for (int i = 0; i < 4; i++) {
                        ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                                invoiceOptionalFieldsRow + index2++,
                                invoiceOptionalFieldsCol,
                                0, // CNTBTCH
                                "java.lang.Integer", integerCellStyle);
                    }
                    // CNTITEM (Item/Invoice number/index)
                    index2 = 0;
                    ++invoiceOptionalFieldsCol;
                    for (int i = 0; i < 4; i++) {
                        ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                                invoiceOptionalFieldsRow + index2++,
                                invoiceOptionalFieldsCol,
                                invoiceRow, // CNTITEM
                                "java.lang.Integer", integerCellStyle);
                    }
                    // OPTFIELD                    
                    index2 = 0;
                    ++invoiceOptionalFieldsCol;
                    // DEPTCODE
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            "DEPTCODE", // DEPTCODE
                            "java.lang.String", stringCellStyle);
                    // INVCONTACT
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            "INVCONTACT", // INVCONTACT
                            "java.lang.String", stringCellStyle);
                    // JOBNO
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            "JOBNO", // JOBNO
                            "java.lang.String", stringCellStyle);
                    // REFNO
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            "REFNO", // REFNO
                            "java.lang.String", stringCellStyle);
                    // OPTFIELD/VALUE                    
                    index2 = 0;
                    ++invoiceOptionalFieldsCol;
                    // DEPTCODE
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            job.getDepartmentAssignedToJob().getCode(), // DEPTCODE
                            "java.lang.String", stringCellStyle);
                    // INVCONTACT
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            job.getContact().getFirstName()
                            + " " + job.getContact().getLastName(), // INVCONTACT
                            "java.lang.String", stringCellStyle);
                    // JOBNO
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            job.getJobNumber(), // JOBNO
                            "java.lang.String", stringCellStyle);
                    // REFNO
                    ReportUtils.setExcelCellValue(wb, invoiceOptionalFields,
                            invoiceOptionalFieldsRow + index2++,
                            invoiceOptionalFieldsCol,
                            "", // REFNO
                            "java.lang.String", stringCellStyle);

                    // Prepare for next invoice
                    invoiceDetailsRow = invoiceDetailsRow + index;
                    invoiceOptionalFieldsRow = invoiceOptionalFieldsRow + index2;
                    invoiceRow++;

                }
            }

            // Write modified Excel file and return it
            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    /**
     * Get the accounting codes associated with a job.
     *
     * @param job
     * @return
     */
    public List<String> getAccountingCodes(Job job) {
        List<String> codes = new ArrayList<>();

        prepareToInvoiceJobCosting(job);

        codes.add(getRevenueCodeAbbreviation(job));
        if (getTax(job).getTaxValue() > 0.0) {
            codes.add(getTaxCodeAbbreviation(job));
        }
        if (getDiscount(job).getDiscountValue() > 0.0) {
            codes.add(getDiscountCodeAbbreviation(job));
        }

        return codes;
    }

    /**
     * Gets the abbreviated discount code associated with the discount for a
     * job.
     *
     * @param job
     * @return
     */
    private String getDiscountCodeAbbreviation(Job job) {
        String currentDiscountCode
                = getDiscount(job).getAccountingCode().getCode();
        String deptFullCode = HumanResourceManager.getDepartmentFullCode(getEntityManager1(),
                job.getDepartmentAssignedToJob());

        // Find an accounting code that contains the department's full code
        AccountingCode accountingCode
                = AccountingCode.findActiveByCode(getEntityManager1(),
                        currentDiscountCode + "-" + deptFullCode);
        if (accountingCode != null) {
            return accountingCode.getAbbreviation();
        } else {
            return getDiscount(job).getAccountingCode().getAbbreviation();
        }

    }

    /**
     * Gets the abbreviated tax code associated with the tax for a job.
     *
     * @param job
     * @return
     */
    private String getTaxCodeAbbreviation(Job job) {
        String currentTaxCode
                = getTax(job).getAccountingCode().getCode();
        String deptFullCode = HumanResourceManager.getDepartmentFullCode(getEntityManager1(),
                job.getDepartmentAssignedToJob());

        // Find an accounting code that contains the department's full code
        AccountingCode accountingCode
                = AccountingCode.findActiveByCode(getEntityManager1(),
                        currentTaxCode + "-" + deptFullCode);

        if (accountingCode != null) {
            return accountingCode.getAbbreviation();
        } else {
            return getTax(job).getAccountingCode().getAbbreviation();
        }

    }

    /**
     * Gets the abbreviated revenue code associated with the service for a job.
     *
     * @param job
     * @return
     */
    private String getRevenueCodeAbbreviation(Job job) {
        String revenueCode;
        String revenueCodeAbbr;

        if (!job.getServices().isEmpty()) {
            revenueCode = job.getServices().get(0).getAccountingCode().getCode();

            String deptFullCode = HumanResourceManager.getDepartmentFullCode(getEntityManager1(),
                    job.getDepartmentAssignedToJob());

            // Find an accounting code that contains the department's full code
            AccountingCode accountingCode
                    = AccountingCode.findActiveByCode(getEntityManager1(),
                            revenueCode + "-" + deptFullCode);

            if (accountingCode != null) {
                revenueCodeAbbr = accountingCode.getAbbreviation();
            } else {
                revenueCodeAbbr = "MISC";
            }

        } else {
            // Get and use default accounting code
            Service service = Service.findActiveByNameAndAccountingCode(
                    getEntityManager1(),
                    "Miscellaneous",
                    HumanResourceManager.getDepartmentFullCode(getEntityManager1(),
                            job.getDepartmentAssignedToJob()));

            if (service != null) {
                revenueCodeAbbr = service.getAccountingCode().getAbbreviation();
            } else {
                // NB: Just using this revenue code for testing for now.
                // This value is to be obtained from system option.
                revenueCodeAbbr = "MISC";
            }
        }

        return revenueCodeAbbr;

    }

    /**
     * Gets the flag that determines if an object is being edited.
     *
     * @return
     */
    public Boolean getEdit() {
        return edit;
    }

    /**
     * Sets the flag that determines if an object is being edited.
     *
     * @param edit
     */
    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    /**
     * Finds and gets the JobManager JSF bean.
     *
     * @return
     */
    public JobManager getJobManager() {
        if (jobManager == null) {
            jobManager = BeanUtils.findBean("jobManager");
        }
        return jobManager;
    }

    /**
     * Finds/gets the JobContracManager session bean.
     *
     * @return
     */
    public JobContractManager getJobContractManager() {
        if (jobContractManager == null) {
            jobContractManager = BeanUtils.findBean("jobContractManager");
        }
        return jobContractManager;
    }

    /**
     * Initializes an instance of the JobFinanceManger class.
     */
    private void init() {
        longProcessProgress = 0;
        accPacCustomer = new AccPacCustomer(null);
        useAccPacCustomerList = false;
        selectedCashPayment = null;
        selectedCostComponent = null;
        unitCostDepartment = null;
        jobCostDepartment = null;
        filteredAccPacCustomerDocuments = new ArrayList<>();
        isActiveJobCostingAndPaymentsOnly = true;
        jobCostingAndPaymentSearchText = "";
        selectedJobCostingTemplate = "";
        searchType = "My dept's proforma invoices";
        searchText = "";
        dateSearchPeriod = new DatePeriod("This month", "month",
                "dateAndTimeEntered", null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();
        proformaInvoiceSearchText = "";
        costEstimateSearchText = "";
    }

    /**
     * Resets an instance of this class.
     */
    public void reset() {
        init();
    }

    public Boolean getEnableSubcontractWithCosting() {
        return (Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                "enableSubcontractWithCosting");
    }

    public Boolean getUseMulticurrency() {
        return SystemOption.getBoolean(getEntityManager1(), "useMulticurrency");
    }

    /**
     * Gets the enableOnlyPaymentEditing field. Sets it to false if it is null.
     *
     * @return
     */
    public Boolean getEnableOnlyPaymentEditing() {
        if (enableOnlyPaymentEditing == null) {
            enableOnlyPaymentEditing = false;
        }
        return enableOnlyPaymentEditing;
    }

    /**
     * Sets the enableOnlyPaymentEditing field.
     *
     * @param enableOnlyPaymentEditing
     */
    public void setEnableOnlyPaymentEditing(Boolean enableOnlyPaymentEditing) {
        this.enableOnlyPaymentEditing = enableOnlyPaymentEditing;
    }

    /**
     * Creates and gets an EntityManager object using the EMF1
     * EntityManagerFactory object.
     *
     * @return
     */
    public EntityManager getEntityManager1() {
        return getJobManager().getEntityManager1();
    }

    /**
     * Gets the logged on user field.
     *
     * @return
     */
    public User getUser() {
        return getJobManager().getUser();
    }

    /**
     * Gets the filteredAccPacCustomerDocuments field.
     *
     * @return
     */
    public List<AccPacDocument> getFilteredAccPacCustomerDocuments() {
        return filteredAccPacCustomerDocuments;
    }

    /**
     * Sets the filteredAccPacCustomerDocuments field.
     *
     * @param filteredAccPacCustomerDocuments
     */
    public void setFilteredAccPacCustomerDocuments(List<AccPacDocument> filteredAccPacCustomerDocuments) {
        this.filteredAccPacCustomerDocuments = filteredAccPacCustomerDocuments;
    }

    /**
     * Gets the showPrepayments field. Sets the field to false if it is null.
     *
     * @return
     */
    public Boolean getShowPrepayments() {
        if (showPrepayments == null) {
            showPrepayments = false;
        }
        return showPrepayments;
    }

    /**
     * Sets the showPrepayments field.
     *
     * @param showPrepayments
     */
    public void setShowPrepayments(Boolean showPrepayments) {
        this.showPrepayments = showPrepayments;
    }

    /**
     * Gets the jobCostDepartment field. Sets the field to a "no-name"
     * department if it is null.
     *
     * @return
     */
    public Department getJobCostDepartment() {
        if (jobCostDepartment == null) {
            jobCostDepartment = new Department("");
        }
        return jobCostDepartment;
    }

    /**
     * Sets the jobCostDepartment field.
     *
     * @param jobCostDepartment
     */
    public void setJobCostDepartment(Department jobCostDepartment) {
        this.jobCostDepartment = jobCostDepartment;
    }

    /**
     * Gets the currentJobWithCosting field. Sets it to a new Job object if it
     * is null.
     *
     * @return
     */
    public Job getCurrentJobWithCosting() {
        if (currentJobWithCosting == null) {
            currentJobWithCosting = new Job();
        }
        return currentJobWithCosting;
    }

    /**
     * Sets the currentJobWithCosting field.
     *
     * @param currentJobWithCosting
     */
    public void setCurrentJobWithCosting(Job currentJobWithCosting) {
        this.currentJobWithCosting = currentJobWithCosting;
    }

    /**
     * Gets the jobsWithCostings field. Sets it to an empty list if it is null.
     *
     * @return
     */
    public List<Job> getJobsWithCostings() {
        if (jobsWithCostings == null) {
            jobsWithCostings = new ArrayList<>();
        }
        return jobsWithCostings;
    }

    /**
     * Gets the unitCosts field. Sets it to an empty list if it is null.
     *
     * @return
     */
    public List<UnitCost> getUnitCosts() {
        if (unitCosts == null) {
            unitCosts = new ArrayList<>();
        }

        return unitCosts;
    }

    /**
     * Gets the currentUnitCost field. Sets it to a new UnitCost object if it is
     * null.
     *
     * @return
     */
    public UnitCost getCurrentUnitCost() {
        if (currentUnitCost == null) {
            currentUnitCost = new UnitCost();
        }
        return currentUnitCost;
    }

    /**
     * Sets the currentUnitCost field.
     *
     * @param currentUnitCost
     */
    public void setCurrentUnitCost(UnitCost currentUnitCost) {
        this.currentUnitCost = currentUnitCost;
    }

    /**
     * Gets the unitCostDepartment field. Sets it to a "no-name" Department
     * object if it is null.
     *
     * @return
     */
    public Department getUnitCostDepartment() {
        if (unitCostDepartment == null) {
            unitCostDepartment = new Department("");
        }
        return unitCostDepartment;
    }

    /**
     * Sets the unitCostDepartment field.
     *
     * @param unitCostDepartment
     */
    public void setUnitCostDepartment(Department unitCostDepartment) {
        this.unitCostDepartment = unitCostDepartment;
    }

    /**
     * Determines if the user has the privilege to edit a job costing.
     *
     * @return
     */
    public Boolean getCanEditJobCosting() {

        return getCanEditJobCosting(getCurrentJob());
    }

    public Boolean getCanEditJobCosting(Job job) {

        return getUser().can("BeFinancialAdministrator")
                || job.getJobCostingAndPayment().getCashPayments().isEmpty();
    }

    /**
     * Gets the selectedJobCostingTemplate field.
     *
     * @return
     */
    public String getSelectedJobCostingTemplate() {
        return selectedJobCostingTemplate;
    }

    /**
     * Sets the selectedJobCostingTemplate field.
     *
     * @param selectedJobCostingTemplate
     */
    public void setSelectedJobCostingTemplate(String selectedJobCostingTemplate) {
        this.selectedJobCostingTemplate = selectedJobCostingTemplate;
    }

    /**
     * Determines if a job is assigned to the department of the user.
     *
     * @param job
     * @return
     */
    private Boolean isJobAssignedToUserDepartment(Job job) {

        if (getUser() != null) {
            if (job.getDepartment().getId().longValue() == getUser().getEmployee().getDepartment().getId().longValue()) {
                return true;
            } else {
                return job.getSubContractedDepartment().getId().longValue() == getUser().getEmployee().getDepartment().getId().longValue();
            }
        } else {
            return false;
        }
    }

    /**
     * Sets the selectedCostComponent field to the PrimeFaces selected object.
     *
     * @param event
     */
    public void onCostComponentSelect(SelectEvent event) {
        selectedCostComponent = (CostComponent) event.getObject();
    }

    /**
     * Gets the selectedCostComponent field.
     *
     * @return
     */
    public CostComponent getSelectedCostComponent() {
        return selectedCostComponent;
    }

    /**
     * Sets the selectedCostComponent field.
     *
     * @param selectedCostComponent
     */
    public void setSelectedCostComponent(CostComponent selectedCostComponent) {
        this.selectedCostComponent = selectedCostComponent;
    }

    /**
     * Gets the useAccPacCustomerList field.
     *
     * @return
     */
    public Boolean getUseAccPacCustomerList() {
        return useAccPacCustomerList;
    }

    /**
     * Sets the useAccPacCustomerList field.
     *
     * @param useAccPacCustomerList
     */
    public void setUseAccPacCustomerList(Boolean useAccPacCustomerList) {
        this.useAccPacCustomerList = useAccPacCustomerList;
    }

    /**
     * Gets the number of documents that are due/overdue by the given number of
     * days.
     *
     * @param days
     * @return
     */
    public Integer getNumberOfDocumentsPassDocDate(Integer days) {
        Integer count = 0;

        for (AccPacDocument doc : filteredAccPacCustomerDocuments) {
            if (doc.getDaysOverDocumentDate() >= days) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Gets the status of a financial account as "hold" or "active" based on the
     * the total cost of outstanding invoices that are overdue by the maximum
     * allowed number of days.
     *
     * @return
     */
    public String getAccountStatus() {
        if (getTotalInvoicesAmountOverMaxInvDays().doubleValue() > 0.0
                && getTotalInvoicesAmount().doubleValue() > 0.0) {
            return "hold";
        } else {
            return "active";
        }
    }

    /**
     * Gets the total cost of invoices that are pass the maximum allowed overdue
     * days.
     *
     * @return
     */
    public BigDecimal getTotalInvoicesAmountOverMaxInvDays() {
        BigDecimal total = new BigDecimal(0.0);

        for (AccPacDocument doc : filteredAccPacCustomerDocuments) {
            if (doc.getDaysOverdue() > getMaxDaysPassInvoiceDate()) {
                total = total.add(doc.getCustCurrencyAmountDue());
            }
        }

        return total;
    }

    /**
     * Gets the total cost of invoices in the filteredAccPacCustomerDocuments
     * field.
     *
     * @return
     */
    public BigDecimal getTotalInvoicesAmount() {
        BigDecimal total = new BigDecimal(0.0);

        for (AccPacDocument doc : filteredAccPacCustomerDocuments) {
            total = total.add(doc.getCustCurrencyAmountDue());
        }

        return total;
    }

    /**
     * Gets the maximum allowed overdue days for invoices.
     *
     * @return
     */
    public Integer getMaxDaysPassInvoiceDate() {

        EntityManager em = getEntityManager1();

        int days = (Integer) SystemOption.getOptionValueObject(em, "maxDaysPassInvoiceDate");

        return days;
    }

    /**
     * Get status based on the total amount on documents/invoices pass the max
     * allowed days pass the invoice date.
     *
     * @return
     */
    public String getAccPacCustomerAccountStatus() {

        if (getAccountStatus().equals("hold")) {
            return "HOLD";
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Gets the number of documents/invoices that are pass the maximum allowed
     * overdue days.
     *
     * @return
     */
    public Integer getNumDocumentsPassMaxInvDate() {
        return getNumberOfDocumentsPassDocDate(getMaxDaysPassInvoiceDate());
    }

    /**
     * Updates and gets the longProcessProgress field that stores the progress
     * of a server activity.
     *
     * @return
     */
    public Integer getLongProcessProgress() {
        if (longProcessProgress == null) {
            longProcessProgress = 0;
        } else {
            if (longProcessProgress < 10) {
                // This is to ensure that this method does not make the progress
                // complete as this is done elsewhere.
                longProcessProgress = longProcessProgress + 1;
            }
        }

        return longProcessProgress;
    }

    /**
     * A callback method that is called when an activity is complete. The
     * longProcessProgress field is set to 0.
     */
    public void onLongProcessComplete() {
        longProcessProgress = 0;
    }

    /**
     * Sets the longProcessProgress field.
     *
     * @param longProcessProgress
     */
    public void setLongProcessProgress(Integer longProcessProgress) {
        this.longProcessProgress = longProcessProgress;
    }

    public StreamedContent getJobCostingAnalysisFile(EntityManager em) {

        HashMap parameters = new HashMap();

        try {
            String logoURL = (String) SystemOption.getOptionValueObject(em, "logoURL");
            parameters.put("logoURL", logoURL);
            parameters.put("jobId", getCurrentJob().getId());
            parameters.put("contactPersonName", BusinessEntityUtils.getContactFullName(getCurrentJob().getContact()));
            parameters.put("customerAddress", getCurrentJob().getBillingAddress().toString());
            parameters.put("contactNumbers", getCurrentJob().getContact().getMainPhoneNumber().getLocalNumber());
            parameters.put("jobDescription", getCurrentJob().getJobDescription());
            parameters.put("totalCost", getCurrentJob().getJobCostingAndPayment().getTotalJobCostingsAmount());
            parameters.put("depositReceiptNumbers", getCurrentJob().getJobCostingAndPayment().getReceiptNumbers());
            parameters.put("discount", getCurrentJob().getJobCostingAndPayment().getDiscount().getDiscountValue());
            parameters.put("discountType", getCurrentJob().getJobCostingAndPayment().getDiscount().getDiscountValueType());
            parameters.put("deposit", getCurrentJob().getJobCostingAndPayment().getTotalPayment());
            parameters.put("amountDue", getCurrentJob().getJobCostingAndPayment().getAmountDue());
            parameters.put("totalTax", getTotalTax(getCurrentJob()));
            parameters.put("totalTaxLabel", getCurrentJob().getJobCostingAndPayment().getTotalTaxLabel());
            parameters.put("grandTotalCostLabel", getCurrentJob().getJobCostingAndPayment().getTotalCostWithTaxLabel().toUpperCase().trim());
            parameters.put("grandTotalCost", getCurrentJob().getJobCostingAndPayment().getTotalCost());
            if (getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy() != null) {
                parameters.put("preparedBy",
                        getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy().getFirstName() + " "
                        + getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy().getLastName());
            }
            if (getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy() != null) {
                parameters.put("approvedBy",
                        getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy().getFirstName() + " "
                        + getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy().getLastName());
            }
            parameters.put("approvalDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(
                            getCurrentJob().getJobStatusAndTracking().getDateCostingApproved()));

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamContent;
                    // Compile report
                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(em, "jobCosting"));

                    // Generate report
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamContent = DefaultStreamedContent.builder()
                            .stream(() -> new ByteArrayInputStream(fileBytes))
                            .contentType("application/pdf")
                            .name("Job Costing - " + getCurrentJob().getJobNumber() + ".pdf")
                            .build();

                    setLongProcessProgress(100);

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

    public StreamedContent getProformaInvoice(EntityManager em) {

        HashMap parameters = new HashMap();
        // tk the currency symbol is to be obtained from default currency
        DecimalFormat currencyFormatter = new DecimalFormat("JMD#,##0.00");

        try {
            parameters.put("jobId", getCurrentJob().getId());
            parameters.put("proformaNumber", getCurrentJob().getJobNumber());
            if (getCurrentJob().getClient().getTaxRegistrationNumber().isEmpty()) {
                parameters.put("TRNorID", getCurrentJob().getClient().getIdentification());
            } else {
                parameters.put("TRNorID", getCurrentJob().getClient().getTaxRegistrationNumber());
            }
            if (getCurrentJob().getClient().getCreditLimit() > 0) {
                parameters.put("creditClient", "Yes");
            } else {
                parameters.put("creditClient", "No");
            }
            parameters.put("contactEmail", getCurrentJob().getContact().getInternet().getEmail1());
            parameters.put("responsibleDepartment", Department.findAssignedToJob(getCurrentJob(), em).getName());
            parameters.put("departmentCode", Department.findAssignedToJob(getCurrentJob(), em).getCode());
            parameters.put("dateAndTimePrepared",
                    DateUtils.formatDateAndTime(getCurrentJob().getJobStatusAndTracking().getDateCostingCompleted()));
            if (getCurrentJob().getClient().getCreditLimit() > 0) {
                parameters.put("standardNote",
                        (String) SystemOption.getOptionValueObject(em, "creditClientProformaStandardNote"));
            } else {
                parameters.put("standardNote",
                        (String) SystemOption.getOptionValueObject(em, "nonCreditClientProformaStandardNote"));
            }
            parameters.put("additionalNote", getCurrentJob().getJobCostingAndPayment().getDescription());
            parameters.put("reimbursable",
                    currencyFormatter.format(getCurrentJob().getJobCostingAndPayment().getReimbursable()));
            parameters.put("reasonForReimbursable", getCurrentJob().getComment());
            parameters.put("contactPersonName", BusinessEntityUtils.getContactFullName(getCurrentJob().getContact()));
            parameters.put("customerAddress", getCurrentJob().getBillingAddress().toString());
            parameters.put("contactNumbers", getCurrentJob().getContact().getMainPhoneNumber().getLocalNumber());
            parameters.put("jobDescription", getCurrentJob().getJobDescription());
            parameters.put("totalCost", currencyFormatter.format(getCurrentJob().getJobCostingAndPayment().getTotalJobCostingsAmount()));
            parameters.put("discount", getCurrentJob().getJobCostingAndPayment().getDiscount().getDiscountValue());
            parameters.put("discountType", getCurrentJob().getJobCostingAndPayment().getDiscount().getDiscountValueType());
            parameters.put("totalTax", currencyFormatter.format(getTotalTax(getCurrentJob())));
            parameters.put("totalTaxLabel",
                    Tax.findByName(getEntityManager1(), (String) SystemOption.getOptionValueObject(getEntityManager1(), "defaultTax")).getName());
            parameters.put("grandTotalCostLabel", getCurrentJob().getJobCostingAndPayment().getTotalCostWithTaxLabel().toUpperCase().trim());
            parameters.put("grandTotalCost", currencyFormatter.format(
                    getCurrentJob().getJobCostingAndPayment().getProformaTotalCost()));
            if (getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy() != null) {
                parameters.put("preparedBy",
                        getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy().getFirstName() + " "
                        + getCurrentJob().getJobCostingAndPayment().getCostingPreparedBy().getLastName());
            }
            if (getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy() != null) {
                parameters.put("approvedBy",
                        getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy().getFirstName() + " "
                        + getCurrentJob().getJobCostingAndPayment().getCostingApprovedBy().getLastName());
            }
            parameters.put("approvalDate",
                    BusinessEntityUtils.getDateInMediumDateFormat(
                            getCurrentJob().getJobStatusAndTracking().getDateCostingApproved()));

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamContent;
                    // Compile report
                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(em, "proformaInvoiceFormTemplate"));

                    // Generate report
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamContent = DefaultStreamedContent.builder()
                            .stream(() -> new ByteArrayInputStream(fileBytes))
                            .contentType("application/pdf")
                            .name("Proforma Invoice - " + getCurrentJob().getJobNumber() + ".pdf")
                            .build();

                    setLongProcessProgress(100);

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

    public StreamedContent getJobCostingFile() {
        EntityManager em;

        try {
            em = getEntityManager1();

            if (getCurrentJob().getIsDirty()) {
                getCurrentJob().getJobCostingAndPayment().save(em);
                getCurrentJob().setIsDirty(false);
            }

            jobCostingFile = getJobCostingAnalysisFile(em);

            setLongProcessProgress(100);

        } catch (Exception e) {
            System.out.println(e);
            setLongProcessProgress(100);
        }

        return jobCostingFile;
    }

    /**
     * Determines if the user can export a job costing.
     *
     * @return
     */
    public Boolean getCanExportJobCosting() {

        return !(getCurrentJob().getJobCostingAndPayment().getCostingApproved()
                && getCurrentJob().getJobCostingAndPayment().getCostingCompleted());
    }

    public Boolean getDisableJobCostingEdit() {
        return getCurrentJob().getJobStatusAndTracking().getWorkProgress().equals("Completed")
                || getCurrentJob().getJobCostingAndPayment().getInvoiced()
                || getCurrentJob().getJobCostingAndPayment().getCostingApproved();
    }

    public StreamedContent getProformaInvoiceFile() {
        EntityManager em;

        try {
            em = getEntityManager1();

            if (getCurrentJob().getIsDirty()) {
                getCurrentJob().getJobCostingAndPayment().save(em);
                getCurrentJob().setIsDirty(false);
            }

            jobCostingFile = getProformaInvoice(em);

            setLongProcessProgress(100);

        } catch (Exception e) {
            System.out.println(e);
            setLongProcessProgress(100);
        }

        return jobCostingFile;
    }

    /**
     * Prepares to invoice a job costing.
     *
     * @param job
     */
    private void prepareToInvoiceJobCosting(Job job) {

        // Ensure that services are added based on the service contract
        getJobContractManager().addServices(job);
        // Ensure that an accounting Id is added for the client  
        AccPacCustomer financialAccount = AccPacCustomer.findByName(
                getEntityManager2(), job.getClient().getName());
        if (financialAccount != null) {
            // Set accounting Id
            job.getClient().setAccountingId(financialAccount.getIdCust());
            // Set credit limit 
            job.getClient().setCreditLimit((financialAccount.getCreditLimit().doubleValue()));
            // Update and save
            job.getClient().setEditedBy(getUser().getEmployee());
            job.getClient().setDateEdited(new Date());
            job.getClient().save(getEntityManager1());
        }

    }

    /**
     * Invoices the job costing for the current job.
     */
    public void invoiceJobCosting() {
        invoiceJobCosting(getCurrentJob(), getCurrentJob().getJobCostingAndPayment().getInvoiced());
    }

    /**
     * Determines if a job costing can be exported.
     *
     * @param job
     * @return
     */
    public Boolean canExportInvoice(Job job) {

        // Ensure the invoice date is set if the job costing was invoiced.
        if (job.getJobCostingAndPayment().getInvoiced()) {
            if (job.getJobStatusAndTracking().getDateCostingInvoiced() == null) {
                job.getJobStatusAndTracking().setDateCostingInvoiced(new Date());
            }
        } else {
            return false;
        }

        // Ensure the client ID is set.
        if (job.getClient().getFinancialAccount().getIdCust().isEmpty()) {

            // Ensure that an accounting Id is added for the client  
            AccPacCustomer financialAccount = AccPacCustomer.findByName(
                    getEntityManager2(), job.getClient().getName());
            if (financialAccount != null) {
                // Set accounting Id
                job.getClient().setAccountingId(financialAccount.getIdCust());
                // Set credit limit 
                job.getClient().setCreditLimit((financialAccount.getCreditLimit().doubleValue()));
                // Update and save
                job.getClient().setEditedBy(getUser().getEmployee());
                job.getClient().setDateEdited(new Date());
                job.getClient().save(getEntityManager1());
            } else {

                return false;
            }
        }

        return true;
    }

    public Boolean getDisableInvoiceJobCosting() {
        return !getUser().getEmployee().getDepartment().getPrivilege().getCanEditInvoicingAndPayment()
                || !getCurrentJob().getJobCostingAndPayment().getCostingApproved();
    }

    /**
     * Determines if a job costing can be invoiced. A PrimeFaces message is
     * displayed if the job costing cannot be invoiced.
     *
     * @param job
     * @return
     */
    public Boolean canInvoiceJobCosting(Job job) {

        // Check for permission to invoice by department that can do invoices
        // NB: This permission will be put in the user's profile in the future.
        if (!getUser().getEmployee().getDepartment().getPrivilege().getCanEditInvoicingAndPayment()) {
            PrimeFacesUtils.addMessage("Permission Denied",
                    "You do not have permission to create an invoice for "
                    + job.getJobNumber(),
                    FacesMessage.SEVERITY_ERROR);

            return false;

        }
        // Check if approved
        if (!job.getJobCostingAndPayment().getCostingApproved()) {

            PrimeFacesUtils.addMessage("Job Costing NOT Approved",
                    "The job costing was not approved for "
                    + job.getJobNumber(),
                    FacesMessage.SEVERITY_ERROR);

            return false;

        }
        // Check for a valid client Id
        if (job.getClient().getFinancialAccount().getIdCust().isEmpty()) {

            PrimeFacesUtils.addMessage("Client Identification required",
                    "The client identification (Id) is not set for "
                    + job.getJobNumber(),
                    FacesMessage.SEVERITY_ERROR);

            return false;

        }

        return true;

    }

    /**
     * Gets all Job TableView preferences.
     *
     * @return
     */
    public List<Preference> getJobTableViewPreferences() {
        EntityManager em = getEntityManager1();

        List<Preference> prefs = Preference.findAllPreferencesByName(em, "jobTableView");

        return prefs;
    }

    /**
     * Determine if the current user can mark the current job costing as being
     * completed. This is done by determining if the job was assigned to the
     * user.
     *
     * @param job
     * @return
     */
    public Boolean canUserCompleteJobCosting(Job job) {
        return isJobAssignedToUserDepartment(job);
    }

    /**
     * Gets the selected Cash Payment object.
     *
     * @return
     */
    public CashPayment getSelectedCashPayment() {
        return selectedCashPayment;
    }

    /**
     * Sets the selected Cash Payment object. The related costs are updated and
     * the is corresponding job is saved.
     *
     * @param selectedCashPayment
     */
    public void setSelectedCashPayment(CashPayment selectedCashPayment) {

        this.selectedCashPayment = selectedCashPayment;

        // If this is a new cash payment ensure that all related costs are updated
        // and the job cost and payment saved.
        if (getSelectedCashPayment().getId() == null) {
            updateFinalCost();
            updateAmountDue();

            if (!getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {
                PrimeFacesUtils.addMessage("Payment and Job NOT Saved!",
                        "Payment and the job and the payment were NOT saved!",
                        FacesMessage.SEVERITY_ERROR);
            }
        }
    }

    /**
     * Creates and gets Entity Manager 2.
     *
     * @return
     */
    public EntityManager getEntityManager2() {
        return getJobManager().getEntityManager2();
    }

    /**
     * Updates the status of the current job costing and payment as being
     * edited.
     */
    public void updateJobCostingAndPayment() {
        setJobCostingAndPaymentDirty(true);
    }

    public void updateCashPayment() {
        getSelectedCashPayment().setIsDirty(true);
    }

    public void updateCostComponent() {
        updateCostType();
        getSelectedCostComponent().setIsDirty(true);
    }

    public void updateSubcontract(AjaxBehaviorEvent event) {
        if (!((SelectOneMenu) event.getComponent()).getValue().toString().equals("null")) {
            Long subcontractId = new Long(((SelectOneMenu) event.getComponent()).getValue().toString());
            Job subcontract = Job.findJobById(getEntityManager1(), subcontractId);

            selectedCostComponent.setCost(subcontract.getJobCostingAndPayment().getFinalCost());
            selectedCostComponent.setName("Subcontract (" + subcontract.getJobNumber() + ")");
            selectedCostComponent.setCode(subcontract.getJobNumber());

            updateCostComponent();
        }
    }

    public void updateAllTaxes(AjaxBehaviorEvent event) {
        EntityManager em = getEntityManager1();

        if (getCurrentJob().getJobCostingAndPayment().getId() != null) {
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findJobCostingAndPaymentById(em,
                            getCurrentJob().getJobCostingAndPayment().getId());
            em.refresh(jcp);

            if (!(jcp.getCashPayments().isEmpty()
                    || getUser().can("BeFinancialAdministrator"))) {

                // Reset cash payments
                getCurrentJob().getJobCostingAndPayment().
                        setCashPayments(jcp.getCashPayments());

                PrimeFacesUtils.addMessage("Permission Denied",
                        "A payment was made on this job so update of this field is not allowed",
                        FacesMessage.SEVERITY_ERROR);

                setJobCostingAndPaymentDirty(false);
            } else {
                updateJobCostingEstimate();
                updateTotalCost();

                setJobCostingAndPaymentDirty(true);
            }

        } else {
            updateJobCostingEstimate();
            updateTotalCost();

            setJobCostingAndPaymentDirty(true);
        }

    }

    public String getSubcontractsMessage() {
        List<Job> subcontracts = new ArrayList<>();
        List<Job> existingSubcontracts = getSubcontractsForCostComponents(getCurrentJob());

        if (getCurrentJob().getId() != null) {
            if (!getCurrentJob().findSubcontracts(getEntityManager1()).isEmpty()) {
                subcontracts.removeAll(existingSubcontracts);
                subcontracts.addAll(getCurrentJob().findSubcontracts(getEntityManager1()));

                if (!subcontracts.isEmpty()) {
                    return ("{ " + subcontracts.size() + " subcontract(s) exist(s) that can be added as cost item(s) }");
                }
            } else if (!getCurrentJob().findPossibleSubcontracts(getEntityManager1()).isEmpty()) {
                subcontracts.removeAll(existingSubcontracts);
                subcontracts.addAll(getCurrentJob().findPossibleSubcontracts(getEntityManager1()));

                if (!subcontracts.isEmpty()) {
                    return ("{ " + subcontracts.size() + " possible subcontract(s) exist(s) that can be added as cost item(s) }");
                }
            } else {
                return "";
            }
        } else {
            return "";
        }

        return "";

    }

    public void updateMinimumDepositRequired() {
        EntityManager em = getEntityManager1();

        if (getCurrentJob().getJobCostingAndPayment().getId() != null) {
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findJobCostingAndPaymentById(em,
                            getCurrentJob().getJobCostingAndPayment().getId());
            em.refresh(jcp);

            if (!(jcp.getCashPayments().isEmpty()
                    || getUser().can("BeFinancialAdministrator"))) {

                // Reset min deposit required
                getCurrentJob().getJobCostingAndPayment().
                        setMinDeposit(jcp.getMinDeposit());

                // Reset cash payments
                getCurrentJob().getJobCostingAndPayment().
                        setCashPayments(jcp.getCashPayments());

                PrimeFacesUtils.addMessage("Permission Denied",
                        "A payment was made on this job so update of this field is not allowed",
                        FacesMessage.SEVERITY_ERROR);

                setJobCostingAndPaymentDirty(false);

            } else {
                updateJobCostingEstimate();

                setJobCostingAndPaymentDirty(true);
            }

        } else {
            updateJobCostingEstimate();

            setJobCostingAndPaymentDirty(true);
        }

    }

    public void updatePurchaseOrderNumber() {
        EntityManager em = getEntityManager1();

        if (getCurrentJob().getJobCostingAndPayment().getId() != null) {
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findJobCostingAndPaymentById(em,
                            getCurrentJob().getJobCostingAndPayment().getId());
            em.refresh(jcp);

            if (!(jcp.getCashPayments().isEmpty()
                    || getUser().can("BeFinancialAdministrator"))) {
                // Reset PO#
                getCurrentJob().getJobCostingAndPayment().
                        setPurchaseOrderNumber(jcp.getPurchaseOrderNumber());

                // Reset cash payments
                getCurrentJob().getJobCostingAndPayment().
                        setCashPayments(jcp.getCashPayments());

                PrimeFacesUtils.addMessage("Permission Denied",
                        "A payment was made on this job so update of this field is not allowed",
                        FacesMessage.SEVERITY_ERROR);

                setJobCostingAndPaymentDirty(false);
            } else {
                setJobCostingAndPaymentDirty(true);
            }

        } else {
            setJobCostingAndPaymentDirty(true);
        }

    }

    public void updateEstimatedCostIncludingTaxes() {
        updateJobCostingEstimate();
    }

    public void updateMinimumDepositIncludingTaxes() {
        updateJobCostingEstimate();
    }

    public void updateJobCostingEstimate() {

        EntityManager em = getEntityManager1();

        if (getCurrentJob().getJobCostingAndPayment().getId() != null) {
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findJobCostingAndPaymentById(em,
                            getCurrentJob().getJobCostingAndPayment().getId());
            em.refresh(jcp);

            if (!(jcp.getCashPayments().isEmpty()
                    || getUser().can("BeFinancialAdministrator"))) {
                // Reset cost estimate
                getCurrentJob().getJobCostingAndPayment().
                        setEstimatedCost(jcp.getEstimatedCost());

                // Reset cash payments
                getCurrentJob().getJobCostingAndPayment().
                        setCashPayments(jcp.getCashPayments());

                PrimeFacesUtils.addMessage("Permission Denied",
                        "A payment was made on this job so update of this field is not allowed",
                        FacesMessage.SEVERITY_ERROR);

                setJobCostingAndPaymentDirty(false);
            } else {

                setJobCostingAndPaymentDirty(true);

            }
        } else {
            setJobCostingAndPaymentDirty(true);
        }

    }

    public void updateTotalDeposit() {
        EntityManager em = getEntityManager1();

        Employee employee = Employee.findById(em, getUser().getEmployee().getId());
        if (employee != null) {
            getCurrentJob().getJobCostingAndPayment().setLastPaymentEnteredBy(employee);
        }
        updateAmountDue();
        setIsDirty(true);
    }

    public void update() {
        setIsDirty(true);
    }

    public void updateJobCostingValidity() {
        if (!validateCurrentJobCosting() && getCurrentJob().getJobCostingAndPayment().getCostingCompleted()) {
            getCurrentJob().getJobCostingAndPayment().setCostingCompleted(false);
            getCurrentJob().getJobCostingAndPayment().setCostingApproved(false);
            //displayCommonMessageDialog(null, "Removing the content of a required field has invalidated this job costing", "Invalid Job Costing", "info");
        } else {
            setJobCostingAndPaymentDirty(true);
        }
    }

    public Boolean getCanPrepareJobCosting() {
        return !getCurrentJob().getJobCostingAndPayment().getCostingApproved();
    }

    public void prepareJobCosting() {

        if (getCurrentJob().getJobCostingAndPayment().getCostingApproved()) {
            getCurrentJob().getJobCostingAndPayment().setCostingCompleted(!getCurrentJob().getJobCostingAndPayment().getCostingCompleted());
            PrimeFacesUtils.addMessage("Job Costing Already Approved",
                    "The job costing preparation status cannot be changed because it was already approved",
                    FacesMessage.SEVERITY_ERROR);
        } else if (getCurrentJob().getJobCostingAndPayment().getCostingCompleted()) {
            getCurrentJob().getJobStatusAndTracking().setDateCostingCompleted(new Date());
            getCurrentJob().getJobStatusAndTracking().setCostingDate(new Date());
            getCurrentJob().getJobCostingAndPayment().setCostingPreparedBy(
                    getUser().getEmployee());

            BusinessEntityActionUtils.addAction(BusinessEntity.Action.PREPARE,
                    getCurrentJob().getActions());

        } else if (!getCurrentJob().getJobCostingAndPayment().getCostingCompleted()) {
            getCurrentJob().getJobStatusAndTracking().setDateCostingCompleted(null);
            getCurrentJob().getJobStatusAndTracking().setCostingDate(null);
            getCurrentJob().getJobCostingAndPayment().setCostingPreparedBy(null);

            BusinessEntityActionUtils.removeAction(BusinessEntity.Action.PREPARE,
                    getCurrentJob().getActions());
        }

        setJobCostingAndPaymentDirty(true);
    }

    /**
     * Determine if the current user is the department's supervisor. This is
     * done by determining if the user is the head/active acting head of the
     * department to which the job was assigned.
     *
     * @param job
     * @return
     */
    public Boolean isUserDepartmentSupervisor(Job job) {
        EntityManager em = getEntityManager1();

        if (job.getDepartment().getId() != null) {
            if (Department.findAssignedToJob(job, em).getHead().getId().longValue() == getUser().getEmployee().getId().longValue()) {
                return true;
            } else {
                return (Department.findAssignedToJob(job, em).getActingHead().getId().longValue() == getUser().getEmployee().getId().longValue())
                        && Department.findAssignedToJob(job, em).getActingHeadActive();
            }
        } else {
            return false;
        }
    }

    public void approveJobCosting() {

        if (canChangeJobCostingApprovalStatus(getCurrentJob(), false)) {
            if (getCurrentJob().getJobCostingAndPayment().getCostingApproved()) {
                getCurrentJob().getJobStatusAndTracking().setDateCostingApproved(new Date());
                getCurrentJob().getJobCostingAndPayment().setCostingApprovedBy(
                        getUser().getEmployee());

                BusinessEntityActionUtils.addAction(BusinessEntity.Action.APPROVE,
                        getCurrentJob().getActions());

            } else {
                getCurrentJob().getJobStatusAndTracking().setDateCostingApproved(null);
                getCurrentJob().getJobCostingAndPayment().setCostingApprovedBy(null);
                BusinessEntityActionUtils.removeAction(BusinessEntity.Action.APPROVE,
                        getCurrentJob().getActions());
            }
            setJobCostingAndPaymentDirty(true);
        } else {
            // Reset the costing status
            getCurrentJob().getJobCostingAndPayment().
                    setCostingApproved(!getCurrentJob().getJobCostingAndPayment().getCostingApproved());
        }
    }

    private Boolean areThereUnapprovedSubcontracts(Job job) {
        if (job.getSubcontracts(getEntityManager1()).isEmpty()) {
            return false;
        } else {
            for (Job subcontract : job.getSubcontracts(getEntityManager1())) {
                if (!subcontract.getJobCostingAndPayment().getCostingApproved()
                        && !subcontract.getJobStatusAndTracking().getWorkProgress().equals("Cancelled")) {
                    return true;
                }
            }
        }

        return false;
    }

    private Boolean checkForCostComponentCode(Job job, String code) {

        for (CostComponent component : job.getJobCostingAndPayment().getCostComponents()) {
            if (component.getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    private Boolean areThereSubcontractCostingsToInclude(Job job) {
        if (job.getSubcontracts(getEntityManager1()).isEmpty()) {
            return false;
        } else {
            for (Job subcontract : job.getSubcontracts(getEntityManager1())) {
                if (!checkForCostComponentCode(job, subcontract.getJobNumber())
                        && !subcontract.getJobStatusAndTracking().getWorkProgress().equals("Cancelled")) {
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean canChangeJobCostingApprovalStatus(Job job, Boolean approve) {

        if (areThereUnapprovedSubcontracts(job)
                && (!job.getJobCostingAndPayment().getEstimate())
                && (job.getJobCostingAndPayment().getCostingApproved() || approve)) {
            PrimeFacesUtils.addMessage("Cannot Approve Job Costing",
                    "The job costing for " + job.getJobNumber()
                    + " cannot be approved until all subcontracts are approved",
                    FacesMessage.SEVERITY_ERROR);

            return false;
        }

        if (!job.getJobCostingAndPayment().getCostingCompleted()
                || job.getJobCostingAndPayment().getInvoiced()) {

            PrimeFacesUtils.addMessage("Cannot Change Approval Status",
                    "The job costing approval status for " + job.getJobNumber()
                    + " cannot be changed before the job costing is prepared or if it was already invoiced",
                    FacesMessage.SEVERITY_ERROR);

            return false;

        } else if (getCanApproveJobCosting(job)) {

            return true;

        } else {

            PrimeFacesUtils.addMessage("No Permission",
                    "You do not have the permission to change the job costing approval status for " + job.getJobNumber(),
                    FacesMessage.SEVERITY_ERROR);

            return false;

        }
    }

    public void updatePreferences() {
        setIsDirty(true);
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

    public void updateNewClient() {
        setIsDirty(true);
    }

    public void updateJobNumber() {
        setIsDirty(true);
    }

    public void updateSamplesCollected() {
        setIsDirty(true);
    }

    public void closeUnitCostDialog() {

        // prompt to save modified job before attempting to create new job
        if (getIsDirty()) {
            // ask to save         
            //displayCommonConfirmationDialog(initDialogActionHandlerId("unitCostDirty"), "This unit cost was modified. Do you wish to save it?", "Unit Cost Not Saved", "info");
        } else {
            PrimeFaces.current().executeScript("PF('unitCostDialog').hide();");
        }

    }

    public void cancelJobCostingAndPaymentEdit(ActionEvent actionEvent) {
        EntityManager em = getEntityManager1();

        // Refetch costing data from database
        if (getCurrentJob() != null) {
            if (getCurrentJob().getId() != null) {
                Job job = Job.findJobById(em, getCurrentJob().getId());
                getCurrentJob().setJobCostingAndPayment(job.getJobCostingAndPayment());
            }
        }

        setJobCostingAndPaymentDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void jobCostingDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (isJobCostingAndPaymentDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {
                    getJobManager().processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Costing and " + getCurrentJob().getType()
                            + " Saved", "This " + getCurrentJob().getType()
                            + " and the costing were saved", FacesMessage.SEVERITY_INFO);
                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Costing and " + getCurrentJob().getType()
                            + " NOT Saved", "This "
                            + getCurrentJob().getType()
                            + " and the costing were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void okJobCostingAndPayment(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okJobCosting(ActionEvent actionEvent) {

        try {

            if (getUser().getEmployee() != null) {
                getCurrentJob().getJobCostingAndPayment().setFinalCostDoneBy(getUser().getEmployee().getName());
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveProformaInvoice(ActionEvent actionEvent) {

        try {

            if (getUser().getEmployee() != null) {
                getCurrentJob().getJobCostingAndPayment().setFinalCostDoneBy(getUser().getEmployee().getName());
            }

            getJobManager().saveJob(getCurrentJob());

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void saveCostEstimate(ActionEvent actionEvent) {

        try {

            if (getUser().getEmployee() != null) {
                getCurrentJob().getJobCostingAndPayment().setFinalCostDoneBy(getUser().getEmployee().getName());
            }

            getJobManager().saveJob(getCurrentJob());

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public Boolean validateCurrentJobCosting() {

        try {
            // check for valid job
            if (getCurrentJob().getId() == null) {
                return false;
            }
            // check for job report # and description
            if ((getCurrentJob().getReportNumber() == null) || (getCurrentJob().getReportNumber().trim().equals(""))) {
                return false;
            }
            if (getCurrentJob().getJobDescription().trim().equals("")) {
                return false;
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return true;

    }

    public void saveUnitCost() {
        EntityManager em = getEntityManager1();

        try {

            // Validate and save objects
            // Department
            Department department = Department.findByName(em, getCurrentUnitCost().getDepartment().getName());
            if (department == null) {
                //setInvalidFormFieldMessage("This unit cost cannot be saved because a valid department was not entered.");

                return;
            } else {
                getCurrentUnitCost().setDepartment(department);
            }

            // Department unit
            DepartmentUnit departmentUnit = DepartmentUnit.findDepartmentUnitByName(em, getCurrentUnitCost().getDepartmentUnit().getName());
            if (departmentUnit == null) {
                getCurrentUnitCost().setDepartmentUnit(DepartmentUnit.getDefaultDepartmentUnit(em, "--"));
            } else {
                getCurrentUnitCost().setDepartmentUnit(departmentUnit);
            }

            // Laboratory unit
            Laboratory laboratory = Laboratory.findLaboratoryByName(em, getCurrentUnitCost().getLaboratory().getName());
            if (laboratory == null) {
                getCurrentUnitCost().setLaboratory(Laboratory.getDefaultLaboratory(em, "--"));
            } else {
                getCurrentUnitCost().setLaboratory(laboratory);
            }

            // Service
            if (getCurrentUnitCost().getService().trim().equals("")) {
                //setInvalidFormFieldMessage("This unit cost cannot be saved because a valid service was not entered.");

                return;
            }

            // Cost
            if (getCurrentUnitCost().getCost() <= 0.0) {
                //setInvalidFormFieldMessage("This unit cost cannot be saved because a valid cost was not entered.");

                return;
            }

            // Effective date
            if (getCurrentUnitCost().getEffectiveDate() == null) {
                //setInvalidFormFieldMessage("This unit cost cannot be saved because a valid effective date was not entered.");

                return;
            }

            // save job to database and check for errors
            em.getTransaction().begin();

            Long id = BusinessEntityUtils.saveBusinessEntity(em, currentUnitCost);
            if (id == null) {

                sendErrorEmail("An error occurred while saving this unit cost",
                        "Unit cost save error occurred");
                return;
            }

            em.getTransaction().commit();
            setIsDirty(false);

        } catch (Exception e) {

            System.out.println(e);
            // send error message to developer's email
            sendErrorEmail("An exception occurred while saving a unit cost!",
                    "\nJMTS User: " + getUser().getUsername()
                    + "\nDate/time: " + new Date()
                    + "\nException detail: " + e);
        }
    }

    public String getCompletedJobCostingEmailMessage(Job job) {
        EntityManager em = getEntityManager1();
        String message = "";
        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

        message = message + "Dear Colleague,<br><br>";
        message = message + "The costing for a job with the following details was completed via the <a href='http://boshrmapp:8080/jmts'>Job Management & Tracking System (JMTS)</a>:<br><br>";
        message = message + "<span style='font-weight:bold'>Job number: </span>" + job.getJobNumber() + "<br>";
        message = message + "<span style='font-weight:bold'>Client: </span>" + job.getClient().getName() + "<br>";
        if (!job.getSubContractedDepartment().getName().equals("--")) {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getSubContractedDepartment().getName() + "<br>";
        } else {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getDepartment().getName() + "<br>";
        }
        message = message + "<span style='font-weight:bold'>Date submitted: </span>" + formatter.format(job.getJobStatusAndTracking().getDateSubmitted()) + "<br>";
        message = message + "<span style='font-weight:bold'>Current assignee: </span>" + BusinessEntityUtils.getPersonFullName(job.getAssignedTo(), Boolean.FALSE) + "<br>";
        message = message + "<span style='font-weight:bold'>Task/Sample descriptions: </span>" + job.getJobSampleDescriptions() + "<br><br>";
        message = message + "As the department's supervisor/head, you are required to review and approve this costing. An email will be automatically sent to the Finance department after approval.<br><br>";
        message = message + "If this job was incorrectly assigned to your department, the department supervisor should contact the person who entered/assigned the job.<br><br>";
        message = message + "This email was automatically generated and sent by the <a href='http://boshrmapp:8080/jmts'>JMTS</a>. Please DO NOT reply.<br><br>";
        message = message + "Signed<br>";
        message = message + SystemOption.getString(em, "jobManagerEmailName");

        return message;
    }

    public String getApprovedJobCostingEmailMessage(Job job) {
        String message = "";
        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
        EntityManager em = getEntityManager1();

        message = message + "Dear Colleague,<br><br>";
        message = message + "The costing for a job with the following details was approved via the <a href='http://boshrmapp:8080/jmts'>Job Management & Tracking System (JMTS)</a>:<br><br>";
        message = message + "<span style='font-weight:bold'>Job number: </span>" + job.getJobNumber() + "<br>";
        message = message + "<span style='font-weight:bold'>Client: </span>" + job.getClient().getName() + "<br>";
        if (!job.getSubContractedDepartment().getName().equals("--")) {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getSubContractedDepartment().getName() + "<br>";
        } else {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getDepartment().getName() + "<br>";
        }
        message = message + "<span style='font-weight:bold'>Date submitted: </span>" + formatter.format(job.getJobStatusAndTracking().getDateSubmitted()) + "<br>";
        message = message + "<span style='font-weight:bold'>Department/Unit Head: </span>" + BusinessEntityUtils.getPersonFullName(Department.findAssignedToJob(job, em).getHead(), false) + "<br>";
        message = message + "<span style='font-weight:bold'>Task/Sample descriptions: </span>" + job.getJobSampleDescriptions() + "<br><br>";
        message = message + "If this email was sent to you in error, please contact the department referred to above.<br><br>";
        message = message + "This email was automatically generated and sent by the <a href='http://boshrmapp:8080/jmts'>JMTS</a>. Please DO NOT reply.<br><br>";
        message = message + "Signed<br>";
        message = message + SystemOption.getString(em, "jobManagerEmailName");

        return message;
    }

    public String getNewJobEmailMessage(Job job) {
        EntityManager em = getEntityManager1();
        String message = "";
        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

        message = message + "Dear Colleague,<br><br>";
        message = message + "A job with the following details was assigned to your department via the <a href='http://boshrmapp:8080/jmts'>Job Management & Tracking System (JMTS)</a>:<br><br>";
        message = message + "<span style='font-weight:bold'>Job number: </span>" + job.getJobNumber() + "<br>";
        message = message + "<span style='font-weight:bold'>Client: </span>" + job.getClient().getName() + "<br>";
        if (!job.getSubContractedDepartment().getName().equals("--")) {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getSubContractedDepartment().getName() + "<br>";
        } else {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getDepartment().getName() + "<br>";
        }
        message = message + "<span style='font-weight:bold'>Date submitted: </span>" + formatter.format(job.getJobStatusAndTracking().getDateSubmitted()) + "<br>";
        message = message + "<span style='font-weight:bold'>Current assignee: </span>" + BusinessEntityUtils.getPersonFullName(job.getAssignedTo(), Boolean.FALSE) + "<br>";
        message = message + "<span style='font-weight:bold'>Entered by: </span>" + BusinessEntityUtils.getPersonFullName(job.getJobStatusAndTracking().getEnteredBy(), Boolean.FALSE) + "<br>";
        message = message + "<span style='font-weight:bold'>Task/Sample descriptions: </span>" + job.getJobSampleDescriptions() + "<br><br>";
        message = message + "If you are the department's supervisor, you should immediately ensure that the job was correctly assigned to your staff member who will see to its completion.<br><br>";
        message = message + "If this job was incorrectly assigned to your department, the department supervisor should contact the person who entered/assigned the job.<br><br>";
        message = message + "This email was automatically generated and sent by the <a href='http://boshrmapp:8080/jmts'>JMTS</a>. Please DO NOT reply.<br><br>";
        message = message + "Signed<br>";
        message = message + SystemOption.getString(em, "jobManagerEmailName");

        return message;
    }

    public String getUpdatedJobEmailMessage(Job job) {
        EntityManager em = getEntityManager1();
        String message = "";
        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

        message = message + "Dear Colleague,<br><br>";
        message = message + "A job with the following details was updated by someone outside of your department via the <a href='http://boshrmapp:8080/jmts'>Job Management & Tracking System (JMTS)</a>:<br><br>";
        message = message + "<span style='font-weight:bold'>Job number: </span>" + job.getJobNumber() + "<br>";
        message = message + "<span style='font-weight:bold'>Client: </span>" + job.getClient().getName() + "<br>";
        if (!job.getSubContractedDepartment().getName().equals("--")) {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getSubContractedDepartment().getName() + "<br>";
        } else {
            message = message + "<span style='font-weight:bold'>Department: </span>" + job.getDepartment().getName() + "<br>";
        }
        message = message + "<span style='font-weight:bold'>Date submitted: </span>" + formatter.format(job.getJobStatusAndTracking().getDateSubmitted()) + "<br>";
        message = message + "<span style='font-weight:bold'>Current assignee: </span>" + BusinessEntityUtils.getPersonFullName(job.getAssignedTo(), Boolean.FALSE) + "<br>";
        message = message + "<span style='font-weight:bold'>Updated by: </span>" + BusinessEntityUtils.getPersonFullName(job.getJobStatusAndTracking().getEditedBy(), Boolean.FALSE) + "<br>";
        message = message + "<span style='font-weight:bold'>Task/Sample descriptions: </span>" + job.getJobSampleDescriptions() + "<br><br>";
        message = message + "You are being informed of this update so that you may take the requisite action.<br><br>";
        message = message + "This email was automatically generated and sent by the <a href='http://boshrmapp:8080/jmts'>JMTS</a>. Please DO NOT reply.<br><br>";
        message = message + "Signed<br>";
        message = message + SystemOption.getString(em, "jobManagerEmailName");

        return message;
    }

    /**
     * Update/create alert for the current job if the job is not completed.
     *
     * @param em
     * @throws java.lang.Exception
     */
    public void updateAlert(EntityManager em) throws Exception {
        if (getCurrentJob().getJobStatusAndTracking().getCompleted() == null) {
            em.getTransaction().begin();

            Notification notification = Notification.findFirstNotificationByOwnerId(em, getCurrentJob().getId());
            if (notification == null) { // This seems to be a new job
                notification = new Notification(getCurrentJob().getId(), new Date(), "Job entered");
                em.persist(notification);
            } else {
                em.refresh(notification);
                notification.setActive(true);
                notification.setDueTime(new Date());
                notification.setStatus("Job saved");
            }

            em.getTransaction().commit();
        } else if (!getCurrentJob().getJobStatusAndTracking().getCompleted()) {
            em.getTransaction().begin();

            Notification notification = Notification.findFirstNotificationByOwnerId(em, getCurrentJob().getId());
            if (notification == null) { // This seems to be a new job
                notification = new Notification(getCurrentJob().getId(), new Date(), "Job saved");
                em.persist(notification);
            } else {
                em.refresh(notification);
                notification.setActive(true);
                notification.setDueTime(new Date());
                notification.setStatus("Job saved");
            }

            em.getTransaction().commit();
        }

    }

    public void sendErrorEmail(String subject, String message) {
        try {
            // Send error message to developer's email            
            MailUtils.postMail(null, null,
                    SystemOption.getString(getEntityManager1(),
                            "jobManagerEmailName"), null, subject, message,
                    "text/plain", getEntityManager1());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void deleteCostComponent() {
        deleteCostComponentByName(selectedCostComponent.getName());
    }

    // Remove this and other code out of JobManager? Put in JobCostingAndPayment or Job?
    public void deleteCostComponentByName(String componentName) {

        List<CostComponent> components = getCurrentJob().getJobCostingAndPayment().getAllSortedCostComponents();
        int index = 0;
        for (CostComponent costComponent : components) {
            if (costComponent.getName().equals(componentName)) {
                components.remove(index);
                setJobCostingAndPaymentDirty(true);

                break;
            }
            ++index;
        }

        updateFinalCost();
        updateAmountDue();
    }

    public void editCostComponent(ActionEvent event) {
        setEdit(true);
    }

    public void createNewCashPayment(ActionEvent event) {

        if (getCurrentJob().getId() != null) {
            selectedCashPayment = new CashPayment();

            // If there were other payments it is assumed that this is a final payment.
            // Otherwsie, it is assumed to be a deposit.
            if (getCurrentJob().getJobCostingAndPayment().getCashPayments().size() > 0) {
                selectedCashPayment.setPaymentPurpose("Final");
            } else {
                selectedCashPayment.setPaymentPurpose("Deposit");
            }

            editCashPayment(event);
        } else {
            PrimeFacesUtils.addMessage("Job NOT Saved",
                    "Job must be saved before a new payment can be added",
                    FacesMessage.SEVERITY_WARN);
        }

    }

    public void editCashPayment(ActionEvent event) {

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

        PrimeFaces.current().dialog().openDynamic("/job/finance/cashPaymentDialog", options, null);

    }

    public void openClientCreditStatusDialog(ActionEvent event) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 500) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/accpac/clientCreditStatusDialog", options, null);

    }

    public void createNewCostComponent(ActionEvent event) {
        selectedCostComponent = new CostComponent();
        selectedCostComponent.setCurrency(getCurrentJob().getJobCostingAndPayment().getCurrency());
        setEdit(false);
    }

    public void cancelCashPaymentEdit() {
        selectedCashPayment.setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelCostComponentEdit() {
        selectedCostComponent.setIsDirty(false);
    }

    public void cashPaymentDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (isJobCostingAndPaymentDirty()) {
                Employee employee = Employee.findById(getEntityManager1(), getUser().getEmployee().getId());
                if (employee != null) {
                    getCurrentJob().getJobCostingAndPayment().setLastPaymentEnteredBy(employee);
                }

                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    getJobManager().processJobActions();

                    PrimeFacesUtils.addMessage("Payment and Job Saved", "The payment and the job were saved", FacesMessage.SEVERITY_INFO);
                }
            }
        }
    }

    public Boolean getIsJobCompleted() {
        return getIsJobCompleted(getCurrentJob());
    }

    public Boolean getIsJobCompleted(Job job) {
        if (job != null) {
            return job.getJobStatusAndTracking().getCompleted();
        } else {
            return false;
        }
    }

    public void okCostingComponent() {
        if (selectedCostComponent.getId() == null && !getEdit()) {
            getCurrentJob().getJobCostingAndPayment().getCostComponents().add(selectedCostComponent);
        }

        setEdit(false);
        updateFinalCost();
        updateAmountDue();

        PrimeFaces.current().executeScript("PF('costingComponentDialog').hide();");
    }

    public void updateFinalCost() {
        getCurrentJob().getJobCostingAndPayment().setFinalCost(getCurrentJob().getJobCostingAndPayment().getTotalJobCostingsAmount());
        setJobCostingAndPaymentDirty(true);
    }

    public void updateTotalCost() {
        updateAmountDue();
    }

    public void updateAmountDue() {
        setJobCostingAndPaymentDirty(true);
    }

    /**
     * This determine if taxes can be applied to the current job.
     *
     * @return
     */
    public Boolean getCanApplyTax() {

        return getCanApplyTax(getCurrentJob());
    }

    public Boolean getCanApplyTax(Job job) {

        return JobCostingAndPayment.getCanApplyTax(job)
                && getCanEditJobCosting(job);
    }

    /**
     * This determines if the user's main department can apply discounts to a
     * job costing.
     *
     * @return
     */
    public Boolean getCanApplyDiscount() {

        return getUser().getEmployee().getDepartment().getPrivilege().getCanApplyDiscountsToJobCosting();
    }

    public Boolean getCanApproveJobCosting() {

        return getCanApproveJobCosting(getCurrentJob());
    }

    public Boolean getCanApproveJobCosting(Job job) {
        EntityManager em = getEntityManager1();

        return ((isUserDepartmentSupervisor(job)
                || (getUser().isMemberOf(em, Department.findAssignedToJob(job, em))
                && getUser().can("ApproveJobCosting")))
                && !job.getJobCostingAndPayment().getInvoiced()
                && job.getJobCostingAndPayment().getCostingCompleted());
    }

    public void openJobCostingDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {
            // Reload cash payments if possible to avoid overwriting them 
            // when saving
            // tk the following was commented out for testing
            EntityManager em = getEntityManager1();
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findJobCostingAndPaymentById(em,
                            getCurrentJob().getJobCostingAndPayment().getId());

            if (jcp != null) {
                em.refresh(jcp);
                getCurrentJob().getJobCostingAndPayment().setCashPayments(jcp.getCashPayments());
                editJobCosting();
            }

            //editJobCosting(); // tk commented out for testing
        } else {
            // tk try to save the job before editing 
            if (getJobManager().getCurrentJob().getIsDirty()) {
                getJobManager().saveCurrentJob();
            }
            // tk edit costing if job was saved
            if (getCurrentJob().getId() != null) {
                editJobCosting();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the costing can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void editJobCosting() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 300) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/jobCostingDialog", options, null);

    }

    public void okClientCreditStatus() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okCashPayment() {

        if (getSelectedCashPayment().getId() == null) {
            getCurrentJob().getJobCostingAndPayment().getCashPayments().add(selectedCashPayment);
            BusinessEntityActionUtils.addAction(BusinessEntity.Action.PAYMENT,
                    getCurrentJob().getActions());
        }

        updateFinalCost();
        updateAmountDue();

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void doSearch() {

        doJobSearch();

    }

    public void updateDateSearchField() {
    }

    public void doJobSearch() {

        jobSearchResultList = findJobs(0);

    }

    public List<Job> findJobs(Integer maxResults) {
        return Job.findJobsByDateSearchField(getEntityManager1(),
                getUser(),
                getJobManager().getDateSearchPeriod(),
                getJobManager().getSearchType(),
                getJobManager().getSearchText(),
                maxResults, true);
    }

    public List<Job> findJobs() {
        return Job.findJobsByDateSearchField(getEntityManager1(),
                getUser(),
                getJobManager().getDateSearchPeriod(),
                getSearchType(),
                getProformaInvoiceSearchText(),
                0, // tk make system option
                true);
    }

    public List<JobCostingAndPayment> completeJobCostingAndPaymentName(String query) {
        EntityManager em;

        try {

            em = getEntityManager1();

            List<JobCostingAndPayment> results
                    = JobCostingAndPayment.findAllActiveJobCostingAndPaymentsByDepartmentAndName(em,
                            Department.findAssignedToJob(getCurrentJob(), em).getName(), query);

            return results;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<JobCostingAndPayment> completeAllJobCostingAndPaymentName(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<JobCostingAndPayment> results = JobCostingAndPayment.findAllJobCostingAndPaymentsByDepartmentAndName(em,
                    Department.findAssignedToJob(getCurrentJob(), em).getName(), query);

            return results;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<String> completeAccPacClientName(String query) {
        EntityManager em2;

        try {

            em2 = getEntityManager2();
            List<AccPacCustomer> clients = AccPacCustomer.findAllByName(em2, query);
            List<String> suggestions = new ArrayList<>();

            for (AccPacCustomer client : clients) {
                suggestions.add(client.getCustomerName());
            }

            return suggestions;
        } catch (Exception e) {

            System.out.println(e);

            return new ArrayList<>();
        }
    }

    // tk use the one in ClientManager?
    public List<AccPacCustomer> completeAccPacClient(String query) {
        EntityManager em2;

        try {
            em2 = getEntityManager2();

            return AccPacCustomer.findAllByName(em2, query);
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<CostCode> getAllCostCodes() {
        EntityManager em = getEntityManager1();

        List<CostCode> codes = CostCode.findAllCostCodes(em);

        return codes;
    }

    /**
     * Gets subcontracts for which cost components exists.
     *
     * @param job
     * @return
     */
    private List<Job> getSubcontractsForCostComponents(Job job) {
        List<Job> subcontracts = new ArrayList<>();

        for (Job subcontract : job.getSubcontracts(getEntityManager1())) {
            if (checkForCostComponentCode(job, subcontract.getJobNumber())
                    && !subcontract.getJobStatusAndTracking().getWorkProgress().equals("Cancelled")) {

                subcontracts.add(subcontract);
            }
        }

        return subcontracts;
    }

    public List<Job> getAllSubcontracts() {
        List<Job> subcontracts = new ArrayList<>();
        List<Job> existingSubcontracts = getSubcontractsForCostComponents(getCurrentJob());

        if (getCurrentJob().getId() != null) {
            subcontracts.removeAll(existingSubcontracts);

            if (!getCurrentJob().findSubcontracts(getEntityManager1()).isEmpty()) {
                subcontracts.addAll(getCurrentJob().findSubcontracts(getEntityManager1()));
            } else {
                subcontracts.addAll(getCurrentJob().findPossibleSubcontracts(getEntityManager1()));
            }

            subcontracts.add(0, new Job("-- select a subcontract --"));
        } else {
            subcontracts.add(0, new Job("-- none exists --"));
        }

        return subcontracts;
    }

    public Job getCurrentJob() {
        return getJobManager().getCurrentJob();
    }

    public void setCurrentJob(Job currentJob) {
        getJobManager().setCurrentJob(currentJob);
    }

    @Override
    public void setIsDirty(Boolean dirty) {
        getCurrentJob().setIsDirty(dirty);
    }

    @Override
    public Boolean getIsDirty() {
        return getCurrentJob().getIsDirty();
    }

    public void setJobCostingAndPaymentDirty(Boolean dirty) {
        getCurrentJob().getJobCostingAndPayment().setIsDirty(dirty);
        getCurrentJob().setIsDirty(dirty);

        if (dirty) {
            getCurrentJob().getJobStatusAndTracking().setEditStatus("(edited)");
        } else {
            getCurrentJob().getJobStatusAndTracking().setEditStatus("");
        }
    }

    public void setJobCostingAndPaymentDirty(Job job, Boolean dirty) {
        job.getJobCostingAndPayment().setIsDirty(dirty);

        if (dirty) {
            job.getJobStatusAndTracking().setEditStatus("(edited)");
        } else {
            job.getJobStatusAndTracking().setEditStatus("");
        }
    }

    public Boolean isJobCostingAndPaymentDirty() {
        return getCurrentJob().getJobCostingAndPayment().getIsDirty();
    }

    public void updateCurrentUnitCostDepartment() {
        EntityManager em;

        try {
            em = getEntityManager1();
            if (currentUnitCost.getDepartment().getName() != null) {
                Department department = Department.findByName(em, currentUnitCost.getDepartment().getName());
                if (department != null) {
                    currentUnitCost.setDepartment(department);
                    setIsDirty(true);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void updateCurrentUnitCostDepartmentUnit() {
        EntityManager em;

        try {
            em = getEntityManager1();
            if (currentUnitCost.getDepartmentUnit().getName() != null) {
                DepartmentUnit departmentUnit = DepartmentUnit.findDepartmentUnitByName(em, currentUnitCost.getDepartmentUnit().getName());
                if (departmentUnit != null) {
                    currentUnitCost.setDepartmentUnit(departmentUnit);
                    setIsDirty(true);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void updateCurrentUnitCostDepartmentLab() {
        EntityManager em;

        try {
            em = getEntityManager1();
            if (currentUnitCost.getLaboratory().getName() != null) {
                Laboratory laboratory = Laboratory.findLaboratoryByName(em, currentUnitCost.getLaboratory().getName());

                if (laboratory != null) {
                    currentUnitCost.setLaboratory(laboratory);
                    setIsDirty(true);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void updateUnitCostDepartment() {
        EntityManager em;

        try {
            em = getEntityManager1();
            if (unitCostDepartment.getName() != null) {
                Department department = Department.findByName(em, unitCostDepartment.getName());
                if (department != null) {
                    unitCostDepartment = department;
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateJobCostDepartment() {
        EntityManager em;

        try {
            em = getEntityManager1();
            if (jobCostDepartment.getName() != null) {
                Department department = Department.findByName(em, jobCostDepartment.getName());
                if (department != null) {
                    jobCostDepartment = department;
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateAccPacClient() {

        setShowPrepayments(false);

        filteredAccPacCustomerDocuments = new ArrayList<>();

        try {
            if (getCurrentJob() != null) {
                if (!getCurrentJob().getClient().getName().equals("")) {
                    accPacCustomer.setCustomerName(getCurrentJob().getClient().getName());
                } else {
                    accPacCustomer.setCustomerName("?");
                }
                updateCreditStatus(null);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void updateAccPacCustomer(SelectEvent event) {
        if (accPacCustomer != null) {
            try {
                EntityManager em = getEntityManager2();

                accPacCustomer = AccPacCustomer.findByName(em, accPacCustomer.getCustomerName());
                if (accPacCustomer == null) {
                    accPacCustomer = new AccPacCustomer();
                    accPacCustomer.setCustomerName("");
                }

                // set the found client name to the present job client
                if (accPacCustomer.getCustomerName() != null) {
                    updateCreditStatus(null);
                }

            } catch (Exception e) {
                System.out.println(e);
                accPacCustomer = new AccPacCustomer();
                accPacCustomer.setCustomerName("");
            }
        }
    }

    public Integer getNumberOfFilteredAccPacCustomerDocuments() {
        if (filteredAccPacCustomerDocuments != null) {
            return filteredAccPacCustomerDocuments.size();
        }

        return 0;
    }

    public Boolean getFilteredDocumentAvailable() {
        if (filteredAccPacCustomerDocuments != null) {
            return !filteredAccPacCustomerDocuments.isEmpty();
        } else {
            return false;
        }
    }

    public void updateCreditStatusSearch() {
        accPacCustomer.setCustomerName(getCurrentJob().getClient().getName());
        updateCreditStatus(null);
    }

    public void updateCreditStatus(SelectEvent event) {
        EntityManager em = getEntityManager2();

        accPacCustomer = AccPacCustomer.findByName(em, accPacCustomer.getCustomerName().trim());

        if (accPacCustomer != null) {
            if (getShowPrepayments()) {
                filteredAccPacCustomerDocuments = AccPacDocument.findAccPacInvoicesDueByCustomerId(em, accPacCustomer.getIdCust(), true);
            } else {
                filteredAccPacCustomerDocuments = AccPacDocument.findAccPacInvoicesDueByCustomerId(em, accPacCustomer.getIdCust(), false);
            }
        } else {
            createNewAccPacCustomer();
        }
    }

    public void updateCreditStatus() {
        updateCreditStatus(null);
    }

    public void createNewAccPacCustomer() {
        if (accPacCustomer != null) {
            accPacCustomer = new AccPacCustomer(accPacCustomer.getCustomerName());
        } else {
            accPacCustomer = new AccPacCustomer(null);
        }
        accPacCustomer.setIdCust(null);
        filteredAccPacCustomerDocuments = new ArrayList<>();
    }

    public String getAccPacCustomerID() {
        if (accPacCustomer.getIdCust() == null) {
            return "";
        } else {
            return accPacCustomer.getIdCust();
        }
    }

    public String getAccPacCustomerName() {
        if (accPacCustomer.getCustomerName() == null) {
            return "{Not found}";
        } else {
            return accPacCustomer.getCustomerName();
        }
    }

    public String getCustomerType() {
        if (accPacCustomer.getIDACCTSET().equals("TRADE")
                && accPacCustomer.getCreditLimit().doubleValue() > 0.0) {
            return "CREDIT";
        } else {
            return "REGULAR";
        }
    }

    public AccPacCustomer getAccPacCustomer() {
        return accPacCustomer;
    }

    public void setAccPacCustomer(AccPacCustomer accPacCustomer) {
        this.accPacCustomer = accPacCustomer;
    }

    public void updateCostingComponents() {
        if (selectedJobCostingTemplate != null) {
            EntityManager em = getEntityManager1();
            JobCostingAndPayment jcp
                    = JobCostingAndPayment.findActiveJobCostingAndPaymentByDepartmentAndName(em,
                            Department.findAssignedToJob(getCurrentJob(), em).getName(),
                            selectedJobCostingTemplate);
            if (jcp != null) {
                getCurrentJob().getJobCostingAndPayment().getCostComponents().clear();
                getCurrentJob().getJobCostingAndPayment().setCostComponents(copyCostComponents(jcp.getCostComponents()));

                setJobCostingAndPaymentDirty(true);
            } else {
                // Nothing yet
            }

            selectedJobCostingTemplate = null;

        }
    }

    public void onJobCostingAndPaymentCellEdit(CellEditEvent event) {

        getFoundJobCostingAndPayments().get(event.getRowIndex()).doSave(getEntityManager1());

    }

    public void removeCurrentJobCostingComponents(EntityManager em) {

        if (!getCurrentJob().getJobCostingAndPayment().getCostComponents().isEmpty()) {
            em.getTransaction().begin();
            for (CostComponent costComponent : getCurrentJob().getJobCostingAndPayment().getCostComponents()) {
                if (costComponent.getId() != null) {
                    costComponent = em.find(CostComponent.class, costComponent.getId());
                    em.remove(costComponent);
                }
            }

            getCurrentJob().getJobCostingAndPayment().getCostComponents().clear();
            BusinessEntityUtils.saveBusinessEntity(em, getCurrentJob().getJobCostingAndPayment());
            em.getTransaction().commit();
        }

    }

    /*
     * Takes a list of job costings enties an set their ids and component ids
     * to null which will result in new job costings being created when
     * the job costins are commited to the database
     */
    public List<JobCosting> copyJobCostings(List<JobCosting> srcCostings) {
        ArrayList<JobCosting> newJobCostings = new ArrayList<>();

        for (JobCosting jobCosting : srcCostings) {
            JobCosting newJobCosting = new JobCosting(jobCosting);
            for (CostComponent costComponent : jobCosting.getCostComponents()) {
                CostComponent newCostComponent = new CostComponent(costComponent);
                newJobCosting.getCostComponents().add(newCostComponent);
            }
            newJobCostings.add(newJobCosting);
        }

        return newJobCostings;
    }

    public List<CostComponent> copyCostComponents(List<CostComponent> srcCostComponents) {
        ArrayList<CostComponent> newCostComponents = new ArrayList<>();

        for (CostComponent costComponent : srcCostComponents) {
            CostComponent newCostComponent = new CostComponent(costComponent);
            newCostComponents.add(newCostComponent);
        }

        return newCostComponents;
    }

    public Long saveCashPayment(EntityManager em, CashPayment cashPayment) {
        return BusinessEntityUtils.saveBusinessEntity(em, cashPayment);
    }

    public List<CashPayment> getCashPaymentsByJobId(EntityManager em, Long jobId) {
        try {
            List<CashPayment> cashPayments
                    = em.createQuery("SELECT c FROM CashPayment c "
                            + "WHERE c.jobId "
                            + "= '" + jobId + "'", CashPayment.class).getResultList();

            return cashPayments;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void deleteCashPayment() {

        List<CashPayment> payments = getCurrentJob().getJobCostingAndPayment().getCashPayments();

        for (CashPayment payment : payments) {
            if (payment.equals(selectedCashPayment)) {
                payments.remove(selectedCashPayment);
                BusinessEntityActionUtils.removeAction(BusinessEntity.Action.PAYMENT,
                        getCurrentJob().getActions());
                break;
            }
        }

        updateFinalCost();
        updateAmountDue();

        // Do job save if possible...
        if (getCurrentJob().getId() != null
                && getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {
            PrimeFacesUtils.addMessage(getCurrentJob().getType() + " Saved",
                    "The payment was deleted and the " + getCurrentJob().getType() + " was saved",
                    FacesMessage.SEVERITY_INFO);
        } else {
            setJobCostingAndPaymentDirty(true);
            PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                    "The payment was deleted but the " + getCurrentJob().getType() + " was not saved",
                    FacesMessage.SEVERITY_WARN);
        }

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void checkForSubcontracts(ActionEvent event) {

    }

    public void openCashPaymentDeleteConfirmDialog(ActionEvent event) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() - 125) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/finance/cashPaymentDeleteConfirmDialog", options, null);

    }

    public void closeJCashPaymentDeleteConfirmDialog() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void openJobPricingsDialog() {

        PrimeFacesUtils.openDialog(null, "jobPricings", true, true, true, 400, 800);
    }

    public void openJobCostingsDialog() {

        PrimeFacesUtils.openDialog(null, "jobCostings", true, true, true, 400, 800);
    }

    public void doJobCostSearch() {
        System.out.println("To be implemented");
    }

    public void createNewUnitCost() {

        currentUnitCost = new UnitCost();

        PrimeFaces.current().ajax().update("unitCostForm");
        PrimeFaces.current().executeScript("PF('unitCostDialog').show();");
    }

    public void editUnitCost() {
    }

    public void onJobCostingsTableCellEdit(CellEditEvent event) {
        System.out.println("Job number of costing: " + getJobsWithCostings().get(event.getRowIndex()).getJobNumber());
    }

    /**
     * This is to be implemented further
     *
     * @return
     */
    public Boolean getDisableSubContracting() {
        try {
            if (getCurrentJob().getIsSubContract() || getCurrentJob().getIsToBeCopied()) {
                return false;
            } else {
                return getCurrentJob().getId() == null;
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public List<String> getDepartmentSupervisorsEmailAddresses(Department department) {
        List<String> emails = new ArrayList<>();

        emails.add(getEmployeeDefaultEmailAdress(department.getHead()));
        // Get the email of the acting head of he/she is currently acting
        if (department.getActingHeadActive()) {
            emails.add(getEmployeeDefaultEmailAdress(department.getActingHead()));
        }

        return emails;
    }

    public String getEmployeeDefaultEmailAdress(Employee employee) {
        String address = "";

        // Get email1 which is treated as the employee's company email address
        if (!employee.getInternet().getEmail1().trim().equals("")) {
            address = employee.getInternet().getEmail1();
        } else {
            // Get and set default email using company domain
            EntityManager em = getEntityManager1();

            String listAsString = (String) SystemOption.getOptionValueObject(em, "domainNames");
            String domainNames[] = listAsString.split(";");

            User user1 = User.findActiveJobManagerUserByEmployeeId(em, employee.getId());

            // Build email address
            if (user1 != null) {
                address = user1.getUsername();
                if (domainNames.length > 0) {
                    address = address + "@" + domainNames[0];
                }
            }

        }

        return address;
    }

    public Boolean isCurrentJobNew() {
        return (getCurrentJob().getId() == null);
    }

    public Department getDepartmentBySystemOptionDeptId(String option) {
        EntityManager em = getEntityManager1();

        Long id = (Long) SystemOption.getOptionValueObject(em, option);

        Department department = Department.findById(em, id);
        em.refresh(department);

        if (department != null) {
            return department;
        } else {
            return new Department("");
        }
    }

    public Boolean getIsMemberOfAccountsDept() {
        return getUser().isMemberOf(
                getEntityManager1(), getDepartmentBySystemOptionDeptId("accountsDepartmentId"));
    }

    public Boolean getIsMemberOfCustomerServiceDept() {
        return getUser().isMemberOf(
                getEntityManager1(), getDepartmentBySystemOptionDeptId("customerServiceDeptId"));
    }

    /**
     * Return discount types. NB: Discount types to be obtained from System
     * Options in the future
     *
     * @param query
     * @return
     */
    public List<String> completeDiscountType(String query) {
        String discountTypes[] = {"Currency", "Percentage"};
        List<String> matchedDiscountTypes = new ArrayList<>();

        for (String discountType : discountTypes) {
            if (discountType.contains(query)) {
                matchedDiscountTypes.add(discountType);
            }
        }

        return matchedDiscountTypes;

    }

}
