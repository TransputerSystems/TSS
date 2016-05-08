package uk.co.transputersystems.transputer.assembler.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Processor {
    private int processor_id;
    @Nonnull private List<Connection> connections;
    @Nonnull private List<IOPin> iopins;

    public List<IOPin> getIopins() {
        return iopins;
    }

    public void setIopins(List<IOPin> iopins) {
        this.iopins = iopins == null ? new ArrayList<>() : iopins;
    }

    public int getProcessor_id() {
        return processor_id;
    }

    public void setProcessor_id(int processor_id) {
        this.processor_id = processor_id;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections == null ? new ArrayList<>() : connections;
    }

    public Processor() {
        processor_id = 0;
        connections = new ArrayList<>();
        iopins = new ArrayList<>();
    }
}
