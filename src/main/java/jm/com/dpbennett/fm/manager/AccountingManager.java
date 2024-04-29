/*
Financial Management (FM) 
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
package jm.com.dpbennett.fm.manager;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.FinancialAccount;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SortMeta;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Desmond Bennett
 */
public class AccountingManager extends GeneralManager implements Serializable {

    private TreeNode<FinancialAccount> chartOfAccounts;
    private List<SortMeta> sortBy;
    private FinancialAccount selectedFinancialAccount;

    public AccountingManager() {
        init();
    }

    @Override
    public final void init() {
        chartOfAccounts = createAccounts();
    }

    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager1();

    }

    // tk sample accounts
    private TreeNode createAccounts() {
        TreeNode<FinancialAccount> chart = new DefaultTreeNode(new FinancialAccount("Bank", "Bank account", 230.0), null);

        TreeNode backup1 = new DefaultTreeNode("document", new FinancialAccount("backup-1.zip", "10kb", 300.0), chart);
        TreeNode applications = new DefaultTreeNode("document", new FinancialAccount("Applications", "100kb", 100.0), backup1);

        return chart;
    }

    public TreeNode<FinancialAccount> getChartOfAccounts() {
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
