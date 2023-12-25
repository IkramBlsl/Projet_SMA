import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;

/**
 * Represents the behavior responsible for selling produced merchandise.
 * It listens for Call for Proposals (CFP) messages from consumers and responds with propositions.
 * Handles the acceptance or rejection of propositions and manages the sale accordingly.
 */
public class SellProducedMerchandiseBehaviour extends CyclicBehaviour {

    private final HashMap<String, Proposition> awaitingPropositions;

    public SellProducedMerchandiseBehaviour(ConsumerProducerAgent a) {
        super(a);
        awaitingPropositions = new HashMap<>();
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;
        // Receive CFP messages from consumers
        ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
        if (msg != null) {
            AID msgSender = msg.getSender();

            if (msg.getPerformative() == ACLMessage.CFP) {
                String msgContent = msg.getContent();
                String[] msgArgs = msgContent.split(" ");

                try {
                    Merchandise producedMerchandise = Merchandise.parseMerchandise(msgArgs[0]);
                    assert producedMerchandise == consumerProducerAgent.getProducedMerchandise();

                    if (consumerProducerAgent.getProducedMerchandiseStock() > 0) {
                        // Create and send a proposition message to the consumer
                        Proposition proposition = new Proposition(msgSender, producedMerchandise, consumerProducerAgent.getProducedMerchandiseStock(), consumerProducerAgent.getProducedMerchandisePrice());
                        awaitingPropositions.putIfAbsent(msgSender.getName(), proposition);
                        ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                        propositionMessage.addReceiver(msgSender);
                        propositionMessage.setContent(proposition.getProduct().getValue() + " " + proposition.getQuantity() + " " + proposition.getPrice());
                        consumerProducerAgent.send(propositionMessage);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error when parsing CFP message : " + e.getMessage());
                    // TODO : Better deal with this
                }
            }

            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                if (awaitingPropositions.containsKey(msgSender.getName())) {
                    String msgContent = msg.getContent();
                    String[] msgArgs = msgContent.split(" ");
                    try {
                        // Parse the quantity from the ACCEPT_PROPOSAL message
                        int quantity = Integer.parseInt(msgArgs[0]);

                        if (quantity <= consumerProducerAgent.getProducedMerchandiseStock()) {
                            consumerProducerAgent.sellProducedMerchandises(quantity);
                            awaitingPropositions.remove(msgSender.getName());

                            ACLMessage confirmMessage = new ACLMessage(ACLMessage.CONFIRM);
                            confirmMessage.addReceiver(msgSender);
                            consumerProducerAgent.send(confirmMessage);
                        } else {
                            ACLMessage disconfirmMessage = new ACLMessage(ACLMessage.DISCONFIRM);
                            disconfirmMessage.addReceiver(msgSender);
                            consumerProducerAgent.send(disconfirmMessage);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error when parsing ACCEPT message : " + e.getMessage());
                        // TODO : Better deal with this
                    }
                } else {
                    throw new RuntimeException("No awaiting propositions for " + msgSender);
                }
            }

            if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                if (awaitingPropositions.containsKey(msgSender.getName())) {
                    awaitingPropositions.remove(msgSender.getName());
                } else {
                    throw new RuntimeException("No awaiting propositions for " + msgSender);
                }
            }
        } else {
            block();
        }
    }

}
