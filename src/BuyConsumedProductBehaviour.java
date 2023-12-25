import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * Class representing the behavior for purchasing consumed product from producers.
 * Compares producers' propositions and accepts the best offer for purchase.
 */
public class BuyConsumedProductBehaviour extends SimpleBehaviour {

    private final ArrayList<Proposition> propositions;
    private int nbExpectedPropositions;

    /**
     * Constructor for BuyConsumedProductBehaviour class.
     * @param a Consumer-producer agent
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

        consumerProducerAgent.setCurrentlyBuying(true);
        // Send CFP to all producers
        this.nbExpectedPropositions = consumerProducerAgent.sendCFPToConsumedProductProducers();
    }

    /**
     * Action method: processes received messages from producers.
     * If a proposal message is received, it is parsed and added to the propositions list.
     */
    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        if (msg != null) {
            AID msgSender = msg.getSender();
            String msgContent = msg.getContent();
            String[] propositionArgs = msgContent.split(" ");
            try {
                Product consumedProduct = Product.parseProduct(propositionArgs[0]);
                assert consumedProduct == consumerProducerAgent.getConsumedProduct();

                int quantity = Integer.parseInt(propositionArgs[1]);
                float price = Float.parseFloat(propositionArgs[2]);

                propositions.add(new Proposition(msgSender, consumedProduct, quantity, price));
            } catch (Exception e) {
                throw new RuntimeException("Error when parsing PROPOSE message");
                // TODO : Better deal with this
            }
            nbExpectedPropositions--;
        } else {
            block();
        }
    }

    /**
     * Checks if the behavior is done.
     * If the timeout is reached, compares received propositions and proceeds with the purchase.
     * @return True if the behavior is finished, otherwise false
     */
    @Override
    public boolean done() {
        if (nbExpectedPropositions == 0) {
            ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

            try {
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

                for (Proposition proposition : propositions) {
                    if (proposition != bestProposition) {
                        consumerProducerAgent.sendREJECTToConsumedProductProducer(proposition.getSenderReceiver());
                    }
                }

                int buyQuantity = 0;
                while (buyQuantity < bestProposition.getAvailableQuantity() && ((buyQuantity + 1) * bestProposition.getPrice()) < consumerProducerAgent.getMoney()) {
                    buyQuantity++;
                }

                if (buyQuantity == 0) {
                    consumerProducerAgent.sendREJECTToConsumedProductProducer(bestProposition.getSenderReceiver());
                    consumerProducerAgent.setCurrentlyBuying(false);
                } else {
                    consumerProducerAgent.sendACCEPTToConsumedProductProducer(bestProposition.getSenderReceiver(), buyQuantity);
                    consumerProducerAgent.addBehaviour(new AwaitConfirmBehaviour(consumerProducerAgent, bestProposition, buyQuantity));
                }
            } catch (NoSuchElementException e) {
                consumerProducerAgent.setCurrentlyBuying(false);
            }

            return true;
        }
        return false;
    }

    private static class AwaitConfirmBehaviour extends SimpleBehaviour {

        private boolean messageReceived;

        private final Proposition proposition;
        private final int buyQuantity;

        public AwaitConfirmBehaviour(ConsumerProducerAgent a, Proposition proposition, int buyQuantity) {
            super(a);
            this.proposition = proposition;
            this.buyQuantity = buyQuantity;
            this.messageReceived = false;
        }

        @Override
        public void action() {
            ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

            ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)));
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    consumerProducerAgent.buyConsumedProducts(buyQuantity, proposition.getPrice());
                }
                messageReceived = true;
                consumerProducerAgent.setCurrentlyBuying(false);
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return messageReceived;
        }

    }

}
