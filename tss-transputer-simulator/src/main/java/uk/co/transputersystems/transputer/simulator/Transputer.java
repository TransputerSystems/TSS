package uk.co.transputersystems.transputer.simulator;

import uk.co.transputersystems.transputer.simulator.debugger.DebuggerRecordedState;
import uk.co.transputersystems.transputer.simulator.debugger.Process;
import uk.co.transputersystems.transputer.simulator.debugger.ProcessStatus;
import uk.co.transputersystems.transputer.simulator.models.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Scanner;

import static uk.co.transputersystems.transputer.simulator.models.Priority.HIGH;
import static uk.co.transputersystems.transputer.simulator.models.Priority.LOW;

public class Transputer {
    private final byte id;
    public final Registers registers = new Registers();
    private final StatusRegister sreg = new StatusRegister();
    private final int[] FptrReg = new int[2];
    private final int[] BptrReg = new int[2];
    private int Ereg;
    private final int[] ClockReg = new int[2];
    private final byte[] mem;
    public int programEndPtr;
    // It seems that this is actually a memory
    // Location in the reserved memory space
    //public int TPtrLoc[2];
    private final int[] TNextReg = new int[2];
    private final boolean[] TEnabled = new boolean[2];
    private int BMbuffer;

    public final InputLink[] inputLinks = new InputLink[TransputerConstants.IN_PORTS];
    private final OutputLink outputLink = new OutputLink();

    public final DebuggerRecordedState debuggerState = new DebuggerRecordedState();

    public final PrintWriter stdout;
    public final PrintWriter stderr;

    public static void switchStep(Transputer transputers[], PrintWriter stdout) {
        int i, j;

        for (i = 0; i < transputers.length; i++) {
            // Output loop
            // Transfer outData to pendingData on the receiving site
            // Only transfer if link_out has data
            if (transputers[i].outputLink.hasData) {
                // Decode target processor and port
                int targetChannel = transputers[i].outputLink.outChannel;
                int targetProcessor = (targetChannel >> (TransputerConstants.SHIFT_IN_PORTS + 1));
                byte targetPort = (byte)((targetChannel >> 1) & ((1 << TransputerConstants.SHIFT_IN_PORTS) - 1));
                stdout.printf("Switch passed information from transputer %d to %d port %d\n",
                        i, targetProcessor, targetPort);

                // First reset in case anything went wrong before
                transputers[targetProcessor].inputLinks[targetPort].pendingData = transputers[i].outputLink.outData;
                transputers[targetProcessor].inputLinks[targetPort].hasData = true;

                // Sender has no more data to send
                transputers[i].outputLink.hasData = false;
            }

            // Input loop, check every port for data that has been received and
            // let sender know
            for (j = 0; j < TransputerConstants.IN_PORTS; j++) {
                // Check if there's an acknowledgement
                if (transputers[i].inputLinks[j].ack == TransputerConstants.ACKDATA) {
                    // Decode the source processor of the data that was sent.
                    // Don't need port as there's only one ouput channel
                    int sourceChannel = transputers[i].inputLinks[j].inChannel;
                    int sourceProcessor = (sourceChannel >> (TransputerConstants.SHIFT_IN_PORTS + 1));
                    //                byte source_port = (source_port >> 1) & ((1 << SHIFT_IN_PORTS) - 1);

                    transputers[sourceProcessor].outputLink.ack = TransputerConstants.ACKDATA;

                    // Reset ack
                    transputers[i].inputLinks[j].ack = TransputerConstants.NOIO;
                }
            }
        }
    }


    /**
     * @return the nth word after base
     */
    private static int AtWord(int base, int nth) {
        return TransputerHelpers.extractWordSelector(base) + nth * TransputerConstants.BYTESPERWORD;
    }

    /**
     * @return the nth byte after base
     */
    private static int atByte(int base, int nth) {
        return base + nth;
    }

    private static int CalcShiftUp(int SB, int DB) {
        int shift = (DB - SB) % TransputerConstants.BYTESPERWORD;
        if (shift < 0) {
            return shift + TransputerConstants.BYTESPERWORD;
        } else {
            return shift;
        }
    }

    public static int ChanOffset(int val) {
        return val >> TransputerConstants.BYTESELLEN;
    }

    /**
     * @return true if T2 is later than T1, otherwise false
     */
    private static boolean Later(int T1, int T2) {
        return (T1 - T2) > 0;
    }

    private static int Select(int P, int C, int shift_up) {
        int shift_up_bits = shift_up * 8;
        int complement = TransputerConstants.BITSPERWORD - shift_up_bits;
        int low = TransputerHelpers.shiftRight(P, complement) | TransputerHelpers.shiftLeft((-1), shift_up_bits);
        int high = C | TransputerHelpers.shiftLeft((-1), complement);
        high = TransputerHelpers.shiftLeft(high, shift_up_bits) | ~(TransputerHelpers.shiftLeft((-1), shift_up_bits));
        return low & high;
    }

    /**
     * Reads a word from an array in transputer memory
     * @param base A pointer to the start of the array
     * @param nth The array index
     * @return The word that was read
     */
    private int RIndexWord(int base, int nth) {
        ByteBuffer bb = ByteBuffer.wrap(mem, AtWord(base, nth), TransputerConstants.BYTESPERWORD);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    /**
     * Reads a byte from an array in tranputer memory
     * @param base A pointer to the start of the array
     * @param nth The array index
     * @return The byte that was read
     */
    private byte RIndexByte(int base, int nth) {
        return mem[atByte(base, nth)];
    }

    /**
     * Writes a word to an array in transputer memory
     * @param base A pointer to the start of the array
     * @param nth The array index
     */
    private void WIndexWord(int base, int nth, int x) {
        ByteBuffer bb = ByteBuffer.wrap(mem, AtWord(base, nth), TransputerConstants.BYTESPERWORD);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(x);
        debuggerState.memAccessed.add(base);
        debuggerState.memAccessed.add(base + 1);
        debuggerState.memAccessed.add(base + 2);
        debuggerState.memAccessed.add(base + 3);
    }

    /**
     * Writes a byte to an array in transputer memory
     * @param base A pointer to the start of the array
     * @param nth The array index
     */
    private void WIndexByte(int base, int nth, byte x) {
        mem[base + nth] = x;
        debuggerState.memAccessed.add(base + nth);
    }

    private void saveRegistersPendingSoftIO() {
        WIndexWord(registers.Breg, 0, registers.Wptr);
        WIndexWord(registers.Wptr, TransputerConstants.IPTR_S, registers.Iptr + 1);
        WIndexWord(registers.Wptr, TransputerConstants.POINTER_S, registers.Creg);
    }

    public Transputer(byte id, PrintWriter stdout, PrintWriter stderr) {
        mem = new byte[TransputerConstants.MEMSIZE];

        FptrReg[0] = TransputerConstants.NOTPROCESS_P;
        FptrReg[1] = TransputerConstants.NOTPROCESS_P;
        BptrReg[0] = TransputerConstants.NOTPROCESS_P;
        BptrReg[1] = TransputerConstants.NOTPROCESS_P;
        registers.Wptr = TransputerConstants.NOTPROCESS_P;

        this.id = id;
        this.stdout = stdout;
        this.stderr = stderr;

        // Link initialisation

        // Input links
        int i;
        for (i = 0; i < TransputerConstants.IN_PORTS; i++) {
            inputLinks[i] = new InputLink();
            inputLinks[i].fromProcessor = TransputerConstants.NOIO;
            inputLinks[i].toProcessor = TransputerConstants.NOIO;
            inputLinks[i].hasData = false;
            inputLinks[i].ready = false;
            inputLinks[i].requested = false;
            inputLinks[i].enabled = false;
            inputLinks[i].ack = TransputerConstants.NOIO;
        }

        // Output link
        outputLink.fromProcessor = TransputerConstants.NOIO;
        outputLink.toProcessor = TransputerConstants.NOIO;
        outputLink.hasData = false;
        outputLink.requested = false;
        outputLink.ready = true;
        outputLink.ack = TransputerConstants.NOIO;
        outputLink.FptrReg = TransputerConstants.NOTPROCESS_P;
        outputLink.BptrReg = TransputerConstants.NOTPROCESS_P;
    }

    public void printRegisters(PrintWriter output) {
        output.printf("ID: %d \tAreg: %08X \tBreg: %08X \tCreg: %08X\n\t\tOreg: %08X \tWptr: %08X \tIptr: %08X \tPriority: %01X\n\n",
                id, registers.Areg, registers.Breg, registers.Creg, registers.Oreg, TransputerHelpers.extractWorkspacePointer(registers.Wptr), registers.Iptr, TransputerHelpers.extractPriorityBit(registers.Wptr));
    }

    public void printMemory(int numberUnits, int bytesPerUnit, String format, int address, PrintWriter output) {
        output.printf("## Transputer %d\n", id);

        for (int unitSelector = 0; unitSelector < numberUnits; unitSelector++) {
            long unit = 0;
            for (int byteSelector = 0; byteSelector < bytesPerUnit; byteSelector++) {
                unit |= ((long)mem[address + (unitSelector*bytesPerUnit) + byteSelector] & 0xFF) << (byteSelector*8);
            }
            output.printf("0x%08x\t", address + (unitSelector*bytesPerUnit));
            switch (format) {
                case "x":
                    output.printf("%0" + bytesPerUnit*2 + "x", unit);
                    break;
                case "d":
                    output.printf("%d", unit);
                    break;
                case "o":
                    output.printf("o%o", unit);
                    break;
                case "a":
                    output.printf("addr %08x", unit);
                    break;
                case "c":
                    output.printf("%c", (char)unit);
                    break;
                case "f":
                    output.printf("%f", (float)unit);
                    break;
                case "i": // instruction
                    output.printf("%2x", unit);
                    break;
                default:
                    output.printf("%08x", unit);
                    break;
            }
            output.println();
        }

        output.printf("\n");
    }

    public void printRecentMemory(PrintWriter output) {
        int i;
        output.printf("## Transputer %d\n", id);
        output.printf("%-12s    +3 +2 +1 +0\n", "Address");
        for (i = 0; i < TransputerConstants.MEMSIZE; i += 4) {
            if (debuggerState.memAccessed.contains(i) || debuggerState.memAccessed.contains(i + 1) ||
                    debuggerState.memAccessed.contains(i + 2) || debuggerState.memAccessed.contains(i + 3)) {
                output.printf("0d%04d/0x%03X    %02X %02X %02X %02X\n", i, i,
                        mem[i + 3], mem[i + 2],
                        mem[i + 1], mem[i]);
                debuggerState.memAccessed.remove(i);
                debuggerState.memAccessed.remove(i + 1);
                debuggerState.memAccessed.remove(i + 2);
                debuggerState.memAccessed.remove(i + 3);
            }
        }
        output.printf("\n");
    }

    public void printWorkspaceMemory(PrintWriter output) {
        output.printf("## Transputer %d\n", id);
        for (Process process : debuggerState.processes) {
            output.printf("### Process %08X - %s\n", process.getCurrentWptr(), process.status.name());
            List<Integer> wptrs = process.getWptrs();
            for (int i = AtWord(process.getTopWptr(),0); i <= AtWord(wptrs.get(0),0); i += 4) {
                if (wptrs.contains(i) || wptrs.contains(i|1)) {
                    output.printf("-- Workspace %d --\n", wptrs.indexOf(i));
                }
                output.printf("0d%04d/0x%03X    %02X %02X %02X %02X\n", i, i,
                        mem[i + 3], mem[i + 2],
                        mem[i + 1], mem[i]);
            }
            output.printf("\n");
        }
        output.printf("\n");
    }

    public void printLinks(PrintWriter output) {
        int i;
        for (i = 0; i < TransputerConstants.IN_PORTS; i++) {
            output.printf("l:%d Wptr:%08X WptrToProcessor:%08X messageLength:%d messagePointer:%08X fromProcessor:%d,toProcessor:%d,hasData:%b,pendingData:%08X,pointer:%08X,count:%08X,inChannel:%08X,readData:%08X,ready:%b,requested:%b,enabled:%b,ack:%d\n",
                    i,
                    inputLinks[i].Wptr,
                    inputLinks[i].WptrToProcessor,
                    inputLinks[i].messageLength,
                    inputLinks[i].messagePointer,
                    inputLinks[i].fromProcessor,
                    inputLinks[i].toProcessor,
                    inputLinks[i].hasData,
                    inputLinks[i].pendingData,
                    inputLinks[i].pointer,
                    inputLinks[i].count,
                    inputLinks[i].inChannel,
                    inputLinks[i].readData,
                    inputLinks[i].ready,
                    inputLinks[i].requested,
                    inputLinks[i].enabled,
                    inputLinks[i].ack);
        }

        output.printf("ol:Wptr:%08X WptrToProcessor:%08X messageLength:%d FptrReg:%08X BptrReg:%08X fromProcessor:%d toProcessor:%d hasData:%b outData:%08X outPointer:%08X outCount:%08X outChannel:%08X outByte:%08X ready:%b requested:%b enabled:%b ack:%b\n",
                outputLink.Wptr,
                outputLink.WptrToProcessor,
                outputLink.messageLength,
                outputLink.FptrReg,
                outputLink.BptrReg,
                outputLink.fromProcessor,
                outputLink.toProcessor,
                outputLink.hasData,
                outputLink.outData,
                outputLink.outPointer,
                outputLink.outCount,
                outputLink.outChannel,
                outputLink.outByte,
                outputLink.ready,
                outputLink.requested,
                outputLink.enabled,
                outputLink.ack);
    }

    /**
     * Loads a program from a file into memory
     * @throws FileNotFoundException
     * @throws IOException
     */
    public int loadProgram(File fp) throws IOException {

        if (!fp.exists()) {
            return TransputerConstants.ERROR;
        }

        byte instruction;
        int i = TransputerConstants.CODESTART;

        FileInputStream fInput = new FileInputStream(fp);
        Scanner fScanner = new Scanner(fInput);

        // Load entry point address from file first
        fScanner.next("Start");
        fScanner.next("address:");

        if(!fScanner.hasNextInt(16)) {
            stderr.printf("## Transputer %d\n", id);
            stderr.printf("Error reading start address\n");
            return TransputerConstants.ERROR;
        } else {
            int initPointer = fScanner.nextInt(16);

            if (!fScanner.hasNext()) {
                stderr.printf("## Transputer %d\n", id);
                stderr.printf("Error reading start address value\n");
                return TransputerConstants.ERROR;
            } else {
                registers.Iptr = TransputerConstants.CODESTART + initPointer;
            }
        }

        while (fScanner.hasNextInt(16)) {
            instruction = (byte)fScanner.nextInt(16);
            mem[i] = instruction;
            debuggerState.memAccessed.add(i++);
        }

        programEndPtr = i - 1;
        // First word after boot preogram
        // Start processor in low priority
        registers.Wptr = TransputerHelpers.makeWorkspaceDescriptor(TransputerConstants.CODESTART - TransputerConstants.BYTESPERWORD, LOW);
        debuggerState.processes.add(new Process(registers.Wptr, ProcessStatus.RUNNING));

        // set the WDESCINTSAVE to nothing
        WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.WDESCINTSAVE, TransputerConstants.NOTPROCESS_P);

        // TODO: Strictly speaking these are not initialised when the transputer is reset!
        WIndexWord(TransputerConstants.TIMERBASE, 0, TransputerConstants.NOTPROCESS_P);
        WIndexWord(TransputerConstants.TIMERBASE, 1, TransputerConstants.NOTPROCESS_P);

        fInput.close();

        return TransputerConstants.SUCCESS;
    }

