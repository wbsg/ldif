// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g 2011-06-10 17:29:34

  package ldif.local.datasources.dump.parser;
  
  import ldif.local.runtime.LocalNode;
  import ldif.local.runtime.Quad;
  import ldif.util.NTriplesStringConverter;
  import java.io.Reader;
  import java.util.Vector;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class NQuadParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "DOT", "LANGTAG", "URI", "STRING", "NAMEDNODE", "COMMENT", "WS", "STRING_CHARS", "UNICODEESCAPES", "SPECIALESCAPES", "URI_CHARS", "COMMENTSTART", "SPACE", "CHARS_BASE", "HEX", "'^^'"
    };
    public static final int URI_CHARS=14;
    public static final int COMMENTSTART=15;
    public static final int UNICODEESCAPES=12;
    public static final int NAMEDNODE=8;
    public static final int EOF=-1;
    public static final int SPACE=16;
    public static final int CHARS_BASE=17;
    public static final int STRING_CHARS=11;
    public static final int URI=6;
    public static final int T__19=19;
    public static final int SPECIALESCAPES=13;
    public static final int WS=10;
    public static final int HEX=18;
    public static final int DOT=4;
    public static final int COMMENT=9;
    public static final int LANGTAG=5;
    public static final int STRING=7;

    // delegates
    // delegators


        public NQuadParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public NQuadParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return NQuadParser.tokenNames; }
    public String getGrammarFileName() { return "/home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g"; }


      private static final String LITERAL = "LITERAL";
      private static final String DTLITERAL = "DTLITERAL";
      private static final String LTLITERAL = "LTLITERAL";
      private static final String URINODE = "URINODE";
      private static final String BLANKNODE = "BN";
      private String graph = "default";
      
      public void setGraph(String graph) {
        this.graph = graph;
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
      
      private ldif.entity.Node createNode(Vector<String> node, String graph) {
        ldif.entity.Node ldifNode = null;
        if(node.get(0)==LITERAL)        return LocalNode.createLiteral(node.get(1), graph);
        else if(node.get(0)==DTLITERAL) return LocalNode.createTypedLiteral(node.get(1), node.get(2), graph);
        else if(node.get(0)==LTLITERAL) return LocalNode.createLanguageLiteral(node.get(1), node.get(2), graph);
        else if(node.get(0)==URINODE)   return LocalNode.createUriNode(node.get(1), graph);
        else if(node.get(0)==BLANKNODE) return LocalNode.createBlankNode(node.get(1), graph);
        return ldifNode;
      }



    // $ANTLR start "nQuadDoc"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:75:1: nQuadDoc returns [List<Quad> quads] : ( line )* EOF ;
    public final List<Quad> nQuadDoc() throws RecognitionException {
        List<Quad> quads = null;

        Quad line1 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:76:3: ( ( line )* EOF )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:76:5: ( line )* EOF
            {
             List<Quad> qs = new ArrayList<Quad>(); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:77:5: ( line )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==URI||(LA1_0>=NAMEDNODE && LA1_0<=COMMENT)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:78:7: line
            	    {
            	    pushFollow(FOLLOW_line_in_nQuadDoc81);
            	    line1=line();

            	    state._fsp--;

            	     if(line1 != null)
            	                   qs.add(line1);
            	                 else
            	                   System.out.println("Comment");
            	               

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_nQuadDoc92); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return quads;
    }
    // $ANTLR end "nQuadDoc"


    // $ANTLR start "line"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:86:1: line returns [Quad value] : ( comment | quad );
    public final Quad line() throws RecognitionException {
        Quad value = null;

        Quad quad2 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:87:3: ( comment | quad )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==COMMENT) ) {
                alt2=1;
            }
            else if ( (LA2_0==URI||LA2_0==NAMEDNODE) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:87:5: comment
                    {
                    pushFollow(FOLLOW_comment_in_line109);
                    comment();

                    state._fsp--;

                     value = null; 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:88:5: quad
                    {
                    pushFollow(FOLLOW_quad_in_line117);
                    quad2=quad();

                    state._fsp--;

                     value = quad2; 

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
    // $ANTLR end "line"


    // $ANTLR start "quad"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:91:1: quad returns [Quad value] : subject predicate object ( graph )? DOT ;
    public final Quad quad() throws RecognitionException {
        Quad value = null;

        ldif.entity.Node graph3 = null;

        Vector<String> subject4 = null;

        ldif.entity.Node predicate5 = null;

        Vector<String> object6 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:92:3: ( subject predicate object ( graph )? DOT )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:92:5: subject predicate object ( graph )? DOT
            {
             String quadGraph = graph; 
            pushFollow(FOLLOW_subject_in_quad145);
            subject4=subject();

            state._fsp--;

            pushFollow(FOLLOW_predicate_in_quad147);
            predicate5=predicate();

            state._fsp--;

            pushFollow(FOLLOW_object_in_quad149);
            object6=object();

            state._fsp--;

            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:93:30: ( graph )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==URI) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:93:31: graph
                    {
                    pushFollow(FOLLOW_graph_in_quad152);
                    graph3=graph();

                    state._fsp--;

                     quadGraph = graph3.value();

                    }
                    break;

            }

            match(input,DOT,FOLLOW_DOT_in_quad158); 

                  value = new Quad(createNode(subject4, quadGraph), predicate5.value(), createNode(object6, quadGraph), quadGraph); 
                

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
    // $ANTLR end "quad"


    // $ANTLR start "subject"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:99:1: subject returns [Vector<String> value] : ( iriRef | bNode );
    public final Vector<String> subject() throws RecognitionException {
        Vector<String> value = null;

        Vector<String> iriRef7 = null;

        Vector<String> bNode8 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:100:3: ( iriRef | bNode )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==URI) ) {
                alt4=1;
            }
            else if ( (LA4_0==NAMEDNODE) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:100:5: iriRef
                    {
                    pushFollow(FOLLOW_iriRef_in_subject181);
                    iriRef7=iriRef();

                    state._fsp--;

                    value = iriRef7; 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:101:5: bNode
                    {
                    pushFollow(FOLLOW_bNode_in_subject189);
                    bNode8=bNode();

                    state._fsp--;

                    value = bNode8;

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
    // $ANTLR end "subject"


    // $ANTLR start "predicate"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:104:1: predicate returns [ldif.entity.Node value] : iriRef ;
    public final ldif.entity.Node predicate() throws RecognitionException {
        ldif.entity.Node value = null;

        Vector<String> iriRef9 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:105:3: ( iriRef )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:105:5: iriRef
            {
            pushFollow(FOLLOW_iriRef_in_predicate211);
            iriRef9=iriRef();

            state._fsp--;

             value = createNode(iriRef9, null); 

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
    // $ANTLR end "predicate"


    // $ANTLR start "object"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:108:1: object returns [Vector<String> value] : ( iriRef | bNode | literal );
    public final Vector<String> object() throws RecognitionException {
        Vector<String> value = null;

        Vector<String> iriRef10 = null;

        Vector<String> bNode11 = null;

        Vector<String> literal12 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:109:3: ( iriRef | bNode | literal )
            int alt5=3;
            switch ( input.LA(1) ) {
            case URI:
                {
                alt5=1;
                }
                break;
            case NAMEDNODE:
                {
                alt5=2;
                }
                break;
            case STRING:
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
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:109:5: iriRef
                    {
                    pushFollow(FOLLOW_iriRef_in_object230);
                    iriRef10=iriRef();

                    state._fsp--;

                    value = iriRef10; 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:110:5: bNode
                    {
                    pushFollow(FOLLOW_bNode_in_object239);
                    bNode11=bNode();

                    state._fsp--;

                    value = bNode11; 

                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:111:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_object249);
                    literal12=literal();

                    state._fsp--;

                    value = literal12; 

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
    // $ANTLR end "object"


    // $ANTLR start "graph"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:114:1: graph returns [ldif.entity.Node value] : iriRef ;
    public final ldif.entity.Node graph() throws RecognitionException {
        ldif.entity.Node value = null;

        Vector<String> iriRef13 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:115:3: ( iriRef )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:115:5: iriRef
            {
            pushFollow(FOLLOW_iriRef_in_graph268);
            iriRef13=iriRef();

            state._fsp--;

             value = createNode(iriRef13, null);

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
    // $ANTLR end "graph"


    // $ANTLR start "literal"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:118:1: literal returns [Vector<String> value] : string ( LANGTAG | '^^' iriRef )? ;
    public final Vector<String> literal() throws RecognitionException {
        Vector<String> value = null;

        Token LANGTAG15=null;
        String string14 = null;

        Vector<String> iriRef16 = null;


        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:119:3: ( string ( LANGTAG | '^^' iriRef )? )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:119:5: string ( LANGTAG | '^^' iriRef )?
            {
             value = new Vector<String>(); 
            pushFollow(FOLLOW_string_in_literal292);
            string14=string();

            state._fsp--;


                  value.add(0, LITERAL);
                  value.add(1, NTriplesStringConverter.convertFromEscapedString(string14));
                
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:124:3: ( LANGTAG | '^^' iriRef )?
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LANGTAG) ) {
                alt6=1;
            }
            else if ( (LA6_0==19) ) {
                alt6=2;
            }
            switch (alt6) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:125:7: LANGTAG
                    {
                    LANGTAG15=(Token)match(input,LANGTAG,FOLLOW_LANGTAG_in_literal306); 

                            value.add(0, LTLITERAL); 
                            value.add(2, (LANGTAG15!=null?LANGTAG15.getText():null).substring(1));
                          

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:129:7: '^^' iriRef
                    {
                    match(input,19,FOLLOW_19_in_literal316); 
                    pushFollow(FOLLOW_iriRef_in_literal318);
                    iriRef16=iriRef();

                    state._fsp--;


                            value.add(0, DTLITERAL);
                            value.add(2, iriRef16.get(1));
                          

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
    // $ANTLR end "literal"


    // $ANTLR start "iriRef"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:136:1: iriRef returns [Vector<String> value] : URI ;
    public final Vector<String> iriRef() throws RecognitionException {
        Vector<String> value = null;

        Token URI17=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:137:3: ( URI )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:137:5: URI
            {
            URI17=(Token)match(input,URI,FOLLOW_URI_in_iriRef345); 

                  value = new Vector<String>();
                  String iri = (URI17!=null?URI17.getText():null);
                  value.add(0, URINODE);
                  value.add(1, NTriplesStringConverter.convertFromEscapedString(iri.substring(1, iri.length()-1)));
                

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


    // $ANTLR start "string"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:145:1: string returns [String value] : STRING ;
    public final String string() throws RecognitionException {
        String value = null;

        Token STRING18=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:146:3: ( STRING )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:146:5: STRING
            {
            STRING18=(Token)match(input,STRING,FOLLOW_STRING_in_string364); 

                  value = (STRING18!=null?STRING18.getText():null).substring(1, (STRING18!=null?STRING18.getText():null).length()-1);
                

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


    // $ANTLR start "bNode"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:151:1: bNode returns [Vector<String> value] : NAMEDNODE ;
    public final Vector<String> bNode() throws RecognitionException {
        Vector<String> value = null;

        Token NAMEDNODE19=null;

        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:152:3: ( NAMEDNODE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:152:5: NAMEDNODE
            {
            NAMEDNODE19=(Token)match(input,NAMEDNODE,FOLLOW_NAMEDNODE_in_bNode385); 

                  value = new Vector<String>();
                  value.add(0, BLANKNODE);
                  value.add(1, (NAMEDNODE19!=null?NAMEDNODE19.getText():null).substring(2));
                

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
    // $ANTLR end "bNode"


    // $ANTLR start "comment"
    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:159:1: comment : COMMENT ;
    public final void comment() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:160:3: ( COMMENT )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:160:5: COMMENT
            {
            match(input,COMMENT,FOLLOW_COMMENT_in_comment402); 

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
    // $ANTLR end "comment"

    // Delegated rules


 

    public static final BitSet FOLLOW_line_in_nQuadDoc81 = new BitSet(new long[]{0x0000000000000340L});
    public static final BitSet FOLLOW_EOF_in_nQuadDoc92 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comment_in_line109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quad_in_line117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subject_in_quad145 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_predicate_in_quad147 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_object_in_quad149 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_graph_in_quad152 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_DOT_in_quad158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_subject181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bNode_in_subject189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_predicate211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_object230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bNode_in_object239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_object249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iriRef_in_graph268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_in_literal292 = new BitSet(new long[]{0x0000000000080022L});
    public static final BitSet FOLLOW_LANGTAG_in_literal306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_literal316 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_iriRef_in_literal318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URI_in_iriRef345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_string364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMEDNODE_in_bNode385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMENT_in_comment402 = new BitSet(new long[]{0x0000000000000002L});

}