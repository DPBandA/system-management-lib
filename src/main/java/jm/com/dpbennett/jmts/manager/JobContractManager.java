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
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.jmts.JobSample;
import jm.com.dpbennett.business.entity.fm.Service;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.jmts.Job;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import jm.com.dpbennett.business.entity.gm.BusinessEntityManagement;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import static jm.com.dpbennett.business.entity.util.NumberUtils.formatAsCurrency;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.ReportUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;

/**
 * This class manages the functions and operations pertaining to a job contract.
 *
 * @author Desmond Bennett
 */
public class JobContractManager extends GeneralManager
        implements Serializable, BusinessEntityManagement {

    private Integer longProcessProgress;
    private JobManager jobManager;
    private SystemManager systemManager;

    public JobContractManager() {
        init();
    }

    public void okJobServiceContract(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelJobServiceContractEdit(ActionEvent actionEvent) {

        getCurrentJob().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void jobServiceContractDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    getJobManager().processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Service Contract"
                            + " Saved", "This job"
                            + " and the service contract were saved", FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Service Contract"
                            + " NOT Saved", "This job"
                            + " and the service contract were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public void editJobServiceContract() {

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

        PrimeFaces.current().dialog().openDynamic("/job/service/jobServiceContractDialog", options, null);

    }

    public void openJobServiceContractDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobServiceContract();

        } else {

            if (getJobManager().getCurrentJob().getIsDirty()) {
                getJobManager().saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobServiceContract();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before the service contract can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public StreamedContent getServiceContractStreamContentJRXML() {

        HashMap parameters = new HashMap();
        DateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy");

        EntityManager em;

        try {

            em = getEntityManager1();

            // Set parameters
            // Logo URL
            String logoURL = (String) SystemOption.getOptionValueObject(
                    getSystemManager().getEntityManager1(), 
                    "logoURL");
            parameters.put("logoURL", logoURL);
            // Job id
            parameters.put("jobId", getCurrentJob().getId());
            // Business office
            parameters.put("businessOffice", getCurrentJob().getBusinessOffice().getName());
            // Job entry agent
            parameters.put("jobEntryAgent", getCurrentJob().getJobStatusAndTracking().getEnteredBy().getName());
            // Job entry date
            parameters.put("jobEntryDate", dateFormatter.format(getCurrentJob().getJobStatusAndTracking().getDateAndTimeEntered()));
            // Parent department
            parameters.put("parentDepartment", getCurrentJob().getDepartment().getName());
            // Estimated turn around time
            parameters.put("estimatedTAT", getCurrentJob().getEstimatedTurnAroundTimeInDays().toString());
            // Estimated Sub Total
            parameters.put("estimatedSubTotal", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getEstimatedCost(), ""));
            // Estimated Tax (eg Tax)
            parameters.put("estimatedTax", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getEstimatedCost()
                    * getCurrentJob().getJobCostingAndPayment().getTax().getValue(), ""));
            // Estimated Total Cost
            parameters.put("estimatedTotalCost", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getCalculatedCostEstimate(), ""));
            // Minimum First Deposit
            parameters.put("minFirstDeposit", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getCalculatedMinDeposit(), ""));
            // Receipt #
            parameters.put("receiptNumber", getCurrentJob().getJobCostingAndPayment().getReceiptNumbers());
            // Total Paid
            parameters.put("totalPaid", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getTotalPayment(), ""));
            // Payment Breakdown
            // Calculate tax from payment
            Double payment = (100.0 * getCurrentJob().getJobCostingAndPayment().getTotalPayment())
                    / (getCurrentJob().getJobCostingAndPayment().getTax().getTaxValue() + 100.0);
            Double tax = getCurrentJob().getJobCostingAndPayment().getTotalPayment() - payment;
            // Tax
            parameters.put("paymentBreakdownTax", formatAsCurrency(tax, "$"));
            // Payment
            parameters.put("paymentBreakdownJob", formatAsCurrency(payment, "$"));
            // Date of Last Payment
            if (getCurrentJob().getJobCostingAndPayment().getLastPaymentDate() != null) {
                parameters.put("datePaid", dateFormatter.format(getCurrentJob().getJobCostingAndPayment().getLastPaymentDate()));
            }
            // Balance (amount due)
            if (getCurrentJob().getJobCostingAndPayment().getFinalCost() > 0.0) {
                parameters.put("balance", formatAsCurrency(getCurrentJob().getJobCostingAndPayment().getAmountDue(), "$"));
            } else {
                parameters.put("balance", formatAsCurrency(BusinessEntityUtils.roundTo2DecimalPlaces(getCurrentJob().getJobCostingAndPayment().getCalculatedCostEstimate())
                        - BusinessEntityUtils.roundTo2DecimalPlaces(getCurrentJob().getJobCostingAndPayment().getTotalPayment()), "$"));
            }
            // Payment Terms/Information
            if (!getCurrentJob().getJobCostingAndPayment().getAllPaymentTerms().trim().equals("")) {
                parameters.put("paymentTerms",
                        getCurrentJob().getJobCostingAndPayment().getAllPaymentTerms());
            } else {
                parameters.put("paymentTerms", "Not applicable");
            }
            // Agent/Cashier
            parameters.put("agentOrCashier", getCurrentJob().getJobCostingAndPayment().getLastPaymentEnteredBy().getName());
            // Client Name and Billing Address
            Address billingAddress = getCurrentJob().getBillingAddress();
            parameters.put("clientNameAndBillingAddress",
                    getCurrentJob().getClient().getName() + "\n"
                    + billingAddress.getAddressLine1() + "\n"
                    + billingAddress.getAddressLine2() + "\n"
                    + billingAddress.getStateOrProvince() + "\n"
                    + billingAddress.getCity());
            // Contact Person Name
            Contact contactPerson = getCurrentJob().getContact();
            parameters.put("contactPersonName", contactPerson.toString());
            // Email Address
            parameters.put("emailAddress", contactPerson.getInternet().getEmail1());
            // Contact Phone
            parameters.put("contactPhone", contactPerson.getMainPhoneNumber().toString());
            // Contact Fax
            parameters.put("contactFax", contactPerson.getMainFaxNumber().toString());

            // Type of Services Needed
            getCurrentJob().getServiceContract().setJob(getCurrentJob());
            String services
                    = getCurrentJob().getServiceContract().getSelectedServiceForContract().getName()
                    + " ";
            /*
            if (getCurrentJob().getServiceContract().getServiceRequestedTesting()) {
                services = services + "Testing";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedCalibration()) {
                services = services + "Calibration ";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedLabelEvaluation()) {
                services = services + "Label Evaluation ";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedInspection()) {
                services = services + "Inspection ";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedConsultancy()) {
                services = services + "Consultancy ";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedTraining()) {
                services = services + "Training ";
            }
            if (getCurrentJob().getServiceContract().getServiceRequestedOther()) {
                if ((getCurrentJob().getServiceContract().getServiceRequestedOtherText() != null)
                        && (!getCurrentJob().getServiceContract().getServiceRequestedOtherText().isEmpty())) {
                    services = services + " " + getCurrentJob().getServiceContract().getServiceRequestedOtherText();
                }
            }
             */
            parameters.put("typeOfServicesNeeded", services);

            // Fax/Email Report?
            if (getCurrentJob().getServiceContract().getAdditionalServiceFaxResults()) {
                parameters.put("emailReport", "Yes");
            } else {
                parameters.put("emailReport", "No");
            }
            // Expedite Job?
            if (getCurrentJob().getServiceContract().getAdditionalServiceUrgent()) {
                parameters.put("expediteJob", "Yes");
            } else {
                parameters.put("expediteJob", "No");
            }
            // Purchase Order
            parameters.put("purchaseOrder", getCurrentJob().getJobCostingAndPayment().getPurchaseOrderNumber());
            // Client Instruction/Details for Job
            parameters.put("clientInstructionForJob", getCurrentJob().getInstructions());
            // Additional Details for Sample(s)
            String details = "";
            if (getCurrentJob().getJobSamples().isEmpty()) {
                details = "Not applicable";
            } else {
                for (JobSample jobSample : getCurrentJob().getJobSamples()) {
                    if (!jobSample.getDescription().isEmpty()) {
                        details = details + " (" + jobSample.getReference() + ") "
                                + jobSample.getDescription();
                    }
                }
            }
            parameters.put("additionalSampleDetails", details);

            em.getTransaction().begin();
            
            Connection con = BusinessEntityUtils.getConnection(em);

            if (con != null) {
                try {
                    StreamedContent streamContent;
                    // Compile contract
                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(em, "serviceContractJRXML"));

                    // Generate contract
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    streamContent = DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("Service Contract - " + getCurrentJob().getJobNumber() + ".pdf")
                            .stream(() -> new ByteArrayInputStream(fileBytes))
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

    public SystemManager getSystemManager() {
        if (systemManager == null) {
            systemManager = BeanUtils.findBean("systemManager");
        }

        return systemManager;
    }

    public Boolean getUseServiceContractExcel() {
        return (Boolean) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(),
                "useServiceContractExcel");
    }

    public Boolean getUseServiceContractPDF() {
        return (Boolean) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(),
                "useServiceContractJRXML");
    }

