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
 * JADE agent representing the consumer/producer.
 */
public class ConsumerProducerAgent extends Agent {

    private Product consumedProduct;
    private Product producedProduct;

    private float producedProductPrice = 1;
    private int producedProductStock = 0;

    private long consumptionSpeed;
    private long productionSpeed;
    private long priceVariationPeriod;

    private int consumedProductStock = 0;
    private float money = SimulationParameters.CPA_BASE_MONEY;
    private float satisfaction = 1;
    private boolean currentlyBuying = false;

    private float globalSatisfaction = 0;
    private int nbUpdatedGlobalSatisfaction = 0;

    /**
     * This function is responsible for launching the agent representing the consumer/producer.
     */
    protected void setup() {
        // Here, we get, randomly, the production, consumption speed and the price variation period
        Random random = new Random();
        productionSpeed = random.nextLong(SimulationParameters.CPA_PRODUCTION_SPEED_BOUND);
        consumptionSpeed = random.nextLong(SimulationParameters.CPA_CONSUMPTION_SPEED_BOUND);
        priceVariationPeriod = random.nextLong(SimulationParameters.CPA_PRICE_VARIATION_PERIOD_BOUND);


        Object[] args = getArguments();

        // Here, we get the consumed and produced product (arguments passed to the agent when he is created)
        // If the arguments are not valid (product doesn't exist), or not passed, we delete the agent
        try {
            Product consumedProduct = Product.parseProduct(args[0].toString());
            Product producedProduct = Product.parseProduct(args[1].toString());

            this.consumedProduct = consumedProduct;
            this.producedProduct = producedProduct;
        } catch (Exception e) {
            System.err.println("Terminating agent due to exception. " + e.getMessage());
            doDelete(); // Terminate the agent.
            return;
        }

        // Here, we get the production, consumption speed and the price variation period, if they are passed into the arguments at the creation of the agent (like when we clone an agent)
        try {
            consumptionSpeed = Long.parseLong(args[2].toString());
            productionSpeed = Long.parseLong(args[3].toString());
            priceVariationPeriod = Long.parseLong(args[4].toString());
        } catch (Exception ignored) { }

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
        // Add parallel behaviours to the agent
        addBehaviour(pb);
    }
    /**
     * This method is called when the agent is about to be terminated.
     */
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
    /**
     * Searches for producers of the consumed product in the Directory Facilitator (DF).
     */
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
    /**
     * Updates the global satisfaction metric by adding the current satisfaction.
     */
    private void updateGlobalSatisfaction() {
        globalSatisfaction += satisfaction;
        nbUpdatedGlobalSatisfaction++;
    }
    /**
     * Displays the mean satisfaction of the agent.
     */
    private void showMeanSatisfaction() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        float meanSatisfaction = (globalSatisfaction / nbUpdatedGlobalSatisfaction) * 100;
        System.out.println("Agent " + getLocalName() + " mean satisfaction is " + decimalFormat.format(meanSatisfaction) + "%");
    }
    /**
     * Checks if the agent is currently satisfied based on its financial status and satisfaction level.
     *
     * @return True if the agent's money exceeds a certain threshold (defined in SimulationParameters)
     *         and if the agent's satisfaction level is above a certain threshold (0.5), otherwise False.
     */
    public boolean isSatisfied() {
        return (money > SimulationParameters.CPA_MONEY_SATISFACTION && satisfaction > 0.5);
    }
    /**
     * Clones the current agent.
     */
    public void cloneAgent() {
        ContainerController cc = getContainerController();
        try {
            AgentController cp = cc.createNewAgent(getLocalName() + "_" + UUID.randomUUID(), ConsumerProducerAgent.class.getName(), new String[]{consumedProduct.getValue(), producedProduct.getValue(), String.valueOf(consumptionSpeed), String.valueOf(productionSpeed), String.valueOf(priceVariationPeriod)});

            cp.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Sends Call for Proposal (CFP) messages to producers of the consumed product
     * registered in the DF.
     *
     * @return The number of agents that received the CFP messages.
     */
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
    /**
     * Sends a rejection message to a specific producer agent when he finds a cheaper product elsewhere.
     *
     * @param agent The agent to whom the rejection message is sent.
     */
    public void sendREJECTToConsumedProductProducer(AID agent) {
        ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        msg.addReceiver(agent);
        send(msg);
    }
    /**
     * Sends an acceptance message to a producer agent with a specified quantity.
     *
     * @param agent    The agent to whom the acceptance message is sent.
     * @param quantity The quantity of the product accepted by the agent.
     */
    public void sendACCEPTToConsumedProductProducer(AID agent, int quantity) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(agent);
        msg.setContent(String.valueOf(quantity));
        send(msg);
    }
    /**
     * Checks if there is space available in the produced product stock.
     *
     * @return True if there is space in the produced product stock, otherwise False.
     */
    public boolean isSpaceInProducedStock() {
        return producedProductStock < SimulationParameters.CPA_PRODUCED_PRODUCT_MAX_CAPACITY;
    }
    /**
     * Checks if there are any units left in the stock for the consumed product.
     *
     * @return True if there are units available for the consumed product, otherwise False indicating the stock is empty.
     */
    public boolean isStockOfConsumedProduct() {
        return consumedProductStock > 0;
    }
    /**
     * Adds one unit of the produced product to the stock if there is available space.
     *
     * @throws RuntimeException If there is no space left in the stock.
     */
    public void addOneProducedProduct() {
        if (producedProductStock < SimulationParameters.CPA_PRODUCED_PRODUCT_MAX_CAPACITY) {
            producedProductStock++;
        } else {
            throw new RuntimeException("No Space left in Produced Product stock.");
        }
    }
    /**
     * Removes the consumed products from the stock.
     *
     * @throws RuntimeException If there are no consumed products left.
     */
    public void removeOneConsumedProduct() {
        if (consumedProductStock > 0) {
            consumedProductStock--;
        } else {
            throw new RuntimeException("No Consumed Product left.");
        }
    }
    /**
     * Buys a quantity of products from the producer at a specified price if there is enough money.
     *
     * @param quantity The quantity of products to buy.
     * @param price    The price at which the products are bought.
     * @throws RuntimeException If there is no space left in the produced product stock.
     */
    public void buyConsumedProducts(int quantity, float price) {
        if ((quantity * price) <= money) {
            consumedProductStock += quantity;
            money -= (quantity * price);
        } else {
            throw new RuntimeException("No Space left in Produced Product stock.");
        }
    }
    /**
     * Sells a specified quantity of the produced product at a given price.
     *
     * @param quantity The quantity of products to sell.
     * @param price    The selling price per unit.
     * @throws RuntimeException If the specified quantity exceeds the available stock.
     */
    public void sellProducedProducts(int quantity, float price) {
        if (quantity <= producedProductStock) {
            producedProductStock -= quantity;
            money += (quantity * price);
        } else {
            throw new RuntimeException("No stock in produced product.");
        }
    }
    /**
     * Decreases the agent's satisfaction level based on a specified decay factor.
     * Updates the global satisfaction metric.
     */
    public void decreaseSatisfaction() {
        satisfaction *= (float) Math.exp(-SimulationParameters.CPA_DECREASE_SATISFACTION_EXP_FACTOR);
        if (satisfaction < 0) {
            satisfaction = 0;
        }
        updateGlobalSatisfaction();
    }
    /**
     * Resets the agent's satisfaction level to the maximum value (1).
     * Updates the global satisfaction metric.
     */
    public void resetSatisfaction() {
        satisfaction = 1;
        updateGlobalSatisfaction();
    }
    /**
     * Decreases the price of the produced product by a specified factor.
     * If the price goes below zero, it sets the price to zero.
     */
    public void decreasePrice() {
        producedProductPrice -= SimulationParameters.CPA_DECREASE_PRICE_FACTOR;
        if (producedProductPrice < 0) {
            producedProductPrice = 0;
        }
    }
    /**
     * Increases the price of the produced product by a specified factor
     */
    public void increasePrice() {
        producedProductPrice += SimulationParameters.CPA_INCREASE_PRICE_FACTOR;
    }
    /**
     * Retrieves the consumed product associated with this agent.
     *
     * @return The consumed product.
     */
    public Product getConsumedProduct() {
        return consumedProduct;
    }
    /**
     * Retrieves the produced product associated with this agent.
     *
     * @return The produced product.
     */
    public Product getProducedProduct() {
        return producedProduct;
    }
    /**
     * Retrieves the production speed of this agent.
     *
     * @return The production speed value.
     */
    public long getProductionSpeed() {
        return productionSpeed;
    }
    /**
     * Retrieves the consumption speed of this agent.
     *
     * @return The consumption speed value.
     */
    public long getConsumptionSpeed() {
        return consumptionSpeed;
    }
    /**
     * Gets the satisfaction level of the agent.
     *
     * @return The satisfaction level of the agent.
     */
    public float getSatisfaction() {
        return satisfaction;
    }
    /**
     * Gets the current money amount of the agent.
     *
     * @return The current money amount of the agent.
     */
    public float getMoney() {
        return money;
    }
    /**
     * Gets the price of the produced product.
     *
     * @return The price of the produced product.
     */
    public float getProducedProductPrice() {
        return producedProductPrice;
    }
    /**
     * Retrieves the current stock of the produced product held by this agent.
     *
     * @return The current stock of the produced product.
     */
    public int getProducedProductStock() {
        return producedProductStock;
    }
    /**
     * Retrieves the period for price variation of the produced product.
     *
     * @return The price variation period.
     */
    public long getPriceVariationPeriod() {
        return priceVariationPeriod;
    }
    /**
     * Checks if the agent is currently in a buying state.
     *
     * @return True if the agent is currently buying, otherwise False.
     */
    public boolean isCurrentlyBuying() {
        return currentlyBuying;
    }
    /**
     * Sets the currently buying state of the agent.
     *
     * @param currentlyBuying The new buying state to set for the agent.
     */
    public void setCurrentlyBuying(boolean currentlyBuying) {
        this.currentlyBuying = currentlyBuying;
    }

}
