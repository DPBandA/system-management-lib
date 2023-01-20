/*
Report Management (RM) 
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
package jm.com.dpbennett.rm.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.naming.ldap.LdapContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.rm.JobReportItem;
import jm.com.dpbennett.business.entity.jmts.JobSample;
import jm.com.dpbennett.business.entity.fm.JobSubCategory;
import jm.com.dpbennett.business.entity.rm.Report;
import jm.com.dpbennett.business.entity.fm.Sector;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.jmts.Job;
import jm.com.dpbennett.business.entity.sc.Complaint;
import jm.com.dpbennett.business.entity.sc.ComplianceSurvey;
import jm.com.dpbennett.business.entity.sc.FactoryInspection;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.DatePeriodJobReportColumnData;
import jm.com.dpbennett.sm.Authentication;
import jm.com.dpbennett.sm.manager.Manager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.DatePeriodJobReport;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.ReportUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 *
 * @author Desmond Bennett
 */
public class ReportManager implements Serializable, Manager {

    private String columnsToExclude;
    private Integer longProcessProgress;
    private String reportSearchText;
    private List<Report> foundReports;
    private Report selectedReport;
    private Report currentReport;
    private Boolean isActiveReportsOnly;
    private String reportCategory;
    private DatePeriod selectedDatePeriod;
    private Boolean edit;

    /**
     * Creates a new instance of ReportManager
     */
    public ReportManager() {
        init();
    }

    /**
     * Gets the header or title for this application.
     *
     * @return
     */
    @Override
    public String getApplicationHeader() {

        return "Report Management";
    }

    /**
     * Gets the SystemManager object as a session bean.
     *
     * @return
     */
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    /**
     * Gets the main user interface Tab View.
     *
     * @return
     */
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    /**
     * Gets local entity manager.
     *
     * @return
     */
    private EntityManager getLocalEntityManager() {
        Map props = new HashMap();
        props.put("javax.persistence.jdbc.driver",
                (String) SystemOption.getOptionValueObject(getEntityManager1(),
                        "defaultDatabaseDriver"));
        props.put("javax.persistence.jdbc.url",
                (String) SystemOption.getOptionValueObject(getEntityManager1(),
                        "defaultDatabaseURL"));
        props.put("javax.persistence.jdbc.user",
                (String) SystemOption.getOptionValueObject(getEntityManager1(),
                        "defaultDatabaseUsername"));
        props.put("javax.persistence.jdbc.password",
                (String) SystemOption.getOptionValueObject(getEntityManager1(),
                        "defaultDatabasePassword"));

        EntityManagerFactory emf = Persistence
                .createEntityManagerFactory("PU", props);

        return emf.createEntityManager();

    }

    /**
     *
     * @return
     */
    @Override
    public ArrayList<SelectItem> getDatePeriods() {
        ArrayList<SelectItem> datePeriods = new ArrayList<>();

        for (String name : DatePeriod.getDatePeriodNames()) {
            datePeriods.add(new SelectItem(name, name));
        }

        return datePeriods;
    }

