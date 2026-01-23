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
package jm.com.dpbennett.fm.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;
import jm.com.dpbennett.business.entity.pm.PurchaseRequisition;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.validator.ValidatorAdapter;

/**
 *
 * @author Desmond Bennett
 */
@FacesValidator("purchaseOrderNumberValidator")
public class PurchaseOrderNumberValidator extends ValidatorAdapter {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        String poNumber = (String) value;
        Long selectedPurchaseReqId = (Long) component.getAttributes().get("selectedPurchaseReqId");
        Boolean autoGeneratePONumber = (Boolean) component.getAttributes().get("autoGeneratePONumber");

        if (!BusinessEntityUtils.validateText(poNumber.trim())) {
            throw new ValidatorException(getMessage(component.getId()));
        }

        PurchaseRequisition existingPR = PurchaseRequisition.findByPRNumber(getEntityManager(), poNumber);
        if (existingPR != null) {
            long current_prId = selectedPurchaseReqId != null ? selectedPurchaseReqId : -1L;
            if (existingPR.getId() != current_prId) {
                throw new ValidatorException(getMessage("exist"));
            }
        }

        if (autoGeneratePONumber) {
            if (!validatePONumber(poNumber, autoGeneratePONumber)) {
                throw new ValidatorException(getMessage("invalid"));
            }
        }

    }

    private FacesMessage getMessage(String componentId) {
        switch (componentId) {
            case "poNumberEdit":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Purhcase Order Number", "Please enter a valid purchase order number.");            
            case "invalid":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Purhcase Order Number", "This purhcase order cannot be saved because a valid purhcase order number was not entered.");
            case "exist":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Purhcase Order Not Unique", "This purhcase order cannot be saved because the purhcase order number is not unique.");
            default:
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Field Value Required", "Please enter all required fields.");
        }
    }

    public Boolean validatePONumber(String poNumber, Boolean auto) {       
        Integer year;
        Long sequenceNumber = 0L;

        String parts[] = poNumber.split("/");
        if (parts != null) {
            if (parts.length == 3) {
                try {
                   
                    year = Integer.valueOf(parts[1]);
                    if (auto && parts[2].equals("?")) {
                    } else {
                        sequenceNumber = Long.valueOf(parts[2]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(e);
                    return false;
                }
                
                if (year < 1970) {
                    return false;
                }
                if (auto && parts[2].equals("?")) {
                } else if (sequenceNumber < 1L) {
                    return false;
                }
                               
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
