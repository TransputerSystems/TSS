grammar Occam;

tokens { INDENT, DEDENT }


@lexer::members {

  // A queue where extra tokens are pushed on (see the NEWLINE lexer rule).
  private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();

  // The stack that keeps track of the indentation level.
  private java.util.Stack<Integer> indents = new java.util.Stack<>();

  // The amount of opened braces, brackets and parenthesis.
  private int opened = 0;

  // The most recently produced token.
  private Token lastToken = null;

  @Override
  public void emit(Token t) {
    super.setToken(t);
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {

    // Check if the end-of-file is ahead and there are still some DEDENTS expected.
    if (_input.LA(1) == EOF && !this.indents.isEmpty()) {

      // Remove any trailing EOF tokens from our buffer.
      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i).getType() == EOF) {
          tokens.remove(i);
        }
      }

      // First emit an extra line break that serves as the end of the statement.
      this.emit(commonToken(OccamParser.NL, "\n"));

      // Now emit as much DEDENT tokens as needed.
      while (!indents.isEmpty()) {
        this.emit(createDedent());
        indents.pop();
      }

      // Put the EOF back on the token stream.
      this.emit(commonToken(OccamParser.EOF, "<EOF>"));
    }

    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
      // Keep track of the last token on the default channel.
      this.lastToken = next;
    }

    return tokens.isEmpty() ? next : tokens.poll();
  }

  private Token createDedent() {
    CommonToken dedent = commonToken(OccamParser.DEDENT, "");
    dedent.setLine(this.lastToken.getLine());
    return dedent;
  }

  private CommonToken commonToken(int type, String text) {
    int stop = this.getCharIndex() - 1;
    int start = text.isEmpty() ? stop : stop - text.length() + 1;
    return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);
  }

  // Calculates the indentation of the provided spaces, taking the
  // following rules into account:
  //
  // "Tabs are replaced (from left to right) by one to eight spaces
  //  such that the total number of characters up to and including
  //  the replacement is a multiple of eight [...]"
  //
  //  -- https://docs.python.org/3.1/reference/lexical_analysis.html#indentation
  static int getIndentationCount(String spaces) {

    int count = 0;

    for (char ch : spaces.toCharArray()) {
      switch (ch) {
        case '\t':
          count += 8 - (count % 8);
          break;
        default:
          // A normal space char.
          count++;
      }
    }

    return count;
  }

  boolean atStartOfInput() {
    return super.getCharPositionInLine() == 0 && super.getLine() == 1;
  }
}


NL
 : ( {atStartOfInput()}?   SPACES
   | ('\r'? '\n' | '\r' ) SPACES?
   )
   {
     String newLine = getText().replaceAll("[^\r\n]+", "");
     String spaces = getText().replaceAll("[\r\n]+", "");
     int next = _input.LA(1);
     int nextNext = _input.LA(2);

     if (opened > 0 || next == '\r' || next == '\n' || (next == '-' && nextNext == '-')) {
       // If we're inside a list or on a blank line, ignore all indents,
       // dedents and line breaks.
       skip();
     }
     else {
       emit(commonToken(NL, newLine));

       int indent = getIndentationCount(spaces);
       int previous = indents.isEmpty() ? 0 : indents.peek();

       if (indent == previous) {
         // skip indents of the same size as the present indent-size
         skip();
       }
       else if (indent > previous) {
         indents.push(indent);
         emit(commonToken(OccamParser.INDENT, spaces));
       }
       else {
         // Possibly emit more than 1 DEDENT token.
         while(!indents.isEmpty() && indents.peek() > indent) {
           this.emit(createDedent());
           indents.pop();
         }
       }
     }
   }
 ;

file_input  : ( NL | specification ) * EOF;

stmt        : simple_stmt   #stmt_simple_stmt
            | compound_stmt #stmt_compound_stmt
            ;

simple_stmt : small_stmt NL
            ;

small_stmt
            : assignment    #small_stmt_assignment
            | input         #small_stmt_input
            | output        #small_stmt_output
            | SKIP_T        #small_stmt_skip
            | STOP          #small_stmt_stop
            | proc_instance #small_stmt_proc
            ;

