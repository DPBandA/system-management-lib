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
package jm.com.dpbennett.sm.util;

import java.util.List;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class FinancialUtils {

    /**
     * NB: Payment types to be put in database...
     *
     * @param em
     * @return
     */
    public static List<SelectItem> getPaymentTypes(EntityManager em) {

        return getStringListAsSelectItems(em, "cashPaymentTypes");
    }

    /**
     * NB: Payment purposes to be put in database...
     *
     * @return
     * @param em
     */
    public static List getPaymentPurposes(EntityManager em) {

        return getStringListAsSelectItems(em, "cashPaymentPurposes");
    }

    public static List getCostTypeList(EntityManager em) {

        return getStringListAsSelectItems(em, "costTypes");
    }

    /**
     * Returns the discount type that can be applied to a payment/amount NB: To
     * be deprecated
     *
     * @param em
     * @return
     */
    public static List getDiscountTypes(EntityManager em) {

        return getStringListAsSelectItems(em, "discountTypes");
    }

    public static List getValueTypes(EntityManager em) {

        return getStringListAsSelectItems(em, "valueTypes");
    }
    
    public static List getAccountingCodeTypes(EntityManager em) {

        return getStringListAsSelectItems(em, "accountingCodeTypes");
    }

}
