package uk.co.transputersystems.transputer.simulator;

import uk.co.transputersystems.transputer.simulator.models.Priority;

import static uk.co.transputersystems.transputer.simulator.models.Priority.HIGH;
import static uk.co.transputersystems.transputer.simulator.models.Priority.LOW;

public class TransputerHelpers {
    /**
      * Extract Wptr from Wdesc by masking out the priority bit
      */
    public static int extractWorkspacePointer(int x) {
        return (x & (-2));
    }

    /**
     * Extract the Priority from a workspace descriptor.
     */
    public static Priority extractPriority(int Wdesc) {
        return (Wdesc & 1) == 1 ? LOW : HIGH;
    }

    /**
     * Extract the priority bit from a workspace descriptor.
     */
    public static int extractPriorityBit(int Wdesc) {
        return (Wdesc & 1);
    }

    /**
     * Convert a priority to the bit that represents it (LOW = 1, HIGH = 0)
     */
    public static int priorityToBit(Priority priority) {
        return priority == LOW ? 1 : 0;
    }

    /**
     * Combine a Wptr and priority to form a Wdesc
     */
    public static int makeWorkspaceDescriptor(int workspacePointer, Priority priority) {
        return ((extractWorkspacePointer(workspacePointer)) | (priorityToBit(priority)));
    }

    /**
     * Convert a byte address to a word address by masking out the bottom two bits.
     */
    public static int extractWordSelector(int x) {
        return (x & (-4));
    }

    /**
     * Extract the byte selector from a byte address by masking out all but the bottom two bits.
     */
    public static int extractByteSelector(int x) {
        return (x & 3);
    }

    public static int shiftLeft(int x, int y) {
        return ((y >= TransputerConstants.BITSPERWORD) ? 0 : (x << y));
    }

    public static int shiftRight(int x, int y) {
        return ((y >= TransputerConstants.BITSPERWORD) ? 0 : (x >> y));
    }

    /**
     * @return the minimum of x and y
     */
    public static int min(int x, int y) {
        return ((x < y) ? x : y);
    }

    /**
     * Extract the 4-bit opcode from an instruction byte. It is shifted to the least significant position.
     */
    public static byte extractOpcode(byte instruction) {
        return (byte)((instruction >> 4) & 0xF);
    }

    /**
     * Extract the 4-bit direct operand from an instruction byte.
     */
    public static byte extractDirectOperand(byte instruction) {
        return (byte)(instruction & 0xF);
    }

    /**
     * Convert a channel address to port address.
     */
    public static int convertChannelToPort(int channel) {
        return ((channel >> 1) & 0xF);
    }
}
