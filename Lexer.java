import java.io.*;
import java.util.*;

public class Lexer {
	public static int line = 1;
   	private char peek = ' ';

    	private void readch(BufferedReader br) 
    	{
        	try {
             		peek = (char) br.read();
            	} catch (IOException exc) 
		{
              		peek = (char) -1;
            	}
    	}

    	public Token lexical_scan(BufferedReader br) {
        	while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') 
        	{
            		if (peek == '\n') line++;
            		readch(br);
        	}

        	switch (peek) 
		{
        	//gestione dei simboli
            		case '!': //return del token "!"
                	peek = ' ';
                	return Token.not;

            		case '(': //return del token "("
                	peek = ' ';
                	return Token.lpt;
            	
			case ')': //return del token ")"
                	peek = ' ';
                	return Token.rpt;

            		case '{': //return del token "{"
                	peek = ' ';
                	return Token.lpg;

            		case '}': //return del token "}"
                	peek = ' ';
                	return Token.rpg;

            		case '+': //return del token "+"
                	peek = ' ';
                	return Token.plus;

            		case '-': //return del token "-"
                	peek = ' ';
                	return Token.minus;

            		case '*': //return del token "*" e gestione del commento mai aperto
                	try {
                      		br.mark(1);
                      		readch(br);
                      		if(peek == '/')
                      		{
                          		throw new Exception();
                      		}
                      		else
                      		{
                          		br.reset();
                          		peek = ' ';
                          		return Token.mult;
                      		}
                     	}catch(Exception e)
                     	{
                        	System.out.println("il commento non e mai stato aperto");
                          	return null;
                     	}

            		case '/': //return del token "/" e gestione dei commenti "//" o "/*"
                	try {
                      		br.mark(1);
                      		readch(br);
                      		if(peek == '/')
                      		{
                          		while(true)
                          		{
                              			if(peek == '\n' || peek == '\r')
                              			{
                                  			peek = ' ';
                                  			return lexical_scan(br);
                              			}
                              			else if(peek == (char)-1)
                              			{
                                  			return lexical_scan(br);
                              			}
                                  		readch(br);
                              		}
                      		}
                      		else if(peek == '*')
                      		{
                        		while(true)
                          		{
                              			readch(br);
                              			while(peek == '*')
                              			{
                                  			readch(br);
                                  			if(peek == '/')
                                  			{
                                      				peek = ' ';
                                      				return lexical_scan(br);
                                  			}
                                  			else if(peek == (char)-1)
                                  			{
                                      				throw new Exception();
                                  			}
                              			}
                              			if(peek == (char)-1)
                              			{
                                  			throw new Exception();
                              			}
                          		}
                      		}
                      		else
                      		{
                        		br.reset();
                          		peek = ' ';
                          		return Token.div;
                      		}
                    	}catch(Exception e)
                    	{
                          	System.out.println("il commento non e stato chiuso e non ci sono ulteriori caratteri da leggere");
                          	return null;
                    	}

            		case ';': //return del token ";"
                	peek = ' ';
               		return Token.semicolon;

            		case '&': //return del token "&&"
                	readch(br);
                	if (peek == '&') 
                	{
                    		peek = ' ';
                    		return Word.and;
                	} 
                	else 
                	{
                    		System.err.println("dopo & e stato trovato "  + peek );
                    		return null;
                	}

            		case '|': //return del token "||"
                	readch(br);
                	if(peek == '|')
                	{
                  		peek = ' ';
                  		return Word.or;
                	}
                	else
                	{
                  		System.err.println("dopo | e stato trovato "  + peek );
                  		return null;
                	}

            		case '=': //return del token "=" e "=="
                	try {
                  		br.mark(1);
                  		readch(br);
                  		if(peek == '=')
                  		{
                    			peek = ' ';
                    			return Word.eq;
                  		}
                  		else if(peek != '=')
                  		{
                   	 		br.reset();
                    			peek = ' ';
                    			return Token.assign;
                  		}
                	}catch(Exception e)
			{
                  		System.out.println("errore nel buffer");
                	}

            		case '<': //return del token "<", "<=" e "<>"
                	try {
                  		br.mark(1);
                  		readch(br);
                  		if(peek != '=' && peek != '>')
                  		{
                    			br.reset();
                    			peek = ' ';
                    			return Word.lt;
                  		}
                  		else if(peek == '=')
                  		{
                    			peek = ' ';
                    			return Word.le;
                  		}
                  		else if(peek == '>')
                  		{
                    			peek = ' ';
                    			return Word.ne;
                  		}
                	}catch(Exception e)
			{
                  		System.out.println("errore nel buffer");
                	}

            		case '>': //return del token ">" e ">="
                	try {
                  		br.mark(1);
                  		readch(br);
                  		if(peek != '=')
                  		{
                    			br.reset();
                    			peek = ' ';
                    			return Word.gt;
                  		}
                  		else if(peek == '=')
                  		{
                   	 		peek = ' ';
                    			return Word.ge;
                  		}
                	}catch(Exception e)
			{
                  		System.out.println("errore nel buffer");
                	}

            		case (char)-1: //return del token "end of file"
                	return new Token(Tag.EOF);

            		default:
                	if (Character.isLetter(peek) || peek == '_') //gestione delle lettere
			{
                  		String a = "";
                  		if(peek == '_') //la parola inizia con _
                  		{
                    			while(peek == '_')
                    			{
                      				a = a + peek;
                      				readch(br);
                    			}
                    			if(peek >= 'a' && peek <= 'z' || peek >= 'A'&& peek <= 'Z' || peek >= '0' && peek <= '9')
                    			{
                      				try {
                        				while(peek >= 'a' && peek <= 'z' || peek >= 'A' && peek <= 'Z' || peek >= '0' && peek <= '9' || peek == '_')
                        				{
                          					br.mark(1);
                          					a = a + peek;
                          					readch(br);
                        				}
                        				br.reset();
                        			} catch(Exception e)
						{
                       	 				System.out.println("errore nel buffer");
                      				}
                      			peek = ' ';
                      			return new Word (Tag.ID, a);
                    			}
                    			else
                    			{
                      				System.err.println("un identificatore non puo contenere solo _ ");
                      				return null;
                    			}
                  		}
                  		else if(peek >= 'a' && peek <= 'z' || peek >= 'A' && peek <= 'Z') //la parola inizia con una lettera minuscola o maiuscola
                  		{
                    			try{
                    				while(peek >= 'a' && peek <= 'z' || peek >= 'A' && peek <= 'Z' || peek >= '0' && peek <= '9' || peek == '_')
                    				{
                      					br.mark(1);
                      					a = a + peek;
                      					readch(br);
                    				}
                    				br.reset();
                    			} catch(Exception e)
					{
                      				System.out.println("errore nel buffer");
                    			}
                    			peek = ' ';
                    			if(a.equals("cond")) //la parola potrebbe essere una parola chiave
                    			{
			    	  		return Word.cond;
                    			}
                    			else if(a.equals("when"))
                    			{
                      				return Word.when;
                    			}
                    			else if(a.equals("then"))
                    			{
                      				return Word.then;
                    			}
                    			else if(a.equals("else"))
                    			{
                      				return Word.elsetok;
                    			}
                    			else if(a.equals("while"))
                    			{
                      				return Word.whiletok;
                    			}
                    			else if(a.equals("do"))
                    			{
                      				return Word.dotok;
                    			}
                    			else if(a.equals("seq"))
                    			{
                      				return Word.seq;
                    			}
                    			else if(a.equals("print"))
                    			{
                      				return Word.print;
                    			}
                    			else if(a.equals("read"))
                    			{
                      				return Word.read;
                    			}
                    			else //la parola è un identificatore
                    			{
                      				return new Word (Tag.ID, a);
                    			}
                  		}
                  		else //la parola non è una stringa accettata dal linguaggio
                  		{
                    			System.err.println("la parola non e accettata dal linguaggio");
                    			return null;
                  		}
                	} 
			else if (Character.isDigit(peek)) //gestione dei numeri
			{
                  		String i = "";
                  		try {
                  			while (Character.isDigit(peek))
                  			{
                    				br.mark(1);
                    				i = i + peek;
                   				readch(br);
                  			}
                    			br.reset();
                  		}catch(Exception e)
				{
                    			System.out.println("errore nel buffer");
                  		}
                    		peek = ' ';
                    		return new NumberTok(Tag.NUM, Integer.parseInt(i));
                  	}
                  	else
                  	{
                    		System.err.println("la parola non e accettata dal linguaggio");
                    		return null;
                  	}
         	}
    	}

	public static void main(String[] args) 
	{
        	Lexer lex = new Lexer();
        	String path = "Prova.txt"; // il percorso del file da leggere
        	try {
            		BufferedReader br = new BufferedReader(new FileReader(path));
            		Token tok;
            		do {
                		tok = lex.lexical_scan(br);
                		System.out.println("Scan: " + tok);
            		} while (tok.tag != Tag.EOF );
            		br.close();
        	} catch (IOException e) 
		{
			e.printStackTrace();
		}
    	}
}