//    public List<Service> completeService(String query) {
//
//        try {
//
//            return Service.findAllActiveByName(
//                    getEntityManager1(),
//                    query);
//
//        } catch (Exception e) {
//            System.out.println(e);
//
//            return new ArrayList<>();
//        }
//    }

    public JobManager getJobManager() {
        if (jobManager == null) {
            jobManager = BeanUtils.findBean("jobManager");
        }
        return jobManager;
    }

    public Job getCurrentJob() {
        return getJobManager().getCurrentJob();
    }

    @Override
    public final void init() {
        reset();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public User getUser() {
        return getJobManager().getUser();
    }

    @Override
    public EntityManager getEntityManager1() {
        return getJobManager().getEntityManager1();
    }

    public StreamedContent getServiceContractStreamContent() {
        EntityManager em;

        try {

            em = getSystemManager().getEntityManager1();

            String filePath
                    = (String) SystemOption.getOptionValueObject(em, "serviceContract");
            ByteArrayInputStream stream
                    = createServiceContractExcelFileInputStream(getUser(), filePath);

            DefaultStreamedContent dsc = DefaultStreamedContent.builder()
                    .contentType("application/xls")
                    .name("Service Contract - " + getCurrentJob().getJobNumber() + ".xls")
                    .stream(() -> stream)
                    .build();

            return dsc;

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

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

    public StreamedContent getServiceContractFile() {
        StreamedContent serviceContractStreamContent = null;

        try {

            serviceContractStreamContent = getServiceContractStreamContent();

        } catch (Exception e) {
            System.out.println(e);
        }

        return serviceContractStreamContent;
    }

    // service requested - calibration
    public Boolean getServiceRequestedCalibration() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedCalibration();
        } else {
            return false;
        }
    }

    public void setServiceRequestedCalibration(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedCalibration(b);
    }

    // service requested - label evaluation
    public Boolean getServiceRequestedLabelEvaluation() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedLabelEvaluation();
        } else {
            return false;
        }
    }

    public void setServiceRequestedLabelEvaluation(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedLabelEvaluation(b);
    }

    // service requested - inspection
    public Boolean getServiceRequestedInspection() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedInspection();
        } else {
            return false;
        }
    }

    public void setServiceRequestedInspection(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedInspection(b);
    }

    // service requested - consultancy
    public Boolean getServiceRequestedConsultancy() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedConsultancy();
        } else {
            return false;
        }
    }

    public void setServiceRequestedConsultancy(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedConsultancy(b);
    }

    // service requested - training
    public Boolean getServiceRequestedTraining() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedTraining();
        } else {
            return false;
        }
    }

    public void setServiceRequestedTraining(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedTraining(b);
    }

    // service requested - other
    public Boolean getServiceRequestedOther() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getServiceRequestedOther();
        } else {
            return false;
        }
    }

    public void setServiceRequestedOther(Boolean b) {
        getCurrentJob().getServiceContract().setServiceRequestedOther(b);
        if (!b) {
            getCurrentJob().getServiceContract().setServiceRequestedOtherText(null);
        }
    }

    //Intended Market
    //intended martket - local
    public Boolean getIntendedMarketLocal() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketLocal();
        } else {
            return false;
        }
    }

    public void setIntendedMarketLocal(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketLocal(b);
    }

    // intended martket - caricom
    public Boolean getIntendedMarketCaricom() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketCaricom();
        } else {
            return false;
        }
    }

    public void setIntendedMarketCaricom(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketCaricom(b);
    }

    // intended martket - UK
    public Boolean getIntendedMarketUK() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketUK();
        } else {
            return false;
        }
    }

    public void setIntendedMarketUK(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketUK(b);
    }

    // intended martket - USA
    public Boolean getIntendedMarketUSA() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketUSA();
        } else {
            return false;
        }
    }

    public void setIntendedMarketUSA(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketUSA(b);
    }

    // intended martket - Canada
    public Boolean getIntendedMarketCanada() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketCanada();
        } else {
            return false;
        }
    }

    public void setIntendedMarketCanada(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketCanada(b);
    }

    // intended martket - Other
    public Boolean getIntendedMarketOther() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getServiceContract().getIntendedMarketOther();
        } else {
            return false;
        }
    }

    public void setIntendedMarketOther(Boolean b) {
        getCurrentJob().getServiceContract().setIntendedMarketOther(b);
        if (!b) {
            getCurrentJob().getServiceContract().setIntendedMarketOtherText(null);
        }
    }

    /**
     *
     * @return
     */
    public Boolean getCompleted() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getJobStatusAndTracking().getCompleted();
        } else {
            return false;
        }
    }

    public void setCompleted(Boolean b) {
        getCurrentJob().getJobStatusAndTracking().setCompleted(b);
    }

    public Boolean getJobSaved() {
        return getCurrentJob().getId() != null;
    }

    public Boolean getSamplesCollected() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getJobStatusAndTracking().getSamplesCollected();
        } else {
            return false;
        }
    }

    public void setSamplesCollected(Boolean b) {
        getCurrentJob().getJobStatusAndTracking().setSamplesCollected(b);
    }

    public Boolean getDocumentCollected() {
        if (getCurrentJob() != null) {
            return getCurrentJob().getJobStatusAndTracking().getDocumentCollected();
        } else {
            return false;
        }
    }

    public void setDocumentCollected(Boolean b) {
        getCurrentJob().getJobStatusAndTracking().setDocumentCollected(b);
    }

    public void updateJob() {
        setIsDirty(true);
    }

    public void addService(String name) {
        addService(getCurrentJob(), name);

    }

