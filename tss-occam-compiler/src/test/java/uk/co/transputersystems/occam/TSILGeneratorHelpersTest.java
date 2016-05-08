package uk.co.transputersystems.occam;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TSILGeneratorHelpersTest {

    @Test
    public void testParseCharacterLiteral() {
        // Make sure the standard escape sequences return the correct ASCII values
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*c"), (int)'\r');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*C"), (int)'\r');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*n"), (int)'\n');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*N"), (int)'\n');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*t"), (int)'\t');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*T"), (int)'\t');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*s"), (int)' ');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*S"), (int)' ');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*'"), (int)'\'');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*\""), (int)'"');
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("**"), (int)'*');

        // Test some hex numbers
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*#00"), 0);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*#01"), 1);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*#6C"), 108);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("*#7F"), 127);

        // Test some normal characters
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("a"), 97);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("A"), 65);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral("}"), 125);
        Assert.assertEquals(TSILGeneratorHelpers.parseCharacterLiteral(" "), 32);

        // Test for throwing exceptions on some illegal input
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("***")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*nn")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*#")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*#123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("*#1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("'*#01'")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TSILGeneratorHelpers.parseCharacterLiteral("\n")).isInstanceOf(IllegalArgumentException.class);
    }
}
