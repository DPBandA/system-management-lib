/*
Job Management & Tracking System (JMTS) 
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
package jm.com.dpbennett.jmts.converter;

import javax.faces.convert.FacesConverter;
import jm.com.dpbennett.business.entity.jmts.Job;
import jm.com.dpbennett.sm.converter.EntityConverter;

/**
 *
 * @author Desmond Bennett
 */
@FacesConverter(value = "jobConverter", managed = true)
public class JobConverter extends EntityConverter<Job> {

    public JobConverter() {
        super(Job.class);
    }

    @Override
    protected Long getId(Job job) {
        return job.getId();
    }

}
