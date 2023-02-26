/*
Legal Office (LO) 
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
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.Classification;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.rm.DocumentReport;
import jm.com.dpbennett.business.entity.dm.DocumentSequenceNumber;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.lo.LegalDocument;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.rm.manager.ReportManager;
import jm.com.dpbennett.sm.manager.Manager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.TabPanel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 *
 * @author Desmond Bennett
 */
public class LegalDocumentManager implements Serializable, Manager {

    private DatePeriod dateSearchPeriod;
    private String searchType;
    private String searchText;
    private List<LegalDocument> documentSearchResultList;
    private LegalDocument selectedDocument;
    private LegalDocument currentDocument;
    private DocumentReport documentReport;
    private String[] moduleNames;
    private User user;
    private String username;
    private String logonMessage;
    private String password;
    private Integer loginAttempts;
    private Boolean userLoggedIn;
    private String defaultCommandTarget;
    private ArrayList<SelectItem> groupedSearchTypes;
    private ArrayList<SelectItem> allDateSearchFields;

    public LegalDocumentManager() {
        init();
    }

    /**
     * Get application header.
     *
     * @return
     */
    @Override
    public String getApplicationHeader() {
        return "Legal Office";
    }

    public List getLegalDocumentSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Legal Documents", "Legal Documents"));

        return searchTypes;
    }

    public List<DocumentType> completeLegalDocumentType(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<DocumentType> documentTypes = DocumentType.findDocumentTypesByName(em, query);

            return documentTypes;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List getDocumentForms() {
        ArrayList forms = new ArrayList();

        forms.add(new SelectItem("E", "Electronic"));
        forms.add(new SelectItem("H", "Hard copy"));
        forms.add(new SelectItem("V", "Verbal"));

        return forms;
    }

    public List getPriorityLevels() {
        ArrayList levels = new ArrayList();

        levels.add(new SelectItem("--", "--"));
        levels.add(new SelectItem("High", "High"));
        levels.add(new SelectItem("Medium", "Medium"));
        levels.add(new SelectItem("Low", "Low"));
        levels.add(new SelectItem("Emergency", "Emergency"));

        return levels;
    }

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
                getOptionValueObject(getEntityManager1(), "StrategicPriorities");
        List<String> matchedPriority = new ArrayList<>();

        for (String priority : priorities) {
            if (priority.contains(query)) {
                matchedPriority.add(priority);
            }
        }

        return matchedPriority;

    }

