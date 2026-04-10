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

import jm.com.dpbennett.jmts.manager.*;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.StatusNote;
import jm.com.dpbennett.business.entity.fm.FinancialAccount;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett
 */
public class FinancialAccountsTest {

    @Test
    public void creatEntity() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU");
        EntityManager em = emf.createEntityManager();

        FinancialAccountingManager fam = new FinancialAccountingManager();
        System.out.println("Welcome to " + fam.getName() + "!\n\n");

        System.out.println("Create test accounts:");
        em.getTransaction().begin();
        FinancialAccount assets = new FinancialAccount("Assets");
        FinancialAccount cash = new FinancialAccount("Cash");
        FinancialAccount bank = new FinancialAccount("Bank");
        assets.addChild(cash);
        assets.addChild(bank);
        em.persist(assets);
        em.getTransaction().commit();

        fam.loadAccounts(em);

    }

}
