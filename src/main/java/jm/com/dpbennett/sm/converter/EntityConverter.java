/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
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
package jm.com.dpbennett.sm.converter;

/**
 *
 * @author Desmond Bennett
 */
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.persistence.EntityManager;
import java.io.Serializable;
import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

public abstract class EntityConverter<T extends Serializable> implements Converter {

    private final Class<T> entityClass;

    protected EntityConverter(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Decode HTML entities from PrimeFaces (&#x2F;, &amp;, &#39;)
        String decodedValue = StringEscapeUtils.unescapeHtml4(value);

        // If value is numeric → treat as ID
        Long id = null;
        try {
            id = Long.valueOf(decodedValue);
        } catch (NumberFormatException ignore) {
            // Not an ID → treat as text (name lookup style)
        }

        // 🔹 1. PICKLIST LOOKUP (in-memory, fastest)
        if (component instanceof PickList) {

            PickList pickList = (PickList) component;
            DualListModel<T> model = (DualListModel<T>) pickList.getValue();

            if (id != null) {
                for (T item : model.getSource()) {
                    if (getId(item).equals(id)) {
                        return item;
                    }
                }
                for (T item : model.getTarget()) {
                    if (getId(item).equals(id)) {
                        return item;
                    }
                }
            }
        }

        // 🔹 2. DB LOOKUP USING ENTITY MANAGER
        EntityManager em = (EntityManager) component.getAttributes().get("em");

        if (em != null && id != null) {
            return em.find(entityClass, id);
        }

        // 🔹 3. TEXT VALUES (for Autocomplete that matches by name)
        return createFromString(decodedValue);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        if (value == null) {
            return "";
        }

        Long id = getId((T) value);

        return (id == null) ? "" : String.valueOf(id);
    }

    // -------------------------------------------------------
    // ABSTRACT METHODS
    // -------------------------------------------------------

    /**
     * Each entity must return its ID.
     */
    protected abstract Long getId(T entity);

    /**
     * If autocomplete needs to create new instances (e.g. by name),
     * override this in your converter.
     */
    protected T createFromString(String value) {
        throw new UnsupportedOperationException(
            "createFromString() not implemented for " + entityClass.getSimpleName()
        );
    }
}
