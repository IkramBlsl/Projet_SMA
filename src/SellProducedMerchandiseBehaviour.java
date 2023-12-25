import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Represents the behavior responsible for selling produced merchandise.
 * It listens for Call for Proposals (CFP) messages from consumers and responds with propositions.
 * Handles the acceptance or rejection of propositions and manages the sale accordingly.
 */
public class SellProducedMerchandiseBehaviour extends CyclicBehaviour {

    private Proposition awaitingProposition;

    public SellProducedMerchandiseBehaviour(ConsumerProducerAgent a) {
        super(a);
        awaitingProposition = null;
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (awaitingProposition == null) {
            // Receive CFP messages from consumers
            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
            if (msg != null) {
                AID msgSender = msg.getSender();
                String msgContent = msg.getContent();
                String[] msgArgs = msgContent.split(" ");

                try {
                    Merchandise producedMerchandise = Merchandise.parseMerchandise(msgArgs[0]);
                    assert producedMerchandise == consumerProducerAgent.getProducedMerchandise();

                    // Create and send a proposition message to the consumer
                    Proposition proposition = new Proposition(msgSender, producedMerchandise, consumerProducerAgent.getProducedMerchandiseStock(), consumerProducerAgent.getProducedMerchandisePrice());
                    awaitingProposition = proposition;
                    ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                    propositionMessage.addReceiver(msgSender);
                    propositionMessage.setContent(proposition.getProduct().getValue() + " " + proposition.getQuantity() + " " + proposition.getPrice());
                    consumerProducerAgent.send(propositionMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Error when parsing CFP message : " + e.getMessage());
                    // TODO : Better deal with this
                }
            } else {
                block();
            }
        } else {
            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.and(MessageTemplate.MatchReceiver(awaitingProposition.getSender().getResolversArray()), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
            if (msg != null) {
                AID msgSender = msg.getSender();

                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    String msgContent = msg.getContent();
                    String[] msgArgs = msgContent.split(" ");
                    try {
                        // Parse the quantity from the ACCEPT_PROPOSAL message
                        int quantity = Integer.parseInt(msgArgs[0]);

                        consumerProducerAgent.sellProducedMerchandises(quantity);
                        awaitingProposition = null;

                        ACLMessage confirmMessage = new ACLMessage(ACLMessage.CONFIRM);
                        confirmMessage.addReceiver(msgSender);
                        consumerProducerAgent.send(confirmMessage);
                    } catch (Exception e) {
                        throw new RuntimeException("Error when parsing ACCEPT message : " + e.getMessage());
                        // TODO : Better deal with this
                    }
                }

                if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    awaitingProposition = null;
                }
            } else {
                block();
            }
        }
    }

}
