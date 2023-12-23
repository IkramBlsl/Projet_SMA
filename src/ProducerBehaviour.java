import jade.core.behaviours.CyclicBehaviour;


/**
 * Behavior class for the Producer agent.
 * This behavior is cyclic, continuously checking if space is available in the produced merchandise stock.
 * If there is space, it produces merchandise and adds the produced goods to the stock of merchandise sold by the agent.
 */

public class ProducerBehaviour extends CyclicBehaviour {
    /**
     * Constructor for the ProducerBehaviour class.
     *
     * @param a The instance of the ConsumerProducerAgent.
     */

    public ProducerBehaviour(ConsumerProducerAgent a) {
        super(a);
    }

    /**
     * Action method of the behavior.
     * Checks if space is available in the stock of produced merchandise and performs actions accordingly.
     */

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isSpaceInProducedStock()) {
            block((long) (consumerProducerAgent.getProductionSpeed() * 1000));

            // Produce merchandise
            System.out.println("Producing new " + consumerProducerAgent.getProducedMerchandise());
            consumerProducerAgent.addOneProducedMerchandise();
        } else {
            // Optional: Handle the case where there is no space in the produced stock
            System.out.println("No space in produced stock.");
        }
    }
}
