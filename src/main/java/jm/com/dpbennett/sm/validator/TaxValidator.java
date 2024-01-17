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
package jm.com.dpbennett.sm.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;

/**
 *
 * @author desbenn
 */
@FacesValidator("taxValidator")
public class TaxValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (!BusinessEntityUtils.validateIdentifier(value.toString().trim())) {
            throw new ValidatorException(getMessage(component.getId()));
        }
    }
    
    private FacesMessage getMessage(String componentId) {
        switch(componentId) {
            case "taxName":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Valid Name Required", 
                        "The character ' is not allowed in the field value.");            
            case "taxValueType":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Valid Type Required", "Please enter a valid type.");               
            case "taxDescription":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Valid Description Required", "Please enter a valid description.");      
            default:
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Field Value Required", 
                        "The character ' is not allowed in the field value.");
        }
    }
}
