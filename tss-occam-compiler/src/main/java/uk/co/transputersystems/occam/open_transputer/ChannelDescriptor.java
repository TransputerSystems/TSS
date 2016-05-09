package uk.co.transputersystems.occam.open_transputer;

import uk.co.transputersystems.occam.metadata.Workspace;

/**
 * Created by Edward on 09/05/2016.
 */
public class ChannelDescriptor {
    public final Integer index;
    public final String name;
    public final String typeName;
    public final int offset;
    public final Workspace ownerWS;

    public ChannelDescriptor(Integer index, String name, String typeName, Integer offset, Workspace ownerWS) {
        this.index = index;
        this.name = name;
        this.typeName = typeName;
        this.offset = offset;
        this.ownerWS = ownerWS;
    }
}
