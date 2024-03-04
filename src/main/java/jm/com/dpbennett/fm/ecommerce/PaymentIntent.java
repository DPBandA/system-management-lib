package jm.com.dpbennett.fm.ecommerce;

/**
 *
 * @author Desmond Bennett
 */
public class PaymentIntent {

    private String id;
    private Long amount;
    // Other properties

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
}
