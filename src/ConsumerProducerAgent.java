import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class ConsumerProducerAgent extends Agent {

    private Merchandise consumedMerchandise;
    private Merchandise producedMerchandise;

    private float priceProducedMerchandise = 1;
    private int stockProducedMerchandise = 0;
    private int maxStockProducedMerchandise = 500; // TODO

    public float consumptionSpeed;
    private float productionSpeed;

    private int stockConsumedMerchandise = 0;
    private float money = 10;
    private float satisfaction = 1;

    protected void setup() {
        // TODO : Handle production & consumption speed
        productionSpeed = 3;
        consumptionSpeed = 3;

        System.out.println("Hello World (not new!...)!");
        System.out.println("My name is " + getAID().getName());
        System.out.println("My local name is " + getLocalName());

        Object[] args = getArguments();
        try {
            Merchandise consumedMerchandise = Merchandise.parseMerchandise(args[0].toString());
            Merchandise producedMerchandise = Merchandise.parseMerchandise(args[1].toString());

            this.consumedMerchandise = consumedMerchandise;
            this.producedMerchandise = producedMerchandise;
        } catch (Exception e) {
            System.out.println("Terminating agent due to exception.");
            System.err.println(e.getMessage());
            doDelete(); // Terminate the agent.
            // TODO : Dealing better with this catch block
            return;
        }

        System.out.println("Consumed Merchandise is " + consumedMerchandise);
        System.out.println("Produced Merchandise is " + producedMerchandise);

        // Register Agent to DF
        registerToDF();

        // Consumer Behaviour
        addBehaviour(new ConsumerBehaviour(this));

        // Producer Behaviour
        addBehaviour(new ProducerBehaviour(this));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerToDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(producedMerchandise.getValue());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    private AID[] searchConsumedMerchandiseProducersInDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(consumedMerchandise.getValue());
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            AID[] agents = new AID[results.length];
            for (int i = 0; i < results.length; i++) {
                agents[i] = results[i].getName();
            }
            return agents;
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    private void cloneAgent() {}

    public void sendCFPToConsumedMerchandiseProducers() {
        AID[] agents = searchConsumedMerchandiseProducersInDF();
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID agent : agents) {
            msg.addReceiver(agent);
        }
        msg.setContent(consumedMerchandise.getValue());
        send(msg);
    }

    public boolean isSpaceInProducedStock() {
        return stockProducedMerchandise < maxStockProducedMerchandise;
    }

    public boolean isStockOfConsumedMerchandise() {
        return stockConsumedMerchandise > 0;
    }

    public void addOneProducedMerchandise() {
        if (stockProducedMerchandise < maxStockProducedMerchandise) {
            stockProducedMerchandise++;
        } else {
            throw new RuntimeException("No Space left in Produced Merchandise stock.");
        }
    }

    public void removeOneConsumedMerchandise() {
        if (stockConsumedMerchandise > 0) {
            stockConsumedMerchandise--;
        } else {
            throw new RuntimeException("No Consumed Merchandise left.");
        }
    }

    public Merchandise getConsumedMerchandise() {
        return consumedMerchandise;
    }

    public Merchandise getProducedMerchandise() {
        return producedMerchandise;
    }

    public float getProductionSpeed() {
        return productionSpeed;
    }

    public float getConsumptionSpeed() {
        return consumptionSpeed;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

}
