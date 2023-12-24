import jade.core.AID;

/**
 * Represents a proposition for selling merchandise by a producer.
 * Contains information such as the sender, the merchandise, quantity, and price.
 */
public class Proposition {

    private AID sender;
    private Merchandise product;
    private int quantity;
    private float price;

    public Proposition(AID sender, Merchandise product, int quantity, float price) {
        this.sender = sender;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Retrieves the sender of the proposition.
     * @return The sender AID of the proposition
     */
    public AID getSender() {
        return sender;
    }

    /**
     * Retrieves the merchandise of the proposition.
     * @return The merchandise offered in the proposition
     */
    public Merchandise getProduct() {
        return product;
    }


    /**
     * Retrieves the quantity of merchandise offered in the proposition.
     * @return The quantity of merchandise in the proposition
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Retrieves the price of the merchandise offered in the proposition.
     * @return The price of the merchandise in the proposition
     */
    public float getPrice() {
        return price;
    }

}
