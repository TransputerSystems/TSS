package uk.co.transputersystems.occam.il;

import uk.co.transputersystems.occam.ILOpFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class ILOpTest {

    @Test
    public void testFormatOp() {
        // Test a normal IL Op without a calculated offset
        Assert.assertEquals(
                "00000000-0000-0000-0000-000000000000 : Branch          (10000000-0000-0000-0000-000000000000)                           // This is a comment",
                ILOpFormatter.formatOp(
                        new Branch<>(UUID.fromString("00000000-0000-0000-0000-000000000000"), UUID.fromString("10000000-0000-0000-0000-000000000000"), "This is a comment"),
                        new ILBlock<>()
                )
        );

        // Test an IL Op with a set of arguments that exceeds the padding for the comment without a calculated offset
        Assert.assertEquals("00000000-0000-0000-0000-000000000000 : InitProcesses   (10, 10000000-0000-0000-0000-000000000000, [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]) // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                ILOpFormatter.formatOp(
                        new InitProcesses<>(UUID.fromString("00000000-0000-0000-0000-000000000000"), 10, UUID.fromString("10000000-0000-0000-0000-000000000000"), Arrays.asList(1,2,3,4,5,6,7,8,9,10), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                        new ILBlock<>()
                )
        );

        // Test an IL Op with a name that exceeds the expected padding width without a calculated offset
        Assert.assertEquals("00000000-0000-0000-0000-000000000000 : CompareGreaterThan ()                                                            // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                ILOpFormatter.formatOp(
                        new CompareGreaterThan<>(UUID.fromString("00000000-0000-0000-0000-000000000000"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                        new ILBlock<>()
                )
        );

        // Test a normal IL Op with an integer identifier
        Assert.assertEquals("15 : Branch                                            (20)                                                             // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                ILOpFormatter.formatOp(
                        new Branch<>(15, 20, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                        new ILBlock<>()
                )
        );

    }

}

