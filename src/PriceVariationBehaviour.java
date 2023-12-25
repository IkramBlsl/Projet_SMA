import jade.core.behaviours.TickerBehaviour;

public class PriceVariationBehaviour extends TickerBehaviour {

    public PriceVariationBehaviour(ConsumerProducerAgent a) {
        super(a, a.getPriceVariationPeriod());
    }

    @Override
    protected void onTick() {
        ConsumerProducerAgent consumerProducerAgent = (ConsumerProducerAgent) myAgent;

        if (consumerProducerAgent.isSatisfied()) {
            consumerProducerAgent.increasePrice();
        } else {
            consumerProducerAgent.decreasePrice();
        }
    }

}
