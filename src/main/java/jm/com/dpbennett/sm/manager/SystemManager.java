/*
System Management (SM)
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
package jm.com.dpbennett.sm.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.dm.Attachment;
import jm.com.dpbennett.business.entity.sm.Country;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.sm.LdapContext;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.Category;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author Desmond Bennett
 */
public final class SystemManager extends GeneralManager {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;
    @PersistenceUnit(unitName = "FINPU")
    private EntityManagerFactory EMF2;
    @PersistenceUnit(unitName = "ENERBASEPU")
    private EntityManagerFactory EMF3;
    @PersistenceUnit(unitName = "JMTS3PU")
    private EntityManagerFactory EMF4;
    private int activeNavigationTabIndex;
    private Boolean isActiveLdapsOnly;
    private Boolean isActiveDocumentTypesOnly;
    private Boolean isActiveUsersOnly;
    private String systemOptionSearchText;
    private String ldapSearchText;
    private String documentTypeSearchText;
    private String categorySearchText;
    private String countrySearchText;
    private String notificationSearchText;
    private String privilegeSearchText;
    private String moduleSearchText;
    private String attachmentSearchText;
    private List<SystemOption> foundSystemOptions;
    private List<SystemOption> foundSystemOptionsByCategory;
    private List<LdapContext> foundLdapContexts;
    private List<DocumentType> foundDocumentTypes;
    private List<Category> foundCategories;
    private List<Country> foundCountries;
    private List<Notification> foundNotifications;
    private List<Privilege> foundActivePrivileges;
    private List<Modules> foundActiveModules;
    private List<Modules> filteredFoundActiveModules;
    private List<User> foundUsers;
    private List<User> filteredFoundUsers;
    private List<Attachment> foundAttachments;
    private SystemOption selectedSystemOption;
    private DualListModel<Privilege> privilegeDualList;
    private DualListModel<Modules> moduleDualList;
    private LdapContext selectedLdapContext;
    private DocumentType selectedDocumentType;
    private Category selectedCategory;
    private Country selectedCountry;
    private Privilege selectedPrivilege;
    private Notification selectedNotification;
    private Modules selectedModule;
    private User selectedUser;
    private User foundUser;
    private String userSearchText;
    private Attachment attachment;
    private UploadedFile uploadedFile;
    private Email selectedEmail;
    private Boolean isActiveEmailsOnly;
    private List<Email> foundEmails;
    private String emailSearchText;
    private List<Notification> notifications;
    private SystemOption selectedSystemOptionText;

    /**
     * Creates a new instance of SystemManager
     */
    public SystemManager() {
        init();
    }

    public String getCountrySearchText() {
        return countrySearchText;
    }

    public void setCountrySearchText(String countrySearchText) {
        this.countrySearchText = countrySearchText;
    }

    public Country getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(Country selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public List<SystemOption> getSelectedUserSystemOptions() {

        if (getSelectedUser().getId() != null) {
            return SystemOption.findByOwnerId(getEntityManager1(),
                    getSelectedUser().getId());
        }

        return new ArrayList<>();
    }

    public void createNewUserRegistration() {

        EntityManager em = getEntityManager1();

        selectedUser = new User();
        selectedUser.setEmployee(Employee.findDefaultEmployee(em, "--", "--", true));
        selectedUser.setUpdateLDAPUser(getEnableUpdateLDAPUser());

        openRegistrationDialog();
    }

    public void openRegistrationDialog() {

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

        PrimeFaces.current().dialog().openDynamic("registrationDialog", options, null);

    }

    public String getCopyrightOrganization() {
        return SystemOption.getString(getEntityManager1(), "copyrightOrganization");
    }

    public String getOrganizationWebsite() {
        return SystemOption.getString(getEntityManager1(), "organizationWebsite");
    }

    public String getApplicationFooter() {

        return getApplicationHeader() + ", v"
                + SystemOption.getString(getEntityManager1(),
                        "JMTSv");
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "System Administration":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:userSearchButton");
                return true;
            case "Users":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:userSearchButton");
                return true;
            case "Modules":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:moduleSearchButton");
                return true;
            case "Privileges":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:privilegeSearchButton");
                return true;
            case "Categories":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:categorySearchButton");
                return true;
            case "Countries":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:countrySearchButton");
                return true;
            case "Document Types":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:documentTypeSearchButton");
                return true;
            case "Authentication":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:ldapSearchButton");
                return true;
            case "Attachments":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:attachmentSearchButton");
                return true;
            case "Email Templates":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:emailTemplateSearchButton");
                return true;
            case "Notifications":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:notificationSearchButton");
                return true;
            case "Posts":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:postSearchButton");
                return true;
            case "System Settings":
                setDefaultCommandTarget(":appForm:mainTabView:centerTabView:systemOptionSearchButton");
                return true;
            case "Report Templates":
                setDefaultCommandTarget(":appForm:mainTabView:reportTemplateSearchButton");
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        setTabTitle(event.getTab().getTitle());

        for (Modules module : getUser().getActiveModules()) {
            Manager manager = getManager(module.getName());
            if (manager != null) {
                if (manager.handleTabChange(getTabTitle())) {

                    return;
                }
            }
        }

    }

    public void onCentreViewTabChange(TabChangeEvent event) {

        onMainViewTabChange(event);
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "appShortcutIconURL");
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURL");
    }

