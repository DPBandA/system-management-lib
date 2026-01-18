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
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.model.Product;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class ProductAPI {

  
    @Test
    public void getById() {
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
             
        Stripe.apiKey = "sk_test_51IdfUQGCbrRYfT2gOdvWJFrKI8qU2oZ0jGaahLhiB6ZJ3CrrJzGrhyQmiC1re6I5jd7CwQELcxJFbYv8Si9mn7TF007wjcKqaA"; 
        try {
            
            Product p = Product.retrieve("prod_PebUyutppyNSey");
            
            System.out.println("Product: " + p.toJson());
            
            
        } catch (StripeException ex) {
            System.out.println(ex);
        }
    }
}
