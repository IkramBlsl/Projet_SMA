import jade.core.behaviours.TickerBehaviour;


/**
 * Behavior class for the Producer agent.
 * This behavior is cyclic, continuously checking if space is available in the produced merchandise stock.
 * If there is space, it produces merchandise and adds the produced goods to the stock of merchandise sold by the agent.
 */
public class ProducerBehaviour extends TickerBehaviour {

    /**
     * Constructor for the ProducerBehaviour class.
     *
     * @param a The instance of the ConsumerProducerAgent.
     */
    public ProducerBehaviour(ConsumerProducerAgent a) {
        super(a, a.getProductionSpeed());
    }

    /**
     * Tick method of the behavior.
     * Checks if space is available in the stock of produced merchandise and performs actions accordingly.
     */
    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isSpaceInProducedStock()) {
            // Produce merchandise
            consumerProducerAgent.addOneProducedMerchandise();
        } else {
            // Optional: Handle the case where there is no space in the produced stock
            System.out.println("No space in produced stock.");
        }
    }

}