compound_stmt
            : sequence                              #compound_stmt_sequence
            | conditional                           #compound_stmt_conditional
            | selection                             #compound_stmt_selection
            | loop                                  #compound_stmt_loop
            | parallel                              #compound_stmt_parallel
            | alternation                           #compound_stmt_alternation
            | case_input                            #compound_stmt_case_input
            | (specification | allocation) NL stmt  #compound_stmt_spec_or_alloc_stmt
            ;

abbreviation: NAME IS named_operand ':'                                 #abbreviation_name_operand
            | specifier NAME IS named_operand ':'                       #abbreviation_spec_name_operand
            | VAL NAME IS expression ':'                                #abbreviation_expression
            | VAL specifier NAME IS expression ':'                      #abbreviation_spec_expression
            | NAME IS '[' channel ( ',' channel ) * ']' ':'             #abbreviation_name_channel_list
            | specifier NAME IS '[' channel ( ',' channel ) * ']' ':'   #abbreviation_spec_channel_list
            ;

actual      : named_operand     #actual_named_operand
            | channel           #actual_channel
            | expression        #actual_expression
            ;

allocation  : PLACE NAME AT expression ':' ;

alternation : PRI ? ALT NL INDENT alternative (  alternative ) * DEDENT   #alternation_alternatives
            | PRI ? ALT replicator NL INDENT alternative DEDENT             #alternation_replicator_alternative
            ;

alternative : guarded_alternative                                                   #alternative_guarded
            | alternation                                                           #alternative_alternation
            | channel '?' CASE NL INDENT variant ( NL variant ) * DEDENT            #alternative_channel
            | bool '&' channel '?' CASE NL INDENT variant ( NL variant ) * DEDENT   #alternative_bool_channel
            | specification NL alternative                                          #alternative_spec
            ;

assignment  : variable_list ':=' expression_list;

base        : expression;

bool        : expression;

case_expression
            : expression ;

case_input  : channel '?' CASE NL INDENT variant ( NL variant ) * DEDENT ;

channel     : NAME                                          #channel_name
            | channel '[' expression ']'                    #channel_channel_expression
            | '[' channel FROM base ( FOR count ) ? ']'     #channel_from_base
            | '[' channel FOR count ']'                     #channel_for_count
            ;

channel_type : CHANOF protocol                  #channel_type_protocol
            | '[' expression ']' channel_type   #channel_expression_channel_type
            ;

choice      : guarded_choice            #choice_guarded
            | conditional               #choice_conditional
            | specification NL choice   #choice_specification
            ;

conditional : IF NL INDENT choice ( choice ) * DEDENT   #conditional_choices
            | IF replicator NL INDENT choice DEDENT     #conditional_replicator
            ;

conversion  : data_type ( ROUND | TRUNC ) ? operand ;

count       : expression ;

data_type   : BOOL                              #data_type_bool
            | BYTE_KWD                          #data_type_byte
            | INT                               #data_type_int
            | INT16                             #data_type_int16
            | INT32                             #data_type_int32
            | INT64                             #data_type_int64
            | REAL32                            #data_type_real32
            | REAL64                            #data_type_real64
            | NAME                              #data_type_name
            | '[' expression ']' data_type      #data_type_expr_data_type
            ;

declaration : ( data_type | channel_type | timer_type | port_type ) NAME ( ',' NAME )* ':' ;

definition  : DATA TYPE NAME (IS data_type | NL INDENT structured_type DEDENT ) ':'                               #def_DATA_Name
            | PROTOCOL NAME IS (simple_protocol | sequential_protocol) ':'                                        #def_PROTOCOL_NAME_IS
            | PROTOCOL NAME INDENT CASE INDENT ( tagged_protocol ( NL tagged_protocol ) * ) ?  DEDENT DEDENT ':'  #def_PROTOCOL_NAME_INDENT
            | PROC NAME '(' ( formal (',' formal)* )? ')' NL ( INDENT stmt DEDENT )?  ':'                         #def_PROC
            | data_type (',' data_type)* function_header NL INDENT value_process DEDENT ':'                       #def_function_value_process
            | data_type (',' data_type)* function_header IS expression_list ':'                                   #def_function_expression_list
            | specifier NAME RETYPES named_operand ':'                                                            #def_specifier
            | specifier NAME (RETYPES | RESHAPES) named_operand ':'                                               #def_specifier2
            | VAL specifier NAME (RETYPES | RESHAPES) expression ':'                                              #def_val
            ;

