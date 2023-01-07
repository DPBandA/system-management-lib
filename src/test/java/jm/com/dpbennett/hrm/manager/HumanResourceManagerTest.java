/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2018  D P Bennett & Associates Limited

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
package jm.com.dpbennett.hrm.manager;

import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.User;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett
 */
public class HumanResourceManagerTest {

    
    @Test
    public void getDepartmentFullCode() {
        HashMap prop = new HashMap();

        prop.put("javax.persistence.jdbc.user",
                "root");
        prop.put("javax.persistence.jdbc.password",
                ""); // TK REMOVE PWD WHEN DONE AND DISABLE TESTING.
        prop.put("javax.persistence.jdbc.url",
                "jdbc:mysql://172.16.0.10:3306/jmtstest");
        prop.put("javax.persistence.jdbc.driver",
                "com.mysql.jdbc.Driver");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU", prop);
        EntityManager em = emf.createEntityManager();

        User u = User.findActiveJobManagerUserByEmployeeId(em, 261L);
        Department dept = Department.findDepartmentByName(em, "Metallurgy");
        if ((u != null) && (dept != null)) {
             if (u.isMemberOf(em, dept))    {
                 System.out.println("Member of " + dept.getName());
             } 
             else {
                 System.out.println("NOT member of " + dept.getName());
             }
        }
        else {
            System.out.println("User/department not found!");
        }

    }
    
}
