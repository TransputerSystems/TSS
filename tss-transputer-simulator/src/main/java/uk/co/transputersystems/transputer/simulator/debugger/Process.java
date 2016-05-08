package uk.co.transputersystems.transputer.simulator.debugger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Process {
    private List<Integer> Wptrs = new ArrayList<>();
    @Nonnull private Integer topWptr;
    public ProcessStatus status;

    public void updateWptr(@Nonnull Integer wptr) {
        if (wptr < topWptr) {
            topWptr = wptr;
        }
        Wptrs.add(wptr);
    }

    public List<Integer> getWptrs() {
        List<Integer> result = new ArrayList<>();
        result.addAll(Wptrs);
        return result;
    }

    @Nonnull public Integer getTopWptr() {
        return topWptr;
    }

    @Nonnull public Integer getCurrentWptr() {
        return Wptrs.get(Wptrs.size() - 1);
    }

    public Process(int Wptr, ProcessStatus status) {
        this.Wptrs.add(Wptr);
        this.topWptr = Wptr;
        this.status = status;
    }
}
