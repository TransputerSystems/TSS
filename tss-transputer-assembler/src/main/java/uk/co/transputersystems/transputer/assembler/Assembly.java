package uk.co.transputersystems.transputer.assembler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Assembly {
    @Nonnull public final List<Instruction> instructions;
    @Nonnull public final Map<String, Long> labelMap;

    public Assembly(@Nonnull List<Instruction> instructions, @Nonnull Map<String, Long> labelMap) {
        this.instructions = new ArrayList<>(instructions);
        this.labelMap = labelMap;
    }
}
