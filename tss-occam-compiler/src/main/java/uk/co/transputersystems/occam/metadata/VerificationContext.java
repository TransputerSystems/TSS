package uk.co.transputersystems.occam.metadata;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;

import java.util.List;
import java.util.UUID;

/**
 * Created by Edward on 26/02/2016.
 */
public class VerificationContext {

    private List<LibraryInformation> libraries;

    public VerificationContext(List<LibraryInformation> libraries) {
        this.libraries = libraries;
    }

    public List<LibraryInformation> getLibraries() {
        return libraries;
    }

    public boolean isDataTypeKnown(String typeName) {
        for (LibraryInformation library : libraries) {
            if (library.getDataType(typeName) != null) {
                return true;
            }
        }
        return false;
    }

    public void verify(List<ILBlock<UUID, ILOp<UUID>>> ilBlocks) {
        assert (libraries != null);
        assert (libraries.size() > 0);

        for (LibraryInformation library : libraries) {
            library.verify(this);
        }

        for (ILBlock<UUID, ILOp<UUID>> ilBlock : ilBlocks) {
            ilBlock.verify(this);
        }
    }
}
