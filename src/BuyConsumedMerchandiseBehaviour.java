import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class BuyConsumedMerchandiseBehaviour extends SimpleBehaviour {

    private long startTime;
    private final long timeout;  // Timeout in milliseconds

    private final ArrayList<Proposition> propositions;

    public BuyConsumedMerchandiseBehaviour(ConsumerProducerAgent a, long timeout) {
        super(a);
        this.timeout = timeout;
        this.propositions = new ArrayList<>();
    }

    @Override
    public void onStart() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        // Send CFP to all producers
        consumerProducerAgent.sendCFPToConsumedMerchandiseProducers();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        ACLMessage msg = consumerProducerAgent.receive();
        if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
            AID msgSender = msg.getSender();
            String msgContent = msg.getContent();
            String[] propositionArgs = msgContent.split(" ");
            try {
                Merchandise consumedMerchandise = Merchandise.parseMerchandise(propositionArgs[0]);
                assert consumedMerchandise == consumerProducerAgent.getConsumedMerchandise();

                int quantity = Integer.parseInt(propositionArgs[1]);
                float price = Float.parseFloat(propositionArgs[1]);

                propositions.add(new Proposition(msgSender, consumedMerchandise, quantity, price));
            } catch (Exception e) {
                throw new RuntimeException("Error when parsing PROPOSE message");
                // TODO : Better deal with this
            }
        }
    }

    @Override
    public boolean done() {
        if (System.currentTimeMillis() - startTime >= timeout) {
            ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

            try {
                Proposition bestProposition = propositions.getFirst();
                float bestRatio = bestProposition.getQuantity() / bestProposition.getPrice();

                for (int i = 1; i < propositions.size(); i++) {
                    Proposition proposition = propositions.get(i);
                    float currentRatio = proposition.getQuantity() / proposition.getPrice();

                    if (currentRatio > bestRatio) {
                        bestProposition = proposition;
                        bestRatio = currentRatio;
                    }
                }

                for (Proposition proposition : propositions) {
                    if (proposition != bestProposition) {
                        consumerProducerAgent.sendREJECTToConsumedMerchandiseProducer(proposition.getSender());
                    }
                }

                int buyQuantity = 0;
                while (buyQuantity < bestProposition.getQuantity() && ((buyQuantity + 1) * bestProposition.getPrice()) < consumerProducerAgent.getMoney()) {
                    buyQuantity++;
                }

                if (buyQuantity == 0) {
                    consumerProducerAgent.sendREJECTToConsumedMerchandiseProducer(bestProposition.getSender());
                } else {
                    consumerProducerAgent.sendACCEPTToConsumedMerchandiseProducer(bestProposition.getSender(), buyQuantity);
                    block();
                    ACLMessage msg = consumerProducerAgent.receive();
                    if (msg != null && msg.getPerformative() == ACLMessage.CONFIRM) {
                        consumerProducerAgent.buyConsumedMerchandises(buyQuantity, bestProposition.getPrice());
                    }
                }
            } catch (NoSuchElementException e) {
                // TODO : Deal with this exception
                System.out.println("No received propositions.");
            }

            return true;
        }
        return false;
    }

}
