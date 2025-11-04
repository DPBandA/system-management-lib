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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.cert.Certification;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.hrm.Manufacturer;
import jm.com.dpbennett.business.entity.mt.PetrolCompany;
import jm.com.dpbennett.business.entity.mt.PetrolPump;
import jm.com.dpbennett.business.entity.mt.PetrolPumpNozzle;
import jm.com.dpbennett.business.entity.mt.PetrolStation;
import jm.com.dpbennett.business.entity.mt.Scale;
import jm.com.dpbennett.business.entity.mt.Seal;
import jm.com.dpbennett.business.entity.mt.Sticker;
import jm.com.dpbennett.business.entity.mt.TestMeasure;
import jm.com.dpbennett.business.entity.sc.Distributor;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public class LegalMetrologyManager extends GeneralManager implements Serializable {

    private List<PetrolStation> petrolStationSearchResultsList;
    private List<Scale> scaleSearchResultsList;
    private PetrolStation currentPetrolStation;
    private PetrolPump currentPetrolPump;
    private PetrolPumpNozzle currentPetrolPumpNozzle;
    private Scale currentScale;
    private Certification currentCertification;
    private Boolean dirty;
    //private Boolean addPetrolPump;
    private Boolean addPetrolPumpNozzle;
    private int legalMetTabViewActiveIndex;
    private String petrolStationSearchText;
    private String scaleSearchText;
    private Boolean isActivePetrolStationsOnly;
    private Boolean isActiveScalesOnly;
    private List<Certification> certifications;

    public LegalMetrologyManager() {
        init();
    }

    public void savePetrolStation() {
        EntityManager em = getEntityManager1();

        try {

            ReturnMessage message = getCurrentPetrolStation().save(em);

            if (!message.isSuccess()) {
                PrimeFacesUtils.addMessage("Save Error!",
                        "An error occured while saving this petrol station",
                        FacesMessage.SEVERITY_ERROR);
            } else {

                getCurrentPetrolStation().setIsDirty(false);
                PrimeFacesUtils.addMessage("Petrol Station Saved!",
                        "This petrol station was saved",
                        FacesMessage.SEVERITY_INFO);
            }

        } catch (Exception e) {

            System.out.println(e);
        }
    }

    @Override
    public void openDashboardTab(String title) {

        getSystemManager().setDefaultCommandTarget(":dashboardForm:dashboardAccordion:petrolStationSearchButton");

        getSystemManager().getDashboard().openTab(title);
    }

    @Override
    public void openMainViewTab(String title) {

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:petrolStationSearchButton");

        getSystemManager().getMainTabView().openTab(title);
    }

    public Certification getCurrentCertification() {
        return currentCertification;
    }

    public void setCurrentCertification(Certification currentCertification) {
        this.currentCertification = currentCertification;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }

    public ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");

    }

    public void editPetrolCompany() {
        getClientManager().setSelectedClient(getCurrentPetrolStation().getClient());
        getClientManager().setClientDialogTitle("Petrol Company Detail");

        editClient();

    }

    public void petrolCompanyDialogReturn() {
        if (getClientManager().getSelectedClient().getId() != null) {
            getCurrentPetrolStation().setClient(getClientManager().getSelectedClient());
        }
    }

    public void createNewPetrolCompany() {
        getClientManager().createNewClient(true);
        getClientManager().setClientDialogTitle("Consignee Detail");

        editClient();

    }

    public void editClient() {

        getClientManager().editSelectedClient();

    }

    public Boolean getIsPetrolStationClientNameValid() {
        return BusinessEntityUtils.validateText(currentPetrolStation.getClient().getName());
    }

    public Boolean getIsActiveScalesOnly() {

        if (isActiveScalesOnly == null) {
            isActiveScalesOnly = true;
        }

        return isActiveScalesOnly;
    }

    public void setIsActiveScalesOnly(Boolean isActiveScalesOnly) {
        this.isActiveScalesOnly = isActiveScalesOnly;
    }

    public int getNumPetrolStationsFound() {

        return getPetrolStationSearchResultsList().size();
    }

    public int getNumScalesFound() {

        return getScaleSearchResultsList().size();
    }

    @Override
    public SystemManager getSystemManager() {

        return getComplianceManager().getSystemManager();
    }

    public Boolean getIsActivePetrolStationsOnly() {

        if (isActivePetrolStationsOnly == null) {
            isActivePetrolStationsOnly = true;
        }

        return isActivePetrolStationsOnly;
    }

    public void setIsActivePetrolStationsOnly(Boolean isActivePetrolStationsOnly) {
        this.isActivePetrolStationsOnly = isActivePetrolStationsOnly;
    }

    public String getPetrolStationSearchText() {

        if (petrolStationSearchText == null) {
            petrolStationSearchText = "";
        }

        return petrolStationSearchText;
    }

    public void setPetrolStationSearchText(String petrolStationSearchText) {
        this.petrolStationSearchText = petrolStationSearchText;
    }

    public String getScaleSearchText() {

        if (scaleSearchText == null) {
            scaleSearchText = "";
        }

        return scaleSearchText;
    }

    public void setScaleSearchText(String scaleSearchText) {
        this.scaleSearchText = scaleSearchText;
    }

    public final void init() {
        reset();
    }

    @Override
    public void reset() {

        super.reset();

        dirty = false;
        //addPetrolPump = false;
        addPetrolPumpNozzle = false;

        certifications = new ArrayList<>();

    }

    public int getLegalMetTabViewActiveIndex() {
        return legalMetTabViewActiveIndex;
    }

    public void setLegalMetTabViewActiveIndex(int legalMetTabViewActiveIndex) {
        this.legalMetTabViewActiveIndex = legalMetTabViewActiveIndex;
    }

    public Scale getCurrentScale() {
        if (currentScale == null) {
            currentScale = new Scale();
        }
        return currentScale;
    }

    public void setCurrentScale(Scale currentScale) {
        this.currentScale = currentScale;
    }

    public PetrolPumpNozzle getCurrentPetrolPumpNozzle() {
        if (currentPetrolPumpNozzle == null) {
            currentPetrolPumpNozzle = new PetrolPumpNozzle();
        }
        return currentPetrolPumpNozzle;
    }

    public void setCurrentPetrolPumpNozzle(PetrolPumpNozzle currentPetrolPumpNozzle) {
        this.currentPetrolPumpNozzle = currentPetrolPumpNozzle;
    }

    public PetrolPump getCurrentPetrolPump() {
        if (currentPetrolPump == null) {
            currentPetrolPump = new PetrolPump();
        }
        return currentPetrolPump;
    }

    public void setCurrentPetrolPump(PetrolPump currentPetrolPump) {
        this.currentPetrolPump = currentPetrolPump;
    }

    public PetrolStation getCurrentPetrolStation() {
        if (currentPetrolStation == null) {
            currentPetrolStation = new PetrolStation();
        }
        return currentPetrolStation;
    }

    public void setCurrentPetrolStation(PetrolStation currentPetrolStation) {
        this.currentPetrolStation = currentPetrolStation;
    }

    // tk del if not used
    public String getScaleSearchResultsTableHeader() {

        return "App.getSearchResultsTableHeader(currentSearchParameters, getScaleSearchResultsList())";
    }

    public Boolean getDirty() {
        return dirty;
    }

    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

    public List<PetrolStation> getPetrolStationSearchResultsList() {
        if (petrolStationSearchResultsList == null) {
            petrolStationSearchResultsList = new ArrayList<>();
        }
        return petrolStationSearchResultsList;
    }

    public List<Scale> getScaleSearchResultsList() {
        if (scaleSearchResultsList == null) {
            scaleSearchResultsList = new ArrayList<>();
        }
        return scaleSearchResultsList;
    }

    public ComplianceManager getComplianceManager() {

        return BeanUtils.findBean("complianceManager");
    }

    @Override
    public EntityManager getEntityManager1() {
        return getComplianceManager().getEntityManager1();
    }

    public void doCertificationSearch() {
        // tk
        System.out.println("Impl. cert. search");
    }

    public void doPetrolStationSearch() {

        doDefaultSearch(
                getSystemManager().getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Petrol Stations",
                getPetrolStationSearchText(),
                null,
                null);

    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        switch (searchType) {
            case "Petrol Stations":
                int maxResult = SystemOption.getInteger(
                        getSystemManager().getEntityManager1(),
                        "maxSearchResults");

                if (getIsActivePetrolStationsOnly()) {
                    petrolStationSearchResultsList
                            = PetrolStation.findActive(
                                    getEntityManager1(),
                                    searchText,
                                    maxResult);
                } else {
                    petrolStationSearchResultsList
                            = PetrolStation.find(
                                    getEntityManager1(),
                                    searchText, maxResult);
                }

                break;
            default:
                break;
        }
    }

    public void openPetrolStationBrowser() {

        getSystemManager().getMainTabView().openTab("Petrol Stations");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:petrolStationSearchButton");

    }

    public void openScaleBrowser() {

        getSystemManager().getMainTabView().openTab("Scales");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:scaleSearchButton");

    }

    public void doScaleSearch() {

        //this.currentSearchParameters = currentSearchParameters;
//        scaleSearchResultsList = Scale.findScalesByDateSearchField(getEntityManager(),
//                null,
//                currentSearchParameters.getDateField(),
//                currentSearchParameters.getSearchType(),
//                currentSearchParameters.getSearchText(),
//                currentSearchParameters.getDatePeriod().getStartDate(),
//                currentSearchParameters.getDatePeriod().getEndDate(), false);
//
//        if (scaleSearchResultsList == null) {
//            scaleSearchResultsList = new ArrayList<>();
//        }
    }

    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }

    public void saveCurrentPetrolStation() {
//        EntityManager em = getEntityManager();
//        RequestContext context = RequestContext.getCurrentInstance();

//        try {
        // Validate client
//            if (!BusinessEntityUtils.validateName(currentPetrolStation.getClient().getName())) {
//                main.setInvalidFormFieldMessage("Please enter a valid client name. If the client does not exist in the list, press the 'add/edit' button to enter a new client.");
//                context.update("invalidFieldDialogForm");
//                context.execute("invalidFieldDialog.show();");
//                return;
//            } else {
//                Client currentClient = Client.findClientByName(em, currentPetrolStation.getClient().getName(), true);
//                if (currentClient == null) { // new client?
//                    main.setInvalidFormFieldMessage("Please enter a valid client name. If the client does not exist in the list, press the 'add/edit' button to enter a new client.");
//                    context.update("invalidFieldDialogForm");
//                    context.execute("invalidFieldDialog.show();");
//                    return;
//                } else {
//                    currentPetrolStation.setClient(currentClient);
//                    currentClient.setDateLastAccessed(new Date());
//                }
//            }
//
//            // validate name
//            if ((currentPetrolStation.getName() == null) || (currentPetrolStation.getName().trim().isEmpty())) {
//                main.setInvalidFormFieldMessage("Please enter a valid petrol station name");
//                context.update("invalidFieldDialogForm");
//                context.execute("invalidFieldDialog.show();");
//                return;
//            }
//
//            // validate assignee
//            Employee assignee = getEmployeeByName(em, currentPetrolStation.getLastAssignee().getName());
//            if (assignee != null) {
//                currentPetrolStation.setLastAssignee(assignee);
//            } else {
//                main.setInvalidFormFieldMessage("Please select an inspector.");
//                context.update("invalidFieldDialogForm");
//                context.execute("invalidFieldDialog.show();");
//                return;
//            }
//
//            // save 
//            em.getTransaction().begin();
//            // save new objects in list first 
//            for (PetrolPump pump : currentPetrolStation.getPetrolPumps()) {
//                for (PetrolPumpNozzle nozzle : pump.getNozzles()) {
//                    for (Certification certification : nozzle.getCertifications()) {
//                        if (certification.getId() == null) {
//                            BusinessEntityUtils.saveBusinessEntity(em, certification);
//                        }
//                    }
//
//                    if (nozzle.getId() == null) {
//                        BusinessEntityUtils.saveBusinessEntity(em, nozzle);
//                    }
//                }
//                if (pump.getId() == null) {
//                    BusinessEntityUtils.saveBusinessEntity(em, pump);
//                }
//            }
//
//            Long id = BusinessEntityUtils.saveBusinessEntity(em, currentPetrolStation);
//            em.getTransaction().commit();
//            if (id == null) {
//                context.execute("undefinedErrorDialog.show();");
//                sendErrorEmail("An error occured while saving petrol station (ID): " + currentPetrolStation.getId(),
//                        "Petrol station save error occured");
//            }
//
//            em.close();
//            setDirty(false);
//        } catch (Exception e) {
//            System.out.println(e);
//            context.execute("undefinedErrorDialog.show();");
//            sendErrorEmail("An error occured while saving petrol station (ID): " + currentPetrolStation.getId(),
//                    "Petrol station save error occured");
//        }
    }

    public void createNewScale() {

        currentScale = new Scale();

        setDirty(false);
    }

    public void createNewPetrolStation() {

        currentPetrolStation = new PetrolStation();

        PetrolPump pump = new PetrolPump();

        pump.setNumber("1");

        PetrolPumpNozzle nozzle = new PetrolPumpNozzle();
        nozzle.setNumber("1");
        nozzle.setTestMeasures("5,20");
        pump.getNozzles().add(nozzle);

        nozzle = new PetrolPumpNozzle();
        nozzle.setNumber("2");
        nozzle.setTestMeasures("5,20");
        pump.getNozzles().add(nozzle);

        currentPetrolStation.getPetrolPumps().add(pump);

        setDirty(false);

        editPetrolStation();

        openPetrolStationBrowser();

    }

    public void updatePetrolStationClient() {

//        Client client = Client.findClientByName(getEntityManager(), currentPetrolStation.getClient().getName(), true);
//        if (client != null) {
//            currentPetrolStation.setClient(client);
//        }
        setDirty(true);
    }

    public void updateCurrentScaleClient() {

//        Client client = Client.findClientByName(getEntityManager(), currentScale.getClient().getName(), true);
//        if (client != null) {
//            currentScale.setClient(client);
//        }
        setDirty(true);
    }

    public void editPetrolStationClient() {

        System.out.println("tk See how it's done for surveys");
    }

    // Edit the client via the ClientManager
    public void editScaleClient() {

//        ClientManager clientManager = Application.findBean("clientManager");
        // Try to retrieve the client from the database if it exists
//        Client client = Client.findClientByName(getEntityManager(), currentScale.getClient().getName(), true);
//        if (client != null) {
//            currentScale.setClient(client);
//            clientManager.setClient(client);
//        } else {
//            clientManager.setClient(currentPetrolStation.getClient());
//        }
//
//        clientManager.setComponentsToUpdate(":unitDialogForms:scaleClient");
        setDirty(true);
    }

    public void updatePetrolStationLastAssignee() {

        // tk check if the following is still necessary
        //currentPetrolStation.setLastAssignee(getEmployeeByName(getEntityManager(), currentPetrolStation.getLastAssignee().getName()));
        setDirty(true);
    }

    public void cancelEdit() {

        setDirty(false);

    }

    public void updatePetrolStation() {
        setDirty(true);
    }

    public void updatePetrolPump() {
        setDirty(true);
    }

    /**
     * Check if a nozzle has the certificate of its assoc. pump. Presently the
     * certificate issue and expiry dates are used for this
     *
     * @param pump
     * @return
     */
    private Boolean doesNozzleHavePumpCertificate(PetrolPump pump, PetrolPumpNozzle nozzle) {

        // tk update with pump certifications
//        Certification pumpCert = pump.getCertification();
//
//        for (Certification nozzleCert : nozzle.getCertifications()) {
//            if (BusinessEntityUtils.areDatesEqual(pumpCert.getDateIssued(), nozzleCert.getDateIssued())
//                    && BusinessEntityUtils.areDatesEqual(pumpCert.getExpiryDate(), nozzleCert.getExpiryDate())) {
//                return true;
//            }
//        }
        return false;
    }

    private void updatePetroPumpNozzleCertificates(PetrolPump pump, PetrolPumpNozzle nozzle) {
        // tk update i needed when certificates code is updated.

//        if (!doesNozzleHavePumpCertificate(pump, nozzle)) {
//            nozzle.getCertifications().add(new Certification(pump.getCertification()));
//        }
    }

    public void updatePetrolStationDateCertified() {
        // tk update pump/nozzle certification     
//        Date certDate = getCurrentPetrolStation().getCertification().getDateIssued();
//        Date expDate = BusinessEntityUtils.getModifiedDate(certDate, Calendar.MONTH, 6); //tk 6 shud be in options
//
//        getCurrentPetrolStation().getCertification().setExpiryDate(expDate);

        for (PetrolPump pump : getCurrentPetrolStation().getPetrolPumps()) {
//            pump.getCertification().setDateIssued(certDate); // tk set in nozzles too
//            pump.getCertification().setExpiryDate(expDate);

            // tk update nozzle
            for (PetrolPumpNozzle nozzle : pump.getNozzles()) {
                updatePetroPumpNozzleCertificates(pump, nozzle);
            }
        }

        setDirty(true);
    }

    public void updatePetrolStationDateCertificationDue() {
        // tk update pump/nozzle certification          

//        Date date = getCurrentPetrolStation().getCertification().getExpiryDate();
        for (PetrolPump pump : getCurrentPetrolStation().getPetrolPumps()) {
//            pump.getCertification().setExpiryDate(date); // tk set in nozzles too

            // tk update nozzle
            for (PetrolPumpNozzle nozzle : pump.getNozzles()) {
                updatePetroPumpNozzleCertificates(pump, nozzle);
            }

        }

        setDirty(true);
    }

    public void updatePetrolPumpDateCertified() {

        // tk update when certificates code is updated
//        Date certDate = getCurrentPetrolPump().getCertification().getDateIssued();
//        Date expDate = BusinessEntityUtils.getModifiedDate(certDate, Calendar.MONTH, 6); //tk 6 shud be in options
//
//        getCurrentPetrolPump().getCertification().setExpiryDate(expDate);
        // tk update nozzle
        for (PetrolPumpNozzle nozzle : getCurrentPetrolPump().getNozzles()) {
            updatePetroPumpNozzleCertificates(getCurrentPetrolPump(), nozzle);
        }

        // tk add new certiif required for nozzle
        setDirty(true);
    }

    public void updatePetrolPumpDateCertificationDue() {
        // tk update nozzle
        for (PetrolPumpNozzle nozzle : getCurrentPetrolPump().getNozzles()) {
            updatePetroPumpNozzleCertificates(getCurrentPetrolPump(), nozzle);
        }

        setDirty(true);
    }

    public void updatePetrolPumpNozzle() {
        setDirty(true);
    }

    public void editPetrolPump() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/legalmet/petrolPumpDialog",
                options, null);

    }

    public void editPetrolStation() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/legalmet/petrolStationDialog",
                options, null);

    }

    public void editScale() {
    }

    public Boolean getCanDeletePetrolPump() {

        return getCurrentPetrolStation().getPetrolPumps().size() != 1;
    }

    public void deletePetrolPump() {

//        RequestContext context = RequestContext.getCurrentInstance();
//
//        getCurrentPetrolStation().getPetrolPumps().remove(currentPetrolPump);
//
//        currentPetrolPump = new PetrolPump();
//        currentPetrolPump.setNumber("?");
//
//        context.update("unitDialogForms:petrolPumpTable");
        setDirty(true);
    }

    public void deletePetrolPumpNozzle() {

//        RequestContext context = RequestContext.getCurrentInstance();
//
//        getCurrentPetrolPump().getNozzles().remove(currentPetrolPumpNozzle);
//
//        currentPetrolPumpNozzle = new PetrolPumpNozzle();
//        currentPetrolPumpNozzle.setNumber("?");
//
//        context.update("unitDialogForms:petrolPumpNozzleTable");
        setDirty(true);
    }

    public void createNewPump() {

        // tk this means the pump will added to the station when oked.
        // check if this is still need
        //addPetrolPump = true;
        currentPetrolPump = new PetrolPump();
        currentPetrolPump.setOwnerId(getCurrentPetrolStation().getId());

//        PetrolPumpNozzle nozzle = new PetrolPumpNozzle();
//        nozzle.setNumber("1");
//        nozzle.setTestMeasures("5,20");
//        currentPetrolPump.getNozzles().add(nozzle);
//        nozzle = new PetrolPumpNozzle();
//        nozzle.setNumber("2");
//        nozzle.setTestMeasures("5,20");
//        currentPetrolPump.getNozzles().add(nozzle);
        // tk open external dialog here
        editPetrolPump();

    }

    public void createNewCertification() {

    }

    public void createNewNozzle() {

//        RequestContext context = RequestContext.getCurrentInstance();
//
//        // this means the nozzle will added to the station when oked.
//        addPetrolPumpNozzle = true;
//
//        currentPetrolPumpNozzle = new PetrolPumpNozzle();
//        currentPetrolPumpNozzle.setTestMeasures("5,20");
//
//        context.update("unitDialogForms:petrolPumpNozzleDetailDialog");
    }

    public void updateCurrentPetrolPumpManufacturer() {

        try {
//            EntityManager em = getEntityManager();
//
//            // get/validate pump manufacturer
//            Manufacturer manufacturer = App.getValidManufacturer(em, getCurrentPetrolPump().getManufacturer().getName());
//            if (manufacturer != null) {
//                getCurrentPetrolPump().setManufacturer(manufacturer);
//            } else {
            ////                getCurrentPetrolPump().setManufacturer(App.getDefaultManufacturer(em, "--"));
//            }
//            em.close();

            setDirty(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updatePetrolPumps() {
//        RequestContext context = RequestContext.getCurrentInstance();

//        if (addPetrolPump) {
//            currentPetrolStation.getPetrolPumps().add(currentPetrolPump);
//            addPetrolPump = false;
//            setDirty(true);
//        }
    }

    public void updatePetrolPumpNozzlesList() {
//        RequestContext context = RequestContext.getCurrentInstance();

        if (addPetrolPumpNozzle) {
            currentPetrolPump.getNozzles().add(currentPetrolPumpNozzle);
            addPetrolPumpNozzle = false;
            setDirty(true);
        }

    }

    public void cancelPetrolPumpEdit() {

        //addPetrolPump = false;
        
        PrimeFacesUtils.closeDialog(null);
    }

    public void cancelPetrolPumpNozzleEdit() {
        addPetrolPumpNozzle = false;

    }

    public Boolean getCanDeletePetrolPumpNozzle() {

        return getCurrentPetrolPump().getNozzles().size() != 1;
    }

    public void updatePetrolPumpNozzleManufacturer() {
        try {
//            EntityManager em = getEntityManager();
//            Manufacturer manufacturer = App.getValidManufacturer(em, getCurrentPetrolPumpNozzle().getManufacturer().getName());
//            if (manufacturer != null) {
//                getCurrentPetrolPumpNozzle().setManufacturer(manufacturer);
//            } else {
//                getCurrentPetrolPumpNozzle().setManufacturer(App.getDefaultManufacturer(em, "--"));
//            }
//            em.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateCurrentScaleManufacturer() {
        try {
//            EntityManager em = getEntityManager();
//
//            Manufacturer manufacturer = App.getValidManufacturer(em, getCurrentScale().getManufacturer().getName());
//            if (manufacturer != null) {
//                getCurrentScale().setManufacturer(manufacturer);
//            } else {
//                getCurrentScale().setManufacturer(App.getDefaultManufacturer(em, "--"));
//            }
//            em.close();

            setDirty(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void saveCurrentScale() {
//        RequestContext context = RequestContext.getCurrentInstance();

        try {
//            EntityManager em = getEntityManager();
//            // validate client          
//            if (!BusinessEntityUtils.validateName(currentScale.getClient().getName())) {
//                main.setInvalidFormFieldMessage("Please enter a valid client name. If the client does not exist in the list, press the 'add/edit' button to enter a new client.");
//                context.update("invalidFieldDialogForm");
//                context.execute("invalidFieldDialog.show();");
//                return;
//            } else {
//                Client currentClient = Client.findClientByName(em, currentScale.getClient().getName(), true);
//                if (currentClient == null) { // new client?
//                    main.setInvalidFormFieldMessage("Please enter a valid client name. If the client does not exist in the list, press the 'add/edit' button to enter a new client.");
//                    context.update("invalidFieldDialogForm");
//                    context.execute("invalidFieldDialog.show();");
//                    return;
//                } else {
//                    currentScale.setClient(currentClient);
//                    currentClient.setDateLastAccessed(new Date());
//                }
//            }
//
//            // get valid manufacturer
//            Manufacturer manufacturer = App.getValidManufacturer(em, getCurrentScale().getManufacturer().getName());
//            if (manufacturer != null) {
//                getCurrentScale().setManufacturer(manufacturer);
//            } else {
//                getCurrentScale().setManufacturer(App.getDefaultManufacturer(em, "--"));
//            }
//
//            em.getTransaction().begin();
//            // save list of objects firt
//            for (Sticker sticker : currentScale.getStickers()) {
//                if (sticker.getId() == null) {
//                    BusinessEntityUtils.saveBusinessEntity(em, sticker);
//                }
//            }
//            BusinessEntityUtils.saveBusinessEntity(em, currentScale);
//            em.getTransaction().commit();
//
//            em.close();

            setDirty(false);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

//    public void closeScaleDialog2(CloseEvent closeEvent) {
//        closeScaleDialog1(null);
//    }

//    public void closeScaleDialog1(ActionEvent actionEvent) {
//        RequestContext context = RequestContext.getCurrentInstance();
//
//        // prompt to save modified job before attempting to create new job
//        if (getDirty()) {
//            context.update("unitDialogForms:scaleSaveConfirm");
//            context.execute("scaleSaveConfirmDialog.show();");
//
//            return;
//        }

//        setDirty(false);
        // refresh search if possible
//        if (currentSearchParameters != null) {
//            doScaleSearch(currentSearchParameters);
//        }
//    }

    public void updateScale() {
        setDirty(true);
    }

    public void deleteScale() {
        System.out.println("Not yet impl.");
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public void buildGasStationDatabase(
            EntityManager em,
            int sheetNumber,
            String company,
            String station,
            ArrayList<TestMeasure> measures) { // when set to null the each row on sheet is used as company branch. Otherwise each row is company.
        // cell indices
        final int NO = 0;
        final int PARISH = 1;
        final int STATION_NAME = 2;
        final int ADDRESS = 3;
        final int NUMBER_OF_PUMPS = 4;
        final int REJECTED_OR_NOT_WORKING = 5;
        final int CERTIFIED_ON = 6;
        final int EXPIRY_DATE = 7;
        final int STATUS = 8;
        final int RATE = 9;
        final int FINAL_AMOUNT = 10;
        final int NOZZLES_PER_PUMP = 2;
        int rowCount = 0;
        int cellCount = 0;
        PetrolCompany petrolCompany = new PetrolCompany();
        //
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream("GAS STATION DATABASE 2009.xls"));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            em.getTransaction().begin();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                if (i == sheetNumber) {
                    HSSFSheet sheet = wb.getSheetAt(i);
                    petrolCompany.setName(company);
                    for (rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows(); rowCount++) {
//                        for (HSSFRow row : sheet.g) {
                        if (rowCount > 0) { // make sure this is not the header
                            HSSFRow row = sheet.getRow(rowCount);
                            cellCount = 0;
                            // create new station
                            PetrolStation petrolStation = new PetrolStation();
                            if (row != null) {
                                for (cellCount = 0; cellCount < row.getPhysicalNumberOfCells(); cellCount++) {
                                    // populate petrol company and stations with data
                                    HSSFCell cell = row.getCell(cellCount);
                                    if (cell != null) {
                                        switch (cellCount) {
                                            case NO:
                                                break;
                                            case PARISH:
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
//                                                    petrolStation.getBillingAddress().setStateOrProvince(cell.getStringCellValue());
//                                                }
                                                break;
                                            case STATION_NAME: // add station only if name if valid
//                                                if ((cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
//                                                        && (!cell.getRichStringCellValue().getString().trim().equals(""))) {
//                                                    petrolCompany.getPetrolStations().add(petrolStation);
//                                                    petrolStation.setName(cell.getRichStringCellValue().getString());
//                                                }
                                                break;
                                            case ADDRESS:
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
//                                                    petrolStation.getBillingAddress().setAddressLine1(cell.getStringCellValue());
//                                                }
                                                break;
                                            case NUMBER_OF_PUMPS:
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//                                                    int numberOfNozzles = (int) cell.getNumericCellValue();
//                                                    // create petrol pumps and nozzles. use max 2 nozzles per pump.
//                                                    int numberOfPumps = (int) (numberOfNozzles / NOZZLES_PER_PUMP);
//                                                    int oddNozzles = numberOfNozzles % NOZZLES_PER_PUMP;
//
//                                                    if (numberOfPumps > 0) {
//                                                        for (int j = 0; j < numberOfPumps; j++) {
//                                                            PetrolPump petrolPump = new PetrolPump();
//                                                            petrolStation.getPetrolPumps().add(petrolPump);
//                                                            // create NOZZLES_PER_PUMP nozzles
//                                                            for (int k = 0; k < NOZZLES_PER_PUMP; k++) {
//                                                                PetrolPumpNozzle nozzle = new PetrolPumpNozzle(measures,
//                                                                        getDefaultSeal(em, "--"),
//                                                                        getDefaultSticker(em, "--"));
//                                                                petrolPump.getNozzles().add(nozzle);
//                                                            }
//                                                        }
//                                                        // create a pump with odd no. nozzles if any
//                                                        if (oddNozzles > 0) {
//                                                            PetrolPump petrolPump = new PetrolPump();
//                                                            petrolStation.getPetrolPumps().add(petrolPump);
//                                                            // create NOZZLES_PER_PUMP nozzles
//                                                            petrolPump.setNozzles(new ArrayList<PetrolPumpNozzle>());
//                                                            for (int k = 0; k < oddNozzles; k++) {
//                                                                PetrolPumpNozzle nozzle = new PetrolPumpNozzle(measures,
//                                                                        getDefaultSeal(em, "--"),
//                                                                        getDefaultSticker(em, "--"));
//                                                                petrolPump.getNozzles().add(nozzle);
//                                                            }
//                                                        }
//                                                    } else if (oddNozzles > 0) {
//                                                        PetrolPump petrolPump = new PetrolPump();
//                                                        petrolStation.getPetrolPumps().add(petrolPump);
//                                                        // create NOZZLES_PER_PUMP nozzles
//                                                        petrolPump.setNozzles(new ArrayList<PetrolPumpNozzle>());
//                                                        for (int k = 0; k < oddNozzles; k++) {
//                                                            PetrolPumpNozzle nozzle = new PetrolPumpNozzle(measures,
//                                                                    getDefaultSeal(em, "--"),
//                                                                    getDefaultSticker(em, "--"));
//                                                            petrolPump.getNozzles().add(nozzle);
//                                                        }
//                                                    } else {
//                                                        System.out.println("Station has no pump!!");
//                                                    }
//                                                    System.out.println("No. of 2 nozzle pumps: " + numberOfPumps + ", Odd nozzles: " + oddNozzles);
//                                                }
                                                break;
                                            case REJECTED_OR_NOT_WORKING:
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//                                                    int nozzlesRejected = (int) cell.getNumericCellValue();
//                                                    if (nozzlesRejected > 0) {
//                                                        int pumpsToUpdate = (int) (nozzlesRejected / NOZZLES_PER_PUMP);
//                                                        int oddNozzlesToUpdate = nozzlesRejected % NOZZLES_PER_PUMP;
//                                                        // fill out all pump nozzles here
//                                                        if (pumpsToUpdate > 0) {
//                                                            for (int j = 0; j < pumpsToUpdate; j++) {
//                                                                PetrolPump pertrolPump = petrolStation.getPetrolPumps().get(j);
//                                                                for (PetrolPumpNozzle nozzle : pertrolPump.getNozzles()) {
//                                                                    nozzle.setComments("Rejected/Not working");
//                                                                }
//                                                            }
//                                                            // fill out the odd number pump nozzles
//                                                            if (oddNozzlesToUpdate > 0) {
//                                                                System.out.println("Bad nozzles: " + oddNozzlesToUpdate);
//                                                                PetrolPump pertrolPump = petrolStation.getPetrolPumps().get(pumpsToUpdate);
//                                                                for (int j = 0; j < oddNozzlesToUpdate; j++) {
//                                                                    pertrolPump.getNozzles().get(j).setComments("Rejected/Not working");
//                                                                }
//                                                            }
//                                                        } else if (oddNozzlesToUpdate > 0) {
//                                                            System.out.println("Bad nozzles: " + oddNozzlesToUpdate);
//                                                            PetrolPump pertrolPump = petrolStation.getPetrolPumps().get(0);
//                                                            if (!pertrolPump.getNozzles().isEmpty()) {
//                                                                for (int j = 0; j < oddNozzlesToUpdate; j++) {
//                                                                    pertrolPump.getNozzles().get(j).setComments("Rejected/Not working");
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
                                                break;
                                            case CERTIFIED_ON:
                                                // set the date in all pumps for now
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//                                                    Date certifiedOn = cell.getDateCellValue();
//                                                    if (certifiedOn != null) {
//                                                        int numberOfPumps = petrolStation.getPetrolPumps().size();
//                                                        for (int j = 0; j < numberOfPumps; j++) {
//                                                            PetrolPump petrolPump = petrolStation.getPetrolPumps().get(j);
//                                                            petrolPump.getCertification().setDateIssued(certifiedOn);
//                                                        }
//                                                    }
//                                                }
                                                break;
                                            case EXPIRY_DATE:
                                                // set the date in all pumps for now
//                                                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//                                                    Date expiryDate = cell.getDateCellValue();
//                                                    if (expiryDate != null) {
//                                                        int numberOfPumps = petrolStation.getPetrolPumps().size();
//                                                        for (int j = 0; j < numberOfPumps; j++) {
//                                                            PetrolPump petrolPump = petrolStation.getPetrolPumps().get(j);
//                                                            petrolPump.getCertification().setExpiryDate(expiryDate);
//                                                        }
//                                                    }
//                                                }
                                                break;
                                            case STATUS:
                                                break;
                                            case RATE:
                                                break;
                                            case FINAL_AMOUNT:
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // save the petrol company
                    System.out.println("Saving Petrol company: " + company);
                    BusinessEntityUtils.saveBusinessEntity(em, petrolCompany);
                    em.getTransaction().commit();
                    // stat
                    System.out.println("Number of stations imported: " + petrolCompany.getPetrolStations().size());

                    return;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public Manufacturer getDefaultManufacturer(EntityManager em,
            String name) {
        Manufacturer manufacturer = Manufacturer.findManufacturerByName(em, name);

        if (manufacturer == null) {
            manufacturer = new Manufacturer();
            em.getTransaction().begin();
            manufacturer.setName(name);
            BusinessEntityUtils.saveBusinessEntity(em, manufacturer);
            em.getTransaction().commit();
        }

        return manufacturer;
    }

    public Distributor getDefaultDistributor(EntityManager em,
            String name) {
        Distributor distributor = Distributor.findDistributorByName(em, name);

        if (distributor == null) {
            distributor = new Distributor();
            em.getTransaction().begin();
            distributor.setName(name);
            BusinessEntityUtils.saveBusinessEntity(em, distributor);
            em.getTransaction().commit();
        }

        return distributor;
    }

    public PetrolCompany getDefaultPetrolCompany(EntityManager em,
            String name) {
        PetrolCompany petrolCompany = PetrolCompany.findPetrolCompanyByName(em, name);

        if (petrolCompany == null) {
            petrolCompany = new PetrolCompany();

            em.getTransaction().begin();
            petrolCompany.setName(name);
            BusinessEntityUtils.saveBusinessEntity(em, petrolCompany);
            em.getTransaction().commit();
        }

        return petrolCompany;
    }

    public Contact getDefaultContact(EntityManager em, String firstName, String lastName) {

        Contact contact = Contact.findContactByName(em, firstName, lastName);

        // create employee if it does not exist
        if (contact == null) {
            contact = new Contact();
            contact.setFirstName(firstName);
            contact.setLastName(lastName);
            // save
            em.getTransaction().begin();
            BusinessEntityUtils.saveBusinessEntity(em, contact);
            em.getTransaction().commit();
        }

        return contact;
    }

    public Seal getDefaultSeal(EntityManager em,
            String number) {
//        Seal seal = Seal.findSealByNumber(em, number);
//
//        if (seal == null) {
//            seal = new Seal();
//
//            em.getTransaction().begin();
//            seal.setNumber(number);
//            BusinessEntityUtils.saveBusinessEntity(em, seal);
//            em.getTransaction().commit();
//        }

        return null; //seal;
    }

    public Sticker getDefaultSticker(EntityManager em,
            String number) {
        Sticker sticker = Sticker.findStickerByNumber(em, number);

        if (sticker == null) {
            sticker = new Sticker();

            //em.getTransaction().begin();
            sticker.setNumber(number);
            BusinessEntityUtils.saveBusinessEntity(em, sticker);
            //em.getTransaction().commit();
        }

        return sticker;
    }

    public Manufacturer getValidManufacturer(EntityManager em, String name) {

        if ((name == null) || (name.equals(""))) {
            return null;
        } else if (!BusinessEntityUtils.validateName(name)) {
            return null;
        } else {
            Manufacturer currentManufacturer = Manufacturer.findManufacturerByName(em, name);
            if (currentManufacturer == null) {
                // create new
                currentManufacturer = new Manufacturer();
                // replace double quotes with two single quotes to avoid query issues
                currentManufacturer.setName(name.replaceAll("\"", "''"));

                return currentManufacturer;
            } else { // create and return new manufacturer
                return currentManufacturer;
            }
        }
    }

    public void insertStickers(EntityManager em, Date dateAssigned, Integer startNumber, Integer endNumber) {
        em.getTransaction().begin();

        for (int i = startNumber; i < endNumber + 1; i++) {
            System.out.println("Inserting sticker number: " + i);
            Sticker sticker = new Sticker(Integer.toString(i), dateAssigned);
            BusinessEntityUtils.saveBusinessEntity(em, sticker);
        }

        em.getTransaction().commit();
    }

}
