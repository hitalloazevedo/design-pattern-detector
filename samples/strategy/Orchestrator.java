package samples.strategy;

public class Orchestrator {
    private Strategy strategy;
    private static Strategy globalStrategy;

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy() {
        strategy.execute();
    }
}
