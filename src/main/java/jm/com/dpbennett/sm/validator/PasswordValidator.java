/*
Business Entity Library (BEL) 
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

/**
 *
 * @author desbenn
 */
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (value != null) {
            if (value.toString().trim().length() > 0
                    && value.toString().trim().length() < 8) {
                throw new ValidatorException(getMessage("Invalid Password"));
            }

        } else {
            throw new ValidatorException(getMessage("Invalid Password"));
        }

    }

    private FacesMessage getMessage(String messageType) {
        switch (messageType) {
            case "Invalid Password":
                return new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Invalid Password",
                        "The password must be at least eight characters long.");
            default:
                return new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Invalid Password",
                        "The password must be at least eight characters long.");
        }
    }
}
