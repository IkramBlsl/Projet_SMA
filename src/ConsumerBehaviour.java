import jade.core.behaviours.CyclicBehaviour;

public class ConsumerBehaviour extends CyclicBehaviour {

    public ConsumerBehaviour(ConsumerProducerAgent a) {
        super(a);
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isStockOfConsumedMerchandise()) {
            block((long) (consumerProducerAgent.getConsumptionSpeed() * 1000));

            // Consuming merchandise
            System.out.println("Consuming " + consumerProducerAgent.getConsumedMerchandise());
            consumerProducerAgent.removeOneConsumedMerchandise();

            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a consommé. Satisfaction : " + consumerProducerAgent.getSatisfaction());
        } else {
            // Si le stock est trop faible pour consommer, agir en conséquence
            // décider d'acheter auprès d'un producteur
            consumerProducerAgent.addBehaviour(new BuyConsumedMerchandiseBehaviour(consumerProducerAgent, (long) (consumerProducerAgent.getConsumptionSpeed() * 1000)));
            consumerProducerAgent.decreaseSatisfaction(0.1f); // Réduire la satisfaction ( de 0.1 à changer )
            block();
            // TODO Descendre la satisfaction de l'agent
        }
    }

}