/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2024  D P Bennett & Associates Limited

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
import jm.com.dpbennett.business.entity.BusinessEntity;

/**
 *
 * @author desbenn
 */
public class ConverterAdapter implements Converter {

//    private final EntityManagerFactory JMTS;
//    private final EntityManagerFactory FIN;
//    private final EntityManagerFactory JMTS3;

//    public ConverterAdapter() {
//        JMTS = Persistence.createEntityManagerFactory("JMTSPU");
//        FIN = Persistence.createEntityManagerFactory("FINPU");
//        JMTS3 = Persistence.createEntityManagerFactory("JMTS3PU");
//      
//    }

//    public EntityManager getDefaultEntityManager() {
//        return JMTS.createEntityManager();
//    }

//    public EntityManager getEntityManager(/*String emname*/) {
        
//        String em1 = SystemOption.getString(getDefaultEntityManager(), emname);
//
//        switch (em1) {
//            case "JMTS3":
//                return getEntityManager3();
//            case "JMTS":
//            default:
//                return getEntityManager1();
//        }

//    }

//    public EntityManager getEntityManager1() {
//
//        String emName = SystemOption.getString(getDefaultEntityManager(), "SMEM");
//
//        switch (emName) {
//            case "JMTS3":
//                return getEntityManager3();
//            case "JMTS":
//            default:
//                return getDefaultEntityManager();
//        }
//
//    }

//    public EntityManager getEntityManager2() {
//        return FIN.createEntityManager();
//    }
//
//    public EntityManager getEntityManager3() {
//        return JMTS3.createEntityManager();
//    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
       return null; 
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((BusinessEntity) value).getName();
    }

}
