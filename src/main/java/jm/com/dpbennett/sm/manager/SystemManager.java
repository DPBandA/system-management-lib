/*
System Management
Copyright (C) 2021  D P Bennett & Associates Limited

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
package jm.com.dpbennett.sm.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.sm.Country;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.sm.LdapContext;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.Authentication;
import jm.com.dpbennett.sm.Authentication.AuthenticationListener;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.TabPanel;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;
import org.primefaces.event.ToggleEvent;

/**
 *
 * @author Desmond Bennett
 */
public class SystemManager implements Serializable,
        AuthenticationListener {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;
    private MainTabView mainTabView;
    private int activeTabIndex;
    private int activeNavigationTabIndex;
    private String activeTabForm;
    private Tab activeTab;
    private Boolean isActiveLdapsOnly;
    private Boolean isActiveDocumentTypesOnly;
    private String systemOptionSearchText;
    private String ldapSearchText;
    private String documentTypeSearchText;
    private String categorySearchText;
    private List<SystemOption> foundSystemOptions;
    private List<SystemOption> foundFinancialSystemOptions;
    private List<LdapContext> foundLdapContexts;
    private List<DocumentType> foundDocumentTypes;
    private List<Category> foundCategories;
    private SystemOption selectedSystemOption;
    private LdapContext selectedLdapContext;
    private DocumentType selectedDocumentType;
    private Category selectedCategory;
    private Authentication authentication;
    private List<UIUpdateListener> uiUpdateListeners;
    private List<AuthenticationListener> authenticationListeners;
    private Dashboard dashboard;
    //private Boolean westLayoutUnitCollapsed;

    /**
     * Creates a new instance of SystemManager
     */
    public SystemManager() {
        init();
    }

    public String getDateStr(Date date) {
        if (date != null) {
            return BusinessEntityUtils.getDateInMediumDateFormat(date);
        } else {
            return "";
        }
    }

    public void onFinancialSystemOptionCellEdit(CellEditEvent event) {
        int index = event.getRowIndex();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        try {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (!newValue.toString().trim().equals("")) {
                    EntityManager em = getEntityManager();

                    em.getTransaction().begin();
                    SystemOption option = getFoundFinancialSystemOptions().get(index);
                    BusinessEntityUtils.saveBusinessEntity(em, option);
                    em.getTransaction().commit();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public List<SystemOption> getFoundFinancialSystemOptions() {
        if (foundFinancialSystemOptions == null) {
            foundFinancialSystemOptions = SystemOption.findAllFinancialSystemOptions(getEntityManager());
        }
        return foundFinancialSystemOptions;
    }

    public void setFoundFinancialSystemOptions(List<SystemOption> foundFinancialSystemOptions) {
        this.foundFinancialSystemOptions = foundFinancialSystemOptions;
    }

    public void doFinancialSystemOptionSearch() {

        foundFinancialSystemOptions = SystemOption.findFinancialSystemOptions(getEntityManager(), getSystemOptionSearchText());

        if (foundFinancialSystemOptions == null) {
            foundFinancialSystemOptions = new ArrayList<>();
        }

    }

    public void createNewFinancialSystemOption() {

        selectedSystemOption = new SystemOption();
        selectedSystemOption.setCategory("Finance");

        getMainTabView().openTab("Financial Administration");

        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 450, 500);
    }

    public void doDefaultSearch() {
    }

    private void notifyListenersToCompleteLogin() {
        for (AuthenticationListener authenticationListener : authenticationListeners) {
            authenticationListener.completeLogin();
        }
    }

    private void notifyListenersToCompleteLogout() {
        for (AuthenticationListener authenticationListener : authenticationListeners) {
            authenticationListener.completeLogout();
        }
    }

    public void handleKeepAlive() {
        getUser().setPollTime(new Date());

        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager(), "debugMode")) {
            System.out.println(getApplicationHeader() + 
                    " keeping session alive: " + getUser().getPollTime());
        }
        if (getUser().getId() != null) {
            getUser().save(getEntityManager());
        }
    }

    public void updateAllForms() {
        PrimeFaces.current().ajax().update("appForm");
    }

    public void logout() {
        getUser().logActivity("Logged out", getEntityManager());
        reset();
        getAuthentication().notifyLogoutListeners();
        getAuthentication().reset();
    }

    public String getSupportURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager(), "supportURL");
    }

    public Boolean getShowSupportURL() {
        return (Boolean) SystemOption.getOptionValueObject(
                getEntityManager(), "showSupportURL");
    }

    public void editPreferences() {
    }

    public Boolean renderUserMenu() {
        return getUser().getId() != null;
    }

    public void handleLayoutUnitToggle(ToggleEvent event) {

        if (event.getComponent().getId().equals("dashboard")) {
            
        }
    }

    public String getApplicationHeader() {

        return "System Management"; // tk make system option

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

    private void initMainTabView() {

        getMainTabView().reset(getUser());

        if (getUser().getModules().getAdminModule()) {
            getMainTabView().openTab("System Administration");
        }
    }

    private void initDashboard() {

        getDashboard().reset(getUser(), false);

        if (getUser().getModules().getAdminModule()) {
            getDashboard().openTab("System Administration");
        }
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public void closePreferencesDialog2(CloseEvent closeEvent) {
        closePreferencesDialog1(null);
    }

    public void closePreferencesDialog1(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("appForm");
        
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

    public ArrayList<String> completeCountry(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            ArrayList<Country> countries = new ArrayList<>(Country.findCountriesByName(em, query));
            ArrayList<String> countriesList = (ArrayList<String>) (ArrayList<?>) countries;
            
            countriesList.add(0, "-- Unknown --");

            return countriesList;
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

    public List<SelectItem> getWorkProgressList() {

        return getStringListAsSelectItems(getEntityManager(),
                "workProgressList");
    }

    public List<SelectItem> getIdentificationTypeList() {

        return getStringListAsSelectItems(getEntityManager(),
                "identificationTypeList");
    }

    public List<SelectItem> getServiceLocationList() {

        return getStringListAsSelectItems(getEntityManager(),
                "serviceLocationList");
    }

    public List<SelectItem> getJamaicaParishes() {

        return getStringListAsSelectItems(getEntityManager(), "jamaicaParishes");
    }

    public List<SelectItem> getTypesOfBusinessList() {

        return getStringListAsSelectItems(getEntityManager(), "typesOfBusinessList ");
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

    // tk make system option
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
        categories.add(new SelectItem("Report", "Report"));

        return categories;
    }

    private void init() {
        activeTabIndex = 0;
        activeNavigationTabIndex = 0;
        activeTabForm = "";
        foundLdapContexts = null;
        foundSystemOptions = null;
        foundLdapContexts = null;
        systemOptionSearchText = "";
        ldapSearchText = "";
        documentTypeSearchText = "";
        categorySearchText = "";
        // Active flags
        isActiveLdapsOnly = true;
        isActiveDocumentTypesOnly = true;
        uiUpdateListeners = new ArrayList<>();
        dashboard = new Dashboard(getUser());
        mainTabView = new MainTabView(getUser());
        uiUpdateListeners = new ArrayList<>();
        authenticationListeners = new ArrayList<>();

        getAuthentication().addSingleAuthenticationListener(this);
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

    public List<DocumentType> getFoundDocumentTypes() {
        if (foundDocumentTypes == null) {
            foundDocumentTypes = DocumentType.findAllDocumentTypes(getEntityManager());
        }

        return foundDocumentTypes;
    }

    public void setFoundDocumentTypes(List<DocumentType> foundDocumentTypes) {
        this.foundDocumentTypes = foundDocumentTypes;
    }

    public List<Category> getFoundCategories() {
        if (foundCategories == null) {
            foundCategories = Category.findAllCategories(getEntityManager());
        }
        
        return foundCategories;
    }

    public void setFoundCategories(List<Category> foundCategories) {
        this.foundCategories = foundCategories;
    }
       

    public void doDocumentTypeSearch() {

        foundDocumentTypes = DocumentType.findDocumentTypesByName(getEntityManager(), getDocumentTypeSearchText());

    }
    
    public void doCategorySearch() {

        foundCategories = Category.findCategoriesByName(getEntityManager(), getCategorySearchText());

    }

    public void openDocumentTypeDialog(String url) {
        PrimeFacesUtils.openDialog(null, url, true, true, true, 275, 400);
    }

    public void cancelDocumentTypeEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDocumentType() {

        selectedDocumentType.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewDocumentType() {
        selectedDocumentType = new DocumentType();

        getMainTabView().openTab("System Administration");

        editDocumentType();

    }
    
    public void createNewCategory() {
        selectedCategory = new Category();

        getMainTabView().openTab("System Administration");

        editCategory();

    }
    
    public void saveSelectedCategory() {

        selectedCategory.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }
    
    public void editCategory() {
        PrimeFacesUtils.openDialog(null, "categoryDialog", true, true, true, 300, 400);
    }

    public void editDocumentType() {
        openDocumentTypeDialog("documentTypeDialog");
    }
   
    public List<DocumentType> getDocumentTypes() {
        return DocumentType.findAllDocumentTypes(getEntityManager());
    }

    public Boolean getIsActiveLdapsOnly() {
        return isActiveLdapsOnly;
    }

    public void setIsActiveLdapsOnly(Boolean isActiveLdapsOnly) {
        this.isActiveLdapsOnly = isActiveLdapsOnly;
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

    public void reset() {
        getAuthentication().reset();
        dashboard.removeAllTabs();
        dashboard.setRender(false);
        mainTabView.removeAllTabs();
        mainTabView.setRender(false);
        uiUpdateListeners = new ArrayList<>();

        updateAllForms();       
        
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

    public void openSystemBrowser() {
        getMainTabView().openTab("System Administration");
    }

    public void editSystemOption() {
        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 575, 550);
    }

    public void editLdapContext() {
        PrimeFacesUtils.openDialog(null, "ldapDialog", true, true, true, 350, 550);
    }

    public void createNewLdapContext() {
        selectedLdapContext = new LdapContext();
        selectedLdapContext.setActive(true);

        editLdapContext();
    }

    public int getActiveNavigationTabIndex() {
        return activeNavigationTabIndex;
    }

    public void setActiveNavigationTabIndex(int activeNavigationTabIndex) {
        this.activeNavigationTabIndex = activeNavigationTabIndex;
    }

    public void cancelSystemOptionEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelLdapContextEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedSystemOption() {

        selectedSystemOption.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedLdapContext() {

        selectedLdapContext.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewSystemOption() {

        selectedSystemOption = new SystemOption();

        editSystemOption();
    }

    public Tab getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(Tab activeTab) {
        this.activeTab = activeTab;
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

    public User getUser() {
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

    public void handleUserDialogReturn() {
    }

    @Override
    public void completeLogin() {
        getUser().logActivity("Logged in", getEntityManager());

        getUser().save(getEntityManager());

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initDashboard();
        initMainTabView();
        updateAllForms();

        notifyListenersToCompleteLogin();
    }

    @Override
    public void completeLogout() {

        notifyListenersToCompleteLogout();

        getDashboard().removeAllTabs();
        getMainTabView().removeAllTabs();
    }

    public void addUIUpdateListener(SystemManager.UIUpdateListener uiUpdateListener) {

        uiUpdateListeners.add(uiUpdateListener);
    }

    public void addSingleAuthenticationListener(AuthenticationListener authenticationListener) {
        authenticationListeners.remove(authenticationListener);

        authenticationListeners.add(authenticationListener);
    }

    public interface UIUpdateListener {

        public void completeUIUpdate();
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getCategorySearchText() {
        return categorySearchText;
    }

    public void setCategorySearchText(String categorySearchText) {
        this.categorySearchText = categorySearchText;
    }
    
}
