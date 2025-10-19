/*
Job Management & Tracking System (JMTS) 
Copyright (C) 2025  D P Bennett & Associates Limited

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
package jm.com.dpbennett.jmts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import jm.com.dpbennett.business.entity.jmts.Job;

/**
 *
 * @author Desmond Bennett
 */
//@Named(value = "App")
//@ApplicationScoped
//@Singleton
public class JMTSApplication implements Serializable {

    private final Map<String, String> themes = new TreeMap<>();
    private final List<Job> openedJobs;

    public JMTSApplication() {
        openedJobs = new ArrayList<>();
    }

    public List<Job> getOpenedJobs() {
        return openedJobs;
    }

    public void addOpenedJob(Job job) {
        getOpenedJobs().add(job);
    }

    public void removeOpenedJob(Job job) {
        getOpenedJobs().remove(job);
    }

    public Job findOpenedJob(Long jobId) {
        for (Job job : openedJobs) {
            if (Objects.equals(job.getId(), jobId)) {
                return job;
            }
        }

        return null;
    }

    public Map<String, String> getThemes() {
        return themes;
    }

}
