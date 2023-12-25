import jade.core.AID;

/**
 * Represents a proposition for selling product by a producer.
 * Contains information such as the sender, the product, the available quantity, and the price.
 */
public class Proposition {

    private final AID senderReceiver;
    private final Product product;
    private final int availableQuantity;
    private final float price;

    /**
     * This builder creates a sales proposal for products.
     * @param senderReceiver The AID of the sender/receiver of the proposition.
     * @param product The product in the proposition.
     * @param availableQuantity The available quantity of the product in the proposition.
     * @param price The price of the product in the proposition.
     */
    public Proposition(AID senderReceiver, Product product, int availableQuantity, float price) {
        this.senderReceiver = senderReceiver;
        this.product = product;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    /**
     * Retrieves the sender/receiver of the proposition.
     * @return The sender/receiver AID of the proposition.
     */
    public AID getSenderReceiver() {
        return senderReceiver;
    }

    /**
     * Retrieves the product in the proposition.
     * @return The product in the proposition.
     */
    public Product getProduct() {
        return product;
    }


    /**
     * Retrieves the available quantity of the product in the proposition.
     * @return The available quantity of the product in the proposition.
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Retrieves the price of the product in the proposition.
     * @return The price of the product in the proposition.
     */
    public float getPrice() {
        return price;
    }

}
