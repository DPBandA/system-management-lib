/*
Financial Management (FM) 
Copyright (C) 2021  D P Bennett & Associates Limited

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
package jm.com.dpbennett.sm.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.dm.Attachment;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class FileUploadManager {

    private UploadedFile uploadedFile;
    private List<Attachment> attachments;
    
    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");
    }
    
    public EntityManager getEntityManager() {
        return getSystemManager().getEntityManager1();
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
    
    public void upload() {
        if (uploadedFile != null) {
            FacesMessage message = new FacesMessage("Successful", uploadedFile.getFileName() + " was uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            OutputStream outputStream;
            
            // Save file
            File fileToSave = 
                    new File(SystemOption.getOptionValueObject(getEntityManager(), 
                            "purchReqUploadFolder") + 
                            event.getFile().getFileName());
            outputStream = new FileOutputStream(fileToSave);
            outputStream.write(event.getFile().getContent());
            outputStream.close();
            
            // Create attachment
            
            PrimeFacesUtils.addMessage("Successful!", event.getFile().getFileName() + " was uploaded.", FacesMessage.SEVERITY_INFO);

        } catch (IOException ex) {
            Logger.getLogger(FileUploadManager.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public void okAttachment() {
        //tk
        // The return of this dialog should be handled by "dialogReturn" event?
        // for now just close the dialog.
        System.out.println("ok attachment...");
        PrimeFacesUtils.closeDialog(null);

    }

    public void closeDialog() {
        PrimeFacesUtils.closeDialog(null);
    }
}
