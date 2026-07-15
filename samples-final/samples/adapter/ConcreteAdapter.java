package samples.adapter;

public final class ConcreteAdapter implements Adapter {

    private final Legacy legacy;

    public ConcreteAdapter(Legacy legacy) {
        this.legacy = legacy;
    }

    @Override
    public void adapt() {
        legacy.legacyMethod();
        System.out.println("Adapting to the legacy system.");
    }
}
