package samples.generic;

public final class C implements A {

    private final A value;

    public C(A value) {
        this.value = value;
    }

    @Override
    public void run() {
        System.out.println("before");
        value.run();
        System.out.println("after");
    }
}