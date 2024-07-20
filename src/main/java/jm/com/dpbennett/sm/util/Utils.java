/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
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
package jm.com.dpbennett.sm.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class Utils {

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

        // tk make system options
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

//    public static List getSearchTypes() {
//        ArrayList searchTypes = new ArrayList();
//
//        searchTypes.add(new SelectItem("General", "General"));
//
//        return searchTypes;
//    }

    /*

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
                            System.out.println("Error sending error mail!");
                        }
                    }

                }.start();
            }
        } catch (Exception ex) {
            System.out.println("Error sending error mail!");
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
        InternetAddress addressFrom;
        InternetAddress[] addressTo = null;
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

            addressTo = new InternetAddress[1];
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
            System.out.println("An error occurred while posting an email to: " + Arrays.toString(addressTo));
            return new ReturnMessage(false, "An error occurred while posting an email.");
        }

    }

    public static ReturnMessage postMail(
            Session mailSession,
            String from,
            String to,
            String subject,
            String message,
            String contentType) {

        Message msg;
        InternetAddress addressFrom;
        InternetAddress[] addressTo = null;

        try {
            // use default session if none was provided
            msg = new MimeMessage(mailSession);

            // set the from and to address
            addressFrom = new InternetAddress(
                    from, from);
            msg.setFrom(addressFrom);

            addressTo = new InternetAddress[1];

            addressTo[0] = new InternetAddress(to);

            msg.setRecipients(Message.RecipientType.TO, addressTo);

            // Setting the Subject and Content Type
            msg.setSubject(subject);
            msg.setContent(message, contentType);
            Transport.send(msg);

            return new ReturnMessage();

        } catch (UnsupportedEncodingException | MessagingException e) {
            System.out.println("An error occurred while posting an email to : " + Arrays.toString(addressTo));
            return new ReturnMessage(false, "An error occurred while posting an email.");
        }

    }

     */
}
