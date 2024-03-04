package jm.com.dpbennett.fm.ecommerce;

/**
 *
 * @author Desmond Bennett
 */
public class StripeEvent {

    private String id;
    private PaymentIntent data;
    // Other properties

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentIntent getData() {
        return data;
    }

    public void setData(PaymentIntent data) {
        this.data = data;
    }
    
}
