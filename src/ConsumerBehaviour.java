import jade.core.behaviours.TickerBehaviour;

/**
 * Behavior class for the Consumer agent.
 * This behavior is cyclic, continuously checking the stock of the consumed merchandise.
 * If there's enough stock, it consumes the merchandise, updating the agent's satisfaction.
 * If not, it decides to purchase from a producer, reducing the agent's satisfaction.
 */
public class ConsumerBehaviour extends TickerBehaviour {

    /**
     * Constructor for the ConsumerBehaviour class.
     *
     * @param a The instance of the ConsumerProducerAgent.
     */
    public ConsumerBehaviour(ConsumerProducerAgent a) {
        super(a, a.getConsumptionSpeed());
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
            System.out.println("Consuming " + consumerProducerAgent.getConsumedMerchandise());
            consumerProducerAgent.removeOneConsumedMerchandise();

            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a consommé. Satisfaction : " + consumerProducerAgent.getSatisfaction());
            consumerProducerAgent.setSatisfaction(1.0f);
        } else {
            // Si le stock est trop faible pour consommer, agir en conséquence
            // décider d'acheter auprès d'un producteur
            consumerProducerAgent.addBehaviour(new BuyConsumedMerchandiseBehaviour(consumerProducerAgent, consumerProducerAgent.getConsumptionSpeed()));
            consumerProducerAgent.decreaseSatisfaction(0.1f); // Réduire la satisfaction ( de 0.1 à changer )
        }
    }

}