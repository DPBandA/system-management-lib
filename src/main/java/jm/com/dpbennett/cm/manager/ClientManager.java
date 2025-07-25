/*
Client Management 
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
package jm.com.dpbennett.cm.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.fm.AccPacCustomer;
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.fm.Discount;
import jm.com.dpbennett.business.entity.hrm.Internet;
import jm.com.dpbennett.business.entity.fm.Tax;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.cm.model.LazyClientDataModel;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public class ClientManager extends GeneralManager implements Serializable {

    private Boolean isActiveClientsOnly;
    private Client selectedClient;
    private Contact selectedContact;
    private Address selectedAddress;
    private List<Client> foundClients;
    private Boolean edit;
    private String clientDialogTitle;
    private String clientSearchText;
    private LazyClientDataModel lazyClientDataModel;

    /**
     * Creates a new instance of ClientManager
     */
    public ClientManager() {
        init();
    }

    public String getApplicationFooter() {

        return getApplicationHeader() + ", v"
                + SystemOption.getString(getSystemManager().getEntityManager1(),
                        "JMTSv");
    }

    public String getSupportURL() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "supportURL");
    }

    public String getCopyrightOrganization() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "copyrightOrganization");

    }

    public String getOrganizationWebsite() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "organizationWebsite");
    }

    public String getLastSystemNotificationContent() {

        return Notification.findLastActiveSystemNotificationMessage(
                getSystemManager().getEntityManager1());

    }

    @Override
    public int getSizeOfActiveNotifications() {

        return getSystemManager().getActiveNotifications().size();
    }

    @Override
    public boolean getHasActiveNotifications() {
        return getSystemManager().getHasActiveNotifications();
    }

    @Override
    public List<Notification> getNotifications() {

        return getSystemManager().getNotifications();
    }

    @Override
    public void viewUserProfile() {
    }

    @Override
    public void onDashboardTabChange(TabChangeEvent event) {

        onMainViewTabChange(event);
    }

    @Override
    public String getDefaultCommandTarget() {

        return getSystemManager().getDefaultCommandTarget();

    }

    @Override
    public void onMainViewTabChange(TabChangeEvent event) {

        getSystemManager().onMainViewTabChange(event);
    }

    private Employee getEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();

        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    @Override
    public void initDashboard() {

        getDashboard().reset(getUser(), true);

        if (getUser().hasModule("clientManager")) {
            getDashboard().openTab("Client Management");
        }

        if (getUser().hasModule("systemManager")) {
            getDashboard().openTab("System Administration");
        }

    }

    @Override
    public void initMainTabView() {

        getMainTabView().reset(getUser());

        if (getUser().hasModule("clientManager")) {

            openClientsTab();
            
        }
    }

    public LazyClientDataModel getLazyClientDataModel() {

        return lazyClientDataModel;
    }

    public String getClientSearchText() {
        return clientSearchText;
    }

    public void setClientSearchText(String clientSearchText) {
        this.clientSearchText = clientSearchText;
    }

    @Override
    public String getApplicationHeader() {

        return "Client Manager";
    }

    public String getClientDialogTitle() {

        return clientDialogTitle;
    }

    public void setClientDialogTitle(String clientDialogTitle) {
        this.clientDialogTitle = clientDialogTitle;
    }

    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");

    }

    public FinanceManager getFinanceManager() {

        return BeanUtils.findBean("financeManager");

    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    public List<Client> completeActiveClient(String query) {
        int maxResult = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        try {

            return Client.findActive(getEntityManager1(), query, maxResult);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<AccPacCustomer> completeAccPacClient(String query) {
        EntityManager em2;

        try {
            em2 = getEntityManager2();

            return AccPacCustomer.findAllByNameAndId(em2, query);
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public void openClientsTab() {

        getMainTabView().openTab("Clients");

        getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:clientSearchButton");
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
            case "Clients":
                getSystemManager().setDefaultCommandTarget(":mainTabViewForm:mainTabView:clientSearchButton");
                return true;
            default:
                return false;
        }
    }

    public final void init() {

        reset();

    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Clients");
        setSearchText("");
        setDefaultCommandTarget("@this");
        setModuleNames(new String[]{
            "systemManager",
            "clientManager",
            "financeManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "dateEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        foundClients = new ArrayList<>();
        selectedClient = null;
        selectedContact = null;
        selectedAddress = null;
        clientSearchText = "";
        lazyClientDataModel = new LazyClientDataModel();
    }

    public Client getSelectedClient() {
        if (selectedClient == null) {
            return new Client("");
        }
        return selectedClient;
    }

    public void setSelectedClient(Client selectedClient) {
        this.selectedClient = selectedClient;
    }

    public Contact getSelectedContact() {
        return selectedContact;
    }

    public void setSelectedContact(Contact selectedContact) {
        this.selectedContact = selectedContact;

        setEdit(true);
    }

    public Address getSelectedAddress() {
        return selectedAddress;
    }

    public void setSelectedAddress(Address selectedAddress) {
        this.selectedAddress = selectedAddress;

        setEdit(true);
    }

    public void onClientCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundClients().get(event.getRowIndex()));
    }

    public Boolean getIsNewContact() {
        return getSelectedContact().getId() == null && !getEdit();
    }

    public Boolean getIsNewAddress() {
        return getSelectedAddress().getId() == null && !getEdit();
    }

    public Boolean getIsActiveClientsOnly() {
        if (isActiveClientsOnly == null) {
            isActiveClientsOnly = true;
        }
        return isActiveClientsOnly;
    }

    public void setIsActiveClientsOnly(Boolean isActiveClientsOnly) {
        this.isActiveClientsOnly = isActiveClientsOnly;
    }

    public int getNumClientFound() {
        return getFoundClients().size();
    }

    public void doClientSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Clients",
                getClientSearchText(),
                null,
                null);

    }

    public List<Client> getFoundClients() {
        return foundClients;
    }

    public void setFoundClients(List<Client> foundClients) {
        this.foundClients = foundClients;
    }

    public Boolean getIsNewClient() {
        return getSelectedClient().getId() == null;
    }

    public List<Address> completeClientAddress(String query) {
        List<Address> addresses = new ArrayList<>();

        try {

            for (Address address : getSelectedClient().getAddresses()) {
                if (address.toString().toUpperCase().contains(query.toUpperCase())) {
                    addresses.add(address);
                }
            }

            return addresses;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Contact> completeClientContact(String query) {
        List<Contact> contacts = new ArrayList<>();

        try {

            for (Contact contact : getSelectedClient().getContacts()) {
                if (contact.toString().toUpperCase().contains(query.toUpperCase())) {
                    contacts.add(contact);
                }
            }

            return contacts;
        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Address> getAddressesModel() {
        return getSelectedClient().getAddresses();
    }

    public List<Contact> getContactsModel() {
        return getSelectedClient().getContacts();
    }

    public Address getCurrentAddress() {
        return getSelectedClient().getDefaultAddress();
    }

    public void editClient() {
    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 700;
    }

    public void editSelectedClient() {

        setClientDialogTitle("Client");

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

        PrimeFaces.current().dialog().openDynamic("/client/clientDialog", options, null);

    }

    public void updateClient() {
        setIsDirty(true);
    }

    public void updateFinancialAccount() {

        setIsDirty(true);
    }

    public void updateFinancialAccountId() {

        selectedClient.setAccountingId(selectedClient.getFinancialAccount().getIdCust());
        // Set credit limit 
        selectedClient.setCreditLimit((selectedClient.
                getFinancialAccount().
                getCreditLimit().doubleValue()));

        setIsDirty(true);
    }

    public void updateClientName(AjaxBehaviorEvent event) {
        selectedClient.setName(selectedClient.getName().trim());

        setIsDirty(true);
    }

    public void updateContact() {

        getSelectedContact().setIsDirty(true);

        setIsDirty(true);
    }

    public void updateAddress() {

        getSelectedAddress().setIsDirty(true);

        setIsDirty(true);
    }

    public void createNewClient() {
        createNewClient(true);

        getMainTabView().openTab("Clients");

        editSelectedClient();
    }

    public void createNewClient(Boolean active) {
        selectedClient = new Client("", active);
        selectedClient.setDiscount(Discount.findDefault(getFinanceManager().getEntityManager1(), "0.0"));
        selectedClient.setDefaultTax(Tax.findDefault(getFinanceManager().getEntityManager1(), "0.0"));
    }

    public Boolean getIsDirty() {
        return getSelectedClient().getIsDirty();
    }

    // tk could be replaced with the same method in the Client class.
    public void setIsDirty(Boolean isDirty) {
        getSelectedClient().setIsDirty(isDirty);
    }

    public Client getClientById(EntityManager em, Long Id) {

        try {
            return em.find(Client.class, Id);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void cancelClientEdit(ActionEvent actionEvent) {

        setIsDirty(false);

        // Remove unsaved addresses
        Iterator addressIterator = getSelectedClient().getAddresses().iterator();
        Address address;
        while (addressIterator.hasNext()) {
            address = (Address) addressIterator.next();
            if (address.getId() == null) {
                addressIterator.remove();
            }
        }
        // Remove unsaved contacts
        Iterator contactIterator = getSelectedClient().getContacts().iterator();
        Contact contact;
        while (contactIterator.hasNext()) {
            contact = (Contact) contactIterator.next();
            if (contact.getId() == null) {
                contactIterator.remove();
            }
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("CMEM");

    }

    @Override
    public EntityManager getEntityManager2() {

        return getSystemManager().getEntityManager2();

    }

    public void okClient() {
        Boolean hasValidAddress = false;
        Boolean hasValidContact = false;

        try {

            // Validate 
            // Check for a valid address
            for (Address address : selectedClient.getAddresses()) {
                hasValidAddress = hasValidAddress || Address.validate(address);
            }
            if (!hasValidAddress) {
                PrimeFacesUtils.addMessage("Address Required",
                        "A valid address was not entered for this client",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Check for a valid contact
            for (Contact contact : selectedClient.getContacts()) {
                hasValidContact = hasValidContact || Contact.validate(contact);
            }
            if (!hasValidContact) {
                PrimeFacesUtils.addMessage("Contact Required",
                        "A valid contact was not entered for this client",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Update tracking
            if (getIsNewClient()) {
                getSelectedClient().setDateFirstReceived(new Date());
                getSelectedClient().setDateEntered(new Date());
                getSelectedClient().setDateEdited(new Date());
                if (getUser() != null) {
                    selectedClient.setEnteredBy(getEmployee());
                    selectedClient.setEditedBy(getEmployee());
                }
            }

            // Do save
            if (getIsDirty()) {
                getSelectedClient().setDateEdited(new Date());
                if (getUser() != null) {
                    selectedClient.setEditedBy(getEmployee());
                }
                selectedClient.save(getEntityManager1());
                setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Boolean getIsClientValid() {
        return BusinessEntityUtils.validateText(getSelectedClient().getName());
    }

    public void removeContact() {
        getSelectedClient().getContacts().remove(selectedContact);
        setIsDirty(true);
        selectedContact = null;
    }

    public void removeAddress() {
        getSelectedClient().getAddresses().remove(selectedAddress);
        setIsDirty(true);
        selectedAddress = null;
    }

    public Contact getCurrentContact() {
        return getSelectedClient().getDefaultContact();
    }

    public void okContact() {

        selectedContact = selectedContact.prepare();

        if (getIsNewContact()) {
            getSelectedClient().getContacts().add(selectedContact);
        }

        PrimeFaces.current().executeScript("PF('contactFormDialog').hide();");

    }

    public void okAddress() {

        selectedAddress = selectedAddress.prepare();

        if (getIsNewAddress()) {
            getSelectedClient().getAddresses().add(selectedAddress);
        }

        PrimeFaces.current().executeScript("PF('addressFormDialog').hide();");

    }

    public void createNewContact() {
        selectedContact = null;

        for (Contact contact : getSelectedClient().getContacts()) {
            if (contact.getFirstName().trim().isEmpty()) {
                selectedContact = contact;
                break;
            }
        }

        if (selectedContact == null) {
            selectedContact = new Contact("", "", "Main");
            selectedContact.setInternet(new Internet());
        }

        setEdit(false);

        setIsDirty(false);
    }

    public void editCurrentContact() {
        selectedContact = getCurrentContact();
        setEdit(true);
    }

    public void createNewAddress() {
        selectedAddress = null;

        // Find an existing invalid or blank address and use it as the neww address
        for (Address address : getSelectedClient().getAddresses()) {
            if (address.getAddressLine1().trim().isEmpty()) {
                selectedAddress = address;
                break;
            }
        }

        // No existing blank or invalid address found so creating new one.
        if (selectedAddress == null) {
            selectedAddress = new Address("", "Billing");
        }

        setEdit(false);

        setIsDirty(false);
    }

    public void editCurrentAddress() {
        selectedAddress = getCurrentAddress();
        setEdit(true);
    }

    public List<Client> completeClient(String query) {
        EntityManager em = getEntityManager1();
        int maxResult = SystemOption.getInteger(
                getSystemManager().getEntityManager1(),
                "maxSearchResults");

        try {

            List<Client> clients = Client.findActive(em, query, maxResult);

            return clients;

        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
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
            case "Clients":
                int maxResult = SystemOption.getInteger(
                        getSystemManager().getEntityManager1(),
                        "maxSearchResults");

                if (getIsActiveClientsOnly()) {
                    foundClients = Client.findActive(getEntityManager1(),
                            searchText, maxResult);
                } else {
                    foundClients = Client.find(getEntityManager1(), searchText, maxResult);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Clients");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Clients", "Clients"));

        return searchTypes;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Clients":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public String getApplicationSubheader() {
        return "Client Administration & Management";
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(),
                "appShortcutIconURL");
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURL");
    }

    @Override
    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURLImageHeight");
    }

    @Override
    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "logoURLImageWidth");
    }

    @Override
    public void onNotificationSelect(SelectEvent event) {
        EntityManager em = getSystemManager().getEntityManager1();

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
    public void handleSelectedNotification(Notification notification) {
        switch (notification.getType()) {
            case "ClientSearch":

                break;

            default:
                System.out.println("Unkown type");
        }
    }

    @Override
    public MainTabView getMainTabView() {
        return getSystemManager().getMainTabView();
    }

    @Override
    public void handleKeepAlive() {

        super.updateUserActivity("CMv"
                + SystemOption.getString(
                        getSystemManager().getEntityManager1(), "CMv"),
                "Logged in");

        if (getUser().getId() != null) {
            getUser().save(getSystemManager().getEntityManager1());
        }

        if ((Boolean) SystemOption.getOptionValueObject(
                getSystemManager().getEntityManager1(), "debugMode")) {
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
        }

        PrimeFaces.current().ajax().update(":headerForm:notificationBadge");

    }

    @Override
    public void login() {
        login(getSystemManager().getEntityManager1());
    }

    @Override
    public void logout() {
        completeLogout();
    }

    @Override
    public void completeLogout() {

        super.updateUserActivity("CMv"
                + SystemOption.getString(
                        getSystemManager().getEntityManager1(), "CMv"),
                "Logged out");

        if (getUser().getId() != null) {
            getUser().save(getSystemManager().getEntityManager1());
        }

        getDashboard().removeAllTabs();
        getMainTabView().removeAllTabs();

        reset();

    }

    @Override
    public void completeLogin() {

        if (getUser().getId() != null) {
            super.updateUserActivity("CMv"
                    + SystemOption.getString(
                            getSystemManager().getEntityManager1(), "CMv"),
                    "Logged in");
            getUser().save(getSystemManager().getEntityManager1());
        }

        setManagerUser();

        PrimeFaces.current().executeScript("PF('loginDialog').hide();");

        initMainTabView();

        initDashboard();

    }

    @Override
    public void setManagerUser() {

        getManager("systemManager").setUser(getUser());
        getManager("financeManager").setUser(getUser());

    }

}
