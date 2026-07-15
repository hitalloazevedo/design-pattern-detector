package samples.strategy;

public final class ConcreteB implements Strategy {

    @Override
    public void execute() {
        System.out.println("Executing ConcreteB strategy");
    }
}
