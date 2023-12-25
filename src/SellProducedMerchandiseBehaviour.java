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

        ACLMessage msg = consumerProducerAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
        if (msg != null) {
            AID msgSender = msg.getSender();
            String msgContent = msg.getContent();
            String[] msgArgs = msgContent.split(" ");

            try {
                Merchandise producedMerchandise = Merchandise.parseMerchandise(msgArgs[0]);
                assert producedMerchandise == consumerProducerAgent.getProducedMerchandise();

                if (consumerProducerAgent.getProducedMerchandiseStock() > 0) {
                    ACLMessage propositionMessage = new ACLMessage(ACLMessage.PROPOSE);
                    propositionMessage.addReceiver(msgSender);
                    propositionMessage.setContent(producedMerchandise.getValue() + " " + consumerProducerAgent.getProducedMerchandiseStock() + " " + consumerProducerAgent.getProducedMerchandisePrice());
                    consumerProducerAgent.send(propositionMessage);

                    msg = consumerProducerAgent.blockingReceive(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)));
                    if (msg != null) {
                        // TODO : Better deal with this
                        msgSender = msg.getSender();
                        if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            msgContent = msg.getContent();
                            msgArgs = msgContent.split(" ");
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
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error when parsing CFP message : " + e.getMessage());
                // TODO : Better deal with this
            }
        } else {
            block();
        }
    }

}
