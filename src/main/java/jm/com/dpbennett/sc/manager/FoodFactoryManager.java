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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jm.com.dpbennett.business.entity.hrm.Manufacturer;
import jm.com.dpbennett.business.entity.sc.FactoryInspection;
import jm.com.dpbennett.business.entity.sc.FactoryInspectionComponent;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;

/**
 *
 * @author Desmond Bennett
 */
public class FoodFactoryManager extends GeneralManager implements Serializable {

    private List<Manufacturer> factories;
    private String foodFactorySearchText;
    private Manufacturer selectedFactory;
    private Boolean isActiveFactoriesOnly;

    public FoodFactoryManager() {
        init();
    }

    @Override
    public void openDashboardTab(String title) {

        getSystemManager().setDefaultCommandTarget(":dashboardForm:dashboardAccordion:foodFactorySearchButton");

        getSystemManager().getDashboard().openTab(title);
    }

    @Override
    public void openMainViewTab(String title) {

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:foodFactorySearchButton");

        getSystemManager().getMainTabView().openTab(title);
    }

    public final void init() {
        reset();
    }

    @Override
    public void reset() {

        super.reset();

        factories = new ArrayList<>();
        isActiveFactoriesOnly = true;

    }

    public Boolean getIsActiveFactoriesOnly() {
        return isActiveFactoriesOnly;
    }

    public void setIsActiveFactoriesOnly(Boolean isActiveFactoriesOnly) {
        this.isActiveFactoriesOnly = isActiveFactoriesOnly;

        getHumanResourceManager().setIsActiveManufacturersOnly(isActiveFactoriesOnly);
    }

    @Override
    public SystemManager getSystemManager() {

        return getComplianceManager().getSystemManager();
    }

    public void openFactoryBrowser() {

        getSystemManager().getMainTabView().openTab("Factories");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:foodFactorySearchButton");
    }

    public Manufacturer getSelectedFactory() {

        if (selectedFactory == null) {
            return new Manufacturer();
        }

        return selectedFactory;
    }

    public void setSelectedFactory(Manufacturer selectedFactory) {
        this.selectedFactory = selectedFactory;

        getHumanResourceManager().setSelectedManufacturer(selectedFactory);
    }

    public void editSelectedFactory() {

        getHumanResourceManager().editSelectedManufacturer();

    }

    public int getNumFactoriesFound() {
        return getHumanResourceManager().getFoundManufacturers().size();
    }

    public String getFoodFactorySearchText() {

        if (foodFactorySearchText == null) {
            foodFactorySearchText = getHumanResourceManager().getManufacturerSearchText();
        }

        return foodFactorySearchText;
    }

    public void setFoodFactorySearchText(String foodFactorySearchText) {

        getHumanResourceManager().setManufacturerSearchText(foodFactorySearchText);

        this.foodFactorySearchText = foodFactorySearchText;
    }

    public void doFoodFactorySearch() {

        if (getHumanResourceManager().getIsActiveManufacturersOnly()) {
            factories = Manufacturer.findActiveManufacturersByAnyPartOfName(
                    getHumanResourceManager().getEntityManager1(),
                    getHumanResourceManager().getManufacturerSearchText());
        } else {
            factories = Manufacturer.findManufacturersByAnyPartOfName(
                    getHumanResourceManager().getEntityManager1(),
                    getHumanResourceManager().getManufacturerSearchText());
        }
    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    public ComplianceManager getComplianceManager() {
        return BeanUtils.findBean("complianceManager");
    }

    public List<Manufacturer> getFactories() {
        Collections.sort(factories);

        return factories;
    }

    public void setFactories(List<Manufacturer> factories) {
        this.factories = factories;
    }

    public void createNewFoodFactory() {

        getHumanResourceManager().createNewManufacturer(true);
        getHumanResourceManager().getSelectedManufacturer().setType("Food Factory");
        getHumanResourceManager().editSelectedManufacturer();

        openFactoryBrowser();
    }

    // tk Check if it will be used
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