//    public List getLegalDocumentDateSearchFields() {
//        ArrayList dateFields = new ArrayList();
//
//        // add items
//        dateFields.add(new SelectItem("dateOfCompletion", "Date delivered"));
//        dateFields.add(new SelectItem("dateReceived", "Date received"));
//        dateFields.add(new SelectItem("expectedDateOfCompletion", "Agreed delivery date"));
//
//        return dateFields;
//    }
    @Override
    public final void init() {
        reset();

    }

    public void openReportsTab() {
        getReportManager().openReportsTab("Legal");
    }

    @Override
    public void reset() {
        searchType = "Legal Documents";
        searchText = "";
        dateSearchPeriod = new DatePeriod("This month", "month", "dateReceived",
                null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();
        groupedSearchTypes = new ArrayList<>();
        allDateSearchFields = new ArrayList();
        moduleNames = new String[]{
            "systemManager",
            "legalDocumentManager"};
        password = "";
        username = "";
        loginAttempts = 0;
        userLoggedIn = false;
        logonMessage = "Please provide your login details below:";
        String theme = getUser().getPFThemeName();
        user = new User();
        user.setPFThemeName(theme);
        defaultCommandTarget = "@this";
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

    public Boolean getIsClientNameValid() {
        return BusinessEntityUtils.validateName(getCurrentDocument().getExternalClient().getName());
    }

    public void editExternalClient() {

        editClient();
    }

    public void externalClientDialogReturn() {

    }

    public void classificationDialogReturn() {

    }

    public void documentTypeDialogReturn() {
        if (getSystemManager().getSelectedDocumentType().getId() != null) {
            getCurrentDocument().setType(getSystemManager().getSelectedDocumentType());

            updateDocument();
        }
    }

    public void documentDialogReturn() {
        if (getCurrentDocument().getIsDirty()) {
            PrimeFacesUtils.addMessage("Document NOT Saved!", "", FacesMessage.SEVERITY_WARN);
        }
    }

    public void createNewExternalClient() {

        editClient();
    }

    public void editClient() {
        PrimeFacesUtils.openDialog(null, "/client/clientDialog", true, true, true, 600, 700);
    }

    @Override
    public DatePeriod getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    @Override
    public void setDateSearchPeriod(DatePeriod dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
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

        // Do search to update search list.
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

        PrimeFacesUtils.openDialog(null, "/legal/legalDocumentDialog", true, true, true, true, 600, 800);
    }

    public void deleteDocumentConfirmDialog() {
        PrimeFacesUtils.openDialog(null, "/legal/legalDocumentDeleteConfirmDialog", true, true, true, false, 125, 400);
    }

    public void editDocumentType(ActionEvent actionEvent) {

        getSystemManager().setSelectedDocumentType(getCurrentDocument().getType());
        getCurrentDocument().setType(null);
        getSystemManager().openDocumentTypeDialog("/admin/documentTypeDialog");
    }

    public void editClassification(ActionEvent actionEvent) {

        editClassification();
    }

    public void createNewClassification(ActionEvent actionEvent) {

        editClassification();
    }

    public void editClassification() {

        PrimeFacesUtils.openDialog(null, "/finance/classificationDialog", true, true, true, 500, 600);

    }

    public void createNewDocumentType(ActionEvent actionEvent) {
        getSystemManager().setSelectedDocumentType(new DocumentType());

        PrimeFacesUtils.openDialog(null, "/admin/documentTypeDialog", true, true, true, 275, 400);
    }

    public void saveCurrentLegalDocument(ActionEvent actionEvent) {

        if (currentDocument.getIsDirty()) {
            EntityManager em = getEntityManager1();

            try {
                if (DocumentSequenceNumber.findDocumentSequenceNumber(em,
                        currentDocument.getSequenceNumber(),
                        currentDocument.getYearReceived(),
                        currentDocument.getMonthReceived(),
                        currentDocument.getType().getId()) == null) {

                    currentDocument.setSequenceNumber(DocumentSequenceNumber.findNextDocumentSequenceNumber(em,
                            currentDocument.getYearReceived(),
                            currentDocument.getMonthReceived(),
                            currentDocument.getType().getId()));
                }

                if (currentDocument.getAutoGenerateNumber()) {
                    currentDocument.setNumber(LegalDocument.getLegalDocumentNumber(currentDocument, "ED"));
                }

                // Do save, set clean and dismiss dialog
                currentDocument.setEditedBy(getUser().getEmployee());
                if (currentDocument.save(em).isSuccess()) {
                    currentDocument.setIsDirty(false);
                }

                PrimeFaces.current().dialog().closeDynamic(null);

                // Redo search
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
            currentDocument.setResponsibleDepartment(Department.findDepartmentById(getEntityManager1(),
                    currentDocument.getResponsibleDepartment().getId()));
            if (currentDocument.getAutoGenerateNumber()) {
                currentDocument.setNumber(LegalDocument.getLegalDocumentNumber(currentDocument, "ED"));
            }
        }
    }

    public void updateDocumentReport() {
        if (documentReport.getId() != null) {
            documentReport = DocumentReport.findDocumentReportById(getEntityManager1(), documentReport.getId());
            doLegalDocumentSearch();
        }
    }

    public void createNewLegalDocument(ActionEvent action) {
        currentDocument = createNewLegalDocument(getEntityManager1(), getUser());
        editDocument();
    }

    public void openDocumentBrowser() {
        getMainTabView().openTab("Document Browser");
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public User getUser() {
        if (user == null) {
            user = new User();
        }

        return user;
    }

    public LegalDocument createNewLegalDocument(EntityManager em,
            User user) {

        LegalDocument legalDocument = new LegalDocument();
        legalDocument.setAutoGenerateNumber(Boolean.TRUE);

        if (getUser().getId() != null) {
            if (getUser().getEmployee() != null) {
                legalDocument.setResponsibleOfficer(Employee.findEmployeeById(em, getUser().getEmployee().getId()));
                legalDocument.setResponsibleDepartment(Department.findDepartmentById(em, getUser().getEmployee().getDepartment().getId()));
            }
        } else {
            legalDocument.setResponsibleOfficer(Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true));
            legalDocument.setResponsibleDepartment(Department.findDefaultDepartment(em, "--"));
        }

        legalDocument.setRequestingDepartment(Department.findDefaultDepartment(em, "--"));
        legalDocument.setSubmittedBy(Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true));
        legalDocument.setType(DocumentType.findDefaultDocumentType(em, "--"));
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
                    dateSearchPeriod, "By type", selectedDocument.getType().getName());
        } else {
            return new ArrayList<>();
        }
    }

    public void setDocumentSearchResultList(List<LegalDocument> documentSearchResultList) {
        this.documentSearchResultList = documentSearchResultList;
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
    public void updateSearch() {
        setDefaultCommandTarget("doSearch");
    }

    public void updateDatePeriodSearch() {
        getDateSearchPeriod().initDatePeriod();

        doLegalDocumentSearch();
    }

    public void doLegalDocumentSearch() {

        EntityManager em = getEntityManager1();

        documentSearchResultList = LegalDocument.findLegalDocumentsByDateSearchField(em,
                dateSearchPeriod, searchType, searchText.trim());
    }

    public void doLegalDocumentSearch(
            DatePeriod dateSearchPeriod,
            String searchType,
            String searchText) {

        this.dateSearchPeriod = dateSearchPeriod;
        this.searchType = searchType;
        this.searchText = searchText;

        doLegalDocumentSearch();
    }

    @Override
    public void doSearch() {

        for (String moduleName : moduleNames) {

            Modules module = Modules.findActiveModuleByName(
                    getEntityManager1(),
                    moduleName);

            if (getUser().hasModule(moduleName)) {
                if (module != null) {
                    Manager manager = getManager(module.getName());
                    if (manager != null) {
                        manager.doDefaultSearch(
                                getMainTabView(),
                                getDateSearchPeriod().getDateField(),
                                getSearchType(),
                                getSearchText(),
                                getDateSearchPeriod().getStartDate(),
                                getDateSearchPeriod().getEndDate());
                    }
                }
            }
        }

    }

    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");
    }

    public ReportManager getReportManager() {

        return BeanUtils.findBean("reportManager");
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
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        headerCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
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
        cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

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
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        getDateSearchPeriod().setDateField(dateSearchField);
        this.searchType = searchType;
        this.searchText = searchText;
        getDateSearchPeriod().setStartDate(startDate);
        getDateSearchPeriod().setEndDate(endDate);

        switch (searchType) {
            case "Legal Documents":
                doLegalDocumentSearch();
                openDocumentBrowser();
                break;
            default:
                break;
        }
    }

    @Override
    public void initDashboard() {

        initSearchPanel();
    }

    @Override
    public void initMainTabView() {

        getMainTabView().reset(getUser());

        for (String moduleName : moduleNames) {
            Modules module = Modules.findActiveModuleByName(getEntityManager1(),
                    moduleName);
            if (module != null) {
                if (getUser().hasModule(moduleName)) {
                    getMainTabView().openTab(module.getDashboardTitle());
                }
            }
        }
    }

    @Override
    public void completeLogin() {
        getUser().logActivity("Logged in", getEntityManager1());

        getUser().save(getEntityManager1());

        getSystemManager().setUser(getUser());

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initDashboard();

        initMainTabView();

        updateAllForms();
    }

    @Override
    public void completeLogout() {
        getDashboard().removeAllTabs();
        getMainTabView().removeAllTabs();

        getSystemManager().setUser(getUser());
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Legal");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getGroupedSearchTypes() {
        return groupedSearchTypes;
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Legal Documents", "Legal Documents"));

        return searchTypes;
    }

    @Override
    public void handleKeepAlive() {
        getUser().setPollTime(new Date());

        if (SystemOption.getBoolean(getEntityManager1(), "debugMode")) {
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
        return "Legal Office Administration &amp; Management";
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

        for (String moduleName : moduleNames) {

            Modules module = Modules.findActiveModuleByName(
                    getEntityManager1(),
                    moduleName);

            if (getUser().hasModule(moduleName)) {
                if (module != null) {
                    Manager manager = getManager(module.getName());
                    if (manager != null) {
                        groupedSearchTypes.add(manager.getSearchTypesGroup());
                        searchType = manager.getSearchType();
                    }
                }
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
        for (String moduleName : moduleNames) {

            Modules module = Modules.findActiveModuleByName(
                    getEntityManager1(),
                    moduleName);

            if (getUser().hasModule(moduleName)) {
                if (module != null) {
                    Manager manager = getManager(module.getName());
                    if (manager != null) {
                        ArrayList<SelectItem> dateFields = manager.getDateSearchFields(searchType);
                        if (!dateFields.isEmpty()) {
                            allDateSearchFields = dateFields;

                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateDateSearchField() {
    }

    @Override
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
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
        //String tabTitle = event.getTab().getTitle();

        //System.out.println("Tab change: " + tabTitle);
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

    @Override
    public void login() {
        login(getEntityManager1());
    }

    @Override
    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    @Override
    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    @Override
    public Boolean getUserLoggedIn() {
        return userLoggedIn;
    }

    @Override
    public void setUserLoggedIn(Boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public User getUser(EntityManager em) {
        if (user == null) {
            return new User();

        } else {
            try {
                if (user.getId() != null) {
                    User foundUser = em.find(User.class,
                            user.getId());
                    if (foundUser != null) {
                        em.refresh(foundUser);
                        user = foundUser;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                return new User();
            }
        }

        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Boolean checkForLDAPUser(EntityManager em, String username, LdapContext ctx) {
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"displayName"};

            constraints.setReturningAttributes(attrIDs);

            String name = (String) SystemOption.getOptionValueObject(em, "ldapContextName");
            NamingEnumeration answer = ctx.search(name, "SAMAccountName=" + username, constraints);

            if (!answer.hasMore()) { // Assuming only one match
                // LDAP user not found!
                return false;
            }
        } catch (NamingException ex) {
            System.out.println(ex);
            return false;
        }

        return true;
    }

    @Override
    public Boolean validateUser(EntityManager em) {
        Boolean userValidated = false;
        InitialLdapContext ctx;

        try {
            List<jm.com.dpbennett.business.entity.sm.LdapContext> ctxs = jm.com.dpbennett.business.entity.sm.LdapContext.findAllActiveLdapContexts(em);

            for (jm.com.dpbennett.business.entity.sm.LdapContext ldapContext : ctxs) {
                if (ldapContext.getName().equals("LDAP")) {
                    userValidated = jm.com.dpbennett.business.entity.sm.LdapContext.authenticateUser(
                            em,
                            ldapContext,
                            username,
                            password);
                } else {
                    ctx = ldapContext.getInitialLDAPContext(username, password);

                    if (ctx != null) {
                        if (checkForLDAPUser(em, username, ctx)) {
                            // user exists in LDAP                    
                            userValidated = true;
                            break;
                        }
                    }
                }
            }

            // Get the user if one exists
            if (userValidated) {
                System.out.println("User validated.");

                return true;

            } else {
                System.out.println("User NOT validated!");

                return false;
            }

        } catch (Exception e) {
            System.err.println("Problem connecting to directory: " + e);
        }

        return false;
    }

    @Override
    public void checkLoginAttemps() {
        ++loginAttempts;
        if (loginAttempts == 2) {

            try {
                // Send email to system administrator alert if activated
                if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(),
                        "developerEmailAlertActivated")) {
                    MailUtils.postMail(null, null, null,
                            "Failed user login",
                            "Username: " + username + "\nDate/Time: " + new Date(),
                            "text/plain",
                            getEntityManager1());
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else if (loginAttempts > 2) {// tk # attempts to be made option
            PrimeFaces.current().executeScript("PF('loginAttemptsDialog').show();");
        }

        username = "";
        password = "";
    }

    @Override
    public void login(EntityManager em) {
        setUserLoggedIn(false);

        try {

            // Find user and determine if authentication is required for this user
            user = User.findActiveJobManagerUserByUsername(em, username);

            if (user != null) {
                em.refresh(user);
                if (!user.getAuthenticate()) {
                    System.out.println("User will NOT be authenticated.");
                    logonMessage = "Please provide your login details below:";
                    username = "";
                    password = "";
                    setUserLoggedIn(true);

                    completeLogin();

                    PrimeFaces.current().executeScript("PF('loginDialog').hide();");
                } else if (validateUser(em)) {
                    logonMessage = "Please provide your login details below:";
                    username = "";
                    password = "";
                    setUserLoggedIn(true);

                    completeLogin();

                } else {
                    setUserLoggedIn(false);
                    checkLoginAttemps();
                    logonMessage = "Please enter a valid username and password.";
                }
            } else {
                setUserLoggedIn(false);
                logonMessage = "Please enter a registered username.";
                username = "";
                password = "";
            }

        } catch (Exception e) {
            setUserLoggedIn(false);
            System.out.println(e);
            logonMessage = "Login error occurred! Please try again or contact the System Administrator";
        }
    }

    @Override
    public String getLogonMessage() {
        return logonMessage;
    }

    @Override
    public void setLogonMessage(String logonMessage) {
        this.logonMessage = logonMessage;
    }

    @Override
    public void doDefaultCommand() {

        switch (defaultCommandTarget) {
            case "doSearch":
                doSearch();
                break;
            default:
                PrimeFacesUtils.addMessage("Action NOT Taken",
                        "No action was taken. Enter search text if you are doing a search.",
                        FacesMessage.SEVERITY_INFO);
                PrimeFaces.current().ajax().update("appForm:growl3");
                break;
        }
    }

    @Override
    public String getDefaultCommandTarget() {
        return defaultCommandTarget;
    }

    @Override
    public void setDefaultCommandTarget(String defaultCommandTarget) {
        this.defaultCommandTarget = defaultCommandTarget;
    }

    @Override
    public Dashboard getDashboard() {
        return getSystemManager().getDashboard();
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

}
