package samples.strategy;

public final class ConcreteA implements Strategy {

    @Override
    public void execute() {
        System.out.println("Executing ConcreteA strategy");
    }
}
