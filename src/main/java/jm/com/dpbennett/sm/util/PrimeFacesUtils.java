/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jm.com.dpbennett.sm.util;

import java.util.HashMap;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

/**
 *
 * @author D P Bennett
 */
public class PrimeFacesUtils {

    public static void openDialog(Object entity,
            String outcome,
            Boolean modal,
            Boolean draggable,
            Boolean resizable,
            Integer contentHeight,
            Integer contentWidth) {

        PrimeFacesUtils.openDialog(null, outcome, modal, draggable, resizable, true, contentHeight, contentWidth);

    }

    public static void openDialog(Object entity,
            String outcome,
            Boolean modal,
            Boolean draggable,
            Boolean resizable,
            Boolean closable,
            Integer contentHeight,
            Integer contentWidth) {

        Map<String, Object> options = new HashMap<>();
        options.put("modal", modal);
        options.put("draggable", draggable);
        options.put("resizable", resizable);
        options.put("closable", closable);
        options.put("contentHeight", contentHeight);
        options.put("contentWidth", contentWidth);

        PrimeFaces.current().dialog().openDynamic(outcome, options, null);

    }

    public static void closeDialog(Object data) {
        PrimeFaces.current().dialog().closeDynamic(data);
    }

    public static void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
}