    /**
     *
     * @return
     */
    public DatePeriod getReportingDatePeriod1() {
        if (selectedReport.getDatePeriods().isEmpty()) {
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));
        }

        // Ensure that no date period is null
        if (selectedReport.getDatePeriods().get(0).getStartDate() == null) {
            selectedReport.getDatePeriods().get(0).setStartDate(new Date());
        }
        if (selectedReport.getDatePeriods().get(0).getEndDate() == null) {
            selectedReport.getDatePeriods().get(0).setEndDate(new Date());
        }

        return selectedReport.getDatePeriods().get(0);
    }

    /**
     * Special method to be removed later when the current method of generating
     * monthly reports is done away with.
     *
     * @return
     */
    public DatePeriod getMonthlyReportDataDatePeriod() {
        if (getReportingDatePeriod1().getEndDate() == null) {
            getReportingDatePeriod1().setEndDate(new Date());
        }

        return getReportingDatePeriod1();
    }

    /**
     *
     * @param reportingDatePeriod1
     */
    public void setReportingDatePeriod1(DatePeriod reportingDatePeriod1) {
        selectedReport.getDatePeriods().set(0, reportingDatePeriod1);
    }

    /**
     *
     * @return
     */
    public DatePeriod getReportingDatePeriod2() {

        if (selectedReport.getDatePeriods().isEmpty()) {
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().get(1).setShow(false);

        } else if (selectedReport.getDatePeriods().size() == 1) {

            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().get(1).setShow(false);

        }

        // Ensure that no date period is null
        if (selectedReport.getDatePeriods().get(1).getStartDate() == null) {
            selectedReport.getDatePeriods().get(1).setStartDate(new Date());
        }
        if (selectedReport.getDatePeriods().get(1).getEndDate() == null) {
            selectedReport.getDatePeriods().get(1).setEndDate(new Date());
        }

        return selectedReport.getDatePeriods().get(1);
    }

    public void setReportingDatePeriod2(DatePeriod reportingDatePeriod2) {
        selectedReport.getDatePeriods().set(1, reportingDatePeriod2);
    }

    public DatePeriod getReportingDatePeriod3() {

        if (selectedReport.getDatePeriods().isEmpty()) {
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));
            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().get(2).setShow(false);

        } else if (selectedReport.getDatePeriods().size() == 1) {

            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().get(2).setShow(false);

        } else if (selectedReport.getDatePeriods().size() == 2) {

            selectedReport.getDatePeriods().add(
                    new DatePeriod("This month", "month", null, null, null,
                            null, false, false, true));

            selectedReport.getDatePeriods().get(2).setShow(false);

        }

        // Ensure that no date period is null
        if (selectedReport.getDatePeriods().get(2).getStartDate() == null) {
            selectedReport.getDatePeriods().get(2).setStartDate(new Date());
        }
        if (selectedReport.getDatePeriods().get(2).getEndDate() == null) {
            selectedReport.getDatePeriods().get(2).setEndDate(new Date());
        }

        return selectedReport.getDatePeriods().get(2);
    }

    public void setReportingDatePeriod3(DatePeriod reportingDatePeriod3) {
        selectedReport.getDatePeriods().set(2, reportingDatePeriod3);
    }

    public Boolean getIsInvalidReport() {
        return (getSelectedReport().getId() == null);
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public DatePeriod getSelectedDatePeriod() {
        return selectedDatePeriod;
    }

    public void setSelectedDatePeriod(DatePeriod selectedDatePeriod) {
        this.selectedDatePeriod = selectedDatePeriod;
    }

    public void setDatePeriodToDelete(DatePeriod selectedDatePeriod) {
        this.selectedDatePeriod = selectedDatePeriod;

        deleteDatePeriod();
    }

    public void openReportsTab() {
        getMainTabView().openTab("Reports");
    }

    public void openReportTemplatesTab() {
        getMainTabView().openTab("Report Templates");
    }

    public void openReportsTab(String reportCategory) {
        setReportCategory(reportCategory);

        getMainTabView().openTab("Reports");
    }

    public List<Report> completeReport(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Report> reports = Report.findActiveReportsByName(em, query);

            return reports;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Report> completeReportByCategoryAndName(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Report> reports = Report.findActiveReportsByCategoryAndName(em,
                    getReportCategory(), query);

            return reports;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public List getReportCategories() {
        return ReportUtils.getCategories(getEntityManager1());
    }

    public List getReportMimeTypes() {
        return ReportUtils.getMimeTypes();
    }

    public Boolean getIsActiveReportsOnly() {
        if (isActiveReportsOnly == null) {
            isActiveReportsOnly = true;
        }
        return isActiveReportsOnly;
    }

    public void setIsActiveReportsOnly(Boolean isActiveReportsOnly) {
        this.isActiveReportsOnly = isActiveReportsOnly;
    }

    public List<Report> getFoundReports() {
        if (foundReports == null) {
            foundReports = Report.findAllActiveReports(getEntityManager1());
        }

        return foundReports;
    }

    public String getReportSearchText() {
        return reportSearchText;
    }

    public void setReportSearchText(String reportSearchText) {
        this.reportSearchText = reportSearchText;
    }

    public void doReportSearch() {

        if (getIsActiveReportsOnly()) {
            foundReports = Report.findActiveReports(getEntityManager1(), getReportSearchText());
        } else {
            foundReports = Report.findReports(getEntityManager1(), getReportSearchText());
        }

    }

    public void editReport() {
        PrimeFacesUtils.openDialog(null, "reportTemplateDialog", true, true, true, 625, 750);
    }

    public Report getSelectedReport() {
        if (selectedReport == null) {
            selectedReport = new Report();
        }

        return selectedReport;
    }

    public void setSelectedReport(Report selectedReport) {
        this.selectedReport = selectedReport;
    }

    public Report getCurrentReport() {
        if (currentReport == null) {
            currentReport = new Report();
        }

        return currentReport;
    }

    public void setCurrentReport(Report currentReport) {
        this.currentReport = currentReport;
    }

    public void saveCurrentReport() {

        currentReport.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void okSelectedDatePeriod(ActionEvent actionEvent) {
        getSelectedDatePeriod().setIsDirty(true);

        if (getIsNewDatePeriod()) {
            currentReport.getDatePeriods().add(selectedDatePeriod);
        }

        closeDialog(actionEvent);
    }

    public void cancelSelectedDatePeriod(ActionEvent actionEvent) {
        getSelectedDatePeriod().setIsDirty(false);

        closeDialog(actionEvent);
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewReport() {

        currentReport = new Report();

        getMainTabView().openTab("Report Templates");

        editReport();
    }

    public void createNewDatePeriod() {

        selectedDatePeriod = new DatePeriod("This month", "month",
                "", null, null, null, false, false, false);
        selectedDatePeriod.initDatePeriod();

        setEdit(false);

        PrimeFacesUtils.openDialog(null, "reportDatePeriodDialog", true, true, true, 0, 400);
    }

    public Boolean getIsNewDatePeriod() {
        return getSelectedDatePeriod().getId() == null && !getEdit();
    }

    public void editDatePeriod() {

        setEdit(true);

        PrimeFacesUtils.openDialog(null, "reportDatePeriodDialog", true, true, true, 0, 400);

    }

    public void deleteDatePeriod() {
        EntityManager em = getEntityManager1();

        if (selectedDatePeriod.getId() != null) {
            DatePeriod datePeriod = DatePeriod.findById(em, selectedDatePeriod.getId());
            if (datePeriod != null) {
                currentReport.getDatePeriods().remove(selectedDatePeriod);
                currentReport.save(em);
                em.getTransaction().begin();
                em.remove(datePeriod);
                em.getTransaction().commit();
            }
        } else {
            currentReport.getDatePeriods().remove(selectedDatePeriod);
        }

    }

    @Override
    public final void init() {
        reset();
        
    }

    @Override
    public void reset() {
        this.reportSearchText = "";
        this.longProcessProgress = 0;
        this.columnsToExclude = "";
        this.reportCategory = "Job";
    }

    public void closeReportsTab() {
        getMainTabView().closeTab("Reports");
    }

    @Override
    public User getUser() {
        return getSystemManager().getUser();
    }

    @Override
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    public Employee getReportEmployee1() {
        if (selectedReport.getEmployees().isEmpty()) {
            selectedReport.getEmployees().add(getUser().getEmployee());
        }
        return selectedReport.getEmployees().get(0);
    }

    public void setReportEmployee1(Employee reportEmployee1) {
        selectedReport.getEmployees().set(0, reportEmployee1);
    }

    public Department getReportingDepartment1() {
        if (selectedReport.getDepartments().isEmpty()) {
            selectedReport.getDepartments().add(getUser().getEmployee().getDepartment());
        }
        return selectedReport.getDepartments().get(0);
    }

    public Client getReportingClient1() {
        if (selectedReport.getClients().isEmpty()) {
            selectedReport.getClients().add(new Client(""));
        }
        return selectedReport.getClients().get(0);
    }

    public void setReportingDepartment1(Department reportingDepartment1) {
        if (!selectedReport.getDepartments().isEmpty()) {
            selectedReport.getDepartments().set(0, reportingDepartment1);
        }
    }

    public JasperPrint getJasperPrint(Connection con,
            HashMap parameters) {
        JasperPrint jasperPrint = null;
        FileInputStream fis;

        switch (selectedReport.getReportFileMimeType()) {
            case "application/jasper":
                if (getSelectedReport().getUsePackagedReportFileTemplate()) {
                    try {
                        fis = new FileInputStream(getClass().getClassLoader().
                                getResource("/reports/" + selectedReport.getReportFileTemplate()).getFile());

                        jasperPrint = JasperFillManager.fillReport(
                                fis,
                                parameters,
                                con);
                    } catch (FileNotFoundException | JRException e) {
                        System.out.println(e);
                    }

                } else {
                    try {
                        jasperPrint = JasperFillManager.fillReport(
                                selectedReport.getReportFileTemplate(),
                                parameters,
                                con);
                    } catch (JRException e) {
                        System.out.println(e);
                    }
                }

                break;

            case "application/jrxml":
                if (getSelectedReport().getUsePackagedReportFileTemplate()) {
                    try {
                        fis = new FileInputStream(getClass().getClassLoader().
                                getResource("/reports/" + selectedReport.getReportFileTemplate()).getFile());

                        JasperReport jasperReport = JasperCompileManager
                                .compileReport(fis);

                        jasperPrint = JasperFillManager.fillReport(
                                jasperReport,
                                parameters,
                                con);
                    } catch (FileNotFoundException | JRException e) {
                        System.out.println(e);
                    }
                } else {
                    try {
                        JasperReport jasperReport = JasperCompileManager
                                .compileReport(selectedReport.getReportFileTemplate());

                        jasperPrint = JasperFillManager.fillReport(
                                jasperReport,
                                parameters,
                                con);

                    } catch (JRException e) {
                        System.out.println("Error compiling report: " + e);
                    }
                }
                break;

            default:
                break;
        }

        return jasperPrint;
    }

    public StreamedContent getReportStreamedContent() {

        EntityManager em = getEntityManager1();
        HashMap parameters = new HashMap();
        Connection con;
        JasperPrint print;

        try {

            con = BusinessEntityUtils.establishConnection(
                    (String) SystemOption.getOptionValueObject(em, "defaultDatabaseDriver"),
                    (String) SystemOption.getOptionValueObject(em, "defaultDatabaseURL"),
                    (String) SystemOption.getOptionValueObject(em, "defaultDatabaseUsername"),
                    (String) SystemOption.getOptionValueObject(em, "defaultDatabasePassword"));

            if (con != null) {
                StreamedContent streamContent;
                byte[] fileBytes;
                String logoURL = (String) SystemOption.getOptionValueObject(getEntityManager1(),
                        "logoURL");

                // Provide report parameters
                parameters.put("reportTitle", selectedReport.getName());
                parameters.put("logoURL", logoURL);

                // Provide date parameters if required
                if (selectedReport.getDatePeriodRequired()) {
                    for (int i = 0; i < selectedReport.getDatePeriods().size(); i++) {
                        System.out.println("date period: " + selectedReport.getDatePeriods().get(i).getDateField());
                        parameters.put("dateField" + (i + 1),
                                selectedReport.getDatePeriods().get(i).getDateField());
                        parameters.put("startOfPeriod" + (i + 1),
                                selectedReport.getDatePeriods().get(i).initDatePeriod().getStartDate());
                        parameters.put("endOfPeriod" + (i + 1),
                                selectedReport.getDatePeriods().get(i).initDatePeriod().getEndDate());
                    }
                }
                // Provide employee parameters if required
                if (selectedReport.getEmployeeRequired()) {
                    for (int i = 0; i < selectedReport.getEmployees().size(); i++) {
                        parameters.put("employeeId" + (i + 1),
                                selectedReport.getEmployees().get(i).getId());
                    }
                }
                // Provide department parameters if required
                if (selectedReport.getDepartmentRequired()) {
                    for (int i = 0; i < selectedReport.getDepartments().size(); i++) {
                        parameters.put("departmentId" + (i + 1),
                                selectedReport.getDepartments().get(i).getId());
                        parameters.put("departmentName" + (i + 1),
                                selectedReport.getDepartments().get(i).getName());
                    }
                }
                // Provide client parameters if required
                if (selectedReport.getClientRequired()) {
                    for (int i = 0; i < selectedReport.getClients().size(); i++) {
                        parameters.put("clientId" + (i + 1),
                                selectedReport.getClients().get(i).getId());
                        parameters.put("clientName" + (i + 1),
                                selectedReport.getClients().get(i).getName());
                    }
                }

                print = getJasperPrint(con, parameters);

                switch (selectedReport.getReportOutputFileMimeType()) {
                    case "application/pdf":

                        fileBytes = JasperExportManager.exportReportToPdf(print);

                        streamContent = DefaultStreamedContent.builder()
                                .contentType(selectedReport.getReportOutputFileMimeType())
                                .name(selectedReport.getReportFile())
                                .stream(() -> new ByteArrayInputStream(fileBytes))
                                .build();

//                        streamContent = new DefaultStreamedContent(new ByteArrayInputStream(fileBytes),
//                                selectedReport.getReportOutputFileMimeType(),
//                                selectedReport.getReportFile());
                        break;

                    case "application/xlsx":
                    case "application/xls":

                        //JRXlsExporter exporterXLS = new JRXlsExporter();
                        JRXlsxExporter exporterXLS = new JRXlsxExporter();
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
                        exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, outStream);
                        exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
                        exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
                        exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                        exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                        exporterXLS.exportReport();

                        streamContent = DefaultStreamedContent.builder()
                                .contentType(selectedReport.getReportOutputFileMimeType())
                                .name(selectedReport.getReportFile())
                                .stream(() -> new ByteArrayInputStream(outStream.toByteArray()))
                                .build();

                        break;

                    default:
                        fileBytes = JasperExportManager.exportReportToPdf(print);

                        streamContent = DefaultStreamedContent.builder()
                                .contentType(selectedReport.getReportOutputFileMimeType())
                                .name(selectedReport.getReportFile())
                                .stream(() -> new ByteArrayInputStream(fileBytes))
                                .build();

                        break;

                }

                setLongProcessProgress(100);

                return streamContent;

            } else {
                return null;
            }

        } catch (JRException e) {
            System.out.println(e);
            setLongProcessProgress(100);

            return null;
        }

    }

    public StreamedContent getReportFile() {

        StreamedContent reportFile = null;

        try {

            switch (getSelectedReport().getReportFileMimeType()) {
                case "application/jasper":
                case "application/jrxml":
                    reportFile = getReportStreamedContent();
                    break;
                case "application/xlsx":
                    if (getSelectedReport().getName().equals("Analytical Services Report")) {
                        reportFile = getAnalyticalServicesReport(getLocalEntityManager());
                    } else if (getSelectedReport().getName().toUpperCase().contains("COMPLIANCE MONTHLY REPORT")) {
                        reportFile = getComplianceMonthlyReport(getLocalEntityManager());
                    } else if (getSelectedReport().getName().toUpperCase().contains("MONTHLY REPORT")) {
                        reportFile = getMonthlyReport(getLocalEntityManager());
                    }
                    break;
                case "application/xls":
                    if (getSelectedReport().getName().toUpperCase().contains("MONTHLY REPORT")) {
                        reportFile = getMonthlyReport(getLocalEntityManager());
                    }
                    break;
                default:
                    break;
            }

            setLongProcessProgress(100);

        } catch (Exception e) {
            System.out.println(e);
            setLongProcessProgress(100);
        }

        return reportFile;
    }

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

    public void onLongProcessComplete() {
        longProcessProgress = 0;
    }

    public void setLongProcessProgress(Integer longProcessProgress) {
        this.longProcessProgress = longProcessProgress;
    }

    public StreamedContent getMonthlyReport(EntityManager em) {

        ByteArrayInputStream stream;

        try {

            // Get byte stream for report file
            if (getSelectedReport().getUsePackagedReportFileTemplate()) {
                stream = createExcelMonthlyReportFileInputStream(
                        em, new File(getClass().getClassLoader().
                                getResource("/reports" + getSelectedReport().getReportFileTemplate()).getFile()),
                        getReportingDepartment1().getId());
            } else {
                stream = createExcelMonthlyReportFileInputStream(
                        em, new File(getSelectedReport().getReportFileTemplate()),
                        getReportingDepartment1().getId());
            }

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType(selectedReport.getReportOutputFileMimeType())
                    .name(selectedReport.getReportFile())
                    .build();

            //return new DefaultStreamedContent(stream, getSelectedReport().getReportFileMimeType(), getSelectedReport().getReportFile());
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    public StreamedContent getComplianceMonthlyReport(EntityManager em) {

        ByteArrayInputStream stream;

        try {

            // Get byte stream for report file
            if (getSelectedReport().getUsePackagedReportFileTemplate()) {
                stream = createExcelComplianceMonthlyReportFileInputStream(
                        em, new File(getClass().getClassLoader().
                                getResource("/reports/" + getSelectedReport().getReportFileTemplate()).getFile()),
                        getReportingDepartment1().getId());
            } else {
                stream = createExcelComplianceMonthlyReportFileInputStream(
                        em, new File(getSelectedReport().getReportFileTemplate()),
                        getReportingDepartment1().getId());
            }

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType(selectedReport.getReportOutputFileMimeType())
                    .name(selectedReport.getReportFile())
                    .build();
            
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    public StreamedContent getAnalyticalServicesReport(EntityManager em) {

        try {
            ByteArrayInputStream stream;

            if (getSelectedReport().getUsePackagedReportFileTemplate()) {
                stream = analyticalServicesReportFileInputStream(em, new File(getClass().getClassLoader().
                        getResource("/reports/" + getSelectedReport().getReportFileTemplate()).getFile()),
                        getReportingDepartment1().getId());
            } else {
                stream = analyticalServicesReportFileInputStream(em, new File(getSelectedReport().getReportFileTemplate()),
                        getReportingDepartment1().getId());
            }

            return DefaultStreamedContent.builder()
                    .stream(() -> stream)
                    .contentType(selectedReport.getReportOutputFileMimeType())
                    .name(selectedReport.getReportFile())
                    .build();

            
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    public void updateServiceContract() {
    }

    public List<Report> getJobReports() {
        EntityManager em = getEntityManager1();

        List<Report> reports = Report.findAllReports(em);

        return reports;
    }

    public String getColumnsToExclude() {
        return columnsToExclude;
    }

    public void setColumnsToExclude(String columnsToExclude) {
        this.columnsToExclude = columnsToExclude;
    }

    public void updateDepartmentReport() {
    }

    public void updateReportCategory() {
        setSelectedReport(new Report(""));
    }

    public void updateReport() {

    }

    public List<DatePeriodJobReportColumnData> jobSubCategogyGroupReportByDatePeriod(
            EntityManager em,
            String dateSearchField,
            String searchText,
            Date startDate,
            Date endDate) {

        List<DatePeriodJobReportColumnData> data;

        String searchQuery
                = "SELECT NEW jm.com.dpbennett.utils.DatePeriodJobReportColumnData"
                + "("
                + "job.jobSubCategory,"
                + "SUM(jobCostingAndPayment.finalCost),"
                + "SUM(job.noOfTestsOrCalibrations)"
                + ")"
                + " FROM Job job"
                + " JOIN job.jobStatusAndTracking jobStatusAndTracking"
                + " JOIN job.jobSubCategory jobSubCategory"
                + " JOIN job.department department"
                + " JOIN job.subContractedDepartment subContractedDepartment"
                + " JOIN job.jobCostingAndPayment jobCostingAndPayment"
                + " WHERE ((jobStatusAndTracking." + dateSearchField + " >= " + BusinessEntityUtils.getDateString(startDate, "'", "YMD", "-")
                + " AND jobStatusAndTracking." + dateSearchField + " <= " + BusinessEntityUtils.getDateString(endDate, "'", "YMD", "-") + "))"
                + " AND ( UPPER(department.name) = '" + searchText.toUpperCase() + "'"
                + " OR UPPER(subContractedDepartment.name) = '" + searchText.toUpperCase() + "')"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'CANCELLED'"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'WITHDRAWN BY CLIENT'"
                + " GROUP BY job.jobSubCategory"
                + " ORDER BY job.jobSubCategory.subCategory ASC";

        try {
            data = em.createQuery(searchQuery, DatePeriodJobReportColumnData.class).getResultList();
            if (data == null) {
                data = new ArrayList<>();
            }

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        return data;
    }

    public List<DatePeriodJobReportColumnData> sectorReportByDatePeriod(
            EntityManager em,
            String dateSearchField,
            String searchText,
            Date startDate,
            Date endDate) {

        List<DatePeriodJobReportColumnData> data;
        String searchQuery;

        searchQuery
                = "SELECT NEW jm.com.dpbennett.utils.DatePeriodJobReportColumnData"
                + "("
                + "job.sector,"
                + "SUM(job.noOfTestsOrCalibrations)"
                + ")"
                + " FROM Job job"
                + " JOIN job.jobStatusAndTracking jobStatusAndTracking"
                + " JOIN job.sector sector"
                + " JOIN job.department department"
                + " JOIN job.subContractedDepartment subContractedDepartment"
                + " WHERE ((jobStatusAndTracking." + dateSearchField + " >= " + BusinessEntityUtils.getDateString(startDate, "'", "YMD", "-")
                + " AND jobStatusAndTracking." + dateSearchField + " <= " + BusinessEntityUtils.getDateString(endDate, "'", "YMD", "-") + "))"
                + " AND ( UPPER(department.name) = '" + searchText.toUpperCase() + "'"
                + " OR UPPER(subContractedDepartment.name) = '" + searchText.toUpperCase() + "')"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'CANCELLED'"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'WITHDRAWN BY CLIENT'"
                + " GROUP BY job.sector"
                + " ORDER BY job.sector.name ASC";
        try {
            data = em.createQuery(searchQuery, DatePeriodJobReportColumnData.class).getResultList();
            if (data
                    == null) {
                data = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        return data;
    }

    public List<DatePeriodJobReportColumnData> jobReportByDatePeriod(
            EntityManager em,
            String searchText,
            Date startDate,
            Date endDate) {

        List<DatePeriodJobReportColumnData> data;

        String searchQuery
                = "SELECT NEW jm.com.dpbennett.utils.DatePeriodJobReportColumnData"
                + "("
                + "job"
                + ")"
                + " FROM Job job"
                + " JOIN job.jobStatusAndTracking jobStatusAndTracking"
                + " JOIN job.department department"
                + " JOIN job.subContractedDepartment subContractedDepartment"
                + " WHERE ((jobStatusAndTracking.dateOfCompletion >= " + BusinessEntityUtils.getDateString(startDate, "'", "YMD", "-")
                + " AND jobStatusAndTracking.dateOfCompletion <= " + BusinessEntityUtils.getDateString(endDate, "'", "YMD", "-") + ")"
                + " OR"
                + " (jobStatusAndTracking.dateSubmitted >= " + BusinessEntityUtils.getDateString(startDate, "'", "YMD", "-")
                + " AND jobStatusAndTracking.dateSubmitted <= " + BusinessEntityUtils.getDateString(endDate, "'", "YMD", "-") + ")"
                + " OR"
                + " (jobStatusAndTracking.expectedDateOfCompletion >= " + BusinessEntityUtils.getDateString(startDate, "'", "YMD", "-")
                + " AND jobStatusAndTracking.expectedDateOfCompletion <= " + BusinessEntityUtils.getDateString(endDate, "'", "YMD", "-") + "))"
                + " AND ( UPPER(department.name) = '" + searchText.toUpperCase() + "'"
                + " OR UPPER(subContractedDepartment.name) = '" + searchText.toUpperCase() + "')"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'CANCELLED'"
                + " AND UPPER(jobStatusAndTracking.workProgress) <> 'WITHDRAWN BY CLIENT'"
                + " ORDER BY job.sector.name ASC";
        try {
            data = em.createQuery(searchQuery, DatePeriodJobReportColumnData.class).getResultList();
            if (data
                    == null) {
                data = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        return data;
    }

    public FileInputStream createExcelJobReportFileInputStream(
            URL FileUrl,
            User user,
            Department reportingDepartment,
            DatePeriodJobReport jobSubCategoryReport,
            DatePeriodJobReport sectorReport,
            DatePeriodJobReport jobQuantitiesAndServicesReport) throws URISyntaxException {

        try {
            File file = new File(FileUrl.toURI());
            FileInputStream inp = new FileInputStream(file);
            int row = 0;

            // Create workbook from input file
            POIFSFileSystem fileSystem = new POIFSFileSystem((FileInputStream) inp);
            HSSFWorkbook wb = new HSSFWorkbook(fileSystem);
            HSSFCellStyle dataCellStyle = wb.createCellStyle();
            HSSFCellStyle headerCellStyle = wb.createCellStyle();
            headerCellStyle.setFont(ReportUtils.createBoldFont(wb, (short) 14, HSSFColor.BLUE.index));
            HSSFCellStyle columnHeaderCellStyle = wb.createCellStyle();
            columnHeaderCellStyle.setFont(ReportUtils.createBoldFont(wb, (short) 12, HSSFColor.BLUE.index));

            try ( // Create temp file for output
                    FileOutputStream out = new FileOutputStream("MonthlyReport" + user.getId() + ".xls")) {
                HSSFSheet jobSheet = wb.getSheet("Statistics");
                if (jobSheet == null) {
                    jobSheet = wb.createSheet("Statistics");
                }
                
                ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                        "Job Statistics",
                        "java.lang.String", headerCellStyle);

                ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                        reportingDepartment.getName(),
                        "java.lang.String", headerCellStyle);
                
                ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                        BusinessEntityUtils.getDateInMediumDateFormat(new Date()),
                        "java.lang.String", headerCellStyle);
                
                ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                        jobSubCategoryReport.getDatePeriod(0).getPeriodString(),
                        "java.lang.String", headerCellStyle);
                
                row++;
                
                if (jobSubCategoryReport != null) {
                    ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                            "EARNINGS",
                            "java.lang.String", headerCellStyle);
                    
                    row++;
                    for (int i = 0; i < jobSubCategoryReport.getDatePeriods().length; i++) {
                        List<DatePeriodJobReportColumnData> reportColumnData = jobSubCategoryReport.getReportColumnData(jobSubCategoryReport.getDatePeriod(i).getName());
                        // insert table headings
                        ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                                jobSubCategoryReport.getDatePeriod(i).toString(), "java.lang.String", columnHeaderCellStyle);
                        ReportUtils.setExcelCellValue(wb, jobSheet, row, 0,
                                "Job subcategory", "java.lang.String", columnHeaderCellStyle);
                        ReportUtils.setExcelCellValue(wb, jobSheet, row, 1,
                                "Earnings", "java.lang.String", columnHeaderCellStyle);
                        ReportUtils.setExcelCellValue(wb, jobSheet, row++, 2,
                                "Calibrations/Tests", "java.lang.String", columnHeaderCellStyle);
                        
                        // SUMMARY - earnings
                        for (DatePeriodJobReportColumnData data : reportColumnData) {
                            // subcategory
                            ReportUtils.setExcelCellValue(wb, jobSheet, row, 0,
                                    data.getJobSubCategory().getName(), "java.lang.String", dataCellStyle);
                            // cost
                            ReportUtils.setExcelCellValue(wb, jobSheet, row, 1,
                                    data.getJobCostingAndPayment().getFinalCost(), "java.lang.Double", dataCellStyle);
                            // test/cals
                            ReportUtils.setExcelCellValue(wb, jobSheet, row++, 2,
                                    data.getNoOfTestsOrCalibrations(), "java.lang.Integer", dataCellStyle);
                        }
                        ++row;
                    }
                }
                
                if (sectorReport != null) {
                    ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                            "SECTORS SERVED",
                            "java.lang.String", headerCellStyle);
                    
                    row++;
                    for (int i = 0; i < sectorReport.getDatePeriods().length; i++) {
                        List<DatePeriodJobReportColumnData> reportColumnData = sectorReport.getReportColumnData(sectorReport.getDatePeriod(i).getName());
                        // sector table headings
                        ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                                sectorReport.getDatePeriod(i).toString(), "java.lang.String", columnHeaderCellStyle);
                        ReportUtils.setExcelCellValue(wb, jobSheet, row, 0,
                                "Sector", "java.lang.String", columnHeaderCellStyle);
                        
                        ReportUtils.setExcelCellValue(wb, jobSheet, row++, 2,
                                "Calibrations/Tests", "java.lang.String", columnHeaderCellStyle);
                        
                        // SECTOR - tests/cals
                        for (DatePeriodJobReportColumnData data : reportColumnData) {
                            // sector
                            ReportUtils.setExcelCellValue(wb, jobSheet, row, 0,
                                    data.getSector().getName(), "java.lang.String", dataCellStyle);
                            // test/cals
                            ReportUtils.setExcelCellValue(wb, jobSheet, row++, 2,
                                    data.getNoOfTestsOrCalibrations(), "java.lang.Integer", dataCellStyle);
                        }
                        ++row;
                    }
                }
                
                if (jobQuantitiesAndServicesReport != null) {
                    ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                            "JOB QUANTITIES AND SERVICES",
                            "java.lang.String", headerCellStyle);
                    
                    row++;
                    for (int i = 0; i < jobQuantitiesAndServicesReport.getDatePeriods().length; i++) {
                        List<DatePeriodJobReportColumnData> reportColumnData = jobQuantitiesAndServicesReport.getReportColumnData(jobQuantitiesAndServicesReport.getDatePeriod(i).getName());
                        
                        ReportUtils.setExcelCellValue(wb, jobSheet, row++, 0,
                                sectorReport.getDatePeriod(i).toString(), "java.lang.String", headerCellStyle);
                        // JobQuantities And Services table headings
                        List<JobReportItem> jobReportItems = jobQuantitiesAndServicesReport.getJobReportItems();
                        for (JobReportItem jobReportItem : jobReportItems) {
                            ReportUtils.setExcelCellValue(wb, jobSheet, row, 0,
                                    jobReportItem.getName(),
                                    "java.lang.String", dataCellStyle);
                            
                            ReportUtils.setExcelCellValue(wb, jobSheet, row++, 1,
                                    (Double) jobQuantitiesAndServicesReport.getReportItemValue(jobReportItem, jobQuantitiesAndServicesReport.getDatePeriod(i), reportColumnData),
                                    "java.lang.Double", dataCellStyle);
                        }
                        row++;
                    }
                }
                
                // write and save file for later use
                wb.write(out);
            }

            return new FileInputStream("MonthlyReport" + user.getId() + ".xls");

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    public ByteArrayInputStream analyticalServicesReportFileInputStream(
            EntityManager em,
            File reportFile,
            Long departmentId) {

        try {
            FileInputStream inp = new FileInputStream(reportFile);
            int row = 1;
            int col;
            //int cell;

            XSSFWorkbook wb = new XSSFWorkbook(inp);
            XSSFCellStyle stringCellStyle = wb.createCellStyle();
            XSSFCellStyle longCellStyle = wb.createCellStyle();
            XSSFCellStyle integerCellStyle = wb.createCellStyle();
            XSSFCellStyle doubleCellStyle = wb.createCellStyle();
            XSSFCellStyle dateCellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            dateCellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("m/d/yyyy"));

            // Output stream for modified Excel file
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Get sheets          
            XSSFSheet rawData = wb.getSheet("Raw Data");
            XSSFSheet jobReportSheet = wb.getSheet("Jobs Report");
            XSSFSheet employeeReportSheet = wb.getSheet("Employee Report");
            XSSFSheet sectorReportSheet = wb.getSheet("Sector Report");

            // Get report data
            List<Object[]> reportData = Job.getJobRecordsByTrackingDate(
                    em,
                    getReportingDatePeriod1().getDateField(),
                    BusinessEntityUtils.getDateString(getReportingDatePeriod1().getStartDate(), "'", "YMD", "-"),
                    BusinessEntityUtils.getDateString(getReportingDatePeriod1().getEndDate(), "'", "YMD", "-"),
                    departmentId);

            // Fill in report data            
            for (Object[] rowData : reportData) {
                col = 0;
                //  Employee/Assignee
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[7],
                        "java.lang.String", stringCellStyle);
                // No. samples
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Long) rowData[9],
                        "java.lang.Long", longCellStyle);
                // No. tests/calibrations
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Integer) rowData[10],
                        "java.lang.Integer", integerCellStyle);
                // No. tests
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Integer) rowData[11],
                        "java.lang.Integer", integerCellStyle);
                // No. calibrations
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Integer) rowData[12],
                        "java.lang.Integer", integerCellStyle);
                // Total cost
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Double) rowData[8],
                        "java.lang.Double", doubleCellStyle);
                //  Completion date
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Date) rowData[6],
                        "java.util.Date", dateCellStyle);
                //  Expected completion date
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Date) rowData[13],
                        "java.util.Date", dateCellStyle);
                // Job numbers
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[14],
                        "java.lang.String", stringCellStyle);
                // Sample description
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[0],
                        "java.lang.String", stringCellStyle);
                // Client/Source
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[15],
                        "java.lang.String", stringCellStyle);
                //  Date submitted
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (Date) rowData[16],
                        "java.util.Date", dateCellStyle);
                // Sector
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[17],
                        "java.lang.String", stringCellStyle);
                // Turnaround time status
                if ((rowData[6] != null) && (rowData[13] != null)) {
                    ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                            ((Date) rowData[6]).getTime() > ((Date) rowData[13]).getTime()
                            ? "late" : "on-time",
                            "java.lang.String", stringCellStyle);
                } else {
                    ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                            "",
                            "java.lang.String", stringCellStyle);
                }
                // Classification
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[18],
                        "java.lang.String", stringCellStyle);
                // Category
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[19],
                        "java.lang.String", stringCellStyle);
                // Subcategory
                ReportUtils.setExcelCellValue(wb, rawData, row, col++,
                        (String) rowData[20],
                        "java.lang.String", stringCellStyle);

                row++;

            }

            // Set department name and report period
            // Dept. name
            ReportUtils.setExcelCellValue(wb, jobReportSheet, 0, 0,
                    getSelectedReport().getDepartments().get(0).getName(),
                    "java.lang.String", null);
            ReportUtils.setExcelCellValue(wb, employeeReportSheet, 0, 0,
                    getSelectedReport().getDepartments().get(0).getName(),
                    "java.lang.String", null);
            ReportUtils.setExcelCellValue(wb, sectorReportSheet, 0, 0,
                    getSelectedReport().getDepartments().get(0).getName(),
                    "java.lang.String", null);
            // Period
            ReportUtils.setExcelCellValue(wb, jobReportSheet, 2, 0,
                    BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getStartDate())
                    + " - "
                    + BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getEndDate()),
                    "java.lang.String", null);
            ReportUtils.setExcelCellValue(wb, employeeReportSheet, 2, 0,
                    BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getStartDate())
                    + " - "
                    + BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getEndDate()),
                    "java.lang.String", null);
            ReportUtils.setExcelCellValue(wb, sectorReportSheet, 2, 0,
                    BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getStartDate())
                    + " - "
                    + BusinessEntityUtils.getDateInMediumDateFormat(getReportingDatePeriod1().getEndDate()),
                    "java.lang.String", null);

            // Write modified Excel file and return it
            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    public ByteArrayInputStream createExcelMonthlyReportFileInputStream(
            EntityManager em,
            File reportFile,
            Long departmentId) {

        try {
            String status;
            int row = 2;
            FileInputStream inp = new FileInputStream(reportFile);
            XSSFWorkbook wb = new XSSFWorkbook(inp);
            CreationHelper createHelper = wb.getCreationHelper();
            XSSFCellStyle stringCellStyle = wb.createCellStyle();
            stringCellStyle.setWrapText(true);
            XSSFCellStyle longCellStyle = wb.createCellStyle();
            XSSFCellStyle integerCellStyle = wb.createCellStyle();
            XSSFCellStyle doubleCellStyle = wb.createCellStyle();
            XSSFCellStyle dateCellStyle = wb.createCellStyle();
            XSSFCellStyle datePeriodsCellStyle = wb.createCellStyle();
            dateCellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("MMM dd, yyyy"));
            datePeriodsCellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            // Output stream for modified Excel file
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Get sheets ensure that crucial sheets are updated automatically
            XSSFSheet executiveSummary = wb.getSheet("Executive Summary");
            executiveSummary.setForceFormulaRecalculation(true);

            XSSFSheet performanceVSStrategicGoals = wb.getSheet("Performance VS Strategic Goals");
            performanceVSStrategicGoals.setForceFormulaRecalculation(true);

            XSSFSheet manuallyUpdatedStats = wb.getSheet("Manually Updated Stats");
            manuallyUpdatedStats.setForceFormulaRecalculation(true);

            XSSFSheet valuations = wb.getSheet("Valuations");
            valuations.setForceFormulaRecalculation(true);

            XSSFSheet statsA = wb.getSheet("Stats (A)");
            statsA.setForceFormulaRecalculation(true);

            XSSFSheet statsB = wb.getSheet("Stats (B)");
            statsB.setForceFormulaRecalculation(true);

            XSSFSheet rawData = wb.getSheet("Raw Data");

            // Get report data
            // Set date to now first
            List<Object[]> reportData = Job.getJobReportRecords(
                    em,
                    BusinessEntityUtils.getDateString(getReportingDatePeriod1().getStartDate(), "'", "YMD", "-"),
                    BusinessEntityUtils.getDateString(getReportingDatePeriod1().getEndDate(), "'", "YMD", "-"),
                    departmentId);

            // Fill in report data   
            for (Object[] rowData : reportData) {
                // Job number
                ReportUtils.setExcelCellValue(wb, rawData, row, 0,
                        (String) rowData[6],
                        "java.lang.String", stringCellStyle);
                // Client
                ReportUtils.setExcelCellValue(wb, rawData, row, 1,
                        (String) rowData[8],
                        "java.lang.String", stringCellStyle);
                // Business office
                ReportUtils.setExcelCellValue(wb, rawData, row, 2,
                        (String) rowData[11],
                        "java.lang.String", stringCellStyle);
                // Work progress
                ReportUtils.setExcelCellValue(wb, rawData, row, 3,
                        (String) rowData[12],
                        "java.lang.String", stringCellStyle);
                // Service(s)
                ReportUtils.setExcelCellValue(wb, rawData, row, 4,
                        (String) rowData[31],
                        "java.lang.String", stringCellStyle);
                // Instructions
                ReportUtils.setExcelCellValue(wb, rawData, row, 5,
                        (String) rowData[30],
                        "java.lang.String", stringCellStyle);
                // In-house?
                ReportUtils.setExcelCellValue(wb, rawData, row, 6,
                        (String) rowData[32],
                        "java.lang.String", stringCellStyle);
                // Classification
                ReportUtils.setExcelCellValue(wb, rawData, row, 7,
                        (String) rowData[13],
                        "java.lang.String", stringCellStyle);
                // Category
                ReportUtils.setExcelCellValue(wb, rawData, row, 8,
                        (String) rowData[14],
                        "java.lang.String", stringCellStyle);
                // Section (Subcategory)
                ReportUtils.setExcelCellValue(wb, rawData, row, 9,
                        (String) rowData[15],
                        "java.lang.String", stringCellStyle);
                // Sector
                ReportUtils.setExcelCellValue(wb, rawData, row, 10,
                        (String) rowData[16],
                        "java.lang.String", stringCellStyle);
                // Data entry department
                ReportUtils.setExcelCellValue(wb, rawData, row, 13,
                        (String) rowData[33],
                        "java.lang.String", stringCellStyle);
                // Assigned department
                ReportUtils.setExcelCellValue(wb, rawData, row, 14,
                        (String) rowData[9],
                        "java.lang.String", stringCellStyle);
                // Assigned department
                ReportUtils.setExcelCellValue(wb, rawData, row, 15,
                        (String) rowData[10],
                        "java.lang.String", stringCellStyle);
                // No. samples
                ReportUtils.setExcelCellValue(wb, rawData, row, 16,
                        (Long) rowData[5],
                        "java.lang.Long", longCellStyle);
                // No. products
                ReportUtils.setExcelCellValue(wb, rawData, row, 17,
                        (BigDecimal) rowData[34],
                        "java.math.BigDecimal", longCellStyle);
                // No. tests
                ReportUtils.setExcelCellValue(wb, rawData, row, 18,
                        (Integer) rowData[4],
                        "java.lang.Integer", integerCellStyle);
                // No. calibrations
                ReportUtils.setExcelCellValue(wb, rawData, row, 19,
                        (Integer) rowData[35],
                        "java.lang.Integer", integerCellStyle);
                // No. inspections
                ReportUtils.setExcelCellValue(wb, rawData, row, 20,
                        (Integer) rowData[36],
                        "java.lang.Integer", integerCellStyle);
                // No. trainings
                ReportUtils.setExcelCellValue(wb, rawData, row, 21,
                        (Integer) rowData[37],
                        "java.lang.Integer", integerCellStyle);
                // No. label assessments
                ReportUtils.setExcelCellValue(wb, rawData, row, 22,
                        (Integer) rowData[38],
                        "java.lang.Integer", integerCellStyle);
                // No. certifications
                ReportUtils.setExcelCellValue(wb, rawData, row, 23,
                        (Integer) rowData[39],
                        "java.lang.Integer", integerCellStyle);
                // No. consultations
                ReportUtils.setExcelCellValue(wb, rawData, row, 24,
                        (Integer) rowData[40],
                        "java.lang.Integer", integerCellStyle);
                // Other types of assessment
                ReportUtils.setExcelCellValue(wb, rawData, row, 25,
                        (String) rowData[42],
                        "java.lang.String", stringCellStyle);
                // No. other assessments
                ReportUtils.setExcelCellValue(wb, rawData, row, 26,
                        (Integer) rowData[41],
                        "java.lang.Integer", integerCellStyle);
                // Urgent?
                ReportUtils.setExcelCellValue(wb, rawData, row, 30,
                        (String) rowData[43],
                        "java.lang.String", integerCellStyle);
                // Total deposit
                ReportUtils.setExcelCellValue(wb, rawData, row, 31,
                        (Double) rowData[44],
                        "java.lang.Double", doubleCellStyle);
                // Amount due
                if ((rowData[27] != null) && (rowData[26] != null)) {
                    ReportUtils.setExcelCellValue(wb, rawData, row, 32,
                            (Double) rowData[27] - (Double) rowData[26],
                            "java.lang.Double", doubleCellStyle);
                }
                // Estimated cost
                ReportUtils.setExcelCellValue(wb, rawData, row, 33,
                        (Double) rowData[28],
                        "java.lang.Double", doubleCellStyle);
                // Final cost
                ReportUtils.setExcelCellValue(wb, rawData, row, 34,
                        (Double) rowData[27],
                        "java.lang.Double", doubleCellStyle);
                //  Job entry date
                ReportUtils.setExcelCellValue(wb, rawData, row, 36,
                        (Date) rowData[20],
                        "java.util.Date", dateCellStyle);
                //  Submission date 
                ReportUtils.setExcelCellValue(wb, rawData, row, 37,
                        (Date) rowData[29],
                        "java.util.Date", dateCellStyle);
                //  Expected date completion
                ReportUtils.setExcelCellValue(wb, rawData, row, 38,
                        (Date) rowData[17],
                        "java.util.Date", dateCellStyle);
                //  Completion date
                ReportUtils.setExcelCellValue(wb, rawData, row, 39,
                        (Date) rowData[19],
                        "java.util.Date", dateCellStyle);
                //  TAT given to client
                ReportUtils.setExcelCellValue(wb, rawData, row, 40,
                        (Integer) rowData[46],
                        "java.lang.Integer", integerCellStyle);
                //  Assignee
                ReportUtils.setExcelCellValue(wb, rawData, row, 41,
                        (String) rowData[21] + " " + (String) rowData[22],
                        "java.lang.String", stringCellStyle);
                //  Entered by firstname
                ReportUtils.setExcelCellValue(wb, rawData, row, 42,
                        (String) rowData[24],
                        "java.lang.String", stringCellStyle);
                //  Entered by lastname
                ReportUtils.setExcelCellValue(wb, rawData, row, 43,
                        (String) rowData[25],
                        "java.lang.String", stringCellStyle);
                //  List of samples
                ReportUtils.setExcelCellValue(wb, rawData, row, 44,
                        (String) rowData[0],
                        "java.lang.String", stringCellStyle);
                //  List of brands
                ReportUtils.setExcelCellValue(wb, rawData, row, 45,
                        (String) rowData[1],
                        "java.lang.String", stringCellStyle);
                //  List of models
                ReportUtils.setExcelCellValue(wb, rawData, row, 46,
                        (String) rowData[2],
                        "java.lang.String", stringCellStyle);
                //  Comment and results
                ReportUtils.setExcelCellValue(wb, rawData, row, 47,
                        (String) rowData[7],
                        "java.lang.String", stringCellStyle);
                // EDOC Ontime Status
                if (rowData[17] == null) {
                    status = "N/A";
                } else if (rowData[19] == null) {
                    status = "Not Yet Completed";
                } else if (((Date) rowData[17]).before((Date) rowData[19])) {
                    status = "Completed Late";
                } else if (((Date) rowData[17]).after((Date) rowData[19])
                        || ((Date) rowData[17]).equals((Date) rowData[19])) {
                    status = "Completed Early";
                } else {
                    status = "Not Yet Completed";
                }
                ReportUtils.setExcelCellValue(wb, rawData, row, 48,
                        status,
                        "java.lang.String", stringCellStyle);
                //  Expected start date
                ReportUtils.setExcelCellValue(wb, rawData, row, 49,
                        (Date) rowData[47],
                        "java.util.Date", dateCellStyle);
                //  Start date
                ReportUtils.setExcelCellValue(wb, rawData, row, 50,
                        (Date) rowData[48],
                        "java.util.Date", dateCellStyle);
                // ESD Ontime Status
                if (rowData[47] == null) {
                    status = "N/A";
                } else if (rowData[48] == null) {
                    status = "Not Yet Start";
                } else if (((Date) rowData[47]).before((Date) rowData[48])) {
                    status = "Started Late";
                } else if (((Date) rowData[47]).after((Date) rowData[48])
                        || ((Date) rowData[47]).equals((Date) rowData[48])) {
                    status = "Started Early";
                } else {
                    status = "Not Yet Started";
                }
                ReportUtils.setExcelCellValue(wb, rawData, row, 51,
                        status,
                        "java.lang.String", stringCellStyle);
                row++;

            }

            // Insert data at top of sheet
            //  Department name
            Department department = Department.findDepartmentById(em, departmentId);
            ReportUtils.setExcelCellValue(wb, rawData, 0, 1,
                    department.getName(),
                    "java.lang.String", stringCellStyle);
            //  Data starts at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 4,
                    getMonthlyReportDataDatePeriod().getStartDate(),
                    "java.util.Date", datePeriodsCellStyle);
            //  Data ends at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 6,
                    getMonthlyReportDataDatePeriod().getEndDate(),
                    "java.util.Date", datePeriodsCellStyle);
            //  Month starts at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 8,
                    getReportingDatePeriod2().getStartDate(),
                    "java.util.Date", datePeriodsCellStyle);
            //  Month ends at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 10,
                    getReportingDatePeriod2().getEndDate(),
                    "java.util.Date", datePeriodsCellStyle);
            // Year type
            ReportUtils.setExcelCellValue(wb, rawData, 0, 12,
                    getReportingDatePeriod3().getName(),
                    "java.lang.String", datePeriodsCellStyle);
            //  Year starts at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 15,
                    getReportingDatePeriod3().getStartDate(),
                    "java.util.Date", datePeriodsCellStyle);
            //  Year ends at:
            ReportUtils.setExcelCellValue(wb, rawData, 0, 17,
                    getReportingDatePeriod3().getEndDate(),
                    "java.util.Date", datePeriodsCellStyle);

            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    public ByteArrayInputStream createExcelComplianceMonthlyReportFileInputStream(
            EntityManager em,
            File reportFile,
            Long departmentId) {

        try {

            FileInputStream inp = new FileInputStream(reportFile);
            XSSFWorkbook wb = new XSSFWorkbook(inp);

            // Output stream for modified Excel file
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Get sheets ensure that crucial sheets are updated automatically
            XSSFSheet combined = wb.getSheet("Combined");
            combined.setForceFormulaRecalculation(true);

            XSSFSheet kingston = wb.getSheet("Kingston");
            kingston.setForceFormulaRecalculation(true);

            XSSFSheet mandeville = wb.getSheet("Mandeville");
            mandeville.setForceFormulaRecalculation(true);

            XSSFSheet ochoRios = wb.getSheet("Ocho Rios");
            ochoRios.setForceFormulaRecalculation(true);

            XSSFSheet savannaLaMar = wb.getSheet("Savanna-La-Mar");
            savannaLaMar.setForceFormulaRecalculation(true);

            XSSFSheet montegoBay = wb.getSheet("Montego Bay");
            montegoBay.setForceFormulaRecalculation(true);

            XSSFSheet valuations = wb.getSheet("Analyses");
            valuations.setForceFormulaRecalculation(true);

            getComplianceSurveyData(em, departmentId, wb);
            getComplaintData(em, departmentId, wb);
            getFactoryInspectionData(em, departmentId, wb);

            wb.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    private void getComplianceSurveyData(
            EntityManager em,
            Long departmentId,
            XSSFWorkbook wb) {

        int row = 2;
        XSSFSheet factoryInspectionsSheet = wb.getSheet("Surveys");
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFCellStyle stringCellStyle = wb.createCellStyle();
        stringCellStyle.setWrapText(true);
        XSSFCellStyle longCellStyle = wb.createCellStyle();
        XSSFCellStyle dateCellStyle = wb.createCellStyle();
        XSSFCellStyle datePeriodsCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("MMM dd, yyyy"));
        datePeriodsCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Set date to now first
        List<Object[]> reportData = ComplianceSurvey.getReportRecords(
                em,
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getStartDate(), "'", "YMD", "-"),
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getEndDate(), "'", "YMD", "-"),
                departmentId);
        // Fill in report data   
        for (Object[] rowData : reportData) {
            // Job number
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 0,
                    (String) rowData[0],
                    "java.lang.String", stringCellStyle);
            // Consignee
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 1,
                    (String) rowData[1],
                    "java.lang.String", stringCellStyle);
            // Comments
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 2,
                    (String) rowData[2],
                    "java.lang.String", stringCellStyle);
            // Business office
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 3,
                    (String) rowData[3],
                    "java.lang.String", stringCellStyle);
            // Entry doc #
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 4,
                    (String) rowData[4],
                    "java.lang.String", stringCellStyle);
            // Containers
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 5,
                    (String) rowData[5],
                    "java.lang.String", stringCellStyle);
            // Survey type
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 6,
                    (String) rowData[6],
                    "java.lang.String", stringCellStyle);
            // Survey location type
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 7,
                    (String) rowData[7],
                    "java.lang.String", stringCellStyle);
            // Type of establishment
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 8,
                    (String) rowData[8],
                    "java.lang.String", stringCellStyle);
            // Retail outlet
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 9,
                    (String) rowData[9],
                    "java.lang.String", stringCellStyle);
            // Date of survey
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 10,
                    (Date) rowData[10],
                    "java.util.Date", dateCellStyle);
            // Type of port of entry
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 11,
                    (String) rowData[11],
                    "java.lang.String", stringCellStyle);
            // Port of entry
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 12,
                    (String) rowData[12],
                    "java.lang.String", stringCellStyle);
            // Inspection point
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 13,
                    (String) rowData[13],
                    "java.lang.String", stringCellStyle);
            // Broker
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 14,
                    (String) rowData[14],
                    "java.lang.String", stringCellStyle);
            // Reason for detention
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 15,
                    (String) rowData[15],
                    "java.lang.String", stringCellStyle);
            // Standards breached
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 16,
                    (String) rowData[16],
                    "java.lang.String", stringCellStyle);
            // Work progress
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 17,
                    (String) rowData[17],
                    "java.lang.String", stringCellStyle);
            // Inspectors
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 18,
                    (String) rowData[18],
                    "java.lang.String", stringCellStyle);
            // Product quantity
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 19,
                    (BigDecimal) rowData[19],
                    "java.math.BigDecimal", longCellStyle);
            // Profile flagged
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 20,
                    (String) rowData[20],
                    "java.lang.String", stringCellStyle);
            // Commodity codes
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 21,
                    (String) rowData[21],
                    "java.lang.String", stringCellStyle);
            // Detentions
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 22,
                    (BigDecimal) rowData[22],
                    "java.math.BigDecimal", longCellStyle);
            // Destructions
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 23,
                    (BigDecimal) rowData[23],
                    "java.math.BigDecimal", longCellStyle);
            // Seizures
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 24,
                    (BigDecimal) rowData[24],
                    "java.math.BigDecimal", longCellStyle);
            // Condemnations
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 25,
                    (BigDecimal) rowData[25],
                    "java.math.BigDecimal", longCellStyle);
            // Verifications
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 26,
                    (BigDecimal) rowData[26],
                    "java.math.BigDecimal", longCellStyle);
            // Withdrawals
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 27,
                    (BigDecimal) rowData[27],
                    "java.math.BigDecimal", longCellStyle);
            // Products
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 28,
                    (String) rowData[28],
                    "java.lang.String", stringCellStyle);
            // Product categories
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 29,
                    (String) rowData[29],
                    "java.lang.String", stringCellStyle);
            row++;
        }
        // Insert data at top of sheet
        // Department name
        Department department = Department.findDepartmentById(em, departmentId);
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 1,
                department.getName(),
                "java.lang.String", stringCellStyle);
        //  Data starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 4,
                getMonthlyReportDataDatePeriod().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Data ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 6,
                getMonthlyReportDataDatePeriod().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 8,
                getReportingDatePeriod2().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 10,
                getReportingDatePeriod2().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        // Year type
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 12,
                getReportingDatePeriod3().getName(),
                "java.lang.String", datePeriodsCellStyle);
        //  Year starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 15,
                getReportingDatePeriod3().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Year ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 17,
                getReportingDatePeriod3().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
    }

    private void getComplaintData(
            EntityManager em,
            Long departmentId,
            XSSFWorkbook wb) {

        int row = 2;
        XSSFSheet factoryInspectionsSheet = wb.getSheet("Complaints");
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFCellStyle stringCellStyle = wb.createCellStyle();
        stringCellStyle.setWrapText(true);
        //XSSFCellStyle doubleCellStyle = wb.createCellStyle();
        XSSFCellStyle dateCellStyle = wb.createCellStyle();
        XSSFCellStyle datePeriodsCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("MMM dd, yyyy"));
        datePeriodsCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Set date to now first
        List<Object[]> reportData = Complaint.getReportRecords(
                em,
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getStartDate(), "'", "YMD", "-"),
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getEndDate(), "'", "YMD", "-"),
                departmentId);
        // Fill in report data   
        for (Object[] rowData : reportData) {
            // Job number
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 0,
                    (String) rowData[0],
                    "java.lang.String", stringCellStyle);
            // Business office
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 1,
                    (String) rowData[1],
                    "java.lang.String", stringCellStyle);
            // Comments
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 2,
                    (String) rowData[2],
                    "java.lang.String", stringCellStyle);
            // Entered by
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 3,
                    (String) rowData[3],
                    "java.lang.String", stringCellStyle);
            // Date received
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 4,
                    (Date) rowData[4],
                    "java.util.Date", dateCellStyle);
            // Complaint
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 5,
                    (String) rowData[5],
                    "java.lang.String", stringCellStyle);
            // Complainant
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 6,
                    (String) rowData[6],
                    "java.lang.String", stringCellStyle);

            row++;
        }
        // Insert data at top of sheet
        // Department name
        Department department = Department.findDepartmentById(em, departmentId);
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 1,
                department.getName(),
                "java.lang.String", stringCellStyle);
        //  Data starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 4,
                getMonthlyReportDataDatePeriod().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Data ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 6,
                getMonthlyReportDataDatePeriod().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 8,
                getReportingDatePeriod2().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 10,
                getReportingDatePeriod2().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        // Year type
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 12,
                getReportingDatePeriod3().getName(),
                "java.lang.String", datePeriodsCellStyle);
        //  Year starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 15,
                getReportingDatePeriod3().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Year ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 17,
                getReportingDatePeriod3().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
    }

    private void getFactoryInspectionData(
            EntityManager em,
            Long departmentId,
            XSSFWorkbook wb) {

        int row = 2;
        XSSFSheet factoryInspectionsSheet = wb.getSheet("Factory Inspections");
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFCellStyle stringCellStyle = wb.createCellStyle();
        stringCellStyle.setWrapText(true);
        XSSFCellStyle longCellStyle = wb.createCellStyle();
        XSSFCellStyle dateCellStyle = wb.createCellStyle();
        XSSFCellStyle datePeriodsCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("MMM dd, yyyy"));
        datePeriodsCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Set date to now first
        List<Object[]> reportData = FactoryInspection.getReportRecords(
                em,
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getStartDate(), "'", "YMD", "-"),
                BusinessEntityUtils.getDateString(getReportingDatePeriod1().getEndDate(), "'", "YMD", "-"),
                departmentId);
        // Fill in report data   
        for (Object[] rowData : reportData) {
            // Job number
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 0,
                    (String) rowData[0],
                    "java.lang.String", stringCellStyle);
            // Assigned inspector
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 1,
                    (String) rowData[1],
                    "java.lang.String", stringCellStyle);
            // General comments
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 2,
                    (String) rowData[2],
                    "java.lang.String", stringCellStyle);
            // Business office
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 3,
                    (String) rowData[3],
                    "java.lang.String", stringCellStyle);
            // Manufacturer
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 4,
                    (String) rowData[4],
                    "java.lang.String", stringCellStyle);
            // Inspection date
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 5,
                    (Date) rowData[5],
                    "java.util.Date", dateCellStyle);
            // Work progress
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 6,
                    (String) rowData[6],
                    "java.lang.String", stringCellStyle);
            // Work in progress
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 7,
                    (String) rowData[7],
                    "java.lang.String", stringCellStyle);
            // Product quantity
            ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, row, 8,
                    (BigDecimal) rowData[8],
                    "java.math.BigDecimal", longCellStyle);

            row++;
        }
        // Insert data at top of sheet
        // Department name
        Department department = Department.findDepartmentById(em, departmentId);
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 1,
                department.getName(),
                "java.lang.String", stringCellStyle);
        //  Data starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 4,
                getMonthlyReportDataDatePeriod().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Data ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 6,
                getMonthlyReportDataDatePeriod().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 8,
                getReportingDatePeriod2().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Month ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 10,
                getReportingDatePeriod2().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
        // Year type
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 12,
                getReportingDatePeriod3().getName(),
                "java.lang.String", datePeriodsCellStyle);
        //  Year starts at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 15,
                getReportingDatePeriod3().getStartDate(),
                "java.util.Date", datePeriodsCellStyle);
        //  Year ends at:
        ReportUtils.setExcelCellValue(wb, factoryInspectionsSheet, 0, 17,
                getReportingDatePeriod3().getEndDate(),
                "java.util.Date", datePeriodsCellStyle);
    }

    /**
     *
     * @param wb
     * @return
     */
