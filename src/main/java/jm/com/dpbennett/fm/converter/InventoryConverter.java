/*
Financial Management (FM)
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
package jm.com.dpbennett.fm.converter;

import javax.faces.convert.FacesConverter;
import jm.com.dpbennett.business.entity.im.Inventory;
import jm.com.dpbennett.sm.converter.EntityConverter;

/**
 *
 * @author Desmond Bennett
 */
@FacesConverter(value = "inventoryConverter", managed = true)
public class InventoryConverter extends EntityConverter<Inventory> {

    public InventoryConverter() {
        super(Inventory.class);
    }

    @Override
    protected Long getId(Inventory inventory) {
        return inventory.getId();
    }

}
