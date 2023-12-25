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
            // If there is no awaiting propositions (no proposal made to any consumer agent), we wait for receiving a CFP message from a consumer agent
            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
            if (msg != null) {
                try {
                    // Here, we get all message information (sender, content)
                    AID msgSender = msg.getSender();
                    String msgContent = msg.getContent();

                    // We now parse the arguments of the CFP message
                    String[] msgArgs = msgContent.split(" ");

                    // Here, we have one argument (the product). We parse the value of the product to a Product enum.
                    // If we can't parse, or the product asked by the consumer agent is not produced by this agent, we raise a RuntimeException
                    Product producedProduct = Product.parseProduct(msgArgs[0]);
                    assert producedProduct == consumerProducerAgent.getProducedProduct();

                    // Create and send a proposition message to the consumer
                    Proposition proposition = new Proposition(msgSender, producedProduct, consumerProducerAgent.getProducedProductStock(), consumerProducerAgent.getProducedProductPrice());
                    awaitingProposition = proposition;
                    ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                    propositionMessage.addReceiver(msgSender);
                    propositionMessage.setContent(proposition.getProduct().getValue() + " " + proposition.getAvailableQuantity() + " " + proposition.getPrice());
                    consumerProducerAgent.send(propositionMessage);
                    System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a envoyé une proposition à " + msgSender.getLocalName() + ".");
                } catch (Exception e) {
                    System.err.println("Agent " + consumerProducerAgent.getLocalName() + " | Error when parsing CFP message : " + e.getMessage());
                }
            } else {
                block();
            }
        } else {
            // If there is awaiting propositions (proposal made to any consumer agent), we wait for receiving a ACCEPT or REJECT PROPOSAL message from the consumer agent for whom we made the proposition
            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.and(MessageTemplate.MatchSender(awaitingProposition.getSenderReceiver()), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
            if (msg != null) {
                // Here, we get the sender of the message
                AID msgSender = msg.getSender();

                // If the message is an acceptation of the proposal
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    try {
                        // Here, we get message content, and we parse the arguments of the ACCEPT PROPOSAL message
                        String msgContent = msg.getContent();
                        String[] msgArgs = msgContent.split(" ");

                        // Gathering the buy quantity from the ACCEPT_PROPOSAL message
                        int buyQuantity = Integer.parseInt(msgArgs[0]);

                        // We sell the product (updating money and stock of produced products)
                        consumerProducerAgent.sellProducedProducts(buyQuantity, awaitingProposition.getPrice());

                        // We send a confirmation message, the transaction has been made
                        ACLMessage confirmMessage = new ACLMessage(ACLMessage.CONFIRM);
                        confirmMessage.addReceiver(msgSender);
                        consumerProducerAgent.send(confirmMessage);
                        System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a confimé la transaction avec " + msgSender.getLocalName() + ".");
                    } catch (Exception e) {
                        System.err.println("Agent " + consumerProducerAgent.getLocalName() + " | Error when parsing ACCEPT message : " + e.getMessage());
                    }
                }

                // Then we have now no awaiting propositions (as the proposition has been proceed)
                awaitingProposition = null;
            } else {
                block();
            }
        }
    }

}
