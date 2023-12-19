import jade.core.Agent;

public class ConsumerProducer extends Agent {

    private Merchandise consumedMerchandise;
    private Merchandise productMerchandise;

    private float money = 0;
    private float satisfaction = 1;
    private float priceProductMerchandise = 1;

    protected void setup() {
        System.out.println("Hello World (not new!...)!");
        System.out.println("My name is " + getAID().getName());
        System.out.println("My local name is " + getLocalName());

        Object[] args = getArguments();
        try {
            Merchandise consumedMerchandise = Merchandise.parseMerchandise(args[0].toString());
            Merchandise productMerchandise = Merchandise.parseMerchandise(args[1].toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Terminating agent due to exception.");
            doDelete(); // Terminate the agent.
            // TODO : Dealing better with this catch block
            return;
        }
    }

    private void cloneAgent() {}

}
