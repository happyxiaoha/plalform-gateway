/*
 * Copyright 2014 Alexey Plotnik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dc.appengine.router.nginxparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxComment;
import com.dc.appengine.router.nginxparser.NgxEntry;
import com.dc.appengine.router.nginxparser.NgxIfBlock;
import com.dc.appengine.router.nginxparser.NgxParam;
import com.dc.appengine.router.nginxparser.NgxToken;
import com.dc.appengine.router.nginxparser.antlr.NginxLexer;
import com.dc.appengine.router.nginxparser.antlr.NginxListenerImpl;
import com.dc.appengine.router.nginxparser.antlr.NginxParser;
import com.dc.appengine.router.nginxparser.javacc.NginxConfigParser;
import com.dc.appengine.router.nginxparser.javacc.ParseException;

/**
 * Main class that describes Nginx config
 */
public class NgxConfig extends NgxBlock {

    public static final Class<? extends NgxEntry> PARAM = NgxParam.class;
    public static final Class<? extends NgxEntry> COMMENT = NgxComment.class;
    public static final Class<? extends NgxEntry> BLOCK = NgxBlock.class;
    public static final Class<? extends NgxEntry> IF = NgxIfBlock.class;

    /**
     * Parse an existing config
     *
     * @param path Path to config file
     * @return Config object
     * @throws IOException
     */
    public static NgxConfig read(String path) throws IOException {
        FileInputStream input = new FileInputStream(path);
        return read(input);
    }

    public static NgxConfig read(InputStream in) throws IOException {
        return readAntlr(in);
    }

    /**
     * Read config from existing stream
     * @param input stream to read from
     * @return Config object
     * @throws IOException
     * @throws ParseException
     */
    public static NgxConfig readJavaCC(InputStream input) throws IOException, ParseException {
        NginxConfigParser parser = new NginxConfigParser(input);
        return parser.parse();
    }


    public static NgxConfig readAntlr(InputStream in) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(in);
        NginxLexer lexer = new NginxLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NginxParser parser = new NginxParser(tokens);
        ParseTreeWalker walker = new ParseTreeWalker();

        ParseTree tree = parser.config(); // begin parsing at init rule
        NginxListenerImpl listener = new NginxListenerImpl();
        walker.walk(listener, tree);

        return listener.getResult();
    }

    @Override
    public Collection<NgxToken> getTokens() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void addValue(NgxToken token) {
        throw new IllegalStateException("Not implemented");
    }

    public String toString() {
        return "Nginx Config (" + getEntries().size() + " entries)";
    }

}
