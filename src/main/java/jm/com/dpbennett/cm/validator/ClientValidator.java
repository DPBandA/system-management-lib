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
package jm.com.dpbennett.cm.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;

/**
 *
 * @author Desmond Bennett
 */
@FacesValidator("clientValidator")
public class ClientValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        Boolean isNewClient = (Boolean) component.getAttributes().get("isNewClient");

        if (!BusinessEntityUtils.validateText(value.toString().trim())) {
            throw new ValidatorException(getMessage(component.getId()));
        }

    }

    private FacesMessage getMessage(String componentId) {
        switch (componentId) {
            case "clientName":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Name",
                        "The Name cannot be blank.");
            case "trn":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Taxpayer Registration Number (TRN)", 
                        "The TRN cannot be blank.");
            default:
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid Field", 
                        "The field cannot be blank.");
        }
    }
}
