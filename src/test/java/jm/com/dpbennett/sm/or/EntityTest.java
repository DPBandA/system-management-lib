/*
System Management (SM) 
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
package jm.com.dpbennett.sm.or;

import jm.com.dpbennett.sm.*;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.sm.User;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett
 */
public class EntityTest {

    //private static final Logger LOG = Logger.getLogger(EntityTest.class.getName());
    @Test
    public void testEntity() {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU2");
        EntityManager em = emf.createEntityManager();

        List<User> users = User.findAllByName(em, "dbennett", 5);

        if (users != null) {
            System.out.println("User(s): " + users.size());
        }

    }

}
