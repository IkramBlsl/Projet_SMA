import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class SimulationAgent extends Agent {

    protected void setup() {
        long simulationDuration = 600000;

        Object[] args = getArguments();
        try {
            simulationDuration = Long.parseLong(args[0].toString());
        } catch (Exception ignored) {}

        startSimulation();
        addBehaviour(new WakerBehaviour(this, simulationDuration) {
            @Override
            protected void handleElapsedTimeout() {
                myAgent.doDelete();
            }
        });
    }

    protected void takeDown() {
        System.exit(0);
    }

    private void startSimulation() {
        ContainerController cc = getContainerController();
        try {
            AgentController cp1 = cc.createNewAgent("cp1", ConsumerProducerAgent.class.getName(), new String[]{"1", "2"});
            AgentController cp2 = cc.createNewAgent("cp2", ConsumerProducerAgent.class.getName(), new String[]{"2", "1"});

            cp1.start();
            cp2.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }

}