    /**
     * Perform an overflow check
     */
    private void overflowCheck(byte opcode, int a, int b) throws UnexpectedOverflowException {
        if (opcode == TransputerConstants.SUB) {
            opcode = TransputerConstants.ADD;
            b = -b;
        }

        if (opcode == TransputerConstants.ADD) {
            if (a > 0 && b > TransputerConstants.MAXINT - a) {
            /* handle overflow */
                sreg.errorFlag = true;
            } else if (a < 0 && b < TransputerConstants.MININT - a) {
            /* handle underflow */
                sreg.errorFlag = true;
            }
        } else if (opcode == TransputerConstants.MUL) {
            int x = a * b;
            if (a != 0 && x / a != b) {
                sreg.errorFlag = true;
            }
        } else {
            stderr.printf("Unrecognised opcode %X for overflow check\n", opcode);
            throw new UnexpectedOverflowException();
        }
    }

    private void setErrorFlag() {
        sreg.errorFlag = true;
    }

    private void blockMoveFinalStep() {
        stdout.printf("Block_move_final_step\n");
        if (sreg.ioBit) {
            sreg.moveBit = false;
            sreg.ioBit = false;
            runProcess(Ereg);
        } else {
            sreg.moveBit = false;
        }
    }
    
    /**
     * Areg - length, Breg - destination, Creg - source
     */
    private void blockMoveMiddleStep() {
        stdout.printf("Block_move_middle_step\n");
        if (registers.Areg == 0) {
            blockMoveFinalStep();
        } else {
            int SB = TransputerHelpers.extractByteSelector(registers.Creg);
            int DB = TransputerHelpers.extractByteSelector(registers.Breg);
            int shift = CalcShiftUp(SB, DB);
            int current = 0;
            if (registers.Areg > shift) {
                if (shift == 0) {
                    current = RIndexWord(registers.Creg, 0);
                } else {
                    current = RIndexWord(registers.Creg, 1);
                }
            }
            int selected = Select(BMbuffer, current, shift);
            int bytesToWrite = TransputerHelpers.min((TransputerConstants.BITSPERWORD / 8) - DB, registers.Areg);
            writePartWord(registers.Breg, selected, DB, bytesToWrite);
            registers.Breg = atByte(registers.Breg, bytesToWrite);
            registers.Areg = registers.Areg - bytesToWrite;
            registers.Creg = atByte(registers.Creg, bytesToWrite);
            BMbuffer = current;
        }
    }

    private void writePartWord(int address, int word, int startByte, int len) {
        int insert = 0;
        for (int byteIndex = startByte; byteIndex < startByte + len; byteIndex++) {
            insert = insert | TransputerHelpers.shiftLeft(0xFF, (byteIndex * 8));
        }
        int keep = ~insert;
        int buffer = RIndexWord(address, 0);
        buffer = (buffer & keep) | (word & insert);
        WIndexWord(address, 0, buffer);
    }

    /**
     * Areg - length, Breg - destination, Creg - source
     */
    private void blockMoveFirstStep() {
        stdout.printf("move_first_step=> src:%08X dest:%08X len:%08X\n", registers.Creg, registers.Breg, registers.Areg);
        if (registers.Areg == 0) {
            blockMoveFinalStep();
        } else {
            sreg.moveBit = true; // Inserted by me, based on
            int SB = TransputerHelpers.extractByteSelector(registers.Creg);
            int DB = TransputerHelpers.extractByteSelector(registers.Breg);
            int shift = CalcShiftUp(SB, DB);
            int current = RIndexWord(registers.Creg, 0); // addr 16
            int bytesToRead = TransputerHelpers.min((TransputerConstants.BITSPERWORD / 8) - SB, registers.Areg);
            int bytesToWrite = TransputerHelpers.min((TransputerConstants.BITSPERWORD / 8) - DB, registers.Areg);
            int selected;
            if (bytesToRead >= bytesToWrite) {
                selected = Select(current, current, shift);
            } else {
                BMbuffer = current;
                current = RIndexWord(registers.Creg, 1); // addr 20
                selected = Select(BMbuffer, current, shift);
            }
            writePartWord(registers.Breg, selected, DB, bytesToWrite);
            registers.Breg = atByte(registers.Breg, bytesToWrite);
            registers.Areg = registers.Areg - bytesToWrite;
            registers.Creg = atByte(registers.Creg, bytesToWrite);
            BMbuffer = current;
        }
    }

    /**
     * Enqueues a process into the round-robin list of processes
     * @param Wptr a pointer to the enqueued process' workspace
     * @param processPriority the enqueued process' priority
     */
    private void enqueueProcess(int Wptr, Priority processPriority) {
        //IF
        //    Fptr = NotProcess.p
        //        Fptr := ProcPtr
        //    TRUE
        //        WIndexWord(Bptr, Link.s, ProcPtr)
        // Bptr := ProcPtr
        int processPriorityBit = TransputerHelpers.priorityToBit(processPriority);
        if (FptrReg[processPriorityBit] == TransputerConstants.NOTPROCESS_P) {
            FptrReg[processPriorityBit]  = Wptr;
        } else {
            WIndexWord(BptrReg[processPriorityBit], TransputerConstants.LINK_S, Wptr);
        }
        BptrReg[processPriorityBit] = Wptr;

        debuggerState.processes.stream()
                .filter(p -> p.getCurrentWptr() == Wptr)
                .filter(p -> p.status != ProcessStatus.QUEUED)
                .forEach(p -> p.status = ProcessStatus.QUEUED);
    }

