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
@FacesValidator("accountingCodeValidator")
public class AccountingCodeValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (!BusinessEntityUtils.validateAccountingCode(value.toString().trim())) {
            throw new ValidatorException(getMessage(component.getId()));
        }
    }
    
    private FacesMessage getMessage(String componentId) {
        switch(componentId) {
            case "accountingCodeName":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Name", 
                        "The Name cannot be blank or contain the ' or \" character.");            
            case "accountingCodeAccount":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Account", 
                        "The Account cannot be blank or contain the ' or \" character.");             
            case "accountingCodeType":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Type", 
                        "The Type cannot be blank or contain the ' or \" character.");              
            case "accountingCodeDescription":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Description", 
                        "The Description cannot be blank or contain the ' or \" character.");    
            default:
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Field", 
                         "The Field cannot be blank or contain the ' or \" character.");
        }
    }
}
