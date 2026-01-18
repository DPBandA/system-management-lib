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
package jm.com.dpbennett.fm.ecommerce;

import com.google.gson.Gson;
import com.stripe.Stripe;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.EventRetrieveParams;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.sm.SystemOption;

public class StripeWebhookHandler extends HttpServlet {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;

    public EntityManager getEntityManager1() {
        return EMF.createEntityManager();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String payload = sb.toString();

        Gson gson = new Gson();
        StripeEvent event = gson.fromJson(payload, StripeEvent.class);

        if (event != null) {
            processWebhookEvent(event);
        } else {
            System.out.println("StriveEvent NOT received!");
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void processWebhookEvent(StripeEvent event) {

        Stripe.apiKey = SystemOption.getString(getEntityManager1(), "stripe_api_key");
        String eventId = event.getId();

        try {

            EventRetrieveParams params = EventRetrieveParams.builder().build();
            Event event2 = Event.retrieve(eventId, params, null);

            switch (event2.getType()) {
                case "payment_intent.succeeded":
                    // tk
                    System.out.println("Got: " + event2.getType());
                    break;
                default:
                    break;
            }

        } catch (StripeException e) {
            System.out.println(e);
        }

    }   
    
}
