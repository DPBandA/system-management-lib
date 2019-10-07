/*
Job Management & Tracking System (JMTS) 
Copyright (C) 2019  D P Bennett & Associates Limited

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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author Desmond Bennett
 */
@Named(value = "SMApp")
@ApplicationScoped
@Singleton
public class SMApplication implements Serializable {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF1;
    //@PersistenceContext(unitName = "JMTSPU")
    //private EntityManager em;
    //@PersistenceUnit(unitName = "AccPacPU")
    //private EntityManagerFactory EMF2;
    //@PersistenceContext(unitName = "AccPacPU")
    //private EntityManager em2;
    private final Map<String, String> themes = new TreeMap<>();

    public SMApplication() {

        themes.put("Aristo", "aristo");
        themes.put("Black-Tie", "black-tie");
        themes.put("Redmond", "redmond");
        themes.put("Dark Hive", "dark-hive");
    }

    public Map<String, String> getThemes() {
        return themes;
    }

    public EntityManager getEntityManager1() {
        return  EMF1.createEntityManager();
    }

//    public EntityManager getEntityManager2() {
//        return em2; //EMF2.createEntityManager();
//    }

}
