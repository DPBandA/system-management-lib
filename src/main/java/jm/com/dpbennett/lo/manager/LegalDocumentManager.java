/*
LegalOffice (LO) 
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
package jm.com.dpbennett.lo.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.Classification;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.rm.DocumentReport;
import jm.com.dpbennett.business.entity.dm.DocumentSequenceNumber;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.lo.LegalDocument;
import jm.com.dpbennett.business.entity.sm.Module;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.rm.manager.ReportManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public class LegalDocumentManager extends GeneralManager implements Serializable {

    private List<LegalDocument> documentSearchResultList;
    private LegalDocument selectedDocument;
    private LegalDocument currentDocument;
    private DocumentReport documentReport;
    private String searchText;
    private String searchType;

    public LegalDocumentManager() {
        init();
    }
    
    public Employee getEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();
        
        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    public void openClientsTab() {

        getMainTabView().openTab("Clients");
    }

    @Override
    public void initMainTabView() {

        String firstModule;
        firstModule = null;

        getMainTabView().reset(getUser());

        // Legal documents
        if (getUser().hasModule("legalDocumentManager")) {
            Module module = Module.findActiveModuleByName(
                    getSystemManager().getEntityManager1(),
                    "legalDocumentManager");
            if (module != null) {
                openModuleMainTab("legalDocumentManager");

                if (firstModule == null) {
                    firstModule = "legalDocumentManager";
                }
            }
        }

        // Clients
        if (getUser().hasModule("clientManager")) {
            Module module = Module.findActiveModuleByName(
                    getSystemManager().getEntityManager1(),
                    "clientManager");
            if (module != null) {
                openModuleMainTab("clientManager");

                if (firstModule == null) {
                    firstModule = "clientManager";
                }
            }
        }

        openModuleMainTab(firstModule);
    }

    private void openModuleMainTab(String moduleName) {

        if (moduleName != null) {
            switch (moduleName) {
                case "clientManager":
                    getClientManager().openClientsTab();
                    break;
                case "legalDocumentManager":
                    openDocumentBrowser();
                    break;
                case "reportManager":
                    getReportManager().openReportTemplatesTab();
                    break;
                default:
                    break;
            }
        }
    }

    public ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");
    }

    public HumanResourceManager getHumanResourceManager() {
        return BeanUtils.findBean("humanResourceManager");
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Document Browser":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:legalDocumentSearchButton");

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
    public String getSearchType() {
        return searchType;
    }

    @Override
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    @Override
    public String getApplicationHeader() {
        return "LegalOffice";
    }

    public List getLegalDocumentSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Legal Documents", "Legal Documents"));

        return searchTypes;
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 600;
    }

    // tk make system option
    public List getDocumentForms() {
        ArrayList forms = new ArrayList();

        forms.add(new SelectItem("E", "Electronic"));
        forms.add(new SelectItem("H", "Hard copy"));
        forms.add(new SelectItem("V", "Verbal"));

        return forms;
    }

    // tk make system option
    public List getPriorityLevels() {
        ArrayList levels = new ArrayList();

        levels.add(new SelectItem("--", "--"));
        levels.add(new SelectItem("High", "High"));
        levels.add(new SelectItem("Medium", "Medium"));
        levels.add(new SelectItem("Low", "Low"));
        levels.add(new SelectItem("Emergency", "Emergency"));

        return levels;
    }

    // tk make system option
    public List getDocumentStatuses() {
        ArrayList statuses = new ArrayList();

        statuses.add(new SelectItem("--", "--"));
        statuses.add(new SelectItem("Clarification required", "Clarification required"));
        statuses.add(new SelectItem("Completed", "Completed"));
        statuses.add(new SelectItem("On target", "On target"));
        statuses.add(new SelectItem("Transferred to Ministry", "Transferred to Ministry"));

        return statuses;
    }

    public List<String> completeStrategicPriority(String query) {

        List<String> priorities = (List<String>) SystemOption.
                getOptionValueObject(getSystemManager().getEntityManager1(), "StrategicPriorities");
        List<String> matchedPriority = new ArrayList<>();

        for (String priority : priorities) {
            if (priority.contains(query)) {
                matchedPriority.add(priority);
            }
        }

        return matchedPriority;

    }

    @Override
    public final void init() {
        reset();

    }

    public void openReportsTab() {
        getReportManager().openReportsTab("Legal");
    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Legal Documents");
        setSearchText("");
        setModuleNames(new String[]{
            "systemManager",
            "humanResourceManager",
            "reportManager",
            "legalDocumentManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "dateReceived", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

    }

    public void onRowSelect() {
        setDefaultCommandTarget("@this");
    }

    public Boolean getIsClientNameValid() {
        return BusinessEntityUtils.validateText(getCurrentDocument().getExternalClient().getName());
    }

    public void editExternalClient() {

        getClientManager().setSelectedClient(getCurrentDocument().getExternalClient());
        getClientManager().setClientDialogTitle("Client Detail");

        getClientManager().editSelectedClient();

    }

    public void externalClientDialogReturn() {

        if (getClientManager().getSelectedClient().getId() != null) {
            getCurrentDocument().setExternalClient(getClientManager().getSelectedClient());

            getCurrentDocument().setIsDirty(true);
        }

    }

    public void classificationDialogReturn() {

        if (getFinanceManager().getSelectedClassification().getId() != null) {
            getCurrentDocument().setClassification(getFinanceManager().getSelectedClassification());

            updateDocument();
        }

    }

    public void documentTypeDialogReturn() {
        if (getSystemManager().getSelectedDocumentType().getId() != null) {
            getCurrentDocument().setDocumentType(getSystemManager().getSelectedDocumentType());

            updateDocument();
        }
    }

    public void documentDialogReturn() {
        if (getCurrentDocument().getIsDirty()) {
            PrimeFacesUtils.addMessage("Document NOT Saved!", "", FacesMessage.SEVERITY_WARN);
        }
    }

    public void createNewExternalClient() {

        getClientManager().createNewClient(true);
        getClientManager().setClientDialogTitle("Client Detail");

        getClientManager().editSelectedClient();

    }

    public DocumentReport getDocumentReport() {
        if (documentReport == null) {
            documentReport = new DocumentReport();
        }
        return documentReport;
    }

    public void setDocumentReport(DocumentReport documentReport) {
        this.documentReport = documentReport;
    }

    public void handleStartSearchDateSelect(SelectEvent event) {
        doLegalDocumentSearch();
    }

    public void handleEndSearchDateSelect(SelectEvent event) {
        doLegalDocumentSearch();
    }

    public int getNumberOfDocumentsFound() {
        if (documentSearchResultList != null) {
            return documentSearchResultList.size();
        } else {
            return 0;
        }
    }

    public void deleteDocument() {
        EntityManager em = getEntityManager1();

        em.getTransaction().begin();
        LegalDocument document = em.find(LegalDocument.class, selectedDocument.getId());
        em.remove(document);
        em.flush();
        em.getTransaction().commit();

        doLegalDocumentSearch();

        closeDialog(null);
    }

    public void cancelDocumentEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void editDocument() {
        getCurrentDocument().setIsDirty(false);

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

        PrimeFaces.current().dialog().openDynamic("/legal/legalDocumentDialog", options, null);

    }

    public void deleteDocumentConfirmDialog() {

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

        PrimeFaces.current().dialog().openDynamic("/legal/legalDocumentDeleteConfirmDialog", options, null);

    }

    public void editDocumentType(ActionEvent actionEvent) {

        getSystemManager().setSelectedDocumentType(getCurrentDocument().getDocumentType());
        getCurrentDocument().setDocumentType(null);

        getSystemManager().editDocumentType();

    }

    public void editClassification(ActionEvent actionEvent) {

        getFinanceManager().setSelectedClassification(getCurrentDocument().getClassification());
        getFinanceManager().editClassification();

    }

    public void createNewClassification(ActionEvent actionEvent) {

        getFinanceManager().setSelectedClassification(new Classification());
        getFinanceManager().getSelectedClassification().setCategory("Legal");
        getFinanceManager().editClassification();

    }

    public void createNewDocumentType(ActionEvent actionEvent) {
        getSystemManager().setSelectedDocumentType(new DocumentType());

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

        PrimeFaces.current().dialog().openDynamic("/admin/documentTypeDialog", options, null);

    }

    public void saveCurrentLegalDocument(ActionEvent actionEvent) {

        if (currentDocument.getIsDirty()) {
            EntityManager em = getEntityManager1();

            try {
                if (DocumentSequenceNumber.findDocumentSequenceNumber(em,
                        currentDocument.getSequenceNumber(),
                        currentDocument.getYearReceived(),
                        currentDocument.getMonthReceived(),
                        currentDocument.getDocumentType().getId()) == null) {

                    currentDocument.setSequenceNumber(DocumentSequenceNumber.findNextDocumentSequenceNumber(em,
                            currentDocument.getYearReceived(),
                            currentDocument.getMonthReceived(),
                            currentDocument.getDocumentType().getId()));
                }

                if (currentDocument.getAutoGenerateNumber()) {
                    currentDocument.setNumber(LegalDocument.getLegalDocumentNumber(currentDocument, "ED"));
                }

                currentDocument.setEditedBy(getEmployee());
                if (currentDocument.save(em).isSuccess()) {
                    currentDocument.setIsDirty(false);
                }

                PrimeFaces.current().dialog().closeDynamic(null);

                doLegalDocumentSearch();

            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            PrimeFaces.current().dialog().closeDynamic(null);
        }
    }

    public void updateDocument() {

        if (currentDocument.getAutoGenerateNumber()) {
            currentDocument.setNumber(LegalDocument.getLegalDocumentNumber(currentDocument, "ED"));
        }

        getCurrentDocument().setIsDirty(true);
    }

    public void updateDateReceived() {
        Calendar c = Calendar.getInstance();

        c.setTime(currentDocument.getDateReceived());
        currentDocument.setMonthReceived(c.get(Calendar.MONTH));
        currentDocument.setYearReceived(c.get(Calendar.YEAR));

        updateDocument();
    }

    public void updateDepartmentResponsible() {
        if (currentDocument.getResponsibleDepartment().getId() != null) {
            currentDocument.setResponsibleDepartment(Department.findById(getHumanResourceManager().getEntityManager1(),
                    currentDocument.getResponsibleDepartment().getId()));
            if (currentDocument.getAutoGenerateNumber()) {
                currentDocument.setNumber(LegalDocument.getLegalDocumentNumber(currentDocument, "ED"));
            }
        }
    }

    public void updateDocumentReport() {
        if (documentReport.getId() != null) {
            documentReport = DocumentReport.findDocumentReportById(getReportManager().getEntityManager1(), documentReport.getId());
            doLegalDocumentSearch();
        }
    }

    public void createNewLegalDocument(ActionEvent action) {
        currentDocument = createNewLegalDocument(getEntityManager1(), getUser());
        editDocument();
    }

    public void openDocumentBrowser() {
        getMainTabView().openTab("Document Browser");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:legalDocumentSearchButton");
    }

    public LegalDocument createNewLegalDocument(EntityManager em,
            User user) {

        LegalDocument legalDocument = new LegalDocument();
        legalDocument.setAutoGenerateNumber(Boolean.TRUE);

        if (getUser().getId() != null) {
            if (getEmployee() != null) {
                legalDocument.setResponsibleOfficer(Employee.findById(em, getEmployee().getId()));
                legalDocument.setResponsibleDepartment(Department.findById(em, getEmployee().getDepartment().getId()));
            }
        } else {
            legalDocument.setResponsibleOfficer(Employee.findDefault(getHumanResourceManager().getEntityManager1(), "--", "--", true));
            legalDocument.setResponsibleDepartment(Department.findDefault(em, "--"));
        }

        legalDocument.setRequestingDepartment(Department.findDefault(em, "--"));
        legalDocument.setSubmittedBy(Employee.findDefault(getHumanResourceManager().getEntityManager1(), "--", "--", true));
        legalDocument.setDocumentType(DocumentType.findDefaultDocumentType(em, "--"));
        legalDocument.setClassification(Classification.findClassificationByName(em, "--"));
        legalDocument.setDocumentForm("H");
        legalDocument.setNumber(LegalDocument.getLegalDocumentNumber(legalDocument, "ED"));
        legalDocument.setDateReceived(new Date());

        return legalDocument;
    }

    public LegalDocument getCurrentDocument() {
        if (currentDocument == null) {
            currentDocument = createNewLegalDocument(getEntityManager1(), getUser());
        }

        return currentDocument;
    }

    public void setTargetDocument(LegalDocument legalDocument) {
        currentDocument = legalDocument;
    }

    public void setCurrentDocument(LegalDocument currentDocument) {

        this.currentDocument = LegalDocument.findLegalDocumentById(getEntityManager1(),
                currentDocument.getId());

        this.currentDocument.setVisited(true);
    }

    public LegalDocument getSelectedDocument() {

        return selectedDocument;
    }

    public void setSelectedDocument(LegalDocument selectedDocument) {

        this.selectedDocument = selectedDocument;
    }

    public List<LegalDocument> getDocumentSearchResultList() {
        return documentSearchResultList;
    }

    public List<LegalDocument> getDocumentSearchByTypeResultList() {
        EntityManager em = getEntityManager1();

        if (selectedDocument != null) {
            return LegalDocument.findLegalDocumentsByDateSearchField(em,
                    getDateSearchPeriod(), "By type", selectedDocument.getDocumentType().getName());
        } else {
            return new ArrayList<>();
        }
    }

    public void setDocumentSearchResultList(List<LegalDocument> documentSearchResultList) {
        this.documentSearchResultList = documentSearchResultList;
    }

    public void updateDatePeriodSearch() {
        getDateSearchPeriod().initDatePeriod();

        doLegalDocumentSearch();
    }

    public void doLegalDocumentSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Legal Documents",
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
            case "Legal Documents":
                documentSearchResultList = LegalDocument.findLegalDocumentsByDateSearchField(
                        getEntityManager1(),
                        getDateSearchPeriod(),
                        searchType,
                        searchText);

                break;
            default:
                break;
        }
    }

    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");
    }

    public ReportManager getReportManager() {

        return BeanUtils.findBean("reportManager");
    }

    public FinanceManager getFinanceManager() {

        return BeanUtils.findBean("financeManager");
    }

    public void formatDocumentTableXLS(Object document, String headerTitle) {
        HSSFWorkbook wb = (HSSFWorkbook) document;
        HSSFSheet sheet = wb.getSheetAt(0);
        // get columns row
        int numCols = sheet.getRow(0).getPhysicalNumberOfCells();
        // create heading row
        sheet.shiftRows(0, sheet.getLastRowNum(), 1);

        HSSFRow header = sheet.getRow(0);
        HSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);
        HSSFCellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        for (int i = 0; i < numCols; i++) {
            header.createCell(i);
            HSSFCell cell = header.getCell(i);
            cell.setCellStyle(headerCellStyle);
        }
        header.getCell(0).setCellValue(headerTitle);
        // merge header cells
        sheet.addMergedRegion(new CellRangeAddress(
                0, //first row
                (short) 0, //last row
                0, //first column
                (short) (numCols - 1) //last column
        ));

        // Column setup
        // get columns row
        HSSFRow cols = sheet.getRow(1);
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // set columns widths
        for (int i = 0; i < cols.getPhysicalNumberOfCells(); i++) {

            sheet.autoSizeColumn(i);

            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }

        }
        // set columns cell style
        for (int i = 0; i < cols.getPhysicalNumberOfCells(); i++) {
            HSSFCell cell = cols.getCell(i);
            cell.setCellStyle(cellStyle);
        }
    }

    public void postProcessDocumentTableXLS(Object document) {
        formatDocumentTableXLS(document, "Document by group");
    }

    public void postProcessXLS(Object document) {
        formatDocumentTableXLS(document, documentReport.getName());
    }

    public List<String> completeGoal(String query) {
        // tk put in sys options
        String goals[] = {"# 1", "# 2", "# 3", "# 4", "# 5"};
        List<String> matchedGoals = new ArrayList<>();

        for (String goal : goals) {
            if (goal.contains(query)) {
                matchedGoals.add(goal);
            }
        }

        return matchedGoals;

    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Legal");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Legal Documents", "Legal Documents"));

        return searchTypes;
    }

    @Override
    public String getApplicationSubheader() {
        return "Legal Office Administration";
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("LOEM");
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
    }

    @Override
    public String getAppShortcutIconURL() {

        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "appShortcutIconURL");

    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURL");
    }

    @Override
    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURLImageHeight");
    }

    @Override
    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURLImageWidth");
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
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Legal Documents":
                dateSearchFields.add(new SelectItem("dateOfCompletion", "Date delivered"));
                dateSearchFields.add(new SelectItem("dateReceived", "Date received"));
                dateSearchFields.add(new SelectItem("expectedDateOfCompletion", "Agreed delivery date"));

                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    public ArrayList<SelectItem> getDateSearchFields() {

        return getDateSearchFields("Legal Documents");
    }

    @Override
    public void handleSelectedNotification(Notification notification) {
        switch (notification.getType()) {
            case "LegalDocumentSearch":

                break;

            default:
                System.out.println("Unkown type");
        }
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public void handleKeepAlive() {

        updateUserActivity("LOv"
                + SystemOption.getString(
                        getSystemManager().getEntityManager1(), "LOv"),
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
    public void login() {
        login(getSystemManager().getEntityManager1());
    }
    
    @Override
    public void logout() {
        completeLogout();
    }

    @Override
    public void completeLogout() {

        updateUserActivity("LOv"
                + SystemOption.getString(getSystemManager().getEntityManager1(), "LOv"),
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
            updateUserActivity("LOv"
                    + SystemOption.getString(getSystemManager().getEntityManager1(), "LOv"),
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
        getManager("humanResourceManager").setUser(getUser());
        getManager("reportManager").setUser(getUser());

    }

}