/*
Learning Management System (LMS) 
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
package jm.com.dpbennett.lms.manager;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.faces.model.SelectItem;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import org.primefaces.event.SelectEvent;
import jm.com.dpbennett.business.entity.gm.BusinessEntityManagement;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.MailUtils;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.fm.manager.FinanceManager;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.rm.manager.ReportManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.Manager;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.DateUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import org.primefaces.event.TabChangeEvent;
import jm.com.dpbennett.business.entity.sm.Module;

/**
 *
 * @author Desmond Bennett
 */
public class LearningManager extends GeneralManager
        implements Serializable, BusinessEntityManagement {

    @PersistenceUnit(unitName = "HRMPU")
    private EntityManagerFactory HRMPU;
    private String searchText;

    public LearningManager() {
        init();
    }

    @Override
    public List<String> getModuleNames() {

        if (moduleNames == null) {
            moduleNames = new ArrayList<>();

            moduleNames.add("learningManager");
            moduleNames.add("systemManager");
            moduleNames.add("clientManager");
            //moduleNames.add("financeManager");
            moduleNames.add("humanResourceManager");
            moduleNames.add("reportManager");

        }

        return moduleNames;
    }

    @Override
    public void initDashboard() {

        EntityManager em = getSystemManager().getEntityManager1();

        getSystemManager().getDashboard().reset(getUser(), true);

        for (String moduleName : getModuleNames()) {
            Manager manager = getManager(moduleName);
            if (manager != null) {
                Module mod = Module.findActiveByName(em, moduleName);
                if (mod != null) {
                    manager.openDashboardTab(mod.getDashboardTitle());
                }
            }
        }

    }

    @Override
    public void initMainTabView() {

        EntityManager em = getSystemManager().getEntityManager1();

        getSystemManager().getMainTabView().reset(getUser());

        for (String moduleName : getModuleNames()) {
            Manager manager = getManager(moduleName);
            if (manager != null) {
                Module mod = Module.findActiveByName(em, moduleName);
                if (mod != null) {
                    manager.openMainViewTab(mod.getMainViewTitle());
                }
            }

        }
    }

    @Override
    public void reInitUI() {
        getSystemManager().reInitUI();
        getFinanceManager().reInitUI();
        getHumanResourceManager().reInitUI();
    }

    @Override
    public void openDashboardTab(String title) {

        //getSystemManager().setDefaultCommandTarget(":dashboardForm:dashboardAccordion:jobSearchButton");
        getSystemManager().getDashboard().openTab(title);
    }

    @Override
    public void openMainViewTab(String title) {

        //getSystemManager().setDefaultCommandTarget(":dashboardForm:dashboardAccordion:jobSearchButton");
        getSystemManager().getMainTabView().openTab(title);
    }

    public EntityManagerFactory getHRMPU() {

        return HRMPU;
    }

    public Employee getUserEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();

        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {
//            case "Job Management":
//            case "Jobs":
//            case "Proforma Invoices":
//                getSystemManager().setDefaultCommandTarget(":dashboardForm:dashboardAccordion:jobSearchButton");
//
//                return true;

            default:
                return false;
        }
    }

    @Override
    public String getSearchText() {

        if (searchText == null) {
            searchText = "";
        }

        return searchText;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public String getLogoURL() {
        return SystemOption.getString(
                getSystemManager().getEntityManager1(), "AALogo");
    }

    @Override
    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");

    }

    @Override
    public String getAppShortcutIconURL() {

        return SystemOption.getString(
                getSystemManager().getEntityManager1(), "JMTSLogo");

    }

    public Boolean booleanSetting(String setting) {

        return SystemOption.getBoolean(getSystemManager().getEntityManager1(),
                setting);

    }

    @Override
    public String getApplicationHeader() {
        return SystemOption.getString(getSystemManager().getEntityManager1(),
                "AAName");
    }

    public final void init() {
        reset();
    }

    public FinanceManager getFinanceManager() {

        return BeanUtils.findBean("financeManager");
    }

    public ReportManager getReportManager() {

        return BeanUtils.findBean("reportManager");
    }

    public ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");
    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    @Override
    public void reset() {
        super.reset();

        setName("learningManager");
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateAndTimeEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

    }

    public void openSystemBrowser() {

        getSystemManager().getMainTabView().openTab("System Administration");
    }

    public void openFinancialAdministration() {

        getSystemManager().getMainTabView().openTab("Financial Administration");
    }

    public void openHumanResourceBrowser() {

        getSystemManager().getMainTabView().openTab("Human Resource");
    }

    @Override
    public EntityManager getEntityManager1() {

        return getHRMPU().createEntityManager();
    }

    public void openSystemAdministrationTab() {

        getSystemManager().getMainTabView().openTab("System Administration");

    }

    public void openFinancialAdministrationTab() {

        getSystemManager().getMainTabView().openTab("Financial Administration");

    }

    public List<String> completeSearchText(String query) {
        List<String> suggestions = new ArrayList<>();

        return suggestions;
    }

    @Override
    public EntityManager getEntityManager2() {

        return getFinanceManager().getFINPU().createEntityManager();
    }

    public void update() {
        setIsDirty(true);
    }

    public void sendErrorEmail(String subject, String message) {
        try {
            EntityManager em = getSystemManager().getEntityManager1();

            MailUtils.postMail(null,
                    SystemOption.getString(em, "jobManagerEmailAddress"),
                    SystemOption.getString(em, "jobManagerEmailName"),
                    SystemOption.getString(em, "softwareDeveloperEmailAddress"),
                    subject, message,
                    "text/plain", em);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void doDefaultSearch() {

        doDefaultSearch(
                getSystemManager().getMainTabView(),
                getDateSearchPeriod().getDateField(),
                getSearchType(),
                getSearchText(),
                getDateSearchPeriod().getStartDate(),
                getDateSearchPeriod().getEndDate());

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
//            case "General":
//            case "My jobs":
//            case "My department's jobs":
//            case "Parent jobs only":
//            case "Unapproved job costings":
//            case "Appr'd & uninv'd jobs":
//            case "Incomplete jobs":
//            case "Invoiced jobs":
//                doJobSearch(getJobSearchResultList(), false);
//                openJobBrowser();
//                break;
//            case "My dept's proforma invoices":
//                doJobSearch(getJobFinanceManager().getJobSearchResultList(), true);
//                getJobFinanceManager().openProformaInvoicesTab();
//                break;
            default:
                break;
        }

    }

    public void postJobManagerMailToUser(
            Session mailSession,
            User user,
            String subject,
            String message) throws Exception {

        boolean debug = false;
        Message msg;
        EntityManager em = getSystemManager().getEntityManager1();

        if (mailSession == null) {

            Properties props = new Properties();
            String mailServer = (String) SystemOption.getOptionValueObject(em, "mail.smtp.host");
            props.put("mail.smtp.host", mailServer);

            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(debug);
            msg = new MimeMessage(session);
        } else {
            msg = new MimeMessage(mailSession);
        }

        String email = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailAddress");
        String name = (String) SystemOption.getOptionValueObject(em, "jobManagerEmailName");
        InternetAddress addressFrom = new InternetAddress(email, name);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[1];
        if (user != null) {
            addressTo[0] = new InternetAddress(user.getUsername(), user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
        } else {
            String email1 = (String) SystemOption.getOptionValueObject(em, "administratorEmailAddress");
            String name1 = (String) SystemOption.getOptionValueObject(em, "administratorEmailName");
            addressTo[0] = new InternetAddress(email1, name1);
        }

        msg.setRecipients(Message.RecipientType.TO, addressTo);

        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }

    public void postJobManagerMail(
            Session mailSession,
            String addressedTo,
            String fullNameOfAddressedTo,
            String subject,
            String message) throws Exception {

        boolean debug = false;
        Message msg;
        EntityManager em = getSystemManager().getEntityManager1();

        try {
            if (mailSession == null) {

                Properties props = new Properties();
                String mailServer = (String) SystemOption.getOptionValueObject(
                        em, "mail.smtp.host");
                String trust = (String) SystemOption.getOptionValueObject(
                        em, "mail.smtp.ssl.trust");
                props.put("mail.smtp.host", mailServer);
                props.setProperty("mail.smtp.ssl.trust", trust);

                Session session = Session.getDefaultInstance(props, null);
                session.setDebug(debug);
                msg = new MimeMessage(session);
            } else {
                msg = new MimeMessage(mailSession);
            }

            String email = (String) SystemOption.getOptionValueObject(
                    em, "jobManagerEmailAddress");
            String name = (String) SystemOption.getOptionValueObject(
                    em, "jobManagerEmailName");
            InternetAddress addressFrom = new InternetAddress(email, name);
            msg.setFrom(addressFrom);

            InternetAddress[] addressTo = new InternetAddress[1];

            addressTo[0] = new InternetAddress(addressedTo, fullNameOfAddressedTo);

            msg.setRecipients(Message.RecipientType.TO, addressTo);

            msg.setSubject(subject);
            msg.setContent(message, "text/html; charset=utf-8");

            Transport.send(msg);
        } catch (UnsupportedEncodingException | MessagingException e) {
            System.out.println(e);
        }
    }

    public void openReportsTab() {

        getSystemManager().getMainTabView().openTab("Reports");
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {

        // tk
        return null;
        //return getAuthorizedSearchTypes();
    }

    @Override
    public String getApplicationSubheader() {

        return SystemOption.getString(getSystemManager().getEntityManager1(), "AATagLine");
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

    public ArrayList<SelectItem> getDateSearchFields() {
        return getDateSearchFields(getSearchType());
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields;

        switch (searchType) {
//            case "General":
//            case "My jobs":
//            case "My department's jobs":
//            case "Parent jobs only":
//            case "Unapproved job costings":
//            case "Appr'd & uninv'd jobs":
//            case "Incomplete jobs":
//            case "Invoiced jobs":
//            case "My dept's proforma invoices":
//                dateSearchFields = DateUtils.getJobDateSearchFields();
//                break;
            default:
                dateSearchFields = DateUtils.getJobDateSearchFields();
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void handleSelectedNotification(Notification notification) {
        switch (notification.getType()) {
//            case "JobSearch":
//
//                try {
//
//                } catch (NumberFormatException e) {
//                    System.out.println(e);
//                }
//
//                break;

            default:
                System.out.println("Unknown type");
        }
    }

    @Override
    public void setIsDirty(Boolean isDirty) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Boolean getIsDirty() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
