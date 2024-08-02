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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.StatusNote;
import jm.com.dpbennett.business.entity.fm.Classification;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.fm.JobCategory;
import jm.com.dpbennett.business.entity.jmts.JobCostingAndPayment;
import jm.com.dpbennett.business.entity.fm.JobSubCategory;
import jm.com.dpbennett.business.entity.sm.Preference;
import jm.com.dpbennett.business.entity.fm.Sector;
import jm.com.dpbennett.business.entity.jmts.ServiceContract;
import jm.com.dpbennett.business.entity.jmts.ServiceRequest;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.fm.AccPacCustomer;
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.hrm.BusinessOffice;
import jm.com.dpbennett.business.entity.jmts.Job;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;
import jm.com.dpbennett.business.entity.gm.BusinessEntityManagement;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.sm.Module;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityActionUtils;
import jm.com.dpbennett.business.entity.util.MailUtils;
import static jm.com.dpbennett.business.entity.util.NumberUtils.formatAsCurrency;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.fm.manager.InventoryManager;
import jm.com.dpbennett.fm.manager.PurchasingManager;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.jmts.JMTSApplication;
import jm.com.dpbennett.rm.manager.ReportManager;
import jm.com.dpbennett.sc.manager.ComplianceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.UnselectEvent;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.JobDataModel;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.ReportUtils;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public class JobManager extends GeneralManager
        implements Serializable, BusinessEntityManagement {

    private JMTSApplication application;
    private Job currentJob;
    private Job selectedJob;
    @ManagedProperty(value = "Jobs")
    private Boolean useAccPacCustomerList;
    private Boolean showJobEntry;
    private List<Job> jobSearchResultList;
    private Job[] selectedJobs;
    private AccPacCustomer accPacCustomer;
    private StatusNote selectedStatusNote;
    private SystemManager systemManager;
    private String searchText;
    private String searchType;

    public JobManager() {
        init();
    }

    public void jobGroupingDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Grouping"
                            + " Saved", "This job"
                            + " and the job grouping were saved", FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Grouping"
                            + " NOT Saved", "This job"
                            + " and the job grouping were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void jobStatusAndTrackingDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Status and Tracking"
                            + " Saved", "This job"
                            + " and the job status and tracking were saved",
                            FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Status and Tracking"
                            + " NOT Saved", "This job"
                            + " and the job status and tracking were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void jobReportingDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Reporting"
                            + " Saved", "This job"
                            + " and the job reporting were saved",
                            FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Reporting"
                            + " NOT Saved", "This job"
                            + " and the job reporting were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void editJobGrouping() {

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

        PrimeFaces.current().dialog().openDynamic("/job/grouping/jobGroupingDialog", options, null);

    }

    public void editJobStatusAndTracking() {

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

        PrimeFaces.current().dialog().openDynamic("/job/tracking/jobStatusAndTrackingDialog", options, null);

    }

    public void editJobReporting() {

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

        PrimeFaces.current().dialog().openDynamic("/job/reporting/jobReportingDialog", options, null);

    }

    public void openJobGroupingDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobGrouping();

        } else {

            if (getCurrentJob().getIsDirty()) {
                saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobGrouping();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the grouping can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void openJobStatusAndTrackingDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobStatusAndTracking();

        } else {

            if (getCurrentJob().getIsDirty()) {
                saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobStatusAndTracking();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the status and tracking can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void openJobReportingDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobReporting();

        } else {

            if (getCurrentJob().getIsDirty()) {
                saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobReporting();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the reporting can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void okJobGrouping(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okJobStatusAndTracking(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okJobReporting(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelJobGroupingEdit(ActionEvent actionEvent) {

        getCurrentJob().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelJobStatusAndTrackingEdit(ActionEvent actionEvent) {

        getCurrentJob().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelJobReportingEdit(ActionEvent actionEvent) {

        getCurrentJob().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Job Browser":
                getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:jobSearchButton");

                return true;

            case "Proforma Invoices":
                getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:proformaSearchButton");

                return true;

            default:
                return false;
        }
    }

    @Override
    public String getSearchText() {
        return searchText;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public String getLogoURL() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(), "JMTSLogo");
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public String getApplicationFooter() {

        return getApplicationHeader() + ", v"
                + SystemOption.getString(getSystemManager().getEntityManager1(),
                        "JMTSv");
    }

    public Boolean enableJobDialogTab(String tab) {

        if (tab.equals("General")
                && (getCurrentJob().getType().equals("Proforma Invoice")
                || getCurrentJob().getType().equals("Job"))) {
            return true;
        } else if (tab.equals("Services")) {
            return true;
        } else if (tab.equals("Samples") && getCurrentJob().getType().equals("Job")) {
            return true;
        } else if (tab.equals("CostingAndPayment")) {
            return true;
        } else if (tab.equals("Grouping") && getCurrentJob().getType().equals("Job")) {
            return true;
        } else if (tab.equals("StatusAndTracking") && getCurrentJob().getType().equals("Job")) {
            return true;
        } else if (tab.equals("Reporting") && getCurrentJob().getType().equals("Job")) {
            return true;
        }

        return false;

    }

    public Boolean getEnableJobDialogCostingTab() {

        return (getCurrentJob().getType().equals("Proforma Invoice")
                || getJobFinanceManager().getEnableOnlyPaymentEditing());

    }

    public SystemManager getSystemManager() {
        if (systemManager == null) {
            systemManager = BeanUtils.findBean("systemManager");
        }

        return systemManager;
    }

    @Override
    public String getAppShortcutIconURL() {

        return SystemOption.getString(
                getSystemManager().getEntityManager1(), "JMTSLogo");

    }

    private void sendJobEntryEmail(
            EntityManager em,
            Employee sendTo,
            String role,
            String action) {

        Email email = Email.findActiveEmailByName(em, "job-email-template");

        String jobNumber = getCurrentJob().getJobNumber();
        String department = getCurrentJob().getDepartmentAssignedToJob().getName();
        String APPURL = (String) SystemOption.getOptionValueObject(em, "appURL");
        String assignee = getCurrentJob().getAssignedTo().getFirstName()
                + " " + getCurrentJob().getAssignedTo().getLastName();
        String enteredBy = getCurrentJob().getJobStatusAndTracking().getEnteredBy().getFirstName()
                + " " + getCurrentJob().getJobStatusAndTracking().getEnteredBy().getLastName();
        String dateSubmitted = BusinessEntityUtils.
                getDateInMediumDateFormat(getCurrentJob().getJobStatusAndTracking().getDateSubmitted());
        String instructions = getCurrentJob().getInstructions();

        try {
            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    sendTo.getInternet().getEmail1(),
                    email.getSubject().
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber),
                    email.getContent("/correspondences/").
                            replace("{assignee}", assignee).
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber).
                            replace("{APPURL}", APPURL).
                            replace("{enteredBy}", enteredBy).
                            replace("{department}", department).
                            replace("{dateSubmitted}", dateSubmitted).
                            replace("{role}", role).
                            replace("{instructions}", instructions),
                    email.getContentType(),
                    em);
        } catch (Exception e) {
            System.out.println("Error sending email...");
        }

    }

    private void sendJobPaymentEmail(
            EntityManager em,
            Employee sendTo,
            String role,
            String action) {

        Email email = Email.findActiveEmailByName(em, "job-payment-email-template");

        String jobNumber = getCurrentJob().getJobNumber();
        String department = getCurrentJob().getDepartmentAssignedToJob().getName();
        String APPURL = (String) SystemOption.getOptionValueObject(em, "appURL");
        String assignee = getCurrentJob().getAssignedTo().getFirstName()
                + " " + getCurrentJob().getAssignedTo().getLastName();
        String paymentAmount = "$0.00";
        if (!getCurrentJob().getCashPayments().isEmpty()) {
            // Get and use last payment  
            paymentAmount = formatAsCurrency(getCurrentJob().getCashPayments().
                    get(getCurrentJob().getCashPayments().size() - 1).getPayment(), "$");
        }
        String dateOfPayment = BusinessEntityUtils.
                getDateInMediumDateFormat(
                        getCurrentJob().getCashPayments().
                                get(getCurrentJob().getCashPayments().size() - 1).getDateOfPayment());
        String paymentPurpose = getCurrentJob().getCashPayments().
                get(getCurrentJob().getCashPayments().size() - 1).getPaymentPurpose();

        try {
            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    sendTo.getInternet().getEmail1(),
                    email.getSubject().
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber),
                    email.getContent("/correspondences/").
                            replace("{assignee}", assignee).
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber).
                            replace("{APPURL}", APPURL).
                            replace("{paymentAmount}", paymentAmount).
                            replace("{department}", department).
                            replace("{dateOfPayment}", dateOfPayment).
                            replace("{role}", role).
                            replace("{paymentPurpose}", paymentPurpose),
                    email.getContentType(),
                    em);
        } catch (Exception e) {
            System.out.println("Error sending email...");
        }

    }

    private void sendChildJobCostingApprovalEmail(
            EntityManager em,
            Employee sendTo,
            String role,
            String action) {

        Email email = Email.findActiveEmailByName(em, "job-child-approval-email-template");

        String jobNumber = getCurrentJob().getJobNumber();
        String department = getCurrentJob().getDepartmentAssignedToJob().getName();
        String APPURL = (String) SystemOption.getOptionValueObject(em, "appURL");
        String assignee = getCurrentJob().getAssignedTo().getFirstName()
                + " " + getCurrentJob().getAssignedTo().getLastName();
        String approvalAmount = formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getFinalCost(), "$");
        String dateOfApproval = BusinessEntityUtils.
                getDateInMediumDateFormat(
                        getCurrentJob().getJobStatusAndTracking().getDateCostingApproved());

        try {
            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    sendTo.getInternet().getEmail1(),
                    email.getSubject().
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber),
                    email.getContent("/correspondences/").
                            replace("{assignee}", assignee).
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber).
                            replace("{APPURL}", APPURL).
                            replace("{approvalAmount}", approvalAmount).
                            replace("{department}", department).
                            replace("{dateOfApproval}", dateOfApproval).
                            replace("{role}", role),
                    email.getContentType(),
                    em);
        } catch (Exception e) {
            System.out.println("Error sending email...");
        }

    }

    private void sendJobCostingPreparedEmail(
            EntityManager em,
            Employee sendTo,
            String role,
            String action) {

        Email email = Email.findActiveEmailByName(em, "job-costing-prepared-email-template");

        String jobNumber = getCurrentJob().getJobNumber();
        String department = getCurrentJob().getDepartmentAssignedToJob().getName();
        String APPURL = (String) SystemOption.getOptionValueObject(em, "appURL");
        String head = sendTo.getFirstName()
                + " " + sendTo.getLastName();
        String amount = formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getFinalCost(), "$");
        String dateOfPreparation = BusinessEntityUtils.
                getDateInMediumDateFormat(
                        getCurrentJob().getJobStatusAndTracking().getDateCostingCompleted());

        try {
            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    sendTo.getInternet().getEmail1(),
                    email.getSubject().
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber),
                    email.getContent("/correspondences/").
                            replace("{head}", head).
                            replace("{action}", action).
                            replace("{jobNumber}", jobNumber).
                            replace("{APPURL}", APPURL).
                            replace("{amount}", amount).
                            replace("{department}", department).
                            replace("{datePrepared}", dateOfPreparation).
                            replace("{role}", role),
                    email.getContentType(),
                    em);
        } catch (Exception e) {
            System.out.println("Error sending email...");
        }

    }

    public void processJobActions() {
        for (BusinessEntity.Action action : getCurrentJob().getActions()) {
            switch (action) {
                case CREATE:
                    if (!Objects.equals(getCurrentJob().getAssignedTo().getId(),
                            getCurrentJob().getJobStatusAndTracking().getEnteredBy().getId())) {

                        sendJobEntryEmail(getSystemManager().getEntityManager1(),
                                getCurrentJob().getAssignedTo(),
                                "job assignee", "entered");
                    }
                    break;
                case PREPARE:
                    if (getCurrentJob().getIsSubContract()) {

                        sendJobCostingPreparedEmail(getSystemManager().getEntityManager1(),
                                getCurrentJob().getSubContractedDepartment().getHead(),
                                "head", "prepared");

                        if (getCurrentJob().getSubContractedDepartment().getActingHeadActive()) {
                            sendJobCostingPreparedEmail(getSystemManager().getEntityManager1(),
                                    getCurrentJob().getSubContractedDepartment().getHead(),
                                    "acting head", "prepared");
                        }

                    } else {

                        sendJobCostingPreparedEmail(getSystemManager().getEntityManager1(),
                                getCurrentJob().getDepartment().getHead(),
                                "head", "prepared");

                        if (getCurrentJob().getDepartment().getActingHeadActive()) {
                            sendJobCostingPreparedEmail(getSystemManager().getEntityManager1(),
                                    getCurrentJob().getDepartment().getHead(),
                                    "acting head", "prepared");
                        }

                    }

                    break;
                case APPROVE:
                    if (getCurrentJob().getIsSubContract()) {
                        if (getCurrentJob().getParent() != null) {
                            sendChildJobCostingApprovalEmail(getSystemManager().getEntityManager1(),
                                    getCurrentJob().getParent().getAssignedTo(),
                                    "assignee", "approved");
                        }
                    }
                    break;
                case PAYMENT:
                    sendJobPaymentEmail(getSystemManager().getEntityManager1(),
                            getCurrentJob().getAssignedTo(),
                            "job assignee", "payment");
                    break;
                default:
                    break;
            }
        }

        getCurrentJob().getActions().clear();
    }

    public void editStatusNote() {

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

        PrimeFaces.current().dialog().openDynamic("/job/tracking/statusNoteDialog", options, null);

    }

    public StatusNote getSelectedStatusNote() {
        return selectedStatusNote;
    }

    public void setSelectedStatusNote(StatusNote selectedStatusNote) {
        this.selectedStatusNote = selectedStatusNote;
    }

    public void okStatusNote() {
        selectedStatusNote.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelStatusNote() {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewStatusNote() {

        selectedStatusNote = new StatusNote();

        selectedStatusNote.setEntityId(getCurrentJob().getId());
        selectedStatusNote.setCreatedBy(getUser().getEmployee());
        selectedStatusNote.setDateCreated(new Date());
        selectedStatusNote.setHeader("Enter a status note below");

        editStatusNote();
    }

    public void statusNoteDialogReturn() {
    }

    public List<StatusNote> getStatusNotes() {
        List<StatusNote> notes = new ArrayList<>();

        if (getCurrentJob().getId() != null) {
            notes = StatusNote.findActiveStatusNotesByEntityId(getEntityManager1(),
                    getCurrentJob().getId());
        }

        if (!getCurrentJob().getJobStatusAndTracking().getStatusNote().isEmpty()) {
            notes.add(new StatusNote(getCurrentJob().getJobStatusAndTracking().getStatusNote(),
                    null,
                    null));
        }

        return notes;
    }

    public Boolean enableMultipleStatusNotes() {
        return SystemOption.getBoolean(getSystemManager().getEntityManager1(),
                "enableMultipleStatusNotes");
    }

    /**
     * Determines if a job dialog field is to be disabled.
     *
     * @param field
     * @return
     */
    public Boolean disableJobDialogField(String field) {

        return disableJobDialogField(getCurrentJob(), field);

    }

    public Boolean disableJobDialogField(Job job, String field) {

        Boolean fieldDisablingActive
                = SystemOption.getBoolean(
                        getSystemManager().getEntityManager1(),
                        "activateJobDialogFieldDisabling");

        Boolean userHasPrivilege = getUser().can("EditDisabledJobField");

        Boolean jobIsNotNew = job.getId() != null;

        switch (field) {
            case "businessOffice":
            case "classification":
            case "client":
            case "clientActionsMenu":
            case "billingAddress":
            case "clientContact":
            case "dateSubmitted":
            case "subContractedDepartment":
            case "estimatedTAT":
            case "tatRequired":
            case "instructions":
            case "service":
            case "additionalService":
            case "serviceLocation":
            case "specialInstructions":
            case "samples":
            case "otherServiceText":
            case "additionalServiceOtherText":
                return fieldDisablingActive
                        && !userHasPrivilege
                        && jobIsNotNew;
            case "department":
                if (getUser().can("CreateDirectSubcontract")) {
                    return false;
                }

                return (fieldDisablingActive
                        && !userHasPrivilege
                        && (jobIsNotNew)) || getDisableDepartment(job);
            case "costingandpayment":
                return (fieldDisablingActive
                        && !userHasPrivilege
                        && (jobIsNotNew)) || getJobFinanceManager().getIsJobCompleted(job);
            case "discount":
                return (fieldDisablingActive
                        && !userHasPrivilege
                        && (jobIsNotNew)) || !getJobFinanceManager().getCanApplyDiscount();
            case "tax":
                return (fieldDisablingActive
                        && !userHasPrivilege
                        && (jobIsNotNew)) || !getJobFinanceManager().getCanApplyTax(job);
            default:
                return false;
        }

    }

    public Boolean getDisableDepartment() {

        return getDisableDepartment(getCurrentJob());
    }

    public Boolean getDisableDepartment(Job job) {

        return getRenderSubContractingDepartment(job);
    }

    public Boolean getRenderSubContractingDepartment() {
        return getRenderSubContractingDepartment(getCurrentJob());
    }

    public Boolean getRenderSubContractingDepartment(Job job) {
        return job.getIsToBeSubcontracted() || job.getIsSubContract();
    }

    /**
     * Conditionally disable department entry. Currently not used.
     *
     * @return
     */
    public Boolean getDisableDepartmentEntry() {

        // allow department entry only if business office is null
        if (currentJob != null) {
            if (currentJob.getBusinessOffice() != null) {
                return !currentJob.getBusinessOffice().getCode().trim().equals("");
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onJobCellEdit(CellEditEvent event) {
        EntityManager em = getEntityManager1();

        Job job = getJobSearchResultList().get(event.getRowIndex());
        Job savedJob = Job.findJobById(em, job.getId());

        if (!isJobNew(job)) {
            savedJob = Job.findJobById(em, job.getId());
            if (savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && !User.isUserDepartmentSupervisor(savedJob, getUser(), em)) {

                job.setIsDirty(false);

                PrimeFacesUtils.addMessage(
                        "Job Cannot Be Saved",
                        "This job is marked as completed so changes cannot be saved. You may contact your supervisor or a system administrator",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }
        }

        switch (event.getColumn().getHeaderText()) {
            case "Instructions":
                if (!disableJobDialogField(job, "instructions")
                        && !savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")) {
                    setIsJobDirty(job, true);
                    saveJob(job);
                }
                break;
            case "Submitted":
                if (!disableJobDialogField(job, "dateSubmitted")
                        && !savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")) {
                    setIsJobDirty(job, true);
                    saveJob(job);
                }
                break;
            case "EDOC":
                if (!savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")) {
                    setIsJobDirty(job, true);
                    saveJob(job);
                }
                break;

        }

    }

    @Override
    public String getApplicationHeader() {
        return SystemOption.getString(getSystemManager().getEntityManager1(), "JMTSName");
    }

    public String getCopyrightOrganization() {
        return SystemOption.getString(getSystemManager().getEntityManager1(), "copyrightOrganization");
    }

    public String getOrganizationWebsite() {
        return SystemOption.getString(getSystemManager().getEntityManager1(), "organizationWebsite");
    }

    /**
     * Gets the ApplicationScoped object that is associated with this webapp.
     *
     * @return
     */
    public JMTSApplication getApplication() {
        if (application == null) {
            application = BeanUtils.findBean("App");
        }
        return application;
    }

    public List<String> getJobTableViews() {
        EntityManager em;

        try {
            em = getSystemManager().getEntityManager1();

            List<String> preferenceValues = Preference.findAllPreferenceValues(em, "");

            return preferenceValues;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void updateAccPacCustomer(SelectEvent event) {
        EntityManager em = getEntityManager2();

        accPacCustomer = AccPacCustomer.findByName(em, accPacCustomer.getCustomerName().trim());
        if (accPacCustomer != null) {
            if (accPacCustomer.getIdCust() != null) {
                accPacCustomer.setIsDirty(true);
            }
        }
    }

    /**
     * Gets the Accpac customer field.
     *
     * @return
     */
    public AccPacCustomer getAccPacCustomer() {
        if (accPacCustomer == null) {
            accPacCustomer = new AccPacCustomer();
        }
        return accPacCustomer;
    }

    /**
     * Sets the Accpac customer field.
     *
     * @param accPacCustomer
     */
    public void setAccPacCustomer(AccPacCustomer accPacCustomer) {
        this.accPacCustomer = accPacCustomer;
    }

    public void onJobCostingSelect(SelectEvent event) {
    }

    public void onJobCostingUnSelect(UnselectEvent event) {
    }

    /**
     * Handles the editing of cells in the Job Costing table.
     *
     * @param event
     */
    public void onJobCostingCellEdit(CellEditEvent event) {

        // Set edited by
        getJobSearchResultList().get(event.getRowIndex()).
                getClient().setEditedBy(getUser().getEmployee());

        // Set date edited
        getJobSearchResultList().get(event.getRowIndex()).
                getClient().setDateEdited(new Date());

        // Set the Accounting ID
        getJobSearchResultList().get(event.getRowIndex()).
                getClient().setAccountingId(
                        getJobSearchResultList().get(event.getRowIndex()).
                                getClient().getFinancialAccount().getIdCust());

        // Set credit limit
        getJobSearchResultList().get(event.getRowIndex()).
                getClient().setCreditLimit(
                        getJobSearchResultList().get(event.getRowIndex()).
                                getClient().getFinancialAccount().getCreditLimit().doubleValue());

        // Save
        getJobSearchResultList().get(event.getRowIndex()).
                getClient().save(getEntityManager1());

    }

    /**
     * Handles the initialization of the JobManager session bean.
     *
     */
    @Override
    public final void init() {
        reset();
    }

    /**
     * Get JobContractManager SessionScoped bean.
     *
     * @return
     */
    public JobContractManager getJobContractManager() {

        return BeanUtils.findBean("jobContractManager");
    }

    /**
     * Get JobSampleManager SessionScoped bean.
     *
     * @return
     */
    public JobSampleManager getJobSampleManager() {

        return BeanUtils.findBean("jobSampleManager");
    }

    /**
     * Get JobFinanceManager SessionScoped bean.
     *
     * @return
     */
    public JobFinanceManager getJobFinanceManager() {

        return BeanUtils.findBean("jobFinanceManager");
    }

    public FinanceManager getFinanceManager() {

        return BeanUtils.findBean("financeManager");
    }

    public ReportManager getReportManager() {
        return BeanUtils.findBean("reportManager");
    }

    public ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");
    }

    public HumanResourceManager getHumanResourceManager() {
        return BeanUtils.findBean("humanResourceManager");
    }

    public PurchasingManager getPurchasingManager() {
        return BeanUtils.findBean("purchasingManager");
    }

    public InventoryManager getInventoryManager() {
        return BeanUtils.findBean("inventoryManager");
    }

    public ComplianceManager getComplianceManager() {
        return BeanUtils.findBean("complianceManager");
    }

    public ArrayList<SelectItem> getAuthorizedSearchTypes() {

        ArrayList searchTypes = new ArrayList();

        if (getUser().can("EditJob")
                || getUser().can("EnterJob")
                || getUser().can("EditInvoicingAndPayment")) {

            searchTypes.add(new SelectItem("General", "General"));
            searchTypes.add(new SelectItem("My jobs", "My jobs"));
            searchTypes.add(new SelectItem("My department's jobs", "My department's jobs"));
            searchTypes.add(new SelectItem("Parent jobs only", "Parent jobs only"));
            searchTypes.add(new SelectItem("Unapproved job costings", "Unapproved job costings"));
            searchTypes.add(new SelectItem("Appr'd & uninv'd jobs", "Appr'd & uninv'd jobs"));
            searchTypes.add(new SelectItem("Incomplete jobs", "Incomplete jobs"));
            searchTypes.add(new SelectItem("Invoiced jobs", "Invoiced jobs"));
            searchTypes.add(new SelectItem("My dept's proforma invoices",
                    "My dept's proforma invoices"));

        } else {

            searchTypes.add(new SelectItem("My jobs", "My jobs"));
            searchTypes.add(new SelectItem("My department's jobs", "My department's jobs"));
            searchTypes.add(new SelectItem("My dept's proforma invoices",
                    "My dept's proforma invoices"));

        }

        return searchTypes;

    }

    public void clientDialogReturn() {
        if (getClientManager().getSelectedClient().getId() != null) {
            getCurrentJob().setClient(getClientManager().getSelectedClient());
        }
    }

    public void jobDialogReturn() {
        if (currentJob.getIsDirty()) {
            PrimeFacesUtils.addMessage("Job was NOT saved", "The recently edited job was not saved", FacesMessage.SEVERITY_WARN);
            PrimeFaces.current().ajax().update("appForm:growl3");
            currentJob.setIsDirty(false);
        }

    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("My department's jobs");
        setSearchText("");
        setModuleNames(new String[]{
            "jobManager",
            "jobFinanceManager",
            "jobContractManager",
            "clientManager",
            "reportManager",
            "systemManager",
            "financeManager",
            "purchasingManager",
            "inventoryManager",
            "humanResourceManager",
            "purchasingManager",
            "complianceManager"
        });

        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateAndTimeEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        showJobEntry = false;
        useAccPacCustomerList = false;
        jobSearchResultList = new ArrayList<>();
        getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:jobSearchButton");

    }

    public void openSystemBrowser() {
        getMainTabView().openTab("System Administration");
    }

    public void openFinancialAdministration() {
        getMainTabView().openTab("Financial Administration");
    }

    public void openHumanResourceBrowser() {

        getMainTabView().openTab("Human Resource");
    }

    public Boolean getCanApplyTax() {
        return JobCostingAndPayment.getCanApplyTax(getCurrentJob());
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("JMTSEM");
    }

    public void prepareToCloseJobDetail() {
        PrimeFacesUtils.closeDialog(null);
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

    public void openJobBrowser() {

        if (getSearchType().equals("Unapproved job costings")) {
            getUser().setJobTableViewPreference("Job Costings");
        }

        getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:jobSearchButton");

        getMainTabView().openTab("Job Browser");

    }

    public void openSystemAdministrationTab() {

        getMainTabView().openTab("System Administration");

    }

    public void openFinancialAdministrationTab() {

        getMainTabView().openTab("Financial Administration");

    }

    public Boolean getShowJobEntry() {
        return showJobEntry;
    }

    public void setShowJobEntry(Boolean showJobEntry) {
        this.showJobEntry = showJobEntry;
    }

    private Boolean isJobAssignedToUserDepartment() {

        if (getUser() != null) {
            if (currentJob.getDepartment().getId().longValue() == getUser().getEmployee().getDepartment().getId().longValue()) {
                return true;
            } else {
                return currentJob.getSubContractedDepartment().getId().longValue()
                        == getUser().getEmployee().getDepartment().getId().longValue();
            }
        } else {
            return false;
        }
    }

    public Boolean getCanEnterJob() {
        if (getUser() != null) {
            return getUser().can("EnterJob");
        } else {
            return false;
        }
    }

    /**
     * Can edit job only if the job is assigned to your department or if you
     * have job entry privilege
     *
     * @return
     */
    public Boolean getCanEditDepartmentalJob() {
        if (getCanEnterJob()) {
            return true;
        }

        if (getUser() != null) {
            return getUser().can("EditDepartmentJob") && isJobAssignedToUserDepartment();
        } else {
            return false;
        }
    }

    public Boolean getCanEditOwnJob() {
        if (getCanEnterJob()) {
            return true;
        }

        if (getUser() != null) {
            return getUser().can("EditOwnJob");
        } else {
            return false;
        }
    }

    /**
     * For future implementation if necessary
     *
     * @param query
     * @return
     */
    public List<String> completeSearchText(String query) {
        List<String> suggestions = new ArrayList<>();

        return suggestions;
    }

    public void createNewJob() {

        EntityManager em = getEntityManager1();

        createJob(em, false, false);
        getJobFinanceManager().setEnableOnlyPaymentEditing(false);

        editJob();
        openJobBrowser();
    }

    public void createNewSubcontract() {

        EntityManager em = getEntityManager1();

        createJob(em, false, false);
        getJobFinanceManager().setEnableOnlyPaymentEditing(false);
        getCurrentJob().setIsToBeSubcontracted(true);

        editJob();
        openJobBrowser();
    }

    public StreamedContent getServiceContractFile() {
        StreamedContent serviceContractStreamContent = null;

        try {

            Boolean useServiceContractJRXML
                    = (Boolean) SystemOption.getOptionValueObject(
                            getSystemManager().getEntityManager1(),
                            "useServiceContractJRXML");

            if (useServiceContractJRXML) {

                serviceContractStreamContent = getJobContractManager().getServiceContractStreamContentJRXML();
            } else {
                serviceContractStreamContent = getJobContractManager().getServiceContractStreamContent();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return serviceContractStreamContent;
    }

    public StreamedContent getServiceContractFileInExcel() {
        StreamedContent serviceContractStreamContent = null;

        try {

            serviceContractStreamContent
                    = getJobContractManager().getServiceContractStreamContent();

        } catch (Exception e) {
            System.out.println(e);
        }

        return serviceContractStreamContent;
    }

    public StreamedContent getServiceContractFileInPDF() {
        StreamedContent serviceContractStreamContent = null;

        try {

            serviceContractStreamContent = getJobContractManager().getServiceContractStreamContentJRXML();

        } catch (Exception e) {
            System.out.println(e);
        }

        return serviceContractStreamContent;
    }

    public Boolean getCurrentJobIsValid() {
        return getCurrentJob().getId() != null && !getCurrentJob().getIsDirty();
    }

    public List<Preference> getJobTableViewPreferences() {
        EntityManager em = getSystemManager().getEntityManager1();

        List<Preference> prefs = Preference.findAllPreferencesByName(em, "jobTableView");

        return prefs;
    }

    public void setJobCompletionDate(Date date) {
        currentJob.getJobStatusAndTracking().setDateOfCompletion(date);
    }

    public Date getJobCompletionDate() {
        if (currentJob != null) {
            return currentJob.getJobStatusAndTracking().getDateOfCompletion();
        } else {
            return null;
        }
    }

    // NB: This and other code that get date is no longer necessary. Clean up!
    public Date getExpectedDateOfCompletion() {
        if (currentJob != null) {
            return currentJob.getJobStatusAndTracking().getExpectedDateOfCompletion();
        } else {
            return null;
        }
    }

    public void setExpectedDateOfCompletion(Date date) {
        currentJob.getJobStatusAndTracking().setExpectedDateOfCompletion(date);
    }

    public Date getDateDocumentCollected() {
        if (currentJob != null) {
            if (currentJob.getJobStatusAndTracking().getDateDocumentCollected() != null) {
                return currentJob.getJobStatusAndTracking().getDateDocumentCollected();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setDateDocumentCollected(Date date) {
        currentJob.getJobStatusAndTracking().setDateDocumentCollected(date);
    }

    /**
     *
     * @return
     */
    public Boolean getCompleted() {
        if (currentJob != null) {
            return currentJob.getJobStatusAndTracking().getCompleted();
        } else {
            return false;
        }
    }

    public void setCompleted(Boolean b) {
        currentJob.getJobStatusAndTracking().setCompleted(b);
    }

    public Boolean getJobSaved() {
        return getCurrentJob().getId() != null;
    }

    public Boolean getSamplesCollected() {
        if (currentJob != null) {
            return currentJob.getJobStatusAndTracking().getSamplesCollected();
        } else {
            return false;
        }
    }

    public void setSamplesCollected(Boolean b) {
        currentJob.getJobStatusAndTracking().setSamplesCollected(b);
    }

    public Boolean getDocumentCollected() {
        if (currentJob != null) {
            return currentJob.getJobStatusAndTracking().getDocumentCollected();
        } else {
            return false;
        }
    }

    public void setDocumentCollected(Boolean b) {
        currentJob.getJobStatusAndTracking().setDocumentCollected(b);
    }

    @Override
    public EntityManager getEntityManager2() {

        return getSystemManager().getEntityManager2();
    }

    public void updateJobCategory() {
        setIsDirty(true);
    }

    public void updateJobSubCategory() {
        setIsDirty(true);
    }

    public void updateJob(AjaxBehaviorEvent event) {
        setIsDirty(true);
    }

    public void updateStartDate(AjaxBehaviorEvent event) {
        if ((getCurrentJob().getJobStatusAndTracking().getStartDate() != null)
                && getCurrentJob().getJobStatusAndTracking().getWorkProgress().equals("Not started")) {
            getCurrentJob().getJobStatusAndTracking().setWorkProgress("Ongoing");
        }

        setIsDirty(true);
    }

    public void updateJobView(AjaxBehaviorEvent event) {
        getUser().save(getSystemManager().getEntityManager1());
    }

    public void updateJobClassification() {

        // Setup default tax
        if (currentJob.getClassification().getDefaultTax().getId() != null) {
            currentJob.getJobCostingAndPayment().setTax(currentJob.getClassification().getDefaultTax());
        }

        // Get the clasification saved for use in setting taxes
        // Update all costs that depend on tax
        if (currentJob.getId() != null) {
            getJobFinanceManager().updateAllTaxes(null);
        }

        setIsDirty(true);
    }

    public void updateTestsAndCalibration() {

        currentJob.setNoOfTestsOrCalibrations(currentJob.getNoOfTests() + currentJob.getNoOfCalibrations());

        setIsDirty(true);
    }

    public void update() {
        setIsDirty(true);
    }

    public void updateDocumentsCollectedBy() {

        if (!currentJob.getJobStatusAndTracking().getDocumentCollected()) {
            currentJob.getJobStatusAndTracking().setDocumentCollectedBy("");
            setDateDocumentCollected(null);
        } else {
            setDateDocumentCollected(new Date());
        }

        setIsDirty(true);
    }

    public void updateJobCompleted() {
        if (getCompleted()) {
            currentJob.getJobStatusAndTracking().setWorkProgress("Completed");
            setJobCompletionDate(new Date());
        } else {
            currentJob.getJobStatusAndTracking().setWorkProgress("Not started");
            setJobCompletionDate(null);
        }
        setIsDirty(true);
    }

    public void updateSamplesCollectedBy() {

        if (!currentJob.getJobStatusAndTracking().getSamplesCollected()) {
            currentJob.getJobStatusAndTracking().setSamplesCollectedBy("");
            currentJob.getJobStatusAndTracking().setDateSamplesCollected(null);
        } else {
            currentJob.getJobStatusAndTracking().setDateSamplesCollected(new Date());
        }

        setIsDirty(true);
    }

    public void updateJobReportNumber() {
        setIsDirty(true);
    }

    public void updateAutoGenerateJobNumber() {

        if (currentJob.getAutoGenerateJobNumber()) {
            currentJob.setJobNumber(getCurrentJobNumber());
        }

        setIsDirty(true);

    }

    public void updateNewClient() {
        setIsDirty(true);
    }

    public void updateSamplesCollected() {
        setIsDirty(true);
    }

    public Boolean checkWorkProgressReadinessToBeChanged() {
        return checkJobWorkProgressReadinessToBeChanged(getCurrentJob());
    }

    public void updateWorkProgress() {
        updateJobWorkProgress(getCurrentJob());
    }

    public Boolean checkJobWorkProgressReadinessToBeChanged(Job job) {
        EntityManager em = getEntityManager1();

        // Find the currently stored job and check it's work status
        if (job.getId() != null) {
            Job savedJob = Job.findJobById(em, job.getId());

            // Do not allow flagging job as completed unless job costing is approved
            if (!job.getJobCostingAndPayment().getCostingApproved()
                    && job.getJobStatusAndTracking().getWorkProgress().equals("Completed")) {

                PrimeFacesUtils.addMessage(job.getType()
                        + " Work Progress Cannot Be As Marked Completed",
                        "The " + job.getType()
                        + " costing needs to be approved before this job can be marked as completed.",
                        FacesMessage.SEVERITY_WARN);

                return false;
            }

            if (savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && !getUser().isUserDepartmentSupervisor(job, em)) {

                // Reset current job to its saved work progress
                job.getJobStatusAndTracking().
                        setWorkProgress(savedJob.getJobStatusAndTracking().getWorkProgress());

                PrimeFacesUtils.addMessage(job.getType() + " Work Progress Cannot Be Changed",
                        "\"This " + job.getType() + " is marked as completed and cannot be changed. You may contact your supervisor.",
                        FacesMessage.SEVERITY_WARN);

                return false;
            } else if (savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && (getUser().isUserDepartmentSupervisor(job, em))) {
                // System admin can change work status even if it's completed.
                return true;
            } else if (!savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && job.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && !job.getJobCostingAndPayment().getCostingCompleted()) {

                // Reset current job to its saved work progress
                job.getJobStatusAndTracking().
                        setWorkProgress(savedJob.getJobStatusAndTracking().getWorkProgress());

                PrimeFacesUtils.addMessage(job.getType() + " Work Progress Cannot Be As Marked Completed",
                        "The " + job.getType() + " costing needs to be prepared before this "
                        + job.getType()
                        + " can be marked as completed.",
                        FacesMessage.SEVERITY_WARN);

                return false;

            }
        } else {

            PrimeFacesUtils.addMessage(job.getType() + " Work Progress Cannot be Changed",
                    "This " + job.getType() + "'s work progress cannot be changed until the "
                    + job.getType() + " is saved.",
                    FacesMessage.SEVERITY_WARN);
            return false;
        }

        return true;
    }

    public void updateJobWorkProgress(Job job) {

        if (checkJobWorkProgressReadinessToBeChanged(job)) {
            if (!job.getJobStatusAndTracking().getWorkProgress().equals("Completed")) {
                job.getJobStatusAndTracking().setCompleted(false);
                job.getJobStatusAndTracking().setSamplesCollected(false);
                job.getJobStatusAndTracking().setDocumentCollected(false);
                // overall job completion
                job.getJobStatusAndTracking().setDateOfCompletion(null);
                job.getJobStatusAndTracking().
                        setCompletedBy(null);
                // sample collection
                job.getJobStatusAndTracking().setSamplesCollectedBy(null);
                job.getJobStatusAndTracking().setDateSamplesCollected(null);
                // document collection
                job.getJobStatusAndTracking().setDocumentCollectedBy(null);
                job.getJobStatusAndTracking().setDateDocumentCollected(null);

                // Update start date
                if (job.getJobStatusAndTracking().getWorkProgress().equals("Ongoing")
                        && job.getJobStatusAndTracking().getStartDate() == null) {
                    job.getJobStatusAndTracking().setStartDate(new Date());
                } else if (job.getJobStatusAndTracking().getWorkProgress().equals("Not started")) {
                    job.getJobStatusAndTracking().setStartDate(null);
                }

            } else {
                job.getJobStatusAndTracking().setCompleted(true);
                job.getJobStatusAndTracking().setDateOfCompletion(new Date());
                job.getJobStatusAndTracking().
                        setCompletedBy(getUser().getEmployee());
            }

            setIsDirty(true);
        } else {
            if (job.getId() != null) {
                // Reset work progress to the currently saved state
                Job job1 = Job.findJobById(getEntityManager1(), job.getId());
                if (job1 != null) {
                    job.getJobStatusAndTracking().setWorkProgress(job1.getJobStatusAndTracking().getWorkProgress());
                } else {
                    job.getJobStatusAndTracking().setWorkProgress("Not started");
                }
            } else {
                job.getJobStatusAndTracking().setWorkProgress("Not started");
            }
        }

    }

    public void resetCurrentJob() {
        EntityManager em = getEntityManager1();

        createJob(em, false, false);
    }

    public Boolean createJob(EntityManager em, Boolean isSubcontract, Boolean copyCosting) {

        try {
            if (isSubcontract) {

                // Save current job as parent job for use in the subcontract
                Job parent = currentJob;
                // Create copy of job and use current sequence number and year.                
                Long currentJobSequenceNumber = parent.getJobSequenceNumber();
                Integer yearReceived = parent.getYearReceived();
                currentJob = Job.copy(em, parent, getUser(), true, false);
                currentJob.setParent(parent);
                currentJob.setClassification(new Classification());
                currentJob.setClient(new Client());
                currentJob.setBillingAddress(new Address());
                currentJob.setContact(new Contact());
                currentJob.setAssignedTo(new Employee());
                currentJob.setRepresentatives(null);
                currentJob.setEstimatedTurnAroundTimeInDays(0);
                currentJob.setEstimatedTurnAroundTimeRequired(true);
                currentJob.setInstructions("");
                currentJob.setSector(Sector.findSectorByName(em, "--"));
                currentJob.setJobCategory(JobCategory.findJobCategoryByName(em, "--"));
                currentJob.setJobSubCategory(JobSubCategory.findJobSubCategoryByName(em, "--"));
                currentJob.setSubContractedDepartment(new Department());
                currentJob.setIsToBeSubcontracted(isSubcontract);
                currentJob.getJobStatusAndTracking().setDateAndTimeEntered(null);
                currentJob.setYearReceived(yearReceived);
                currentJob.setJobSequenceNumber(currentJobSequenceNumber);
                currentJob.setJobNumber(Job.generateJobNumber(currentJob, em));
                // Services
                currentJob.setServiceContract(new ServiceContract());
                currentJob.setServices(null);

                if (copyCosting) {
                    currentJob.getJobCostingAndPayment().setCostComponents(
                            getJobFinanceManager().copyCostComponents(parent.getJobCostingAndPayment().getCostComponents()));
                    currentJob.getJobCostingAndPayment().setIsDirty(true);
                }

            } else {
                currentJob = Job.create(em, getUser(), true);
            }
            if (currentJob == null) {
                PrimeFacesUtils.addMessage("Job NOT Created",
                        "An error occurred while creating a job. Try again or contact the System Administrator",
                        FacesMessage.SEVERITY_ERROR);
            } else {
                if (isSubcontract) {
                    setIsDirty(true);
                } else {
                    setIsDirty(false);
                }

                BusinessEntityActionUtils.addAction(BusinessEntity.Action.CREATE,
                        currentJob.getActions());
            }

            getJobFinanceManager().setAccPacCustomer(new AccPacCustomer(""));

        } catch (Exception e) {
            System.out.println(e);
        }

        return true;
    }

    /**
     *
     * @param serviceRequest
     */
    public void createNewJob(ServiceRequest serviceRequest) {
        // handle user privilege and return if the user does not have
        // the privilege to do what they wish
        EntityManager em = getEntityManager1();
        createJob(em, false, false);

        // fill in fields from service request
        currentJob.setBusinessOffice(serviceRequest.getBusinessOffice());
        currentJob.setJobSequenceNumber(serviceRequest.getServiceRequestSequenceNumber());
        currentJob.getClient().doCopy(serviceRequest.getClient());
        currentJob.setDepartment(serviceRequest.getDepartment());
        currentJob.setAssignedTo(serviceRequest.getAssignedTo());
        if (currentJob.getAutoGenerateJobNumber()) {
            currentJob.setJobNumber(Job.generateJobNumber(currentJob, em));
        }
        // set job dirty to ensure it is saved if attempt is made to close it
        //  before saving
        setIsDirty(true);
    }

    public void subContractJob(ActionEvent actionEvent) {
        EntityManager em = getEntityManager1();

        if (currentJob.getId() == null || currentJob.getIsDirty()) {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "This job must be saved before it can be subcontracted",
                    FacesMessage.SEVERITY_ERROR);
            return;
        } else if (currentJob.getIsSubContract()) {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "A subcontract cannot be subcontracted",
                    FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (createJob(em, true, false)) {
            PrimeFacesUtils.addMessage("Job Copied for Subcontract",
                    "The current job was copied but the copy was not saved. "
                    + "Please enter or change the details for the copied job as required for the subcontract",
                    FacesMessage.SEVERITY_INFO);
        } else {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "The subcontract was not created. Contact your System Administrator",
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    public void subContractJobWithCosting(ActionEvent actionEvent) {
        EntityManager em = getEntityManager1();

        if (currentJob.getId() == null || currentJob.getIsDirty()) {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "This job must be saved before it can be subcontracted",
                    FacesMessage.SEVERITY_ERROR);
            return;
        } else if (currentJob.getIsSubContract()) {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "A subcontract cannot be subcontracted",
                    FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (createJob(em, true, true)) {
            PrimeFacesUtils.addMessage("Job and Costing Copied for Subcontract",
                    "The current job and its costing was copied but the copy was not saved. "
                    + "Please enter or change the details for the copied job as required for the subcontract",
                    FacesMessage.SEVERITY_INFO);
        } else {
            PrimeFacesUtils.addMessage("Subcontract NOT Created",
                    "The subcontract was not created. Contact your System Administrator",
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    public void cancelClientEdit(ActionEvent actionEvent) {
        if (currentJob.getClient().getId() == null) {
            currentJob.getClient().setName("");
        }
    }

    public String getSearchResultsTableHeader() {
        return ReportUtils.getSearchResultsTableHeader(getDateSearchPeriod(), getJobSearchResultList());
    }

    public void cancelJobEdit(ActionEvent actionEvent) {
        setIsDirty(false);
        PrimeFacesUtils.closeDialog(null);
        //doJobSearch();
    }

    private boolean prepareAndSaveJob(Job job) {
        ReturnMessage returnMessage;

        returnMessage = job.prepareAndSave(getEntityManager1(), getUser());

        if (returnMessage.isSuccess()) {
//            if (job.getJobCostingAndPayment().getEstimate()) {
            PrimeFacesUtils.addMessage("Saved!", job.getType() + " was saved", FacesMessage.SEVERITY_INFO);
//            } else {
//                PrimeFacesUtils.addMessage("Saved!", "Job was saved", FacesMessage.SEVERITY_INFO);
//            }
            job.getJobStatusAndTracking().setEditStatus("        ");

            return true;
        } else {
            PrimeFacesUtils.addMessage(job.getType() + " NOT Saved!",
                    job.getType() + " was NOT saved. Please contact the System Administrator!: "
                    + returnMessage.getDetail(), // tk to be commented out.
                    FacesMessage.SEVERITY_ERROR);

            sendErrorEmail("An error occurred while saving a " + job.getType(),
                    job.getType() + " number: " + job.getJobNumber()
                    + "\nJMTS User: " + getUser().getUsername()
                    + "\nDate/time: " + new Date()
                    + "\nDetail: " + returnMessage.getDetail());
        }

        return false;
    }

    public void saveJob(Job job) {
        EntityManager em = getEntityManager1();
        Job savedJob;

        // Check if cost estimate exceeds credit limit
        if (isJobNew(job)) {
            if (job.getClient().getCreditLimit() > 0.0
                    && !job.getJobCostingAndPayment().getEstimate()
                    && job.getJobCostingAndPayment().getCalculatedCostEstimate() > 0.0) {
                if (job.getClient().getCreditLimit()
                        < job.getJobCostingAndPayment().getCalculatedCostEstimate()) {
                    PrimeFacesUtils.addMessage(
                            job.getType() + " Cannot Be Saved",
                            "This " + job.getType() + "'s cost estimate exceeds the client's credit limit.",
                            FacesMessage.SEVERITY_ERROR);

                    return;
                }
            }
        }

        // Do not save changed job if it's already marked as completed in the database
        // However, saving is allowed if the user belongs to the "Invoicing department"
        // or is a system administrator
        if (!isJobNew(job)) {
            savedJob = Job.findJobById(em, job.getId());
            if (savedJob.getJobStatusAndTracking().getWorkProgress().equals("Completed")
                    && !User.isUserDepartmentSupervisor(savedJob, getUser(), em)) {

                job.setIsDirty(false);

                PrimeFacesUtils.addMessage(
                        job.getType() + " Cannot Be Saved",
                        "This " + job.getType() + " is marked as completed so changes cannot be saved. You may contact your supervisor or a system administrator",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }
        }

        // Ensure that at least 1 service is selected
//        if (job.getServices().isEmpty()) {
//            PrimeFacesUtils.addMessage("Service(s) NOT Selected",
//                    "Please select at least one service",
//                    FacesMessage.SEVERITY_ERROR);
//
//            return;
//        }
        // Check if there exists another job/subcontract with the same job number.
        Job savedSubcontract = Job.findJobByJobNumber(getEntityManager1(), job.getJobNumber());
        if (savedSubcontract != null && isJobNew(job)
                && !savedSubcontract.getJobStatusAndTracking().getWorkProgress().equals("Cancelled")) {
            PrimeFacesUtils.addMessage("Job/Subcontract already exists!",
                    "This job/subcontract cannot be saved because another job/subcontract already exists with the same job number",
                    FacesMessage.SEVERITY_ERROR);

            return;
        }

        // Do privelege checks and save if possible
        // Check for job entry privileges
        if (isJobNew(job)
                && ((getUser().can("EnterDepartmentJob") // Use Department.findDepartmentAssignedToJob() instead?
                && (getUser().isMemberOf(em, job.getDepartment()) || getUser().isMemberOf(em, job.getSubContractedDepartment())))
                // Can the user assign a job to themself provided that the user belongs to the job's parent department?
                || (getUser().can("EnterOwnJob")
                && Objects.equals(getUser().getEmployee().getId(), job.getAssignedTo().getId()) // Use Department.findDepartmentAssignedToJob() instead?
                && (getUser().isMemberOf(em, job.getDepartment()) || getUser().isMemberOf(em, job.getSubContractedDepartment())))
                // Can the user enter any job?
                || getUser().can("EnterJob"))) {

            if (prepareAndSaveJob(job)) {
                processJobActions();
            }

        } else if (!isJobNew(job)) {
            savedJob = Job.findJobById(em, job.getId());
            // Check for job editing privileges
            if ((getUser().can("EditDepartmentJob") // Use Department.findDepartmentAssignedToJob() instead?
                    && (getUser().isMemberOf(em, savedJob.getDepartment()) || getUser().isMemberOf(em, savedJob.getSubContractedDepartment())))
                    // Can the user assign a job to themself provided that the user belongs to the job's parent department?
                    || (getUser().can("EditOwnJob")
                    && Objects.equals(getUser().getEmployee().getId(), savedJob.getAssignedTo().getId()) // Use Department.findDepartmentAssignedToJob() instead?
                    && (getUser().isMemberOf(em, savedJob.getDepartment()) || getUser().isMemberOf(em, savedJob.getSubContractedDepartment())))
                    // Can the user edit any job?
                    || getUser().can("EditJob")) {

                if (prepareAndSaveJob(job)) {
                    processJobActions();
                }

            } else {
                PrimeFacesUtils.addMessage("Insufficient Privilege",
                        "You do not have the privilege to enter/edit " + job.getType() + "s. \n"
                        + "Please contact the System Administrator for assistance.",
                        FacesMessage.SEVERITY_ERROR);
            }
        } else {
            PrimeFacesUtils.addMessage("Insufficient Privilege",
                    "You do not have the privilege to enter/edit " + job.getType() + "s. \n"
                    + "Please contact the System Administrator for assistance.",
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    public void saveCurrentJob() {

        saveJob(getCurrentJob());
    }

    public Boolean getIsClientNameValid() {

        return BusinessEntityUtils.validateText(currentJob.getClient().getName());

    }

    public Boolean getIsBillingAddressNameValid() {
        return BusinessEntityUtils.validateText(currentJob.getBillingAddress().getName());
    }

    /**
     * NB: Message body and subject are to be obtained from a "template". The
     * variables in the template are to be inserted where {variable} appears.
     *
     * @param job
     * @return
     */
    public String getUpdatedJobEmailMessage(Job job) {
        EntityManager em = getSystemManager().getEntityManager1();
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
        message = message + "<span style='font-weight:bold'>Current assignee: </span>" + BusinessEntityUtils.getPersonFullName(job.getAssignedTo(), false) + "<br>";
        message = message + "<span style='font-weight:bold'>Updated by: </span>" + BusinessEntityUtils.getPersonFullName(job.getJobStatusAndTracking().getEditedBy(), false) + "<br>";
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

            Notification notification = Notification.findFirstNotificationByOwnerId(em, currentJob.getId());
            if (notification == null) { // This seems to be a new job
                notification = new Notification(currentJob.getId(), new Date(), "Job entered");
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

            Notification notification = Notification.findFirstNotificationByOwnerId(em, currentJob.getId());
            if (notification == null) { // This seems to be a new job
                notification = new Notification(currentJob.getId(), new Date(), "Job saved");
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
            EntityManager em = getSystemManager().getEntityManager1();

            // send error message to developer's email            
            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    SystemOption.getString(em, "softwareDeveloperEmailAddress"),
                    subject, message,
                    "text/plain", em);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void editJobServiceContractDialog() {

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

        PrimeFaces.current().dialog().openDynamic("/job/jobServiceContractDialog", options, null);

    }

    public String getJobAssignee() {
        if (currentJob.getAssignedTo() != null) {
            return currentJob.getAssignedTo().getLastName() + ", " + currentJob.getAssignedTo().getFirstName();
        } else {
            return "";
        }
    }

    public String getCurrentJobNumber() {
        return Job.generateJobNumber(currentJob, getEntityManager1());
    }

    public Date getJobSubmissionDate() {
        if (currentJob != null) {
            if (currentJob.getJobStatusAndTracking().getDateSubmitted() != null) {
                return currentJob.getJobStatusAndTracking().getDateSubmitted();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setJobSubmissionDate(Date date) {
        currentJob.getJobStatusAndTracking().setDateSubmitted(date);
    }

    public void updateDateSubmitted() {

        if (currentJob.getAutoGenerateJobNumber()) {
            currentJob.setJobNumber(Job.generateJobNumber(currentJob, getEntityManager1()));
        }

        if (currentJob.getId() != null) {
            getJobFinanceManager().updateAllTaxes(null);
        }

        setIsDirty(true);
    }

    public void updateDateJobCompleted(SelectEvent event) {
        Date selectedDate = (Date) event.getObject();

        currentJob.getJobStatusAndTracking().setDateOfCompletion(selectedDate);

        setIsDirty(true);
    }

    public void updateDateExpectedCompletionDate(SelectEvent event) {
        Date selectedDate = (Date) event.getObject();

        currentJob.getJobStatusAndTracking().setExpectedDateOfCompletion(selectedDate);

        setIsDirty(true);
    }

    public List<Address> getCurrentJobClientAddresses() {

        return getCurrentJob().getClient().getAddresses();
    }

    public List<Address> completeClientAddress(String query) {
        List<Address> addresses = new ArrayList<>();

        try {

            for (Address address : getCurrentJob().getClient().getAddresses()) {
                if (address.toString().toUpperCase().contains(query.toUpperCase())) {
                    addresses.add(address);
                }
            }

            return addresses;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Contact> getCurrentJobClientContacts() {

        return getCurrentJob().getClient().getContacts();
    }

    public List<Contact> completeClientContact(String query) {
        List<Contact> contacts = new ArrayList<>();

        try {

            for (Contact contact : getCurrentJob().getClient().getContacts()) {
                if (contact.toString().toUpperCase().contains(query.toUpperCase())) {
                    contacts.add(contact);
                }
            }

            return contacts;
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Job> findJobs(Integer maxResults) {
        return Job.findJobsByDateSearchField(getEntityManager1(),
                getUser(),
                getDateSearchPeriod(),
                getSearchType(),
                getSearchText(),
                maxResults, false);
    }

    public void doDefaultSearch() {
        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                getSearchType(),
                getSearchText(),
                getDateSearchPeriod().getStartDate(),
                getDateSearchPeriod().getEndDate());
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
            case "General":
            case "My jobs":
            case "My department's jobs":
            case "Parent jobs only":
            case "Unapproved job costings":
            case "Appr'd & uninv'd jobs":
            case "Incomplete jobs":
            case "Invoiced jobs":
                search();
                break;
            case "My dept's proforma invoices":
                getJobFinanceManager().openProformaInvoicesTab();
                getJobFinanceManager().doJobSearch();
                break;
            default:
                break;
        }

    }

    public void search() {

        doJobSearch();

    }

    public void doJobSearch() {

        if (getUser().getId() != null) {
            jobSearchResultList = findJobs(0);
        } else {
            jobSearchResultList = new ArrayList<>();
        }

    }

    // tk del?
    public void doJobSearch(Integer maxResults) {

        if (getUser().getId() != null) {
            jobSearchResultList = findJobs(maxResults);

        } else {
            jobSearchResultList = new ArrayList<>();
        }

    }

    // tk del?
    public void doJobSearch(DatePeriod dateSearchPeriod, String searchType, String searchText) {

        doJobSearch();
    }

    /**
     *
     * @return
     */
//    public List<Classification> getActiveClassifications() {
//        EntityManager em = getEntityManager1();
//
//        List<Classification> classifications = Classification.findAllActiveClassifications(em);
//
//        return classifications;
//    }
//
//    public List<Sector> getActiveSectors() {
//        EntityManager em = getEntityManager1();
//
//        List<Sector> sectors = Sector.findAllActiveSectors(em);
//
//        return sectors;
//    }
    public List<Address> getClientAddresses() {

        List<Address> addresses = getCurrentJob().getClient().getAddresses();

        return addresses;
    }

//    public List<JobCategory> getActiveJobCategories() {
//        EntityManager em = getEntityManager1();
//
//        List<JobCategory> categories = JobCategory.findAllActiveJobCategories(em);
//
//        return categories;
//    }
//
//    public List<JobSubCategory> getActiveJobSubCategories() {
//        EntityManager em = getEntityManager1();
//
//        List<JobSubCategory> subCategories = JobSubCategory.findAllActiveJobSubCategories(em);
//
//        return subCategories;
//    }
    public List<Job> getJobSearchResultList() {
        return jobSearchResultList;
    }

    public Job getCurrentJob() {
        if (currentJob == null) {
            resetCurrentJob();
        }
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public void editJob() {

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

        PrimeFaces.current().dialog().openDynamic("/job/jobDialog", options, null);

    }

    public Job getSavedCurrentJob(Job currentJob) {
        int i = 0;
        Job foundJob = Job.findJobById(getEntityManager1(), currentJob.getId());
        for (Job job : jobSearchResultList) {
            if (Objects.equals(job.getId(), foundJob.getId())) {
                jobSearchResultList.set(i, foundJob);
                break;
            }
            ++i;
        }

        return foundJob;
    }

    public void setEditCurrentJob(Job currentJob) {

        this.currentJob = getSavedCurrentJob(currentJob);
        this.currentJob.setVisited(true);
        this.currentJob.getJobStatusAndTracking().setEditStatus("        ");
        getJobFinanceManager().setEnableOnlyPaymentEditing(false);
    }

    public void copyCurrentJob() {

        EntityManager em = getEntityManager1();

        // Do not allow copying of suhcontracts
        if (currentJob.getIsSubContract()) {

            PrimeFacesUtils.addMessage("Job Copy NOT Created",
                    "A subcontract cannot be copied",
                    FacesMessage.SEVERITY_ERROR);
        } else if (currentJob.getId() == null || currentJob.getIsDirty()) {
            PrimeFacesUtils.addMessage("Job Copy NOT Created",
                    "The current job must be saved before it can be copied",
                    FacesMessage.SEVERITY_ERROR);
        } else {

            currentJob = Job.copy(em, currentJob, getUser(), true, false);
            BusinessEntityActionUtils.addAction(BusinessEntity.Action.CREATE,
                    currentJob.getActions());
            getJobFinanceManager().setEnableOnlyPaymentEditing(false);

            PrimeFacesUtils.addMessage("Job Copied",
                    "The current job was copied but the copy was not saved. "
                    + "Please enter or change the details for the copied job as required",
                    FacesMessage.SEVERITY_INFO);
        }

    }

    @Override
    public void setIsDirty(Boolean dirty) {
        setIsJobDirty(getCurrentJob(), dirty);
    }

    @Override
    public Boolean getIsDirty() {
        return getIsJobDirty(getCurrentJob());
    }

    public void setIsJobDirty(Job job, Boolean dirty) {
        job.setIsDirty(dirty);
        if (dirty) {
            job.getJobStatusAndTracking().setEditStatus("(edited)");
        } else {
            job.getJobStatusAndTracking().setEditStatus("        ");
        }
    }

    public Boolean getIsJobDirty(Job job) {
        return job.getIsDirty();
    }

    public void updateSector() {
        setIsDirty(true);
    }

    public void updateJobSearch() {
        getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:jobSearchButton");
    }

    public void updateBillingAddress() {
        setIsDirty(true);
    }

    public void updateDepartment() {

        try {

            if (currentJob.getAutoGenerateJobNumber()) {
                currentJob.setJobNumber(getCurrentJobNumber());
            }

            if (currentJob.getId() != null) {
                getJobFinanceManager().updateAllTaxes(null);
            }

            setIsDirty(true);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void updateSubContractedDepartment() {

        try {

            if (currentJob.getAutoGenerateJobNumber()) {
                currentJob.setJobNumber(getCurrentJobNumber());
            }

            if (currentJob.getId() != null) {
                getJobFinanceManager().updateAllTaxes(null);
            }

            setIsDirty(true);

        } catch (Exception e) {
            System.out.println(e + ": updateSubContractedDepartment");
        }
    }

    /**
     * Do update for the client field on the General tab on the Job Details form
     */
    public void updateJobEntryTabClient() {

        getJobFinanceManager().getAccPacCustomer().setCustomerName(currentJob.getClient().getName());
        if (useAccPacCustomerList) {
            getJobFinanceManager().updateCreditStatus(null);
        }

        currentJob.setBillingAddress(new Address());
        currentJob.setContact(new Contact());

        // Set default tax
        if (currentJob.getClient().getDefaultTax().getId() != null) {
            currentJob.getJobCostingAndPayment().setTax(currentJob.getClient().getDefaultTax());
        }

        // Set default discount
        if (currentJob.getClient().getDiscount().getId() != null) {
            currentJob.getJobCostingAndPayment().setDiscount(currentJob.getClient().getDiscount());
        }

        setIsDirty(true);
    }

    public Job getSelectedJob() {
        if (selectedJob == null) {
            selectedJob = new Job();
            selectedJob.setJobNumber("");
        }
        return selectedJob;
    }

    public void setSelectedJob(Job selectedJob) {
        this.selectedJob = selectedJob;
    }

    public void createNewJobClient() {
        getClientManager().createNewClient(true);
        getClientManager().setClientDialogTitle("Client Detail");

        getClientManager().editSelectedClient();

    }

    public void editJobClient() {
        getClientManager().setSelectedClient(getCurrentJob().getClient());
        getClientManager().setClientDialogTitle("Client Detail");

        getClientManager().editSelectedClient();

    }

    public ServiceRequest createNewServiceRequest(EntityManager em,
            User user,
            Boolean autoGenerateServiceRequestNumber) {

        ServiceRequest sr = new ServiceRequest();
        sr.setClient(new Client("", false));
        sr.setServiceRequestNumber("");
        sr.setJobDescription("");
        sr.setBusinessOffice(BusinessOffice.findDefaultBusinessOffice(em, "Head Office"));
        sr.setClassification(Classification.findClassificationByName(em, "--"));
        sr.setSector(Sector.findSectorByName(em, "--"));
        sr.setJobCategory(JobCategory.findJobCategoryByName(em, "--"));
        sr.setJobSubCategory(JobSubCategory.findJobSubCategoryByName(em, "--"));
        sr.setServiceContract(new ServiceContract());
        sr.setAutoGenerateServiceRequestNumber(autoGenerateServiceRequestNumber);
        sr.setDateSubmitted(new Date());

        return sr;
    }

//    public User createNewUser(EntityManager em) {
//        User jmuser = new User();
//
//        jmuser.setEmployee(Employee.findDefault(em, "--", "--", true));
//
//        return jmuser;
//    }
//    public EntityManagerFactory setupDatabaseConnection(String PU) {
//        try {
//            EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU);
//            if (emf.isOpen()) {
//                return emf;
//            } else {
//                return null;
//            }
//        } catch (Exception ex) {
//            System.out.println(PU + " Connection failed: " + ex);
//            return null;
//        }
//    }
//    public HashMap<String, String> getConnectionProperties(
//            String url,
//            String driver,
//            String username,
//            String password) {
//
//        // Setup new database connection properties
//        HashMap<String, String> prop = new HashMap<>();
//        prop.put("javax.persistence.jdbc.user", username);
//        prop.put("javax.persistence.jdbc.password", password);
//        prop.put("javax.persistence.jdbc.url", url);
//        prop.put("javax.persistence.jdbc.driver", driver);
//
//        return prop;
//    }
    public Date getCurrentDate() {
        return new Date();
    }

    public Long getJobCountByQuery(EntityManager em, String query) {
        try {
            return (Long) em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            System.out.println(e);
            return 0L;
        }
    }

    public Long saveServiceContract(EntityManager em, ServiceContract serviceContract) {
        return BusinessEntityUtils.saveBusinessEntity(em, serviceContract);
    }

    public void postJobManagerMailToUser(
            Session mailSession,
            User user,
            String subject,
            String message) throws Exception {

        boolean debug = false;
        Message msg;
        EntityManager em = getSystemManager().getEntityManager1();

        if (mailSession == null) {
            //Set the host smtp address
            Properties props = new Properties();
            String mailServer = (String) SystemOption.getOptionValueObject(em, "mail.smtp.host");
            props.put("mail.smtp.host", mailServer);

            // create some properties and get the default Session
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(debug);
            msg = new MimeMessage(session);
        } else {
            msg = new MimeMessage(mailSession);
        }

        // set the from and to address
        String email = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailAddress");
        String name = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailName");
        InternetAddress addressFrom = new InternetAddress(email, name); // option job manager email addres
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[1];
        if (user != null) {
            addressTo[0] = new InternetAddress(user.getUsername(), user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
        } else {
            String email1 = (String) SystemOption.getOptionValueObject(em, "administratorEmailAddress");
            String name1 = (String) SystemOption.getOptionValueObject(em, "administratorEmailName");
            addressTo[0] = new InternetAddress(email1, name1);
        }

        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }

    public void postJobManagerMail(
            Session mailSession,
            String addressedTo,
            String fullNameOfAddressedTo,
            String subject,
            String message) throws Exception {

        boolean debug = false;
        Message msg;
        EntityManager em = getSystemManager().getEntityManager1();

        try {
            if (mailSession == null) {
                //Set the host smtp address
                Properties props = new Properties();
                String mailServer = (String) SystemOption.getOptionValueObject(em, "mail.smtp.host");
                String trust = (String) SystemOption.getOptionValueObject(em, "mail.smtp.ssl.trust");
                props.put("mail.smtp.host", mailServer);
                props.setProperty("mail.smtp.ssl.trust", trust);

                // create some properties and get the default Session
                Session session = Session.getDefaultInstance(props, null);
                session.setDebug(debug);
                msg = new MimeMessage(session);
            } else {
                msg = new MimeMessage(mailSession);
            }

            // set the from and to address
            String email = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailAddress");
            String name = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailName");
            InternetAddress addressFrom = new InternetAddress(email, name);
            msg.setFrom(addressFrom);

            InternetAddress[] addressTo = new InternetAddress[1];

            addressTo[0] = new InternetAddress(addressedTo, fullNameOfAddressedTo);

            msg.setRecipients(Message.RecipientType.TO, addressTo);

            // Setting the Subject and Content Type
            msg.setSubject(subject);
            msg.setContent(message, "text/html; charset=utf-8");

            Transport.send(msg);
        } catch (UnsupportedEncodingException | MessagingException e) {
            System.out.println(e);
        }
    }

    public JobDataModel getJobsModel() {
        return new JobDataModel(jobSearchResultList);
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
            System.out.println(e + ": getDisableSubContracting");
        }

        return false;
    }

    public Boolean isJobNew(Job job) {
        return (job.getId() == null);
    }

    public Boolean isCurrentJobNew() {
        return (getCurrentJob().getId() == null);
    }

    public void openClientsTab() {

        getMainTabView().openTab("Clients");
    }

    public void openReportsTab() {
        getMainTabView().openTab("Reports");
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Job Search Types");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getGroupedSearchTypes() {
        ArrayList<SelectItem> groupedSearchTypes = new ArrayList<>();

        groupedSearchTypes.add(getSearchTypesGroup());

        return groupedSearchTypes;
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {

        return getAuthorizedSearchTypes();
    }

    @Override
    public String getApplicationSubheader() {

        return SystemOption.getString(getSystemManager().getEntityManager1(), "JMTSTagLine");
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

    @Override
    public String getSearchType() {
        return searchType;
    }

    @Override
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public ArrayList<SelectItem> getDateSearchFields() {
        return getDateSearchFields(getSearchType());
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "General":
            case "My jobs":
            case "My department's jobs":
            case "Parent jobs only":
            case "Unapproved job costings":
            case "Appr'd & uninv'd jobs":
            case "Incomplete jobs":
            case "Invoiced jobs":
                dateSearchFields = DateUtils.getDateSearchFields();
                break;
            case "My dept's proforma invoices":
                dateSearchFields.add(new SelectItem("dateAndTimeEntered", "Date entered"));
                break;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void handleSelectedNotification(Notification notification) {
        switch (notification.getType()) {
            case "JobSearch":

                try {

            } catch (NumberFormatException e) {
                System.out.println(e);
            }

            break;

            default:
                System.out.println("Unkown type");
        }
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    private void openModuleMainTab(String moduleName) {

        if (moduleName != null) {
            switch (moduleName) {
                case "complianceManager":
                    getComplianceManager().openSurveysBrowser();
                    break;
                case "jobManager":
                    // tk remove after testing
//                    getJobFinanceManager().openProformaInvoicesTab();
//                    getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:proformaSearchButton");

                    openJobBrowser();
                    getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:jobSearchButton");

                    break;
                case "clientManager":
                    getClientManager().openClientsTab();
                    break;
                case "purchasingManager":
                    getPurchasingManager().openPurchaseReqsTab();
                    break;
                case "inventoryManager":
                    getInventoryManager().openInventoryProductBrowser();
                    getInventoryManager().openInventoryTab();
                    getInventoryManager().openInventoryRequisitionTab();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void initMainTabView() {

        String firstModule;
        firstModule = null;

        getMainTabView().reset(getUser());
//
//        if (getUser().hasModule("purchasingManager")) {
//            getFinanceManager().openDashboardTab();
//        }
        // Compliance
//        if (getUser().hasModule("complianceManager")) {
//            Module module = Module.findActiveModuleByName(
//                    getSystemManager().getEntityManager1(),
//                    "complianceManager");
//            if (module != null) {
//                openModuleMainTab("complianceManager");
//
//                if (firstModule == null) {
//                    firstModule = "complianceManager";
//                }
//
//            }
//        }

        // Proformas | Jobs
        if (getUser().hasModule("jobManager")) {
            Module module = Module.findActiveModuleByName(
                    getSystemManager().getEntityManager1(),
                    "jobManager");
            if (module != null) {
                openModuleMainTab("jobManager");

                if (firstModule == null) {
                    firstModule = "jobManager";
                }
            }
        }
        // Clients
//        if (getUser().hasModule("clientManager")) {
//            Module module = Module.findActiveModuleByName(
//                    getSystemManager().getEntityManager1(),
//                    "clientManager");
//            if (module != null) {
//                openModuleMainTab("clientManager");
//
//                if (firstModule == null) {
//                    firstModule = "clientManager";
//                }
//            }
//        }
        // Procurement
        if (getUser().hasModule("purchasingManager")) {
            Module module = Module.findActiveModuleByName(
                    getSystemManager().getEntityManager1(),
                    "purchasingManager");
            if (module != null) {
                openModuleMainTab("purchasingManager");

                if (firstModule == null) {
                    firstModule = "purchasingManager";
                }
            }
        }

        // Inventory
        if (getUser().hasModule("inventoryManager")) {
            Module module = Module.findActiveModuleByName(
                    getSystemManager().getEntityManager1(),
                    "inventoryManager");
            if (module != null) {
                openModuleMainTab("inventoryManager");

                if (firstModule == null) {
                    firstModule = "inventoryManager";
                }
            }
        }

        openModuleMainTab(firstModule);
    }

    @Override
    public void handleKeepAlive() {

        super.updateUserActivity("JMTSv"
                + SystemOption.getString(getSystemManager().getEntityManager1(), "JMTSv"),
                "Logged in");

        if (getUser().getId() != null) {
            getUser().save(getSystemManager().getEntityManager1());
        }

        if ((Boolean) SystemOption.getOptionValueObject(getSystemManager().getEntityManager1(), "debugMode")) {
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
        }

        PrimeFaces.current().ajax().update(":appForm:notificationBadge");

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

        super.updateUserActivity("JMTSv"
                + SystemOption.getString(getSystemManager().getEntityManager1(), "JMTSv"),
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
            super.updateUserActivity("JMTSv"
                    + SystemOption.getString(getSystemManager().getEntityManager1(), "JMTSv"),
                    "Logged in");
            getUser().save(getSystemManager().getEntityManager1());
        }

        setManagerUser();

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initMainTabView();

    }

    @Override
    public void setManagerUser() {

        getManager("systemManager").setUser(getUser());
        getManager("clientManager").setUser(getUser());
        getManager("reportManager").setUser(getUser());
        getManager("financeManager").setUser(getUser());
        getManager("humanResourceManager").setUser(getUser());
        getManager("complianceManager").setUser(getUser());

    }

}
