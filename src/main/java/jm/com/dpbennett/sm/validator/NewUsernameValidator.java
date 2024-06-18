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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;

/**
 *
 * @author desbenn
 */
@FacesValidator("newUsernameValidator")
public class NewUsernameValidator implements Validator {

    private EntityManagerFactory emf;
    private EntityManager em;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (value != null) {
            if (!BusinessEntityUtils.validateName(value.toString())) {
                throw new ValidatorException(getMessage("Invalid Username"));
            } else {
                emf = Persistence.createEntityManagerFactory("JMTSPU");
                em = emf.createEntityManager();

                // Find active user by username
                User foundUser = User.findActiveJobManagerUserByUsername(em, value.toString().trim());
                if (foundUser != null) {
                    throw new ValidatorException(getMessage("Username Exists"));
                }
            }
        } else {
            throw new ValidatorException(getMessage("Invalid Username"));
        }

    }

    private FacesMessage getMessage(String messageType) {
        switch (messageType) {
            case "Invalid Username":
                return new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Invalid Username",
                        "The characters ,, #, @, $, %, ^, *, +, =, \", ', &, !, `, : and ; are not allowed.");
            case "Username Exists":
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Username Exists", 
                        "Sorry, that username is already taken. Please choose another.");
            default:
                return new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Username",
                        "The characters ,, #, @, $, %, ^, *, +, =, \", ', &, !, `, : and ; are not allowed.");
        }
    }
}
