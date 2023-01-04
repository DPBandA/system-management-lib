/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jm.com.dpbennett.sm.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.sm.Authentication;

/**
 *
 * @author Desmond Bennett
 */
public interface Manager {

    public void init();

    public void reset();

    public SelectItemGroup getSearchTypesGroup();

    public List<SelectItem> getGroupedSearchTypes();

    public String getSearchType();

    public void setSearchType(String searchType);

    public SelectItem[] getSearchTypes();

    public DatePeriod getDateSearchPeriod();

    public void setDateSearchPeriod(DatePeriod dateSearchPeriod);

    public void doSearch();

    public void doDefaultSearch();

    public void handleKeepAlive();

    public String getApplicationHeader();

    public String getApplicationSubheader();

    public void logout();

    public void initSearchPanel();

    public void initSearchTypes();

    public void initDateSearchFields();

    public Authentication getAuthentication();

    public Manager getManager(String name);

    public ArrayList<SelectItem> getDatePeriods();

    public ArrayList<SelectItem> getAllDateSearchFields();

    public void updateSearchType();

    public void updateDateSearchField();

    public String getSearchText();

    public void setSearchText(String searchText);

    public Map<String, List<SelectItem>> getSearchTypeToDateFieldMap();
    
    public void setSearchTypeToDateFieldMap(Map<String, List<SelectItem>> searchTypeToDateFieldMap);

    public User getUser();

    public EntityManager getEntityManager1();

    public EntityManager getEntityManager2();

    public void completeLogin();

    public void completeLogout();

}
