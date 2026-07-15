package samples.validation;

public final class C implements A {

    private final B dependency;

    public C(B dependency) {
        this.dependency = dependency;
    }

    @Override
    public void execute() {
        dependency.run();
        System.out.println("done");
    }
}