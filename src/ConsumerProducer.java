import jade.core.Agent;

public class ConsumerProducer extends Agent {

    protected void setup() {
        System.out.println("Hello World (not new!...)!");
        System.out.println("My name is " + getAID().getName());
        System.out.println("My local name is " + getLocalName());
    }

}
