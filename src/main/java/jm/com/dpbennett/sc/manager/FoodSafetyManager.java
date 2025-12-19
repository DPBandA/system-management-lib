/*
Standards Compliance (SC) 
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
package jm.com.dpbennett.sc.manager;

import java.io.Serializable;
import jm.com.dpbennett.business.entity.sc.FactoryInspection;
import jm.com.dpbennett.business.entity.sc.FactoryInspectionComponent;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;

/**
 *
 * @author Desmond Bennett
 */
public class FoodSafetyManager extends GeneralManager implements Serializable {

    public FoodSafetyManager() {
        init();
    }

    public final void init() {
        reset();
    }

    @Override
    public void reset() {

        super.reset();
        
        setName("foodSafetyManager");

    }

    @Override
    public SystemManager getSystemManager() {

        return getComplianceManager().getSystemManager();
    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    public ComplianceManager getComplianceManager() {
        return BeanUtils.findBean("complianceManager");
    }

    // tk Check if this will be used considering that inspections are handled by SC.
    public void createFactoryInspectionComponents(FactoryInspection inspection) {
        // ESTABLISHMENT ENVIRONS category
        FactoryInspectionComponent component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Location", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Waste Disposal", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Building Exterior", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Drainage", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Yard Storage", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Tank", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("ESTABLISHMENT ENVIRONS", "Other Buildings", false);
        inspection.getInspectionComponents().add(component);
        // BUILDING INTERIOR
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Design & Contruction", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Pipes/Conduits/Beams", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Other Overhead Structures", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Lighting/Lighting Intensity", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Ventilation", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Drainage Systems", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Maintenance of walls", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Maintenance of floors", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Maintenance of ceilings", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Gutters/drains", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("BUILDING INTERIOR", "Screens/Blowers/Insecticutor", false);
        inspection.getInspectionComponents().add(component);
        // RECEIVING
        component = new FactoryInspectionComponent("RECEIVING", "Raw Material Infestation", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("RECEIVING", "Raw Material Infestation", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("RECEIVING", "Raw Material Infestation", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("RECEIVING", "Raw Material Infestation", false);
        inspection.getInspectionComponents().add(component);
        component = new FactoryInspectionComponent("RECEIVING", "Raw Material Infestation", false);
        inspection.getInspectionComponents().add(component);
    }
}
