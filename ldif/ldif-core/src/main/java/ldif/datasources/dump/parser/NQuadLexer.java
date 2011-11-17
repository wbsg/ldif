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

// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g 2011-06-14 15:14:27

package ldif.datasources.dump.parser;

import org.antlr.runtime.*;

public class NQuadLexer extends Lexer {
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

      public void recover(RecognitionException re) {
        String hdr = getErrorHeader(re);
        String msg = getErrorMessage(re, this.getTokenNames());
        
        throw new ParseException(hdr + " " + msg);
      }
      
      public void reportError(RecognitionException re) {
        String hdr = getErrorHeader(re);
        String msg = getErrorMessage(re, this.getTokenNames());
        
        throw new ParseException(hdr + " " + msg);
      }


    // delegates
    // delegators

    public NQuadLexer() {;} 
    public NQuadLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public NQuadLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g"; }

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:26:7: ( '^^' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:26:9: '^^'
            {
            match("^^"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "LANGTAG"
    public final void mLANGTAG() throws RecognitionException {
        try {
            int _type = LANGTAG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:3: ( '@' ( 'a' .. 'z' | 'A' .. 'Z' )+ ( '-' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )+ )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:5: '@' ( 'a' .. 'z' | 'A' .. 'Z' )+ ( '-' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )+ )*
            {
            match('@'); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:9: ( 'a' .. 'z' | 'A' .. 'Z' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='A' && LA1_0<='Z')||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            	    {
            	    if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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

            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:32: ( '-' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )+ )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='-') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:33: '-' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )+
            	    {
            	    match('-'); 
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:172:37: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )+
            	    int cnt2=0;
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||(LA2_0>='a' && LA2_0<='z')) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            	    	    {
            	    	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	    	        input.consume();

            	    	    }
            	    	    else {
            	    	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	    	        recover(mse);
            	    	        throw mse;}


            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt2 >= 1 ) break loop2;
            	                EarlyExitException eee =
            	                    new EarlyExitException(2, input);
            	                throw eee;
            	        }
            	        cnt2++;
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LANGTAG"

