# Occam Intermediate Language

## Instructions

* `add` Add
* `and` Bitwise And
* `beq` Branch if Equal to 0
* `bgt` Branch if Greater Than 0
* `bgte` Branch if Greater Than or Equal to 0
* `blt` Branch if Less Than 0
* `blte` Branch if Less Than or Equal to 0
* `bneq` Branch if Not Equal to 0
* `br` Unconditional Branch
* `brf` Branch if False
* `brt` Branch if True
* `call` Call
* `ceq` Compare Equal
* `cgt` Compare Greater Than
* `clt` Compare Less Than
* `div` Divide
* `endp` End Process
* `initprocs` Initialise Processes
* `label` Label
* `ldarg` Load Argument
* `ldarga` Load Argument's Address
* `ldloc` Load Local
* `ldloca` Load Local's Address
* `ldvar` Load Variable
* `ldvara` Load Variable's Address
* `mod` Modulo
* `mul` Multiply
* `neg` Negate
* `not` Bitwise Not
* `or` Bitwise Or
* `pop` Pop value from stack
* `loop` Loop
* `ret` Return
* `rotl` Left Rotate
* `rotr` Right Rotate
* `sar` Arithmetic Right Shift
* `starg` Store Argument
* `startp` Start Process
* `stloc` Store Local
* `stvar` Store Variable
* `shl` Logical Left Shift
* `shr` Logical Right Shift
* `sub` Subtract
* `xor` Bitwise Xor

## Example Occam Program

```Occam
PROC init ()
    INT x, y:                           locals
    SEQ                                 sequential IL ops (blocks)
        my.function (x+1)               function call, arguments
        x := 4                          asignment of locals
        y := (x+1)                      loading locals, binary operations
        CHAN OF INT c:                  channel declaration
        PAR                             parallel IL ops (blocks)
            some.procedure (x,y,c)      procedure call, arguments including channel
            another.procedure (c)       
        y := 5                          end of parallel ops
```

## Instruction Specifications

### `initprocs` Initialise Processes

* Initialises the stack ready for the specified number of processes to be executed (number must include the current process if an endp instruction will be executed for the current process) and with the specified continuation address for after all the processes (including the current process) have completed.
* Metadata:
    * `numProcesses` : The number of processes executing in parallel (including the current process and any proceses that will be started.)
    * `continueILOpID` : The ID of the IL op the current process should continue executing at after `numProcesses` `endp` instructions have been executed.

----

### `endp` End Process

* Terminates the current process and continues a parent process if it is no longer waiting on any other children.
* Metadata:
    * `creatorILOpID` : The ID of the `startp` IL op which started the process or `initprocs` IL op which initialised the number of processes to wait on.

----

### `label` Label

* Represents a label which can be used for calls etc. The label is preferably human readable, otherwise it is preferable to use the next IL op's (or a SKIP IL op's) ID.
* Metadata:
    * `label` The label name.
    * `isGlobal` Whether the label is accssible outside of the file or not.

----

### `startp` Start Process

* Starts a new process at the specified instruction.
* Metadata:
    * `firstILOpID` : The ID of the first IL op of the new process
    * `newPriority` : The priority of the new process. Use Current to indicate the priority should equal the current process' priority.
