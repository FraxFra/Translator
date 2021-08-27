import java.io.*;

public class Translator 
{
        private Lexer lex;
        private BufferedReader pbr;
        private Token look;
        SymbolTable st = new SymbolTable();
        CodeGenerator code = new CodeGenerator();
        private int count = 0;
        private int i = 0;

        public Translator(Lexer l, BufferedReader br) 
        {
                lex = l;
                pbr = br;
                move();
        }

        void move() 
        {
                look = lex.lexical_scan(pbr);
                System.err.println("token = " + look);
        }

        void error(String s) 
        {
                throw new Error("near line " + lex.line + ": " + s);
        }

        void match(int t) 
        {
                if (look.tag == t) 
                {
                        if (look.tag != Tag.EOF) move();
                } 
                else error("syntax error");
        }

        public void prog() 
        {
                if(look.tag=='='||look.tag=='{'||look.tag==Tag.PRINT||look.tag==Tag.READ||look.tag==Tag.COND||look.tag==Tag.WHILE)
                {
                        statlist();
                        match(Tag.EOF);
                        try
                        {
        	                code.toJasmin();
                        }
                        catch(java.io.IOException e)
                        {
        	                System.out.println("IO error\n");
                        };
	        }
                else if (look.tag==Tag.EOF)
                {
                        System.out.println("Il file non contiene elementi!");
                }
                else error("prog");
        }

        public void stat() 
        {
                switch(look.tag) 
                {
                        case '=': // = ID <expr>
                        match('=');
                        int id_addr_eq = st.lookupAddress(((Word)look).lexeme);
                        if(look.tag == Tag.ID)
                        {
                                if(id_addr_eq == -1)
                                {
                                        count++;
                                        id_addr_eq = count;
                                        st.insert(((Word)look).lexeme,count);
                                }
                                match(Tag.ID);
                                expr();
                                code.emit(OpCode.istore,id_addr_eq);
                        }
                        else
                        {
                                error("Errore nella grammatica dopo = ");
                        }
                        break;

                        case Tag.PRINT: // print (<exprlist>)
                        match(Tag.PRINT);
                        match('(');
                        exprlist(OpCode.invokestatic);
                        match(')');
                        break;

                        case Tag.READ: // read (ID)
                        match(Tag.READ);
                        match('(');
                        if (look.tag==Tag.ID)
                        {
                                int id_addr_read = st.lookupAddress(((Word)look).lexeme);
                                if(id_addr_read == -1)
                                {
                                        count++;
                                        id_addr_read = count;
                                        st.insert(((Word)look).lexeme,count);
                                }
                                match(Tag.ID);
                                match(')');
                                code.emit(OpCode.invokestatic,0);
                                code.emit(OpCode.istore,id_addr_read);
                        }
                        else error("Errore nella grammatica all'interno di print(ID)");
                        break;

                        case Tag.COND: // cond <whenlist> else <stat>
                        String str = "s" + i;
                        int id_addr_COND = st.lookupAddress(str);
                        if(id_addr_COND == -1)
                        {
                                count++;
                                id_addr_COND = count;
                                st.insert(str,count);
                                i++;
                        }
                        code.emit(OpCode.ldc,0);
                        code.emit(OpCode.istore, id_addr_COND);
                        match(Tag.COND);
                        whenlist(str);
                        int ltrue = code.newLabel();
                        int lfalse = code.newLabel();
                        match(Tag.ELSE);
                        code.emit(OpCode.ldc,0);
                        code.emit(OpCode.iload, st.lookupAddress(str));
                        code.emit(OpCode.if_icmpeq, ltrue);
                        code.emit(OpCode.GOto, lfalse);
                        code.emitLabel(ltrue);
                        stat();
                        code.emitLabel(lfalse);
                        break;

                        case Tag.WHILE: // while (<bexpr>) <stat>
                        match(Tag.WHILE);
                        match('(');
                        int lhere = code.newLabel();
                        int lnext = code.newLabel();
                        int lciclo = code.newLabel();
                        code.emitLabel(lciclo);
                        bexpr(lhere);
                        match(')');
                        code.emit(OpCode.GOto,lnext);
                        code.emitLabel(lhere);
                        stat();
                        code.emit(OpCode.GOto, lciclo);
                        code.emitLabel(lnext);
                        break;

                        case '{': // {<statlist>}
                        move();
                        statlist();
                        match('}');
                        break;

                        default:
                        error("errore in stat()");
                }
        }

