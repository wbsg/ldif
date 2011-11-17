/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g 2011-05-04 13:48:23

  package de.fuberlin.wiwiss.r2r.parser;
  
  import java.util.Set;
  import java.util.HashSet;
  import java.util.Map;
  import java.util.HashMap;
  import de.fuberlin.wiwiss.r2r.*;
  import com.hp.hpl.jena.util.PrintUtil;
  import de.fuberlin.wiwiss.r2r.parser.ParseException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SPARQL2LDIFParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FILTER", "NIL", "BLANKNODEBRACKETOPEN", "BLANKNODEBRACKETCLOSE", "COLLECTIONOPEN", "COLLECTIONCLOSE", "VAR1", "VAR2", "STR", "LANG", "LANGMATCHES", "DATATYPE", "BOUND", "SAMETERM", "ISIRI", "ISURI", "ISBLANK", "ISLITERAL", "REGEX", "LANGTAG", "INTEGER", "DECIMAL", "DOUBLE", "INTEGER_POSITIVE", "DECIMAL_POSITIVE", "DOUBLE_POSITIVE", "INTEGER_NEGATIVE", "DECIMAL_NEGATIVE", "DOUBLE_NEGATIVE", "TRUE", "FALSE", "STRING_LITERAL1", "STRING_LITERAL2", "STRING_LITERAL_LONG1", "STRING_LITERAL_LONG2", "IRI_REF", "PNAME_LN", "BLANK_NODE_LABEL", "G", "R", "A", "P", "H", "GRAPH", "S", "T", "I", "U", "O", "N", "L", "OPTIONAL", "UNION", "M", "C", "E", "B", "K", "D", "F", "Y", "X", "J", "Q", "V", "W", "Z", "PN_PREFIX", "PNAME_NS", "PN_LOCAL", "VARNAME", "EXPONENT", "ECHAR", "WS", "BLOCKOPEN", "BLOCKCLOSE", "ANON", "PN_CHARS_U", "PN_CHARS_BASE", "PN_CHARS", "HEX", "COMMENT", "'.'", "','", "';'", "'a'", "'||'", "'&&'", "'='", "'!='", "'<'", "'>'", "'<='", "'>='", "'+'", "'-'", "'*'", "'/'", "'!'", "'^^'"
    };
    public static final int EXPONENT=75;
    public static final int GRAPH=47;
    public static final int REGEX=22;
    public static final int PNAME_LN=40;
    public static final int EOF=-1;
    public static final int VARNAME=74;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int ISLITERAL=21;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int LANGMATCHES=14;
    public static final int DOUBLE=26;
    public static final int PN_CHARS_U=81;
    public static final int COMMENT=85;
    public static final int T__99=99;
    public static final int T__98=98;
    public static final int DOUBLE_POSITIVE=29;
    public static final int BOUND=16;
    public static final int T__97=97;
    public static final int ISIRI=18;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int D=62;
    public static final int E=59;
    public static final int F=63;
    public static final int G=42;
    public static final int A=44;
    public static final int B=60;
    public static final int C=58;
    public static final int L=54;
    public static final int M=57;
    public static final int N=53;
    public static final int O=52;
    public static final int H=46;
    public static final int I=50;
    public static final int J=66;
    public static final int BLANK_NODE_LABEL=41;
    public static final int K=61;
    public static final int U=51;
    public static final int T=49;
    public static final int W=69;
    public static final int V=68;
    public static final int Q=67;
    public static final int P=45;
    public static final int S=48;
    public static final int R=43;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int Y=64;
    public static final int ISBLANK=20;
    public static final int T__88=88;
    public static final int X=65;
    public static final int Z=70;
    public static final int BLOCKOPEN=78;
    public static final int WS=77;
    public static final int NIL=5;
    public static final int INTEGER_POSITIVE=27;
    public static final int STRING_LITERAL2=36;
    public static final int FILTER=4;
    public static final int STRING_LITERAL1=35;
    public static final int PN_CHARS=83;
    public static final int DATATYPE=15;
    public static final int DOUBLE_NEGATIVE=32;
    public static final int FALSE=34;
    public static final int LANG=13;
    public static final int BLANKNODEBRACKETCLOSE=7;
    public static final int IRI_REF=39;
    public static final int BLANKNODEBRACKETOPEN=6;
    public static final int COLLECTIONCLOSE=9;
    public static final int ISURI=19;
    public static final int STR=12;
    public static final int SAMETERM=17;
    public static final int COLLECTIONOPEN=8;
    public static final int HEX=84;
    public static final int BLOCKCLOSE=79;
    public static final int T__103=103;
    public static final int DECIMAL_POSITIVE=28;
    public static final int INTEGER=24;
    public static final int INTEGER_NEGATIVE=30;
    public static final int PN_LOCAL=73;
    public static final int PNAME_NS=72;
    public static final int T__102=102;
    public static final int T__101=101;
    public static final int T__100=100;
    public static final int TRUE=33;
    public static final int UNION=56;
    public static final int ECHAR=76;
    public static final int OPTIONAL=55;
    public static final int ANON=80;
    public static final int PN_CHARS_BASE=82;
    public static final int STRING_LITERAL_LONG2=38;
    public static final int DECIMAL=25;
    public static final int VAR1=10;
    public static final int VAR2=11;
    public static final int STRING_LITERAL_LONG1=37;
    public static final int DECIMAL_NEGATIVE=31;
    public static final int PN_PREFIX=71;
    public static final int LANGTAG=23;

    // delegates
    // delegators


        public SPARQL2LDIFParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SPARQL2LDIFParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return SPARQL2LDIFParser.tokenNames; }
    public String getGrammarFileName() { return "/home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g"; }


      PrefixMapper prefixMapper;
      public boolean subject = false;
      public boolean object = false;
      public boolean property = false;
      public Node propertyValue = null;
      public Node subjectValue = null;
      List<NodeTriple> triples = new ArrayList<NodeTriple>();
      public int blankNodeCounter = 1;
      
      public void setPrefixMapper(PrefixMapper pm) {
        prefixMapper = pm;
      }

      public void recover(IntStream input, RecognitionException re) {
        String hdr = getErrorHeader(re);
        String msg = getErrorMessage(re, this.getTokenNames());
        
        throw new ParseException(hdr + " " + msg);
      }
      
      public void reportError(RecognitionException re) {
        String hdr = getErrorHeader(re);
        String msg = getErrorMessage(re, this.getTokenNames());
        
        throw new ParseException(hdr + " " + msg);
      }




    // $ANTLR start "sourcePattern"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:69:1: sourcePattern returns [List<NodeTriple> nodetriples] : ( triplesBlock | ( '{' triplesBlock '}' )+ ) ;
    public final List<NodeTriple> sourcePattern() throws RecognitionException {
        List<NodeTriple> nodetriples = null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:70:3: ( ( triplesBlock | ( '{' triplesBlock '}' )+ ) )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:70:5: ( triplesBlock | ( '{' triplesBlock '}' )+ )
            {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:70:5: ( triplesBlock | ( '{' triplesBlock '}' )+ )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==NIL||(LA2_0>=VAR1 && LA2_0<=VAR2)||(LA2_0>=INTEGER && LA2_0<=BLANK_NODE_LABEL)) ) {
                alt2=1;
            }
            else if ( (LA2_0==BLOCKOPEN) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:70:6: triplesBlock
                    {
                    pushFollow(FOLLOW_triplesBlock_in_sourcePattern62);
                    triplesBlock();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:71:5: ( '{' triplesBlock '}' )+
                    {
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:71:5: ( '{' triplesBlock '}' )+
                    int cnt1=0;
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==BLOCKOPEN) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:71:6: '{' triplesBlock '}'
                    	    {
                    	    match(input,BLOCKOPEN,FOLLOW_BLOCKOPEN_in_sourcePattern69); 
                    	    pushFollow(FOLLOW_triplesBlock_in_sourcePattern71);
                    	    triplesBlock();

                    	    state._fsp--;

                    	    match(input,BLOCKCLOSE,FOLLOW_BLOCKCLOSE_in_sourcePattern73); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt1 >= 1 ) break loop1;
                                EarlyExitException eee =
                                    new EarlyExitException(1, input);
                                throw eee;
                        }
                        cnt1++;
                    } while (true);


                    }
                    break;

            }

            nodetriples =triples;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return nodetriples;
    }
    // $ANTLR end "sourcePattern"


    // $ANTLR start "triplesBlock"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:83:1: triplesBlock : triplesSameSubject ( '.' ( triplesSameSubject )? )* ;
    public final void triplesBlock() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:3: ( triplesSameSubject ( '.' ( triplesSameSubject )? )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:5: triplesSameSubject ( '.' ( triplesSameSubject )? )*
            {
            pushFollow(FOLLOW_triplesSameSubject_in_triplesBlock102);
            triplesSameSubject();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:24: ( '.' ( triplesSameSubject )? )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==86) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:26: '.' ( triplesSameSubject )?
            	    {
            	    match(input,86,FOLLOW_86_in_triplesBlock106); 
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:30: ( triplesSameSubject )?
            	    int alt3=2;
            	    int LA3_0 = input.LA(1);

            	    if ( (LA3_0==NIL||(LA3_0>=VAR1 && LA3_0<=VAR2)||(LA3_0>=INTEGER && LA3_0<=BLANK_NODE_LABEL)) ) {
            	        alt3=1;
            	    }
            	    switch (alt3) {
            	        case 1 :
            	            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:84:30: triplesSameSubject
            	            {
            	            pushFollow(FOLLOW_triplesSameSubject_in_triplesBlock108);
            	            triplesSameSubject();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "triplesBlock"


    // $ANTLR start "filter"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:103:1: filter : FILTER constraint ;
    public final void filter() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:104:3: ( FILTER constraint )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:104:5: FILTER constraint
            {
            match(input,FILTER,FOLLOW_FILTER_in_filter145); 
            pushFollow(FOLLOW_constraint_in_filter147);
            constraint();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "filter"


    // $ANTLR start "constraint"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:107:1: constraint : ( brackettedExpression | builtInCall | functionCall );
    public final void constraint() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:108:3: ( brackettedExpression | builtInCall | functionCall )
            int alt5=3;
            switch ( input.LA(1) ) {
            case COLLECTIONOPEN:
                {
                alt5=1;
                }
                break;
            case STR:
            case LANG:
            case LANGMATCHES:
            case DATATYPE:
            case BOUND:
            case SAMETERM:
            case ISIRI:
            case ISURI:
            case ISBLANK:
            case ISLITERAL:
            case REGEX:
                {
                alt5=2;
                }
                break;
            case IRI_REF:
            case PNAME_LN:
                {
                alt5=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:108:5: brackettedExpression
                    {
                    pushFollow(FOLLOW_brackettedExpression_in_constraint162);
                    brackettedExpression();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:108:28: builtInCall
                    {
                    pushFollow(FOLLOW_builtInCall_in_constraint166);
                    builtInCall();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:108:42: functionCall
                    {
                    pushFollow(FOLLOW_functionCall_in_constraint170);
                    functionCall();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "constraint"


    // $ANTLR start "functionCall"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:111:1: functionCall : iriRef argList ;
    public final void functionCall() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:112:3: ( iriRef argList )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:112:5: iriRef argList
            {
            pushFollow(FOLLOW_iriRef_in_functionCall185);
            iriRef();

            state._fsp--;

            pushFollow(FOLLOW_argList_in_functionCall187);
            argList();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "functionCall"


    // $ANTLR start "argList"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:115:1: argList : ( NIL | '(' expression ( ',' expression )* ')' );
    public final void argList() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:116:3: ( NIL | '(' expression ( ',' expression )* ')' )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==NIL) ) {
                alt7=1;
            }
            else if ( (LA7_0==COLLECTIONOPEN) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:116:5: NIL
                    {
                    match(input,NIL,FOLLOW_NIL_in_argList200); 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:116:11: '(' expression ( ',' expression )* ')'
                    {
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_argList204); 
                    pushFollow(FOLLOW_expression_in_argList206);
                    expression();

                    state._fsp--;

                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:116:26: ( ',' expression )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==87) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:116:28: ',' expression
                    	    {
                    	    match(input,87,FOLLOW_87_in_argList210); 
                    	    pushFollow(FOLLOW_expression_in_argList212);
                    	    expression();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_argList217); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "argList"


    // $ANTLR start "triplesSameSubject"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:119:1: triplesSameSubject : varOrTerm propertyListNotEmpty ;
    public final void triplesSameSubject() throws RecognitionException {
        Node varOrTerm1 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:120:3: ( varOrTerm propertyListNotEmpty )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:120:5: varOrTerm propertyListNotEmpty
            {
            subject=true;
            pushFollow(FOLLOW_varOrTerm_in_triplesSameSubject238);
            varOrTerm1=varOrTerm();

            state._fsp--;

             subjectValue = varOrTerm1;
            subject=false;
            pushFollow(FOLLOW_propertyListNotEmpty_in_triplesSameSubject252);
            propertyListNotEmpty();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "triplesSameSubject"


    // $ANTLR start "propertyListNotEmpty"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:126:1: propertyListNotEmpty : v= verb oList= objectList ( ';' ( verb objectList )? )* ;
    public final void propertyListNotEmpty() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:127:3: (v= verb oList= objectList ( ';' ( verb objectList )? )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:127:5: v= verb oList= objectList ( ';' ( verb objectList )? )*
            {
            pushFollow(FOLLOW_verb_in_propertyListNotEmpty270);
            verb();

            state._fsp--;

            pushFollow(FOLLOW_objectList_in_propertyListNotEmpty274);
            objectList();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:128:4: ( ';' ( verb objectList )? )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==88) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:128:6: ';' ( verb objectList )?
            	    {
            	    match(input,88,FOLLOW_88_in_propertyListNotEmpty281); 
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:128:10: ( verb objectList )?
            	    int alt8=2;
            	    int LA8_0 = input.LA(1);

            	    if ( ((LA8_0>=IRI_REF && LA8_0<=PNAME_LN)||LA8_0==89) ) {
            	        alt8=1;
            	    }
            	    switch (alt8) {
            	        case 1 :
            	            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:128:11: verb objectList
            	            {
            	            pushFollow(FOLLOW_verb_in_propertyListNotEmpty284);
            	            verb();

            	            state._fsp--;

            	            pushFollow(FOLLOW_objectList_in_propertyListNotEmpty286);
            	            objectList();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "propertyListNotEmpty"


    // $ANTLR start "propertyList"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:131:1: propertyList : ( propertyListNotEmpty )? ;
    public final void propertyList() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:132:3: ( ( propertyListNotEmpty )? )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:132:5: ( propertyListNotEmpty )?
            {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:132:5: ( propertyListNotEmpty )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=IRI_REF && LA10_0<=PNAME_LN)||LA10_0==89) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:132:5: propertyListNotEmpty
                    {
                    pushFollow(FOLLOW_propertyListNotEmpty_in_propertyList305);
                    propertyListNotEmpty();

                    state._fsp--;


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "propertyList"


    // $ANTLR start "objectList"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:135:1: objectList : o= object ( ',' o= object )* ;
    public final void objectList() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:136:3: (o= object ( ',' o= object )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:136:5: o= object ( ',' o= object )*
            {
            pushFollow(FOLLOW_object_in_objectList323);
            object();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:137:5: ( ',' o= object )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==87) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:137:7: ',' o= object
            	    {
            	    match(input,87,FOLLOW_87_in_objectList331); 
            	    pushFollow(FOLLOW_object_in_objectList335);
            	    object();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "objectList"


    // $ANTLR start "object"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:141:1: object : ( graphNode ) ;
    public final void object() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:142:3: ( ( graphNode ) )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:142:4: ( graphNode )
            {
            object=true;
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:143:4: ( graphNode )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:143:5: graphNode
            {
            pushFollow(FOLLOW_graphNode_in_object362);
            graphNode();

            state._fsp--;


            }

            object=false;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "object"


    // $ANTLR start "verb"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:147:1: verb : ( iriRef | 'a' );
    public final void verb() throws RecognitionException {
        Node iriRef2 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:148:3: ( iriRef | 'a' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=IRI_REF && LA12_0<=PNAME_LN)) ) {
                alt12=1;
            }
            else if ( (LA12_0==89) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:148:5: iriRef
                    {
                    property=true;
                    pushFollow(FOLLOW_iriRef_in_verb387);
                    iriRef2=iriRef();

                    state._fsp--;

                     propertyValue = iriRef2; 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:150:5: 'a'
                    {
                    match(input,89,FOLLOW_89_in_verb395); 
                     propertyValue = Node.createUriNode(PrintUtil.expandQname("rdf:type")); 
                    property=false;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "verb"


    // $ANTLR start "triplesNode"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:156:1: triplesNode : ( collection | blankNodePropertyList );
    public final void triplesNode() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:157:3: ( collection | blankNodePropertyList )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==COLLECTIONOPEN) ) {
                alt13=1;
            }
            else if ( (LA13_0==BLANKNODEBRACKETOPEN) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:157:5: collection
                    {
                    pushFollow(FOLLOW_collection_in_triplesNode422);
                    collection();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:158:5: blankNodePropertyList
                    {
                    pushFollow(FOLLOW_blankNodePropertyList_in_triplesNode429);
                    blankNodePropertyList();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "triplesNode"


    // $ANTLR start "blankNodePropertyList"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:161:1: blankNodePropertyList : BLANKNODEBRACKETOPEN propertyListNotEmpty BLANKNODEBRACKETCLOSE ;
    public final void blankNodePropertyList() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:162:3: ( BLANKNODEBRACKETOPEN propertyListNotEmpty BLANKNODEBRACKETCLOSE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:162:5: BLANKNODEBRACKETOPEN propertyListNotEmpty BLANKNODEBRACKETCLOSE
            {

                  //Two underscores for not colliding with blank node IDs from the pattern
                  Node blankNode = Node.createBlankNode("__:bn" + blankNodeCounter++);
                  triples.add(new NodeTriple(subjectValue, propertyValue, blankNode));
                  subjectValue = blankNode;
                
            match(input,BLANKNODEBRACKETOPEN,FOLLOW_BLANKNODEBRACKETOPEN_in_blankNodePropertyList446); 
            pushFollow(FOLLOW_propertyListNotEmpty_in_blankNodePropertyList448);
            propertyListNotEmpty();

            state._fsp--;

            match(input,BLANKNODEBRACKETCLOSE,FOLLOW_BLANKNODEBRACKETCLOSE_in_blankNodePropertyList450); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "blankNodePropertyList"


    // $ANTLR start "collection"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:171:1: collection : COLLECTIONOPEN ( graphNode )+ COLLECTIONCLOSE ;
    public final void collection() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:172:3: ( COLLECTIONOPEN ( graphNode )+ COLLECTIONCLOSE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:172:5: COLLECTIONOPEN ( graphNode )+ COLLECTIONCLOSE
            {
            match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_collection465); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:172:20: ( graphNode )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( ((LA14_0>=NIL && LA14_0<=BLANKNODEBRACKETOPEN)||LA14_0==COLLECTIONOPEN||(LA14_0>=VAR1 && LA14_0<=VAR2)||(LA14_0>=INTEGER && LA14_0<=BLANK_NODE_LABEL)) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:172:20: graphNode
            	    {
            	    pushFollow(FOLLOW_graphNode_in_collection467);
            	    graphNode();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);

            match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_collection470); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "collection"


    // $ANTLR start "graphNode"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:175:1: graphNode : ( varOrTerm | triplesNode );
    public final void graphNode() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:176:3: ( varOrTerm | triplesNode )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==NIL||(LA15_0>=VAR1 && LA15_0<=VAR2)||(LA15_0>=INTEGER && LA15_0<=BLANK_NODE_LABEL)) ) {
                alt15=1;
            }
            else if ( (LA15_0==BLANKNODEBRACKETOPEN||LA15_0==COLLECTIONOPEN) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:176:5: varOrTerm
                    {
                    pushFollow(FOLLOW_varOrTerm_in_graphNode485);
                    varOrTerm();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:177:5: triplesNode
                    {
                    pushFollow(FOLLOW_triplesNode_in_graphNode491);
                    triplesNode();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "graphNode"


    // $ANTLR start "varOrTerm"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:180:1: varOrTerm returns [Node value] : ( var | graphTerm ) ;
    public final Node varOrTerm() throws RecognitionException {
        Node value = null;

        Node var3 = null;

        Node graphTerm4 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:181:3: ( ( var | graphTerm ) )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:181:5: ( var | graphTerm )
            {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:181:5: ( var | graphTerm )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0>=VAR1 && LA16_0<=VAR2)) ) {
                alt16=1;
            }
            else if ( (LA16_0==NIL||(LA16_0>=INTEGER && LA16_0<=BLANK_NODE_LABEL)) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:181:6: var
                    {
                    pushFollow(FOLLOW_var_in_varOrTerm511);
                    var3=var();

                    state._fsp--;

                    value = var3;

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:182:5: graphTerm
                    {
                    pushFollow(FOLLOW_graphTerm_in_varOrTerm519);
                    graphTerm4=graphTerm();

                    state._fsp--;

                    value =graphTerm4;

                    }
                    break;

            }


                  if(object) {
                    triples.add(new NodeTriple(subjectValue,propertyValue,value));
                  }
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "varOrTerm"


    // $ANTLR start "varOrIriRef"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:190:1: varOrIriRef returns [Node value] : ( var | iriRef );
    public final Node varOrIriRef() throws RecognitionException {
        Node value = null;

        Node var5 = null;

        Node iriRef6 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:191:3: ( var | iriRef )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=VAR1 && LA17_0<=VAR2)) ) {
                alt17=1;
            }
            else if ( ((LA17_0>=IRI_REF && LA17_0<=PNAME_LN)) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:191:5: var
                    {
                    pushFollow(FOLLOW_var_in_varOrIriRef547);
                    var5=var();

                    state._fsp--;

                    value = var5;

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:192:5: iriRef
                    {
                    pushFollow(FOLLOW_iriRef_in_varOrIriRef555);
                    iriRef6=iriRef();

                    state._fsp--;

                    value = iriRef6;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "varOrIriRef"


    // $ANTLR start "var"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:195:1: var returns [Node value] : ( VAR1 | VAR2 );
    public final Node var() throws RecognitionException {
        Node value = null;

        Token VAR17=null;
        Token VAR28=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:196:3: ( VAR1 | VAR2 )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==VAR1) ) {
                alt18=1;
            }
            else if ( (LA18_0==VAR2) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:196:5: VAR1
                    {
                    VAR17=(Token)match(input,VAR1,FOLLOW_VAR1_in_var576); 
                     value = Node.createVariableNode((VAR17!=null?VAR17.getText():null).substring(1));

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:197:5: VAR2
                    {
                    VAR28=(Token)match(input,VAR2,FOLLOW_VAR2_in_var584); 
                     value = Node.createVariableNode((VAR28!=null?VAR28.getText():null).substring(1));

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "var"


    // $ANTLR start "graphTerm"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:200:1: graphTerm returns [Node value] : ( iriRef | rdfLiteral | numericLiteral | booleanLiteral | blankNode | NIL );
    public final Node graphTerm() throws RecognitionException {
        Node value = null;

        Node iriRef9 = null;

        Node rdfLiteral10 = null;

        SPARQL2LDIFParser.numericLiteral_return numericLiteral11 = null;

        SPARQL2LDIFParser.booleanLiteral_return booleanLiteral12 = null;

        Node blankNode13 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:201:3: ( iriRef | rdfLiteral | numericLiteral | booleanLiteral | blankNode | NIL )
            int alt19=6;
            switch ( input.LA(1) ) {
            case IRI_REF:
            case PNAME_LN:
                {
                alt19=1;
                }
                break;
            case STRING_LITERAL1:
            case STRING_LITERAL2:
            case STRING_LITERAL_LONG1:
            case STRING_LITERAL_LONG2:
                {
                alt19=2;
                }
                break;
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
                {
                alt19=3;
                }
                break;
            case TRUE:
            case FALSE:
                {
                alt19=4;
                }
                break;
            case BLANK_NODE_LABEL:
                {
                alt19=5;
                }
                break;
            case NIL:
                {
                alt19=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }

            switch (alt19) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:201:5: iriRef
                    {
                    pushFollow(FOLLOW_iriRef_in_graphTerm604);
                    iriRef9=iriRef();

                    state._fsp--;

                    value =iriRef9;

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:202:7: rdfLiteral
                    {
                    pushFollow(FOLLOW_rdfLiteral_in_graphTerm614);
                    rdfLiteral10=rdfLiteral();

                    state._fsp--;

                    value = rdfLiteral10;

                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:203:7: numericLiteral
                    {
                    pushFollow(FOLLOW_numericLiteral_in_graphTerm624);
                    numericLiteral11=numericLiteral();

                    state._fsp--;

                    value = Node.createConstantLiteral((numericLiteral11!=null?input.toString(numericLiteral11.start,numericLiteral11.stop):null));

                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:204:7: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_graphTerm634);
                    booleanLiteral12=booleanLiteral();

                    state._fsp--;

                    value = Node.createConstantLiteral((booleanLiteral12!=null?input.toString(booleanLiteral12.start,booleanLiteral12.stop):null));

                    }
                    break;
                case 5 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:205:7: blankNode
                    {
                    pushFollow(FOLLOW_blankNode_in_graphTerm644);
                    blankNode13=blankNode();

                    state._fsp--;

                    value =blankNode13;

                    }
                    break;
                case 6 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:206:7: NIL
                    {
                    match(input,NIL,FOLLOW_NIL_in_graphTerm654); 
                    value =Node.createUriNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "graphTerm"


    // $ANTLR start "expression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:209:1: expression : conditionalOrExpression ;
    public final void expression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:210:3: ( conditionalOrExpression )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:210:5: conditionalOrExpression
            {
            pushFollow(FOLLOW_conditionalOrExpression_in_expression671);
            conditionalOrExpression();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "conditionalOrExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:213:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
    public final void conditionalOrExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:214:3: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:214:5: conditionalAndExpression ( '||' conditionalAndExpression )*
            {
            pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression686);
            conditionalAndExpression();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:214:30: ( '||' conditionalAndExpression )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==90) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:214:31: '||' conditionalAndExpression
            	    {
            	    match(input,90,FOLLOW_90_in_conditionalOrExpression689); 
            	    pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression691);
            	    conditionalAndExpression();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "conditionalOrExpression"


    // $ANTLR start "conditionalAndExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:217:1: conditionalAndExpression : valueLogical ( '&&' valueLogical )* ;
    public final void conditionalAndExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:218:3: ( valueLogical ( '&&' valueLogical )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:218:5: valueLogical ( '&&' valueLogical )*
            {
            pushFollow(FOLLOW_valueLogical_in_conditionalAndExpression708);
            valueLogical();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:218:18: ( '&&' valueLogical )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==91) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:218:20: '&&' valueLogical
            	    {
            	    match(input,91,FOLLOW_91_in_conditionalAndExpression712); 
            	    pushFollow(FOLLOW_valueLogical_in_conditionalAndExpression714);
            	    valueLogical();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "conditionalAndExpression"


    // $ANTLR start "valueLogical"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:221:1: valueLogical : relationalExpression ;
    public final void valueLogical() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:222:3: ( relationalExpression )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:222:5: relationalExpression
            {
            pushFollow(FOLLOW_relationalExpression_in_valueLogical732);
            relationalExpression();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "valueLogical"


    // $ANTLR start "relationalExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:225:1: relationalExpression : numericExpression ( '=' numericExpression | '!=' numericExpression | '<' numericExpression | '>' numericExpression | '<=' numericExpression | '>=' numericExpression )? ;
    public final void relationalExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:226:3: ( numericExpression ( '=' numericExpression | '!=' numericExpression | '<' numericExpression | '>' numericExpression | '<=' numericExpression | '>=' numericExpression )? )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:226:5: numericExpression ( '=' numericExpression | '!=' numericExpression | '<' numericExpression | '>' numericExpression | '<=' numericExpression | '>=' numericExpression )?
            {
            pushFollow(FOLLOW_numericExpression_in_relationalExpression747);
            numericExpression();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:226:23: ( '=' numericExpression | '!=' numericExpression | '<' numericExpression | '>' numericExpression | '<=' numericExpression | '>=' numericExpression )?
            int alt22=7;
            switch ( input.LA(1) ) {
                case 92:
                    {
                    alt22=1;
                    }
                    break;
                case 93:
                    {
                    alt22=2;
                    }
                    break;
                case 94:
                    {
                    alt22=3;
                    }
                    break;
                case 95:
                    {
                    alt22=4;
                    }
                    break;
                case 96:
                    {
                    alt22=5;
                    }
                    break;
                case 97:
                    {
                    alt22=6;
                    }
                    break;
            }

            switch (alt22) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:227:25: '=' numericExpression
                    {
                    match(input,92,FOLLOW_92_in_relationalExpression775); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression777);
                    numericExpression();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:228:25: '!=' numericExpression
                    {
                    match(input,93,FOLLOW_93_in_relationalExpression804); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression806);
                    numericExpression();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:229:25: '<' numericExpression
                    {
                    match(input,94,FOLLOW_94_in_relationalExpression833); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression835);
                    numericExpression();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:230:25: '>' numericExpression
                    {
                    match(input,95,FOLLOW_95_in_relationalExpression862); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression864);
                    numericExpression();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:231:25: '<=' numericExpression
                    {
                    match(input,96,FOLLOW_96_in_relationalExpression891); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression893);
                    numericExpression();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:232:25: '>=' numericExpression
                    {
                    match(input,97,FOLLOW_97_in_relationalExpression919); 
                    pushFollow(FOLLOW_numericExpression_in_relationalExpression921);
                    numericExpression();

                    state._fsp--;


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "relationalExpression"


    // $ANTLR start "numericExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:236:2: numericExpression : additiveExpression ;
    public final void numericExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:237:4: ( additiveExpression )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:237:6: additiveExpression
            {
            pushFollow(FOLLOW_additiveExpression_in_numericExpression963);
            additiveExpression();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "numericExpression"


    // $ANTLR start "additiveExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:240:2: additiveExpression : multiplicativeExpression ( '+' multiplicativeExpression | '-' multiplicativeExpression | numericLiteralPositive | numericLiteralNegative )* ;
    public final void additiveExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:241:4: ( multiplicativeExpression ( '+' multiplicativeExpression | '-' multiplicativeExpression | numericLiteralPositive | numericLiteralNegative )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:241:6: multiplicativeExpression ( '+' multiplicativeExpression | '-' multiplicativeExpression | numericLiteralPositive | numericLiteralNegative )*
            {
            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression982);
            multiplicativeExpression();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:241:31: ( '+' multiplicativeExpression | '-' multiplicativeExpression | numericLiteralPositive | numericLiteralNegative )*
            loop23:
            do {
                int alt23=5;
                switch ( input.LA(1) ) {
                case 98:
                    {
                    alt23=1;
                    }
                    break;
                case 99:
                    {
                    alt23=2;
                    }
                    break;
                case INTEGER_POSITIVE:
                case DECIMAL_POSITIVE:
                case DOUBLE_POSITIVE:
                    {
                    alt23=3;
                    }
                    break;
                case INTEGER_NEGATIVE:
                case DECIMAL_NEGATIVE:
                case DOUBLE_NEGATIVE:
                    {
                    alt23=4;
                    }
                    break;

                }

                switch (alt23) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:241:33: '+' multiplicativeExpression
            	    {
            	    match(input,98,FOLLOW_98_in_additiveExpression986); 
            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression988);
            	    multiplicativeExpression();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:242:33: '-' multiplicativeExpression
            	    {
            	    match(input,99,FOLLOW_99_in_additiveExpression1022); 
            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression1024);
            	    multiplicativeExpression();

            	    state._fsp--;


            	    }
            	    break;
            	case 3 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:243:33: numericLiteralPositive
            	    {
            	    pushFollow(FOLLOW_numericLiteralPositive_in_additiveExpression1058);
            	    numericLiteralPositive();

            	    state._fsp--;


            	    }
            	    break;
            	case 4 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:244:33: numericLiteralNegative
            	    {
            	    pushFollow(FOLLOW_numericLiteralNegative_in_additiveExpression1092);
            	    numericLiteralNegative();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "additiveExpression"


    // $ANTLR start "multiplicativeExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:248:2: multiplicativeExpression : unaryExpression ( '*' unaryExpression | '/' unaryExpression )* ;
    public final void multiplicativeExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:249:4: ( unaryExpression ( '*' unaryExpression | '/' unaryExpression )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:249:6: unaryExpression ( '*' unaryExpression | '/' unaryExpression )*
            {
            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1144);
            unaryExpression();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:249:22: ( '*' unaryExpression | '/' unaryExpression )*
            loop24:
            do {
                int alt24=3;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==100) ) {
                    alt24=1;
                }
                else if ( (LA24_0==101) ) {
                    alt24=2;
                }


                switch (alt24) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:249:24: '*' unaryExpression
            	    {
            	    match(input,100,FOLLOW_100_in_multiplicativeExpression1148); 
            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1150);
            	    unaryExpression();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:249:46: '/' unaryExpression
            	    {
            	    match(input,101,FOLLOW_101_in_multiplicativeExpression1154); 
            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1157);
            	    unaryExpression();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "multiplicativeExpression"


    // $ANTLR start "unaryExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:252:2: unaryExpression : ( '!' primaryExpression | '+' primaryExpression | '-' primaryExpression | primaryExpression );
    public final void unaryExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:253:4: ( '!' primaryExpression | '+' primaryExpression | '-' primaryExpression | primaryExpression )
            int alt25=4;
            switch ( input.LA(1) ) {
            case 102:
                {
                alt25=1;
                }
                break;
            case 98:
                {
                alt25=2;
                }
                break;
            case 99:
                {
                alt25=3;
                }
                break;
            case COLLECTIONOPEN:
            case VAR1:
            case VAR2:
            case STR:
            case LANG:
            case LANGMATCHES:
            case DATATYPE:
            case BOUND:
            case SAMETERM:
            case ISIRI:
            case ISURI:
            case ISBLANK:
            case ISLITERAL:
            case REGEX:
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case TRUE:
            case FALSE:
            case STRING_LITERAL1:
            case STRING_LITERAL2:
            case STRING_LITERAL_LONG1:
            case STRING_LITERAL_LONG2:
            case IRI_REF:
            case PNAME_LN:
                {
                alt25=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:253:6: '!' primaryExpression
                    {
                    match(input,102,FOLLOW_102_in_unaryExpression1178); 
                    pushFollow(FOLLOW_primaryExpression_in_unaryExpression1180);
                    primaryExpression();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:254:6: '+' primaryExpression
                    {
                    match(input,98,FOLLOW_98_in_unaryExpression1187); 
                    pushFollow(FOLLOW_primaryExpression_in_unaryExpression1189);
                    primaryExpression();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:255:6: '-' primaryExpression
                    {
                    match(input,99,FOLLOW_99_in_unaryExpression1196); 
                    pushFollow(FOLLOW_primaryExpression_in_unaryExpression1198);
                    primaryExpression();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:256:6: primaryExpression
                    {
                    pushFollow(FOLLOW_primaryExpression_in_unaryExpression1205);
                    primaryExpression();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "unaryExpression"


    // $ANTLR start "primaryExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:259:2: primaryExpression : ( brackettedExpression | builtInCall | iriRefOrFunction | rdfLiteral | numericLiteral | booleanLiteral | var );
    public final void primaryExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:260:4: ( brackettedExpression | builtInCall | iriRefOrFunction | rdfLiteral | numericLiteral | booleanLiteral | var )
            int alt26=7;
            switch ( input.LA(1) ) {
            case COLLECTIONOPEN:
                {
                alt26=1;
                }
                break;
            case STR:
            case LANG:
            case LANGMATCHES:
            case DATATYPE:
            case BOUND:
            case SAMETERM:
            case ISIRI:
            case ISURI:
            case ISBLANK:
            case ISLITERAL:
            case REGEX:
                {
                alt26=2;
                }
                break;
            case IRI_REF:
            case PNAME_LN:
                {
                alt26=3;
                }
                break;
            case STRING_LITERAL1:
            case STRING_LITERAL2:
            case STRING_LITERAL_LONG1:
            case STRING_LITERAL_LONG2:
                {
                alt26=4;
                }
                break;
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
                {
                alt26=5;
                }
                break;
            case TRUE:
            case FALSE:
                {
                alt26=6;
                }
                break;
            case VAR1:
            case VAR2:
                {
                alt26=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:260:6: brackettedExpression
                    {
                    pushFollow(FOLLOW_brackettedExpression_in_primaryExpression1222);
                    brackettedExpression();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:261:6: builtInCall
                    {
                    pushFollow(FOLLOW_builtInCall_in_primaryExpression1229);
                    builtInCall();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:262:6: iriRefOrFunction
                    {
                    pushFollow(FOLLOW_iriRefOrFunction_in_primaryExpression1236);
                    iriRefOrFunction();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:263:6: rdfLiteral
                    {
                    pushFollow(FOLLOW_rdfLiteral_in_primaryExpression1243);
                    rdfLiteral();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:264:6: numericLiteral
                    {
                    pushFollow(FOLLOW_numericLiteral_in_primaryExpression1250);
                    numericLiteral();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:265:6: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_primaryExpression1257);
                    booleanLiteral();

                    state._fsp--;


                    }
                    break;
                case 7 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:266:6: var
                    {
                    pushFollow(FOLLOW_var_in_primaryExpression1264);
                    var();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "primaryExpression"


    // $ANTLR start "brackettedExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:269:2: brackettedExpression : '(' expression ')' ;
    public final void brackettedExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:270:4: ( '(' expression ')' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:270:6: '(' expression ')'
            {
            match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_brackettedExpression1283); 
            pushFollow(FOLLOW_expression_in_brackettedExpression1285);
            expression();

            state._fsp--;

            match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_brackettedExpression1287); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "brackettedExpression"


    // $ANTLR start "builtInCall"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:273:2: builtInCall : ( STR '(' expression ')' | LANG '(' expression ')' | LANGMATCHES '(' expression ',' expression ')' | DATATYPE '(' expression ')' | BOUND '(' var ')' | SAMETERM '(' expression ',' expression ')' | ISIRI '(' expression ')' | ISURI '(' expression ')' | ISBLANK '(' expression ')' | ISLITERAL '(' expression ')' | regexExpression );
    public final void builtInCall() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:274:4: ( STR '(' expression ')' | LANG '(' expression ')' | LANGMATCHES '(' expression ',' expression ')' | DATATYPE '(' expression ')' | BOUND '(' var ')' | SAMETERM '(' expression ',' expression ')' | ISIRI '(' expression ')' | ISURI '(' expression ')' | ISBLANK '(' expression ')' | ISLITERAL '(' expression ')' | regexExpression )
            int alt27=11;
            switch ( input.LA(1) ) {
            case STR:
                {
                alt27=1;
                }
                break;
            case LANG:
                {
                alt27=2;
                }
                break;
            case LANGMATCHES:
                {
                alt27=3;
                }
                break;
            case DATATYPE:
                {
                alt27=4;
                }
                break;
            case BOUND:
                {
                alt27=5;
                }
                break;
            case SAMETERM:
                {
                alt27=6;
                }
                break;
            case ISIRI:
                {
                alt27=7;
                }
                break;
            case ISURI:
                {
                alt27=8;
                }
                break;
            case ISBLANK:
                {
                alt27=9;
                }
                break;
            case ISLITERAL:
                {
                alt27=10;
                }
                break;
            case REGEX:
                {
                alt27=11;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:274:6: STR '(' expression ')'
                    {
                    match(input,STR,FOLLOW_STR_in_builtInCall1306); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1308); 
                    pushFollow(FOLLOW_expression_in_builtInCall1310);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1312); 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:275:6: LANG '(' expression ')'
                    {
                    match(input,LANG,FOLLOW_LANG_in_builtInCall1319); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1321); 
                    pushFollow(FOLLOW_expression_in_builtInCall1323);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1325); 

                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:276:6: LANGMATCHES '(' expression ',' expression ')'
                    {
                    match(input,LANGMATCHES,FOLLOW_LANGMATCHES_in_builtInCall1332); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1334); 
                    pushFollow(FOLLOW_expression_in_builtInCall1336);
                    expression();

                    state._fsp--;

                    match(input,87,FOLLOW_87_in_builtInCall1338); 
                    pushFollow(FOLLOW_expression_in_builtInCall1340);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1342); 

                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:277:6: DATATYPE '(' expression ')'
                    {
                    match(input,DATATYPE,FOLLOW_DATATYPE_in_builtInCall1349); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1351); 
                    pushFollow(FOLLOW_expression_in_builtInCall1353);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1355); 

                    }
                    break;
                case 5 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:278:6: BOUND '(' var ')'
                    {
                    match(input,BOUND,FOLLOW_BOUND_in_builtInCall1362); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1364); 
                    pushFollow(FOLLOW_var_in_builtInCall1366);
                    var();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1368); 

                    }
                    break;
                case 6 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:279:6: SAMETERM '(' expression ',' expression ')'
                    {
                    match(input,SAMETERM,FOLLOW_SAMETERM_in_builtInCall1375); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1377); 
                    pushFollow(FOLLOW_expression_in_builtInCall1379);
                    expression();

                    state._fsp--;

                    match(input,87,FOLLOW_87_in_builtInCall1381); 
                    pushFollow(FOLLOW_expression_in_builtInCall1383);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1385); 

                    }
                    break;
                case 7 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:280:6: ISIRI '(' expression ')'
                    {
                    match(input,ISIRI,FOLLOW_ISIRI_in_builtInCall1392); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1394); 
                    pushFollow(FOLLOW_expression_in_builtInCall1396);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1398); 

                    }
                    break;
                case 8 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:281:6: ISURI '(' expression ')'
                    {
                    match(input,ISURI,FOLLOW_ISURI_in_builtInCall1405); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1407); 
                    pushFollow(FOLLOW_expression_in_builtInCall1409);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1411); 

                    }
                    break;
                case 9 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:282:6: ISBLANK '(' expression ')'
                    {
                    match(input,ISBLANK,FOLLOW_ISBLANK_in_builtInCall1419); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1421); 
                    pushFollow(FOLLOW_expression_in_builtInCall1423);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1425); 

                    }
                    break;
                case 10 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:283:6: ISLITERAL '(' expression ')'
                    {
                    match(input,ISLITERAL,FOLLOW_ISLITERAL_in_builtInCall1432); 
                    match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_builtInCall1434); 
                    pushFollow(FOLLOW_expression_in_builtInCall1436);
                    expression();

                    state._fsp--;

                    match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_builtInCall1438); 

                    }
                    break;
                case 11 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:284:6: regexExpression
                    {
                    pushFollow(FOLLOW_regexExpression_in_builtInCall1445);
                    regexExpression();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "builtInCall"


    // $ANTLR start "regexExpression"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:287:2: regexExpression : REGEX '(' expression ',' expression ( ',' expression )? ')' ;
    public final void regexExpression() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:288:4: ( REGEX '(' expression ',' expression ( ',' expression )? ')' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:288:6: REGEX '(' expression ',' expression ( ',' expression )? ')'
            {
            match(input,REGEX,FOLLOW_REGEX_in_regexExpression1465); 
            match(input,COLLECTIONOPEN,FOLLOW_COLLECTIONOPEN_in_regexExpression1467); 
            pushFollow(FOLLOW_expression_in_regexExpression1469);
            expression();

            state._fsp--;

            match(input,87,FOLLOW_87_in_regexExpression1471); 
            pushFollow(FOLLOW_expression_in_regexExpression1473);
            expression();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:288:42: ( ',' expression )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==87) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:288:43: ',' expression
                    {
                    match(input,87,FOLLOW_87_in_regexExpression1476); 
                    pushFollow(FOLLOW_expression_in_regexExpression1478);
                    expression();

                    state._fsp--;


                    }
                    break;

            }

            match(input,COLLECTIONCLOSE,FOLLOW_COLLECTIONCLOSE_in_regexExpression1482); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "regexExpression"


    // $ANTLR start "iriRefOrFunction"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:291:2: iriRefOrFunction : iriRef ( argList )? ;
    public final void iriRefOrFunction() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:292:4: ( iriRef ( argList )? )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:292:6: iriRef ( argList )?
            {
            pushFollow(FOLLOW_iriRef_in_iriRefOrFunction1501);
            iriRef();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:292:13: ( argList )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NIL||LA29_0==COLLECTIONOPEN) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:292:13: argList
                    {
                    pushFollow(FOLLOW_argList_in_iriRefOrFunction1503);
                    argList();

                    state._fsp--;


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "iriRefOrFunction"


    // $ANTLR start "rdfLiteral"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:295:2: rdfLiteral returns [Node value] : s= string (l= LANGTAG | ( '^^' iriRef ) )? ;
    public final Node rdfLiteral() throws RecognitionException {
        Node value = null;

        Token l=null;
        String s = null;

        Node iriRef14 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:296:4: (s= string (l= LANGTAG | ( '^^' iriRef ) )? )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:296:6: s= string (l= LANGTAG | ( '^^' iriRef ) )?
            {
            pushFollow(FOLLOW_string_in_rdfLiteral1529);
            s=string();

            state._fsp--;

            value = Node.createLiteral(s);
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:297:8: (l= LANGTAG | ( '^^' iriRef ) )?
            int alt30=3;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==LANGTAG) ) {
                alt30=1;
            }
            else if ( (LA30_0==103) ) {
                alt30=2;
            }
            switch (alt30) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:297:9: l= LANGTAG
                    {
                    l=(Token)match(input,LANGTAG,FOLLOW_LANGTAG_in_rdfLiteral1543); 
                     String langTag = (l!=null?l.getText():null);
                                        value = Node.createLanguageLiteral(s, langTag.substring(1));

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:299:10: ( '^^' iriRef )
                    {
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:299:10: ( '^^' iriRef )
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:299:11: '^^' iriRef
                    {
                    match(input,103,FOLLOW_103_in_rdfLiteral1558); 
                    pushFollow(FOLLOW_iriRef_in_rdfLiteral1560);
                    iriRef14=iriRef();

                    state._fsp--;

                    value = Node.createTypedLiteral(s, iriRef14.value());

                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "rdfLiteral"

    public static class numericLiteral_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "numericLiteral"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:302:2: numericLiteral : ( numericLiteralUnsigned | numericLiteralPositive | numericLiteralNegative );
    public final SPARQL2LDIFParser.numericLiteral_return numericLiteral() throws RecognitionException {
        SPARQL2LDIFParser.numericLiteral_return retval = new SPARQL2LDIFParser.numericLiteral_return();
        retval.start = input.LT(1);

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:303:4: ( numericLiteralUnsigned | numericLiteralPositive | numericLiteralNegative )
            int alt31=3;
            switch ( input.LA(1) ) {
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
                {
                alt31=1;
                }
                break;
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
                {
                alt31=2;
                }
                break;
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
                {
                alt31=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }

            switch (alt31) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:303:6: numericLiteralUnsigned
                    {
                    pushFollow(FOLLOW_numericLiteralUnsigned_in_numericLiteral1585);
                    numericLiteralUnsigned();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:303:31: numericLiteralPositive
                    {
                    pushFollow(FOLLOW_numericLiteralPositive_in_numericLiteral1589);
                    numericLiteralPositive();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:303:56: numericLiteralNegative
                    {
                    pushFollow(FOLLOW_numericLiteralNegative_in_numericLiteral1593);
                    numericLiteralNegative();

                    state._fsp--;


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "numericLiteral"


    // $ANTLR start "numericLiteralUnsigned"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:306:2: numericLiteralUnsigned : ( INTEGER | DECIMAL | DOUBLE );
    public final void numericLiteralUnsigned() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:307:4: ( INTEGER | DECIMAL | DOUBLE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:
            {
            if ( (input.LA(1)>=INTEGER && input.LA(1)<=DOUBLE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "numericLiteralUnsigned"


    // $ANTLR start "numericLiteralPositive"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:310:2: numericLiteralPositive : ( INTEGER_POSITIVE | DECIMAL_POSITIVE | DOUBLE_POSITIVE );
    public final void numericLiteralPositive() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:311:4: ( INTEGER_POSITIVE | DECIMAL_POSITIVE | DOUBLE_POSITIVE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:
            {
            if ( (input.LA(1)>=INTEGER_POSITIVE && input.LA(1)<=DOUBLE_POSITIVE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "numericLiteralPositive"


    // $ANTLR start "numericLiteralNegative"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:314:2: numericLiteralNegative : ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE | DOUBLE_NEGATIVE );
    public final void numericLiteralNegative() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:315:4: ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE | DOUBLE_NEGATIVE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:
            {
            if ( (input.LA(1)>=INTEGER_NEGATIVE && input.LA(1)<=DOUBLE_NEGATIVE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "numericLiteralNegative"

    public static class booleanLiteral_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "booleanLiteral"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:318:2: booleanLiteral : ( TRUE | FALSE );
    public final SPARQL2LDIFParser.booleanLiteral_return booleanLiteral() throws RecognitionException {
        SPARQL2LDIFParser.booleanLiteral_return retval = new SPARQL2LDIFParser.booleanLiteral_return();
        retval.start = input.LT(1);

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:319:4: ( TRUE | FALSE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:
            {
            if ( (input.LA(1)>=TRUE && input.LA(1)<=FALSE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "booleanLiteral"


    // $ANTLR start "string"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:321:2: string returns [String value] : (s= STRING_LITERAL1 | s= STRING_LITERAL2 | s= STRING_LITERAL_LONG1 | s= STRING_LITERAL_LONG2 );
    public final String string() throws RecognitionException {
        String value = null;

        Token s=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:322:4: (s= STRING_LITERAL1 | s= STRING_LITERAL2 | s= STRING_LITERAL_LONG1 | s= STRING_LITERAL_LONG2 )
            int alt32=4;
            switch ( input.LA(1) ) {
            case STRING_LITERAL1:
                {
                alt32=1;
                }
                break;
            case STRING_LITERAL2:
                {
                alt32=2;
                }
                break;
            case STRING_LITERAL_LONG1:
                {
                alt32=3;
                }
                break;
            case STRING_LITERAL_LONG2:
                {
                alt32=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:322:6: s= STRING_LITERAL1
                    {
                    s=(Token)match(input,STRING_LITERAL1,FOLLOW_STRING_LITERAL1_in_string1719); 
                     String temp = (s!=null?s.getText():null); value = temp.substring(1, temp.length() - 1); 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:323:6: s= STRING_LITERAL2
                    {
                    s=(Token)match(input,STRING_LITERAL2,FOLLOW_STRING_LITERAL2_in_string1730); 
                     String temp = (s!=null?s.getText():null); value = temp.substring(1, temp.length() - 1); 

                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:324:6: s= STRING_LITERAL_LONG1
                    {
                    s=(Token)match(input,STRING_LITERAL_LONG1,FOLLOW_STRING_LITERAL_LONG1_in_string1741); 
                     String temp = (s!=null?s.getText():null); value = temp.substring(3, temp.length() - 3); 

                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:325:6: s= STRING_LITERAL_LONG2
                    {
                    s=(Token)match(input,STRING_LITERAL_LONG2,FOLLOW_STRING_LITERAL_LONG2_in_string1752); 
                     String temp = (s!=null?s.getText():null); value = temp.substring(3, temp.length() - 3); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "string"


    // $ANTLR start "iriRef"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:328:1: iriRef returns [Node value] : ( IRI_REF | prefixedName );
    public final Node iriRef() throws RecognitionException {
        Node value = null;

        Token IRI_REF15=null;
        SPARQL2LDIFParser.prefixedName_return prefixedName16 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:329:4: ( IRI_REF | prefixedName )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==IRI_REF) ) {
                alt33=1;
            }
            else if ( (LA33_0==PNAME_LN) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:329:6: IRI_REF
                    {
                    IRI_REF15=(Token)match(input,IRI_REF,FOLLOW_IRI_REF_in_iriRef1774); 
                     
                           String iri = (IRI_REF15!=null?IRI_REF15.getText():null);
                           value = Node.createUriNode(iri.substring(1, iri.length()-1));
                         

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:334:6: prefixedName
                    {
                    pushFollow(FOLLOW_prefixedName_in_iriRef1789);
                    prefixedName16=prefixedName();

                    state._fsp--;

                     
                         String qName = (prefixedName16!=null?input.toString(prefixedName16.start,prefixedName16.stop):null);
                         String iri = PrintUtil.expandQname(qName);
                         if(qName.equals(iri))
                         {
                           String[] prefixAndName = (qName.split(":"));
                           iri = prefixMapper.resolvePrefix(prefixAndName[0]);
                           if(iri==null)
                            throw new IllegalArgumentException("Uknown namespace prefix: " + prefixAndName[0]);
                           else {
                             if(prefixAndName.length < 2)
                               value = Node.createUriNode(iri);
                             else
                               value = Node.createUriNode(iri + prefixAndName[1]);
                           }  
                         }
                         else {
                           value = Node.createUriNode(iri);
                         }
                       

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "iriRef"

    public static class prefixedName_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "prefixedName"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:357:2: prefixedName : p= PNAME_LN ;
    public final SPARQL2LDIFParser.prefixedName_return prefixedName() throws RecognitionException {
        SPARQL2LDIFParser.prefixedName_return retval = new SPARQL2LDIFParser.prefixedName_return();
        retval.start = input.LT(1);

        Token p=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:358:4: (p= PNAME_LN )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:358:6: p= PNAME_LN
            {
            p=(Token)match(input,PNAME_LN,FOLLOW_PNAME_LN_in_prefixedName1815); 

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "prefixedName"


    // $ANTLR start "blankNode"
    // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:362:2: blankNode returns [Node value] : b= BLANK_NODE_LABEL ;
    public final Node blankNode() throws RecognitionException {
        Node value = null;

        Token b=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:363:4: (b= BLANK_NODE_LABEL )
            // /home/andreas/workspace/ANTLRTester/antlr-files/SPARQL2LDIF.g:363:6: b= BLANK_NODE_LABEL
            {
            b=(Token)match(input,BLANK_NODE_LABEL,FOLLOW_BLANK_NODE_LABEL_in_blankNode1841); 
            value = Node.createBlankNode((b!=null?b.getText():null));

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "blankNode"

    // Delegated rules


 

    public static final BitSet FOLLOW_triplesBlock_in_sourcePattern62 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLOCKOPEN_in_sourcePattern69 = new BitSet(new long[]{0x000003FFFF000C20L});
    public static final BitSet FOLLOW_triplesBlock_in_sourcePattern71 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_BLOCKCLOSE_in_sourcePattern73 = new BitSet(new long[]{0x0000000000000002L,0x0000000000004000L});
    public static final BitSet FOLLOW_triplesSameSubject_in_triplesBlock102 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_triplesBlock106 = new BitSet(new long[]{0x000003FFFF000C22L,0x0000000000400000L});
    public static final BitSet FOLLOW_triplesSameSubject_in_triplesBlock108 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_FILTER_in_filter145 = new BitSet(new long[]{0x00000180007FF100L});
    public static final BitSet FOLLOW_constraint_in_filter147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_brackettedExpression_in_constraint162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_builtInCall_in_constraint166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionCall_in_constraint170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_functionCall185 = new BitSet(new long[]{0x0000000000000120L});
    public static final BitSet FOLLOW_argList_in_functionCall187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NIL_in_argList200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_argList204 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_argList206 = new BitSet(new long[]{0x0000000000000200L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_argList210 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_argList212 = new BitSet(new long[]{0x0000000000000200L,0x0000000000800000L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_argList217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varOrTerm_in_triplesSameSubject238 = new BitSet(new long[]{0x0000018000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_propertyListNotEmpty_in_triplesSameSubject252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_verb_in_propertyListNotEmpty270 = new BitSet(new long[]{0x000003FFFF000D60L});
    public static final BitSet FOLLOW_objectList_in_propertyListNotEmpty274 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_propertyListNotEmpty281 = new BitSet(new long[]{0x0000018000000002L,0x0000000003000000L});
    public static final BitSet FOLLOW_verb_in_propertyListNotEmpty284 = new BitSet(new long[]{0x000003FFFF000D60L});
    public static final BitSet FOLLOW_objectList_in_propertyListNotEmpty286 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_propertyListNotEmpty_in_propertyList305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_in_objectList323 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_objectList331 = new BitSet(new long[]{0x000003FFFF000D60L});
    public static final BitSet FOLLOW_object_in_objectList335 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_graphNode_in_object362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_verb387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_verb395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_in_triplesNode422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blankNodePropertyList_in_triplesNode429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANKNODEBRACKETOPEN_in_blankNodePropertyList446 = new BitSet(new long[]{0x0000018000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_propertyListNotEmpty_in_blankNodePropertyList448 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_BLANKNODEBRACKETCLOSE_in_blankNodePropertyList450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_collection465 = new BitSet(new long[]{0x000003FFFF000D60L});
    public static final BitSet FOLLOW_graphNode_in_collection467 = new BitSet(new long[]{0x000003FFFF000F60L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_collection470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varOrTerm_in_graphNode485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_triplesNode_in_graphNode491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_varOrTerm511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_graphTerm_in_varOrTerm519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_varOrIriRef547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_varOrIriRef555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR1_in_var576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR2_in_var584 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_graphTerm604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rdfLiteral_in_graphTerm614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_graphTerm624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_graphTerm634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blankNode_in_graphTerm644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NIL_in_graphTerm654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalOrExpression_in_expression671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression686 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_conditionalOrExpression689 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression691 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_valueLogical_in_conditionalAndExpression708 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_91_in_conditionalAndExpression712 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_valueLogical_in_conditionalAndExpression714 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_relationalExpression_in_valueLogical732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression747 = new BitSet(new long[]{0x0000000000000002L,0x00000003F0000000L});
    public static final BitSet FOLLOW_92_in_relationalExpression775 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_relationalExpression804 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_94_in_relationalExpression833 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_95_in_relationalExpression862 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_96_in_relationalExpression891 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_97_in_relationalExpression919 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_numericExpression_in_relationalExpression921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_additiveExpression_in_numericExpression963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression982 = new BitSet(new long[]{0x00000001FF000002L,0x0000000C00000000L});
    public static final BitSet FOLLOW_98_in_additiveExpression986 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression988 = new BitSet(new long[]{0x00000001FF000002L,0x0000000C00000000L});
    public static final BitSet FOLLOW_99_in_additiveExpression1022 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression1024 = new BitSet(new long[]{0x00000001FF000002L,0x0000000C00000000L});
    public static final BitSet FOLLOW_numericLiteralPositive_in_additiveExpression1058 = new BitSet(new long[]{0x00000001FF000002L,0x0000000C00000000L});
    public static final BitSet FOLLOW_numericLiteralNegative_in_additiveExpression1092 = new BitSet(new long[]{0x00000001FF000002L,0x0000000C00000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1144 = new BitSet(new long[]{0x0000000000000002L,0x0000003000000000L});
    public static final BitSet FOLLOW_100_in_multiplicativeExpression1148 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1150 = new BitSet(new long[]{0x0000000000000002L,0x0000003000000000L});
    public static final BitSet FOLLOW_101_in_multiplicativeExpression1154 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1157 = new BitSet(new long[]{0x0000000000000002L,0x0000003000000000L});
    public static final BitSet FOLLOW_102_in_unaryExpression1178 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_primaryExpression_in_unaryExpression1180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_98_in_unaryExpression1187 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_primaryExpression_in_unaryExpression1189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_99_in_unaryExpression1196 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_primaryExpression_in_unaryExpression1198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primaryExpression_in_unaryExpression1205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_brackettedExpression_in_primaryExpression1222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_builtInCall_in_primaryExpression1229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRefOrFunction_in_primaryExpression1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rdfLiteral_in_primaryExpression1243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_primaryExpression1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_primaryExpression1257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_primaryExpression1264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_brackettedExpression1283 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_brackettedExpression1285 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_brackettedExpression1287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STR_in_builtInCall1306 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1308 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1310 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LANG_in_builtInCall1319 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1321 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1323 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LANGMATCHES_in_builtInCall1332 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1334 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1336 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_builtInCall1338 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1340 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DATATYPE_in_builtInCall1349 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1351 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1353 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOUND_in_builtInCall1362 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1364 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_var_in_builtInCall1366 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SAMETERM_in_builtInCall1375 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1377 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1379 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_builtInCall1381 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1383 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ISIRI_in_builtInCall1392 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1394 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1396 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ISURI_in_builtInCall1405 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1407 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1409 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ISBLANK_in_builtInCall1419 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1421 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1423 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ISLITERAL_in_builtInCall1432 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_builtInCall1434 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_builtInCall1436 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_builtInCall1438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regexExpression_in_builtInCall1445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REGEX_in_regexExpression1465 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COLLECTIONOPEN_in_regexExpression1467 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_regexExpression1469 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_regexExpression1471 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_regexExpression1473 = new BitSet(new long[]{0x0000000000000200L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_regexExpression1476 = new BitSet(new long[]{0x000001FFFF7FFD00L,0x0000004C00000000L});
    public static final BitSet FOLLOW_expression_in_regexExpression1478 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLLECTIONCLOSE_in_regexExpression1482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_iriRefOrFunction1501 = new BitSet(new long[]{0x0000000000000122L});
    public static final BitSet FOLLOW_argList_in_iriRefOrFunction1503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_in_rdfLiteral1529 = new BitSet(new long[]{0x0000000000800002L,0x0000008000000000L});
    public static final BitSet FOLLOW_LANGTAG_in_rdfLiteral1543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_103_in_rdfLiteral1558 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_iriRef_in_rdfLiteral1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteralUnsigned_in_numericLiteral1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteralPositive_in_numericLiteral1589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteralNegative_in_numericLiteral1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_numericLiteralUnsigned0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_numericLiteralPositive0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_numericLiteralNegative0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_booleanLiteral0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL1_in_string1719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL2_in_string1730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_LONG1_in_string1741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_LONG2_in_string1752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IRI_REF_in_iriRef1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefixedName_in_iriRef1789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PNAME_LN_in_prefixedName1815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_NODE_LABEL_in_blankNode1841 = new BitSet(new long[]{0x0000000000000002L});

}