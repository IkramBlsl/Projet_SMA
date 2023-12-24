import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SellProducedMerchandiseBehaviour extends CyclicBehaviour {

    public SellProducedMerchandiseBehaviour(ConsumerProducerAgent a) {
        super(a);
    }

    @Override
    public void action() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CFP), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
        if (msg != null) {
            AID msgSender = msg.getSender();
            String msgContent = msg.getContent();
            String[] msgArgs = msgContent.split(" ");

            if (msg.getPerformative() == ACLMessage.CFP) {
                try {
                    Merchandise producedMerchandise = Merchandise.parseMerchandise(msgArgs[0]);
                    assert producedMerchandise == consumerProducerAgent.getProducedMerchandise();

                    ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                    propositionMessage.addReceiver(msgSender);
                    propositionMessage.setContent(producedMerchandise.getValue() + " " + consumerProducerAgent.getStockProducedMerchandise() + " " + consumerProducerAgent.getPriceProducedMerchandise());
                    consumerProducerAgent.send(propositionMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Error when parsing CFP message");
                    // TODO : Better deal with this
                }
            }

            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                // TODO : Better deal with this (because we accept proposal of anyone without veryfing that we propose something, etc...)
                try {
                    int quantity = Integer.parseInt(msgArgs[0]);

                    consumerProducerAgent.sellProducedMerchandises(quantity);

                    ACLMessage confirmMessage = new ACLMessage(ACLMessage.CONFIRM);
                    confirmMessage.addReceiver(msgSender);
                    consumerProducerAgent.send(confirmMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Error when parsing ACCEPT message : " + e.getMessage());
                    // TODO : Better deal with this
                }
            }
        } else {
            block();
        }
    }

}
