/*
System Management (SM)
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
package jm.com.dpbennett.sm.manager;

import java.io.IOException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.dm.Issue;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.sm.util.BeanUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

/**
 *
 * @author Desmond Bennett
 */
public final class OperationsManager extends GeneralManager {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory SMPU;
    private Issue issue;
    
    public OperationsManager() {
        init();
    }
    
    public void init() {
        reset();
    }
    
    @Override
    public void reset() {
        setName("operationsManager");
    }

    @Override
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public EntityManagerFactory getSMPU() {

        return SMPU;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }
    
     public void createIssue() {

        setIssue(new Issue());

        // tk open issue dialog.
        try {
            // tk get 
            submitGitHubIssue("Issue Title", "This is a test issue from Java!");
        } catch (IOException ex) {
            System.out.println("submitIssue: " + ex);
        }
    }

    // tk
    public void submitGitHubIssue(
            String title,
            String body) throws IOException {

        OkHttpClient client = new OkHttpClient();
        // tk get from system options
        String GITHUB_API_URL = "https://api.github.com/repos/DPBandA/job-management-tracking-system/issues";
        String TOKEN = SystemOption.getString(getEntityManager1(), "GitHubIssueToken");

        // Create JSON payload for the issue
        JSONObject issueDetails = new JSONObject();
        issueDetails.put("title", title);
        issueDetails.put("body", body);

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                issueDetails.toString(),
                MediaType.parse("application/json")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(GITHUB_API_URL)
                .header("Authorization", "token " + TOKEN)
                .post(requestBody)
                .build();

        // tk display growl message here?        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Issue created successfully: "
                        + response.body().string());
            } else {
                System.out.println("Failed to create issue: " + response.code()
                        + " - " + response.message());
            }
        }

    }
    
}
