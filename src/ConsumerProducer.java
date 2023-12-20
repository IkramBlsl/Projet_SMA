import jade.core.Agent;

public class ConsumerProducer extends Agent {

    private Merchandise consumedMerchandise;
    private Merchandise producedMerchandise;

    private float priceProducedMerchandise = 1;
    private int stockProducedMerchandise = 0;
    private int maxStockProducedMerchandise = 500; // TODO

    private float productionSpeed;

    private int stockConsumedMerchandise = 0;
    private float money = 10;
    private float satisfaction = 1;

    protected void setup() {
        // TODO : Handle production speed
        productionSpeed = 3;

        System.out.println("Hello World (not new!...)!");
        System.out.println("My name is " + getAID().getName());
        System.out.println("My local name is " + getLocalName());

        Object[] args = getArguments();
        try {
            Merchandise consumedMerchandise = Merchandise.parseMerchandise(args[0].toString());
            Merchandise producedMerchandise = Merchandise.parseMerchandise(args[1].toString());

            this.consumedMerchandise = consumedMerchandise;
            this.producedMerchandise = producedMerchandise;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Terminating agent due to exception.");
            doDelete(); // Terminate the agent.
            // TODO : Dealing better with this catch block
            return;
        }

        // Ajout du comportement du consommateur
        addBehaviour(new ConsumBehaviour(this, stockConsumedMerchandise,10));
        System.out.println("Consumed Merchandise is " + consumedMerchandise);
        System.out.println("Produced Merchandise is " + producedMerchandise);

        addBehaviour(new ProducerBehaviour(this));
    }

    private void cloneAgent() {}

    public boolean isSpaceInProducedStock() {
        return stockProducedMerchandise < maxStockProducedMerchandise;
    }

    public void addProducedMerchandise() {
        stockProducedMerchandise++;
    }

    public Merchandise getConsumedMerchandise() {
        return consumedMerchandise;
    }

    public Merchandise getProducedMerchandise() {
        return producedMerchandise;
    }

    public float getProductionSpeed() {
        return productionSpeed;
    }

}
