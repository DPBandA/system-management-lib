/*
System Management (SM)
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
package jm.com.dpbennett.sm.manager;

import java.io.Serializable;
import java.util.List;
import javax.faces.event.ActionEvent;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.dm.Post;
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

    public DocumentManager() {
        init();
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

    public void editPost() {

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
        
        // tk
        System.out.println("Creating new post...");

        selectedPost = new Post();

        //openPostDialog();

        //openPostsTab();

    }

    public void doPostSearch() {

        setDefaultCommandTarget("@this");

        if (getIsActivePostsOnly()) {
            //foundPosts = Post.findActive(getEntityManager1(), getPostSearchText());
        } else {
            //foundPosts = Post.find(getEntityManager1(), getPostSearchText());
        }

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
        setSearchType("Posts");
        setDateSearchPeriod(new DatePeriod("This month", "month",
                "dateEntered", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

    }

}
