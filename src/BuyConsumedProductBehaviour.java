import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.desktop.SystemSleepEvent;
import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * Represents an agent's purchasing behavior.
 * This behavior is triggered when the agent runs out of consumables.
 */
public class BuyConsumedProductBehaviour extends SimpleBehaviour {

    private final ArrayList<Proposition> propositions;
    private int nbExpectedPropositions;

    /**
     * This constructor creates a consumable purchasing behavior for the agent.
     * @param a consumer-producer agent for which this behavior is linked.
     */
    public BuyConsumedProductBehaviour(ConsumerProducerAgent a) {
        super(a);
        this.propositions = new ArrayList<>();
    }

    /**
     * Method executed at the beginning of the behavior.
     * Sends a call for proposals to all producers of the consumed product.
     */
    @Override
    public void onStart() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        // Send CFP to all producers, and start the buying process
        // The expected number of propositions is returned to know how many message we need to gather before choosing best proposition.
        consumerProducerAgent.setCurrentlyBuying(true);
        this.nbExpectedPropositions = consumerProducerAgent.sendCFPToConsumedProductProducers();
    }

    /**
     * Action method: processes received proposals from producers.
     * If a proposal message is received, it is parsed and added to the propositions list.
     */
    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        // Await reception of proposal messages from contacted producers
        ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        if (msg != null) {
            try {
                // Gather message information (sender, content), parse the content into arguments
                AID msgSender = msg.getSender();
                String msgContent = msg.getContent();
                String[] propositionArgs = msgContent.split(" ");

                // Here, we get the product of the proposition, if it's not a consumed product of this agent, it's raising an exception
                Product consumedProduct = Product.parseProduct(propositionArgs[0]);
                assert consumedProduct == consumerProducerAgent.getConsumedProduct();

                // Here we parse and get available quantity, and the price/product of the proposition
                int availableQuantity = Integer.parseInt(propositionArgs[1]);
                float price = Float.parseFloat(propositionArgs[2]);

                // We register this proposition for further evaluation of the best proposal
                propositions.add(new Proposition(msgSender, consumedProduct, availableQuantity, price));
                System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a reçu une proposition de " + msgSender.getLocalName() + ".");
            } catch (Exception e) {
                System.err.println("Agent " + consumerProducerAgent.getLocalName() + " | Error when parsing PROPOSE message. " + e.getMessage());
            }

            // Decreasing number of expected propositions
            nbExpectedPropositions--;
        } else {
            block();
        }
    }

    /**
     * Checks if the behavior is done.
     * If the number of expected propositions is achieved, then this behaviour is done.
     * @return True if the behavior is finished, otherwise false.
     */
    @Override
    public boolean done() {
        if (nbExpectedPropositions == 0) {
            ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

            try {
                // We get the best proposition.
                // Best proposition is the proposition with the best ratio between available quantity and price
                Proposition bestProposition = propositions.getFirst();
                float bestRatio = bestProposition.getAvailableQuantity() / bestProposition.getPrice();
                for (int i = 1; i < propositions.size(); i++) {
                    Proposition proposition = propositions.get(i);
                    float currentRatio = proposition.getAvailableQuantity() / proposition.getPrice();

                    if (currentRatio > bestRatio) {
                        bestProposition = proposition;
                        bestRatio = currentRatio;
                    }
                }

                // For each non-best propositions, we send a message to reject them
                for (Proposition proposition : propositions) {
                    if (proposition != bestProposition) {
                        consumerProducerAgent.sendREJECTToConsumedProductProducer(proposition.getSenderReceiver());
                    }
                }

                // Here, we get the buy quantity.
                // It's simply the quantity that we can afford with the price. It cannot exceed the available quantity.
                int buyQuantity = 0;
                while (buyQuantity < bestProposition.getAvailableQuantity() && ((buyQuantity + 1) * bestProposition.getPrice()) < consumerProducerAgent.getMoney()) {
                    buyQuantity++;
                }

                // If the buy quantity is none, then we reject this proposal.
                // Otherwise, we send an acceptance for this proposal to the producer. We wait then for the confirmation of the transaction.
                if (buyQuantity == 0) {
                    consumerProducerAgent.sendREJECTToConsumedProductProducer(bestProposition.getSenderReceiver());
                    consumerProducerAgent.setCurrentlyBuying(false);
                } else {
                    consumerProducerAgent.sendACCEPTToConsumedProductProducer(bestProposition.getSenderReceiver(), buyQuantity);
                    System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a accepté une proposition de " + bestProposition.getSenderReceiver().getLocalName() + ".");
                    consumerProducerAgent.addBehaviour(new AwaitConfirmBehaviour(consumerProducerAgent, bestProposition, buyQuantity));
                }
            } catch (NoSuchElementException e) {
                consumerProducerAgent.setCurrentlyBuying(false);
            }

            return true;
        }
        return false;
    }

    /**
     * Represents an agent's awaiting the confirmation of the transaction behavior.
     * This behavior is triggered when the agent accept a proposal.
     */
    private static class AwaitConfirmBehaviour extends SimpleBehaviour {

        private boolean messageReceived;

        private final Proposition proposition;
        private final int buyQuantity;

        /**
         * This constructor creates an awaiting confirmation of transaction behavior for the agent.
         * @param a consumer-producer agent for which this behavior is linked.
         * @param proposition The proposition for which we are waiting the confirmation from the producer
         * @param buyQuantity The expected buy quantity of the product in the transaction.
         */
        public AwaitConfirmBehaviour(ConsumerProducerAgent a, Proposition proposition, int buyQuantity) {
            super(a);
            this.proposition = proposition;
            this.buyQuantity = buyQuantity;
            this.messageReceived = false;
        }

        /**
         * This function awaits a confirmation message from the producer.
         * When we receive the confirmation message from the producer, we then close the transaction.
         */
        @Override
        public void action() {
            ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchSender(proposition.getSenderReceiver())));
            if (msg != null) {
                // Adding buyed quantity of the consumed product to the agent, and removing the money use for it.
                consumerProducerAgent.buyConsumedProducts(buyQuantity, proposition.getPrice());
                messageReceived = true;
                consumerProducerAgent.setCurrentlyBuying(false);
                System.out.println("Agent " + consumerProducerAgent.getLocalName() + " a terminé la transaction avec " + proposition.getSenderReceiver().getLocalName() + ".");
            } else {
                block();
            }
        }

        /**
         * Checks if the behavior is done.
         * When we received the confirmation message, behavior is done.
         * @return True if the behavior is finished, otherwise false.
         */
        @Override
        public boolean done() {
            return messageReceived;
        }

    }

}
