/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jm.com.dpbennett.sm.util;

import java.util.ArrayList;
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
//        ArrayList costTypes = new ArrayList();
//
//        costTypes.add(new SelectItem("--", "--"));
//        costTypes.add(new SelectItem("FIXED", "Fixed"));
//        costTypes.add(new SelectItem("HEADING", "Heading"));
//        costTypes.add(new SelectItem("VARIABLE", "Variable"));
//        costTypes.add(new SelectItem("SUBCONTRACT", "Subcontract"));
//
//        return costTypes;
        
        return getStringListAsSelectItems(em, "cashPaymentPurposes");
    }

    /**
     * Returns the discount type that can be applied to a payment/amount NB: To
     * be deprecated
     *
     * @return
     */
    public static List getDiscountTypes() {
        ArrayList discountTypes = new ArrayList();

        discountTypes.add(new SelectItem("Currency", "Currency: "));
        discountTypes.add(new SelectItem("Percentage", "Percentage: "));

        return discountTypes;
    }

}
