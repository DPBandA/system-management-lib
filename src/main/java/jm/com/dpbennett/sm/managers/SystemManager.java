/*
System Management
Copyright (C) 2017  D P Bennett & Associates Limited

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
package jm.com.dpbennett.sm.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.AccountingCode;
import jm.com.dpbennett.business.entity.BusinessOffice;
import jm.com.dpbennett.business.entity.Classification;
import jm.com.dpbennett.business.entity.Country;
import jm.com.dpbennett.business.entity.DatePeriod;
import jm.com.dpbennett.business.entity.Department;
import jm.com.dpbennett.business.entity.DocumentReport;
import jm.com.dpbennett.business.entity.DocumentStandard;
import jm.com.dpbennett.business.entity.DocumentType;
import jm.com.dpbennett.business.entity.Employee;
import jm.com.dpbennett.business.entity.JobManagerUser;
import jm.com.dpbennett.business.entity.LdapContext;
import jm.com.dpbennett.business.entity.Preference;
import jm.com.dpbennett.business.entity.Sector;
import jm.com.dpbennett.business.entity.Service;
import jm.com.dpbennett.business.entity.SystemOption;
import jm.com.dpbennett.business.entity.Tax;
import jm.com.dpbennett.business.entity.utils.BusinessEntityUtils;
import jm.com.dpbennett.sm.Authentication;
import jm.com.dpbennett.sm.utils.BeanUtils;
import jm.com.dpbennett.sm.utils.Dashboard;
import jm.com.dpbennett.sm.utils.DateUtils;
import jm.com.dpbennett.sm.utils.Utils;
import jm.com.dpbennett.sm.utils.MainTabView;
import jm.com.dpbennett.sm.utils.PrimeFacesUtils;
import jm.com.dpbennett.sm.utils.TabPanel;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;
import org.primefaces.event.ToggleEvent;

/**
 *
 * @author Desmond Bennett
 */