        private void expr() 
        {
                switch(look.tag) 
                {
                        case '-': // - <expr> <expr>
                        match('-');
                        expr();
                        expr();
                        code.emit(OpCode.isub);
                        break;

                        case '/': // / <expr> <expr>
                        match('/');
                        expr();
                        expr();
                        code.emit(OpCode.idiv);
                        break;

                        case '+': // + (exprlist)
                        match('+');
                        match('(');
                        exprlist(OpCode.iadd);
                        match(')');
                        break;

                        case '*': // * (exprlist)
                        match('*');
                        match('(');
                        exprlist(OpCode.imul);
                        match(')');
                        break;

                        case Tag.NUM : // NUM
                        code.emit(OpCode.ldc, ((NumberTok)look).numero);
                        match(Tag.NUM);
                        break;

                        case Tag.ID : // ID
                        int id_addr_ID = st.lookupAddress(((Word)look).lexeme);
                        if(id_addr_ID == -1)
                        {
                                count++;
                                id_addr_ID = count;
                                st.insert(((Word)look).lexeme,count);
                        }
                        code.emit(OpCode.iload, id_addr_ID);
                        match(Tag.ID);
                        break;
                }
        }

        private void whenlist(String str) // <whenitem> <whenlistp>
        { 
                if(look.tag==Tag.WHEN)
                {
                        whenitem(str);
                         whenlistp(str);
                }
                else error("Errore in whenlist");
        }

        private void whenlistp(String str) // <whenitem> <whenlistp> | NULL
        { 
                if(look.tag==Tag.WHEN)
                {
                        whenitem(str);
                        whenlistp(str);
                }
                else if(look.tag==Tag.ELSE||look.tag=='}'){}
                else error("Errore in whenlistp");
        }

        private void whenitem(String str) // when (bexpr) do stat
        { 
                match(Tag.WHEN);
                match('(');
                int ltrue = code.newLabel();
                int lnext = code.newLabel();
                bexpr(ltrue);
                match(')');
                code.emit(OpCode.GOto,lnext);
                match(Tag.DO);
                code.emitLabel(ltrue);
                code.emit(OpCode.ldc,1);
                code.emit(OpCode.istore, st.lookupAddress(str));
                stat();
                code.emitLabel(lnext);
        }

        private void bexpr(int ltrue_prog) // RELOP <expr> <expr>
        { 
                if(look.tag == Tag.RELOP)
                {
                        if (look == Word.ne)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpne, ltrue_prog);
                        }
                        else if (look == Word.eq)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpeq, ltrue_prog);
                        }
                        else if (look == Word.ge)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpge, ltrue_prog);
                        }
                        else if (look == Word.le)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmple, ltrue_prog);
                        }
                        else if (look == Word.gt)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpgt, ltrue_prog);
                        }
                        else if (look == Word.lt)
                        {
                                move();
                                expr();
                                expr();
                                code.emit(OpCode.if_icmplt, ltrue_prog);
                        }
                }
        }

        private void statlist() // <statlist> <statlistp>
        { 
                if(look.tag==Tag.PRINT||look.tag=='='||look.tag==Tag.READ||look.tag==Tag.COND||look.tag==Tag.WHILE||look.tag=='{')
                {
                        stat();
                        statlist_p();
                }
                else error("Errore in statlist");
        }

        private void statlist_p() // ; <statlist> <statlistp> | NULL
        { 
                if (look.tag == ';')
                {
                        match(';');
                        stat();
                        statlist_p();
                }
                else if(look.tag=='='||look.tag=='}'||look.tag=='{'||look.tag==Tag.PRINT||look.tag==Tag.READ||look.tag==Tag.COND||look.tag==Tag.WHILE||look.tag==Tag.EOF){}
                else error("Errore in statlist_p");
        }

        private void exprlist(OpCode Opcode) // <expr> <exprlistp>
        { 
                if(look.tag==Tag.NUM||look.tag==Tag.ID||look.tag=='+'||look.tag=='/'||look.tag=='*'||look.tag=='-')
                {
                        expr();
                        if (Opcode == OpCode.invokestatic)
                        {
                                code.emit(Opcode,1);
                        }
                        exprlistp(Opcode);
                }
                else error("Errore in exprlist");
        }

        private void exprlistp(OpCode Opcode) // <expr> <exprlistp> | NULL
        { 
                if(look.tag==Tag.NUM||look.tag==Tag.ID||look.tag=='+'||look.tag=='/'||look.tag=='*'||look.tag=='-')
                {
                        expr();
                        if (Opcode == OpCode.invokestatic)
                        {
                                code.emit(Opcode,1);
                        }
                        else
                        {
                                code.emit(Opcode);
                        }
                        exprlistp(Opcode);
                }
                else if(look.tag==')'){}
                else error("Errore in exprlistp");
        }

        public static void main(String[] args) 
        {
                Lexer lex = new Lexer();
                String path = "Prova.txt"; // il percorso del file da leggere
                try
                {
                        BufferedReader br = new BufferedReader(new FileReader(path));
                        Translator translator = new Translator(lex, br);
                        translator.prog();
                        br.close();
                } catch (IOException e) 
                {
                        e.printStackTrace();
                }
        }
}
