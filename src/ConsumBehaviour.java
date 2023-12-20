import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class ConsumBehaviour extends Behaviour {

    private ConsumerProducer agent;
    private int stockConsumedMerchandise; // Stock initial de la marchandise consommée
    private int quantiteNecessairePourConsommer; // Quantité minimale pour consommer

    public ConsumBehaviour(ConsumerProducer agent, int initialStock, int minQuantityToConsume) {
        super(agent);
        this.agent = agent;
        this.stockConsumedMerchandise = initialStock;
        this.quantiteNecessairePourConsommer = minQuantityToConsume;
    }

    @Override
    public void action() {
        // Simulation de la consommation
        if (stockConsumedMerchandise >= quantiteNecessairePourConsommer) {
            // Consommer la marchandise et mettre à jour le stock
            stockConsumedMerchandise -= quantiteNecessairePourConsommer;

            // Calculer la satisfaction en fonction du stock
            double satisfaction = (double) stockConsumedMerchandise / quantiteNecessairePourConsommer;

            // Mettre à jour d'autres paramètres de l'agent en fonction de la satisfaction, etc.

            // Afficher les informations ou exécuter d'autres actions nécessaires
            System.out.println("Agent " + agent.getLocalName() + " a consommé. Satisfaction : " + satisfaction);
        } else {
            // Si le stock est trop faible pour consommer, agir en conséquence
            // décider d'acheter auprès d'un producteur s'il y a de l'argent disponible
        }
    }

    @Override
    public boolean done() {
        //touche sur le clavier ou quitter après la fin de la transaction...
        return false;
    }
}