# Workspaces

---

## Workspaces in general

### What is a workspace?

A workspace is a block of memory for a process. It acts as the memory stack space for the process. It contains arguments, locals and temporaries (which result from overflow from the A, B and C registers being pushed to).

A workspace is special in that its size is pre-calculated i.e. statically allocated at compile time. Its size is that of the size required for all the process' locals, arguments, temporaries and any child workspaces.

For those familiar with modern languages and architectures, a workspace is most closely related to a stack frame.

### When are workspaces created?

A single, massive workspace is created for the main process when the processor starts. All other workspaces are created as chunks allocated from this.

Child workspaces originate from creating a new process, which will require a new workspace. A new process may be created for a statement within a parallel. The new workspace is allocated as a block from the current workspace.

This nested and statically allocated system of workspaces is also the reason recursive calls are not supported.

### When are workspaces destroyed?

When a process ends, the space for its workspaces becomes free for use again. Due to the way parallels are implemented, this can mean that space within a current workspace gets re-used for multiple child workspaces at different (sequential) times.

---

## Workspaces in the compiler

### What is a scope?

A scope is a region of code for which a name operand exists. A named operand is a variable, channel, abbreviation, port or timer. Scopes form a tree structure as shown below:

```
File
  |
  | > Function
  |      |
  |      | > Scope
  |      |      |
  |      |      | > Scope
  |      |      |     |
  |      |      |    ...
  |      |      |
  |      |      | > Scope
  |      |      |     |
  |      |      |    ...
  |      |      |
  |      |     ...
  |     ...
  |
  | > Function
  |      |
  |     ...
 ...
```

A single scope can only contain one named operand of a given name. Child scopes can *hide* a named opernad declared in a parent scope by declaring a named operand of the same name. The type of the hider operand need not be the same as that of the hidden named operand.

### What is the relationship between a workspace and a scope?

A workspace contains a flat list of arguments, locals and other workspaces. In this sense, it is what is commonly referred to as an environment. However, workspaces form a linked list so that a process can access a local variable from a parent process (sometimes referred to as procedure).

A scope is associated with a single workspace. This means that the variables within that scope (but not necessarily its child scopes) reside within the specified workspace. A local variable (or argument) can be located in memory by knowing the current scope, and thus current workspace, and comparing that to the workspace for the scope which declared the variable. If the workspace is different, then the scope tree can be chased up from the current scope to the declaring scope, keeping track of how many workspaces are passed through.

An example of the relationship between code, scopes and workspaces is shown below. (Note that this example is simplified from the metadata actually produced by the compiler. The compiler's metadata is more bloated.)

```
CODE                    | SCOPES            | WORKSPACES
--------------------------------------------------------
INT a :                   Scope               Workspace 0
SEQ                         | > a               |
    PAR                     |                   |
        INT b :             | > Scope           | > Workspace 1
        b := 1              |      - > b        |     : 'b' located in current workspace
        SEQ                 |                   |
            INT c :         | > Scope           | > Workspace 2
            a := 2          |      | > c        |     : 'a' located in parent workspace
            c := 3          |      -            |     : 'c' located in current workspace
    a := 4                  -                   : 'a' located in current workspace

| = Continuation of scope/workspace
- = Temrination of scope/workspace
: = Use of variable from a workspace
```

The following is a more complex example to demonstrate where new scopes and workspaces aren't always directly correlated:

```
CODE                    | SCOPES                   | WORKSPACES
--------------------------------------------------------
INT a :                   Scope                      Workspace 0
SEQ                         | > a                       |
    REAL32 a :              | > Scope                   |
    PAR                     |     | > a (hides)         |
        INT b :             |     | > Scope             | > Workspace 1
        b := 1              |     |     - > b           |     : 'b' located in current workspace
        SEQ                 |     |                     |
            INT c :         |     | > Scope             | > Workspace 2
            a := 2          |           | > c           |     : 'a' located in parent workspace (at 2nd location)
            c := 3          |           -               |     : 'c' located in current workspace
    a := 4                  -                           : 'a' located in current workspace (at 1st location)

| = Continuation of scope/workspace
- = Temrination of scope/workspace
: = Use of variable from a workspace
```

To rewrite the scope/workspace tree above in unified form shows that multiple scopes can be part of the same workspace:

 ```
 Scope : Workspace 0
    | (Declares 'a' @ location 0 of workspace 0)
    |
    | > Scope : Workspace 0
          | (Declares 'a' @ location 1 of workspace 0)
          |
          | > Scope : Workspace 1
          |     | (Declares 'b' at location 0 of workspace 1)
          |
          | > Scope : Workspace 2
                | (Declares 'c' at location 0 of workspace 2)
 ```

### How are workspaces tracked?

Workspaces are tracked using a unique identifier. Since workspaces need to be tracked within a function, the `Function` class (which extends `Scope`) handles all new workspace id generation.

Following this, a scope always requests its parent generates a new workspace id for it except in `Function` where `generateWorkspaceId` is overridden and uses a simple counter to generate unique ids.