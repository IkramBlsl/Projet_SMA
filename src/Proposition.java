import jade.core.AID;

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

    public AID getSender() {
        return sender;
    }

    public Merchandise getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }

}
