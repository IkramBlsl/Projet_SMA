import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

/**
 * Behavior class for the Consumer agent.
 * This behavior is cyclic, continuously checking the stock of the consumed merchandise.
 * If there's enough stock, it consumes the merchandise, updating the agent's satisfaction.
 * If not, it decides to purchase from a producer, reducing the agent's satisfaction.
 */
public class ConsumerBehaviour extends TickerBehaviour {

    private boolean previouslySatisfied;

    /**
     * Constructor for the ConsumerBehaviour class.
     *
     * @param a The instance of the ConsumerProducerAgent.
     */
    public ConsumerBehaviour(ConsumerProducerAgent a) {
        super(a, a.getConsumptionSpeed());
        previouslySatisfied = false;
    }

    /**
     * Action method of the behavior.
     * Checks the stock of the consumed merchandise and performs actions accordingly.
     */
    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isStockOfConsumedMerchandise()) {
            // Consuming merchandise
            consumerProducerAgent.removeOneConsumedMerchandise();
            consumerProducerAgent.resetSatisfaction();

            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a consommé.");
        } else {
            // Si le stock est trop faible pour consommer, agir en conséquence
            // décider d'acheter auprès d'un producteur
            if (!consumerProducerAgent.isCurrentlyBuying()) {
                consumerProducerAgent.addBehaviour(new BuyConsumedMerchandiseBehaviour(consumerProducerAgent));
            }
            consumerProducerAgent.decreaseSatisfaction();
            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " n'a pas consommé. Satisfaction : " + consumerProducerAgent.getSatisfaction());
        }

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