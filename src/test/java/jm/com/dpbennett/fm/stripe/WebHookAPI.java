package jm.com.dpbennett.fm.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett <info@dpbennett.com.jm at http//dpbennett.com.jm>
 */
public class WebHookAPI {

    @Test
    public void getById() {

        Stripe.apiKey = "sk_test_51IdfUQGCbrRYfT2gOdvWJFrKI8qU2oZ0jGaahLhiB6ZJ3CrrJzGrhyQmiC1re6I5jd7CwQELcxJFbYv8Si9mn7TF007wjcKqaA";
        try {
            Customer cus = Customer.retrieve("cus_Mqg7eRrw2R3Aod");
            System.out.println("Name: " + cus.getName());
        } catch (StripeException ex) {
            System.out.println(ex);
        }
        
        
    }
}
