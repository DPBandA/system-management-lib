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
import java.util.List;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.FinancialAccount;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
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
    private EntityManager entityManager;

    public FinancialAccountingManager() {
        init();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
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

    @Override
    public void viewUserProfile() {
    }

    @Override
    public void onDashboardTabChange(TabChangeEvent event) {

        onMainViewTabChange(event);
    }

    @Override
    public String getDefaultCommandTarget() {

        return getSystemManager().getDefaultCommandTarget();

    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        getSystemManager().onMainViewTabChange(event);
    }

    public final void init() {
        setName("financialAccountingManager");
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

    public TreeNode<FinancialAccount> loadAccounts() {
        return loadAccounts(getEntityManager1());
    }

    public TreeNode<FinancialAccount> loadAccounts(EntityManager em) {
        chartOfAccounts = new DefaultTreeNode("Accounts", null);

        List<FinancialAccount> rootAccounts
                = em.createQuery("SELECT a FROM FinancialAccount a WHERE a.parent IS NULL ORDER BY a.name", FinancialAccount.class)
                        .getResultList();

        for (FinancialAccount account : rootAccounts) {
            TreeNode accountNode = new DefaultTreeNode(account, chartOfAccounts);
            buildTree(accountNode, account);
        }

        return chartOfAccounts;
    }

    private void buildTree(TreeNode parentNode, FinancialAccount parentAcc) {
        for (FinancialAccount child : parentAcc.getChildren()) {
            TreeNode childNode = new DefaultTreeNode(child, parentNode);
            buildTree(childNode, child);
        }
    }

    public TreeNode<FinancialAccount> getChartOfAccounts() {

        if (chartOfAccounts == null) {
            loadAccounts();

        }

        return chartOfAccounts;
    }

    public void setChartOfAccounts(TreeNode<FinancialAccount> chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
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
