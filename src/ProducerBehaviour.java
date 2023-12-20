import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class ProducerBehaviour extends CyclicBehaviour {

    public ProducerBehaviour(ConsumerProducer a) {
        super(a);
    }

    @Override
    public void action() {
        ConsumerProducer consumerProducerAgent = (ConsumerProducer) myAgent;

        if (consumerProducerAgent.isSpaceInProducedStock()) {
            block((long) (consumerProducerAgent.getProductionSpeed() * 1000));

            // Produce merchandise
            System.out.println("Producing new " + consumerProducerAgent.getProducedMerchandise());
            consumerProducerAgent.addProducedMerchandise();
        } else {
            // Optional: Handle the case where there is no space in the produced stock
            System.out.println("No space in produced stock.");
        }
    }
}