    // $ANTLR start "NAMEDNODE"
    public final void mNAMEDNODE() throws RecognitionException {
        try {
            int _type = NAMEDNODE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:176:3: ( '_:' ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:176:5: '_:' ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )*
            {
            match("_:"); 

            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:176:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAMEDNODE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:180:3: ( ( '\\u0020' | '\\u0009' | '\\u000D' | '\\u000A' ) )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:180:5: ( '\\u0020' | '\\u0009' | '\\u000D' | '\\u000A' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:184:3: ( '.' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:184:5: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:3: ( '\"' ( STRING_CHARS | UNICODEESCAPES | SPECIALESCAPES )* '\"' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:5: '\"' ( STRING_CHARS | UNICODEESCAPES | SPECIALESCAPES )* '\"'
            {
            match('\"'); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:9: ( STRING_CHARS | UNICODEESCAPES | SPECIALESCAPES )*
            loop5:
            do {
                int alt5=4;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>=' ' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='[')||(LA5_0>=']' && LA5_0<='~')) ) {
                    alt5=1;
                }
                else if ( (LA5_0=='\\') ) {
                    int LA5_3 = input.LA(2);

                    if ( (LA5_3=='U'||LA5_3=='u') ) {
                        alt5=2;
                    }
                    else if ( (LA5_3=='\"'||LA5_3=='\\'||LA5_3=='n'||LA5_3=='r'||LA5_3=='t') ) {
                        alt5=3;
                    }


                }


                switch (alt5) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:10: STRING_CHARS
            	    {
            	    mSTRING_CHARS(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:25: UNICODEESCAPES
            	    {
            	    mUNICODEESCAPES(); 

            	    }
            	    break;
            	case 3 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:188:42: SPECIALESCAPES
            	    {
            	    mSPECIALESCAPES(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "URI"
    public final void mURI() throws RecognitionException {
        try {
            int _type = URI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:3: ( '<' ( URI_CHARS | UNICODEESCAPES | SPECIALESCAPES )* '>' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:5: '<' ( URI_CHARS | UNICODEESCAPES | SPECIALESCAPES )* '>'
            {
            match('<'); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:9: ( URI_CHARS | UNICODEESCAPES | SPECIALESCAPES )*
            loop6:
            do {
                int alt6=4;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='!' && LA6_0<=';')||LA6_0=='='||(LA6_0>='?' && LA6_0<='[')||(LA6_0>=']' && LA6_0<='~')) ) {
                    alt6=1;
                }
                else if ( (LA6_0=='\\') ) {
                    int LA6_3 = input.LA(2);

                    if ( (LA6_3=='U'||LA6_3=='u') ) {
                        alt6=2;
                    }
                    else if ( (LA6_3=='\"'||LA6_3=='\\'||LA6_3=='n'||LA6_3=='r'||LA6_3=='t') ) {
                        alt6=3;
                    }


                }


                switch (alt6) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:10: URI_CHARS
            	    {
            	    mURI_CHARS(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:22: UNICODEESCAPES
            	    {
            	    mUNICODEESCAPES(); 

            	    }
            	    break;
            	case 3 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:192:39: SPECIALESCAPES
            	    {
            	    mSPECIALESCAPES(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URI"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:196:3: ( COMMENTSTART (~ ( '\\n' | '\\r' ) )* )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:196:5: COMMENTSTART (~ ( '\\n' | '\\r' ) )*
            {
            mCOMMENTSTART(); 
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:196:18: (~ ( '\\n' | '\\r' ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='\u0000' && LA7_0<='\t')||(LA7_0>='\u000B' && LA7_0<='\f')||(LA7_0>='\u000E' && LA7_0<='\uFFFF')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:196:19: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "STRING_CHARS"
    public final void mSTRING_CHARS() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:212:3: ( SPACE | '<' | '>' | CHARS_BASE )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            {
            if ( (input.LA(1)>=' ' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "STRING_CHARS"

    // $ANTLR start "UNICODEESCAPES"
    public final void mUNICODEESCAPES() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:219:3: ( '\\\\u' HEX HEX HEX HEX | '\\\\U' HEX HEX HEX HEX HEX HEX HEX HEX )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='\\') ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1=='u') ) {
                    alt8=1;
                }
                else if ( (LA8_1=='U') ) {
                    alt8=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:219:5: '\\\\u' HEX HEX HEX HEX
                    {
                    match("\\u"); 

                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 

                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:220:5: '\\\\U' HEX HEX HEX HEX HEX HEX HEX HEX
                    {
                    match("\\U"); 

                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 
                    mHEX(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "UNICODEESCAPES"

    // $ANTLR start "SPECIALESCAPES"
    public final void mSPECIALESCAPES() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:224:3: ( '\\\\t' | '\\\\n' | '\\\\\"' | '\\\\\\\\' | '\\\\r' )
            int alt9=5;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\\') ) {
                switch ( input.LA(2) ) {
                case 't':
                    {
                    alt9=1;
                    }
                    break;
                case 'n':
                    {
                    alt9=2;
                    }
                    break;
                case '\"':
                    {
                    alt9=3;
                    }
                    break;
                case '\\':
                    {
                    alt9=4;
                    }
                    break;
                case 'r':
                    {
                    alt9=5;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:224:5: '\\\\t'
                    {
                    match("\\t"); 


                    }
                    break;
                case 2 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:225:5: '\\\\n'
                    {
                    match("\\n"); 


                    }
                    break;
                case 3 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:226:5: '\\\\\"'
                    {
                    match("\\\""); 


                    }
                    break;
                case 4 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:227:5: '\\\\\\\\'
                    {
                    match("\\\\"); 


                    }
                    break;
                case 5 :
                    // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:228:5: '\\\\r'
                    {
                    match("\\r"); 


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "SPECIALESCAPES"

    // $ANTLR start "HEX"
    public final void mHEX() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:232:3: ( '0' .. '9' | 'A' .. 'Z' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX"

    // $ANTLR start "URI_CHARS"
    public final void mURI_CHARS() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:237:3: ( CHARS_BASE | '\\u0022' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            {
            if ( (input.LA(1)>='!' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='?' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "URI_CHARS"

    // $ANTLR start "SPACE"
    public final void mSPACE() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:242:3: ( '\\u0020' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:242:5: '\\u0020'
            {
            match(' '); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "SPACE"

    // $ANTLR start "CHARS_BASE"
    public final void mCHARS_BASE() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:246:3: ( '\\u0021' | '\\u0023' .. '\\u003B' | '\\u003D' | '\\u003F' .. '\\u005B' | '\\u005D' .. '\\u007E' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<=';')||input.LA(1)=='='||(input.LA(1)>='?' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "CHARS_BASE"

    // $ANTLR start "COMMENTSTART"
    public final void mCOMMENTSTART() throws RecognitionException {
        try {
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:254:3: ( '#' )
            // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:254:5: '#'
            {
            match('#'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "COMMENTSTART"

    public void mTokens() throws RecognitionException {
        // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:8: ( T__19 | LANGTAG | NAMEDNODE | WS | DOT | STRING | URI | COMMENT )
        int alt10=8;
        switch ( input.LA(1) ) {
        case '^':
            {
            alt10=1;
            }
            break;
        case '@':
            {
            alt10=2;
            }
            break;
        case '_':
            {
            alt10=3;
            }
            break;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt10=4;
            }
            break;
        case '.':
            {
            alt10=5;
            }
            break;
        case '\"':
            {
            alt10=6;
            }
            break;
        case '<':
            {
            alt10=7;
            }
            break;
        case '#':
            {
            alt10=8;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 10, 0, input);

            throw nvae;
        }

        switch (alt10) {
            case 1 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:10: T__19
                {
                mT__19(); 

                }
                break;
            case 2 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:16: LANGTAG
                {
                mLANGTAG(); 

                }
                break;
            case 3 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:24: NAMEDNODE
                {
                mNAMEDNODE(); 

                }
                break;
            case 4 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:34: WS
                {
                mWS(); 

                }
                break;
            case 5 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:37: DOT
                {
                mDOT(); 

                }
                break;
            case 6 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:41: STRING
                {
                mSTRING(); 

                }
                break;
            case 7 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:48: URI
                {
                mURI(); 

                }
                break;
            case 8 :
                // /home/andreas/workspace/ANTLRTester/antlr-files/NQuad.g:1:52: COMMENT
                {
                mCOMMENT(); 

                }
                break;

        }

    }


 

}