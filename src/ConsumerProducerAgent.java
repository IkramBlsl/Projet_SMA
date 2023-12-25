import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

/**
 * Agent class responsible for managing the consumption and production of product.
 * Manages the stock, satisfaction level, and communication with other agents for trading.
 */
public class ConsumerProducerAgent extends Agent {

    private static final int PRODUCED_PRODUCT_MAX_STOCK = 500;

    private Product consumedProduct;
    private Product producedProduct;

    private float producedProductPrice = 1;
    private int producedProductStock = 0;

    private long consumptionSpeed;
    private long productionSpeed;
    private long priceVariationPeriod;

    private int consumedProductStock = 0;
    private float money = 10;
    private float satisfaction = 1;
    private boolean currentlyBuying = false;

    private float globalSatisfaction = 0;
    private int nbUpdatedGlobalSatisfaction = 0;

    protected void setup() {
        Random random = new Random();
        productionSpeed = random.nextLong(1000, 5000);
        consumptionSpeed = random.nextLong(1000, 5000);
        priceVariationPeriod = random.nextLong(1000, 5000);

        Object[] args = getArguments();
        try {
            Product consumedProduct = Product.parseProduct(args[0].toString());
            Product producedProduct = Product.parseProduct(args[1].toString());

            this.consumedProduct = consumedProduct;
            this.producedProduct = producedProduct;
        } catch (Exception e) {
            System.out.println("Terminating agent due to exception. " + e.getMessage());
            doDelete(); // Terminate the agent.
            return;
        }

        // Register Agent to DF
        registerToDF();

        ParallelBehaviour pb = new ParallelBehaviour();

        // Consumer Behaviour
        pb.addSubBehaviour(new ConsumerBehaviour(this));
        // Producer Behaviour
        pb.addSubBehaviour(new ProducerBehaviour(this));
        // Selling Behaviour
        pb.addSubBehaviour(new SellProducedProductBehaviour(this));
        // Price variation Behaviour
        pb.addSubBehaviour(new PriceVariationBehaviour(this));

        addBehaviour(pb);
    }

    @Override
    protected void takeDown() {
        showMeanSatisfaction();
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
        sd.setType(producedProduct.getValue());
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    private AID[] searchConsumedProductProducersInDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(consumedProduct.getValue());
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

    private void updateGlobalSatisfaction() {
        globalSatisfaction += satisfaction;
        nbUpdatedGlobalSatisfaction++;
    }

    private void showMeanSatisfaction() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        float meanSatisfaction = (globalSatisfaction / nbUpdatedGlobalSatisfaction) * 100;
        System.out.println("Agent " + getLocalName() + " mean satisfaction is " + decimalFormat.format(meanSatisfaction) + "%");
    }

    public boolean isSatisfied() {
        return (money > 10 && satisfaction > 0.5);
    }

    public void cloneAgent() {
        ContainerController cc = getContainerController();
        try {
            AgentController cp = cc.createNewAgent(getLocalName() + "_" + UUID.randomUUID(), ConsumerProducerAgent.class.getName(), new String[]{consumedProduct.getValue(), producedProduct.getValue()});

            cp.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }

    public int sendCFPToConsumedProductProducers() {
        AID[] agents = searchConsumedProductProducersInDF();
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID agent : agents) {
            msg.addReceiver(agent);
        }
        msg.setContent(consumedProduct.getValue());
        send(msg);

        return agents.length;
    }

    public void sendREJECTToConsumedProductProducer(AID agent) {
        ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        msg.addReceiver(agent);
        send(msg);
    }

    public void sendACCEPTToConsumedProductProducer(AID agent, int quantity) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(agent);
        msg.setContent(String.valueOf(quantity));
        send(msg);
    }

    public boolean isSpaceInProducedStock() {
        return producedProductStock < PRODUCED_PRODUCT_MAX_STOCK;
    }

    public boolean isStockOfConsumedProduct() {
        return consumedProductStock > 0;
    }

    public void addOneProducedProduct() {
        if (producedProductStock < PRODUCED_PRODUCT_MAX_STOCK) {
            producedProductStock++;
        } else {
            throw new RuntimeException("No Space left in Produced Product stock.");
        }
    }

    public void removeOneConsumedProduct() {
        if (consumedProductStock > 0) {
            consumedProductStock--;
        } else {
            throw new RuntimeException("No Consumed Product left.");
        }
    }

    public void buyConsumedProducts(int quantity, float price) {
        if ((quantity * price) <= money) {
            consumedProductStock += quantity;
            money -= (quantity * price);
        } else {
            throw new RuntimeException("No Space left in Produced Product stock.");
        }
    }

    public void sellProducedProducts(int quantity) {
        if (quantity <= producedProductStock) {
            producedProductStock -= quantity;
            money += (quantity * producedProductPrice);
        } else {
            throw new RuntimeException("No stock in produced product.");
        }
    }

    public void decreaseSatisfaction() {
        satisfaction *= (float) Math.exp(-0.05);
        if (satisfaction < 0) {
            satisfaction = 0;
        }
        updateGlobalSatisfaction();
    }

    public void resetSatisfaction() {
        satisfaction = 1;
        updateGlobalSatisfaction();
    }

    public void decreasePrice() {
        producedProductPrice -= 0.1F;
        if (producedProductPrice < 0) {
            producedProductPrice = 0;
        }
    }

    public void increasePrice() {
        producedProductPrice += 0.1F;
    }

    public Product getConsumedProduct() {
        return consumedProduct;
    }

    public Product getProducedProduct() {
        return producedProduct;
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

    public float getProducedProductPrice() {
        return producedProductPrice;
    }

    public int getProducedProductStock() {
        return producedProductStock;
    }

    public long getPriceVariationPeriod() {
        return priceVariationPeriod;
    }

    public boolean isCurrentlyBuying() {
        return currentlyBuying;
    }

    public void setCurrentlyBuying(boolean currentlyBuying) {
        this.currentlyBuying = currentlyBuying;
    }

}
