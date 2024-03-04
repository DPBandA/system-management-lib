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
