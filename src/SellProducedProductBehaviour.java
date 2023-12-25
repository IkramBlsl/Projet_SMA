import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Represents the behavior responsible for selling produced product.
 * It listens for Call for Proposals (CFP) messages from consumers and responds with propositions.
 * Handles the acceptance or rejection of propositions and manages the sale accordingly.
 */
public class SellProducedProductBehaviour extends CyclicBehaviour {

    private Proposition awaitingProposition;

    /**
     * This constructor creates a sales behavior for products produced by the agent.
     * @param a Consumer-producer agent for which this behavior is linked.
     */
    public SellProducedProductBehaviour(ConsumerProducerAgent a) {
        super(a);
        awaitingProposition = null;
    }

    /**
     * This function is called cyclically each time a message is received by the agent.
     * If this agent has not made any proposal to a consumer agent, then it waits for a CFP from a consumer agent of the product produced, in order to make a proposal.
     * Otherwise, it waits for an acceptance or refusal of the current proposal from the consumer agent, in order to close the "transaction".
     */
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
                    Product producedProduct = Product.parseProduct(msgArgs[0]);
                    assert producedProduct == consumerProducerAgent.getProducedProduct();

                    // Create and send a proposition message to the consumer
                    Proposition proposition = new Proposition(msgSender, producedProduct, consumerProducerAgent.getProducedProductStock(), consumerProducerAgent.getProducedProductPrice());
                    awaitingProposition = proposition;
                    ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                    propositionMessage.addReceiver(msgSender);
                    propositionMessage.setContent(proposition.getProduct().getValue() + " " + proposition.getAvailableQuantity() + " " + proposition.getPrice());
                    consumerProducerAgent.send(propositionMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Error when parsing CFP message : " + e.getMessage());
                    // TODO : Better deal with this
                }
            } else {
                block();
            }
        } else {
            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.and(MessageTemplate.MatchReceiver(awaitingProposition.getSenderReceiver().getResolversArray()), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
            if (msg != null) {
                AID msgSender = msg.getSender();

                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    String msgContent = msg.getContent();
                    String[] msgArgs = msgContent.split(" ");
                    try {
                        // Parse the quantity from the ACCEPT_PROPOSAL message
                        int quantity = Integer.parseInt(msgArgs[0]);

                        consumerProducerAgent.sellProducedProducts(quantity);
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
