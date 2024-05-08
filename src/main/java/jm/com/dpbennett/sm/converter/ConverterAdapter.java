/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2017  D P Bennett & Associates Limited

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
package jm.com.dpbennett.sm.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.BusinessEntity;
import jm.com.dpbennett.business.entity.sm.SystemOption;

/**
 *
 * @author desbenn
 */
public class ConverterAdapter implements Converter {

    private final EntityManagerFactory emf;
    private final EntityManagerFactory emf2;
    private final EntityManagerFactory emf3;
    private final EntityManagerFactory emf4;
    private final EntityManager em;
    private final EntityManager em2;
    private final EntityManager em3;
    private final EntityManager em4;

    public ConverterAdapter() {
        emf = Persistence.createEntityManagerFactory("JMTSPU");
        emf2 = Persistence.createEntityManagerFactory("FINPU");
        emf3 = Persistence.createEntityManagerFactory("ENERBASEPU");
        emf4 = Persistence.createEntityManagerFactory("JMTS3PU");

        em = emf.createEntityManager();
        em2 = emf2.createEntityManager();
        em3 = emf3.createEntityManager();
        em4 = emf4.createEntityManager();
    }

//    public EntityManager getEntityManager() {
//
//        return getEntityManager(SystemOption.getString(getDefaultEntityManager(), "SMEM"));
//
//    }

    public EntityManager getDefaultEntityManager() {
        return em;
    }

    public EntityManager getEntityManager(String emname) {
        
        String em1 = SystemOption.getString(getDefaultEntityManager(), emname);

        switch (em1) {
            case "JMTS3":
                return getEntityManager4();
            case "JMTS":
            default:
                return getEntityManager1();
        }

    }

    public EntityManager getEntityManager1() {

        String emName = SystemOption.getString(getDefaultEntityManager(), "SMEM");

        switch (emName) {
            case "JMTS3":
                return getEntityManager4();
            case "JMTS":
            default:
                return getDefaultEntityManager();
        }

    }

    public EntityManager getEntityManager2() {
        return em2;
    }

    public EntityManager getEntityManager3() {
        return em3;
    }

    public EntityManager getEntityManager4() {

        return em4;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((BusinessEntity) value).getName();
    }

}
