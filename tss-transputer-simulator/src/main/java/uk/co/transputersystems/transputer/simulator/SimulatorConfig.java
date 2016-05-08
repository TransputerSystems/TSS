package uk.co.transputersystems.transputer.simulator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public class SimulatorConfig {
    public final boolean interactive;
    @Nullable public final File testChecker;
    @Nullable public final File schedChecker;
    @Nullable public final File timerChecker;
    @Nonnull public final List<File> binaries;
    public final boolean printWorkspaceMemory;

    public SimulatorConfig(boolean interactive, @Nullable File testChecker, @Nullable File schedChecker, @Nullable File timerChecker, @Nonnull List<File> binaries, boolean printWorkspaceMemory) {
        this.interactive = interactive;
        this.testChecker = testChecker;
        this.schedChecker = schedChecker;
        this.timerChecker = timerChecker;
        this.binaries = binaries;
        this.printWorkspaceMemory = printWorkspaceMemory;
    }
}
