/*
LabelPrint 
Copyright (C) 2023  D P Bennett & Associates Limited

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.mt.EnergyLabel;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Modules;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.rm.manager.ReportManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

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
            if (getUser().hasModule("energyLabelManager")) {
                getMainTabView().openTab(module.getDashboardTitle());
            }
        }

    }

    public void openReportsTab() {
        getMainTabView().openTab("Reports");
    }

    public void openEnergyLabelBrowser() {
        getMainTabView().openTab("Label Browser");
    }

    public void openClientsTab() {

        getMainTabView().openTab("Clients");
    }

    public void energyLabelDialogReturn() {
//        if (currentJob.getIsDirty()) {
//            PrimeFacesUtils.addMessage("Job was NOT saved", "The recently edited job was not saved", FacesMessage.SEVERITY_WARN);
//            PrimeFaces.current().ajax().update("appForm:growl3");
//            currentJob.setIsDirty(false);
//        }

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
        return "Energy Label Generation and Printing";
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

    public void cancelLabelEdit() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updateEnergyLabel() {
        selectedEnergyLabel.setIsDirty(true);
    }

    public EnergyLabel getSelectedEnergyLabel() {
        return selectedEnergyLabel;
    }

    public void setSelectedEnergyLabel(EnergyLabel selectedEnergyLabel) {
        this.selectedEnergyLabel = selectedEnergyLabel;
    }

    public void editSelectedEnergyLabel() {

        //PrimeFacesUtils.openDialog(null, "labelDialog", true, true, true, 600, 700);
        PrimeFacesUtils.openDialog(null, "labelDialog", true, true, true, true, 400, 800);
    }

    public void createNewEnergyLabel() {
        selectedEnergyLabel = new EnergyLabel();

        editSelectedEnergyLabel();
    }

    @Override
    public String getApplicationHeader() {

        return "LabelPrint";
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

    public ReportManager getReportManager() {
        return BeanUtils.findBean("reportManager");
    }

    public ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");
    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Energy labels");
        setSearchText("");
        setDefaultCommandTarget("doSearch");
        setModuleNames(new String[]{
            "energyLabelManager",
            "clientManager",
            "systemManager",
            "reportManager"
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
                // tk be replaced with search on all fields
                foundEnergyLabels = findLabels(searchText);
                openEnergyLabelBrowser();
                break;
            default:
                break;
        }

    }

    // tk Implement find* method in the EnergyLabel class that searches more fields.
    public List<EnergyLabel> findLabels(String searchPattern) {

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
                + " OR r.efficiencyRatio LIKE '%" + searchPattern + "%'";

        try {
            labelsFound = (List<EnergyLabel>) getEntityManager1().createQuery(query).getResultList();
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

        return getSystemManager().getEntityManager1();

    }

    @Override
    public EntityManager getEntityManager2() {

        return getSystemManager().getEntityManager2();

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

    public void exportLabelToRasterGraphic(Document doc) {

        try {

//            String svgURI
//                    = new File("C:\\LabelPrint\\images\\CROSQACEnergyLabel.svg").toURL().toString();
            TranscoderInput input = new TranscoderInput(/*svgURI*/doc);
            OutputStream ostream;
            TranscoderOutput output;

            //switch (formatName) {
            //case "jpg":
            ostream = new FileOutputStream("C:\\LabelPrint\\images\\yesjpg" + ".jpg");
            output = new TranscoderOutput(ostream);
            JPEGTranscoder t = new JPEGTranscoder();

            t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));

            t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,
                    new Float("2000"));
            t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,
                    new Float("2712"));

            t.transcode(input, output);

            ostream.flush();
            ostream.close();

        } catch (IOException | TranscoderException e) {
            System.out.println(e);
        } finally {

        }
    }

    public void getSVDoc() {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            //String uri = "http://www.example.org/diagram.svg";
            //Document doc = f.createDocument(uri);
            File svgFile = new File("C:\\LabelPrint\\images\\CROSQACEnergyLabel.svg");
            Document doc = f.createDocument(svgFile.toURI().toString());

            setElementText(doc, "yearOfEvaluation", "2023", "start");

            System.out.println("Got it!!");

            exportLabelToRasterGraphic(doc);

            System.out.println("Exported it!");

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    private void setElementText(Document svgDocument,
            String elementId, String content, String anchor) {
        if (svgDocument != null) {
            Element element = svgDocument.getElementById(elementId);
            element.setAttribute("text-anchor", anchor);
            element.setTextContent(content);
        }
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

}
