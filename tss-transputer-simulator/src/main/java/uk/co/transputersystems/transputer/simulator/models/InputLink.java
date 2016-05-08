package uk.co.transputersystems.transputer.simulator.models;

/**
 * Created by Edward on 28/02/2016.
 */
public class InputLink {
    // Shared with transputer
    public int Wptr;
    public int WptrToProcessor;
    public int messageLength;
    public int messagePointer;

    public int fromProcessor;
    public int toProcessor;

    public boolean hasData;
    public byte pendingData;
    // Used in processInputLink to store data permanently as otherwise
    // it would be overwritten in next switch_step iteration
    public int pointer, count, inChannel;
    public byte readData;

    /**
     * Has a byte been input?
     */
    public boolean ready;

    /**
     * Is transfer pending?
     */
    public boolean requested;

    /**
     * Is link enabled?
     */
    public boolean enabled;

    public byte ack;
}
