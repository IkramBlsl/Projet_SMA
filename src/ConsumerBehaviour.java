import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

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
            consumerProducerAgent.sendMessageToProducers();
            // TODO Descendre la satisfaction de l'agent
        }
    }

}