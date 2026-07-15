package samples.strategy;

public class ConcreteB implements Strategy {
    @Override
    public void execute() {
        System.out.println("Executing ConcreteB strategy");
    }
}
