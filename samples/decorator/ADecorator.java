package samples.decorator;

public class ADecorator implements Decorator {
    private final Decorator decorated;

    public ADecorator(Decorator decorated) {
        this.decorated = decorated;
    }

    @Override
    public void doSomething() {
        System.out.println("ADecorator doing something before the decorated item.");
        decorated.doSomething();
        System.out.println("ADecorator doing something after the decorated item.");
    }
    
}
