grammar DebuggerCommand;

command : HELP                                                          #help
        | TRANSPUTERS                                                   #transputers
        | transputer_list EXAMINE EXAMINE_FLAG? address                 #examine
        | transputer_list INFO MEM                                      #info_mem
        | transputer_list INFO REG                                      #info_reg
        | transputer_list INFO SREG                                     #info_s_reg
        | transputer_list INFO CREG                                     #info_c_reg
        | transputer_list INFO LINK                                     #info_link
        | transputer_list INFO INSTRUCTION                              #info_instruction
        | transputer_list INFO BREAK                                    #info_break
        | transputer_list INFO MEMSIZE                                  #info_memsize
        | transputer_list INFO PROCESSES                                #info_processes
        | transputer_list BREAK address                                 #break
        | transputer_list DELETE address                                #delete
        | STEP                                                          #step
        | CONTINUE                                                      #continue
        ;

transputer_list: NUMBER (',' NUMBER)* ;

address: NUMBER | HEXNUMBER ;

EXAMINE_FLAG: '/' NUMBER UNIT FORMAT ;

NUMBER: DIGIT+ ;
HEXNUMBER: '0x' HEXDIGIT+ ;

HELP:           H E L P ;
TRANSPUTERS:    T R A N S P U T E R S | T ;
EXAMINE:        E X A M I N E | X ;
INFO:           I N F O | I ;
MEM:            M E M  | M ;
REG:            R E G ;
SREG:           S '-' R E G ;
CREG:           C '-' R E G ;
LINK:           L I N K ;
INSTRUCTION:    I N S T R U C T I O N ;
BREAK:          B R E A K | B ;
MEMSIZE:        M E M S I Z E ;
PROCESSES:      P R O C E S S E S | P ;
STEP:           S T E P | S ;
CONTINUE:       C O N T I N U E | C ;
DELETE:         D E L E T E ;

UNIT: B | H | W | G ;
FORMAT: X | D | O | A | C | F | S | I ;

fragment A: ('a'|'A');
fragment B: ('b'|'B');
fragment C: ('c'|'C');
fragment D: ('d'|'D');
fragment E: ('e'|'E');
fragment F: ('f'|'F');
fragment G: ('g'|'G');
fragment H: ('h'|'H');
fragment I: ('i'|'I');
fragment J: ('j'|'J');
fragment K: ('k'|'K');
fragment L: ('l'|'L');
fragment M: ('m'|'M');
fragment N: ('n'|'N');
fragment O: ('o'|'O');
fragment P: ('p'|'P');
fragment Q: ('q'|'Q');
fragment R: ('r'|'R');
fragment S: ('s'|'S');
fragment T: ('t'|'T');
fragment U: ('u'|'U');
fragment V: ('v'|'V');
fragment W: ('w'|'W');
fragment X: ('x'|'X');
fragment Y: ('y'|'Y');
fragment Z: ('z'|'Z');

fragment LETTER: ('a' .. 'z')
               | ('A' .. 'Z')
               ;

fragment DIGIT: ('0' .. '9') ;

fragment HEXDIGIT: ('0' .. '9') | ('a' .. 'f') | ('A' .. 'F') ;

WS : [ \r\n\t] + -> channel (HIDDEN) ;