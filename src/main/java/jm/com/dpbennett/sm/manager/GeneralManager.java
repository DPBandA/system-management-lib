/*
General Management (GM)
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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.LdapContext;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.sm.Module;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.TabPanel;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 *
 * @author Desmond Bennett
 */
public class GeneralManager implements Manager, Serializable {

    private String searchText;
    private String searchType;
    private Dashboard dashboard;
    private MainTabView mainTabView;
    private ArrayList<SelectItem> groupedSearchTypes;
    private DatePeriod dateSearchPeriod;
    private ArrayList<SelectItem> allDateSearchFields;
    private String[] moduleNames;
    private User user;
    private String username;
    private String logonMessage;
    private String password;
    private Integer loginAttempts;
    private Boolean userLoggedIn;
    private String defaultCommandTarget;
    private String tabTitle;
    private String registrationMessage;
    private String confirmedPassword;

    /**
     * Creates a new instance of SystemManager
     */
    public GeneralManager() {
        init();
    }

    /**
     * NB: A manager must call this method to set the User field of all managers
     * on which it depends.
     */
    public void setManagerUser() {

        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public String getRegistrationMessage() {
        return registrationMessage;
    }

    @Override
    public void setRegistrationMessage(String registrationMessage) {
        this.registrationMessage = registrationMessage;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public String[] getModuleNames() {
        return moduleNames;
    }

    public void setModuleNames(String[] moduleNames) {
        this.moduleNames = moduleNames;
    }

    @Override
    public DatePeriod getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    @Override
    public void setDateSearchPeriod(DatePeriod dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
    }

    @Override
    public ArrayList<SelectItem> getGroupedSearchTypes() {
        return groupedSearchTypes;
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
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "appShortcutIconURL");
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
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void doSearch() {

        for (String moduleName : moduleNames) {

            Module module = Module.findActiveModuleByName(
                    getEntityManager1(),
                    moduleName);

            if (getUser().hasModule(moduleName)) {
                if (module != null) {
                    Manager manager = getManager(module.getName());
                    if (manager != null) {
                        if (hasSearchType(manager, getSearchType())) {
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
    }

    @Override
    public void handleKeepAlive() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAllForms() {
        PrimeFaces.current().ajax().update(":appForm");
    }

    @Override
    public void logout() {

        throw new UnsupportedOperationException("Not supported yet.");
        
    }

    @Override
    public Boolean renderUserMenu() {
        
        return getUser().getId() != null;
        
    }

    @Override
    public String getApplicationHeader() {

        return "General Management";

    }

    @Override
    public String getApplicationSubheader() {

        return "General System Administration &amp; Management";

    }

    @Override
    public void onMainViewTabClose(TabCloseEvent event) {
        String tabId = ((TabPanel) event.getData()).getId();

        getMainTabView().closeTab(tabId);
    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        setTabTitle(event.getTab().getTitle());

    }

    @Override
    public void initMainTabView() {

        getMainTabView().reset(getUser());

        for (String moduleName : moduleNames) {
            Module module = Module.findActiveModuleByName(getEntityManager1(),
                    moduleName);
            if (module != null) {
                if (getUser().hasModule(moduleName)) {
                    getMainTabView().openTab(module.getDashboardTitle());
                }
            }
        }
    }

    @Override
    public void initDashboard() {
        initSearchPanel();
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Administration");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public void initSearchPanel() {

        initSearchTypes();
        updateSearchType();
    }

    @Override
    public void initSearchTypes() {

        String orgSearchType = searchType;

        groupedSearchTypes.clear();

        for (String moduleName : moduleNames) {

            Module module = Module.findActiveModuleByName(
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

        searchType = orgSearchType;
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
            case "Options":
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

    @Override
    public Dashboard getDashboard() {
        return dashboard;
    }

    /**
     * Gets the SessionScoped bean that deals with user authentication.
     *
     * @param name
     * @return
     */
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

    public void setAllDateSearchFields(ArrayList<SelectItem> allDateSearchFields) {
        this.allDateSearchFields = allDateSearchFields;
    }

    @Override
    public void init() {

        reset();
    }

    @Override
    public void updateDateSearchField() {
    }

    private Boolean hasSearchType(Manager manager, String searchType) {

        for (SelectItem type : manager.getSearchTypes()) {
            if (type.getLabel().equals(searchType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateSearchType() {

        for (String moduleName : moduleNames) {

            Module module = Module.findActiveModuleByName(
                    getEntityManager1(),
                    moduleName);

            if (getUser().hasModule(moduleName)) {
                if (module != null) {
                    Manager manager = getManager(module.getName());
                    if (manager != null) {
                        if (hasSearchType(manager, searchType)) {
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
    public void handleSelectedNotification(Notification notification) {

        switch (notification.getType()) {
            case "GeneralNotification":

                break;
            default:
                System.out.println("Unkown type");
        }

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
    public void reset() {
        moduleNames = new String[]{
            "systemManager"};
        searchText = "";
        dashboard = new Dashboard(getUser());
        mainTabView = new MainTabView(getUser());
        groupedSearchTypes = new ArrayList<>();
        allDateSearchFields = new ArrayList();
        searchType = "General";
        dateSearchPeriod = new DatePeriod("This month", "month",
                "dateEntered", null, null, null, false, false, false);
        dateSearchPeriod.initDatePeriod();
        password = "";
        username = "";
        loginAttempts = 0;
        userLoggedIn = false;
        logonMessage = "Please provide your login details below";
        registrationMessage = "Please provide your registration details below";
        String theme = getUser().getPFThemeName();
        user = new User();
        user.setPFThemeName(theme);
        defaultCommandTarget = "doSearch";

    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {

        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("General", "General"));

        return searchTypes;
    }

    @Override
    public User getUser() {

        if (user == null) {
            user = new User();
        }

        return user;

    }

    @Override
    public MainTabView getMainTabView() {
        return mainTabView;
    }

    @Override
    public EntityManager getEntityManager1() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityManager getEntityManager2() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void completeLogin() {

        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void completeLogout() {

        throw new UnsupportedOperationException("Not supported yet.");
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
        this.username = username.trim();
    }

    @Override
    public User getUser(EntityManager em) {
        if (user == null) {
            return new User();
        } else {
            try {
                if (user.getId() != null) {
                    User userFound = em.find(User.class, user.getId());
                    if (userFound != null) {
                        em.refresh(userFound);
                        user = userFound;
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
    public Boolean checkForLDAPUser(EntityManager em, String username, javax.naming.ldap.LdapContext ctx) {
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"displayName"};

            constraints.setReturningAttributes(attrIDs);

            String name = (String) SystemOption.getOptionValueObject(em, "ldapContextName");
            NamingEnumeration answer = ctx.search(name, "SAMAccountName=" + username, constraints);

            if (!answer.hasMore()) { // Assuming only one match
                // LDAP user not found!
                return Boolean.FALSE;
            }
        } catch (NamingException ex) {
            System.out.println(ex);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean validateUser(EntityManager em) {
        Boolean userValidated = false;
        InitialLdapContext ctx;

        try {
            List<jm.com.dpbennett.business.entity.sm.LdapContext> ctxs = jm.com.dpbennett.business.entity.sm.LdapContext.findAllActiveLdapContexts(em);

            for (jm.com.dpbennett.business.entity.sm.LdapContext ldapContext : ctxs) {
                if (ldapContext.getName().equals("LDAP")) {
                    userValidated = LdapContext.authenticateUser(
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
                    MailUtils.postMail(null,
                            null,
                            SystemOption.getString(getEntityManager1(), "jobManagerEmailName"),
                            null,
                            "Failed user login",
                            "Username: " + username + "\nDate/Time: " + new Date(),
                            "text/plain",
                            getEntityManager1());
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else if (loginAttempts > 2) {// tk # attempts to be made system option
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

        doSearch();

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
    public void updateSearch() {
        setDefaultCommandTarget("doSearch");
    }

    public void updateUserActivity(String appVersion, String activity) {

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

        getUser().setPollTime(new Date());

        getUser().setActivity(appVersion + " " + activity
                + " (" + formatter.format(getUser().getPollTime()) + ")");

    }

    @Override
    public boolean handleTabChange(String tabTitle) {
        return false;
    }

    @Override
    public String getConfirmedPassword() {

        return confirmedPassword;
    }

    @Override
    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }

    @Override
    public void register() {

        throw new UnsupportedOperationException("Not supported yet.");

    }

}
