/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jm.com.dpbennett.sm.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.faces.model.SelectItem;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class DateUtils {

    public static ArrayList getDateSearchFields() {
        ArrayList dateSearchFields = new ArrayList();

        dateSearchFields.add(new SelectItem("dateAndTimeEntered", "Date entered"));
        dateSearchFields.add(new SelectItem("dateReceived", "Date received"));
        dateSearchFields.add(new SelectItem("dateSubmitted", "Date submitted"));
        dateSearchFields.add(new SelectItem("dateCostingApproved", "Date costing approved"));
        dateSearchFields.add(new SelectItem("dateCostingInvoiced", "Date job invoiced"));
        dateSearchFields.add(new SelectItem("dateOfCompletion", "Date completed"));
        dateSearchFields.add(new SelectItem("expectedDateOfCompletion", "Exp'ted date of completion"));
        dateSearchFields.add(new SelectItem("dateSamplesCollected", "Date sample(s) collected"));
        dateSearchFields.add(new SelectItem("dateDocumentCollected", "Date document(s) collected"));

        return dateSearchFields;
    }

    public static ArrayList getDateSearchFields(String category) {
        ArrayList dateSearchFields = new ArrayList();

        switch (category) {
            case "Job":
                dateSearchFields.add(new SelectItem("dateAndTimeEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateSubmitted", "Date submitted"));
                dateSearchFields.add(new SelectItem("dateCostingApproved", "Date costing approved"));
                dateSearchFields.add(new SelectItem("dateOfCompletion", "Date completed"));
                dateSearchFields.add(new SelectItem("expectedDateOfCompletion", "Exp'ted date of completion"));
                dateSearchFields.add(new SelectItem("dateSamplesCollected", "Date sample(s) collected"));
                dateSearchFields.add(new SelectItem("dateDocumentCollected", "Date document(s) collected"));
            case "Legal":
                dateSearchFields.add(new SelectItem("dateReceived", "Date received"));
                dateSearchFields.add(new SelectItem("dateOfCompletion", "Date delivered"));
                dateSearchFields.add(new SelectItem("expectedDateOfCompletion", "Agreed delivery date"));
                break;
            case "All":
                return getDateSearchFields();
            default:
                return getDateSearchFields();
        }

        return dateSearchFields;
    }

    public static String formatDateAndTime(Date date) {
        String dateAndTime;
        Calendar c = Calendar.getInstance();
        String year;
        String month;
        String day;
        String hour;
        String min;
        String sec;

        c.setTime(date);

        // Year
        year = "" + c.get(Calendar.YEAR);
        // Month        
        int month_int = c.get(Calendar.MONTH) + 1;
        if (month_int < 10) {
            month = "0" + month_int;
        } else {
            month = "" + month_int;
        }
        // Day
        int day_int = c.get(Calendar.DAY_OF_MONTH);
        if (day_int < 10) {
            day = "0" + day_int;
        } else {
            day = "" + day_int;
        }
        // Hour
        int hour_int = c.get(Calendar.HOUR_OF_DAY);
        if (hour_int < 10) {
            hour = "0" + hour_int;
        } else {
            hour = "" + hour_int;
        }
        // Min
        int min_int = c.get(Calendar.MINUTE);
        if (min_int < 10) {
            min = "0" + min_int;
        } else {
            min = "" + min_int;
        }
        // Sec
        int sec_int = c.get(Calendar.SECOND);
        if (sec_int < 10) {
            sec = "0" + sec_int;
        } else {
            sec = "" + sec_int;
        }
        
        dateAndTime = year + "-" + month  + "-" + day + " " + 
                hour  + ":" + min  + ":" + sec;

        return dateAndTime;
    }
}
