/*
Financial Management (FM)
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
package jm.com.dpbennett.fm.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.im.Inventory;
import jm.com.dpbennett.sm.converter.ConverterAdapter;

/**
 *
 * @author desbenn
 */
@FacesConverter("activeInventoryConverter")
public class ActiveInventoryConverter extends ConverterAdapter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        EntityManager em = (EntityManager) component.getAttributes().get("em");
        Inventory inventory = 
                Inventory.findActiveByName(em, value);

        if (inventory == null) {
            inventory = new Inventory(value);
        }
       
        return inventory;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Inventory) value).getName();
    }
    
    

}
