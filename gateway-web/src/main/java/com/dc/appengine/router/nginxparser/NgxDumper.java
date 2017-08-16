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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxEntry;
import com.dc.appengine.router.nginxparser.NgxEntryType;
import com.dc.appengine.router.nginxparser.NgxIfBlock;

/**
 * 作用：NgxDumper is used to serialize an existing or manually created NgxConfig object
 */
public class NgxDumper {

    private NgxConfig config;
    private final static int PAD_SIZE = 2;
    private final static String PAD_SYMBOL = "  ";
    private final static String LBRACE = "{";
    private final static String RBRACE = "}";
    private final static String LF = "\n";
    private final static String CRLF = "\r\n";

    
    /**
     * 
     */
    public NgxDumper(NgxConfig config) {
        this.config = config;
    }

    /**
     * Converts config int String
     * @return Serialized config
     */
    public String dump() {
        StringWriter writer = new StringWriter();
        writeToStream(config, new PrintWriter(writer), 0);
        return writer.toString();
    }

    /**
     * Serializes config and sends result to the provided OutputStream
     * 当out中有内容时，new NgxDumper(conf).dump(new FileOutputStream(f2String));
     * 如上的conf中的内容将覆盖掉out中内容。
     * @param out stream to write to
     */
    public void dump(OutputStream out) {
        writeToStream(config, new PrintWriter(out), 0);
    }

    /**
     * public void dump调用writeToStream()
     */
    private void writeToStream(NgxBlock config, PrintWriter writer, int level) {
        for (NgxEntry entry : config) {
            NgxEntryType type = NgxEntryType.fromClass(entry.getClass());
            switch (type) {
                case BLOCK:
                    NgxBlock block = (NgxBlock) entry;
                    writer.append(getOffset(level))
                            .append(block.toString())
                            .append(getLineEnding());
                    writeToStream(block, writer, level + 1);
                    writer
                            .append(getOffset(level))
                            .append(RBRACE)
                            .append(getLineEnding());
                    break;
                case IF:
                    NgxIfBlock ifBlock = (NgxIfBlock) entry;
                    writer
                            .append(getOffset(level))
                            .append(ifBlock.toString())
                            .append(getLineEnding());
                    writeToStream(ifBlock, writer, level + 1);
                    writer
                            .append(getOffset(level))
                            .append(LBRACE)
                            .append(getLineEnding());
                case COMMENT:
                case PARAM:
                    writer
                            .append(getOffset(level))
                            .append(entry.toString())
                            .append(getLineEnding());
                    break;
            }
        }
        writer.flush();
    }

    public String getOffset(int level) {
        String offset = "";
        for (int i = 0; i < level; i++) {
            offset += PAD_SYMBOL;
        }
        return offset;
    }

    public String getLineEnding() {
        return LF;
    }
}
