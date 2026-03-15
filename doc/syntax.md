# Syntax

```
program    = 'program' [ vars ] statements 'end'
vars       = 'var' var { ',' var } ';'
var        = ID [ '=' expression ]
statements = { statement }
statement  = ID '=' expression ';'
           | 'if' expression 'then' statements [ 'else' statements ] 'end'
           | 'while' expression 'do' statements 'end'
           | 'display' expression ';'
           | 'proc' routine
           | 'func' routine
routine    = ID args [ vars ] statements 'end'
args       = '(' [ ID { ',' ID } ] ')'
expression = [ '-' | '+' ] term { ( '-' | '+' ) term }
term       = factor { ( '*' | '/' ) factor }
factor     = '(' expression ')' | ID | INT
```