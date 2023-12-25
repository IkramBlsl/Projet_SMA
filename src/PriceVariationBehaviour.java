import jade.core.behaviours.TickerBehaviour;

public class PriceVariationBehaviour extends TickerBehaviour {

    public PriceVariationBehaviour(ConsumerProducerAgent a) {
        super(a, a.getPriceVariationPeriod());
    }

    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.getMoney() > 10 && consumerProducerAgent.getSatisfaction() > 0.5) {
            consumerProducerAgent.increasePrice();
        } else {
            consumerProducerAgent.decreasePrice();
        }
    }

}