public class SystemManager implements Serializable, 
        Authentication.AuthenticationListener {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;
    private MainTabView mainTabView;
    private int activeTabIndex;
    private int activeNavigationTabIndex;
    private String activeTabForm;
    private Tab activeTab;
    private String dateSearchField;
    private String dateSearchPeriod;
    private String searchType;
    private Boolean startSearchDateDisabled;
    private Boolean endSearchDateDisabled;
    private Boolean privilegeValue;
    private Boolean searchTextVisible;
    private Boolean isActiveUsersOnly;
    private Boolean isActiveClassificationsOnly;
    private Boolean isActiveSectorsOnly;
    private Boolean isActiveServicesOnly;
    private Boolean isActiveLdapsOnly;
    private Boolean isActiveDocumentTypesOnly;
    private Date startDate;
    private Date endDate;
    private JobManagerUser selectedUser;
    private JobManagerUser foundUser;
    private String searchText;
    private String userSearchText;
    private String generalSearchText;
    private String systemOptionSearchText;
    private String classificationSearchText;
    private String sectorSearchText;
    private String serviceSearchText;
    private String ldapSearchText;
    private String documentTypeSearchText;
    private List<JobManagerUser> foundUsers;
    private List<SystemOption> foundSystemOptions;
    private List<LdapContext> foundLdapContexts;
    private List<Classification> foundClassifications;
    private List<Sector> foundSectors;
    private List<Service> foundServices;
    private List<DocumentStandard> foundDocumentStandards;
    private List<DocumentType> foundDocumentTypes;
    private DocumentType selectedDocumentType;
    private SystemOption selectedSystemOption;
    private Classification selectedClassification;
    private Sector selectedSector;
    private LdapContext selectedLdapContext;
    private Service selectedService;
    private Authentication authentication;
    private List<UIUpdateListener> uiUpdateListeners;
    private List<SearchActionListener> searchActionListeners;
    private List<LoginActionListener> loginActionListeners;
    private Dashboard dashboard;
    private Boolean westLayoutUnitCollapsed;
    private String applicationHeader;

    /**
     * Creates a new instance of SystemManager
     */
    public SystemManager() {
        init();
    }

    public void setApplicationHeader(String applicationHeader) {
        this.applicationHeader = applicationHeader;
    }

    public void doDefaultSearch() {
        notifyListenersToDoDefaultSearch();
    }

    private void notifyListenersToDoDefaultSearch() {
        for (SearchActionListener systemActionListener : searchActionListeners) {
            systemActionListener.doDefaultSearch();
        }
    }
    
    private void notifyListenersToDoLogin() {
        for (LoginActionListener loginActionListener : loginActionListeners) {
            loginActionListener.doLogin();
        }
    }

    public void handleKeepAlive() {
        getUser().setPollTime(new Date());

        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager(), "debugMode")) {
            System.out.println("Handling keep alive session: doing polling ..." + getUser().getPollTime());
        }
        if (getUser().getId() != null) {
            getUser().save(getEntityManager());
        }
    }

    public void updateAllForms() {
        PrimeFaces.current().ajax().update("dashboardForm");
        PrimeFaces.current().ajax().update("mainTabViewForm");
        PrimeFaces.current().ajax().update("headerForm");
    }

    public void setupAuthentication() {

        if (!getAuthentication().getUserLoggedIn()) {
            getAuthentication().reset();
            getAuthentication().addLoginListener(this);
        }
    }

    public void logout() {
        getUser().logActivity("Logged out", getEntityManager());
        reset();
        getAuthentication().notifyLogoutListeners();
        getAuthentication().reset();
    }

    public void editPreferences() {
    }

    public Boolean renderUserMenu() {
        return getUser().getId() != null;
    }

    public Boolean getWestLayoutUnitCollapsed() {
        return westLayoutUnitCollapsed;
    }

    public void setWestLayoutUnitCollapsed(Boolean westLayoutUnitCollapsed) {
        this.westLayoutUnitCollapsed = westLayoutUnitCollapsed;
    }

    public void handleLayoutUnitToggle(ToggleEvent event) {

        if (event.getComponent().getId().equals("dashboard")) {
            westLayoutUnitCollapsed = !event.getVisibility().name().equals("VISIBLE");
        }
    }

    public String getApplicationHeader() {

        if (applicationHeader != null) {
            return applicationHeader;

        } else {
            applicationHeader = (String) SystemOption.getOptionValueObject(getEntityManager(),
                    "applicationHeader");
        }

        return (!"".equals(applicationHeader) ? applicationHeader : "System Management");

    }

    public Boolean getIsDebugMode() {
        return (Boolean) SystemOption.getOptionValueObject(
                getEntityManager(), "debugMode");
    }

    public String getApplicationSubheader() {
        String subHeader;

        if (getIsDebugMode()) {
            subHeader = "Testing & Training Version";
        } else {
            subHeader = (String) SystemOption.getOptionValueObject(
                    getEntityManager(), "applicationSubheader");

            if (subHeader != null) {
                if (subHeader.trim().equals("None")) {
                    return getUser().getEmployee().getDepartment().getName();
                }
            } else {
                subHeader = "";
            }
        }

        return subHeader;
    }

    public void onMainViewTabClose(TabCloseEvent event) {
        String tabId = ((TabPanel) event.getData()).getId();

        mainTabView.closeTab(tabId);
    }

    public void onMainViewTabChange(TabChangeEvent event) {
    }

    public void onDashboardTabChange(TabChangeEvent event) {
        getDashboard().setSelectedTabId(((TabPanel) event.getData()).getId());
    }

    public void updateDashboard(String tabId) {
        PrimeFaces.current().ajax().update("dashboardForm");
    }

    public void initUI() {
        initDashboard();
        initMainTabView();
    }

    private void initMainTabView() {

        getMainTabView().reset(getUser());
        
        getMainTabView().openTab("System Administration");
    }
    
    public void addDashboardTab(TabPanel tab) {
         getDashboard().getTabs().add(tab);
    }
  
    private void initDashboard() {

        getDashboard().reset(getUser());
        
        addDashboardTab(new TabPanel("System Administration", "System Administration"));
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public List<String> completePreferenceValue(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<String> preferenceValues = Preference.findAllPreferenceValues(em, query);

            return preferenceValues;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    private void notifyUIUpdateListeners() {
        for (SystemManager.UIUpdateListener uiUpdateListener : uiUpdateListeners) {
            uiUpdateListener.completeUIUpdate();
        }
    }

    public void closePreferencesDialog2(CloseEvent closeEvent) {
        closePreferencesDialog1(null);
    }

    public void closePreferencesDialog1(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("headerForm");
        PrimeFaces.current().executeScript("PF('preferencesDialog').hide();");
    }

    /**
     * Gets the SessionScoped bean that deals with user authentication.
     *
     * @return
     */
    public Authentication getAuthentication() {
        if (authentication == null) {
            authentication = BeanUtils.findBean("authentication");
        }

        return authentication;
    }

    public List<Tax> completeTax(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<Tax> taxes = Tax.findTaxesByNameAndDescription(em, query);

            return taxes;

        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<AccountingCode> completeAccountingCode(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<AccountingCode> codes = AccountingCode.findActiveAccountingCodes(em, query);

            return codes;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getDateStr(Date date) {
        if (date != null) {
            return BusinessEntityUtils.getDateInMediumDateFormat(date);
        } else {
            return "";
        }
    }

    public String getServiceSearchText() {
        return serviceSearchText;
    }

    public void setServiceSearchText(String serviceSearchText) {
        this.serviceSearchText = serviceSearchText;
    }

    public ArrayList<String> completeCountry(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            ArrayList<Country> countries = new ArrayList<>(Country.findCountriesByName(em, query));
            ArrayList<String> countriesList = (ArrayList<String>) (ArrayList<?>) countries;

            return countriesList;
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Service> completeService(String query) {

        try {
            return Service.findAllByName(getEntityManager(), query);
        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<BusinessOffice> completeBusinessOffice(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<BusinessOffice> offices = BusinessOffice.findActiveBusinessOfficesByName(em, query);

            return offices;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<SelectItem> getDatePeriods() {
        ArrayList<SelectItem> datePeriods = new ArrayList<>();

        for (String name : DatePeriod.getDatePeriodNames()) {
            datePeriods.add(new SelectItem(name, name));
        }

        return datePeriods;
    }

    public List getDateSearchFields() {
        return DateUtils.getDateSearchFields("All");
    }

    // tk Put in LegalMetrologyManager
    public List<SelectItem> getTestMeasures() {

        return getStringListAsSelectItems(getEntityManager(), "petrolTestMeasures");
    }

    public List<SelectItem> getEquipmentWorkingStatus() {

        return getStringListAsSelectItems(getEntityManager(), "equipmentWorkingStatusList");
    }

    public List<SelectItem> getWorkProgressList() {

        return getStringListAsSelectItems(getEntityManager(),
                "workProgressList");
    }

    public List getContactTypes() {

        return getStringListAsSelectItems(getEntityManager(), "personalContactTypes");
    }

    public List<SelectItem> getJamaicaParishes() {

        return getStringListAsSelectItems(getEntityManager(), "jamaicaParishes");
    }

    public static List<SelectItem> getStringListAsSelectItems(EntityManager em,
            String systemOption) {

        ArrayList list = new ArrayList();

        List<String> stringList = (List<String>) SystemOption.getOptionValueObject(em, systemOption);

        for (String name : stringList) {
            list.add(new SelectItem(name, name));
        }

        return list;
    }

    public List getValueTypes() {
        ArrayList valueTypes = new ArrayList();

        valueTypes.add(new SelectItem("String", "String"));
        valueTypes.add(new SelectItem("Boolean", "Boolean"));
        valueTypes.add(new SelectItem("Integer", "Integer"));
        valueTypes.add(new SelectItem("Double", "Double"));
        valueTypes.add(new SelectItem("Long", "Long"));
        valueTypes.add(new SelectItem("List<String>", "List<String>"));

        return valueTypes;
    }

    // tk make system option
    public List getSystemOptionCategories() {
        ArrayList categories = new ArrayList();

        categories.add(new SelectItem("System", "System"));
        categories.add(new SelectItem("Authentication", "Authentication"));
        categories.add(new SelectItem("Database", "Database"));
        categories.add(new SelectItem("Compliance", "Compliance"));
        categories.add(new SelectItem("Document", "Document"));
        categories.add(new SelectItem("Finance", "Finance"));
        categories.add(new SelectItem("General", "General"));
        categories.add(new SelectItem("GUI", "GUI"));
        categories.add(new SelectItem("Job", "Job"));
        categories.add(new SelectItem("Legal", "Legal"));
        categories.add(new SelectItem("Metrology", "Metrology"));
        categories.add(new SelectItem("Notification", "Notification"));

        return categories;
    }

    private void init() {
        activeTabIndex = 0;
        activeNavigationTabIndex = 0;
        activeTabForm = "";
        applicationHeader = null;
        searchType = "General";
        dateSearchField = "dateReceived";
        dateSearchPeriod = "thisMonth";
        searchTextVisible = true;
        foundUsers = null;
        foundLdapContexts = null;
        foundSystemOptions = null;
        foundLdapContexts = null;
        foundClassifications = null;
        foundDocumentStandards = null;
        // Search texts
        searchText = "";
        userSearchText = "";
        generalSearchText = "";
        systemOptionSearchText = "";
        classificationSearchText = "";
        sectorSearchText = "";
        serviceSearchText = "";
        ldapSearchText = "";
        documentTypeSearchText = "";
        // Active flags
        isActiveSectorsOnly = true;
        isActiveLdapsOnly = true;
        isActiveDocumentTypesOnly = true;
        isActiveServicesOnly = true;
        uiUpdateListeners = new ArrayList<>();
        dashboard = new Dashboard(getUser());
        mainTabView = new MainTabView(getUser());
        westLayoutUnitCollapsed = true;
        searchActionListeners = new ArrayList<>();
        uiUpdateListeners = new ArrayList<>();
        loginActionListeners = new ArrayList<>();
    }

    public Boolean getIsActiveServicesOnly() {
        return isActiveServicesOnly;
    }

    public void setIsActiveServicesOnly(Boolean isActiveServicesOnly) {
        this.isActiveServicesOnly = isActiveServicesOnly;
    }

    public Boolean getIsActiveDocumentTypesOnly() {
        return isActiveDocumentTypesOnly;
    }

    public void setIsActiveDocumentTypesOnly(Boolean isActiveDocumentTypesOnly) {
        this.isActiveDocumentTypesOnly = isActiveDocumentTypesOnly;
    }

    public String getDocumentTypeSearchText() {
        return documentTypeSearchText;
    }

    public void setDocumentTypeSearchText(String documentTypeSearchText) {
        this.documentTypeSearchText = documentTypeSearchText;
    }

    public DocumentType getSelectedDocumentType() {
        return selectedDocumentType;
    }

    public void setSelectedDocumentType(DocumentType selectedDocumentType) {
        this.selectedDocumentType = selectedDocumentType;
    }

    public Boolean getIsActiveLdapsOnly() {
        return isActiveLdapsOnly;
    }

    public void setIsActiveLdapsOnly(Boolean isActiveLdapsOnly) {
        this.isActiveLdapsOnly = isActiveLdapsOnly;
    }

    public Boolean getIsActiveSectorsOnly() {
        return isActiveSectorsOnly;
    }

    public void setIsActiveSectorsOnly(Boolean isActiveSectorsOnly) {
        this.isActiveSectorsOnly = isActiveSectorsOnly;
    }

    public LdapContext getSelectedLdapContext() {
        return selectedLdapContext;
    }

    public void setSelectedLdapContext(LdapContext selectedLdapContext) {
        this.selectedLdapContext = selectedLdapContext;
    }

    public String getLdapSearchText() {
        return ldapSearchText;
    }

    public void setLdapSearchText(String ldapSearchText) {
        this.ldapSearchText = ldapSearchText;
    }

    public String getSectorSearchText() {
        return sectorSearchText;
    }

    public void setSectorSearchText(String sectorSearchText) {
        this.sectorSearchText = sectorSearchText;
    }

    public Sector getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSector(Sector selectedSector) {
        this.selectedSector = selectedSector;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public Classification getSelectedClassification() {
        return selectedClassification;
    }

    public void setSelectedClassification(Classification selectedClassification) {
        this.selectedClassification = selectedClassification;
    }

    public void reset() {
        getAuthentication().reset();
        dashboard.removeAllTabs();
        dashboard.setRender(false);
        mainTabView.removeAllTabs();
        mainTabView.setRender(false);
        uiUpdateListeners = new ArrayList<>();
        westLayoutUnitCollapsed = true;

        updateAllForms();

        // Return to default theme
        PrimeFaces.current().executeScript(
                "PF('longProcessDialogVar').hide();"
                + "PrimeFaces.changeTheme('"
                + getUser().getUserInterfaceThemeName() + "');"
                + "PF('layoutVar').toggle('west');");
    }

    public Boolean getIsActiveClassificationsOnly() {
        if (isActiveClassificationsOnly == null) {
            isActiveClassificationsOnly = true;
        }
        return isActiveClassificationsOnly;
    }

    public void setIsActiveClassificationsOnly(Boolean isActiveClassificationsOnly) {
        this.isActiveClassificationsOnly = isActiveClassificationsOnly;
    }

    public String getClassificationSearchText() {
        return classificationSearchText;
    }

    public void setClassificationSearchText(String classificationSearchText) {
        this.classificationSearchText = classificationSearchText;
    }

    public Boolean getIsActiveUsersOnly() {
        if (isActiveUsersOnly == null) {
            isActiveUsersOnly = true;
        }
        return isActiveUsersOnly;
    }

    public void setIsActiveUsersOnly(Boolean isActiveUsersOnly) {
        this.isActiveUsersOnly = isActiveUsersOnly;
    }

    public Boolean getPrivilegeValue() {
        if (privilegeValue == null) {
            privilegeValue = false;
        }
        return privilegeValue;
    }

    public void setPrivilegeValue(Boolean privilegeValue) {
        this.privilegeValue = privilegeValue;
    }

    public void onPrivilegeValueChanged(ValueChangeEvent event) {
        System.out.println("Test" + event.getSource());

    }

    public List<DocumentType> getFoundDocumentTypes() {
        if (foundDocumentTypes == null) {
            foundDocumentTypes = DocumentType.findAllDocumentTypes(getEntityManager());
        }

        return foundDocumentTypes;
    }

    public void setFoundDocumentTypes(List<DocumentType> foundDocumentTypes) {
        this.foundDocumentTypes = foundDocumentTypes;
    }

    public List<Sector> getFoundSectors() {
        if (foundSectors == null) {
            foundSectors = Sector.findAllActiveSectors(getEntityManager());
        }
        return foundSectors;
    }

    public void setFoundSectors(List<Sector> foundSectors) {
        this.foundSectors = foundSectors;
    }

    public List<Service> getFoundServices() {
        if (foundServices == null) {
            foundServices = Service.findAllActive(getEntityManager());
        }
        return foundServices;
    }

    public void setFoundServices(List<Service> foundServices) {
        this.foundServices = foundServices;
    }

    public List<DocumentStandard> getFoundDocumentStandards() {
        if (foundDocumentStandards == null) {
            foundDocumentStandards = DocumentStandard.findAllDocumentStandards(getEntityManager());
        }
        return foundDocumentStandards;
    }

    public void setFoundDocumentStandards(List<DocumentStandard> foundDocumentStandards) {
        this.foundDocumentStandards = foundDocumentStandards;
    }

    public List<Classification> getFoundClassifications() {
        if (foundClassifications == null) {
            foundClassifications = Classification.findAllActiveClassifications(getEntityManager());
        }
        return foundClassifications;
    }

    public SystemOption getSelectedSystemOption() {
        if (selectedSystemOption == null) {
            selectedSystemOption = new SystemOption();
        }
        return selectedSystemOption;
    }

    public void setSelectedSystemOption(SystemOption selectedSystemOption) {
        this.selectedSystemOption = selectedSystemOption;
    }

    public void onSystemOptionCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    EntityManager em = getEntityManager();

                    em.getTransaction().begin();
                    SystemOption option = getFoundSystemOptions().get(index);
                    BusinessEntityUtils.saveBusinessEntity(em, option);
                    em.getTransaction().commit();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void onLDAPCellEdit(CellEditEvent event) {

        getFoundLdapContexts().get(event.getRowIndex()).save(getEntityManager());

    }

    public void onClassificationCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager(), getFoundClassifications().get(event.getRowIndex()));
    }

    public void onSectorCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager(), getFoundSectors().get(event.getRowIndex()));
    }

    public void onServiceCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager(), getFoundServices().get(event.getRowIndex()));
    }

    public void onDocumentStandardCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager(), getFoundDocumentStandards().get(event.getRowIndex()));
    }

    public List<LdapContext> getFoundLdapContexts() {
        if (foundLdapContexts == null) {
            foundLdapContexts = LdapContext.findAllActiveLdapContexts(getEntityManager());
        }
        return foundLdapContexts;
    }

    public List<SystemOption> getFoundSystemOptions() {
        if (foundSystemOptions == null) {
            foundSystemOptions = SystemOption.findAllSystemOptions(getEntityManager());
        }
        return foundSystemOptions;
    }

    public void setFoundSystemOptions(List<SystemOption> foundSystemOptions) {
        this.foundSystemOptions = foundSystemOptions;
    }

    public String getSystemOptionSearchText() {
        return systemOptionSearchText;
    }

    public void setSystemOptionSearchText(String systemOptionSearchText) {
        this.systemOptionSearchText = systemOptionSearchText;
    }

    public String getGeneralSearchText() {
        return generalSearchText;
    }

    public void setGeneralSearchText(String generalSearchText) {
        this.generalSearchText = generalSearchText;
    }

    public List<JobManagerUser> getFoundUsers() {
        if (foundUsers == null) {
            foundUsers = JobManagerUser.findAllActiveJobManagerUsers(getEntityManager());
        }
        return foundUsers;
    }

    public String getUserSearchText() {
        return userSearchText;
    }

    public void setUserSearchText(String userSearchText) {
        this.userSearchText = userSearchText;
    }

    public void doClassificationSearch() {

        if (getIsActiveClassificationsOnly()) {
            foundClassifications = Classification.findActiveClassificationsByName(getEntityManager(), getClassificationSearchText());
        } else {
            foundClassifications = Classification.findClassificationsByName(getEntityManager(), getClassificationSearchText());
        }

    }

    public void doSectorSearch() {

        if (getIsActiveSectorsOnly()) {
            foundSectors = Sector.findActiveSectorsByName(getEntityManager(), getSectorSearchText());
        } else {
            foundSectors = Sector.findSectorsByName(getEntityManager(), getSectorSearchText());
        }

    }

    public void doServiceSearch() {
        if (getIsActiveServicesOnly()) {
            foundServices = Service.findAllActiveByName(getEntityManager(), getServiceSearchText());
        } else {
            foundServices = Service.findAllByName(getEntityManager(), getServiceSearchText());
        }
    }

    public void doDocumentTypeSearch() {

        foundDocumentTypes = DocumentType.findDocumentTypesByName(getEntityManager(), getDocumentTypeSearchText());

        selectSystemAdminTab("dataListsTabViewVar", "Document types", 4, 4);
    }

    /**
     * Select an system administration tab based on whether or not the tab is
     * already opened.
     *
     * @param innerTabViewVar
     * @param innerTabName
     * @param adminTabIndex
     * @param innerTabIndex
     */
    private void selectSystemAdminTab(String innerTabViewVar, String innerTabName, int adminTabIndex, int innerTabIndex) {
        if (getMainTabView().findTab("System Administration") == null) {
            getMainTabView().openTab("System Administration");
            PrimeFaces.current().executeScript("PF('centerTabVar').select(" + adminTabIndex + ");");
            PrimeFacesUtils.addMessage("Select Tab", "Select the " + innerTabName + " tab to begin search", FacesMessage.SEVERITY_INFO);
        } else {
            PrimeFaces.current().executeScript("PF('centerTabVar').select(" + adminTabIndex + ");");
            PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");
        }
    }

    public void doSystemOptionSearch() {

        foundSystemOptions = SystemOption.findSystemOptions(getEntityManager(), getSystemOptionSearchText());

        if (foundSystemOptions == null) {
            foundSystemOptions = new ArrayList<>();
        }

    }

    public void doLdapContextSearch() {
        if (getIsActiveLdapsOnly()) {
            foundLdapContexts = LdapContext.findActiveLdapContexts(getEntityManager(), getLdapSearchText());
        } else {
            foundLdapContexts = LdapContext.findLdapContexts(getEntityManager(), getLdapSearchText());
        }

    }

    public void doUserSearch() {

        if (getIsActiveUsersOnly()) {
            foundUsers = JobManagerUser.findActiveJobManagerUsersByName(getEntityManager(), getUserSearchText());
        } else {
            foundUsers = JobManagerUser.findJobManagerUsersByName(getEntityManager(), getUserSearchText());
        }

    }

    public void openSystemBrowser() {
        getMainTabView().openTab("System Administration");
    }

    public String getFoundUser() {

        if (foundUser != null) {
            return foundUser.getUsername();
        } else {
            foundUser = new JobManagerUser();
            foundUser.setUsername("");

            return foundUser.getUsername();
        }
    }

    public void setFoundUser(String username) {
        foundUser.setUsername(username);
    }

    public void editSystemOption() {
        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 430, 500);
    }

    public void editClassification() {
        PrimeFacesUtils.openDialog(null, "classificationDialog", true, true, true, 325, 600);
    }

    public void editLdapContext() {
        PrimeFacesUtils.openDialog(null, "ldapDialog", true, true, true, 240, 450);
    }

    public void createNewLdapContext() {
        selectedLdapContext = new LdapContext();

        PrimeFacesUtils.openDialog(null, "ldapDialog", true, true, true, 240, 450);
    }

    public void openDocumentTypeDialog(String url) {
        PrimeFacesUtils.openDialog(null, url, true, true, true, 175, 400);
    }

    public void editBusiness() {
        PrimeFacesUtils.openDialog(null, "businessDialog", true, true, true, 600, 700);
    }

    public void editUser() {
        PrimeFacesUtils.openDialog(getSelectedUser(), "adminUserDialog", true, true, true, 430, 750);
    }

    public JobManagerUser getSelectedUser() {
        // init with current logged on user if null
        if (selectedUser == null) {
            selectedUser = new JobManagerUser();
        }

        return selectedUser;
    }

    public void setSelectedUser(JobManagerUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    public Boolean getSearchTextVisible() {
        return searchTextVisible;
    }

    public void setSearchTextVisible(Boolean searchTextVisible) {
        this.searchTextVisible = searchTextVisible;
    }

    public int getActiveNavigationTabIndex() {
        return activeNavigationTabIndex;
    }

    public void setActiveNavigationTabIndex(int activeNavigationTabIndex) {
        this.activeNavigationTabIndex = activeNavigationTabIndex;
    }

    public void loadDocument() {
    }

    public void cancelUserEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelDocumentTypeEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelBusinessEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelSystemOptionEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelClassificationEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelLdapContextEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelSectorEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedUser(ActionEvent actionEvent) {

        selectedUser.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedDocumentType() {

        selectedDocumentType.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedSystemOption() {

        selectedSystemOption.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedClassification() {

        selectedClassification.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedLdapContext() {

        selectedLdapContext.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedSector() {

        selectedSector.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedService() {

        selectedService.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void updateSelectedUserEmployee() {
        if (selectedUser.getEmployee() != null) {
            if (selectedUser.getEmployee().getId() != null) {
                selectedUser.setEmployee(Employee.findEmployeeById(getEntityManager(), selectedUser.getEmployee().getId()));
            } else {
                Employee employee = Employee.findDefaultEmployee(getEntityManager(), "--", "--", true);
                if (selectedUser.getEmployee() != null) {
                    selectedUser.setEmployee(employee);
                }
            }
        } else {
            Employee employee = Employee.findDefaultEmployee(getEntityManager(), "--", "--", true);
            if (selectedUser.getEmployee() != null) {
                selectedUser.setEmployee(employee);
            }
        }
    }

    public void updateSelectedUser() {

        EntityManager em = getEntityManager();

        if (selectedUser.getId() != null) {
            selectedUser = JobManagerUser.findJobManagerUserById(em, selectedUser.getId());
        }
    }

    public void updateCanEnterDepartmentJob() {

    }

    public void updateFoundUser(SelectEvent event) {

        EntityManager em = getEntityManager();

        JobManagerUser u = JobManagerUser.findJobManagerUserByUsername(em, foundUser.getUsername().trim());
        if (u != null) {
            foundUser = u;
            selectedUser = u;
        }
    }

    public List getPersonalTitles() {
        return Utils.getPersonalTitles();
    }

    public List getSexes() {
        return Utils.getSexes();
    }

    public List<String> completeUser(String query) {

        try {
            List<JobManagerUser> users = JobManagerUser.findJobManagerUsersByUsername(getEntityManager(), query);
            List<String> suggestions = new ArrayList<>();
            if (users != null) {
                if (!users.isEmpty()) {
                    for (JobManagerUser u : users) {
                        suggestions.add(u.getUsername());
                    }
                }
            }

            return suggestions;
        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void createNewUser() {
        EntityManager em = getEntityManager();

        selectedUser = new JobManagerUser();
        selectedUser.setEmployee(Employee.findDefaultEmployee(em, "--", "--", true));

        PrimeFacesUtils.openDialog(selectedUser, "adminUserDialog", true, true, true, 430, 750);
    }

    public void createNewClassification() {
        selectedClassification = new Classification();

        PrimeFacesUtils.openDialog(null, "classificationDialog", true, true, true, 325, 600);
    }

    public void createNewSector() {
        selectedSector = new Sector();

        PrimeFacesUtils.openDialog(null, "sectorDialog", true, true, true, 275, 600);
    }

    public void createNewService() {
        selectedService = new Service();

        PrimeFacesUtils.openDialog(null, "serviceDialog", true, true, true, 0, 600);
    }

    public void createNewDocumentType() {
        selectedDocumentType = new DocumentType();

        getMainTabView().openTab("System Administration");
        PrimeFaces.current().executeScript("PF('centerTabVar').select(4);");

        PrimeFacesUtils.openDialog(null, "documentTypeDialog", true, true, true, 275, 400);

    }

    public void editSector() {
        PrimeFacesUtils.openDialog(null, "sectorDialog", true, true, true, 275, 600);
    }

    public void editService() {
        PrimeFacesUtils.openDialog(null, "serviceDialog", true, true, true, 0, 600);
    }

    public void editDocumentType() {
        openDocumentTypeDialog("documentTypeDialog");
    }

    public void createNewDocumentStandard() {
        foundDocumentStandards.add(0, new DocumentStandard());
    }

    public void createNewSystemOption() {

        selectedSystemOption = new SystemOption();

        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 430, 500);
    }

    public void createNewFinancialSystemOption() {

        selectedSystemOption = new SystemOption();
        selectedSystemOption.setCategory("Finance");

        getMainTabView().openTab("Financial Administration");

        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 430, 500);
    }

    public void fetchDepartment(ActionEvent action) {
    }

    public void fetchEmployee(ActionEvent action) {
    }

    public void exportDocumentReportTable() {
    }

    public Tab getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(Tab activeTab) {
        this.activeTab = activeTab;
    }

    public String getDateSearchField() {
        return dateSearchField;
    }

    public void setDateSearchField(String dateSearchField) {
        this.dateSearchField = dateSearchField;
    }

    public String getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    public void setDateSearchPeriod(String dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getEndSearchDateDisabled() {
        return endSearchDateDisabled;
    }

    public void setEndSearchDateDisabled(Boolean endSearchDateDisabled) {
        this.endSearchDateDisabled = endSearchDateDisabled;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Boolean getStartSearchDateDisabled() {
        return startSearchDateDisabled;
    }

    public void setStartSearchDateDisabled(Boolean startSearchDateDisabled) {
        this.startSearchDateDisabled = startSearchDateDisabled;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public List<BusinessOffice> getBusinessOffices() {
        return BusinessOffice.findAllBusinessOffices(getEntityManager());
    }

    public List<Department> getDepartments() {
        return Department.findAllDepartments(getEntityManager());
    }

    public List<DocumentType> getDocumentTypes() {
        return DocumentType.findAllDocumentTypes(getEntityManager());
    }

    public List<Classification> getClassifications() {
        return Classification.findAllClassifications(getEntityManager());
    }

    public List<DocumentReport> getDocumentReports() {
        return DocumentReport.findAllDocumentReports(getEntityManager());
    }

    public List<Employee> getEmployees() {
        return Employee.findAllEmployees(getEntityManager());
    }

    public List<JobManagerUser> getLoggedInJobManagerUsers() {
        return new ArrayList<>();
    }

    public List<SystemOption> getAllSystemOptions() {
        foundSystemOptions = SystemOption.findAllSystemOptions(getEntityManager());

        return foundSystemOptions;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public String getActiveTabForm() {
        return activeTabForm;
    }

    public void setActiveTabForm(String activeTabForm) {
        this.activeTabForm = activeTabForm;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public String getSystemInfo() {
        return "";
    }

    public JobManagerUser getUser() {
        return getAuthentication().getUser();
    }

    public void updatePreferences() {
        getUser().save(getEntityManager());
    }

    public MainTabView getMainTabView() {
        return mainTabView;
    }

    public EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public void updateUserPrivilege(ValueChangeEvent event) {
    }

    public void handleUserDialogReturn() {
    }

    @Override
    public void completeLogin() {
        getUser().logActivity("Logged in", getEntityManager());

        getUser().save(getEntityManager());

        if (getWestLayoutUnitCollapsed()) {
            setWestLayoutUnitCollapsed(false);
            PrimeFaces.current().executeScript("PF('layoutVar').toggle('west');");
        }

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        PrimeFaces.current().executeScript("PrimeFaces.changeTheme('"
                + getUser().getUserInterfaceThemeName() + "');");

        initUI();
        updateAllForms();
        
        notifyListenersToDoLogin();
    }

    @Override
    public void completeLogout() {
        System.out.println("completeLogout()...Not supported yet in SystemManager.");
    }

    public void addUIUpdateListener(SystemManager.UIUpdateListener uiUpdateListener) {

        uiUpdateListeners.add(uiUpdateListener);
    }

    public void addSingleSearchActionListener(SearchActionListener searchActionListener) {
        searchActionListeners.clear();
        searchActionListeners.add(searchActionListener);
    }
    
    public void addSingleLoginActionListener(LoginActionListener loginActionListener) {
        loginActionListeners.clear();
        loginActionListeners.add(loginActionListener);
    }

    public interface UIUpdateListener {

        public void completeUIUpdate();
    }

    public interface SearchActionListener {

        public void doDefaultSearch();
    }
    
     public interface LoginActionListener {

        public void doLogin();
    }
}
