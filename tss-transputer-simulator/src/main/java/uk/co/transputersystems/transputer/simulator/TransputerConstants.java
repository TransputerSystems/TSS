package uk.co.transputersystems.transputer.simulator;

/**
 * Created by Edward on 28/02/2016.
 */
public class TransputerConstants {
    public static final int MEMSIZE = 10352;

    public static final int BYTESPERWORD = 4;
    public static final int BITSPERBYTE = 8;
    public static final int BITSPERWORD = (BYTESPERWORD * BITSPERBYTE);
    public static final int MININT = (-(1 << (BITSPERWORD - 1)));
    public static final int MAXINT = ((1 << (BITSPERWORD - 1)) - 1);

    // TODO: Not sure what this is!!!!!!!
    public static final int LINKCHANS = 4;

    public static final int BYTESELLEN = 2;

    // Opcodes primary instructions
    public static final byte PFIX = 0x2;
    public static final byte NFIX = 0x6;
    public static final byte OPR = 0xF;
    public static final byte LDC = 0x4;
    public static final byte LDL = 0x7;
    public static final byte STL = 0xD;
    public static final byte LDLP = 0x1;
    public static final byte ADC = 0x8;
    public static final byte EQC = 0xC;
    public static final byte J = 0x0;
    public static final byte CJ = 0xA;
    public static final byte LDNL = 0x3;
    public static final byte STNL = 0xE;
    public static final byte LDNLP = 0x5;
    public static final byte CALL = 0x9;
    public static final byte AJW = 0xB;

    // Opcodes secondary instructions
    public static final byte REV = 0x00;
    public static final byte ADD = 0x05;
    public static final byte SUB = 0x0C;
    public static final byte MUL = 0x53;
    public static final byte DIV = 0x2C;
    public static final byte AND = 0x46;
    public static final byte OR = 0x4B;
    public static final byte XOR = 0x33;
    public static final byte NOT = 0x32;
    public static final byte SHL = 0x41;
    public static final byte SHR = 0x40;
    public static final byte GT = 0x09;
    public static final byte LEND = 0x21;
    public static final byte BSUB = 0x02;
    public static final byte WSUB = 0x0A;
    public static final byte BCNT = 0x34;
    public static final byte WCNT = 0x3F;
    public static final byte LDPI = 0x1B;
    public static final byte MOVE = 0x4A;
    public static final byte IN = 0x07;
    public static final byte OUT = 0x0B;
    public static final byte OUTWORD = 0x0F;
    public static final byte GCALL = 0x06;
    public static final byte GAJW = 0x3C;
    public static final byte RET = 0x20;
    public static final byte STARTP = 0x0D;
    public static final byte ENDP = 0x03;
    public static final byte RUNP = 0x39;
    public static final byte STOPP = 0x15;
    public static final byte LDPRI = 0x1E;
    public static final byte LDTIMER = 0x22;
    public static final byte TIN = 0x2B;
    public static final byte ALT = 0x43;
    public static final byte ALTWT = 0x44;
    public static final byte ALTEND = 0x45;
    public static final byte TALT = 0x4E;
    public static final byte TALTWT = 0x51;
    public static final byte ENBS = 0x49;
    public static final byte DISS = 0x30;
    public static final byte ENBC = 0x48;
    public static final byte DISC = 0x2F;
    public static final byte ENBT = 0x47;
    public static final byte DIST = 0x2E;
    public static final byte RESETCH = 0x12;
    public static final byte STHF = 0x18;
    public static final byte STLF = 0x1C;
    public static final byte STTIMER = 0x54;
    public static final byte STHB = 0x50;
    public static final byte STLB = 0x17;
    public static final byte SAVEH = 0x3E;
    public static final byte SAVEL = 0x3D;
    public static final byte MINT = 0x42;
    public static final byte DIFF = 0x04;
    public static final byte SUM = 0x52;

    public static final byte CSUB = 0x13;
    public static final byte CCNT = 0x4D;
    public static final byte TESTERR = 0x29;
    public static final byte SETERR = 0x10;
    public static final byte STOPERR = 0x55;
    public static final byte CLRHALTERR = 0x57;
    public static final byte SETHALTERR = 0x58;
    public static final byte TESTHALTERR = 0x59;

    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    // Offsets from Wptr to hold scheduling information
    public static final int IPTR_S = -1;
    public static final int LINK_S = -2;
    public static final int STATE_S = -3;
    public static final int POINTER_S = -3;
    public static final int TLINK_S = -4;
    public static final int LENGTH_S = -4;
    public static final int TIME_S = -5;
    public static final int CHAN_S = -5;

    // Constants values
    public static final int NOTPROCESS_P = MININT;
    public static final int ENABLING_P = (MININT + 1);
    public static final int WAITING_P = (MININT + 2);
    public static final int READY_P = (MININT + 3);
    public static final int TIMESET_P = (MININT + 1);
    public static final int TIMENOTSET_P = (MININT + 2);
    public static final int NONESELECTED_O = -1;

    // Links
    public static final int IN_PORTS = 16;
    public static final int SHIFT_IN_PORTS = 4;
    public static final byte NOIO = 0;
    public static final int RUNREQUEST = 1;
    public static final int ACKRUN = 2;
    public static final int READYREQUEST = 3;
    public static final int ACKREADY = 4;
    public static final int PERFORMIO = 5;
    public static final int STATUSENQUIRY = 6;
    public static final int READYFALSE = 7;
    public static final int ENABLE = 8;
    public static final int RESETREQUEST = 9;
    public static final int ACKRESET = 10;
    public static final int ACKDATA = 11;

    /*
     * MOSTNEG INT + IBOARDSIZE --> +---------------+
     *                              |               |
     *                              |      FREE     |
     *                              |     MEMORY    |
     *                              |               |
     *                              +---------------+
     *                              |     VECTOR    |
     *                              |     SPACE     |
     *                              +---------------+
     *                              |               |
     *                              |      CODE     |
     *                              |               |
     *                     CODE --> +---------------+
     *                              | SCALAR SPACE  |
     *                              | (WORKSPACE)   |
     *                 MEMSTART --> +---------------+
     *                              |    RESERVED   |
     * RESERVED --> MOSTNEG INT --> +---------------+
    */
    public static final int RESERVED = 0;
    public static final int MEMSTART = (28 * BYTESPERWORD);
    public static final int CODESTART = (MEMSTART + ((MEMSIZE - MEMSTART) / 2));

    // base address in reserved memory to store registers when
    // executing high priority process -- in words!
    public static final int SAVEBASE = (RESERVED + 11) * BYTESPERWORD;
    public static final int WDESCINTSAVE = 0;
    public static final int IPTRINTSAVE = 1;
    public static final int AREGINTSAVE = 2;
    public static final int BREGINTSAVE = 3;
    public static final int CREGINTSAVE = 4;
    public static final int STATUSINTSAVE = 5;
    public static final int EREGINTSAVE = 6;

    public static final int TIMERBASE = ((RESERVED + 9) * BYTESPERWORD);


}
