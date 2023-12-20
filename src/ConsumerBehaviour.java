import jade.core.behaviours.CyclicBehaviour;

public class ConsumerBehaviour extends CyclicBehaviour {

    public ConsumerBehaviour(ConsumerProducerAgent a) {
        super(a);
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        // Simulation de la consommation
        if (consumerProducerAgent.isStockOfConsumedMerchandise()) {
            // Consuming merchandise
            System.out.println("Consuming " + consumerProducerAgent.getConsumedMerchandise());
            consumerProducerAgent.removeConsumedMerchandise();

            // Afficher les informations ou exécuter d'autres actions nécessaires
            System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a consommé. Satisfaction : " + consumerProducerAgent.getSatisfaction());
        } else {
            // Si le stock est trop faible pour consommer, agir en conséquence
            // décider d'acheter auprès d'un producteur s'il y a de l'argent disponible
        }
    }

}