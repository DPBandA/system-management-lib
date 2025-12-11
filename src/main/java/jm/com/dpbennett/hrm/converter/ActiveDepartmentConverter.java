/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2025  D P Bennett & Associates Limited

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
package jm.com.dpbennett.hrm.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import jm.com.dpbennett.business.entity.hrm.Department;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Desmond Bennett
 */
@FacesConverter(value = "activeDepartmentConverter", forClass = Department.class)
public class ActiveDepartmentConverter implements Converter<Department> {

    @Override
    public Department getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        // The picklist stores objects in the dual list.
        DualListModel<Department> dualList = (DualListModel<Department>) ((PickList) component).getValue();

        // Search in source
        for (Department d : dualList.getSource()) {
            if (String.valueOf(d.getId()).equals(value)) {
                return d;
            }
        }

        // Search in target
        for (Department d : dualList.getTarget()) {
            if (String.valueOf(d.getId()).equals(value)) {
                return d;
            }
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Department value) {
        return value == null ? "" : String.valueOf(value.getId());
    }
}