/*
LabelPrint 
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
package jm.com.dpbennett.mt.manager;

import com.google.zxing.WriterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.mt.EnergyLabel;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.business.entity.util.QRCodeGenerator;
import jm.com.dpbennett.business.entity.util.ReturnMessage;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.StreamedContent;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Desmond Bennett
 */
public class EnergyLabelManager extends GeneralManager
        implements Serializable {

    private List<EnergyLabel> foundEnergyLabels;
    private EnergyLabel selectedEnergyLabel;
    private String energyLabelSearchText;
    private Document svgDocument;

    /**
     * Creates a new instance of LabelManager
     */
    public EnergyLabelManager() {
        init();
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Label Browser":
                getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:energyLabelSearchButton");

                return true;

            default:
                return false;
        }
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 600;
    }

    public void onLabelDialogTabChange(TabChangeEvent event) {
        switch (event.getTab().getTitle()) {
            case "Preview":
                break;
            default:
                break;
        }

    }

    public String getEnergyLabelSearchText() {
        return energyLabelSearchText;
    }

    public void setEnergyLabelSearchText(String energyLabelSearchText) {
        this.energyLabelSearchText = energyLabelSearchText;
    }

    @Override
    public void initMainTabView() {

        getMainTabView().reset(getUser());

        Modules module = Modules.findActiveModuleByName(getEntityManager1(),
                "energyLabelManager");
        if (module != null) {
            getMainTabView().openTab(module.getDashboardTitle());
        }

    }

    public void openEnergyLabelBrowser() {

        getMainTabView().openTab("Label Browser");
        
        getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:energyLabelSearchButton");

    }

    public void openComplianceSettingsTab() {

        getSystemManager().doSystemOptionSearch("Compliance");

        getMainTabView().openTab("Compliance");
    }

    public void openEnergyEfficiencySettingsTab() {

        getSystemManager().doSystemOptionSearch("Energy Efficiency");

        getMainTabView().openTab("Energy Efficiency");
    }

    public void openLabelPrintSettingsTab() {

        getSystemManager().doSystemOptionSearch("LabelPrint");

        getMainTabView().openTab("LabelPrint");
    }

    public void energyLabelDialogReturn() {

        if (selectedEnergyLabel.getIsDirty()) {
            PrimeFacesUtils.addMessage("Energy Label was NOT saved!",
                    "The recently edited energy label was not saved", FacesMessage.SEVERITY_WARN);
            PrimeFaces.current().ajax().update("appForm:growl3");
            selectedEnergyLabel.setIsDirty(false);
        } else {
            doEnergyLabelSearch();
        }

    }

    @Override
    public void onNotificationSelect(SelectEvent event) {
        EntityManager em = getEntityManager1();

        Notification notification = Notification.findNotificationByNameAndOwnerId(
                em,
                (String) event.getObject(),
                getUser().getId(),
                false);

        if (notification != null) {

            handleSelectedNotification(notification);

            notification.setActive(false);
            notification.save(em);
        }
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "LabelPrintLogoURL");
    }

    @Override
    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageHeight");
    }

    @Override
    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageWidth");
    }

    @Override
    public String getApplicationSubheader() {
        return "Energy label generation and printing";
    }

    public List<SelectItem> getEnergyEfficiencyProductTypes() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "energyEfficiencyProductTypes");
    }

    public List<SelectItem> getDefrostTypes() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "defrostTypes");
    }

    public List<SelectItem> getRefrigeratorFeatures() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "refrigeratorFeatures");
    }

    public List<SelectItem> getRatedVoltages() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "ratedVoltages");
    }

    public List<SelectItem> getRatedFrequencies() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "ratedFrequencies");
    }

    public List<SelectItem> getEnergyEfficiencyClasses() {
        return SystemManager.getStringListAsSelectItems(getEntityManager1(),
                "energyEfficiencyClasses");
    }

    public void okLabel() {
        if (selectedEnergyLabel.getIsDirty()) {
            selectedEnergyLabel.save(getEntityManager1());
            selectedEnergyLabel.setIsDirty(false);
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveEnergyLabel() {

            ReturnMessage returnMessage = selectedEnergyLabel.save(getEntityManager1());
            
            if (returnMessage.isSuccess()) {

                selectedEnergyLabel.setIsDirty(false);
                selectedEnergyLabel.setEditStatus("        ");
                PrimeFacesUtils.addMessage("Saved!", "Energy label was saved",
                        FacesMessage.SEVERITY_INFO);

            } else {

                PrimeFacesUtils.addMessage("Energy Label NOT Saved!",
                        "Energy label was NOT saved. Please contact the System Administrator!",
                        FacesMessage.SEVERITY_ERROR);

            }

    }

    public void cancelLabelEdit() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updateEnergyLabel() {

        selectedEnergyLabel.setIsDirty(true);
        selectedEnergyLabel.setEditStatus("(edited)");

    }

    public EnergyLabel getSelectedEnergyLabel() {
        return selectedEnergyLabel;
    }

    public void setSelectedEnergyLabel(EnergyLabel selectedEnergyLabel) {
        this.selectedEnergyLabel = selectedEnergyLabel;
    }

    public void editSelectedEnergyLabel() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("labelDialog", options, null);

    }

    public void createNewEnergyLabel() {
        EntityManager em = getEntityManager1();
        selectedEnergyLabel = new EnergyLabel();

        String defaultProductType = SystemOption.getString(em, "defaultProductType");
        String defaultRatedVoltage = SystemOption.getString(em, "defaultRatedVoltage");
        String defaultRatedFrequency = SystemOption.getString(em, "defaultRatedFrequency");
        String costPerKWh_1 = SystemOption.getString(em, "costPerKWh_1");
        String costPerKWh_2 = SystemOption.getString(em, "costPerKWh_2");

        selectedEnergyLabel.setType(defaultProductType);
        selectedEnergyLabel.setDefrost("Automatic");
        selectedEnergyLabel.setRatedVoltage(defaultRatedVoltage);
        selectedEnergyLabel.setRatedFrequency(defaultRatedFrequency);
        selectedEnergyLabel.setCostPerKwh(costPerKWh_1);
        selectedEnergyLabel.setCostPerKwh2(costPerKWh_2);
        selectedEnergyLabel.setValidity("" + BusinessEntityUtils.getCurrentYear());
        selectedEnergyLabel.setYearOfEvaluation("" + BusinessEntityUtils.getCurrentYear());

        editSelectedEnergyLabel();
    }

    @Override
    public String getApplicationHeader() {

        return "LabelPrint";
    }

    public String getApplicationFooter() {

        return "LabelPrint, v" + SystemOption.getString(getEntityManager1(),
                "LPv");
    }

    /**
     * Gets the SystemManager object as a session bean.
     *
     * @return
     */
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "LabelPrintAppShortcutIconURL");
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public final void init() {
        reset();
    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Energy labels");
        setSearchText("");
        getSystemManager().setDefaultCommandTarget(":appForm:mainTabView:energyLabelSearchButton");
        setModuleNames(new String[]{
            "energyLabelManager"
        });
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateAndTimeEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();
        energyLabelSearchText = "";

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
            case "Energy labels":
                foundEnergyLabels = findLabels(searchText);
                openEnergyLabelBrowser();
                break;
            default:
                break;
        }

    }

    public List<EnergyLabel> findLabels(String searchPattern) {

        int maxLabelSearchResults = SystemOption.getInteger(
                getEntityManager1(), "maxLabelSearchResults");
        List<EnergyLabel> labelsFound;

        String query = "SELECT r FROM EnergyLabel r WHERE"
                + " r.model LIKE '%" + searchPattern + "%'"
                + " OR r.brand LIKE '%" + searchPattern + "%'"
                + " OR r.starRating LIKE '%" + searchPattern + "%'"
                + " OR r.ratedVoltage LIKE '%" + searchPattern + "%'"
                + " OR r.ratedCurrent LIKE '%" + searchPattern + "%'"
                + " OR r.ratedFrequency LIKE '%" + searchPattern + "%'"
                + " OR r.annualConsumption LIKE '%" + searchPattern + "%'"
                + " OR r.capacity LIKE '%" + searchPattern + "%'"
                + " OR r.freshFoodCompartmentVol LIKE '%" + searchPattern + "%'"
                + " OR r.freezerCompartmentVol LIKE '%" + searchPattern + "%'"
                + " OR r.coolingCapacity LIKE '%" + searchPattern + "%'"
                + " OR r.heatingCapacity LIKE '%" + searchPattern + "%'"
                + " OR r.costPerKwh LIKE '%" + searchPattern + "%'"
                + " OR r.costPerKwh2 LIKE '%" + searchPattern + "%'"
                + " OR r.CEC LIKE '%" + searchPattern + "%'"
                + " OR r.BEC LIKE '%" + searchPattern + "%'"
                + " OR r.ERF LIKE '%" + searchPattern + "%'"
                + " OR r.totalAdjustedVol LIKE '%" + searchPattern + "%'"
                + " OR r.Cf LIKE '%" + searchPattern + "%'"
                + " OR r.Cv LIKE '%" + searchPattern + "%'"
                + " OR r.AEER LIKE '%" + searchPattern + "%'"
                + " OR r.ACOP LIKE '%" + searchPattern + "%'"
                + " OR r.country LIKE '%" + searchPattern + "%'"
                + " OR r.defrost LIKE '%" + searchPattern + "%'"
                + " OR r.distributor LIKE '%" + searchPattern + "%'"
                + " OR r.jobNumber LIKE '%" + searchPattern + "%'"
                + " OR r.labelName LIKE '%" + searchPattern + "%'"
                + " OR r.manufacturer LIKE '%" + searchPattern + "%'"
                + " OR r.operatingCost LIKE '%" + searchPattern + "%'"
                + " OR r.standard LIKE '%" + searchPattern + "%'"
                + " OR r.type LIKE '%" + searchPattern + "%'"
                + " OR r.validity LIKE '%" + searchPattern + "%'"
                + " OR r.yearOfEvaluation LIKE '%" + searchPattern + "%'"
                + " OR r.feature1 LIKE '%" + searchPattern + "%'"
                + " OR r.feature2 LIKE '%" + searchPattern + "%'"
                + " OR r.letterRating LIKE '%" + searchPattern + "%'"
                + " OR r.batchCode LIKE '%" + searchPattern + "%'"
                + " OR r.serialNumber LIKE '%" + searchPattern + "%'"
                + " OR r.efficiencyRatio LIKE '%" + searchPattern + "%'";

        try {
            labelsFound = (List<EnergyLabel>) getEntityManager1()
                    .createQuery(query)
                    .setMaxResults(maxLabelSearchResults)
                    .getResultList();
        } catch (Exception e) {
            System.out.println(e);

            labelsFound = new ArrayList<>();
        }

        return labelsFound;
    }

    public List<EnergyLabel> getFoundEnergyLabels() {
        if (foundEnergyLabels == null) {
            foundEnergyLabels = findLabels("");
        }
        return foundEnergyLabels;
    }

    public void setFoundEnergyLabels(List<EnergyLabel> foundEnergyLabels) {
        this.foundEnergyLabels = foundEnergyLabels;
    }

    public void onLabelCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundEnergyLabels().get(event.getRowIndex()));
    }

    public int getNumLabelsFound() {
        return getFoundEnergyLabels().size();
    }

    public void doEnergyLabelSearch() {

        foundEnergyLabels = findLabels(getEnergyLabelSearchText());
    }


    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("ELMEM");
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Energy labels", "Energy labels"));

        return searchTypes;
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Energy Labels");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Energy labels":
//                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
//                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                dateSearchFields.add(new SelectItem("-- Not applicable --",
                        "-- Not applicable --"));
                break;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void handleKeepAlive() {

        super.updateUserActivity("LPv"
                + SystemOption.getString(getEntityManager1(), "LPv"),
                "Logged in");

        super.handleKeepAlive();

    }

    @Override
    public void completeLogout() {

        super.updateUserActivity("LPv"
                + SystemOption.getString(getEntityManager1(), "LPv"),
                "Logged out");

        super.completeLogout();

    }

    @Override
    public void completeLogin() {

        super.updateUserActivity("LPv"
                + SystemOption.getString(getEntityManager1(), "LPv"),
                "Logged in");

        super.completeLogin();

    }

    // SVG manipulation
    public void updateLabel() {

        EntityManager em = getEntityManager1();

        if (svgDocument != null) {
            try {

                if (getSelectedEnergyLabel().getType().trim().equals("Room Air-conditioner")) {
                    // Year of evaluation
                    setElementText("yearOfEvaluation", getSelectedEnergyLabel().getYearOfEvaluation(), "start");
                    // Letter rating                
                    eraseAllRatingLetters();
                    renderRating(getSelectedEnergyLabel().getLetterRating(), true);
                    // Annual consumption
                    setElementText("annualConsumption", getSelectedEnergyLabel().getAnnualConsumption(), "middle");
                    // Batch code/serial number
                    if (SystemOption.getBoolean(em, "useSerialNumber")) {
                        if (!getSelectedEnergyLabel().getSerialNumber().trim().isEmpty()) {
                            setElementText("batchCodeLabel", "Serial No.", "");
                            setElementText("batchCode", getSelectedEnergyLabel().getSerialNumber(), "");
                        } else {
                            setElementText("batchCodeLabel", "Batch Code", "");
                            setElementText("batchCode", getSelectedEnergyLabel().getBatchCode(), "");
                        }
                    } else {
                        setElementText("batchCodeLabel", "Batch Code", "");
                        setElementText("batchCode", getSelectedEnergyLabel().getBatchCode(), "");
                    }
                    // Efficiency ratio
                    setElementText("efficiencyRatio", getSelectedEnergyLabel().getEfficiencyRatio(), "middle");
                    // Carrier
                    setElementText("carrier", getSelectedEnergyLabel().getManufacturer(), "end");
                    // Code
                    setElementText("code", getSelectedEnergyLabel().getModel(), "end");
                    Element qrcode = svgDocument.getElementById("qrcode");
                    try {
                        qrcode.setAttributeNS(SVGConstants.XLINK_NAMESPACE_URI,
                                SVGConstants.XLINK_HREF_QNAME,
                                "data:image/png;base64,"
                                + QRCodeGenerator.getQRCodeImageData(
                                        getQRCodeData(), 125));
                    } catch (WriterException | IOException ex) {
                        System.out.println(ex);
                    }

                } else {
                    // Year of evaluation
                    setElementText("yearOfEvaluation", getSelectedEnergyLabel().getYearOfEvaluation(), "start");
                    // Manufacturer
                    setElementText("manufacturer", getSelectedEnergyLabel().getManufacturer(), "end");
                    // Model(s)
                    setElementText("models", "Model(s) " + getSelectedEnergyLabel().getModel(), "end");
                    // Capacity
                    setElementText("capacity",
                            "Capacity "
                            + getSelectedEnergyLabel().getCapacity()
                            + " Litres", "end");
                    // Electrical ratings
                    String electricalRatings
                            = getSelectedEnergyLabel().getRatedVoltage() + "V, "
                            + getSelectedEnergyLabel().getRatedFrequency() + "Hz";

                    if (!getSelectedEnergyLabel().getRatedCurrent().trim().isEmpty()) {
                        electricalRatings = electricalRatings + ", "
                                + getSelectedEnergyLabel().getRatedCurrent() + "A";
                    }
                    setElementText("electricalRatings", electricalRatings, "end");
                    // Type
                    setElementText("type", getSelectedEnergyLabel().getType(), "start");
                    // Defrost
                    setElementText("defrost", "- " + getSelectedEnergyLabel().getDefrost(), "start");
                    // Feature 1
                    if (!getSelectedEnergyLabel().getFeature1().trim().isEmpty()) {
                        setElementText("feature1", "- " + getSelectedEnergyLabel().getFeature1(), "start");
                    } else {
                        setElementText("feature1", "", "start");
                    }
                    // Feature 2
                    if (!getSelectedEnergyLabel().getFeature2().trim().isEmpty()) {
                        setElementText("feature2", "- " + getSelectedEnergyLabel().getFeature2(), "start");
                    } else {
                        setElementText("feature2", "", "start");
                    }
                    // Letter rating                
                    eraseAllRatingLetters();
                    renderRating(getSelectedEnergyLabel().getLetterRating(), true);
                    // Operating cost
                    setElementText("operatingCost", getSelectedEnergyLabel().getOperatingCost(), "start");
                    // Annual consumption
                    setElementText("annualConsumption", getSelectedEnergyLabel().getAnnualConsumption(), "start");
                    // Annual consumption unit placement
                    Element annualConsumption = svgDocument.getElementById("annualConsumption");
                    SVGLocatable locatable = (SVGLocatable) annualConsumption;
                    SVGRect rect = locatable.getBBox();
                    Element annualConsumptionUnitSpan = svgDocument.getElementById("annualConsumptionUnitSpan");
                    if (annualConsumptionUnitSpan != null && rect != null) {
                        annualConsumptionUnitSpan.setAttribute("x", "" + (rect.getX() + rect.getWidth()));
                    } else {
                        int length = getSelectedEnergyLabel().getAnnualConsumption().length();
                        int annualConsumptionUnitXMulConst
                                = SystemOption.getInteger(em, "annualConsumptionUnitXMulConst");
                        double annualConsumptionUnitXMul
                                = SystemOption.getDouble(em, "annualConsumptionUnitXMul");

                        annualConsumptionUnitXMulConst = length - annualConsumptionUnitXMulConst;

                        annualConsumptionUnitSpan = svgDocument.getElementById("annualConsumptionUnitSpan");
                        String annualConsumptionUnitSpanX
                                = annualConsumptionUnitSpan.getAttribute("x");
                        Double annualConsumptionUnitSpanXValue
                                = Double.valueOf(annualConsumptionUnitSpanX);
                        annualConsumptionUnitSpan.setAttribute("x",
                                Double.toString(annualConsumptionUnitSpanXValue
                                        + annualConsumptionUnitXMulConst
                                        * annualConsumptionUnitXMul));
                    }
                    // Batch code/serial number
                    if (SystemOption.getBoolean(em, "useSerialNumber")) {
                        if (!getSelectedEnergyLabel().getSerialNumber().trim().isEmpty()) {
                            setElementText("batchCodeLabel", "Serial No.", "");
                            setElementText("batchCode", getSelectedEnergyLabel().getSerialNumber(), "");
                        } else {
                            setElementText("batchCodeLabel", "Batch Code", "");
                            setElementText("batchCode", getSelectedEnergyLabel().getBatchCode(), "");
                        }
                    } else {
                        setElementText("batchCodeLabel", "Batch Code", "");
                        setElementText("batchCode", getSelectedEnergyLabel().getBatchCode(), "");
                    }
                    // QR Code
                    Element qrcode = svgDocument.getElementById("qrcode");
                    try {
                        qrcode.setAttributeNS(SVGConstants.XLINK_NAMESPACE_URI,
                                SVGConstants.XLINK_HREF_QNAME,
                                "data:image/png;base64,"
                                + QRCodeGenerator.getQRCodeImageData(
                                        getQRCodeData(),
                                        SystemOption.getInteger(em,
                                                "qRCodeImageSize")));
                    } catch (WriterException | IOException ex) {
                        System.out.println(ex);
                    }

                }

            } catch (DOMException e) {
                System.out.println("Error updating label...: " + e);
            }

        }

    }

    private String getQRCodeData() {

        String data = "Manufacturer: " + getSelectedEnergyLabel().getManufacturer() + "\n"
                + "Distributor: " + getSelectedEnergyLabel().getDistributor() + "\n"
                + "Country of origin: " + getSelectedEnergyLabel().getCountry() + "\n"
                + "Brand: " + getSelectedEnergyLabel().getBrand() + "\n"
                + "Model: " + getSelectedEnergyLabel().getModel() + "\n"
                + "Electricity rate 1: " + getSelectedEnergyLabel().getCostPerKwh() + "\n"
                + "Electricity rate 2: " + getSelectedEnergyLabel().getCostPerKwh2() + "\n"
                + "Year of evaluation: " + getSelectedEnergyLabel().getYearOfEvaluation() + "\n"
                + "Batch code: " + getSelectedEnergyLabel().getBatchCode() + "\n"
                + "";

        return data;

    }

    public void renderRating(String ratingLetter, Boolean render) {

        try {
            if (svgDocument != null) {

                Element rating = svgDocument.getElementById("rating" + ratingLetter);

                if (render) {
                    rating.setAttribute("visibility", "visible");
                } else {
                    rating.setAttribute("visibility", "hidden");
                }
            }
        } catch (DOMException e) {
            System.out.println(e);
        }

    }

    private void eraseAllRatingLetters() {

        renderRating("A", false);
        renderRating("B", false);
        renderRating("C", false);
        renderRating("D", false);
        renderRating("E", false);
        renderRating("F", false);

    }

    private void setElementText(String elementId, String content, String anchor) {

        if (svgDocument != null) {
            Element element = svgDocument.getElementById(elementId);
            if (element != null) {
                element.setAttribute("text-anchor", anchor);
                element.setTextContent(content);
            }
        }

    }

    public void setElementFill(String elementId, String fill) {

        if (svgDocument != null) {
            Element element = svgDocument.getElementById(elementId);
            element.setAttribute("style", "fill:" + fill);
        }
    }

    public void setElementStyle(String elementId, String style) {

        if (svgDocument != null) {
            Element element = svgDocument.getElementById(elementId);
            element.setAttribute("style", style);
        }
    }

    public StreamedContent getEnergyLabelImage() {

        try {

            ByteArrayInputStream stream = getLabelImageByteArrayInputStream();

            DefaultStreamedContent dsc = DefaultStreamedContent.builder()
                    .contentType("image/jpg")
                    .name(getSelectedEnergyLabel().getBrand()
                            + "-" + getSelectedEnergyLabel().getModel()
                            + ".jpg")
                    .stream(() -> stream)
                    .build();

            return dsc;

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public StreamedContent getEnergyLabelImagePreview() {

        try {

            ByteArrayInputStream stream = getLabelImageByteArrayInputStream();

            DefaultStreamedContent dsc = DefaultStreamedContent.builder()
                    .contentType("image/jpg")
                    .stream(() -> stream)
                    .build();

            return dsc;

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public ByteArrayInputStream getLabelImageByteArrayInputStream() {

        EntityManager em = getEntityManager1();

        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            File svgFile;

            if (getSelectedEnergyLabel().getType().trim().equals("Room Air-conditioner")) {

                svgFile = new File(SystemOption.getString(em, "aCLabelTemplate"));

            } else {

                svgFile = new File(SystemOption.getString(em, "refrigeratorLabelTemplate"));
            }
            svgDocument = f.createDocument(svgFile.toURI().toString());

            updateLabel();

            TranscoderInput input = new TranscoderInput(svgDocument);
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(ostream);
            JPEGTranscoder t = new JPEGTranscoder();

            t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
                    new Float(SystemOption.getDouble(em, "jPEGTranscoderKeyQuality")).floatValue());

            if (getSelectedEnergyLabel().getType().trim().equals("Room Air-conditioner")) {
                t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,
                        Float.valueOf(SystemOption.getString(em, "aCImageWidth")));
                t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,
                        Float.valueOf(SystemOption.getString(em, "aCImageHeight")));
                t.transcode(input, output);
            } else {
                t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,
                        Float.valueOf(SystemOption.getString(em, "fridgeImageWidth")));
                t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,
                        Float.valueOf(SystemOption.getString(em, "fridgeImageHeight")));
                t.transcode(input, output);
            }

            return new ByteArrayInputStream(ostream.toByteArray());

        } catch (IOException | TranscoderException ex) {
            System.out.println(ex);
        }

        return null;

    }

}
