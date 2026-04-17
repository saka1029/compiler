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
           | 'input' ID { ',' ID }';'
           | 'output' expression { ',' expression }';'
args       = '(' [ ID { ',' ID } ] ')'
expression = [ '-' | '+' ] term { ( '-' | '+' ) term }
term       = factor { ( '*' | '/' | '%' ) factor }
factor     = '(' expression ')'
           | ID
           | ID '(' [expression { ',' expression }] ')'
           | INT
```

# アドレス表の構成

## グローバル変数
```
program var a, b, c ...
```
```
Map<String, Integer> globals;
```
|キー|値|
|-|-|
|a|0|
|b|1|
|c|2|

## ローカル変数
```
func f(a, b, c) var d, e; ...
```
```
Map<String, Integer> locals;
```
|キー|値| |
|-|-:|-|
|a|-6|-argSize -3 + 0|
|b|-5|-argSize -3 + 1|
|c|-4|-argSize -3 + 2|
|f|-1| |
|d|0| |
|e|1| |

引数a, b, cの値はすべての名前を読み取り、
argSizeが確定した時点で一般式「-argSize - 3 + i」を使って計算する。
関数の名前fは値-1固定で追加する。

## 関数
```
func p() ... end func q() ... end
```
```
Map<String, Integer> functions;
```

|キー|値|
|-|-|
|p|(関数pの先頭アドレス)|
|q|(関数qの先頭アドレス)|