delayed_input
            : named_operand '?' AFTER expression ;

dyadic_operator
            : LTHAN
            | PLUS
            | MINUS
            | TIMES
            | DIVIDE
            | PLUS_MOD
            | MINUS_MOD
            | TIMES_MOD
            | REM
            | BITWISE_AND
            | AND_KWD
            | BITWISE_OR
            | OR_KWD
            | XOR
            | BITWISE_AND_KWD
            | GTHAN
            | REM_KWD
            | EQUAL
            | BITOR
            | NOTEQ
            | LTHANEQ
            | GTHANEQ
            | AFTER
            | RIGHTSHIFT
            | LEFTSHIFT
            ;

expression  : operand                           #expression_operand
            | monadic_operator operand          #expression_monadic
            | operand dyadic_operator operand   #expression_dyadic_operator
            | (MOSTPOS | MOSTNEG) data_type     #expression_most_data_type
            | SIZE data_type                    #expression_size_of
            | conversion                        #expression_conversion
            ;

expression_list
            : function_call                                         #expression_list_function_call
            | expression ( ',' expression )*                        #expression_list_expressions
            //| '(' value_process  NL ')' //todo: expression list, add new line support
            ;

field_name  : NAME ;

formal      : specifier NAME ( ',' NAME ) *
            | VAL specifier NAME ( ',' NAME ) *
            ;

function_call
            : NAME '(' ( expression ( ',' expression) * ) ? ')'
            ;

function_header
            : FUNCTION NAME '(' (  formal (',' formal )*  )? ')'
            ;

guard       : input                       #guard_input
            | bool '&' ( input | SKIP_T ) #guard_bool_input_or_skip
            ;

guarded_alternative
            : guard NL (INDENT stmt DEDENT);

guarded_choice
            : bool NL (INDENT stmt DEDENT);

input       : named_operand '?' input_item (  ';' input_item  ) * #input_named_operand_input_items
            | channel '?' CASE tagged_list                        #input_named_operand_tagged_list
            | delayed_input                                       #input_delayed_input
            ;

input_item  : named_operand                     #input_item_variable
            | named_operand '::' named_operand  #input_item_multiple_variables
            ;

literal     : INTEGER ('(' data_type ')')?                      #literal_integer
            | BYTE_LITERAL ('(' data_type ')')?                 #literal_byte
            | REAL ('(' data_type ')')?                         #literal_real
            | TRUE_LITERAL                                      #literal_true
            | FALSE_LITERAL                                     #literal_false
            ;

loop        : WHILE bool NL INDENT stmt DEDENT;

monadic_operator
            : NOT_KWD
            | BITWISE_NOT
            | BITWISE_NOT_KWD
            | MINUS
            | MINUS_MOD
            | SIZE
            ;

operand     : named_operand                                       #operand_variable
            | literal                                             #operand_literal
            | table                                               #operand_table
            | '(' expression ')'                                  #operand_expression
            | '(' value_process NL ')'                            #operand_value_process
            | function_call                                       #operand_function_call
            | operand '[' expression ']'                          #operand_operand_expression
            | BYTESIN '(' ( operand | data_type ) ')'             #operand_bytesin
            | OFFSETOF '(' NAME ',' field_name ')'                #operand_offsetof
            ;

option      : case_expression ( ',' case_expression ) * NL INDENT stmt DEDENT #option_case_expression_stmt
            | ELSE NL INDENT stmt DEDENT                                      #option_else
            | specification NL option                                         #option_spec_option
            ;

output      : named_operand '!' outputitem (';' outputitem ) *                #output_named_operand_outputitems
            | named_operand '!' tag ( ';' outputitem ( ';' outputitem ) * ) ? #output_named_operand_tag_outputitems
            ;

outputitem  : expression                      #outputitem_single_expression
            | expression '::' expression      #outputitem_multiple_expression
            ;

parallel    : PRI ? PAR suite                               #parallel_pripar_suite
            | PRI ? PAR replicator NL INDENT stmt DEDENT    #parallel_pripar_replicator
            | placedpar                                     #parallel_placedpar
            ;

