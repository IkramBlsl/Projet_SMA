import jade.core.Agent;

public class ConsumerProducerAgent extends Agent {

    private Merchandise consumedMerchandise;
    private Merchandise producedMerchandise;

    private float priceProducedMerchandise = 1;
    private int stockProducedMerchandise = 0;
    private int maxStockProducedMerchandise = 500; // TODO

    public float consumptionSpeed;
    private float productionSpeed;

    private int stockConsumedMerchandise = 0;
    private float money = 10;
    private float satisfaction = 1;

    protected void setup() {
        // TODO : Handle production & consumption speed
        productionSpeed = 3;
        consumptionSpeed = 3;

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

        System.out.println("Consumed Merchandise is " + consumedMerchandise);
        System.out.println("Produced Merchandise is " + producedMerchandise);

        // Consumer Behaviour
        addBehaviour(new ConsumerBehaviour(this));

        // Producer Behaviour
        addBehaviour(new ProducerBehaviour(this));
    }

    private void cloneAgent() {}

    public boolean isSpaceInProducedStock() {
        return stockProducedMerchandise < maxStockProducedMerchandise;
    }

    public boolean isStockOfConsumedMerchandise() {
        return stockConsumedMerchandise > 0;
    }

    public void addProducedMerchandise() {
        if (stockProducedMerchandise < maxStockProducedMerchandise) {
            stockProducedMerchandise++;
        } else {
            throw new RuntimeException("No Space left in Produced Merchandise stock.");
        }
    }

    public void removeConsumedMerchandise() {
        if (stockConsumedMerchandise > 0) {
            stockConsumedMerchandise--;
        } else {
            throw new RuntimeException("No Consumed Merchandise left.");
        }
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

    public float getConsumptionSpeed() {
        return consumptionSpeed;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

}
