/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jm.com.dpbennett.sm.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javax.faces.model.SelectItem;
import jm.com.dpbennett.business.entity.hrm.User;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.ReturnMessage;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class Utils {

    private static ArrayList countries;

    /**
     * Gets 10 years starting with the current year. To be verified!
     *
     * @return
     */
    public List getYears() {
        List years = new ArrayList();

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (Integer i = currentYear; i > (currentYear - 10); i--) {
            years.add(new SelectItem(i, i.toString()));
        }

        return years;
    }

    public static List getPersonalTitles() {
        ArrayList titles = new ArrayList();

        titles.add(new SelectItem("--", "--"));
        titles.add(new SelectItem("Mr.", "Mr."));
        titles.add(new SelectItem("Ms.", "Ms."));
        titles.add(new SelectItem("Mrs.", "Mrs."));
        titles.add(new SelectItem("Miss", "Miss"));
        titles.add(new SelectItem("Dr.", "Dr."));

        return titles;
    }

    public static List getSexes() {
        ArrayList titles = new ArrayList();

        titles.add(new SelectItem("--", "--"));
        titles.add(new SelectItem("Male", "Male"));
        titles.add(new SelectItem("Female", "Female"));

        return titles;
    }

    public static List getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("General", "General"));

        return searchTypes;
    }

    public static void sendErrorEmail(final String subject,
            final String message,
            final EntityManager em) {
        try {
            if ((Boolean) SystemOption.getOptionValueObject(em,
                    "developerEmailAlertActivated")) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Utils.postMail(null, null, null, subject, message,
                                    "text/plain", em);
                        } catch (Exception e) {
                            System.out.println("Error sending error mail: " + e);
                        }
                    }

                }.start();
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static ReturnMessage postMail(
            Session mailSession,
            Employee fromEmployee,
            Employee toEmployee,
            String subject,
            String message,
            String contentType,
            EntityManager em) {

        boolean debug = false;
        Message msg;

        try {
            // use default session if none was provided
            if (mailSession == null) {
                //Set the host smtp address
                Properties props = new Properties();
                props.put("mail.smtp.host", (String) SystemOption.getOptionValueObject(em, "mail.smtp.host"));

                // create some properties and get the default Session
                Session session = Session.getDefaultInstance(props, null);
                session.setDebug(debug);
                msg = new MimeMessage(session);
            } else {
                msg = new MimeMessage(mailSession);
            }

            // set the from and to address
            InternetAddress addressFrom = null;
            if (fromEmployee == null) {
                addressFrom = new InternetAddress(
                        (String) SystemOption.getOptionValueObject(em, "jobManagerEmailAddress"),
                        (String) SystemOption.getOptionValueObject(em, "jobManagerEmailName"));
            } else {
                addressFrom = new InternetAddress(
                        fromEmployee.getInternet().getEmail1(),
                        fromEmployee.getFirstName() + " " + fromEmployee.getLastName());
            }
            msg.setFrom(addressFrom);

            InternetAddress[] addressTo = new InternetAddress[1];
            if (toEmployee != null) {               
                    addressTo[0] = new InternetAddress(toEmployee.getInternet().getEmail1());
            } else {
                addressTo[0] = new InternetAddress(
                        (String) SystemOption.getOptionValueObject(em, "administratorEmailAddress"));
            }

            msg.setRecipients(Message.RecipientType.TO, addressTo);

            // Setting the Subject and Content Type
            msg.setSubject(subject);
            msg.setContent(message, contentType);
            Transport.send(msg);
            
            return new ReturnMessage();
            
        } catch (UnsupportedEncodingException | MessagingException e) {
            System.out.println("An error occurred while posting an email: " + e);
            return new ReturnMessage(false, "An error occurred while posting an email.");
        }

    }

}
