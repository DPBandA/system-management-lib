/*
Report Manager (RM) - Reporting module. 
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
package jm.com.dpbennett.sm;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.auth.Privilege;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.sc.MarketProduct;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett
 */
public class EntityTest {

    @Test
    public void testFindActiveMarketProductByName() {
        HashMap prop = new HashMap();

        prop.put("javax.persistence.jdbc.user",
                "root");
        prop.put("javax.persistence.jdbc.password",
                ""); // NB: REMOVE PWD WHEN DONE AND DISABLE TESTING.
        prop.put("javax.persistence.jdbc.url",
                "jdbc:mysql://localhost:3306/jmts");
        prop.put("javax.persistence.jdbc.driver",
                "com.mysql.cj.jdbc.Driver");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU", prop);
        EntityManager em = emf.createEntityManager();

        List<Privilege> privs = Privilege.findActivePrivileges(em, "");
        
        for (Privilege priv : privs) {
            System.out.println("Privilege: " + priv.getDescription());
        }
        

    }

}
