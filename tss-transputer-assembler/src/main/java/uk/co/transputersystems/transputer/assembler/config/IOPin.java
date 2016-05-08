package uk.co.transputersystems.transputer.assembler.config;

public class IOPin {
    private int addr;
    private String channel;
    private int config;

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getConfig() {
        return config;
    }

    public void setConfig(int config) {
        this.config = config;
    }

    public IOPin() {
        addr = 0;
        channel = "";
        config = 0;
    }
}
