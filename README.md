# Translator

Il Translator è un automa che genera in output bytecode JVM prendendo in input codice di programmazione scritto seguendo una grammatica.
La grammatica utilizzata per implementare il Translator è la seguente:

-------- GRAMMATICA --------
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
        
N.B. <> indica un metodo del Translator, | indica le possibili produzioni della grammatica mentre tutti gli altri elementi sono simboli terminali.
----------------------------
        
Il funzionamento del Translator si basa su due procedure:
        - Inizialmente viene eseguito il Lexer il quale analizzerà dal punto di vista lessicale il codice andando a generare una sequanza di token corrispondente
        - Successivamente il Translator prenderà in input la lista di token e andrà a generare il bytecode corrispondente anche grazie all'utilizzo del CodeGenerator

Per provare e testare il programma seguire i seguenti passaggi:
        - Assicurarsi di aver installato la java virtual machine sul proprio computer
        - Scaricare tutti i file nella propria directory principale
        - Compilare tutti i file da terminale attraverso il comando "javac NomeFile.java" lasciando Translator.java per ultimo
        - Scrivere del codice generico UTILIZZANDO LA GRAMMATICA SOPRA RIPORTATA e salvarlo nel file "Prova.txt"
        - Eseguire "Translator.java" attraverso il comando "java Translator" e ciò produrrà un file "Output.j" contenente il bytecode
L'esito dell'ultima azione potrebbe avere due esiti:
        - la grammatica è stata rispettata ed il file "Output.j" è stato creato
        - la grammatica non è stata rispettata, sul terminale è possibile osservare l'errore che ha determinato l'errore e il file "Output.j" non è stato creato
Infine sarà possibile provare con mano il funzionamento del bytecode andando a scaricare dal sito "http://jasmin.sourceforge.net/" il file jasmin attraveso il quale sarà possibile far eseguire direttamente dalla macchina il codice; per fare ciò eseguire su terminale il comando "java -jar jasmin.jar Output.j".
