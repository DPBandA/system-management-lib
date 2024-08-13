/*
System Management (SM)
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
package jm.com.dpbennett.sm.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.dm.Post;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public final class DocumentManager extends GeneralManager implements Serializable {

    private Boolean isActivePostsOnly;
    private String postSearchText;
    private List<Post> foundPosts;
    private Post selectedPost;
    private SystemManager systemManager;

    public DocumentManager() {
        init();
    }

    public Employee getEmployee() {
        EntityManager hrmem = getHumanResourceManager().getEntityManager1();

        return Employee.findById(hrmem, getUser().getEmployee().getId());
    }

    public HumanResourceManager getHumanResourceManager() {

        return BeanUtils.findBean("humanResourceManager");
    }

    @Override
    public User getUser() {

        return getSystemManager().getUser();
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

            // Update tracking
            if (getIsNewPost()) {
                getSelectedPost().setDateEntered(new Date());
                getSelectedPost().setDateEdited(new Date());

                if (getUser() != null) {
                    getSelectedPost().setEditedBy(getEmployee());
                }
            }

            // Do save
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

    public SystemManager getSystemManager() {

        if (systemManager == null) {
            systemManager = BeanUtils.findBean("systemManager");
        }

        return systemManager;
    }

    @Override
    public EntityManager getEntityManager1() {

        return getSystemManager().getEntityManager("SMEM");
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
            //foundPosts = Post.findActive(getEntityManager1(), "", 25);
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

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public void editSelectedPost() {

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

        PrimeFaces.current().dialog().openDynamic("/admin/postDialog", options, null);

    }

    public void createNewPost() {

        selectedPost = new Post();
        selectedPost.setIsDirty(true);

        editSelectedPost();

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

    @Override
    public void init() {

        reset();
    }

    @Override
    public void reset() {
        super.reset();

        isActivePostsOnly = true;
        postSearchText = "";
        setSearchType("Posts");
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

    }

}
