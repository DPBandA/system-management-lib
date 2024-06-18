/*
System Management (GM)
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

import java.util.ArrayList;
import java.util.Date;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.sm.util.Dashboard;
import jm.com.dpbennett.sm.util.MainTabView;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 *
 * @author Desmond Bennett
 */
public interface Manager {

    public void init();

    public void reset();

    public SelectItemGroup getSearchTypesGroup();

    public ArrayList<SelectItem> getGroupedSearchTypes();

    public String getSearchType();

    public void setSearchType(String searchType);

    public ArrayList<SelectItem> getSearchTypes();

    public ArrayList<SelectItem> getDateSearchFields(String searchType);

    public DatePeriod getDateSearchPeriod();

    public void setDateSearchPeriod(DatePeriod dateSearchPeriod);

    public void doSearch();

    public void doDefaultCommand();

    public String getDefaultCommandTarget();

    public void setDefaultCommandTarget(String defaultCommandTarget);

    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate);

    public void handleKeepAlive();

    public String getApplicationHeader();

    public String getApplicationSubheader();

    public void login();

    public void login(EntityManager em);

    public void register();

    public void logout();

    public Integer getLoginAttempts();

    public void setLoginAttempts(Integer loginAttempts);

    public Boolean getUserLoggedIn();

    public void setUserLoggedIn(Boolean userLoggedIn);

    public String getPassword();

    public void setPassword(String password);

    public String getConfirmedPassword();

    public void setConfirmedPassword(String confirmedPassword);

    public String getUsername();

    public void setUsername(String username);

    public User getUser();

    public void setUser(User user);

    public User getUser(EntityManager em);

    public Boolean checkForLDAPUser(EntityManager em, String username,
            javax.naming.ldap.LdapContext ctx);

    public Boolean validateUser(EntityManager em);

    public void checkLoginAttemps();

    public String getLogonMessage();

    public void setLogonMessage(String logonMessage);

    public String getRegistrationMessage();

    public void setRegistrationMessage(String registrationMessage);

    public void initSearchPanel();

    public void initSearchTypes();

    public Manager getManager(String name);

    public ArrayList<SelectItem> getDatePeriods();

    public ArrayList<SelectItem> getAllDateSearchFields();

    public void updateSearch();

    public void updateSearchType();

    public void updateDateSearchField();

    public String getSearchText();

    public void setSearchText(String searchText);

    public EntityManager getEntityManager1();

    public EntityManager getEntityManager2();

    public void completeLogin();

    public void completeLogout();

    public void initDashboard();

    public void initMainTabView();

    public void updateAllForms();

    public void onMainViewTabClose(TabCloseEvent event);

    public void onMainViewTabChange(TabChangeEvent event);

    public String getAppShortcutIconURL();

    public Boolean renderUserMenu();

    public String getLogoURL();

    public Integer getLogoURLImageHeight();

    public Integer getLogoURLImageWidth();

    public void onNotificationSelect(SelectEvent event);

    public Dashboard getDashboard();

    public MainTabView getMainTabView();

    public void handleSelectedNotification(Notification notification);

    public boolean handleTabChange(String tabTitle);

}
