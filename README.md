# Translator

The Translator is a programm that generates bytecode starting with a specified grammar.
INPUT -> file.txt containing source code made with GRAMMAR A
OUTPUT -> Object.j

-------- GRAMMAR A ---------
<prog> -> <statlist> EOF
<statlist> -> <stat> <statlistp>
<statlistp> -> ; <stat> <statlistp> | epsilon
<stat> -> = ID <expr> | print ( <exprlist> ) | read ( ID ) | cond <whenlist> else <stat> | while ( <bexpr> ) <stat> | { <statlist> }
<whenlist> -> <whenitem> <whenlistp> 
<whenlistp> -> <whenitem> <whenlistp> | epsilon
<whenitem> -> when ( <bexpr> ) do <stat> 
<bexpr> -> RELOP <expr> <expr>
<expr> -> + ( <exprlist> ) | * ( <exprlist> ) | - <expr> <expr> | / <expr> <expr> | NUM | ID
<exprlist> -> <expr> <exprlistp>
<exprlistp> -> <expr> <exprlistp> | epsilon
--------------------------