//    HSSFCellStyle getDefaultCellStyle(HSSFWorkbook wb) {
//        HSSFCellStyle cellStyle = wb.createCellStyle();
//        Font font = wb.createFont();
//        font.setFontHeightInPoints((short) 12);
//        font.setFontName("Arial");
//        cellStyle.setFont(font);
//        cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
//        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//
//        return cellStyle;
//    }

    /**
     *
     * @param wb
     * @return
     */
//    Font getWingdingsFont(HSSFWorkbook wb) {
//        Font font = wb.createFont();
//        font.setFontHeightInPoints((short) 14);
//        font.setFontName("Wingdings");
//
//        return font;
//    }

    /**
     *
     * @param wb
     * @param fontName
     * @param fontsize
     * @return
     */
//    Font getFont(HSSFWorkbook wb, String fontName, short fontsize) {
//        Font font = wb.createFont();
//        font.setFontHeightInPoints(fontsize);
//        font.setFontName(fontName);
//
//        return font;
//    }

    /**
     *
     * @param samples
     * @param method
     * @return
     */
    public Boolean checkForSampleDisposalMethod(List<JobSample> samples, Integer method) {
        for (JobSample jobSample : samples) {
            if (jobSample.getMethodOfDisposal().compareTo(method) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param em
     * @param department
     * @param jobSubCategory
     */
    public void associateDepartmentWithJobSubCategory(
            EntityManager em,
            Department department,
            JobSubCategory jobSubCategory) {

        em.getTransaction().begin();
        jobSubCategory.getDepartments().add(department);
        BusinessEntityUtils.saveBusinessEntity(em, jobSubCategory);
        em.getTransaction().commit();

    }

    /**
     *
     * @param em
     * @param department
     * @param sector
     */
    public void associateDepartmentWithSector(
            EntityManager em,
            Department department,
            Sector sector) {

        em.getTransaction().begin();
        sector.getDepartments().add(department);
        BusinessEntityUtils.saveBusinessEntity(em, sector);
        em.getTransaction().commit();
    }

    /**
     *
     * @param em
     * @param department
     * @param jobReportItem
     */
    public void associateDepartmentWithJobReportItem(
            EntityManager em,
            Department department,
            JobReportItem jobReportItem) {

        em.getTransaction().begin();
        jobReportItem.getDepartments().add(department);
        BusinessEntityUtils.saveBusinessEntity(em, jobReportItem);
        em.getTransaction().commit();
    }

    /**
     *
     * @return
     */
    public ArrayList getDateSearchFields() {
        return DateUtils.getDateSearchFields(getSelectedReport().getCategory());
    }

    @Override
    public void doDefaultSearch( String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {
        switch (getSystemManager().getDashboard().getSelectedTabId()) {
            case "Report Management":

                break;
            default:
                break;
        }
    }

    @Override
    public void initDashboard() {
        
        if (getUser().hasModule("ReportModule")) {
            getSystemManager().getDashboard().openTab(getUser().
                    getActiveModule("ReportModule").getDashboardTitle());
        }
        
    }

    @Override
    public void initMainTabView() {

        if (getUser().hasModule("ReportModule")) {
            getMainTabView().openTab("Report Templates");
            getMainTabView().openTab(getUser().
                    getActiveModule("ReportModule").getMainViewTitle());
        }

    }

    @Override
    public void completeLogin() {
        initDashboard();
        initMainTabView();
    }

    @Override
    public void completeLogout() {
        reset();
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<SelectItem> getGroupedSearchTypes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getSearchType() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setSearchType(String searchType) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public DatePeriod getDateSearchPeriod() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setDateSearchPeriod(DatePeriod dateSearchPeriod) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void doSearch() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void handleKeepAlive() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getApplicationSubheader() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void initSearchPanel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void initSearchTypes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Manager getManager(String name) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<SelectItem> getAllDateSearchFields() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateSearchType() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateDateSearchField() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getSearchText() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setSearchText(String searchText) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public EntityManager getEntityManager2() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateAllForms() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onMainViewTabClose(TabCloseEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getAppShortcutIconURL() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean renderUserMenu() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getLogoURL() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Integer getLogoURLImageHeight() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Integer getLogoURLImageWidth() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onNotificationSelect(SelectEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