    /**
     * Activates a process, i.e. loads the instruction pointer from memory
     */
    private void activateProcess() {
        registers.Oreg = 0;
        registers.Iptr = RIndexWord(registers.Wptr, TransputerConstants.IPTR_S);
        debuggerState.processes.stream()
                .filter(p -> p.getCurrentWptr() == registers.Wptr)
                .filter(p -> p.status == ProcessStatus.QUEUED)
                .forEach(p -> p.status = ProcessStatus.RUNNING);
    }

    private void hardChannelInputAction(int channelNumber) {
        int portNumber = TransputerHelpers.convertChannelToPort(channelNumber);

        // We are not doing this because our hardware channels are different
        //WIndexWord(registers.Breg, 0, registers.Wptr);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.IPTR_S, registers.Iptr);

        inputLinks[portNumber].fromProcessor = TransputerConstants.PERFORMIO;
        inputLinks[portNumber].messagePointer = registers.Creg;
        inputLinks[portNumber].messageLength = registers.Areg;
        inputLinks[portNumber].Wptr = registers.Wptr;
        inputLinks[portNumber].inChannel = channelNumber;
    }

    private void hardChannelOutputAction() {
        // We are not doing this because our hardware channels are different
        //WIndexWord(registers.Breg, 0, registers.Wptr);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.IPTR_S, registers.Iptr);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.POINTER_S, registers.Creg);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.LENGTH_S, registers.Areg);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.CHAN_S, registers.Breg);
        WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.LINK_S, TransputerConstants.NOTPROCESS_P);
        outputLink.Wptr = registers.Wptr;

