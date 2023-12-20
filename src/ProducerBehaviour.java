import jade.core.behaviours.CyclicBehaviour;

public class ProducerBehaviour extends CyclicBehaviour {

    public ProducerBehaviour(ConsumerProducerAgent a) {
        super(a);
    }

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
