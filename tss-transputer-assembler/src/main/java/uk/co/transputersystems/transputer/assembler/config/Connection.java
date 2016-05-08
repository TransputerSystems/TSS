package uk.co.transputersystems.transputer.assembler.config;

public class Connection {
    private int target_processor;
    private int dest_port;
    private String channel;

    public int getTarget_processor() {
        return target_processor;
    }

    public void setTarget_processor(int target_processor) {
        this.target_processor = target_processor;
    }

    public int getDest_port() {
        return dest_port;
    }

    public void setDest_port(int dest_port) {
        this.dest_port = dest_port;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel == null ? "" : channel;
    }

    public Connection() {
        target_processor = 0;
        dest_port = 0;
        channel = "";
    }
}