        outputLink.fromProcessor = TransputerConstants.PERFORMIO;
    }

    private void performInput() {
        //int channelNumber = ChanOffset(registers.Breg);
        int channelNumber = registers.Breg;
        //if (channelNumber >= LINKCHANS) {
        if ((channelNumber & 1) == 0) {
            stdout.printf("channelNumber >= LINKCHANS\n");
            int processDescriptor = RIndexWord(registers.Breg, 0);
            if (processDescriptor == TransputerConstants.NOTPROCESS_P) {
                stdout.printf("processDescriptor == NOTPROCESS_P\n");
                // not ready to transfer, wait
                saveRegistersPendingSoftIO();
                sreg.gotoStartNewProcess = true;
            } else {
                // ready to transfer
                registers.Iptr += 1;
                stdout.printf("ready to transfer\n");
                WIndexWord(registers.Breg, 0, TransputerConstants.NOTPROCESS_P);
                int processPointer = TransputerHelpers.extractWorkspacePointer(processDescriptor);
                int sourcePtr = TransputerHelpers.extractWorkspacePointer(RIndexWord(processPointer, TransputerConstants.POINTER_S));
                Ereg = processDescriptor;
                registers.Breg = registers.Creg;
                registers.Creg = sourcePtr;
                sreg.moveBit = true;
                sreg.ioBit = true;
                blockMoveFirstStep();
            }
        } else {
            // hardware channel
            //HardChannelInputOutputAction(chan_num);
            registers.Iptr += 1;
            hardChannelInputAction(channelNumber);
            sreg.gotoStartNewProcess = true;
        }
    }

    private void performOutput() {
        //int channelNumber = ChanOffset(registers.Breg);
        int channelNumber = registers.Breg;
        //if (channelNumber >= LINKCHANS) {
        if ((channelNumber & 1) == 0) {
            // Internal channel
            int processDescriptor = RIndexWord(registers.Breg, 0);
            if (processDescriptor == TransputerConstants.NOTPROCESS_P) {
                saveRegistersPendingSoftIO();
                sreg.gotoStartNewProcess = true;
            } else {
                int processPointer = TransputerHelpers.extractWorkspacePointer(processDescriptor);
                int destinationPointer = RIndexWord(processPointer, TransputerConstants.POINTER_S);
                if (destinationPointer == TransputerConstants.ENABLING_P) {
                    WIndexWord(processPointer, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
                    saveRegistersPendingSoftIO();
                    sreg.gotoStartNewProcess = true;
                } else if (destinationPointer == TransputerConstants.WAITING_P) {
                    WIndexWord(processPointer, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
                    saveRegistersPendingSoftIO();
                    sreg.gotoStartNewProcess = true;
                    registers.Iptr += 1;
                    runProcess(processDescriptor);
                } else if (destinationPointer == TransputerConstants.READY_P) {
                    saveRegistersPendingSoftIO();
                    sreg.gotoStartNewProcess = true;
                } else {
                    // reset channel
                    registers.Iptr += 1;
                    WIndexWord(registers.Breg, 0, TransputerConstants.NOTPROCESS_P);
                    Ereg = processDescriptor;
                    registers.Breg = destinationPointer;
                    sreg.moveBit = true;
                    sreg.ioBit = true;
                    blockMoveFirstStep();
                }
            }
        } else {
            // Hard channel
            registers.Iptr += 1;
            hardChannelOutputAction();
            sreg.gotoStartNewProcess = true;
        }
    }

    // Documentation says that we should tell whether DMA is in use
    private void saveRegisters() {
        WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.WDESCINTSAVE, registers.Wptr);
        if (TransputerHelpers.extractPriority(registers.Wptr) == LOW) {
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.IPTRINTSAVE, registers.Iptr);
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.AREGINTSAVE, registers.Areg);
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.BREGINTSAVE, registers.Breg);
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.CREGINTSAVE, registers.Creg);
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.STATUSINTSAVE, sreg.errorFlag ? 1 : 0);
        }

        // This is not a concurrent simulation so this case will never
        // occur
        if (sreg.moveBit) {
            WIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.EREGINTSAVE, Ereg);
        }
    }

    /**
     * Runs a process
     * @param Wptr a pointer to the process workspace
     */
    private void runProcess(int Wptr) {
        Priority newProcessPriority = TransputerHelpers.extractPriority(Wptr);
        Priority currentProcessPriority = TransputerHelpers.extractPriority(registers.Wptr);
        int processPointer = TransputerHelpers.extractWorkspacePointer(Wptr);
        stdout.printf("run_proc\n");

        switch (currentProcessPriority) {
            case HIGH:
                // If the current process is high priority, queue up the new process
                stdout.printf("run_proc.enqueue_pri0\n");
                enqueueProcess(processPointer, newProcessPriority);
                debuggerState.processes.add(new Process(Wptr, ProcessStatus.QUEUED));
                break;
            case LOW:
                switch (newProcessPriority) {
                    case HIGH:
                        // If the current process is low priority and the new process high priority, switch immediately
                        stdout.printf("run_proc changing priority\n");
                        saveRegisters();
                        registers.Wptr = Wptr;
                        // TODO: Error flag stuff
                        // probable nothing has to change though
                        activateProcess();
                        debuggerState.processes.add(new Process(registers.Wptr, ProcessStatus.RUNNING));
                        break;
                    case LOW:
                        // If the current process is low priority and the new process high priority, queue the new process
                        stdout.printf("run_proc.proc_pri==1\n");
                        if (TransputerHelpers.extractWorkspacePointer(registers.Wptr) == TransputerConstants.NOTPROCESS_P) {
                            stdout.printf("run_proc-.No proc running\n");
                            registers.Wptr = Wptr;
                            activateProcess();
                            debuggerState.processes.add(new Process(Wptr, ProcessStatus.RUNNING));
                        } else {
                            stdout.printf("runproc.enqueue_pri1\n");
                            enqueueProcess(processPointer, LOW);
                            debuggerState.processes.add(new Process(Wptr, ProcessStatus.QUEUED));
                        }
                        break;
                }
                break;
        }
    }

    private void dequeueProcess(Priority processPriority) {
        debuggerState.processes.stream().filter(p -> p.getCurrentWptr() == registers.Wptr).findFirst().get().status = ProcessStatus.TERMINATED;
        int processPriorityBit = TransputerHelpers.priorityToBit(processPriority);
        registers.Wptr = FptrReg[processPriorityBit] | processPriorityBit;
        if (FptrReg[processPriorityBit] == BptrReg[processPriorityBit]) {
            FptrReg[processPriorityBit] = TransputerConstants.NOTPROCESS_P;
        } else {
            FptrReg[processPriorityBit] = RIndexWord(FptrReg[processPriorityBit], TransputerConstants.LINK_S);
        }
    }

    private void restoreRegisters() {
        stdout.printf("restore_registers\n");
        registers.Wptr = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.WDESCINTSAVE);
        if (registers.Wptr != (TransputerConstants.NOTPROCESS_P | 1)){
            registers.Iptr = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.IPTRINTSAVE);
            registers.Areg = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.AREGINTSAVE);
            registers.Breg = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.BREGINTSAVE);
            registers.Creg = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.CREGINTSAVE);
            sreg.errorFlag = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.STATUSINTSAVE) != 0;
        }
        if (sreg.moveBit) {
            Ereg = RIndexWord(TransputerConstants.SAVEBASE, TransputerConstants.EREGINTSAVE);
        }
    }

    private void startNewProcess() {
        sreg.gotoStartNewProcess = false;
        Priority currentProcessPriority = TransputerHelpers.extractPriority(registers.Wptr);
        switch (currentProcessPriority) {
            case HIGH:
                stdout.printf("priority 0\n");
                if (FptrReg[0] != TransputerConstants.NOTPROCESS_P) {
                    stdout.printf("!=NOTPROCESS_P\n");
                    dequeueProcess(HIGH);
                    activateProcess();
                } else {
                    stdout.printf("NOTPROCESS_P\n");
                    restoreRegisters();
                    if (TransputerHelpers.extractWorkspacePointer(registers.Wptr) == TransputerConstants.NOTPROCESS_P && FptrReg[1] != TransputerConstants.NOTPROCESS_P) {
                        stdout.printf("no interrupted process\n");
                        dequeueProcess(LOW);
                        activateProcess();
                    } else if (TransputerHelpers.extractWorkspacePointer(registers.Wptr) == TransputerConstants.NOTPROCESS_P) {
                        // Do nothing ...
                    } else if (sreg.moveBit) {
                        blockMoveFirstStep();
                    }
                }
                break;
            case LOW:
                stdout.printf("priority 1\n");
                if (FptrReg[1] != TransputerConstants.NOTPROCESS_P) {
                    dequeueProcess(LOW);
                    activateProcess();
                } else {
                    stdout.printf("Wptr = NOTPROCESS_P\n");
                    registers.Wptr = TransputerConstants.NOTPROCESS_P | 1;
                }
                break;
        }
    }

    /**
     * TODO: is 'sel' 'selected'?
     */
    private void isThisSelProcess() {
        int disableStat = RIndexWord(registers.Wptr, 0);
        stdout.printf("is_this_sel_proc\n");
        if (disableStat == TransputerConstants.NONESELECTED_O) {
            stdout.printf("Noneselected\n");
            WIndexWord(registers.Wptr, 0, registers.Areg);
            registers.Areg = 1; // TRUE
        } else {
            stdout.printf("else\n");
            registers.Areg = 0; // FALSE
        }
    }

    private void enqueueLinkOut(int processPointer) {
        if (outputLink.FptrReg == TransputerConstants.NOTPROCESS_P) {
            outputLink.FptrReg = processPointer;
        } else {
            WIndexWord(TransputerHelpers.extractWorkspacePointer(outputLink.BptrReg), TransputerConstants.LINK_S, processPointer);
        }
        outputLink.BptrReg = processPointer;
    }

    private void dequeueLinkOut() {
        outputLink.WptrPrivate = outputLink.FptrReg;

        if (outputLink.FptrReg == outputLink.BptrReg) {
            outputLink.FptrReg = TransputerConstants.NOTPROCESS_P;
        } else {
            outputLink.FptrReg = RIndexWord(TransputerHelpers.extractWorkspacePointer(outputLink.FptrReg), TransputerConstants.LINK_S);
        }
        outputLink.outPointer = RIndexWord(TransputerHelpers.extractWorkspacePointer(outputLink.WptrPrivate), TransputerConstants.POINTER_S);
        outputLink.outCount = RIndexWord(TransputerHelpers.extractWorkspacePointer(outputLink.WptrPrivate), TransputerConstants.LENGTH_S);
        outputLink.outChannel = RIndexWord(TransputerHelpers.extractWorkspacePointer(outputLink.WptrPrivate), TransputerConstants.CHAN_S);
        outputLink.requested = true;
    }

    public void processInputLink(InputLink inputLink) throws UnexpectedOverflowException {
        int token = inputLink.fromProcessor;
        // LinkInData ? byte
        if (inputLink.hasData) {
            // Reset state
            inputLink.hasData = false;
            inputLink.readData = inputLink.pendingData;
            inputLink.ready = true;
        } else if (token != TransputerConstants.NOIO) {
            inputLink.fromProcessor = TransputerConstants.NOIO;
            if (token == TransputerConstants.ENABLE) {
                inputLink.enabled = true;
            } else if (token == TransputerConstants.STATUSENQUIRY) {
                inputLink.enabled = false;
                if (inputLink.ready) {
                    inputLink.toProcessor = TransputerConstants.READYREQUEST;
                } else {
                    inputLink.toProcessor = TransputerConstants.READYFALSE;
                }
            } else if (token == TransputerConstants.PERFORMIO) {
                inputLink.pointer = inputLink.messagePointer;
                inputLink.count = inputLink.messageLength;
                inputLink.requested = true;
            } else if (token == TransputerConstants.RESETREQUEST) {
                inputLink.ready = false;
                inputLink.enabled = false;
                inputLink.requested = false;
                inputLink.toProcessor = TransputerConstants.ACKRESET;
            }
        } else if (inputLink.requested && inputLink.ready) {
            // Acknowledge and store byte
            // Acknowledge
            inputLink.ack = TransputerConstants.ACKDATA;
            WIndexByte(inputLink.pointer, 0, inputLink.readData);
            inputLink.pointer = atByte(inputLink.pointer, 1);
            inputLink.count = inputLink.count - 1;
            if (inputLink.count == 0) {
                inputLink.requested = false;
                // PAR
                inputLink.toProcessor = TransputerConstants.RUNREQUEST;
                inputLink.WptrToProcessor = inputLink.Wptr;
                int interaction;
                interaction = inputLink.fromProcessor;
                while (interaction == TransputerConstants.NOIO) {
                    interaction = inputLink.fromProcessor;
                    performStep();
                }
                inputLink.fromProcessor = TransputerConstants.NOIO;
                if (interaction == TransputerConstants.ACKRUN) {
                    // SKIP
                } else if (interaction == TransputerConstants.RESETREQUEST) {
                    // Haven't implemented different priorities for hardware links
                    // priority = input_link.fromProcessor
                }
            } else {
                // SKIP
            }
            inputLink.ready = false;
        } else if (inputLink.enabled && inputLink.ready) {
            // Inform processor that link is ready
            inputLink.enabled = false;
            // PAR
            inputLink.toProcessor = TransputerConstants.READYREQUEST;
            inputLink.WptrToProcessor = inputLink.Wptr;
            int interaction = TransputerConstants.NOIO;
            while (interaction == TransputerConstants.NOIO) {
                interaction = inputLink.fromProcessor;
                performStep();
            }
            inputLink.fromProcessor = TransputerConstants.NOIO;
            if (interaction == TransputerConstants.ACKREADY) {
                // SKIP
            } else if (interaction == TransputerConstants.STATUSENQUIRY) {
                // Haven't implemented different priorities for hardware links
                // priority = input_link.form_processor
            } else if (interaction == TransputerConstants.RESETREQUEST) {
                //priority = input_link.fromProcessor;
                inputLink.ready = false;
            }
        }
    }

    public void processOutputLink() throws UnexpectedOverflowException {
        int token;
        token = outputLink.fromProcessor;
        outputLink.fromProcessor = TransputerConstants.NOIO;
        if (token == TransputerConstants.PERFORMIO) {
            int processPointer = outputLink.Wptr;

            if (!(outputLink.ready) || outputLink.requested ||
                    outputLink.FptrReg != TransputerConstants.NOTPROCESS_P) {
                enqueueLinkOut(processPointer);
            } else {
                outputLink.WptrPrivate = processPointer;
                outputLink.outPointer = RIndexWord(TransputerHelpers.extractWorkspacePointer(processPointer), TransputerConstants.POINTER_S);
                outputLink.outCount = RIndexWord(TransputerHelpers.extractWorkspacePointer(processPointer), TransputerConstants.LENGTH_S);
                outputLink.outChannel = RIndexWord(TransputerHelpers.extractWorkspacePointer(processPointer), TransputerConstants.CHAN_S);
                outputLink.requested = true;
            }
        } else if (token == TransputerConstants.RESETREQUEST) {
            outputLink.ready = true;
            outputLink.requested = false;
            outputLink.toProcessor = TransputerConstants.ACKRESET;
        } else if (outputLink.ready && outputLink.requested) {
            if (outputLink.outCount == 0) {
                outputLink.requested = false;
                // PAR
                //ToProcessor ! RUNREQUEST;
                outputLink.toProcessor = TransputerConstants.RUNREQUEST;
                outputLink.WptrToProcessor = outputLink.Wptr;

                int interaction;
                interaction = outputLink.fromProcessor;
                while (interaction != TransputerConstants.ACKRUN) {
                    if (interaction == TransputerConstants.PERFORMIO) {
                        enqueueLinkOut(outputLink.Wptr);
                        outputLink.fromProcessor = TransputerConstants.NOIO;
                    }
                    performStep();
                    interaction = outputLink.fromProcessor;
                }
                outputLink.fromProcessor = TransputerConstants.NOIO;
            } else {
                outputLink.outByte = RIndexByte(outputLink.outPointer, 0);
                outputLink.outPointer = atByte(outputLink.outPointer, 1);
                outputLink.outCount = outputLink.outCount - 1;

                // LinkOutData ! outputLink.outByte
                outputLink.hasData = true;
                outputLink.outData = outputLink.outByte;
                outputLink.ready = false;
            }
        } else if ((outputLink.ready) && !(outputLink.requested) && (outputLink.FptrReg != TransputerConstants.NOTPROCESS_P)) {
            dequeueLinkOut();
            outputLink.requested = true;
        } else if ((token = outputLink.ack) != TransputerConstants.NOIO) {
            outputLink.ack = TransputerConstants.NOIO;
            outputLink.ready = true;
        }
    }

    private void disableChannel() throws UnexpectedOverflowException {
        if (registers.Breg != 0 /*FALSE*/) {
            stdout.printf("Breg != FALSE\n");
            //int chan_num = ChanOffset(registers.Creg);
            int channelNumber = registers.Creg;

            //if (chan_num >= LINKCHANS) {
            if ((channelNumber & 1) == 0) {
                stdout.printf("Soft channel\n");
                registers.Breg = RIndexWord(registers.Creg, 0);
                if (registers.Breg == TransputerConstants.NOTPROCESS_P) {
                    stdout.printf("Breg == NOTPROCESS_P\n");
                    registers.Areg = 0; // FALSE
                } else if (registers.Breg == registers.Wptr) {
                    stdout.printf("Breg == Wdescreg\n");
                    WIndexWord(registers.Creg, 0, TransputerConstants.NOTPROCESS_P);
                    registers.Areg = 0; // FALSE
                } else {
                    stdout.printf("Breg else\n");
                    isThisSelProcess();
                }
            } else { // link-channel
                // TODO: Using physical link
                int token;
                int portNumber = TransputerHelpers.convertChannelToPort(channelNumber);

                inputLinks[portNumber].Wptr = TransputerConstants.NOTPROCESS_P;
                // PAR
                inputLinks[portNumber].fromProcessor = TransputerConstants.STATUSENQUIRY;
                // FROM
                token = inputLinks[portNumber].toProcessor;
                while (token == TransputerConstants.NOIO) {
                    token = inputLinks[portNumber].toProcessor;
                    processInputLink(inputLinks[portNumber]);
                }
                inputLinks[portNumber].toProcessor = TransputerConstants.NOIO;

                if (token == TransputerConstants.READYREQUEST) {
                    isThisSelProcess();
                } else if (token == TransputerConstants.READYFALSE) {
                    registers.Areg = 0; // FALSE
                }
            }
        } else {
            registers.Areg = 0; // FALSE
        }
    }

    private void enableChannel() throws UnexpectedOverflowException {
        if (registers.Areg != 0 /*FALSE*/) {
//        int chan_num = ChanOffset(registers.Breg);
            int channelNumber = registers.Breg;

//        if (chan_num >= LINKCHANS) { // Internal channel
            if ((channelNumber & 1) == 0) {
                int tmp = RIndexWord(registers.Breg, 0);
                if (tmp == TransputerConstants.NOTPROCESS_P) {
                    WIndexWord(registers.Breg, 0, registers.Wptr);
                } else if (tmp == registers.Wptr) {
                    // Do nothing
                } else {
                    WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.STATE_S, TransputerConstants.READY_P);
                }
            } else { // link-channel
                // TODO: Using physical link
                int token;
                int portNumber = TransputerHelpers.convertChannelToPort(channelNumber);

                // PAR
                inputLinks[portNumber].fromProcessor = TransputerConstants.STATUSENQUIRY;
                // FROM
                token = inputLinks[portNumber].toProcessor;
                while (token == TransputerConstants.NOIO) {
                    token = inputLinks[portNumber].toProcessor;
                    processInputLink(inputLinks[portNumber]);
                }
                inputLinks[portNumber].toProcessor = TransputerConstants.NOIO;

                if (token == TransputerConstants.READYREQUEST) {
                    WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), TransputerConstants.STATE_S, TransputerConstants.READY_P);
                } else if (token == TransputerConstants.READYFALSE) {
                    inputLinks[portNumber].fromProcessor = TransputerConstants.ENABLE;
                    inputLinks[portNumber].Wptr = registers.Wptr;
                }
            }
        }
        registers.Breg = registers.Creg;
    }

    private void initiateWait() {
        WIndexWord(registers.Wptr, TransputerConstants.STATE_S, TransputerConstants.WAITING_P);
        WIndexWord(registers.Wptr, TransputerConstants.IPTR_S, registers.Iptr);
        registers.Iptr -= 1;
        sreg.gotoStartNewProcess = true;
    }

    /**
     * The first step of inserting the current process into the timer queue
     * Areg - time, Breg - previous, Creg - subsequent
     */
    private void timerQueueInsertFirstStep() {
        stdout.printf("insert_first_step\n");
        sreg.timeIns = true;
        WIndexWord(registers.Wptr, TransputerConstants.STATE_S, TransputerConstants.WAITING_P);
        WIndexWord(registers.Wptr, TransputerConstants.TIME_S, registers.Areg);

        registers.Breg = AtWord(TransputerConstants.TIMERBASE, TransputerHelpers.priorityToBit(TransputerHelpers.extractPriority(registers.Wptr)));
        timerQueueInsertTest();
    }

    /**
     * Areg - time, Breg - previous, Creg - subsequent
     */
    private void timerQueueInsertMiddleStep() {
        stdout.printf("insert_middle_step\n");
        registers.Breg = AtWord(registers.Creg, TransputerConstants.TLINK_S);
        timerQueueInsertTest();
    }

    /**
     * Areg - time, Breg - previous, Creg - subsequent
     */
    private void timerQueueInsertTest() {
        stdout.printf("insert_test\n");
        registers.Creg = RIndexWord(registers.Breg, 0);
        if (registers.Creg == TransputerConstants.NOTPROCESS_P) {
            timerQueueInsertFinalStep();
        } else{
            int subsequentTime = RIndexWord(registers.Creg, TransputerConstants.TIME_S);
            boolean laterFlag = Later(registers.Areg, subsequentTime);
            if (!laterFlag) {
                timerQueueInsertFinalStep();
            }
        }
    }

    /**
     * Areg - time, Breg - previous, Creg - subsequent
     */
    private void timerQueueInsertFinalStep() {
        stdout.printf("insert_final_step\n");
        WIndexWord(registers.Breg, 0, TransputerHelpers.extractWorkspacePointer(registers.Wptr));
        WIndexWord(registers.Wptr, TransputerConstants.TLINK_S, registers.Creg);
        WIndexWord(registers.Wptr, TransputerConstants.IPTR_S, registers.Iptr + 1);

        registers.Breg = RIndexWord(TransputerConstants.TIMERBASE, TransputerHelpers.extractPriorityBit(registers.Wptr));
        TNextReg[TransputerHelpers.extractPriorityBit(registers.Wptr)] = RIndexWord(registers.Breg, TransputerConstants.TIME_S);
        TEnabled[TransputerHelpers.extractPriorityBit(registers.Wptr)] = true;

        // This is just so that the simulator agrees with the hdl
        registers.Creg = AtWord(TransputerConstants.TIMERBASE, TransputerHelpers.extractPriorityBit(registers.Wptr));

        sreg.timeIns = false;
        sreg.gotoStartNewProcess = true;
    }

    /**
     * Breg - previous, Creg - subsequent
     */
    private void timerQueueDeleteFirstStep() {
        stdout.printf("delete_first_step\n");
        sreg.timeDel = true;
        TEnabled[TransputerHelpers.extractPriorityBit(registers.Wptr)] = false;

        registers.Breg = AtWord(TransputerConstants.TIMERBASE, TransputerHelpers.extractPriorityBit(registers.Wptr));
//    *prev = TPtrLoc[extractPriority(registers.Wptr)];
        timerQueueDeleteTest();
    }

    /**
     * Breg - previous, Creg - subsequent
     */
    private void timerQueueDeleteMiddleStep() {
        stdout.printf("delete_middle_step\n");
        registers.Breg = AtWord(registers.Creg, TransputerConstants.TLINK_S);
        timerQueueDeleteTest();
    }

    /**
     * Breg - previous, Creg - subsequent
     */
    private void timerQueueDeleteTest() {
        stdout.printf("delete_test\n");
        registers.Creg = RIndexWord(registers.Breg, 0);
        if (registers.Creg == TransputerHelpers.extractWorkspacePointer(registers.Wptr)) {
            timerQueueDeleteFinalStep();
        }
    }

    /**
     * Breg - previous, Creg - subsequent
     */
    private void timerQueueDeleteFinalStep() {
        stdout.printf("delete_final_step\n");
        registers.Creg = RIndexWord(registers.Wptr, TransputerConstants.TLINK_S);
        WIndexWord(registers.Breg, 0, registers.Creg);
        WIndexWord(registers.Wptr, TransputerConstants.TLINK_S, TransputerConstants.TIMENOTSET_P);

        registers.Breg = RIndexWord(TransputerConstants.TIMERBASE, TransputerHelpers.extractPriorityBit(registers.Wptr));
//    *prev = RIndexWord(TPtrLoc[extractPriority(registers.Wptr)], 0);
        if (registers.Breg != TransputerConstants.NOTPROCESS_P) {
            TNextReg[TransputerHelpers.extractPriorityBit(registers.Wptr)] = RIndexWord(registers.Breg, TransputerConstants.TIME_S);
            TEnabled[TransputerHelpers.extractPriorityBit(registers.Wptr)] = true;
        }
        sreg.timeDel = false;
    }

    /**
     * Handle a timer request
     * @param priorityQueue The timer which has made the request
     */
    private void handleTimerRequest(Priority priorityQueue) {
        int priorityBit = TransputerHelpers.priorityToBit(priorityQueue);
        TEnabled[priorityBit] = false;
        //int frontproc = RIndexWord(TPtrLoc[queue_id], 0);
        int frontproc = RIndexWord(TransputerConstants.TIMERBASE, priorityBit);
        int secondproc = RIndexWord(frontproc, TransputerConstants.TLINK_S);
        WIndexWord(frontproc, TransputerConstants.TLINK_S, TransputerConstants.TIMESET_P);
        WIndexWord(TransputerConstants.TIMERBASE, priorityBit, secondproc);
        //WIndexWord(TPtrLoc[queue_id], 0, secondproc);
        if (secondproc != TransputerConstants.NOTPROCESS_P) {
            stdout.printf("secondproc != NOTPROCESS_P\n");
            TNextReg[priorityBit] = RIndexWord(secondproc, TransputerConstants.TIME_S);
            TEnabled[priorityBit] = true;
        }
        int status = RIndexWord(frontproc, TransputerConstants.POINTER_S);
        if (status == TransputerConstants.WAITING_P) {
            WIndexWord(frontproc, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
            runProcess(TransputerHelpers.makeWorkspaceDescriptor(frontproc, priorityQueue));
        }
    }

    public void handshakeInput(int i, int token) throws UnexpectedOverflowException {
        while (token == TransputerConstants.NOIO) {
            token = inputLinks[i].toProcessor;
            processInputLink(inputLinks[i]);
        }
        inputLinks[i].toProcessor = TransputerConstants.NOIO;
    }

    public void handshakeOutput(int token) throws UnexpectedOverflowException {
        while (token == TransputerConstants.NOIO) {
            token = outputLink.toProcessor;
            processOutputLink();
        }
        outputLink.toProcessor = TransputerConstants.NOIO;
    }

    /**
     * Execute a secondary instruction, i.e. one where the actual opcode is in the operand register
     */
    private void executeSecondaryInstruction() throws UnexpectedOverflowException {
        byte opcode = (byte)(registers.Oreg & 0xFF);
        int tmp;
        boolean laterFlag;
        int shift_val;
        int op0, op1;
        Process processToUpdate;

        registers.Oreg = 0;

        switch(opcode) {
            case(TransputerConstants.REV):
                stdout.printf("rev\n");
                tmp = registers.Areg;
                registers.Areg = registers.Breg;
                registers.Breg = tmp;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.ADD):
                stdout.printf("add\n");
                overflowCheck(TransputerConstants.ADD, registers.Breg, registers.Areg);
                registers.Areg = registers.Areg + registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                // TODO: OverflowCheck()
                break;
            case(TransputerConstants.SUB):
                stdout.printf("sub\n");
                overflowCheck(TransputerConstants.SUB, registers.Breg, registers.Areg);
                registers.Areg = registers.Breg - registers.Areg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                // TODO: OverflowCheck()
                break;
            case(TransputerConstants.MUL):
                stdout.printf("mul\n");
                overflowCheck(TransputerConstants.MUL, registers.Breg, registers.Areg);
                registers.Areg = registers.Breg * registers.Areg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                // TODO: OverflowCheck()
                break;
            case(TransputerConstants.DIV):
                stdout.printf("div\n");
                if ((registers.Breg == TransputerConstants.MININT && registers.Areg == -1) ||
                        registers.Areg == 0) {
                    setErrorFlag();
                } else {
                    registers.Areg = registers.Breg / registers.Areg;
                }
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.DIFF):
                stdout.printf("diff\n");
                registers.Areg = (registers.Breg - registers.Areg);
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.SUM):
                stdout.printf("sum\n");
                registers.Areg = registers.Breg + registers.Areg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.AND):
                stdout.printf("and\n");
                registers.Areg = registers.Areg & registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.OR):
                stdout.printf("or\n");
                registers.Areg = registers.Areg | registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.XOR):
                stdout.printf("xor\n");
                registers.Areg = registers.Areg ^ registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.NOT):
                stdout.printf("not\n");
                registers.Areg = ~registers.Areg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.SHL):
                stdout.printf("shl\n");
                // TODO: verify this works correctly
                shift_val = registers.Areg;
                if (shift_val < TransputerConstants.BITSPERWORD) {
                    registers.Areg = registers.Breg << registers.Areg;
                } else {
                    registers.Areg = 0;
                }
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.SHR):
                stdout.printf("shr\n");
                // Logical shift right
                shift_val = registers.Areg;
                if (shift_val < TransputerConstants.BITSPERWORD) {
                    registers.Areg = registers.Breg >> registers.Areg;
                } else {
                    registers.Areg = 0;
                }
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.GT):
                stdout.printf("gt\n");
                if (registers.Breg > registers.Areg) {
                    registers.Areg = 1; // TRUE
                } else {
                    registers.Areg = 0; // FALSE
                }
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.LEND):
                stdout.printf("lend\n");
                // registers.Creg holds the loop counter
                registers.Creg = RIndexWord(registers.Breg, 1);
                registers.Creg = registers.Creg - 1;
                WIndexWord(registers.Breg, 1, registers.Creg);
                if (registers.Creg > 0) {
                    registers.Creg = RIndexWord(registers.Breg, 0);
                    registers.Creg = registers.Creg + 1;
                    WIndexWord(registers.Breg, 0, registers.Creg);
                    registers.Iptr = atByte(registers.Iptr + 1, -(registers.Areg));
                } else if (registers.Creg <= 0) {
                    registers.Iptr += 1;
                }
                // TODO: TimeSlice()
            case(TransputerConstants.BSUB):
                stdout.printf("bsub\n");
                registers.Areg = atByte(registers.Areg, registers.Breg);
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.WSUB):
                stdout.printf("wsub\n");
                registers.Areg = AtWord(registers.Areg, registers.Breg);
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.BCNT):
                stdout.printf("bcnt\n");
                registers.Areg = registers.Areg * TransputerConstants.BYTESPERWORD;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.WCNT):
                stdout.printf("wcnt\n");
                registers.Creg = registers.Breg;
                registers.Breg = TransputerHelpers.extractByteSelector(registers.Areg);
                registers.Areg = registers.Areg >> TransputerConstants.BYTESELLEN;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.LDPI):
                stdout.printf("ldpi\n");
                registers.Areg = atByte(registers.Iptr + 1, registers.Areg);
                registers.Iptr += 1;
                break;
            case(TransputerConstants.MOVE):
                stdout.printf("move\n");
                blockMoveFirstStep();
                registers.Iptr += 1;
                break;
            case(TransputerConstants.GCALL):
                stdout.printf("gcall\n");
                tmp = registers.Areg;
                registers.Areg = registers.Iptr + 1;
                registers.Iptr = tmp;
                break;
            case(TransputerConstants.GAJW):
                stdout.printf("gajw\n");
                processToUpdate = debuggerState.processes.stream()
                        .filter(p -> p.status == ProcessStatus.RUNNING)
                        .filter(p -> p.getCurrentWptr() == registers.Wptr)
                        .findFirst().get();
                tmp = TransputerHelpers.extractWorkspacePointer(registers.Wptr);
                registers.Wptr = TransputerHelpers.extractWorkspacePointer(registers.Areg) | TransputerHelpers.extractPriorityBit(registers.Wptr);
                registers.Areg = tmp & 0xFFFFFFFC;
                registers.Iptr += 1;
                processToUpdate.updateWptr(registers.Wptr);
                break;
            case(TransputerConstants.RET):
                stdout.printf("ret\n");
                processToUpdate = debuggerState.processes.stream()
                        .filter(p -> p.status == ProcessStatus.RUNNING)
                        .filter(p -> p.getCurrentWptr() == registers.Wptr)
                        .findFirst().get();
                registers.Iptr = RIndexWord(registers.Wptr, 0);
                registers.Wptr = AtWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), 4) | TransputerHelpers.extractPriorityBit(registers.Wptr);
                processToUpdate.updateWptr(registers.Wptr);
                break;
            case(TransputerConstants.STARTP):
                stdout.printf("startp\n");
                registers.Iptr += 1;
                tmp = atByte(registers.Iptr, registers.Breg);
                WIndexWord(registers.Areg, TransputerConstants.IPTR_S, tmp);
                runProcess(registers.Areg | TransputerHelpers.extractPriorityBit(registers.Wptr));
                break;
            case(TransputerConstants.ENDP):
                stdout.printf("endp\n");
                processToUpdate = debuggerState.processes.stream()
                        .filter(p -> p.status == ProcessStatus.RUNNING)
                        .filter(p -> p.getCurrentWptr() == registers.Wptr)
                        .findFirst().get();
                tmp = RIndexWord(registers.Areg, 1);
                if (tmp == 1) {
                    // continue as process with waiting workspace Areg
                    registers.Iptr = RIndexWord(registers.Areg, 0);
                    registers.Wptr = registers.Areg | TransputerHelpers.extractPriorityBit(registers.Wptr);
                    processToUpdate.updateWptr(registers.Wptr);
                } else {
                    // start next waiting process
                    stdout.printf("ENDP elseA\n");
                    WIndexWord(registers.Areg, 1, tmp - 1);
                    sreg.gotoStartNewProcess = true;
                    processToUpdate.status = ProcessStatus.TERMINATED;
                }
                break;
            case(TransputerConstants.RUNP):
                stdout.printf("runp\n");
                registers.Iptr += 1;
                runProcess(registers.Areg);
                break;
            case(TransputerConstants.STOPP):
                stdout.printf("stopp\n");
                registers.Iptr += 1;
                WIndexWord(registers.Wptr, TransputerConstants.IPTR_S, registers.Iptr);
                sreg.gotoStartNewProcess = true;
                break;
            case(TransputerConstants.LDPRI):
                stdout.printf("ldpri\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = TransputerHelpers.extractPriorityBit(registers.Wptr);
                registers.Iptr += 1;
                break;
            case(TransputerConstants.MINT):
                stdout.printf("mint\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = TransputerConstants.MININT;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.ALT):
                stdout.printf("alt\n");
                registers.Iptr += 1;
                WIndexWord(registers.Wptr, TransputerConstants.STATE_S, TransputerConstants.ENABLING_P);
                break;
            case(TransputerConstants.ALTWT):
                stdout.printf("altwt\n");
                registers.Iptr += 1;
                WIndexWord(registers.Wptr, 0, TransputerConstants.NONESELECTED_O);
                registers.Areg = RIndexWord(registers.Wptr, TransputerConstants.STATE_S);
                if (registers.Areg == TransputerConstants.READY_P) {
                    // Do nothing
                } else {
                    initiateWait();
                }
                break;
            case(TransputerConstants.ALTEND):
                stdout.printf("altend\n");
                registers.Iptr += 1;
                tmp = RIndexWord(registers.Wptr, 0);
                registers.Iptr = atByte(registers.Iptr, tmp);
                break;
            case(TransputerConstants.LDTIMER):
                stdout.printf("ldtimer\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = ClockReg[TransputerHelpers.extractPriorityBit(registers.Wptr)];
                registers.Iptr += 1;
                break;
            case(TransputerConstants.TIN):
                stdout.printf("tin\n");
                laterFlag = Later(ClockReg[TransputerHelpers.extractPriorityBit(registers.Wptr)],
                        registers.Areg);
                if (!laterFlag) {
                    registers.Areg += 1;
                    timerQueueInsertFirstStep();
                }
                break;
            case(TransputerConstants.TALT):
                stdout.printf("talt\n");
                WIndexWord(registers.Wptr, TransputerConstants.TLINK_S, TransputerConstants.TIMENOTSET_P);
                WIndexWord(registers.Wptr, TransputerConstants.STATE_S, TransputerConstants.ENABLING_P);
                registers.Iptr += 1;
                break;
            case(TransputerConstants.TALTWT):
                stdout.printf("taltwt\n");
                registers.Iptr += 1;
                WIndexWord(registers.Wptr, 0, TransputerConstants.NONESELECTED_O);
                registers.Creg = RIndexWord(registers.Wptr, TransputerConstants.STATE_S);
                if (registers.Creg == TransputerConstants.READY_P) {
                    WIndexWord(registers.Wptr, TransputerConstants.TIME_S, ClockReg[TransputerHelpers.extractPriorityBit(registers.Wptr)]);
                } else {
                    registers.Breg = RIndexWord(registers.Wptr, TransputerConstants.TLINK_S);
                    if (registers.Breg == TransputerConstants.TIMENOTSET_P) {
                        initiateWait();
                    } else if (registers.Breg == TransputerConstants.TIMESET_P) {
                        registers.Areg = RIndexWord(registers.Wptr, TransputerConstants.TIME_S);
                        laterFlag = Later(ClockReg[TransputerHelpers.extractPriorityBit(registers.Wptr)],
                                registers.Areg);
                        if (laterFlag) {
                            WIndexWord(registers.Wptr, TransputerConstants.STATE_S, TransputerConstants.READY_P);
                            WIndexWord(registers.Wptr, TransputerConstants.TIME_S, ClockReg[TransputerHelpers.extractPriorityBit(registers.Wptr)]);
                        } else {
                            registers.Areg += 1;
                            registers.Iptr -= 1;
                            timerQueueInsertFirstStep();
                        }
                    }
                }
                break;
            case(TransputerConstants.ENBS):
                stdout.printf("enbs\n");
                break;
            case(TransputerConstants.DISS):
                stdout.printf("diss\n");
                break;
            case(TransputerConstants.ENBC):
                stdout.printf("enbc\n");
                enableChannel();
                registers.Iptr += 1;
                break;
            case(TransputerConstants.DISC):
                stdout.printf("disc\n");
                disableChannel();
                registers.Iptr += 1;
                break;
            case(TransputerConstants.ENBT):
                stdout.printf("enbt\n");
                if (registers.Areg != 0 /*FALSE*/) {
                    tmp = RIndexWord(registers.Wptr, TransputerConstants.TLINK_S);
                    if (tmp == TransputerConstants.TIMENOTSET_P) {
                        WIndexWord(registers.Wptr, TransputerConstants.TLINK_S, TransputerConstants.TIMESET_P);
                        WIndexWord(registers.Wptr, TransputerConstants.TIME_S, registers.Breg);
                    } else if (tmp == TransputerConstants.TIMESET_P) {
                        tmp = RIndexWord(registers.Wptr, TransputerConstants.TIME_S);
                        laterFlag = Later(tmp, registers.Breg);
                        if (laterFlag) {
                            WIndexWord(registers.Wptr, TransputerConstants.TIME_S, registers.Breg);
                        }
                    }
                } else if(registers.Areg == 0 /*FALSE*/) {
                    // Do nothing...
                }
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.DIST):
                stdout.printf("dist\n");
                registers.Iptr += 1;
                if (registers.Breg != 0 /*FALSE*/) {
                    registers.Oreg = RIndexWord(registers.Wptr, TransputerConstants.TLINK_S);
                    if (registers.Oreg == TransputerConstants.TIMENOTSET_P) {
                        registers.Areg = 0 /*FALSE*/;
                    } else if (registers.Oreg == TransputerConstants.TIMESET_P) {
                        registers.Oreg = RIndexWord(registers.Wptr, TransputerConstants.TIME_S);
                        laterFlag = Later(registers.Oreg, registers.Creg);
                        if (laterFlag) {
                            isThisSelProcess();
                        } else {
                            registers.Areg = 0; // FALSE
                        }
                    } else {
                        timerQueueDeleteFirstStep();
                        registers.Areg = 0; // FALSE
                    }
                } else if (registers.Breg == 0 /*FALSE*/) {
                    registers.Areg = 0; // FALSE
                }
                registers.Oreg = 0;
                break;

            case(TransputerConstants.CSUB):
                stdout.printf("csub\n");
                op0 = registers.Areg;
                op1 = registers.Breg;
                if(op1 >= op0) {
                    sreg.errorFlag = true;
                }
                registers.Areg = registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.CCNT):
                stdout.printf("ccnt\n");
                op0 = registers.Areg;
                op1 = registers.Breg;
                if(op1 == 0 || op1 > op0) {
                    sreg.errorFlag = true;
                }
                registers.Areg = registers.Breg;
                registers.Breg = registers.Creg;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.TESTERR):
                stdout.printf("testerr\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = !sreg.errorFlag ? 1 /*TRUE*/ : 0 /*FALSE*/;
                sreg.errorFlag = false;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.SETERR):
                stdout.printf("seterr\n");
                registers.Iptr += 1;
                sreg.errorFlag = true;
                break;
            case(TransputerConstants.STOPERR):
                stdout.printf("stoperr\n");
                registers.Iptr += 1;
                if (sreg.errorFlag) {
                    WIndexWord(registers.Wptr, TransputerConstants.IPTR_S, registers.Iptr);
                    sreg.gotoStartNewProcess = true;
                }
                break;
            case(TransputerConstants.CLRHALTERR):
                stdout.printf("clrhalterr\n");
                registers.Iptr += 1;
                sreg.haltOnErr = false;
                break;
            case(TransputerConstants.SETHALTERR):
                stdout.printf("sethalterr\n");
                registers.Iptr += 1;
                sreg.haltOnErr = true;
                break;
            case(TransputerConstants.TESTHALTERR):
                stdout.printf("testhalterr");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = (sreg.haltOnErr) ? 1 /*TRUE*/ : 0 /*FALSE*/;
                registers.Iptr += 1;
                break;

            // This is very specific and complicates matters with the input and
            // output link so we decided against using it.
            // It is intended to be used in setups that provide special
            // redundancy.
            // The commented out implementation assumes that inputLinks have
            // ports 0-14 and that the outputLink has port 15.
            case(TransputerConstants.RESETCH):
                stdout.printf("resetch\n");
