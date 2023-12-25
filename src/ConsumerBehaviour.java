import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

/**
 * Represents an agent's responsible consumption behavior.
 * This behavior is triggered every X milliseconds (determined by the consumption speed).
 * If the agent has stocks of the product consumed, it consumes a product.
 * If not, it will try to buy the product from a producer. And his satisfaction will decrease.
 */
public class ConsumerBehaviour extends TickerBehaviour {

    private boolean previouslySatisfied;

    /**
     * This constructor creates a consumption behavior for the product consumed by the agent.
     * @param a consumer-producer agent for which this behavior is linked.
     */
    public ConsumerBehaviour(ConsumerProducerAgent a) {
        super(a, a.getConsumptionSpeed());
        previouslySatisfied = false;
    }

    /**
     * This function is called every X milliseconds (depending on consumption speed).
     * If the agent has stocks of the product consumed, he will consume a product.
     * If not, he will try to buy the product from a producer. And his satisfaction will decrease.
     */
    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isStockOfConsumedProduct()) {
            // If there is stock of the product consumed, it consumes one of the products in the stock.
            consumerProducerAgent.removeOneConsumedProduct();
            if (consumerProducerAgent.getSatisfaction() < 1) {
                consumerProducerAgent.resetSatisfaction();
            }
            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a consommé.");
        } else {
            // If there is no longer any stock of the product consumed by the agent, then the agent will try to buy the product in question (if he isn't already doing so).
            // And his satisfaction will decrease exponentially.
            if (!consumerProducerAgent.isCurrentlyBuying()) {
                consumerProducerAgent.addBehaviour(new BuyConsumedProductBehaviour(consumerProducerAgent));
            }
            consumerProducerAgent.decreaseSatisfaction();
            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " n'a pas consommé. Satisfaction : " + consumerProducerAgent.getSatisfaction());
        }

        // If the agent becomes satisfied (it wasn't before), it will decide to clone itself with a certain probability.
        if (!previouslySatisfied && consumerProducerAgent.isSatisfied()) {
            Random random = new Random();
            if (random.nextFloat(0, 1) < 0.4) {
                consumerProducerAgent.cloneAgent();
            }
            previouslySatisfied = true;
        } else if (previouslySatisfied && !consumerProducerAgent.isSatisfied()) {
            previouslySatisfied = false;
        }
    }

}