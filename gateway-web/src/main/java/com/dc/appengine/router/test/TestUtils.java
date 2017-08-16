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

package com.dc.appengine.router.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;


public class TestUtils {
    public static NgxConfig parseJavaCC(String path) throws Exception {
        InputStream input = getStream(path);
        return NgxConfig.readJavaCC(input);
    }

    public static NgxConfig parseAntlr(String path) throws Exception {
        return NgxConfig.read(getStream(path));
    }

    public static InputStream getStream(String path) {
       // return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    	File file = new File(path);
    	InputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return in;
    }

    public static String dump(String path) throws Exception {
        NgxConfig conf = TestUtils.parseAntlr(path);
        NgxDumper dumper = new NgxDumper(conf);
        return dumper.dump();
    }
}
