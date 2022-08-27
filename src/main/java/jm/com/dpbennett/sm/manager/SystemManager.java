/*
System Management (SM)
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

import static com.oracle.jrockit.jfr.ContentType.Timestamp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.auth.Privilege;
import jm.com.dpbennett.business.entity.dm.Attachment;
import jm.com.dpbennett.business.entity.sm.Country;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.sm.LdapContext;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.Authentication;
import jm.com.dpbennett.sm.Authentication.AuthenticationListener;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.TabPanel;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author Desmond Bennett
 */
public class SystemManager implements Serializable,
        AuthenticationListener {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;
    @PersistenceUnit(unitName = "FINPU")
    private EntityManagerFactory EMF2;
    private MainTabView mainTabView;
    private int activeTabIndex;
    private int activeNavigationTabIndex;
    private String activeTabForm;
    private Tab activeTab;
    private Boolean isActiveLdapsOnly;
    private Boolean isActiveDocumentTypesOnly;
    private Boolean isActiveUsersOnly;
    private String systemOptionSearchText;
    private String ldapSearchText;
    private String documentTypeSearchText;
    private String categorySearchText;
    private String notificationSearchText;
    private String privilegeSearchText;
    private String moduleSearchText;
    private String searchText;
    private String searchType;
    private String attachmentSearchText;
    private List<SystemOption> foundSystemOptions;
    private List<SystemOption> foundFinancialSystemOptions;
    private List<LdapContext> foundLdapContexts;
    private List<DocumentType> foundDocumentTypes;
    private List<Category> foundCategories;
    private List<Notification> foundNotifications;
    private List<Privilege> foundActivePrivileges;
    private List<Modules> foundActiveModules;
    private List<User> foundUsers;
    private List<Attachment> foundAttachments;
    private SystemOption selectedSystemOption;
    private DualListModel<Privilege> privilegeDualList;
    private DualListModel<Modules> moduleDualList;
    private LdapContext selectedLdapContext;
    private DocumentType selectedDocumentType;
    private Category selectedCategory;
    private Privilege selectedPrivilege;
    private Notification selectedNotification;
    private Modules selectedModule;
    private Authentication authentication;
    private List<UIUpdateListener> uiUpdateListeners;
    private List<AuthenticationListener> authenticationListeners;
    private Dashboard dashboard;
    // User related
    private User selectedUser;
    private User foundUser;
    private String userSearchText;
    private String usersTableId;
    private Attachment attachment;
    private UploadedFile uploadedFile;
    private List<SelectItem> groupedSearchTypes;
    private DatePeriod dateSearchPeriod;
    private Email selectedEmail;
    private Boolean isActiveEmailsOnly;
    private List<Email> foundEmails;
    private String emailSearchText;

    /**
     * Creates a new instance of SystemManager
     */
    public SystemManager() {
        init();
    }

    public List<Notification> getFoundNotifications() {
        if (foundNotifications == null) {
            foundNotifications = Notification.findAllActiveNotifications(getEntityManager());
        }

        return foundNotifications;
    }

    public void setFoundNotifications(List<Notification> foundNotifications) {
        this.foundNotifications = foundNotifications;
    }

    public Notification getSelectedNotification() {
        return selectedNotification;
    }

    public void setSelectedNotification(Notification selectedNotification) {
        this.selectedNotification = selectedNotification;
    }

    // tk make system options
    public List getEmailTypes() {
        ArrayList categories = new ArrayList();

        categories.add(new SelectItem("", ""));
        categories.add(new SelectItem("Template", "Template"));
        categories.add(new SelectItem("Instance", "Instance"));

        return categories;
    }

    public String getEmailSearchText() {
        return emailSearchText;
    }

    public void setEmailSearchText(String emailSearchText) {
        this.emailSearchText = emailSearchText;
    }

    public List<Email> getFoundEmails() {
        if (foundEmails == null) {
            foundEmails = Email.findAllActiveEmails(getEntityManager());
        }

        return foundEmails;
    }

    public void setFoundEmails(List<Email> foundEmails) {
        this.foundEmails = foundEmails;
    }

    public Email getSelectedEmail() {
        return selectedEmail;
    }

    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public void editEmailTemplate() {
        PrimeFacesUtils.openDialog(null, "/admin/emailTemplateDialog", true, true, true, 600, 700);
    }

    public void saveSelectedEmail() {

        selectedEmail.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewEmailTemplate() {

        selectedEmail = new Email();

        editEmailTemplate();
    }

    public void doEmailSearch() {

        if (getIsActiveEmailsOnly()) {
            foundEmails = Email.findActiveEmails(getEntityManager(), getEmailSearchText());
        } else {
            foundEmails = Email.findEmails(getEntityManager(), getEmailSearchText());
        }

    }

    public DatePeriod getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    public void setDateSearchPeriod(DatePeriod dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
    }

    public List<SelectItem> getGroupedSearchTypes() {
        return groupedSearchTypes;
    }

    public String getRenderDateSearchFields() {
        switch (searchType) {
            case "Users":
            case "Privileges":
            case "Categories":
            case "Document Types":
            case "Options":
            case "Authentication":
            case "Modules":
            case "Attachments":
                return "false";
            default:
                return "true";
        }
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getAttachmentSearchText() {
        return attachmentSearchText;
    }

    public void setAttachmentSearchText(String attachmentSearchText) {
        this.attachmentSearchText = attachmentSearchText;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public DualListModel<Modules> getModuleDualList() {
        return moduleDualList;
    }

    public void setModuleDualList(DualListModel<Modules> moduleDualList) {
        this.moduleDualList = moduleDualList;
    }

    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager(), "appShortcutIconURL");
    }

    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager(), "logoURL");
    }

    // tk get these from Category records. see SC for technique.
    public List getEmailCategories() {
        ArrayList categories = new ArrayList();

        categories.add(new SelectItem("", ""));
        categories.add(new SelectItem("Purchase Requisition", "Purchase Requisition"));
        categories.add(new SelectItem("Job", "Job"));

        return categories;
    }

    // tk make system options
    public List getContentTypes() {
        ArrayList types = new ArrayList();

        types.add(new SelectItem("text/plain", "text/plain"));
        types.add(new SelectItem("text/html", "text/html"));
        types.add(new SelectItem("text/html; charset=utf-8", "text/html; charset=utf-8"));

        return types;
    }

    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager(), "logoURLImageHeight");
    }

    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager(), "logoURLImageWidth");
    }

    public void okPickList() {
        closeDialog(null);
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void addUserModules() {
        List<Modules> source = Modules.findAllActiveModules(getEntityManager());
        List<Modules> target = selectedUser.getActiveModules();

        source.removeAll(target);

        moduleDualList = new DualListModel<>(source, target);

        openModulePickListDialog();
    }

    private List<Privilege> getUserActiveModulePrivileges() {
        List<Privilege> privs = new ArrayList<>();

        for (Modules mod : getSelectedUser().getActiveModules()) {
            privs.addAll(mod.getPrivileges());
        }

        return privs;
    }

    public void addUserPrivileges() {
        List<Privilege> source = getUserActiveModulePrivileges();
        List<Privilege> target = selectedUser.getPrivileges();

        source.removeAll(target);

        privilegeDualList = new DualListModel<>(source, target);

        openPrivilegePickListDialog();
    }

    public void openModulePickListDialog() {
        PrimeFacesUtils.openDialog(null, "modulePickListDialog", true, true, true, 500, 600);
    }

    public void addModulePrivileges() {
        List<Privilege> source = Privilege.findActivePrivileges(getEntityManager(), "");
        List<Privilege> target = selectedModule.getPrivileges();

        source.removeAll(target);

        privilegeDualList = new DualListModel<>(source, target);

        openPrivilegePickListDialog();
    }

    public void addModulePrivilegesDialogReturn() {

        getSelectedModule().setPrivileges(privilegeDualList.getTarget());

    }

    public void addUserModulesDialogReturn() {

        getSelectedUser().setActiveModules(moduleDualList.getTarget());

    }

    public void addUserPrivilegesDialogReturn() {

        getSelectedUser().setPrivileges(privilegeDualList.getTarget());

    }

    public void openPrivilegePickListDialog() {
        PrimeFacesUtils.openDialog(null, "privilegePickListDialog", true, true, true, 500, 600);
    }

    public DualListModel<Privilege> getPrivilegeDualList() {
        return privilegeDualList;
    }

    public void setPrivilegeDualList(DualListModel<Privilege> privilegeDualList) {
        this.privilegeDualList = privilegeDualList;
    }

    public String getUsersTableId() {
        return usersTableId;
    }

    public void setUsersTableId(String usersTableId) {
        this.usersTableId = usersTableId;
    }

    public String getPrivilegeSearchText() {
        return privilegeSearchText;
    }

    public void setPrivilegeSearchText(String privilegeSearchText) {
        this.privilegeSearchText = privilegeSearchText;
    }

    public Privilege getSelectedPrivilege() {
        return selectedPrivilege;
    }

    public void setSelectedPrivilege(Privilege selectedPrivilege) {
        this.selectedPrivilege = selectedPrivilege;
    }

    public Modules getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(Modules selectedModule) {
        this.selectedModule = selectedModule;
    }

    public List<Employee> completeActiveEmployee(String query) {
        EntityManager em;

        try {

            em = getEntityManager();
            List<Employee> employees = Employee.findActiveEmployeesByName(em, query);

            if (employees != null) {
                return employees;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public void updatePreferences() {
        updateUserPreferences(getUser());
    }

    public void updateSelectedUserPreferences() {
        updateUserPreferences(getSelectedUser());
    }

    public void updateUserPreferences(User user) {
        user.save(getEntityManager());
    }

    public Boolean getIsActiveUsersOnly() {

        return isActiveUsersOnly;
    }

    public void setIsActiveUsersOnly(Boolean isActiveUsersOnly) {
        this.isActiveUsersOnly = isActiveUsersOnly;
    }

    public List<User> getFoundUsers() {
        if (foundUsers == null) {
            foundUsers = User.findAllActiveJobManagerUsers(getEntityManager());
        }
        return foundUsers;
    }

    public List<Attachment> getFoundAttachments() {
        if (foundAttachments == null) {
            foundAttachments
                    = Attachment.findAttachmentsByName(getEntityManager(), "");
        }
        return foundAttachments;
    }

    public String getUserSearchText() {
        return userSearchText;
    }

    public void setUserSearchText(String userSearchText) {
        this.userSearchText = userSearchText;
    }

    public void doUserSearch() {

        if (getIsActiveUsersOnly()) {
            foundUsers = User.findActiveJobManagerUsersByName(getEntityManager(), getUserSearchText());
        } else {
            foundUsers = User.findJobManagerUsersByName(getEntityManager(), getUserSearchText());
        }

    }

    public void doAttachmentSearch() {
        foundAttachments = Attachment.findAttachmentsByName(getEntityManager(), getAttachmentSearchText());
    }

    public String getFoundUser() {

        if (foundUser != null) {
            return foundUser.getUsername();
        } else {
            foundUser = new User();
            foundUser.setUsername("");

            return foundUser.getUsername();
        }
    }

    public void setFoundUser(String username) {
        foundUser.setUsername(username);
    }

    public void editUser() {
        PrimeFacesUtils.openDialog(getSelectedUser(), "userDialog", true, true, true, 700, 900);
    }

    public User getSelectedUser() {
        if (selectedUser == null) {
            selectedUser = new User();
        }

        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void cancelUserEdit(ActionEvent actionEvent) {
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
            selectedUser = User.findJobManagerUserById(em, selectedUser.getId());
        }
    }

    public void updateFoundUser(SelectEvent event) {

        EntityManager em = getEntityManager();

        User u = User.findJobManagerUserByUsername(em, foundUser.getUsername().trim());
        if (u != null) {
            foundUser = u;
            selectedUser = u;
        }
    }

    public List<String> completeUser(String query) {

        try {
            List<User> users = User.findJobManagerUsersByUsername(getEntityManager(), query);
            List<String> suggestions = new ArrayList<>();
            if (users != null) {
                if (!users.isEmpty()) {
                    for (User u : users) {
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

        selectedUser = new User();
        selectedUser.setEmployee(Employee.findDefaultEmployee(em, "--", "--", true));

        editUser();
    }

    public void updateUserPrivilege(ValueChangeEvent event) {
    }

    public void handleUserDialogReturn() {
    }

    public void saveSelectedUser(ActionEvent actionEvent) {

        selectedUser.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void closePreferencesDialog(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("appForm");

        PrimeFaces.current().executeScript("PF('preferencesDialog').hide();");
    }

    public void closeUserProfileDialog(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("appForm");

        PrimeFaces.current().executeScript("PF('userProfileDialog').hide();");
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

        PrimeFacesUtils.openDialog(null, "systemOptionDialog", true, true, true, 600, 600);
    }

    public void doDefaultSearch() {
        switch (getSearchType()) {
            case "Users":
                setUserSearchText(getSearchText());
                doUserSearch();
                selectSystemAdminTab("centerTabVar", 0);
                break;
            case "Privileges":
                setPrivilegeSearchText(getSearchText());
                doActivePrivilegeSearch();
                selectSystemAdminTab("centerTabVar", 1);
                break;
            case "Categories":
                setCategorySearchText(getSearchText());
                doCategorySearch();
                selectSystemAdminTab("centerTabVar", 2);
                break;
            case "Document Types":
                setDocumentTypeSearchText(getSearchText());
                doDocumentTypeSearch();
                selectSystemAdminTab("centerTabVar", 3);
                break;
            case "Options":
                setSystemOptionSearchText(getSearchText());
                doSystemOptionSearch();
                selectSystemAdminTab("centerTabVar", 4);
                break;
            case "Authentication":
                setLdapSearchText(getSearchText());
                doLdapContextSearch();
                selectSystemAdminTab("centerTabVar", 5);
                break;
            case "Modules":
                setModuleSearchText(getSearchText());
                doActiveModuleSearch();
                selectSystemAdminTab("centerTabVar", 6);
                break;
            case "Attachments":
                setAttachmentSearchText(getSearchText());
                doAttachmentSearch();
                selectSystemAdminTab("centerTabVar", 7);
                break;
            default:
                break;
        }
    }

    public void doSearch() {
        System.out.println("Doing default search...");
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
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
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

    public void viewUserProfile() {
    }

    public Boolean renderUserMenu() {
        return getUser().getId() != null;
    }

    public void handleLayoutUnitToggle(ToggleEvent event) {

        if (event.getComponent().getId().equals("dashboard")) {

        }
    }

    public String getApplicationHeader() {

        return "System Management";

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

        String tabTitle = event.getTab().getTitle();

        switch (tabTitle) {
            case "Human Resource":
                setUsersTableId(":appForm:mainTabView:humanResourceTabView:usersTable");
                PrimeFaces.current().ajax().update(":appForm:mainTabView:humanResourceTabView");
                break;
            case "System Administration":
                setUsersTableId(":appForm:mainTabView:centerTabView:usersTable");
                PrimeFaces.current().ajax().update(":appForm:mainTabView:centerTabView");
                break;

        }
    }

    public void onDashboardTabChange(TabChangeEvent event) {
        getDashboard().setSelectedTabId(((TabPanel) event.getData()).getId());
    }

    public void updateDashboard(String tabId) {
        PrimeFaces.current().ajax().update("appForm");
    }

    private void initMainTabView() {

        getMainTabView().reset(getUser());

        if (getUser().hasModule("SystemAdministrationModule")) {

            Modules sysAdmin = getUser().getActiveModule("SystemAdministrationModule");

            getMainTabView().openTab(sysAdmin.getMainViewTitle());
        }
    }

    private void initDashboard() {

        getDashboard().reset(getUser(), false);

        if (getUser().hasModule("SystemAdministrationModule")) {
            Modules sysAdmin = getUser().getActiveModule("SystemAdministrationModule");

            getDashboard().setSelectedTabId(sysAdmin.getDashboardTitle());

            getDashboard().openTab(sysAdmin.getDashboardTitle());
        }

        initSearchTypes();
    }

    private void initSearchTypes() {
        SelectItemGroup adminGroup = new SelectItemGroup("Administration");
        adminGroup.setSelectItems(new SelectItem[]{
            new SelectItem("Users", "Users"),
            new SelectItem("Privileges", "Privileges"),
            new SelectItem("Categories", "Categories"),
            new SelectItem("Document Types", "Document Types"),
            new SelectItem("Options", "Options"),
            new SelectItem("Authentication", "Authentication"),
            new SelectItem("Modules", "Modules"),
            new SelectItem("Attachments", "Attachments")
        });
        groupedSearchTypes.add(adminGroup);
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
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

    public List<SelectItem> getAttachmentTypeList() {

        return getStringListAsSelectItems(getEntityManager(),
                "attachmentTypeList");
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

    public static List<SelectItem> getStringListAsSelectItemsWithCaps(EntityManager em,
            String systemOption) {

        ArrayList list = new ArrayList();

        List<String> stringList = (List<String>) SystemOption.getOptionValueObject(em, systemOption);

        for (String name : stringList) {
            list.add(new SelectItem(name, StringUtils.capitalize(name)));
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

    public List<SelectItem> getJobTableViews() {
        ArrayList views = new ArrayList();

        views.add(new SelectItem("Jobs", "Jobs"));
        views.add(new SelectItem("Job Costings", "Job Costings"));
        views.add(new SelectItem("Cashier View", "Cashier View"));

        return views;
    }

    public List<SelectItem> getPFThemes() {

        return getStringListAsSelectItemsWithCaps(getEntityManager(), "PFThemes");
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
        notificationSearchText = "";
        privilegeSearchText = "";
        moduleSearchText = "";
        userSearchText = "";
        searchText = "";
        attachmentSearchText = "";
        emailSearchText = "";
        // Active flags
        isActiveLdapsOnly = true;
        isActiveDocumentTypesOnly = true;
        isActiveUsersOnly = true;
        isActiveEmailsOnly = true;
        uiUpdateListeners = new ArrayList<>();
        dashboard = new Dashboard(getUser());
        mainTabView = new MainTabView(getUser());
        uiUpdateListeners = new ArrayList<>();
        authenticationListeners = new ArrayList<>();
        groupedSearchTypes = new ArrayList<>();
        usersTableId = ":appForm:mainTabView:centerTabView:usersTable";
        searchType = "Users";
        dateSearchPeriod = new DatePeriod("This month", "month",
                "dateAndTimeEntered", null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();

        getAuthentication().addSingleAuthenticationListener(this);
    }

    public Boolean getIsActiveEmailsOnly() {
        return isActiveEmailsOnly;
    }

    public void setIsActiveEmailsOnly(Boolean isActiveEmailsOnly) {
        this.isActiveEmailsOnly = isActiveEmailsOnly;
    }

    public void updateDateSearchField() {
    }

    public String getModuleSearchText() {
        return moduleSearchText;
    }

    public void setModuleSearchText(String moduleSearchText) {
        this.moduleSearchText = moduleSearchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
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

    public List<Privilege> getFoundActivePrivileges() {
        if (foundActivePrivileges == null) {
            foundActivePrivileges = Privilege.findActivePrivileges(getEntityManager(), "");
        }

        return foundActivePrivileges;
    }

    public void setFoundActivePrivileges(List<Privilege> foundActivePrivileges) {
        this.foundActivePrivileges = foundActivePrivileges;
    }

    public List<Modules> getFoundActiveModules() {
        if (foundActiveModules == null) {
            foundActiveModules = Modules.findActiveModules(getEntityManager(), "");
        }

        return foundActiveModules;
    }

    public void setFoundActiveModules(List<Modules> foundActiveModules) {
        this.foundActiveModules = foundActiveModules;
    }

    public void doDocumentTypeSearch() {

        foundDocumentTypes = DocumentType.findDocumentTypesByName(getEntityManager(), getDocumentTypeSearchText());

    }

    public void doCategorySearch() {

        foundCategories = Category.findCategoriesByName(getEntityManager(), getCategorySearchText());

    }

    public void doNotificationSearch() {

        foundNotifications = Notification.findNotificationsByName(getEntityManager(), getNotificationSearchText());
    }

    public void doActivePrivilegeSearch() {

        foundActivePrivileges
                = Privilege.findActivePrivileges(getEntityManager(), getPrivilegeSearchText());

    }

    public void doActiveModuleSearch() {

        foundActiveModules
                = Modules.findActiveModules(getEntityManager(), getModuleSearchText());

    }

    public void openDocumentTypeDialog(String url) {
        PrimeFacesUtils.openDialog(null, url, true, true, true, 300, 400);
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

        editDocumentType();

    }

    public void createNewCategory() {
        selectedCategory = new Category();

        editCategory();

    }

    public void createNewNotification() {
        selectedNotification = new Notification();
        selectedNotification.setOwnerId(getUser().getId());
        
        selectedNotification.setIssueTime(new Date());

        editNotification();

    }

    public void createNewModule() {
        selectedModule = new Modules();
        selectedModule.setActive(true);

        editModule();

    }

    public void saveSelectedCategory() {

        selectedCategory.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedNotification() {

        selectedNotification.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedPrivilege() {

        selectedPrivilege.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedModule() {

        selectedModule.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void editPrivilege() {
        PrimeFacesUtils.openDialog(null, "privilegeDialog", true, true, true, 400, 500);
    }

    public void editNotification() {
        PrimeFacesUtils.openDialog(null, "notificationDialog", true, true, true, 0, 500);
    }
    
    public void deleteNotification() {
        // tk
        System.out.println("Deleting notification...");
    }

    public void editModule() {
        PrimeFacesUtils.openDialog(null, "moduleDialog", true, true, true, 750, 900);
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
     * @param innerTabIndex
     */
    public void selectSystemAdminTab(String innerTabViewVar, int innerTabIndex) {
        if (getMainTabView().findTab("System Administration") == null) {
            getMainTabView().openTab("System Administration");
            PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");
        } else {
            PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");
        }
    }

    public ArrayList getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("General", "General"));
        searchTypes.add(new SelectItem("My jobs", "My jobs"));
        searchTypes.add(new SelectItem("My department's jobs", "My department's jobs"));
        searchTypes.add(new SelectItem("Parent jobs only", "Parent jobs only"));
        searchTypes.add(new SelectItem("Unapproved job costings", "Unapproved job costings"));
        searchTypes.add(new SelectItem("Appr'd & uninv'd jobs", "Appr'd & uninv'd jobs"));

        return searchTypes;
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

    public void createNewAttachment() {
        attachment = new Attachment();
        String destURL = (String) SystemOption.getOptionValueObject(getEntityManager(),
                "defaultUploadLocation");
        attachment.setDestinationURL(destURL);
        openAttachmentDialog();
    }

    public void openAttachmentDialog() {
        PrimeFacesUtils.openDialog(null, "/admin/attachmentDialog", true, true, true, 575, 550);
    }

    public void editLdapContext() {
        PrimeFacesUtils.openDialog(null, "ldapDialog", true, true, true, 375, 550);
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

    public void okAttachment() {
        if (getUploadedFile() != null) {
            uploadAttachment();

        } else {
            PrimeFacesUtils.addMessage("No File", "No file was choosen",
                    FacesMessage.SEVERITY_INFO);
        }
    }

    public void uploadAttachment() {

        try {

            OutputStream outputStream;

            // Source file
            String sourceURL = getUploadedFile().getFileName();
            getAttachment().setSourceURL(sourceURL);
            if (getAttachment().getName().isEmpty()) {
                getAttachment().setName(sourceURL);
            }
            // Save file
            // getAttachment().setDestinationURL(destinationURL);
            File fileToSave = new File(getAttachment().getDestinationURL() + getUploadedFile().getFileName());
            outputStream = new FileOutputStream(fileToSave);
            outputStream.write(getUploadedFile().getContent());
            outputStream.close();

            PrimeFacesUtils.addMessage("Succesful", getUploadedFile().getFileName() + " was uploaded.", FacesMessage.SEVERITY_INFO);

            getAttachment().save(getEntityManager());

            closeDialog(null);

        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            PrimeFacesUtils.addMessage("File Not Found", getUploadedFile().getFileName() + " was NOT found", FacesMessage.SEVERITY_ERROR);
        } catch (IOException ex) {
            System.out.println(ex);
            PrimeFacesUtils.addMessage("Read/Write Error", " A read/write error occured with the file "
                    + getUploadedFile().getFileName(), FacesMessage.SEVERITY_ERROR);
        }

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

    public MainTabView getMainTabView() {
        return mainTabView;
    }

    public EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public EntityManager getEntityManager2() {
        return EMF2.createEntityManager();
    }

    public Date getCurrentDate() {
        return new Date();
    }

    /**
     * NB: May be deprecated in the future when the modules field is removed.
     */
    private void initUserModules() {
        if (getUser().getActiveModule().getAdminModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "SystemAdministrationModule"));
        }
        if (getUser().getActiveModule().getCertificationModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "CertificationModule"));
        }
        if (getUser().getActiveModule().getComplianceModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "RegulatoryModule"));
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "ComplianceModule"));
        }
        if (getUser().getActiveModule().getCrmModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "CRMModule"));
        }
        if (getUser().getActiveModule().getFinancialAdminModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "FinancialManagementModule"));
        }
        if (getUser().getActiveModule().getFoodsModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "FoodsInspectorateModule"));
        }
        if (getUser().getActiveModule().getHrmModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "HRMModule"));
        }
        if (getUser().getActiveModule().getJobManagementAndTrackingModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "JobManagementAndTrackingModule"));
        }
        if (getUser().getActiveModule().getLegalMetrologyModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "LegalMetrologyModule"));
        }
        if (getUser().getActiveModule().getLegalOfficeModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "LegalOfficeModule"));
        }
        if (getUser().getActiveModule().getPurchaseManagementModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "PurchaseManagementModule"));
        }
        if (getUser().getActiveModule().getReportModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "ReportModule"));
        }
        if (getUser().getActiveModule().getServiceRequestModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "ServiceRequestModule"));
        }
        if (getUser().getActiveModule().getStandardsModule()) {
            getUser().getActiveModules().
                    add(Modules.findActiveModuleByName(getEntityManager(), "StandardsManagementModule"));
        }
    }

    /**
     * NB: May be deprecated in the future when the privilege field is removed.
     */
    private void initUserPrivileges() {
        if (getUser().getActivePrivilege().getCanBeJMTSAdministrator()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "BeJMTSAdministrator"));
        }
        if (getUser().getActivePrivilege().getCanBeSuperUser()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "BeSuperUser"));
        }
        if (getUser().getActivePrivilege().getCanApplyDiscountsToJobCosting()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "ApplyDiscountsToJobCosting	"));
        }
        if (getUser().getActivePrivilege().getCanApplyTaxesToJobCosting()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "ApplyTaxesToJobCosting"));
        }
        if (getUser().getActivePrivilege().getCanApproveJobCosting()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "ApproveJobCosting"));
        }
        if (getUser().getActivePrivilege().getCanBeFinancialAdministrator()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "BeFinancialAdministrator"));
        }
        if (getUser().getActivePrivilege().getCanDeleteClient()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "DeleteClient"));
        }
        if (getUser().getActivePrivilege().getCanDeleteDepartment()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "DeleteDepartment"));
        }
        if (getUser().getActivePrivilege().getCanDeleteEmployee()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "DeleteEmployee"));
        }
        if (getUser().getActivePrivilege().getCanDeleteJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "DeleteJob"));
        }
        if (getUser().getActivePrivilege().getCanEditDepartmentJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EditDepartmentJob"));
        }
        if (getUser().getActivePrivilege().getCanEditDisabledJobField()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EditDisabledJobField"));
        }
        if (getUser().getActivePrivilege().getCanAddClient()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "AddClient"));
        }
        if (getUser().getActivePrivilege().getCanAddSupplier()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "AddSupplier"));
        }
        if (getUser().getActivePrivilege().getCanEditInvoicingAndPayment()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EditInvoicingAndPayment"));
        }
        if (getUser().getActivePrivilege().getCanEditJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EditJob"));
        }
        if (getUser().getActivePrivilege().getCanEditOwnJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EditOwnJob"));
        }
        if (getUser().getActivePrivilege().getCanEnterDepartmentJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EnterDepartmentJob"));
        }
        if (getUser().getActivePrivilege().getCanEnterJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EnterJob"));
        }
        if (getUser().getActivePrivilege().getCanEnterOwnJob()) {
            getUser().getPrivileges().add(
                    Privilege.findActivePrivilegeByName(getEntityManager(), "EnterOwnJob"));
        }

    }

    @Override
    public void completeLogin() {
        getUser().logActivity("Logged in", getEntityManager());

        // NB: This is done for now to get the modules from the user active Modules class
        // that is deprecated.
        if (getUser().getActiveModules().isEmpty()) {
            initUserModules();
        }
        // NB: This is done for now to get the privileges from the user Privilege class
        // that is deprecated.
        if (getUser().getPrivileges().isEmpty()) {
            initUserPrivileges();
        }

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

    public String getNotificationSearchText() {
        return notificationSearchText;
    }

    public void setNotificationSearchText(String notificationSearchText) {
        this.notificationSearchText = notificationSearchText;
    }

}
