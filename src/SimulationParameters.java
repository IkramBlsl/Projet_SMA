/**
 * This class represents the simulation parameters of the simulation.
 */
public class SimulationParameters {

    public static final long SIMULATION_DURATION = 120_000;

    public static final int CPA_BASE_MONEY = 10;

    public static final int CPA_PRODUCED_PRODUCT_MAX_CAPACITY = 500;

    public static final long CPA_PRODUCTION_SPEED_BOUND = 7000;
    public static final long CPA_CONSUMPTION_SPEED_BOUND = 7000;
    public static final long CPA_PRICE_VARIATION_PERIOD_BOUND = 7000;

    public static final int CPA_MONEY_SATISFACTION = 10;

    public static final float CPA_DECREASE_SATISFACTION_EXP_FACTOR = (float) 0.05;

    public static final float CPA_DECREASE_PRICE_FACTOR = (float) 0.1;
    public static final float CPA_INCREASE_PRICE_FACTOR = (float) 0.1;

    public static final float CPA_CLONING_FACTOR = (float) 0.5;

}
