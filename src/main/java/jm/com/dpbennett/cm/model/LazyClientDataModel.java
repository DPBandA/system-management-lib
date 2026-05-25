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
package jm.com.dpbennett.cm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.cm.Client;
import jm.com.dpbennett.cm.manager.ClientManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

/**
 *
 * @author Desmond Bennett
 */
public class LazyClientDataModel extends LazyDataModel<Client> {

    private static final long serialVersionUID = 1L;

    private List<Client> datasource;

    public LazyClientDataModel() {
        datasource = new ArrayList<>();
    }

    @Override
    public Client getRowData(String rowKey) {
        for (Client client : datasource) {
            if (Objects.equals(client.getId(), Long.valueOf(rowKey))) {
                return client;
            }
        }

        return null;
    }

    @Override
    public String getRowKey(Client client) {
        return String.valueOf(client.getId());
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {

        return datasource.size();
    }

    private ClientManager getClientManager() {

        return BeanUtils.findBean("clientManager");
    }

    @Override
    public List<Client> load(int offset, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

        EntityManager em = getClientManager().getEntityManager1();
        datasource = new ArrayList<>();
        FilterMeta filterValue;

        for (Map.Entry<String, FilterMeta> entry : filterBy.entrySet()) {

            filterValue = entry.getValue();

            if (filterValue.getFilterValue() != null) {
                datasource = Client.findActive(em, offset, pageSize, filterValue.getFilterValue().toString());
            } 
        }

        return datasource;
    }

}