single_stmt : NL INDENT stmt DEDENT;

suite       : NL INDENT (stmt+ | simple_stmt) DEDENT
            ;

placedpar   : PLACED PAR NL INDENT placedpar ( NL placedpar ) *     #placedpar_placedpars
            | PLACED PAR replicator NL INDENT placedpar DEDENT      #placedpar_replicator_placedpar
            | PROCESSOR expression NL INDENT stmt DEDENT            #placedpar_expression_stmt
            ;

port_type    : PORTOF data_type                         #port_type_data_type
            | '[' expression ']' port_type              #port_type_expression_port_type
            ;

proc_instance
            : NAME '(' ( actual (',' actual ) * ) ? ')';

protocol    : NAME                                      #protcol_name
            | simple_protocol                           #protcol_simple_protocol
            ;

replicator  : NAME EQUAL base FOR count;

selection   : CASE selector NL INDENT option *  DEDENT ;

selector    : expression;

sequence    : SEQ suite                                 #sequence_suite
            | SEQ replicator NL INDENT stmt DEDENT      #sequence_replicator
            ;

sequential_protocol
            : simple_protocol ( ';' simple_protocol ) *;

simple_protocol
            : data_type                                 #simple_protocol_data_type
            | ANY                                       #simple_protocol_any
            | data_type '::' '['']' data_type           #simple_protocol_data_type_data_type
            ;

specification
            : declaration  #specificationDec
            | abbreviation #specificationAbrv
            | definition   #specificationDef
            ;

specifier   : data_type                     #specifier_data_type
            | channel_type                  #specifier_channel_type
            | timer_type                    #specifier_timer_type
            | port_type                     #specifier_port_type
            | '['(expression)?']'specifier  #specifier_expression_specifier
            ;

structured_type
            : PACKED ? RECORD NL INDENT  ( single_record_declaration ) ( NL single_record_declaration) *  NL DEDENT ;

single_record_declaration
            : (data_type field_name ( ',' field_name ) * ':')
            ;

table       : STRING_LITERAL ( '(' NAME ')' ) ?         #table_string
            | '[' expression ( ',' expression ) * ']'   #table_expressions
            | table '[' expression ']'                  #table_table_expression
            | '[' table FROM base (FOR count) ? ']'     #table_table_base_count
            | '[' table FOR count ']'                   #table_table_count
            ;

tag         : NAME;

tagged_list  : tag ( ';' input_item ( ';' input_item ) * ) ? ;

tagged_protocol
            : tag ( ';' sequential_protocol ) ? ;

timer_type  : TIMER                         #timer_type_timer
            | '[' expression ']' timer_type #timer_expression_timer_type
            ;

value_process
            : VALOF NL INDENT stmt RESULT expression_list NL DEDENT     #value_process_stmt
            | specification NL value_process                            #value_process_specification
            ;

named_operand : NAME                                           #named_operand_name
              | named_operand '[' expression ']'               #named_operand_expression
              | '[' named_operand FROM base (FOR count)? ']'   #named_operand_base_count
              | '[' named_operand FOR count ']'                #named_operand_count
            ;

variable_list: named_operand ( ',' named_operand ) *;

variant     : tagged_list NL INDENT stmt DEDENT         #variant_tagged_list_stmt
            | specification NL variant                  #variant_specification_variant
            ;

// FLOW CONTROL
CASE        : 'CASE' ;
ELSE        : 'ELSE' ;
FUNCTION    : 'FUNCTION' ;
IF          : 'IF' ;
PAR         : 'PAR' ;
PROC        : 'PROC' ;
SEQ         : 'SEQ' ;
SKIP_T      : 'SKIP' ;
STOP        : 'STOP' ;
WHILE       : 'WHILE' ;