////        int chan_num;
//        tmp = RIndexWord(registers.Areg, 0);
//        WIndexWord(registers.Areg, 0, NOTPROCESS_P);
//        //chan_num = ChanOffset(registers.Areg);
//        chan_num = registers.Areg;
//
//        // Hard channel
////        if (chan_num < LINKCHANS)
//        if (chan_num & 1) {
//            int token;
//            int port_no = convertChannelToPort(chan_num);
//            // PAR
//
//            // Output channel
//            if (port_no == IN_PORTS) {
//                outputLink.fromProcessor = RESETREQUEST;
//                handshakeOutput(token);
//            } else if (port_no < IN_PORTS) {
//                inputLinks[port_no].fromProcessor = RESETREQUEST;
//                handshakeInput(port_no, token);
//            }
//        }
//
//        registers.Areg = tmp;
//        registers.Iptr += 1;
                break;
            case(TransputerConstants.STHF):
                stdout.printf("sthf\n");
                break;
            case(TransputerConstants.STLF):
                stdout.printf("stlf\n");
                break;
            case(TransputerConstants.STTIMER):
                stdout.printf("sttimer\n");
                break;
            case(TransputerConstants.STHB):
                stdout.printf("sthb\n");
                break;
            case(TransputerConstants.STLB):
                stdout.printf("stlb\n");
                break;
            case(TransputerConstants.SAVEH):
                stdout.printf("saveh\n");
                break;
            case(TransputerConstants.SAVEL):
                stdout.printf("savel\n");
                break;
            case(TransputerConstants.IN):
                stdout.printf("in\n");
                performInput();
                break;
            case(TransputerConstants.OUT):
                stdout.printf("out\n");
                performOutput();
                break;
            case(TransputerConstants.OUTWORD):
                stdout.printf("outword\n");
                WIndexWord(registers.Wptr, 0, registers.Areg);
                registers.Areg = TransputerConstants.BYTESPERWORD;
                registers.Creg = TransputerHelpers.extractWorkspacePointer(registers.Wptr);
                performOutput();
                break;
            default:
                System.err.printf("Instruction opcode '%02X' not implemented at Iptr '%08X'\n", opcode, registers.Iptr);
                System.exit(1);
        }
    }

    public void printNextInstruction(PrintWriter output) {
        printMemory(1, 1, "i", registers.Iptr, output);
    }

    public void printSRegisters(PrintWriter output) {
        output.printf("ID:%d", id);
        if (sreg.errorFlag) {
            output.printf("  ErrorFlag:TRUE");
        } else {
            output.printf("  ErrorFlag:FALSE");
        }

        if (sreg.moveBit) {
            output.printf("  MoveBit:TRUE");
        } else {
            output.printf("  MoveBit:FALSE");
        }

        if (sreg.haltOnErr) {
            output.printf("  HaltOnError:TRUE");
        } else {
            output.printf("  HaltOnError:FALSE");
        }

        if (sreg.gotoStartNewProcess) {
            output.printf("  GotoSNP:TRUE");
        } else {
            output.printf("  GotoSNP:FALSE");
        }

        if (sreg.ioBit) {
            output.printf("  IOBit:TRUE");
        } else {
            output.printf("  IOBit:FALSE");
        }

        if (sreg.timeIns) {
            output.printf("  TimeIns:TRUE");
        } else {
            output.printf("  TimeIns:FALSE");
        }

        if (sreg.timeDel) {
            output.printf("  TimeDel:TRUE");
        } else {
            output.printf("  TimeDel:FALSE");
        }

        if (sreg.distAndIns) {
            output.printf("  DistAndIns:TRUE\n");
        } else {
            output.printf("  DistAndIns:FALSE\n");
        }
    }

    public void printCRegisters(PrintWriter output) {
        output.printf("## Transputer %d\n", id);
        output.printf("==Process queue==\n");
        output.printf("%8s\t%8s\t%8s\n", "Priority", "FPtr", "BPtr");
        output.printf("%8d\t%08X\t%08X\n", 0, FptrReg[0], BptrReg[0]);
        output.printf("%8d\t%08X\t%08X\n\n", 1, FptrReg[1], BptrReg[1]);

        output.printf("==Process clock == ClockReg==\n");
        output.printf("%8s\t%8s\n", "Priority", "Value");
        output.printf("%8d\t%08X\n", 0, ClockReg[0]);
        output.printf("%8d\t%08X\n\n", 1, ClockReg[1]);

        output.printf("==Other registers==\n");
        output.printf("%12s\t%8s\t%8s\n", "Name", "Priority", "Value");
//      output.printf("%12s\t%8d\t%08X\n", "TPtrLock", 0, TPtrLoc[0]);
//      output.printf("%12s\t%8d\t%08X\n", "TPtrLock", 1, TPtrLoc[1]);
        output.printf("%12s\t%8d\t%08X\n", "TNextReg", 0, TNextReg[0]);
        output.printf("%12s\t%8d\t%08X\n", "TNextReg", 1, TNextReg[1]);
        output.printf("%12s\t%8d\t%08b\n", "TEnabled", 0, TEnabled[0]);
        output.printf("%12s\t%8d\t%08b\n\n", "TEnabled", 1, TEnabled[1]);

        output.printf("Ereg:%08X\tBMBuffer:%08X\n", Ereg, BMbuffer);
    }

    public void printProcessList(PrintWriter output) {
        output.printf("## Transputer %d\n", id);
        output.printf("ID Wptr    \t\tStatus\n");
        int i = 0;
        for (Process process : debuggerState.processes) {
            output.printf("%02d 0x%08X\t%s\n", i++, process.getCurrentWptr(), process.status.name());
        }
    }

    public void printBreakpoints(PrintWriter output) {
        int i;
        boolean has_breaks = false;
        output.printf("## Transputer %d\n", id);
        output.printf("%8s\t%8s\t%8s\n", "Hex addr", "Dec addr", "Value");
        for (i = 0; i < TransputerConstants.MEMSIZE; i++) {
            if (debuggerState.breakpoints.contains(i)) {
                output.printf("%08X\t%8d\t%02X\n", i, i, mem[i]);
                has_breaks = true;
            }
        }
        if(!has_breaks) {
            output.printf("There are no set breakpoints\n");
        }
    }

    public void unsetBreakpoint(int addr, PrintWriter output) {
        output.printf("## Transputer %d\n", id);
        if (addr < 0 || addr >= TransputerConstants.MEMSIZE) {
            output.printf("Invalid address\n");
        } else if (!debuggerState.breakpoints.contains(addr)) {
            output.printf("No breakpoint found at Hex_addr:%08X Dec_addr:%-8d Value:%08X\n",
                    addr, addr, mem[addr]);
        } else {
            debuggerState.breakpoints.remove(addr);
            output.printf("Unset breakpoint successful\n");
        }
    }

    public void setBreakpoint(int addr, PrintWriter output) {
        output.printf("## Transputer %d\n", id);
        if (addr < 0 || addr >= TransputerConstants.MEMSIZE) {
            output.printf("Invalid address\n");
        } else if (debuggerState.breakpoints.contains(addr)) {
            output.printf("Breakpoint already exists at Hex_addr:%08X Dec_addr:%-8d Value:%08X\n",
                    addr, addr, mem[addr]);
        } else {
            debuggerState.breakpoints.add(addr);
            output.printf("Set breakpoint successful\n");
        }
    }

    /**
     * Executes a primary instruction, i.e. an instruction which uses the operand as a parameter
     */
    private void executePrimaryInstruction() throws UnexpectedOverflowException {
        byte opcode = TransputerHelpers.extractOpcode(mem[registers.Iptr]);
        byte operand = TransputerHelpers.extractDirectOperand(mem[registers.Iptr]);
        Process processToUpdate;

        registers.Oreg = operand | registers.Oreg;

        stdout.printf("Executed ");
        switch(opcode) {
            case(TransputerConstants.PFIX):
                stdout.printf("pfix\n");
                registers.Oreg = registers.Oreg << 4;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.NFIX):
                stdout.printf("nfix\n");
                registers.Oreg = (~registers.Oreg) << 4;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.OPR):
                stdout.printf("opr ");
                executeSecondaryInstruction();
                break;
            case(TransputerConstants.LDC):
                stdout.printf("ldc\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = registers.Oreg;
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.LDL):
                stdout.printf("ldl\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = RIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), registers.Oreg);
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.STL):
                stdout.printf("stl\n");
                WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), registers.Oreg, registers.Areg);
                registers.Areg = registers.Breg;
                registers.Breg = registers.Creg;
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.LDLP):
                stdout.printf("ldlp\n");
                registers.Creg = registers.Breg;
                registers.Breg = registers.Areg;
                registers.Areg = AtWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), registers.Oreg);
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.ADC):
                stdout.printf("adc\n");
                overflowCheck(TransputerConstants.ADD, registers.Oreg, registers.Areg);
                registers.Areg = registers.Areg + registers.Oreg;
                // TODO: OverflowCheck();
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.EQC):
                stdout.printf("eqc\n");
                if (registers.Areg == registers.Oreg) {
                    registers.Areg = 1;
                }
                else {
                    registers.Areg = 0;
                }
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.J):
                stdout.printf("j\n");
                registers.Iptr = atByte(registers.Iptr + 1, registers.Oreg);
                // TODO
                //TimeSlice();
                registers.Oreg = 0;
                break;
            case(TransputerConstants.CJ):
                stdout.printf("cj\n");
                if (registers.Areg == 0) {
                    registers.Iptr = atByte(registers.Iptr + 1, registers.Oreg);
                }
                else {
                    registers.Areg = registers.Breg;
                    registers.Breg = registers.Creg;
                    registers.Creg = 0;
                    registers.Iptr += 1;
                }
                registers.Oreg = 0;
                break;
            case(TransputerConstants.LDNL):
                stdout.printf("ldnl\n");
                registers.Areg = RIndexWord(registers.Areg, registers.Oreg);
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.STNL):
                stdout.printf("stnl\n");
                WIndexWord(registers.Areg, registers.Oreg, registers.Breg);
                registers.Areg = registers.Creg;
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.LDNLP):
                stdout.printf("ldnlp\n");
                // TODO: Not sure if AtWord is word aligned only!
                registers.Areg = AtWord(registers.Areg, registers.Oreg);
                registers.Oreg = 0;
                registers.Iptr += 1;
                break;
            case(TransputerConstants.CALL):
                stdout.printf("call\n");
                WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), -1, registers.Creg);
                WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), -2, registers.Breg);
                WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), -3, registers.Areg);
                WIndexWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), -4, registers.Iptr + 1);
                processToUpdate = debuggerState.processes.stream()
                        .filter(p -> p.status == ProcessStatus.RUNNING)
                        .filter(p -> p.getCurrentWptr() == registers.Wptr)
                        .findFirst().get();
                registers.Areg = registers.Iptr + 1;
                registers.Wptr = AtWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), -4) | TransputerHelpers.extractPriorityBit(registers.Wptr);
                registers.Iptr = atByte(registers.Iptr + 1, registers.Oreg);
                registers.Oreg = 0;
                processToUpdate.updateWptr(registers.Wptr);
                break;
            case(TransputerConstants.AJW):
                stdout.printf("ajw\n");
                processToUpdate = debuggerState.processes.stream()
                        .filter(p -> p.status == ProcessStatus.RUNNING)
                        .filter(p -> p.getCurrentWptr() == registers.Wptr)
                        .findFirst().get();
                registers.Wptr = AtWord(TransputerHelpers.extractWorkspacePointer(registers.Wptr), registers.Oreg) | TransputerHelpers.extractPriorityBit(registers.Wptr);
                registers.Oreg = 0;
                registers.Iptr += 1;
                processToUpdate.updateWptr(registers.Wptr);
                break;
            default:
                System.err.printf("Instruction opcode '%02X' not implemented at Iptr '%08X'\n", opcode, registers.Iptr);
                System.exit(1);
        }

    }

    /**
     * Handle a request from an input channel to the processor.
     */
    private void handleInputChannelRequest(InputLink inputLink, int request) {
        inputLink.toProcessor = TransputerConstants.NOIO;
        if (request == TransputerConstants.RUNREQUEST) {
            int channelContent;
            inputLink.fromProcessor = TransputerConstants.ACKRUN;
            channelContent = inputLink.WptrToProcessor;

            if (channelContent == TransputerConstants.NOTPROCESS_P) {
            } else {
                inputLink.Wptr = TransputerConstants.NOTPROCESS_P;
                runProcess(channelContent);
            }
        } else if (request == TransputerConstants.READYREQUEST) {
            int channelContent, procPtr, status;
            // Needed to make the cancellable ReadyRequest work
            inputLink.fromProcessor = TransputerConstants.ACKREADY;
            channelContent = inputLink.WptrToProcessor;
            procPtr = TransputerHelpers.extractWorkspacePointer(channelContent);
            status = RIndexWord(procPtr, TransputerConstants.POINTER_S);
            if (status == TransputerConstants.ENABLING_P) {
                WIndexWord(procPtr, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
            } else if (status == TransputerConstants.READY_P) {
                // SKIP
            } else if (status == TransputerConstants.WAITING_P) {
                WIndexWord(procPtr, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
                runProcess(channelContent);
            }
        }
    }

    /**
     * Handle a request from an output channel to the processor.
     */
    private void handleOutputChannelRequest(OutputLink outputLink, int request) {
        outputLink.toProcessor = TransputerConstants.NOIO;
        if (request == TransputerConstants.RUNREQUEST) {
            int channelContent;
            outputLink.fromProcessor = TransputerConstants.ACKRUN;
            channelContent = outputLink.WptrToProcessor;

            if (channelContent == TransputerConstants.NOTPROCESS_P) {
            } else {
                outputLink.Wptr = TransputerConstants.NOTPROCESS_P;
                runProcess(channelContent);
            }
        }
        // READYREQUEST is never sent from processOutputLink. It has been
        // implemented nevertheless to be consistent with
        // handleInputChannelRequest
        else if (request == TransputerConstants.READYREQUEST) {
            int channelContent, procPtr, status;
            // Needed to make the cancellable ReadyRequest work
            outputLink.fromProcessor = TransputerConstants.ACKREADY;
            channelContent = outputLink.WptrToProcessor;
            procPtr = TransputerHelpers.extractWorkspacePointer(channelContent);
            status = RIndexWord(procPtr, TransputerConstants.POINTER_S);
            if (status == TransputerConstants.ENABLING_P) {
                WIndexWord(procPtr, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
            } else if (status == TransputerConstants.READY_P) {
                // SKIP
            } else if (status == TransputerConstants.WAITING_P) {
                WIndexWord(procPtr, TransputerConstants.POINTER_S, TransputerConstants.READY_P);
                runProcess(channelContent);
            }
        }
    }

    /**
     * Check to see if the input or output ports are transmitting or receiving data
     */
    private boolean checkChannels() {
        int i;
        for (i = 0; i < TransputerConstants.IN_PORTS; i++) {
            if (inputLinks[i].toProcessor != TransputerConstants.NOIO) {
                handleInputChannelRequest(inputLinks[i], inputLinks[i].toProcessor);
                return true;
            }
        }

        if (outputLink.toProcessor != TransputerConstants.NOIO) {
            handleOutputChannelRequest(outputLink, outputLink.toProcessor);
            return true;
        }
        return false;
    }

    /**
     * Execute a single transputer instruction
     * @return true if the process is valid, false otherwise
     */
    public boolean performStep() throws UnexpectedOverflowException {
        // completed indicates if current instruction has terminated
        boolean completed = sreg.gotoStartNewProcess || sreg.ioBit || sreg.moveBit || sreg.timeIns || sreg.timeDel;

        if (sreg.gotoStartNewProcess) {
            stdout.printf("performStep => startNewProcess\n");
            startNewProcess();
            return true;
            // For the timers I think we need to condition on TEnabled as well because
            // otherwise we might find ourselves in an infinite loop comparing the
            // "head" of the timer list to the clock
        } else if (TEnabled[0] && TransputerHelpers.extractPriorityBit(registers.Wptr) == 0 && completed &&
                Later(ClockReg[0], TNextReg[0])) {
            stdout.printf("performStep => handleTimerRequest(0), PRIORITY(0)\n");
            handleTimerRequest(HIGH);
            return true;
        } else if (completed && checkChannels()) {
            return true;
        } else if (TEnabled[0] && TransputerHelpers.extractPriorityBit(registers.Wptr) == 1 &&
                Later(ClockReg[0], TNextReg[0])) {
            stdout.printf("performStep => transputer_handle_timer_reg(0), PRIORITY(1)\n");
            handleTimerRequest(HIGH);
            return true;
        } else if (TEnabled[1] && TransputerHelpers.extractPriorityBit(registers.Wptr) == 1 && completed &&
                Later(ClockReg[1], TNextReg[1])) {
            stdout.printf("performStep => transputer_handle_timer_reg(1), PRIORITY(1)\n");
            handleTimerRequest(LOW);
            return true;
        }

        if (TransputerHelpers.extractWorkspacePointer(registers.Wptr) == TransputerConstants.NOTPROCESS_P) {
            // Not sure how to use this
            stdout.printf("WARNING: performStep() 'Wptr' == NOTPROCESS_P\n");
            return false;
        }

        if (sreg.timeDel) {
            timerQueueDeleteMiddleStep();
        } else if (sreg.timeIns) {
            timerQueueInsertMiddleStep();
        } else if (sreg.moveBit) {
            blockMoveMiddleStep();
        } else {
            executePrimaryInstruction();
        }

        return true;
    }

    public void incrementClock(long loopCount) {
        // Increment high priority clock by 1
        ClockReg[0] += 1;

        // Increment low priority clock by
        if (loopCount % 4 == 0) {
            ClockReg[1] += 1;
        }
    }

    public void logState(long index, PrintWriter wr) {
        // Dump register values to a file
        wr.printf("%10X %08X %08X %08X %08X %08X %08X %01b %01b\n",
                index, registers.Areg, registers.Breg, registers.Creg,
                registers.Oreg, registers.Iptr, registers.Wptr,
                sreg.errorFlag, sreg.haltOnErr);
    }

    public void logSched(long index, PrintWriter wr) {
        // Log the state of the normal registers
        logState(index, wr);
        // Log the state of the shceduler registers and status register bits
        wr.printf("%08X %08X %08X %08X %08X %01b %01b %01b\n",
                FptrReg[0], BptrReg[0], FptrReg[1], BptrReg[1],
                Ereg, sreg.gotoStartNewProcess, sreg.moveBit,
                sreg.ioBit);
    }

    public void logTimer(long index, PrintWriter wr) {
        // Log state of the normal and scheduler registers
        logSched(index, wr);
        // Log state of the timer registers and status register bits
        wr.printf("%08X %08X %08b %08b %01b %01b\n",
                TNextReg[0], TNextReg[1], TEnabled[0],
                TEnabled[1], sreg.timeIns, sreg.timeDel);
    }
}
