package uk.co.transputersystems.transputer.assembler;

import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AssemblerRegexTest {

    @Test
    public void testInputCommentPatternMatches() {
        Map<String, String> tests = new HashMap<>();

        tests.put("-- a simple comment", "a simple comment");
        tests.put("--a simple comment", "a simple comment");
        tests.put(" -- a simple comment", "a simple comment");
        tests.put("\t-- a simple comment", "a simple comment");
        tests.put("   -- a simple comment", "a simple comment");
        tests.put(" -- a compléx\tcomment", "a compléx\tcomment");

        for (Map.Entry<String, String> test : tests.entrySet()) {
            Matcher m = Assembler.inputComment.matcher(test.getKey());
            assertTrue(m.matches());
            assertEquals(test.getValue(), m.group("comment"));
        }
    }

    @Test
    public void testInputCommentPatternNonMatches() {
        assertFalse(Assembler.inputComment.matcher("L1: -- a simple comment").matches());
        assertFalse(Assembler.inputComment.matcher("L1: j L4 -- a simple comment").matches());
        assertFalse(Assembler.inputComment.matcher("ajw -1 -- a simple comment").matches());
    }

    @Test
    public void testInstructionPatternMatchesLabel() {
        checkInstructionPatternMatches("L5:", "L5", null, null, null, null, null);
    }

    @Test
    public void testInstructionPatternMatchesLabelWithTrailingSpaces() {
        checkInstructionPatternMatches("L5:\t ", "L5", null, null, null, null, null);
    }

    @Test
    public void testInstructionPatternMatchesSectionLabelWithComment() {
        checkInstructionPatternMatches("L0: -- section label", "L0", null, null, null, null, "section label");
    }

    @Test
    public void testInstructionPatternMatchesSpaceIndentedOpcodeWithLabelOperand() {
        checkInstructionPatternMatches("            call        L0", null, "call        L0", "call", null, "L0", null);
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelOperand() {
        checkInstructionPatternMatches("\tcall\tL0", null, "call\tL0", "call", null, "L0", null);
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelOperandWithCommentWithTrailingSpaces() {
        checkInstructionPatternMatches("\tcall\tL0 -- a comment ", null, "call\tL0", "call", null, "L0", "a comment");
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelArithmeticOperand() {
        checkInstructionPatternMatches("\tcall\tL1 - L0", null, "call\tL1 - L0", "call", null, "L1 - L0", null);
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelOperandWithTrailingSpaces() {
        checkInstructionPatternMatches("\tcall\tL0  \t", null, "call\tL0", "call", null, "L0", null);
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelArithmeticOperandWithTrailingSpaces() {
        checkInstructionPatternMatches("\tcall\tL1 - L0  \t", null, "call\tL1 - L0", "call", null, "L1 - L0", null);
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithLabelArithmeticOperandWithCommentWithTrailingSpaces() {
        checkInstructionPatternMatches("\tcall\tL1 - L0  -- a comment\t", null, "call\tL1 - L0", "call", null, "L1 - L0", "a comment");
    }

    @Test
    public void testInstructionPatternMatchesTabIndentedOpcodeWithNegativeLabelOperand() {
        checkInstructionPatternMatches("\tcall\t-L4\t", null, "call\t-L4", "call", null, "-L4", null);
    }

    @Test
    public void testInstructionPatternMatchesSpaceIndentedOpcodeWithConstantOperand() {
        checkInstructionPatternMatches("\tldc\t10", null, "ldc\t10", "ldc", "10", null, null);
    }

    @Test
    public void testInstructionPatternMatchesSpaceIndentedOpcodeWithNegativeConstantOperand() {
        checkInstructionPatternMatches("\tldc\t-99", null, "ldc\t-99", "ldc", "-99", null, null);
    }

    @Test
    public void testInstructionPatternMatchesIndentedLabelWithComment() {
        checkInstructionPatternMatches("\tL1: -- a comment ", "L1", null, null, null, null, "a comment");
    }

    @Test
    public void testInstructionPatternMatchesUnindentedOpcodeWithConstantOperand() {
        checkInstructionPatternMatches("ajw 5", null, "ajw 5", "ajw", "5", null, null);
    }

    @Test
    public void testInstructionPatternMatchesUnindentedOpcodeWithLabelOperand() {
        checkInstructionPatternMatches("j ALABEL", null, "j ALABEL", "j", null, "ALABEL", null);
    }

    @Test
    public void testInstructionPatternMatchesLongLabel() {
        checkInstructionPatternMatches("GlobalVariable_global1:", "GlobalVariable_global1", null, null, null, null, null);
    }

    @Test
    public void testInstructionPatternMatchesIndirectOpWithTrailingSpaces() {
        checkInstructionPatternMatches("startp      ", null, "startp", "startp", null, null, null);
    }

    @Test
    public void testInstructionPatternMatchesOpWithComplexLabelArithmeticOperand() {
        checkInstructionPatternMatches("ldc         L1-L~StartP_8", null, "ldc         L1-L~StartP_8", "ldc", null, "L1-L~StartP_8", null);
    }

    @Test
    public void testInstructionPatternMatchesOpWithDollarLabelOperand() {
        checkInstructionPatternMatches("ldc         $", null, "ldc         $", "ldc", null, "$", null);
    }

    @Test
    public void testInstructionPatternMatchesOpWithComplexDollarLabelOperand() {
        checkInstructionPatternMatches("ldc         $ - MYLABEL", null, "ldc         $ - MYLABEL", "ldc", null, "$ - MYLABEL", null);
    }

    private void checkInstructionPatternMatches(String line, String expectedLabel, String expectedCode, String expectedOpcode, String expectedConstantOperand, String expectedLabelOperand, String expectedComment) {
        Matcher m = Assembler.instructionPattern.matcher(line);
        assertTrue(m.matches());
        assertEquals(expectedLabel, m.group("label"));
        assertEquals(expectedCode, m.group("code"));
        assertEquals(expectedOpcode, m.group("opcode"));
        assertEquals(expectedConstantOperand, m.group("constantoperand"));
        assertEquals(expectedLabelOperand, m.group("labeloperand"));
        assertEquals(expectedComment, m.group("comment"));

    }
    
    @Test
    public void testDirectivePatternMatchesData() {
        checkDirectivePatternMatches("#data       4", null, "#data       4", null);
    }

    @Test
    public void testDirectivePatternMatchesDataWithSeveralDigits() {
        checkDirectivePatternMatches("#data       4", null, "#data       4", null);
    }

    @Test
    public void testDirectivePatternMatchesIndentedDataWithSeveralDigits() {
        checkDirectivePatternMatches("\t#data       4", null, "#data       4", null);
    }

    @Test
    public void testDirectivePatternMatchesChan() {
        checkDirectivePatternMatches("#chan       chanName0", null, "#chan       chanName0", null);
    }

    @Test
    public void testDirectivePatternMatchesChanWithCommentAndLabel() {
        checkDirectivePatternMatches("L0: #chan       chanName0\t-- a comment", "L0", "#chan       chanName0", "a comment");
    }

    private void checkDirectivePatternMatches(String line, String expectedLabel, String expectedDirective, String expectedComment) {
        Matcher m = Assembler.directivePattern.matcher(line);
        assertTrue(m.matches());
        assertEquals(expectedLabel, m.group("label"));
        assertEquals(expectedDirective, m.group("directive"));
        assertEquals(expectedComment, m.group("comment"));

    }

    @Test
    public void testDollarPatternMatches$C() {
        Matcher m = Assembler.dollarPattern.matcher("$C");
        assertTrue(m.find());
        assertEquals("C", m.group("current"));
        assertEquals(null, m.group("number"));
    }

    @Test
    public void testDollarPatternMatchesComplex$C() {
        Matcher m = Assembler.dollarPattern.matcher("LABEL1-$C-LABEL2");
        assertTrue(m.find());
        assertEquals("C", m.group("current"));
        assertEquals(null, m.group("number"));
    }

    @Test
    public void testDollarPatternMatches$WithOneDigit() {
        Matcher m = Assembler.dollarPattern.matcher("$0");
        assertTrue(m.find());
        assertEquals(null, m.group("current"));
        assertEquals("0", m.group("number"));
    }

    @Test
    public void testDollarPatternMatches$WithTwoDigits() {
        Matcher m = Assembler.dollarPattern.matcher("$67");
        assertTrue(m.find());
        assertEquals(null, m.group("current"));
        assertEquals("67", m.group("number"));
    }

    @Test
    public void testDollarPatternMatchesComplex$WithTwoDigits() {
        Matcher m = Assembler.dollarPattern.matcher("LABEL0-$19+5");
        assertTrue(m.find());
        assertEquals(null, m.group("current"));
        assertEquals("19", m.group("number"));
    }
}
