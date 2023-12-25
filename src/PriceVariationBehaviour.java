import jade.core.behaviours.TickerBehaviour;

/**
 * Represents the behavior responsible for the agent's price variation.
 * If the agent is satisfied, it will increase its price.
 * If not, he will lower them.
 */
public class PriceVariationBehaviour extends TickerBehaviour {

    /**
     * This constructor creates a price variation behavior for products produced by the agent.
     * @param a consumer-producer agent for which this behavior is linked.
     */
    public PriceVariationBehaviour(ConsumerProducerAgent a) {
        super(a, a.getPriceVariationPeriod());
    }

    /**
     * This function is called every X milliseconds (depending on production speed).
     * If the agent is satisfied, it raises its price.
     * Otherwise, he does nothing.
     */
    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isSatisfied()) {
            consumerProducerAgent.increasePrice();
        } else {
            consumerProducerAgent.decreasePrice();
        }
    }

}
