grammar AssemblerExpression;

expression: '(' expression ')'                      #parenExp
          | MINUS expression                        #negExp
          | expression '*' expression               #mulExp
          | expression (MINUS|PLUS) expression      #addSubExp
          | LABEL                                   #labelExp
          | NUMBER                                  #numberExp
          ;

NUMBER: DIGIT+ ;

LABEL: ( LABEL_SYMBOL | DIGIT | LETTER )+;

fragment LABEL_SYMBOL: '~' | '_' | '.' ;

fragment LETTER: ('a' .. 'z')
               | ('A' .. 'Z')
               ;

fragment DIGIT: ('0' .. '9') ;

MINUS: '-' ;
PLUS: '+' ;

WS : [ \r\n\t] + -> channel (HIDDEN) ;