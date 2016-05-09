# Assembler

## Usage

```
./tas --input inputfile --output outputfile [--config configfile] 
```

Where `inputfile` is an input assembly file, `outputfile` is the path to output the binary to and `configfile` is an optional config yaml file.

## EBNF Grammar

```
assembly file = { line , newline } ;

line = [ label , { space } , ":" ] , { space } , [ code ] , space , { space } , [ comment ] ;

code = opcode , [ space , { space } , operand ]
     | "#data" , space , { space } , digit , { digit } 
     | "#chan" , space , { space } , ( letter | digit ) , { letter | digit } ;
     
label =  ( letter | digit | label symbol ) , { letter | digit | label symbol } ;

operand = operand atom , [ binary operator , operand atom ] ;

operand atom = number 
             | label 
             | "$"
             | "(" , operand , ")" ; 

comment = "--" , [ space , { character | space } ] ;

newline = "\n" | "\r" | "\r\n" ;

character = letter | digit | symbol ;

letter = uppercase letter | lowercase letter

uppercase letter = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" 
                 | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" 
                 | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z" ;

lowercase letter = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" 
                 | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" 
                 | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z" ;

digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;

number = [ "-" ], digit, { digit } ;

symbol = "[" | "]" | "{" | "}" | "(" | ")" | "<" | ">" | "'" | '"' 
       | "=" | "|" | "." | "," | ";" | "_" ;
       
label symbol = "~" | "_" | "." ;

binary operator = "+" | "-" | "*" ;

space = " " | "\t" ;

indent = "    " | "\t" ;

```

## Directives

### #data

Expands to the specified number of **words** of space, filled with zeros.
 
#### Example

```
#data 1
```

Produces (hex):
```
00
00
00
00
```
on a 32-bit system or
```
00
00
00
00
00
00
00
00
```
on a 64-bit system.

### #chan

A `#chan` directive takes the form `#chan channelname`, where `channelname` is the name of a channel specified in the config yaml file supplied to the assembler.

This is expanded to a `ldc` instruction by the assembler, with the operand equal to the address of the named channel.

TODO: document the config format

### #data

A `#data` directive takes the form `#data numbytes`, where `numbytes` is a positive integer specifying the number of bytes of zeros to be allocated at that position.

## Labels

Labels may consist of letters, numbers, underscores and tildes.

### Init

The assembly must contain an `init:` label to mark where execution begins.

### $X

`$X` in a label operand denotes the *relative address from the start of the file* of the start of the instruction after `X` instructions from the current instruction. Negative counts (i.e. negative `X`) is not permitted and any count which refers to an instruction beyond the end of the file (i.e. which does not exist) is not permitted. Use of either will result in an error. `$C` provides the address of the start of the current instruction (as opposed to the address of the end of the current instruction, provided by `$0`).

When counting, lines that contain only labels or comments are ignored. Any executable instruction (e.g. `ldc 5`, `confio`) counts as a single instruction. Any directive (`#chan`, `#data`) counts as a single instruction.

## Example

```
L0: -- section label
init:
			ldc 		10
			stl 		0
			ldl 		0
			call 		DECREMENT-$C
			ldl			-3
			stl 		0
			j	 		END-$0
DECREMENT:	ajw 		-0
			ldl 		1
			adc 		-1
			stl 		1
			ajw 		0
			ret
END: -- exit program
```

## Config

You can bind labels to specific channels/ports using a config.

### Example

```
processor:
    processor_id: 0
    connections:
        - target_processor: 1
          dest_port: 0
          channel: channel0
    iopins:
        - addr: 70
          channel: sensor
          config: 0
```

You can use `sensor` and `channel0` as labels to refer to the `addr` and `dest_port`.