package uk.co.transputersystems.occam.metadata;

public class Timer extends NamedOperand {

    public Timer(String name) {
        super(name, "{TIMER}");
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (typeName == "{TIMER}");
    }
}
