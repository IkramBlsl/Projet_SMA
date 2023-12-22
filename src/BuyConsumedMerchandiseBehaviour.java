import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class BuyConsumedMerchandiseBehaviour extends SimpleBehaviour {

    private long startTime;
    private final long timeout;  // Timeout in milliseconds
    private ConsumerProducerAgent ConsumerProducerAgent; //

    public BuyConsumedMerchandiseBehaviour(ConsumerProducerAgent a, long timeout) {
        super(a);
        this.timeout = timeout;
        this.ConsumerProducerAgent = a;
    }

    @Override
    public void onStart() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        // Send CFP to all producers
        consumerProducerAgent.sendCFPToConsumedMerchandiseProducers();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        ACLMessage msg = consumerProducerAgent.receive();
        if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
            // TODO : Process message
        }
    }

    @Override
    public boolean done() {
        if (System.currentTimeMillis() - startTime >= timeout) {
            // TODO : Accept the best proposal among all of the received proposals from the producers

            ConsumerProducerAgent.setSatisfaction(1.0f); // Définir la satisfaction à 1.0 directement
            return true;
        }
        return false;
    }

}