// TYPES AND VARIABLES
BOOL        : 'BOOL' ;
BYTE_KWD    : 'BYTE' ;
CHANOF      : 'CHAN OF' ;
DATA        : 'DATA' ;
INT         : 'INT' ;
INT16       : 'INT16' ;
INT32       : 'INT32' ;
INT64       : 'INT64' ;
MOSTNEG     : 'MOSTNEG' ;
MOSTPOS     : 'MOSTPOS' ;
PACKED      : 'PACKED' ;
PORTOF      : 'PORT OF' ;
PROTOCOL    : 'PROTOCOL' ;
REAL32      : 'REAL32' ;
REAL64      : 'REAL64' ;
RECORD      : 'RECORD' ;
TIMER       : 'TIMER' ;
TYPE        : 'TYPE' ;
VAL         : 'VAL' ;
VALOF       : 'VALOF' ;

// OPERATORS
AFTER       : 'AFTER' ;
AND_KWD     : 'AND' ;       //boolean AND
BYTESIN     : 'BYTESIN' ;   // width, 'bytes in'
BITWISE_AND_KWD  : 'BITAND' ;
BITWISE_NOT_KWD  : 'BITNOT' ;
BITOR       : 'BITOR' ;
MINUS_MOD   : 'MINUS' ;     // modulo subtraction/negation
NOT_KWD     : 'NOT' ;       // boolean not
OR_KWD      : 'OR' ;        //boolean OR
PLUS_MOD    : 'PLUS' ;      // modulo addition
REM_KWD     : 'REM' ;       // remainder
ROUND       : 'ROUND' ;
SIZE        : 'SIZE' ;
TIMES_MOD   : 'TIMES' ;     // modulo multiplication
TRUNC       : 'TRUNC' ;     // truncation

PLUS        : '+' ;
MINUS       : '-' ;
TIMES       : '*' ;
DIVIDE      : '/' ;


// Symbols
REM         : '\\' ;

BITWISE_AND : '/\\' ;
BITWISE_OR  : '\\/' ;
XOR         : '><' ;
BITWISE_NOT : '~' ;
LEFTSHIFT   : '<<' ;
RIGHTSHIFT  : '>>' ;

EQUAL       : '=' ;
LTHAN       : '<' ;
GTHAN       : '>' ;
LTHANEQ     : '<=' ;
GTHANEQ     : '>=' ;
NOTEQ       : '<>' ;

// LITERALS
FALSE_LITERAL: 'FALSE' ;
TRUE_LITERAL: 'TRUE' ;
BYTE_LITERAL: '\'' CHARACTER '\'' ;
STRING_LITERAL
            : '"' CHARACTER* ('*' NL  '*' CHARACTER* )* '"';

INTEGER     : DIGITS | '#'HEXDIGITS ;
REAL        : DIGITS '.' DIGITS ( 'E' EXPONENT )? ;
fragment EXPONENT    : ( '+' | '-' ) DIGITS ;




// COMMUNICATION
INPUT       : '?' ;
OUTPUT      : '!' ;

// OTHER KEYWORDS
ALT         : 'ALT' ;
ANY         : 'ANY' ;
ASM         : 'ASM' ;
AT          : 'AT' ;
FOR         : 'FOR';
FROM        : 'FROM' ;
INLINE      : 'INLINE' ;
IS          : 'IS' ;
OFFSETOF    : 'OFFSETOF' ;
PLACE       : 'PLACE' ;
PLACED      : 'PLACED' ;
PRI         : 'PRI' ;
PROCESSOR   : 'PROCESSOR' ;
RESHAPES    : 'RESHAPES' ;
RESULT      : 'RESULT' ;
RETYPES     : 'RETYPES' ;


NAME        : [a-zA-Z][a-zA-Z0-9.]*;

DIGITS      : DIGIT+ ;
HEXDIGITS   : HEXDIGIT+ ;
DIGIT       : [0-9] ;
HEXDIGIT    : [0-9A-F] ;


IGNORED     : ( SPACES | COMMENT | LINE_JOINING ) -> skip
            ;


fragment SPACES
            : [ \t]+
            ;

fragment COMMENT
            : '--' ~[\r\n]*
            ;

fragment LINE_JOINING
            : '\\' SPACES? ( '\r'? '\n' | '\r' )
            ;

fragment CHARACTER
            : ~['"*]                                                // any ASCII char
            | '*' ( [cC] | [nN] | [tT] | [sS] | '\'' | '"' | '*' )  // escape sequences
            | '*#' HEXDIGIT HEXDIGIT;

