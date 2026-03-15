# Syntax

```
program    = 'program' [ vars ] { func } statements 'end'
vars       = 'var' var { ',' var } ';'
var        = ID [ '=' expression ]
func       = 'func' ID args [ vars ] statements 'end'
statements = { statement }
statement  = ID '=' expression ';'
           | 'if' expression 'then' statements [ 'else' statements ] 'end'
           | 'while' expression 'do' statements 'end'
           | 'display' expression ';'
args       = '(' [ ID { ',' ID } ] ')'
expression = [ '-' | '+' ] term { ( '-' | '+' ) term }
term       = factor { ( '*' | '/' ) factor }
factor     = '(' expression ')'
           | ID
           | call
           | INT
call       = ID '(' [expression { ',' expression }] ')'
```

* `ID ['=' expression ]`<br>
グローバル変数またはローカル変数の定義
* `ID`<br>
グローバル変数またはローカル変数の参照
* `ID '=' expression ';'`<br>
グローバル変数またはローカル変数への代入
* `'func' ID args [ vars ] statements 'end'`<br>
関数の定義
* `ID '(' [expression { ',' expression }] ')'`<br>
関数の呼び出し