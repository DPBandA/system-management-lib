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

import java.util.HashMap;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

/**
 *
 * @author D P Bennett
 */
public class PrimeFacesUtils {

    public static void openDialog(Object entity,
            String outcome,
            Boolean modal,
            Boolean draggable,
            Boolean resizable,
            Integer contentHeight,
            Integer contentWidth) {

        PrimeFacesUtils.openDialog(null, outcome, modal, draggable, resizable, true, contentHeight, contentWidth);

    }

    public static void openDialog(Object entity,
            String outcome,
            Boolean modal,
            Boolean draggable,
            Boolean resizable,
            Boolean closable,
            Integer contentHeight,
            Integer contentWidth) {

        Map<String, Object> options = new HashMap<>();
        options.put("modal", modal);
        options.put("draggable", draggable);
        options.put("resizable", resizable);
        options.put("closable", closable);
        options.put("contentHeight", contentHeight);
        options.put("contentWidth", contentWidth);

        PrimeFaces.current().dialog().openDynamic(outcome, options, null);

    }

    public static void closeDialog(Object data) {
        PrimeFaces.current().dialog().closeDynamic(data);
    }

    public static void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
}
