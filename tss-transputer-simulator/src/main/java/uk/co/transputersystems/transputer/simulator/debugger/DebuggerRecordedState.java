package uk.co.transputersystems.transputer.simulator.debugger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DebuggerRecordedState {
    public final Set<Integer> memAccessed = new HashSet<>();
    public final Set<Integer> breakpoints = new HashSet<>();
    public final List<Process> processes = new ArrayList<>();

    public DebuggerRecordedState() {
    }
}