//    public List<Service> getAllActiveServices() {
//
//        return Service.findAllActive(getEntityManager1());
//    }
    
    public FinanceManager getFinanceManager() {

        return BeanUtils.findBean("financeManager");
        
    }

    public void addService(Job job, String name) {
        Service service = Service.findActiveByExactName(
                getFinanceManager().getEntityManager1(),
                name);

        if (service != null) {
            // Attempt to remove the service to ensure that it's not already added
            removeService(job, name);

            job.getServices().add(service);
        }

    }

    private void removeService(Job job, String name) {
        for (int i = 0; i < job.getServices().size(); i++) {
            if (job.getServices().get(i).getName().equals(name)) {
                job.getServices().remove(i);
            }

        }
    }

    /**
     * Removes the specified service from the current job.
     *
     * @see #removeService(jm.com.dpbennett.business.entity.Job,
     * java.lang.String)
     * @param name
     */
    private void removeService(String name) {
        removeService(getCurrentJob(), name);
    }

    public void updateService(AjaxBehaviorEvent event) {

        enableAllServices(false);

        addService(getCurrentJob().getServiceContract().getSelectedService().getName());

        setIsDirty(true);

    }

    /**
     * Add or remove a service when a service check box is clicked.
     *
     * @see #addService(jm.com.dpbennett.business.entity.Job, java.lang.String)
     * @see #removeService(jm.com.dpbennett.business.entity.Job,
     * java.lang.String)
     * @param event
     */
    public void updateServices(AjaxBehaviorEvent event) {
        Boolean bUpdatedService;

        switch (event.getComponent().getId()) {
            case "testingService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedTesting();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedTesting(bUpdatedService);
                if (bUpdatedService) {
                    addService("Testing");
                } else {
                    removeService("Testing");
                }
                break;
            case "calibrationService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedCalibration();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedCalibration(bUpdatedService);
                if (bUpdatedService) {
                    addService("Calibration");
                } else {
                    removeService("Calibration");
                }
                break;
            case "labelEvaluationService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedLabelEvaluation();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedLabelEvaluation(bUpdatedService);
                if (bUpdatedService) {
                    addService("Label Evaluation");
                } else {
                    removeService("Label Evaluation");
                }
                break;
            case "inspectionService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedInspection();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedInspection(bUpdatedService);
                if (bUpdatedService) {
                    addService("Inspection");
                } else {
                    removeService("Inspection");
                }
                break;
            case "consultancyService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedConsultancy();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedConsultancy(bUpdatedService);
                if (bUpdatedService) {
                    addService("Consultancy");
                } else {
                    removeService("Consultancy");
                }
                break;
            case "trainingService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedTraining();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedTraining(bUpdatedService);
                if (bUpdatedService) {
                    addService("Training");
                } else {
                    removeService("Training");
                }
                break;
            case "serviceRequestedFoodInspectorate":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedFoodInspectorate();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedFoodInspectorate(bUpdatedService);
                if (bUpdatedService) {
                    addService("Food Inspectorate");
                } else {
                    removeService("Food Inspectorate");
                }
                break;
            case "serviceRequestedLegalMetrology":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedLegalMetrology();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedLegalMetrology(bUpdatedService);
                if (bUpdatedService) {
                    addService("Legal Metrology");
                } else {
                    removeService("Legal Metrology");
                }
                break;
            case "serviceRequestedSaleOfPublication":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedSaleOfPublication();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedSaleOfPublication(bUpdatedService);
                if (bUpdatedService) {
                    addService("Sale of Publication");
                } else {
                    removeService("Sale of Publication");
                }
                break;
            case "serviceRequestedStationeryOrPhotocopy":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedStationeryOrPhotocopy();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedStationeryOrPhotocopy(bUpdatedService);
                if (bUpdatedService) {
                    addService("Stationery or Photocopy");
                } else {
                    removeService("Stationery or Photocopy");
                }
                break;
            case "serviceRequestedCertification":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedCertification();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedCertification(bUpdatedService);
                if (bUpdatedService) {
                    addService("Certification");
                } else {
                    removeService("Certification");
                }
                break;
            case "serviceRequestedCertificationStandards":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedCertificationStandards();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedCertificationStandards(bUpdatedService);
                if (bUpdatedService) {
                    addService("Certification Mark Programme");
                } else {
                    removeService("Certification Mark Programme");
                }
                break;
            case "serviceRequestedDetentionRehabInspection":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedDetentionRehabInspection();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedDetentionRehabInspection(bUpdatedService);
                if (bUpdatedService) {
                    addService("Detention, Rehabilitation & Inspection");
                } else {
                    removeService("Detention, Rehabilitation & Inspection");
                }
                break;
            case "serviceRequestedFacilitiesManagement":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedFacilitiesManagement();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedFacilitiesManagement(bUpdatedService);
                if (bUpdatedService) {
                    addService("Facilities Management");
                } else {
                    removeService("Facilities Management");
                }
                break;
            case "serviceRequestedCementTesting":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedCementTesting();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedCementTesting(bUpdatedService);
                if (bUpdatedService) {
                    addService("Cement Testing");
                } else {
                    removeService("Cement Testing");
                }
                break;
            case "serviceRequestedPetrolSampling":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedPetrolSampling();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedPetrolSampling(bUpdatedService);
                if (bUpdatedService) {
                    addService("Petrol Sampling");
                } else {
                    removeService("Petrol Sampling");
                }
                break;
            case "otherService":
                bUpdatedService = getCurrentJob().getServiceContract().getServiceRequestedOther();
                enableAllServices(false);
                getCurrentJob().getServiceContract().setServiceRequestedOther(bUpdatedService);
                if (bUpdatedService) {
                    addService("Other");
                } else {
                    removeService("Other");
                }
                break;
        }

        setIsDirty(true);
    }

    public void addServices() {
        addServices(getCurrentJob());
    }

    public void addServices(Job job) {

        if (job.getServiceContract().getServiceRequestedTesting()) {
            addService(job, "Testing");
        }

        if (job.getServiceContract().getServiceRequestedCalibration()) {
            addService(job, "Calibration");
        }

        if (job.getServiceContract().getServiceRequestedLabelEvaluation()) {
            addService(job, "Label Evaluation");
        }

        if (job.getServiceContract().getServiceRequestedInspection()) {
            addService(job, "Inspection");
        }

        if (job.getServiceContract().getServiceRequestedConsultancy()) {
            addService(job, "Consultancy");
        }

        if (job.getServiceContract().getServiceRequestedTraining()) {
            addService(job, "Training");
        }

        if (job.getServiceContract().getServiceRequestedFoodInspectorate()) {
            addService(job, "Food Inspectorate");
        }

        if (job.getServiceContract().getServiceRequestedLegalMetrology()) {
            addService(job, "Legal Metrology");
        }

        if (job.getServiceContract().getServiceRequestedSaleOfPublication()) {
            addService(job, "Sale of Publication");
        }

        if (job.getServiceContract().getServiceRequestedStationeryOrPhotocopy()) {
            addService(job, "Stationery or Photocopy");
        }

        if (job.getServiceContract().getServiceRequestedCertification()) {
            addService(job, "Certification");
        }

        if (job.getServiceContract().getServiceRequestedCertificationStandards()) {
            addService(job, "Certification Mark Programme");
        }

        if (job.getServiceContract().getServiceRequestedDetentionRehabInspection()) {
            addService(job, "Detention, Rehabilitation & Inspection");
        }

        if (job.getServiceContract().getServiceRequestedFacilitiesManagement()) {
            addService(job, "Facilities Management");
        }

        if (job.getServiceContract().getServiceRequestedCementTesting()) {
            addService(job, "Cement Testing");
        }

        if (job.getServiceContract().getServiceRequestedPetrolSampling()) {
            addService(job, "Petrol Sampling");
        }

        if (job.getServiceContract().getServiceRequestedOther()) {
            addService(job, "Other");
        }

    }

    public void enableAllServices(Boolean b) {
        enableAllServices(getCurrentJob(), b);
    }

    public void enableAllServices(Job job, Boolean b) {

        job.getServiceContract().setServiceRequestedTesting(b);
        job.getServiceContract().setServiceRequestedCalibration(b);
        job.getServiceContract().setServiceRequestedLabelEvaluation(b);
        job.getServiceContract().setServiceRequestedInspection(b);
        job.getServiceContract().setServiceRequestedConsultancy(b);
        job.getServiceContract().setServiceRequestedTraining(b);
        job.getServiceContract().setServiceRequestedFoodInspectorate(b);
        job.getServiceContract().setServiceRequestedLegalMetrology(b);
        job.getServiceContract().setServiceRequestedSaleOfPublication(b);
        job.getServiceContract().setServiceRequestedStationeryOrPhotocopy(b);
        job.getServiceContract().setServiceRequestedCertification(b);
        job.getServiceContract().setServiceRequestedCertificationStandards(b);
        job.getServiceContract().setServiceRequestedDetentionRehabInspection(b);
        job.getServiceContract().setServiceRequestedFacilitiesManagement(b);
        job.getServiceContract().setServiceRequestedCementTesting(b);
        job.getServiceContract().setServiceRequestedPetrolSampling(b);
        job.getServiceContract().setServiceRequestedOther(b);

        // Add/remove all services
        if (b) {
            addServices(job);
        } else {
            job.getServices().clear();
        }

    }

    @Override
    public void setIsDirty(Boolean dirty) {
        getCurrentJob().setIsDirty(dirty);

        if (dirty) {
            getCurrentJob().getJobStatusAndTracking().setEditStatus("(edited)");
        } else {
            getCurrentJob().getJobStatusAndTracking().setEditStatus("");
        }
    }

    @Override
    public Boolean getIsDirty() {
        return getCurrentJob().getIsDirty();
    }
    
     public HumanResourceManager getHumanResourceManager() {
         
        return BeanUtils.findBean("humanResourceManager");
        
    }

    /**
     * Determine if the current user is the department's supervisor. This is
     * done by determining if the user is the head/active acting head of the
     * department to which the job was assigned.
     *
     * @param job
     * @return
     */
    // tk del. Move to User and make static method
    public Boolean isUserDepartmentSupervisor(Job job) {
        EntityManager em = getEntityManager1();

        Job foundJob = Job.findJobById(em, job.getId());

        if (Department.findAssignedToJob(foundJob, em).getHead().getId().longValue() == getUser().getEmployee().getId().longValue()) {
            return true;
        } else {
            return (Department.findAssignedToJob(foundJob, em).getActingHead().getId().longValue() == getUser().getEmployee().getId().longValue())
                    && Department.findAssignedToJob(foundJob, em).getActingHeadActive();
        }
    }

    public void updateAssignee() {
        setIsDirty(true);
    }

    HSSFCellStyle getDefaultCellStyle(HSSFWorkbook wb) {
        HSSFCellStyle cellStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        return cellStyle;
    }

    public Font getWingdingsFont(HSSFWorkbook wb) {
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Wingdings");

        return font;
    }

    public Font getFont(HSSFWorkbook wb, String fontName, short fontsize) {
        Font font = wb.createFont();
        font.setFontHeightInPoints(fontsize);
        font.setFontName(fontName);

        return font;
    }

    public ByteArrayInputStream createServiceContractExcelFileInputStream(
            User user,
            String filePath) {
        try {

            Client client = getCurrentJob().getClient();

            File file = new File(filePath);

            FileInputStream inp = new FileInputStream(file);

            // Create workbook from input file
            POIFSFileSystem fileSystem = new POIFSFileSystem((FileInputStream) inp);

            HSSFWorkbook wb = new HSSFWorkbook(fileSystem);

            // Fonts
            Font defaultFont = getFont(wb, "Arial", (short) 10);
            Font samplesFont = getFont(wb, "Arial", (short) 9);
            Font samplesRefFont = getFont(wb, "Arial", (short) 9);
            samplesRefFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            Font jobNumberFont = getFont(wb, "Arial Black", (short) 14);

            // Cell style
            HSSFCellStyle dataCellStyle;

            // Create temp file for output
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Get service contract sheet
            HSSFSheet serviceContractSheet = wb.getSheet("ServiceContract");
            serviceContractSheet.setActive(true);
            serviceContractSheet.setForceFormulaRecalculation(true);

            // Fill in job data
            // Job number
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 2);
            dataCellStyle.setFont(jobNumberFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A6", // A = 0 , 6 = 5
                    getCurrentJob().getJobNumber(),
                    "java.lang.String", dataCellStyle);

            // Contracting business office       
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "K6",
                    getCurrentJob().getBusinessOffice().getName(),
                    "java.lang.String", dataCellStyle);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "K7",
                    "",
                    "java.lang.String", dataCellStyle);

            // Job entry agent:
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F9",
                    getCurrentJob().getJobStatusAndTracking().getEnteredBy().getName(),
                    "java.lang.String", dataCellStyle);

            // Date agent prepared contract:   
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F10",
                    getCurrentJob().getJobStatusAndTracking().getDateAndTimeEntered(),
                    "java.util.Date", dataCellStyle);

            // Department in charge of job (Parent department):   
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F11",
                    getCurrentJob().getDepartment().getName(),
                    "java.lang.String", dataCellStyle);

            // Estimated turn around time:
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F12",
                    getCurrentJob().getEstimatedTurnAroundTimeInDays(),
                    "java.lang.String", dataCellStyle);

            // Estimated Sub Total:
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F13",
                    getCurrentJob().getJobCostingAndPayment().getEstimatedCost(),
                    "Currency", dataCellStyle);
            // Estimated Tax
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F14",
                    getCurrentJob().getJobCostingAndPayment().getEstimatedCost()
                    * getCurrentJob().getJobCostingAndPayment().getTax().getValue(),
                    "Currency", dataCellStyle);

            // Estimated Total Cost
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F15",
                    getCurrentJob().getJobCostingAndPayment().getCalculatedCostEstimate(),
                    "Currency", dataCellStyle);

            // Minimum First Deposit
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "F16",
                    getCurrentJob().getJobCostingAndPayment().getCalculatedMinDeposit(),
                    "Currency", dataCellStyle);

            // RECEIPT #
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            dataCellStyle.setWrapText(true);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "R9",
                    getCurrentJob().getJobCostingAndPayment().getReceiptNumbers(),
                    "java.lang.String", dataCellStyle);

            // TOTAL PAID
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            dataCellStyle.setWrapText(true);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "V9",
                    getCurrentJob().getJobCostingAndPayment().getTotalPayment(),
                    "Currency", dataCellStyle);

            // PAYMENT BREAKDOWN
            // Calculate tax from payment
            Double payment = (100.0 * getCurrentJob().getJobCostingAndPayment().getTotalPayment())
                    / (getCurrentJob().getJobCostingAndPayment().getTax().getTaxValue() + 100.0);
            Double tax = getCurrentJob().getJobCostingAndPayment().getTotalPayment() - payment;
            // Tax
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "AC9",
                    tax,
                    "Currency", dataCellStyle);
            // Payment
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "AC10",
                    payment,
                    "Currency", dataCellStyle);

            // DATE PAID (date of last payment)
            if (getCurrentJob().getJobCostingAndPayment().getLastPaymentDate() != null) {
                dataCellStyle = getDefaultCellStyle(wb);
                dataCellStyle.setBorderBottom((short) 1);
                dataCellStyle.setFont(defaultFont);
                dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AH9",
                        getCurrentJob().getJobCostingAndPayment().getLastPaymentDate(),
                        "java.util.Date", dataCellStyle);
            }

            // BALANCE (amount due) 
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            if (getCurrentJob().getJobCostingAndPayment().getFinalCost() > 0.0) {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AL9",
                        "exactly",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AL10",
                        getCurrentJob().getJobCostingAndPayment().getAmountDue(),
                        "Currency", dataCellStyle);
            } else {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AL9",
                        "approximately",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AL10",
                        BusinessEntityUtils.roundTo2DecimalPlaces(getCurrentJob().getJobCostingAndPayment().getCalculatedCostEstimate())
                        - BusinessEntityUtils.roundTo2DecimalPlaces(getCurrentJob().getJobCostingAndPayment().getTotalPayment()),
                        "Currency", dataCellStyle);
            }

            // PAYMENT TERMS/INFORMATION
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setBorderRight((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
            if (!getCurrentJob().getJobCostingAndPayment().getAllPaymentTerms().trim().equals("")) {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "R12",
                        getCurrentJob().getJobCostingAndPayment().getAllPaymentTerms(),
                        "java.lang.String", dataCellStyle);
            } else {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "R12",
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
            }

            // THE INFORMATION IN SECTION 3
            // AGENT/CASHIER
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setBorderRight((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "AL12",
                    getCurrentJob().getJobCostingAndPayment().getLastPaymentEnteredBy().getName(),
                    "java.lang.String", dataCellStyle);

            // CLIENT NAME & BILLING ADDRESS
            // Name
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A20",
                    client.getName(),
                    "java.lang.String", dataCellStyle);

            // Billing address    
            Address billingAddress = getCurrentJob().getBillingAddress();
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A22",
                    billingAddress.getAddressLine1(),
                    "java.lang.String", dataCellStyle);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A23",
                    billingAddress.getAddressLine2(),
                    "java.lang.String", dataCellStyle);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A24",
                    billingAddress.getStateOrProvince(),
                    "java.lang.String", dataCellStyle);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A25",
                    billingAddress.getCity(),
                    "java.lang.String", dataCellStyle);

            // Contact person
            // Name
            Contact contactPerson = getCurrentJob().getContact();
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "M20",
                    contactPerson,
                    "java.lang.String", dataCellStyle);

            // Email
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "M22",
                    contactPerson.getInternet().getEmail1(),
                    "java.lang.String", dataCellStyle);

            // Phone
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "Z20",
                    contactPerson.getMainPhoneNumber(),
                    "java.lang.String", dataCellStyle);

            // Fax
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "Z22",
                    contactPerson.getMainFaxNumber(),
                    "java.lang.String", dataCellStyle);

            // TYPE OF SERVICE(S) NEEDED
            // Gather services. 
            getCurrentJob().getServiceContract().setJob(getCurrentJob());
            String services = getCurrentJob().getServiceContract().getSelectedServiceForContract().getName() + " ";
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
            dataCellStyle.setWrapText(true);
            // NB: Gathering services like this will no longer be necessary
            /*
            if (job.getServiceContract().getServiceRequestedTesting()) {
                services = services + "Testing ";
            }
            if (job.getServiceContract().getServiceRequestedCalibration()) {
                services = services + "Calibration ";
            }
            if (job.getServiceContract().getServiceRequestedLabelEvaluation()) {
                services = services + "Label Evaluation ";
            }
            if (job.getServiceContract().getServiceRequestedInspection()) {
                services = services + "Inspection ";
            }
            if (job.getServiceContract().getServiceRequestedConsultancy()) {
                services = services + "Consultancy ";
            }
            if (job.getServiceContract().getServiceRequestedTraining()) {
                services = services + "Training ";
            }
            if (job.getServiceContract().getServiceRequestedOther()) {
                if ((job.getServiceContract().getServiceRequestedOtherText() != null)
                        && (!job.getServiceContract().getServiceRequestedOtherText().isEmpty())) {
                    services = services + " " + job.getServiceContract().getServiceRequestedOtherText();
                }
            }
             */
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "AD21",
                    services,
                    "java.lang.String", dataCellStyle);

            // Fax/Email report?:
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderRight((short) 1);
            dataCellStyle.setBorderBottom((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
            if (getCurrentJob().getServiceContract().getAdditionalServiceFaxResults()) {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN21",
                        "Yes",
                        "java.lang.String", dataCellStyle);
            } else {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN21",
                        "No",
                        "java.lang.String", dataCellStyle);
            }

            // Expedite?
            if (getCurrentJob().getServiceContract().getAdditionalServiceUrgent()) {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN23",
                        "Yes",
                        "java.lang.String", dataCellStyle);
            } else {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN23",
                        "No",
                        "java.lang.String", dataCellStyle);
            }

            // Purchase Order:        
            if (getCurrentJob().getJobCostingAndPayment().getPurchaseOrderNumber().equals("")) {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN25",
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
            } else {
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AN25",
                        getCurrentJob().getJobCostingAndPayment().getPurchaseOrderNumber(),
                        "java.lang.String", dataCellStyle);
            }

            // CLIENT INSTRUCTION/DETAILS FOR JOB
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderTop((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "M24",
                    getCurrentJob().getInstructions(),
                    "java.lang.String", dataCellStyle);

            // DESCRIPTION OF SUBMITTED SAMPLE(S)
            int samplesStartngRow = 32;
            if (!getCurrentJob().getJobSamples().isEmpty()) {
                for (JobSample jobSample : getCurrentJob().getJobSamples()) {
                    dataCellStyle = getDefaultCellStyle(wb);
                    dataCellStyle.setBorderLeft((short) 1);
                    dataCellStyle.setFont(samplesRefFont);
                    dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                    dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                    dataCellStyle.setWrapText(true);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "A" + samplesStartngRow,
                            jobSample.getReference(),
                            "java.lang.String", dataCellStyle);
                    dataCellStyle = getDefaultCellStyle(wb);
                    dataCellStyle.setBorderLeft((short) 1);
                    dataCellStyle.setFont(samplesFont);
                    dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                    dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                    dataCellStyle.setWrapText(true);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "B" + samplesStartngRow,
                            jobSample.getName(),
                            "java.lang.String", dataCellStyle);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "H" + samplesStartngRow,
                            jobSample.getProductBrand(),
                            "java.lang.String", dataCellStyle);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "O" + samplesStartngRow,
                            jobSample.getProductModel(),
                            "java.lang.String", dataCellStyle);

                    String productSerialAndCode = jobSample.getProductSerialNumber();
                    if (!jobSample.getProductCode().equals("")) {
                        productSerialAndCode = productSerialAndCode + "/" + jobSample.getProductCode();
                    }
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "W" + samplesStartngRow,
                            productSerialAndCode,
                            "java.lang.String", dataCellStyle);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "AG" + samplesStartngRow,
                            jobSample.getSampleQuantity(),
                            "java.lang.String", dataCellStyle);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "AI" + samplesStartngRow,
                            jobSample.getQuantity(),
                            "java.lang.String", dataCellStyle);
                    ReportUtils.setExcelCellValue(
                            wb, serviceContractSheet, "AI" + samplesStartngRow,
                            jobSample.getQuantity() + " (" + jobSample.getUnitOfMeasure() + ")",
                            "java.lang.String", dataCellStyle);
                    // Disposal
                    if (jobSample.getMethodOfDisposal() == 2) {
                        ReportUtils.setExcelCellValue(
                                wb, serviceContractSheet, "AO" + samplesStartngRow,
                                "Yes",
                                "java.lang.String", dataCellStyle);
                    } else {
                        ReportUtils.setExcelCellValue(
                                wb, serviceContractSheet, "AO" + samplesStartngRow,
                                "No",
                                "java.lang.String", dataCellStyle);
                    }

                    samplesStartngRow++;
                }
            } else {
                dataCellStyle = getDefaultCellStyle(wb);
                dataCellStyle.setBorderLeft((short) 1);
                dataCellStyle.setFont(samplesRefFont);
                dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataCellStyle.setWrapText(true);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "A" + samplesStartngRow,
                        "",
                        "java.lang.String", dataCellStyle);
                dataCellStyle = getDefaultCellStyle(wb);
                dataCellStyle.setBorderLeft((short) 1);
                dataCellStyle.setFont(samplesFont);
                dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataCellStyle.setWrapText(true);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "B" + samplesStartngRow,
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "H" + samplesStartngRow,
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "O" + samplesStartngRow,
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "W" + samplesStartngRow,
                        "Not applicable",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AG" + samplesStartngRow,
                        "",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AI" + samplesStartngRow,
                        "",
                        "java.lang.String", dataCellStyle);
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AI" + samplesStartngRow,
                        "",
                        "java.lang.String", dataCellStyle);
                // Disposal                   
                ReportUtils.setExcelCellValue(
                        wb, serviceContractSheet, "AO" + samplesStartngRow,
                        "",
                        "java.lang.String", dataCellStyle);
            }

            // ADDITIONAL DETAILS FOR SAMPLE(S) 
            String details = "";
            dataCellStyle = getDefaultCellStyle(wb);
            dataCellStyle.setBorderLeft((short) 1);
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            if (getCurrentJob().getJobSamples().isEmpty()) {
                details = "Not applicable";
            } else {
                for (JobSample jobSample : getCurrentJob().getJobSamples()) {
                    if (!jobSample.getDescription().isEmpty()) {
                        details = details + " (" + jobSample.getReference() + ") "
                                + jobSample.getDescription();
                    }
                }
            }
            ReportUtils.setExcelCellValue(
                    wb, serviceContractSheet, "A27",
                    details,
                    "java.lang.String", dataCellStyle);

            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            System.out.println(e);
        }

        return null;
    }

}
