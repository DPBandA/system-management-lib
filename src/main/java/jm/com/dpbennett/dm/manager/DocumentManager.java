/*
Document Management (SM)
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
package jm.com.dpbennett.dm.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.dm.Attachment;
import jm.com.dpbennett.business.entity.dm.DocumentType;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.dm.Post;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.manager.SystemManager;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author Desmond Bennett
 */
public class DocumentManager extends GeneralManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isActiveDocumentTypesOnly;
    private Boolean isActiveEmailsOnly;
    private Boolean isActivePostsOnly;
    private String postSearchText;
    private String attachmentSearchText;
    private String emailSearchText;
    private String documentTypeSearchText;
    private UploadedFile uploadedFile;
    private List<Post> foundPosts;
    private List<DocumentType> foundDocumentTypes;
    private List<Attachment> foundAttachments;
    private List<Email> foundEmails;
    private Post selectedPost;
    private DocumentType selectedDocumentType;
    private Attachment attachment;
    private Email selectedEmail;
    private Integer innerTabIndex;

    public DocumentManager() {
        init();
    }

    public List getContentTypes() {

        return getStringListAsSelectItems(getEntityManager1(),
                "contentTypeList");
    }

    public List<SelectItem> getAttachmentTypeList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "attachmentTypeList");
    }

    @Override
    public void reInitUI() {
        setInnerTabIndex(0);
        setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:documentTypeSearchButton");
    }

    @Override
    public void openDashboardTab(String title) {

        setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:documentTypeSearchButton");

    }

    @Override
    public void openMainViewTab(String title) {

        setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:documentTypeSearchButton");

        getSystemManager().getMainTabView().openTab(title);
    }

    public Integer getInnerTabIndex() {
        return innerTabIndex;
    }

    public void setInnerTabIndex(Integer innerTabIndex) {
        this.innerTabIndex = innerTabIndex;
    }

    @Override
    public boolean handleTabChange(String tabTitle) {

        switch (tabTitle) {

            case "Document Types":
                setInnerTabIndex(0);
                setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:documentTypeSearchButton");
                return true;
            case "Attachments":
                setInnerTabIndex(1);
                setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:attachmentSearchButton");
                return true;
            case "Email Templates":
                setInnerTabIndex(2);
                setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:emailTemplateSearchButton");
                return true;
            case "Posts":
                setInnerTabIndex(3);
                setDefaultCommandTarget(":mainTabViewForm:mainTabView:DMCenterTabView:postSearchButton");
                return true;
            default:
                return false;
        }

    }

    public void okAttachment() {
        if (getUploadedFile() != null) {
            uploadAttachment();

        } else {
            PrimeFacesUtils.addMessage("No File", "No file was choosen",
                    FacesMessage.SEVERITY_INFO);
        }
    }

    public void uploadAttachment() {

        try {

            OutputStream outputStream;

            String sourceURL = getUploadedFile().getFileName();
            getAttachment().setSourceURL(sourceURL);
            if (getAttachment().getName().isEmpty()) {
                getAttachment().setName(sourceURL);
            }

            File fileToSave = new File(getAttachment().getDestinationURL() + getUploadedFile().getFileName());
            outputStream = new FileOutputStream(fileToSave);
            outputStream.write(getUploadedFile().getContent());
            outputStream.close();

            PrimeFacesUtils.addMessage("Succesful", getUploadedFile().getFileName() + " was uploaded.", FacesMessage.SEVERITY_INFO);

            getAttachment().save(getEntityManager1());

            closeDialog(null);

        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            PrimeFacesUtils.addMessage("File Not Found", getUploadedFile().getFileName() + " was NOT found", FacesMessage.SEVERITY_ERROR);
        } catch (IOException ex) {
            System.out.println(ex);
            PrimeFacesUtils.addMessage("Read/Write Error", " A read/write error occured with the file "
                    + getUploadedFile().getFileName(), FacesMessage.SEVERITY_ERROR);
        }

    }

    public void createNewAttachment() {
        attachment = new Attachment();
        String destURL = (String) SystemOption.getOptionValueObject(getEntityManager1(),
                "defaultUploadLocation");
        attachment.setDestinationURL(destURL);
        openAttachmentDialog();
    }

    public void openAttachmentDialog() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/doc/attachmentDialog", options, null);

    }

    public void openDocumentTypeDialog(String url) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic(url, options, null);

    }

    public void cancelDocumentTypeEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void editDocumentType() {
        openDocumentTypeDialog("/doc/documentTypeDialog");
    }

    public List<DocumentType> getDocumentTypes() {
        return DocumentType.findAll(getEntityManager1());
    }

    public void saveSelectedDocumentType() {

        selectedDocumentType.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewDocumentType() {
        selectedDocumentType = new DocumentType();

        editDocumentType();

    }

    public void doDocumentTypeSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Document Types",
                getDocumentTypeSearchText(),
                null,
                null);

    }

    public Boolean getIsActiveDocumentTypesOnly() {
        return isActiveDocumentTypesOnly;
    }

    public void setIsActiveDocumentTypesOnly(Boolean isActiveDocumentTypesOnly) {
        this.isActiveDocumentTypesOnly = isActiveDocumentTypesOnly;
    }

    public String getDocumentTypeSearchText() {
        return documentTypeSearchText;
    }

    public void setDocumentTypeSearchText(String documentTypeSearchText) {
        this.documentTypeSearchText = documentTypeSearchText;
    }

    public DocumentType getSelectedDocumentType() {
        return selectedDocumentType;
    }

    public void setSelectedDocumentType(DocumentType selectedDocumentType) {
        this.selectedDocumentType = selectedDocumentType;
    }

    public List<DocumentType> getFoundDocumentTypes() {
        if (foundDocumentTypes == null) {
            foundDocumentTypes = new ArrayList<>();
        }

        return foundDocumentTypes;
    }

    public void setFoundDocumentTypes(List<DocumentType> foundDocumentTypes) {
        this.foundDocumentTypes = foundDocumentTypes;
    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {

        int maxSearchResults = SystemOption.getInteger(getEntityManager1(),
                "maxSearchResults");

        switch (searchType) {

            case "Document Types":
                if (getIsActiveDocumentTypesOnly()) {
                    foundDocumentTypes = DocumentType.findAllActiveByName(getEntityManager1(),
                            searchText);
                } else {
                    foundDocumentTypes = DocumentType.findAllByName(getEntityManager1(),
                            searchText);
                }

                break;

            case "Attachments":

                foundAttachments = Attachment.findAttachmentsByName(getEntityManager1(),
                        searchText, maxSearchResults);

                break;
            default:
                break;
        }
    }

    public void doAttachmentSearch() {

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Attachments",
                getAttachmentSearchText(),
                null,
                null);

    }

    public List<Attachment> getFoundAttachments() {
        if (foundAttachments == null) {
            foundAttachments = new ArrayList<>();
        }
        return foundAttachments;
    }

    public Boolean getIsActiveEmailsOnly() {
        return isActiveEmailsOnly;
    }

    public void setIsActiveEmailsOnly(Boolean isActiveEmailsOnly) {
        this.isActiveEmailsOnly = isActiveEmailsOnly;
    }

    public void doEmailSearch() {

        if (getIsActiveEmailsOnly()) {
            foundEmails = Email.findActiveEmails(getEntityManager1(), getEmailSearchText());
        } else {
            foundEmails = Email.findEmails(getEntityManager1(), getEmailSearchText());
        }

    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getAttachmentSearchText() {
        return attachmentSearchText;
    }

    public void setAttachmentSearchText(String attachmentSearchText) {
        this.attachmentSearchText = attachmentSearchText;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public String getEmailSearchText() {
        return emailSearchText;
    }

    public void setEmailSearchText(String emailSearchText) {
        this.emailSearchText = emailSearchText;
    }

    public List<Email> getFoundEmails() {
        if (foundEmails == null) {
            foundEmails = new ArrayList<>();
        }

        return foundEmails;
    }

    public void setFoundEmails(List<Email> foundEmails) {
        this.foundEmails = foundEmails;
    }

    public Email getSelectedEmail() {
        return selectedEmail;
    }

    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public void editEmailTemplate(String dialog) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/doc/" + dialog, options, null);

    }

    public void saveSelectedEmail() {

        selectedEmail.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void createNewEmailTemplate(String dialog) {

        selectedEmail = new Email();

        editEmailTemplate(dialog);
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

    public Employee getEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();

        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    public Boolean getIsNewPost() {
        return getSelectedPost().getId() == null;
    }

    public void cancelPostEdit() {
        getSelectedPost().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updatePost() {
        getSelectedPost().setIsDirty(true);
    }

    public void okPost() {

        try {

            if (getIsNewPost()) {
                getSelectedPost().setDateEntered(new Date());
                getSelectedPost().setDateEdited(new Date());

                if (getUser() != null) {
                    getSelectedPost().setEditedBy(getEmployee());
                }
            }

            if (getSelectedPost().getIsDirty()) {
                getSelectedPost().setDateEdited(new Date());
                if (getUser() != null) {
                    getSelectedPost().setEditedBy(getEmployee());
                }
                getSelectedPost().save(getEntityManager1());
                getSelectedPost().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public SystemManager getSystemManager() {

        return BeanUtils.findBean("systemManager");

    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager1();
    }

    public Boolean getIsActivePostsOnly() {
        return isActivePostsOnly;
    }

    public void setIsActivePostsOnly(Boolean isActivePostsOnly) {
        this.isActivePostsOnly = isActivePostsOnly;
    }

    public String getPostSearchText() {
        return postSearchText;
    }

    public void setPostSearchText(String postSearchText) {
        this.postSearchText = postSearchText;
    }

    public List<Post> getFoundPosts() {
        if (foundPosts == null) {
            foundPosts = new ArrayList<>();
        }

        return foundPosts;
    }

    public void setFoundPosts(List<Post> foundPosts) {
        this.foundPosts = foundPosts;
    }

    public Post getSelectedPost() {
        return selectedPost;
    }

    public void setSelectedPost(Post selectedPost) {
        this.selectedPost = selectedPost;
    }

    @Override
    public Integer getDialogHeight() {
        return 400;
    }

    @Override
    public Integer getDialogWidth() {
        return 500;
    }

    public void editSelectedPost(String dialog) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width(getDialogWidth() + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(true)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/doc/" + dialog, options, null);

    }

    public void createNewPost(String dialog) {

        selectedPost = new Post();
        selectedPost.setIsDirty(true);

        editSelectedPost(dialog);

    }

    public void doPostSearch() {

        int maxSearchResults = SystemOption.getInteger(getEntityManager1(),
                "maxSearchResults");

        if (getIsActivePostsOnly()) {
            foundPosts = Post.findActive(getEntityManager1(), getPostSearchText(), maxSearchResults);
        } else {
            foundPosts = Post.find(getEntityManager1(), getPostSearchText(), maxSearchResults);
        }

    }

    public int getNumberOfPosts() {
        return getFoundPosts().size();
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void init() {

        reset();
    }

    @Override
    public void reset() {
        super.reset();

        setName("documentManager");
        isActivePostsOnly = true;
        postSearchText = "";
        documentTypeSearchText = "";
        attachmentSearchText = "";
        emailSearchText = "";
        isActiveDocumentTypesOnly = true;
        isActiveEmailsOnly = true;
        setSearchType("Posts");
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

    }

}
