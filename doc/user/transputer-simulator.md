# Simulator

## Usage

```
./tsim --binaries=binary[;binary;binary...] --interactive --print-workspace-mem --verilog-testbench-gen=verilogfile --scheduler-test=schedulerfile --timer-test=timerfile
```

Where each `binary` is the path of a binary to execute.

* `--interactive` - pause the simulator before execution starts to allow debugging
* `--print-workspace-mem` - print the contents of workspaces before the simulator exits

## Debugger commands

`<id>`: the id of a Transputer
`<addr>`: a memory address. Can be specified as decimal or as hex prepended by `0x`
`<brk>`: a breakpoint identifier

### `help`

Display a list of the available commands.

### `transputers`

Display a list of the transputers currently being simulated.

### `{<id>, } x [/Nuf] <addr>`

Examine the memory at the provided address.

#### Flags

##### N

Decimal number. Number of units of memory to examine.

##### u

Size of units to examine.

* `b` bytes
* `h` halfwords (two bytes)
* `w` words (four bytes)
* `g` giant words (eight bytes)

##### f

Format for printing the units.

* `i` instruction
* `x` hexadecimal
* `d` decimal
* `o` octal
* `a` address
* `c` character


### `{<id>, } info mem`

Print information about memory used during execution so far.

### `{<id>, } info reg`
 
Print basic register information.

### `{<id>, } info s-reg`

Print information about the s registers.

### `{<id>, } info c-reg`

Print information about the c registers.

### `{<id>, } info link`

Print information about the Transputer links.

### `{<id>, } info break`

Print information about the Transputers' breakpoints.

### `{<id>, } info instruction`

Print information about the next instruction to be executed on the specified Transputers.

### `{<id>, } break <addr>`

Print information about the Transputer links.

### `{<id>, } delete <brk>`

Delete the specified breakpoint.

### `step`

Execute the next instruction.

### `continue`

Continue executing until a breakpoint is hit.