    public String getAdassaStoreShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "AdassaStoreLogo");
    }

    public String getDPBAShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "DPBALogo");
    }

    public String getCurrentYear() {

        return "" + BusinessEntityUtils.getCurrentYear();
    }

    public void removeSelectedSystemOptionText() {

        int index = 0;

        for (SystemOption systemOption : selectedSystemOption.getTextList()) {
            if (systemOption.getOptionValue().
                    equals(selectedSystemOptionText.getOptionValue())) {

                selectedSystemOption.getTextList().remove(index);

                break;
            }

            ++index;
        }

        selectedSystemOption.updateOptionValue();
    }

    public SystemOption getSelectedSystemOptionText() {
        return selectedSystemOptionText;
    }

    public void setSelectedSystemOptionText(SystemOption selectedSystemOptionText) {
        this.selectedSystemOptionText = selectedSystemOptionText;
    }

    public void onAddSystemOptionText() {
        selectedSystemOption.getTextList().
                add(new SystemOption(selectedSystemOption.getName(), "--"));

        selectedSystemOption.updateOptionValue();
    }

    public void onTextListItemCellEdit(CellEditEvent event) {

        selectedSystemOption.updateOptionValue();

    }

    public void updateOptionValueType() {

        selectedSystemOption.updateOptionValueType();

    }

    public List<Client> completeActiveClient(String query) {
        try {
            return Client.findActiveClientsByAnyPartOfName(getEntityManager1(), query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<Employee> completeActiveEmployee(String query) {
        EntityManager em;

        try {

            em = getEntityManager1();
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

    public List<Department> completeActiveDepartment(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Department> departments = Department.findActiveDepartmentsByName(em, query);

            return departments;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public String getScrollPanelHeight() {
        return "350px";
    }

    public List getContactTypes() {

        return getStringListAsSelectItems(getEntityManager1(), "personalContactTypes");
    }

    public List getPersonalTitles() {
        return Utils.getPersonalTitles();
    }

    public List getSexes() {
        return Utils.getSexes();
    }

    public boolean getEnableUpdateLDAPUser() {
        return SystemOption.getBoolean(getEntityManager1(), "updateLDAPUser");
    }

    public boolean getShowUserProfileSecurityTab() {
        return SystemOption.getBoolean(getEntityManager1(), "showUserProfileSecurityTab");
    }

    public void createNewPrivilege() {
        selectedPrivilege = new Privilege();

        editPrivilege();
    }

    public List<Notification> getFoundNotifications() {
        if (foundNotifications == null) {
            foundNotifications = Notification.findAllActiveNotifications(getEntityManager1());
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

    public String getEmailSearchText() {
        return emailSearchText;
    }

    public void setEmailSearchText(String emailSearchText) {
        this.emailSearchText = emailSearchText;
    }

    public List<Email> getFoundEmails() {
        if (foundEmails == null) {
            foundEmails = Email.findAllActiveEmails(getEntityManager1());
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

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/admin/emailTemplateDialog", options, null);

    }

    public void saveSelectedEmail() {

        selectedEmail.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewEmailTemplate() {

        selectedEmail = new Email();

        editEmailTemplate();
    }

    public void doEmailSearch() {

        if (getIsActiveEmailsOnly()) {
            foundEmails = Email.findActiveEmails(getEntityManager1(), getEmailSearchText());
        } else {
            foundEmails = Email.findEmails(getEntityManager1(), getEmailSearchText());
        }

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

    public List getContentTypes() {

        return getStringListAsSelectItems(getEntityManager1(),
                "contentTypeList");
    }

    public void okPickList() {
        closeDialog(null);
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void addUserModules() {
        List<Modules> source = Modules.findAllActiveModules(getEntityManager1(), 0);
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

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("modulePickListDialog", options, null);

    }

    public void addModulePrivileges() {
        List<Privilege> source = Privilege.findActivePrivileges(getEntityManager1(), "");
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

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("privilegePickListDialog", options, null);

    }

    public DualListModel<Privilege> getPrivilegeDualList() {
        return privilegeDualList;
    }

    public void setPrivilegeDualList(DualListModel<Privilege> privilegeDualList) {
        this.privilegeDualList = privilegeDualList;
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

    public void updatePreferences() {
        updateUserPreferences(getUser());
    }

    public void updateSelectedUserPreferences() {
        updateUserPreferences(getSelectedUser());
    }

    public void updateUserPreferences(User user) {
        user.save(getEntityManager1());
    }

    public Boolean getIsActiveUsersOnly() {

        return isActiveUsersOnly;
    }

    public void setIsActiveUsersOnly(Boolean isActiveUsersOnly) {
        this.isActiveUsersOnly = isActiveUsersOnly;
    }

    public List<User> getFoundUsers() {
        if (foundUsers == null) {
            doUserSearch();
        }

        return foundUsers;
    }

    public List<Attachment> getFoundAttachments() {
        if (foundAttachments == null) {
            foundAttachments
                    = Attachment.findAttachmentsByName(getEntityManager1(), "");
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

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Users",
                getUserSearchText(),
                null,
                null);

    }

    public void doUserFilter() {

        foundUsers = new ArrayList<>();

        for (User filteredFoundUser : filteredFoundUsers) {
            if (filteredFoundUser != null) {
                if (filteredFoundUser.getUsername().contains(getUserSearchText())
                        || filteredFoundUser.getPFThemeName().contains(getUserSearchText())
                        || filteredFoundUser.getActivity().contains(getUserSearchText())
                        || filteredFoundUser.getEmployee().toString().contains(getUserSearchText())
                        || filteredFoundUser.getJobTableViewPreference().contains(getUserSearchText())) {
                    foundUsers.add(filteredFoundUser);
                }
            }
        }

        if (foundUsers.isEmpty()) {
            doUserSearch();
        }

    }

    public void doAttachmentSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Attachments",
                getAttachmentSearchText(),
                null,
                null);

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

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("userDialog", options, null);

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
                selectedUser.setEmployee(Employee.findEmployeeById(getEntityManager1(), selectedUser.getEmployee().getId()));
            } else {
                Employee employee = Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true);
                if (selectedUser.getEmployee() != null) {
                    selectedUser.setEmployee(employee);
                }
            }
        } else {
            Employee employee = Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true);
            if (selectedUser.getEmployee() != null) {
                selectedUser.setEmployee(employee);
            }
        }
    }

    public void updateSelectedUser() {

        EntityManager em = getEntityManager1();

        if (selectedUser.getId() != null) {
            selectedUser = User.findJobManagerUserById(em, selectedUser.getId());
        }
    }

    public void updateFoundUser(SelectEvent event) {

        EntityManager em = getEntityManager1();

        User u = User.findJobManagerUserByUsername(em, foundUser.getUsername().trim());
        if (u != null) {
            foundUser = u;
            selectedUser = u;
        }
    }

    public List<String> completeUser(String query) {

        int maxResult = SystemOption.getInteger(getEntityManager1(),
                "maxSearchResults");

        try {
            List<User> users = User.findJobManagerUsersByUsername(
                    getEntityManager1(), query, maxResult);
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

        EntityManager em = getEntityManager1();

        selectedUser = new User();
        selectedUser.setEmployee(Employee.findDefaultEmployee(em, "--", "--", true));
        selectedUser.setUpdateLDAPUser(getEnableUpdateLDAPUser());

        editUser();
    }

    public void updateUserPrivilege(ValueChangeEvent event) {
    }

    public void handleUserDialogReturn() {
    }

    public void saveSelectedUser() {

        if (!getSelectedUser().saveUnique(getEntityManager1()).isSuccess()) {
            PrimeFacesUtils.addMessage(
                    "User Exists",
                    "The user already exists!",
                    FacesMessage.SEVERITY_ERROR);

            return;
        }

        if (getSelectedUser().getUpdateLDAPUser()) {
            if (updateLDAPUser()) {

                PrimeFaces.current().dialog().closeDynamic(null);
            } else {

                PrimeFacesUtils.addMessage(
                        "User Detail NOT Saved",
                        "The user's security detail was NOT saved! Ensure that the password is confirmed.",
                        FacesMessage.SEVERITY_ERROR);

            }
        } else {
            PrimeFaces.current().dialog().closeDynamic(null);
        }

    }

    public void registerSelectedUser(ActionEvent actionEvent) {

        // tk
        System.out.println("Impl. user reg: maybe save user can be used here");

    }

    public boolean updateLDAPUser() {
        EntityManager em = getEntityManager1();
        LdapContext context = LdapContext.findActiveLdapContextByName(em, "LDAP");

        if (!LdapContext.updateUser(context, selectedUser)) {

            // Try to add a new LDAP user if it can't be updated
            // NB: LdapContext.addUser() needs a password to add the user
            if (checkMatchingUserPasswords(selectedUser)) {
                selectedUser.setPassword(selectedUser.getNewPassword());

                System.out.println("Will attempt to add user");
                return LdapContext.addUser(em, context, selectedUser);
            } else {
                return false;
            }

        }

        return true;
    }

    public void closePreferencesDialog(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("appForm");

        PrimeFaces.current().executeScript("PF('preferencesDialog').hide();");
    }

    public void closeUserProfileDialog(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("appForm");

        PrimeFaces.current().executeScript("PF('userProfileDialog').hide();");
    }

    public void saveUserSecurityProfile() {
        saveUserSecurityProfile(getUser());
    }

    public boolean checkMatchingUserPasswords(User user) {

        if (user.getNewPassword().trim().isEmpty()
                || user.getConfirmedNewPassword().trim().isEmpty()) {

            return false;
        }

        return user.getNewPassword().equals(user.getConfirmedNewPassword());
    }

    public void saveUserSecurityProfile(User user) {

        EntityManager em = getEntityManager1();

        if (user.getName().trim().isEmpty()) {

            PrimeFacesUtils.addMessage(
                    "Username Required",
                    "A valid username is required",
                    FacesMessage.SEVERITY_ERROR);

            return;
        }

        if (!checkMatchingUserPasswords(user)) {

            PrimeFacesUtils.addMessage(
                    "No Match",
                    "Passwords do NOT match",
                    FacesMessage.SEVERITY_ERROR);

            return;
        }

        LdapContext ldap = LdapContext.findActiveLdapContextByName(em, "LDAP");

        if (ldap != null) {
            if (LdapContext.updateUserPassword(
                    em,
                    ldap,
                    user.getUsername(),
                    user.getNewPassword().trim())) {

                PrimeFacesUtils.addMessage(
                        "Password Changed",
                        "Your password was changed",
                        FacesMessage.SEVERITY_INFO);
            } else {
                PrimeFacesUtils.addMessage(
                        "Password NOT Changed",
                        "Your password was NOT changed!",
                        FacesMessage.SEVERITY_ERROR);
            }
        } else {
            PrimeFacesUtils.addMessage(
                    "Password NOT Changed",
                    "The authentication server could not be accessed. Your password was NOT changed!",
                    FacesMessage.SEVERITY_ERROR);
        }

    }

    public void saveSelectedUserSecurityProfile() {

        saveUserSecurityProfile(getSelectedUser());

    }

    public String getDateStr(Date date) {
        if (date != null) {
            return BusinessEntityUtils.getDateInMediumDateFormat(date);
        } else {
            return "";
        }
    }

    public void doSystemOptionSearch(String category) {

        foundSystemOptionsByCategory = SystemOption.findSystemOptions(
                getEntityManager1(),
                getSystemOptionSearchText(),
                category);

        if (foundSystemOptionsByCategory == null) {
            foundSystemOptionsByCategory = new ArrayList<>();
        }

    }

    public void doSystemOptionSearch(String category, String searchText) {

        foundSystemOptionsByCategory = SystemOption.findSystemOptions(
                getEntityManager1(),
                searchText,
                category);

        if (foundSystemOptionsByCategory == null) {
            foundSystemOptionsByCategory = new ArrayList<>();
        }

    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        int maxResult = SystemOption.getInteger(getEntityManager1(),
                "maxSearchResults");

        switch (searchType) {
            case "Users":
                if (getIsActiveUsersOnly()) {
                    foundUsers = User.findActiveJobManagerUsersByName(getEntityManager1(),
                            searchText, maxResult);
                } else {
                    foundUsers = User.findJobManagerUsersByName(getEntityManager1(),
                            searchText, maxResult);
                }

                break;
            case "Privileges":
                foundActivePrivileges = Privilege.findActivePrivileges(getEntityManager1(),
                        searchText);

                break;
            case "Categories":
                foundCategories = Category.findCategoriesByName(getEntityManager1(),
                        searchText);

                break;
            case "Countries":
                foundCountries = Country.findCountriesByName(getEntityManager1(),
                        searchText);

                break;
            case "Document Types":
                foundDocumentTypes = DocumentType.findDocumentTypesByName(getEntityManager1(),
                        searchText);

                break;
            case "System Settings":
                foundSystemOptions = SystemOption.findSystemOptions(getEntityManager1(),
                        searchText);

                break;
            case "Authentication":
                if (getIsActiveLdapsOnly()) {
                    foundLdapContexts = LdapContext.findActiveLdapContexts(getEntityManager1(),
                            searchText);
                } else {
                    foundLdapContexts = LdapContext.findLdapContexts(getEntityManager1(),
                            searchText);
                }

                break;
            case "Modules":
                foundActiveModules = Modules.findActiveModules(
                        getEntityManager1(),
                        searchText, maxResult);

                filteredFoundActiveModules = new ArrayList<Modules>(foundActiveModules);

                break;
            case "Attachments":
                foundAttachments = Attachment.findAttachmentsByName(getEntityManager1(),
                        searchText);

                break;
            default:
                break;
        }
    }

    public String getSupportURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "supportURL");
    }

    public Boolean getShowSupportURL() {
        return (Boolean) SystemOption.getOptionValueObject(
                getEntityManager1(), "showSupportURL");
    }

    public void editPreferences() {
    }

    public void viewUserProfile() {
    }

    public void handleLayoutUnitToggle(ToggleEvent event) {

        if (event.getComponent().getId().equals("dashboard")) {

        }
    }

    @Override
    public String getApplicationHeader() {

        return "System Management";

    }

    public Boolean getIsDebugMode() {
        return (Boolean) SystemOption.getOptionValueObject(
                getEntityManager1(), "debugMode");
    }

    @Override
    public String getApplicationSubheader() {
        String subHeader;

        if (getIsDebugMode()) {
            subHeader = "Testing & Training Version";
        } else {
            subHeader = (String) SystemOption.getOptionValueObject(
                    getEntityManager1(), "applicationSubheader");

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

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Administration");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Users":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Privileges":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Categories":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Document Types":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "System Settings":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Authentication":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Modules":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            case "Attachments":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));

                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    public ArrayList<String> completeCountry(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            ArrayList<Country> countries = new ArrayList<>(Country.findCountriesByName(em, query));
            ArrayList<String> countriesList = (ArrayList<String>) (ArrayList<?>) countries;

            countriesList.add(0, "-- Unknown --");

            return countriesList;
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<SelectItem> getWorkProgressList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "workProgressList");
    }

    public List<SelectItem> getAttachmentTypeList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "attachmentTypeList");
    }

    public List<SelectItem> getIdentificationTypeList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "identificationTypeList");
    }

    public List<SelectItem> getServiceLocationList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "serviceLocationList");
    }

    public List<SelectItem> getJamaicaParishes() {

        return getStringListAsSelectItems(getEntityManager1(), "jamaicaParishes");
    }

    public List<SelectItem> getTypesOfBusinessList() {

        return getStringListAsSelectItems(getEntityManager1(), "typesOfBusinessList");
    }

    public static List<SelectItem> getStringListAsSelectItems(EntityManager em,
            String systemOption) {

        ArrayList list = new ArrayList();

        List<String> stringList = (List<String>) SystemOption.getOptionValueObject(em, systemOption);

        if (stringList != null) {
            for (String name : stringList) {
                list.add(new SelectItem(name, name));
            }
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

    public static List<SelectItem> getStringListWithDisplayNameSelectItems(
            EntityManager em,
            String systemOption) {

        ArrayList list = new ArrayList();

        List<String> stringList = (List<String>) SystemOption.getOptionValueObject(em, systemOption);

        for (String name : stringList) {
            String items[] = name.split(",");

            list.add(new SelectItem(items[1], items[0]));
        }

        return list;
    }

    public List getValueTypes() {
        ArrayList valueTypes = new ArrayList();

        valueTypes.add(new SelectItem("String", "Text"));
        valueTypes.add(new SelectItem("Boolean", "Yes or No"));
        valueTypes.add(new SelectItem("Integer", "Small number"));
        valueTypes.add(new SelectItem("Long", "Large number"));
        valueTypes.add(new SelectItem("Double", "Number with decimal point"));
        valueTypes.add(new SelectItem("List<String>", "List of text"));

        return valueTypes;
    }

    public List getBooleanValues() {
        ArrayList values = new ArrayList();

        values.add(new SelectItem("true", "Yes"));
        values.add(new SelectItem("false", "No"));

        return values;
    }

    public List<SelectItem> getPFThemes() {

        return getStringListAsSelectItemsWithCaps(getEntityManager1(), "PFThemes");
    }

    public List<SelectItem> getPFThemes2() {

        return getStringListWithDisplayNameSelectItems(getEntityManager1(), "PFThemes2");
    }

    public List<SelectItem> getUIThemes() {

        return getStringListWithDisplayNameSelectItems(getEntityManager1(), "UIThemes");
    }

    public List<SelectItem> getSystemOptionCategories() {

        return getStringListAsSelectItems(getEntityManager1(),
                "systemOptionCategoryList");
    }

    @Override
    public void init() {

        reset();
    }

    public Boolean getIsActiveEmailsOnly() {
        return isActiveEmailsOnly;
    }

    public void setIsActiveEmailsOnly(Boolean isActiveEmailsOnly) {
        this.isActiveEmailsOnly = isActiveEmailsOnly;
    }

    public String getModuleSearchText() {
        return moduleSearchText;
    }

    public void setModuleSearchText(String moduleSearchText) {
        this.moduleSearchText = moduleSearchText;
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
            foundDocumentTypes = DocumentType.findAllDocumentTypes(getEntityManager1());
        }

        return foundDocumentTypes;
    }

    public void setFoundDocumentTypes(List<DocumentType> foundDocumentTypes) {
        this.foundDocumentTypes = foundDocumentTypes;
    }

    public List<Category> getFoundCategories() {
        if (foundCategories == null) {
            foundCategories = Category.findAllCategories(getEntityManager1());
        }

        return foundCategories;
    }

    public void setFoundCategories(List<Category> foundCategories) {
        this.foundCategories = foundCategories;
    }

    public List<Country> getFoundCountries() {
        if (foundCountries == null) {
            foundCountries = Country.findAllCountries(getEntityManager1());
        }

        return foundCountries;
    }

    public void setFoundCountries(List<Country> foundCountries) {
        this.foundCountries = foundCountries;
    }

    public List<Privilege> getFoundActivePrivileges() {
        if (foundActivePrivileges == null) {
            foundActivePrivileges = Privilege.findActivePrivileges(getEntityManager1(), "");
        }

        return foundActivePrivileges;
    }

    public void setFoundActivePrivileges(List<Privilege> foundActivePrivileges) {
        this.foundActivePrivileges = foundActivePrivileges;
    }

    public List<Modules> getFoundActiveModules() {
        if (foundActiveModules == null) {

            doActiveModuleSearch();
        }

        return foundActiveModules;
    }

    public void setFoundActiveModules(List<Modules> foundActiveModules) {
        this.foundActiveModules = foundActiveModules;
    }

    public void doDocumentTypeSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Document Types",
                getDocumentTypeSearchText(),
                null,
                null);

    }

    public void doCategorySearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Categories",
                getCategorySearchText(),
                null,
                null);
    }

    public void doCountrySearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Countries",
                getCountrySearchText(),
                null,
                null);
    }

    public void doNotificationSearch() {

        foundNotifications = Notification.findNotificationsByName(getEntityManager1(), getNotificationSearchText());
    }

    public void doActivePrivilegeSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Privileges",
                getPrivilegeSearchText(),
                null,
                null);

    }

    public void doActiveModuleSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Modules",
                getModuleSearchText(),
                null,
                null);

    }

    public void doActiveModuleFilter() {

        foundActiveModules = new ArrayList<>();

        for (Modules filteredFoundModule : filteredFoundActiveModules) {
            if (filteredFoundModule != null) {
                if (filteredFoundModule.getDashboardTitle().contains(getModuleSearchText())
                        || filteredFoundModule.getMainViewTitle().contains(getModuleSearchText())
                        || filteredFoundModule.getDescription().contains(getModuleSearchText())
                        || filteredFoundModule.getDashboardTitle().contains(getModuleSearchText())
                        || filteredFoundModule.getName().contains(getModuleSearchText())) {

                    foundActiveModules.add(filteredFoundModule);
                }
            }
        }

        if (foundActiveModules.isEmpty()) {
            doActiveModuleSearch();
        }

    }

    public void openDocumentTypeDialog(String url) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic(url, options, null);

    }

    public void cancelDocumentTypeEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDocumentType() {

        selectedDocumentType.save(getEntityManager1());

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

    public void createNewCountry() {
        selectedCountry = new Country();

        editCountry();

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

        selectedCategory.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedCountry() {

        selectedCountry.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedNotification() {

        selectedNotification.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedPrivilege() {

        selectedPrivilege.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedModule() {

        selectedModule.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void editPrivilege() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("privilegeDialog", options, null);

    }

    public void editNotification() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("notificationDialog", options, null);

    }

    public void deleteNotification() {

        EntityManager em = getEntityManager1();

        getSelectedNotification().delete(em);

        doNotificationSearch();
        PrimeFaces.current().ajax().update("appForm:mainTabView", "appForm:notificationBadge");

    }

    public List<Notification> getNotificationsByOwnerId() {
        EntityManager em = getEntityManager1();

        List<Notification> myNotifications = Notification.findNotificationsByOwnerId(
                em,
                getUser().getId());

        if (myNotifications.isEmpty()) {
            return new ArrayList<>();
        }

        int subListIndex = SystemOption.getInteger(em, "maxNotificationsToDisplay");

        int myNotificationsNum = myNotifications.size();

        if (subListIndex > myNotificationsNum) {
            subListIndex = myNotificationsNum;
        }

        return myNotifications.subList(0, subListIndex);
    }

    public List<Notification> getNotifications() {
        notifications = getNotificationsByOwnerId();

        if (notifications.isEmpty()) {
            Notification notification = new Notification();
            notification.setActive(false);
            notification.setType("None");
            notification.setName("<< You have no notifications >>");
            notifications.add(notification);
        }

        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getActiveNotifications() {
        List<Notification> myActiveNotifications = Notification.findActiveNotificationsByOwnerId(
                getEntityManager1(),
                getUser().getId());

        return myActiveNotifications;

    }

    public int getSizeOfActiveNotifications() {

        return getActiveNotifications().size();
    }

    public boolean getHasActiveNotifications() {
        return (!getActiveNotifications().isEmpty());
    }

    public void editModule() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("moduleDialog", options, null);

    }

    public void editCategory() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("categoryDialog", options, null);

    }

    public void editCountry() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("countryDialog", options, null);

    }

    public void editDocumentType() {
        openDocumentTypeDialog("documentTypeDialog");
    }

    public List<DocumentType> getDocumentTypes() {
        return DocumentType.findAllDocumentTypes(getEntityManager1());
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

    @Override
    public void reset() {
        super.reset();

        activeNavigationTabIndex = 0;
        foundLdapContexts = null;
        foundSystemOptions = null;
        foundSystemOptionsByCategory = new ArrayList<>();
        filteredFoundUsers = new ArrayList<>();
        filteredFoundActiveModules = new ArrayList<>();
        foundLdapContexts = null;
        systemOptionSearchText = "";
        ldapSearchText = "";
        documentTypeSearchText = "";
        categorySearchText = "";
        countrySearchText = "";
        notificationSearchText = "";
        privilegeSearchText = "";
        moduleSearchText = "";
        userSearchText = "";
        attachmentSearchText = "";
        emailSearchText = "";
        isActiveLdapsOnly = true;
        isActiveDocumentTypesOnly = true;
        isActiveUsersOnly = true;
        isActiveEmailsOnly = true;
        setSearchType("Users");
        setModuleNames(new String[]{
            "systemManager"});
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();
        setDefaultCommandTarget(":appForm:mainTabView:centerTabView:userSearchButton");
        setTabTitle("Users");

    }

    public List<SystemOption> getFoundSystemOptionsByCategory() {
        return foundSystemOptionsByCategory;
    }

    public void setFoundSystemOptionsByCategory(List<SystemOption> foundSystemOptionsByCategory) {
        this.foundSystemOptionsByCategory = foundSystemOptionsByCategory;
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
                    EntityManager em = getEntityManager1();

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

        getFoundLdapContexts().get(event.getRowIndex()).save(getEntityManager1());

    }

    public List<LdapContext> getFoundLdapContexts() {
        if (foundLdapContexts == null) {
            foundLdapContexts = LdapContext.findAllActiveLdapContexts(getEntityManager1());
        }
        return foundLdapContexts;
    }

    public List<SystemOption> getFoundSystemOptions() {
        if (foundSystemOptions == null) {
            foundSystemOptions = SystemOption.findAllSystemOptions(getEntityManager1());
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
     * @param mainTabView
     * @param openTab
     * @param innerTabViewVar
     * @param innerTabIndex
     */
    public void selectSystemAdminTab(
            MainTabView mainTabView,
            Boolean openTab,
            String innerTabViewVar,
            int innerTabIndex) {

        if (openTab) {
            mainTabView.openTab("System Administration");
        }

        PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");

    }

    public void selectTab(
            int innerTabIndex) {

        getMainTabView().openTab("System Administration");

        PrimeFaces.current().executeScript("PF('" + "centerTabVar" + "').select(" + innerTabIndex + ");");

    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {

        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Users", "Users"));
        searchTypes.add(new SelectItem("Privileges", "Privileges"));
        searchTypes.add(new SelectItem("Categories", "Categories"));
        searchTypes.add(new SelectItem("Document Types", "Document Types"));
        searchTypes.add(new SelectItem("System Settings", "System Settings"));
        searchTypes.add(new SelectItem("Authentication", "Authentication"));
        searchTypes.add(new SelectItem("Modules", "Modules"));
        searchTypes.add(new SelectItem("Attachments", "Attachments"));

        return searchTypes;
    }

    public void doSystemSettingSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "System Settings",
                getSystemOptionSearchText(),
                null,
                null);

    }

    public void doLdapContextSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Authentication",
                getLdapSearchText(),
                null,
                null);

    }

    public void openSystemBrowser() {

        setDefaultCommandTarget(":appForm:mainTabView:centerTabView:userSearchButton");

        getMainTabView().openTab("System Administration");

    }

    public void editSystemOption() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("systemOptionDialog", options, null);

    }

    public void createNewAttachment() {
        attachment = new Attachment();
        String destURL = (String) SystemOption.getOptionValueObject(getEntityManager1(),
                "defaultUploadLocation");
        attachment.setDestinationURL(destURL);
        openAttachmentDialog();
    }

    public void openAttachmentDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/admin/attachmentDialog", options, null);

    }

    public void editLdapContext() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("ldapDialog", options, null);

    }

    public void openContactUsDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("contactUsDialog", options, null);

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

        selectedSystemOption.save(getEntityManager1());

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

            String sourceURL = getUploadedFile().getFileName();
            getAttachment().setSourceURL(sourceURL);
            if (getAttachment().getName().isEmpty()) {
                getAttachment().setName(sourceURL);
            }

            File fileToSave = new File(getAttachment().getDestinationURL() + getUploadedFile().getFileName());
            outputStream = new FileOutputStream(fileToSave);
            outputStream.write(getUploadedFile().getContent());
            outputStream.close();

            PrimeFacesUtils.addMessage("Succesful", getUploadedFile().getFileName() + " was uploaded.", FacesMessage.SEVERITY_INFO);

            getAttachment().save(getEntityManager1());

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

        selectedLdapContext.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void createNewSelectedUserSystemOption() {
        // tk
        selectedSystemOption = new SystemOption();
        selectedSystemOption.setOptionValueType("String");
        selectedSystemOption.setOwnerId(getSelectedUser().getId());

        editSystemOption();
    }

    public void createNewSystemOption() {

        selectedSystemOption = new SystemOption();
        selectedSystemOption.setOptionValueType("String");

        editSystemOption();
    }

    public void createNewSystemOption(String category) {

        selectedSystemOption = new SystemOption();
        selectedSystemOption.setOptionValueType("String");
        selectedSystemOption.setCategory(category);

        editSystemOption();
    }

    public List<SystemOption> getAllSystemOptions() {
        foundSystemOptions = SystemOption.findAllSystemOptions(getEntityManager1());

        return foundSystemOptions;
    }

    public String getSystemInfo() {
        return "";
    }

    @Override
    public EntityManager getEntityManager1() {
        return EMF.createEntityManager();
    }

    @Override
    public EntityManager getEntityManager2() {
        return EMF2.createEntityManager();
    }

    public EntityManager getEntityManager3() {
        return EMF3.createEntityManager();
    }
    
    public EntityManager getEntityManager4() {
        return EMF4.createEntityManager();
    }

    public Date getCurrentDate() {
        return new Date();
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

    // tk move to JobManager
    public List<SelectItem> getJobTableViews() {
        ArrayList views = new ArrayList();

        views.add(new SelectItem("Jobs", "Jobs"));
        views.add(new SelectItem("Job Costings", "Job Costings"));
        views.add(new SelectItem("Cashier View", "Cashier View"));

        return views;
    }

    @Override
    public void handleKeepAlive() {

        super.updateUserActivity("SMv"
                + SystemOption.getString(getEntityManager1(), "SMv"),
                "Logged in");

        super.handleKeepAlive();

    }

    @Override
    public void completeLogout() {

        super.updateUserActivity("SMv"
                + SystemOption.getString(getEntityManager1(), "SMv"),
                "Logged out");

        super.completeLogout();
    }

    @Override
    public void completeLogin() {

        super.updateUserActivity("SMv"
                + SystemOption.getString(getEntityManager1(), "SMv"),
                "Logged in");

        super.completeLogin();
    }

    public Boolean isSelectedSystemOptionValueType(String valueType) {

        if (valueType.equals("StringList")
                && selectedSystemOption.getOptionValueType().equals("List<String>")) {

            return true;
        }

        return selectedSystemOption.getOptionValueType().equals(valueType)
                && !selectedSystemOption.getOptionValueType().equals("List<String>");

    }

}
