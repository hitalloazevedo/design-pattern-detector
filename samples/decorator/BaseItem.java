package samples.decorator;

public class BaseItem implements Decorator {
    @Override
    public void doSomething() {
        System.out.println("Base item doing something.");
    }
}
