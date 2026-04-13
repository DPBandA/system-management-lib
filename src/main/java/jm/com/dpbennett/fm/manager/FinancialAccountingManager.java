/*
Financial Accounting (FA) 
Copyright (C) 2026  D P Bennett & Associates Limited

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
package jm.com.dpbennett.fm.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.FinancialAccount;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.SortMeta;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Desmond Bennett
 */
public class FinancialAccountingManager extends GeneralManager implements Serializable {

    private TreeNode<FinancialAccount> chartOfAccounts;
    private List<SortMeta> sortBy;
    private FinancialAccount selectedFinancialAccount;
    private FinanceManager financeManager;
    private Boolean isActiveFinancialAccountsOnly;
    private String financialAccountSearchText;

    public FinancialAccountingManager() {
        init();
    }

    public Boolean getIsActiveFinancialAccountsOnly() {
        return isActiveFinancialAccountsOnly;
    }

    public void setIsActiveFinancialAccountsOnly(Boolean isActiveFinancialAccountsOnly) {
        this.isActiveFinancialAccountsOnly = isActiveFinancialAccountsOnly;
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
            case "Accounts":

                List<FinancialAccount> financialAccounts;
                chartOfAccounts = new DefaultTreeNode("Accounts", null);

                if (getIsActiveFinancialAccountsOnly()) {
                    financialAccounts = FinancialAccount.findActive(
                            getEntityManager1());
                } else {
                    financialAccounts = FinancialAccount.find(
                            getEntityManager1());
                }

                for (FinancialAccount account : financialAccounts) {
                    TreeNode accountNode = new DefaultTreeNode(account, chartOfAccounts);
                    buildTree(accountNode, account, searchText);
                }

                break;
            default:
                break;
        }
    }

    public void doFinancialAccountSearch() {

        doDefaultSearch(
                getSystemManager().getMainTabView(),
                null,
                "Accounts",
                getFinancialAccountSearchText(),
                null,
                null);

    }

    public void createNewFinancialAccount() {
        selectedFinancialAccount = new FinancialAccount();

        openFinancialAccountDialog();
    }

    public void cancelDialogEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedFinancialAccount() {

        selectedFinancialAccount.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void openFinancialAccountDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth()) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/finance/financialAccountDialog", options, null);

    }

    @Override
    public User getUser() {

        return getFinanceManager().getUser();

    }

    @Override
    public void onDashboardTabChange(TabChangeEvent event) {

        onMainViewTabChange(event);
    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        getSystemManager().onMainViewTabChange(event);
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Accounts":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAccountSearchButton");
                return true;
            default:
                return false;
        }
    }

    public void openAccountsTab() {

        getSystemManager().getMainTabView().openTab("Accounts");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAccountSearchButton");
    }

    @Override
    public void openDashboardTab(String title) {
        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAccountSearchButton");
    }

    @Override
    public void openMainViewTab(String title) {

        openAccountsTab();
    }

    @Override
    public int getSizeOfActiveNotifications() {

        return getSystemManager().getActiveNotifications().size();
    }

    @Override
    public boolean getHasActiveNotifications() {
        return getSystemManager().getHasActiveNotifications();
    }

    @Override
    public List<Notification> getNotifications() {

        return getSystemManager().getNotifications();
    }

    public final void init() {
        reset();
    }

    @Override
    public void reset() {
        setName("financialAccountingManager");
        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:financialAccountSearchButton");
        financialAccountSearchText = "";
        isActiveFinancialAccountsOnly = true;
    }

    public String getFinancialAccountSearchText() {
        return financialAccountSearchText;
    }

    public void setFinancialAccountSearchText(String financialAccountSearchText) {
        this.financialAccountSearchText = financialAccountSearchText;
    }

    @Override
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public FinanceManager getFinanceManager() {
        if (financeManager == null) {
            financeManager = BeanUtils.findBean("financeManager");
        }

        return financeManager;
    }

    @Override
    public EntityManager getEntityManager1() {
        return getFinanceManager().getEntityManager1();
    }

    public static void buildTree(TreeNode parentNode, FinancialAccount parentAcc, String searchText) {
        for (FinancialAccount child : parentAcc.getChildren()) {
            if (child.getName().toUpperCase().contains(searchText.toUpperCase())) {
                TreeNode childNode = new DefaultTreeNode(child, parentNode);
                buildTree(childNode, child, searchText);
            }
        }
    }

    public TreeNode<FinancialAccount> getChartOfAccounts() {

        if (chartOfAccounts == null) {
            chartOfAccounts = new DefaultTreeNode("Accounts", null);
        }

        return chartOfAccounts;
    }

    public List<SortMeta> getSortBy() {
        return sortBy;
    }

    public void setSortBy(List<SortMeta> sortBy) {
        this.sortBy = sortBy;
    }

    public FinancialAccount getSelectedFinancialAccount() {
        return selectedFinancialAccount;
    }

    public void setSelectedFinancialAccount(FinancialAccount selectedFinancialAccount) {
        this.selectedFinancialAccount = selectedFinancialAccount;
    }

}
