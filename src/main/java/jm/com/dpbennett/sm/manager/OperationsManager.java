/*
Operations Management (OM)
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
package jm.com.dpbennett.sm.manager;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.sm.util.BeanUtils;

/**
 *
 * @author Desmond Bennett
 */
public final class OperationsManager extends GeneralManager {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory SMPU;
    
    public OperationsManager() {
        init();
    }
    
    public void init() {
        reset();
    }

    @Override
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public EntityManagerFactory getSMPU() {

        return SMPU;
    }
    
}
