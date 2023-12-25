import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * JADE agent representing the simulation.
 * Responsible for creating Producer/Consumer agents, and setting simulation parameters.
 */
public class SimulationAgent extends Agent {

    /**
     * This function is responsible for launching the agent representing the simulation.
     */
    protected void setup() {
        // Collect and set the simulation duration parameter. If not indicated, set default simulation duration.
        long simulationDuration;
        Object[] args = getArguments();
        try {
            simulationDuration = Long.parseLong(args[0].toString());
        } catch (Exception ignored) {
            simulationDuration = SimulationParameters.SIMULATION_DURATION;
        }

        // Start simulation.
        startSimulation();

        // Create a waker behavior that terminates the simulation when the simulation time is reached.
        addBehaviour(new WakerBehaviour(this, simulationDuration) {
            @Override
            protected void onWake() {
                // Stop the simulation agent.
                myAgent.doDelete();
            }
        });
    }

    /**
     * This function is responsible for stopping all agents when this agent stops.
     */
    protected void takeDown() {
        System.exit(0);
    }

    /**
     * This function is responsible for launching the simulation.
     * In other words, it launches all Producer/Consumer agents defined in the scenario.
     */
    private void startSimulation() {
        // BASE SIMULATION
        // Creating 4 agents (2 consuming PRODUCT1 & producing PRODUCT 2, 2 consuming PRODUCT2 & producing PRODUCT1)
        ContainerController cc = getContainerController();
        try {
            AgentController cp1 = cc.createNewAgent("cp1", ConsumerProducerAgent.class.getName(), new String[]{"1", "2"});
            AgentController cp2 = cc.createNewAgent("cp2", ConsumerProducerAgent.class.getName(), new String[]{"2", "1"});
            AgentController cp3 = cc.createNewAgent("cp3", ConsumerProducerAgent.class.getName(), new String[]{"1", "2"});
            AgentController cp4 = cc.createNewAgent("cp4", ConsumerProducerAgent.class.getName(), new String[]{"2", "1"});

            cp1.start();
            cp2.start();
            cp3.start();
            cp4.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }

}
