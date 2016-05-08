package uk.co.transputersystems.transputer.simulator.models;

/**
 * Created by Edward on 28/02/2016.
 */
public class OutputLink {
    // Shared
    public int Wptr;
    public int WptrToProcessor;

    // Not shared
    public int WptrPrivate;
    public int messageLength;
    public int FptrReg;
    public int BptrReg;

    // Shared with transputer
    public int fromProcessor;
    public int toProcessor;

    public boolean hasData;
    public byte outData;

    // Used in processOutputLink to store data permanently as otherwise
    // it would be overwritten in next switch_step iteration
    public int outPointer, outCount, outChannel;
    public byte outByte;
    public boolean ready, requested, enabled;

    // Acknowledgement
    public byte ack;
}
