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
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.manager.Manager;
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

        searchTypes.add(new SelectItem("Legal documents", "Legal documents"));

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

    public List getLegalDocumentDateSearchFields() {
        ArrayList dateFields = new ArrayList();

        // add items
        dateFields.add(new SelectItem("dateOfCompletion", "Date delivered"));
        dateFields.add(new SelectItem("dateReceived", "Date received"));
        dateFields.add(new SelectItem("expectedDateOfCompletion", "Agreed delivery date"));

        return dateFields;
    }

    @Override
    public final void init() {
        reset();

    }

    public void openReportsTab() {
        //getReportManager().openReportsTab("Legal");
    }

    @Override
    public void reset() {
        searchType = "Legal documents";
        dateSearchPeriod = new DatePeriod("This month", "month", "dateReceived", null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();
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

    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public User getUser() {
        return getSystemManager().getUser();
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

    public void updateSearch() {
        switch (searchType) {
            case "Legal documents":
                doLegalDocumentSearch();
                break;
            default:
                doLegalDocumentSearch();
                break;
        }
    }

    public void updateDatePeriodSearch() {
        getDateSearchPeriod().initDatePeriod();

        doLegalDocumentSearch();
    }

    public void doLegalDocumentSearch() {

        EntityManager em = getEntityManager1();

        if (searchText != null) {
            documentSearchResultList = LegalDocument.findLegalDocumentsByDateSearchField(em,
                    dateSearchPeriod, searchType, searchText.trim());
        } else { // get all documents based on common test ie "" for now
            documentSearchResultList = LegalDocument.findLegalDocumentsByDateSearchField(em,
                    dateSearchPeriod, searchType, "");
        }

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

        switch (searchType) {
            case "Legal documents":
                doLegalDocumentSearch();
                openDocumentBrowser();
                break;
            default:
                break;
        }

    }

    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");
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
    public void doDefaultSearch( String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {
        //doSearch();
    }

    @Override
    public void initDashboard() {

        if (getUser().hasModule("LegalOfficeModule")) {
            getSystemManager().getDashboard().openTab(getUser().
                    getActiveModule("LegalOfficeModule").getDashboardTitle());
        }

    }

    @Override
    public void initMainTabView() {

        if (getUser().hasModule("LegalOfficeModule")) {
            getMainTabView().openTab(getUser().
                    getActiveModule("LegalOfficeModule").getMainViewTitle());
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
    public ArrayList<SelectItem> getSearchTypes() {
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
    public ArrayList<SelectItem> getDatePeriods() {
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
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
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