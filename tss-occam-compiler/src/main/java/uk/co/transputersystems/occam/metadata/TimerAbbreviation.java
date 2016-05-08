package uk.co.transputersystems.occam.metadata;

public class TimerAbbreviation extends Abbreviation {
    public TimerAbbreviation(Scope declaringScope, String name, String hideName) {
        super(declaringScope, name, "{TIMER}", hideName);
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (typeName == "{TIMER}");
    }
}
