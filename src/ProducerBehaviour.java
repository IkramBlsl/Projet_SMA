import jade.core.behaviours.TickerBehaviour;


/**
 * Behavior class for the Producer agent.
 * This behavior is fired every X milliseconds (determined by the production speed).
 * If there is space in the produced stock, it produces product and adds the produced goods to the stock of produced product of the agent.
 */
public class ProducerBehaviour extends TickerBehaviour {

    /**
     * This constructor creates a production behavior for the product produced by the agent.
     * @param a consumer-producer agent for which this behavior is linked.
     */
    public ProducerBehaviour(ConsumerProducerAgent a) {
        super(a, a.getProductionSpeed());
    }

    /**
     * This function is called every X milliseconds (depending on production speed).
     * If there is room in the stock of products produced, then a product is added to this stock.
     * Otherwise, does nothing.
     */
    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isSpaceInProducedStock()) {
            consumerProducerAgent.addOneProducedProduct();
        }
    }

}
