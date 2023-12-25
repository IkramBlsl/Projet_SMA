import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;
import java.util.UUID;

/**
 * Agent class responsible for managing the consumption and production of merchandise.
 * Manages the stock, satisfaction level, and communication with other agents for trading.
 */
public class ConsumerProducerAgent extends Agent {

    private static final int PRODUCED_MERCHANDISE_MAX_STOCK = 500;

    private Merchandise consumedMerchandise;
    private Merchandise producedMerchandise;

    private float producedMerchandisePrice = 1;
    private int producedMerchandiseStock = 0;

    private long consumptionSpeed;
    private long productionSpeed;
    private long priceVariationPeriod;

    private int consumedMerchandiseStock = 0;
    private float money = 10;
    private float satisfaction = 1;

    protected void setup() {
        Random random = new Random();
        productionSpeed = random.nextLong(2000, 7000);
        consumptionSpeed = random.nextLong(2000, 7000);
        priceVariationPeriod = random.nextLong(2000, 7000);

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

        // Register Agent to DF
        registerToDF();

        // Consumer Behaviour
        addBehaviour(new ConsumerBehaviour(this));

        // Producer Behaviour
        addBehaviour(new ProducerBehaviour(this));

        // Selling Behaviour
        addBehaviour(new SellProducedMerchandiseBehaviour(this));

        // Price variation Behaviour
        addBehaviour(new PriceVariationBehaviour(this));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to register the agent to the Directory Facilitator (DF).
     */

    private void registerToDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(producedMerchandise.getValue());
        sd.setName(getName());
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

    public void cloneAgent() {
        ContainerController cc = getContainerController();
        try {
            AgentController cp = cc.createNewAgent("agent_" + UUID.randomUUID(), ConsumerProducerAgent.class.getName(), new String[]{consumedMerchandise.getValue(), producedMerchandise.getValue()});

            cp.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }

    public int sendCFPToConsumedMerchandiseProducers() {
        AID[] agents = searchConsumedMerchandiseProducersInDF();
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID agent : agents) {
            msg.addReceiver(agent);
        }
        msg.setContent(consumedMerchandise.getValue());
        send(msg);

        return agents.length;
    }

    public void sendREJECTToConsumedMerchandiseProducer(AID agent) {
        ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        msg.addReceiver(agent);
        send(msg);
    }

    public void sendACCEPTToConsumedMerchandiseProducer(AID agent, int quantity) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(agent);
        msg.setContent(String.valueOf(quantity));
        send(msg);
    }

    public boolean isSpaceInProducedStock() {
        return producedMerchandiseStock < PRODUCED_MERCHANDISE_MAX_STOCK;
    }

    public boolean isStockOfConsumedMerchandise() {
        return consumedMerchandiseStock > 0;
    }

    public void addOneProducedMerchandise() {
        if (producedMerchandiseStock < PRODUCED_MERCHANDISE_MAX_STOCK) {
            producedMerchandiseStock++;
        } else {
            throw new RuntimeException("No Space left in Produced Merchandise stock.");
        }
    }

    public void removeOneConsumedMerchandise() {
        if (consumedMerchandiseStock > 0) {
            consumedMerchandiseStock--;
        } else {
            throw new RuntimeException("No Consumed Merchandise left.");
        }
    }

    public void buyConsumedMerchandises(int quantity, float price) {
        if ((quantity * price) <= money) {
            consumedMerchandiseStock += quantity;
            money -= (quantity * price);
        } else {
            throw new RuntimeException("No Space left in Produced Merchandise stock.");
        }
    }

    public void sellProducedMerchandises(int quantity) {
        if (quantity <= producedMerchandiseStock) {
            producedMerchandiseStock -= quantity;
            money += (quantity * producedMerchandisePrice);
        } else {
            throw new RuntimeException("No stock in produced merchandise.");
        }
    }

    public void decreaseSatisfaction() {
        satisfaction *= (float) Math.exp(-0.05);
        if (satisfaction < 0) {
            satisfaction = 0;
        }
    }

    public void setSatisfaction(float newSatisfaction) {
        if (newSatisfaction > 1) {
            satisfaction = 1; // Limiter la satisfaction à 1 (100%)
        } else if (newSatisfaction < 0) {
            satisfaction = 0; // Assurer que la satisfaction ne descend pas en dessous de 0
        } else {
            satisfaction = newSatisfaction; // Définir la satisfaction à la valeur spécifiée
        }
    }

    public void decreasePrice() {
        producedMerchandisePrice *= (float) Math.exp(-0.05);
        if (producedMerchandisePrice < 0) {
            producedMerchandisePrice = 0;
        }
    }

    public void increasePrice() {
        producedMerchandisePrice *= (float) Math.exp(0.05);
    }

    public Merchandise getConsumedMerchandise() {
        return consumedMerchandise;
    }

    public Merchandise getProducedMerchandise() {
        return producedMerchandise;
    }

    public long getProductionSpeed() {
        return productionSpeed;
    }

    public long getConsumptionSpeed() {
        return consumptionSpeed;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

    public float getMoney() {
        return money;
    }

    public float getProducedMerchandisePrice() {
        return producedMerchandisePrice;
    }

    public int getProducedMerchandiseStock() {
        return producedMerchandiseStock;
    }

    public long getPriceVariationPeriod() {
        return priceVariationPeriod;
    }

}
