/*
Financial Management (FM) 
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
package jm.com.dpbennett.fm.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class ProductCatalogAPI {

    @Test
    public void getAllProducts() {
        Stripe.apiKey = "(Get code from JMTS)";

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("limit", 10); // max is 100
            params.put("active", true);

            ProductCollection products = Product.list(params);

            for (Product product : products.getData()) {
                System.out.println("Product: " + product.toJson());
            }

        } catch (StripeException ex) {
            System.out.println(ex);
        }
    }
}