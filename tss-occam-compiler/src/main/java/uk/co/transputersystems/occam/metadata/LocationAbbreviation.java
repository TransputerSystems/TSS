package uk.co.transputersystems.occam.metadata;

/**
 * Created by Ed on 09/04/2016.
 */
public class LocationAbbreviation extends Abbreviation {

    private String locationExpression;

    public LocationAbbreviation(Scope declaringScope, String name, String typeName, String locationExpression) {
        super(declaringScope, name, typeName, "[LOCATION]");

        this.locationExpression = locationExpression;
    }

    public String getLocationExpression() {
        return locationExpression;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        //TODO: Location abbreviation verification
    }
}
