package com.dc.appengine.router.test;

import java.util.Iterator;

import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;
import com.dc.appengine.router.nginxparser.NgxEntry;

public class ParseTestBase {
    public NgxConfig parse(String path) throws Exception {
        return TestUtils.parseAntlr(path);
    }

    public NgxEntry getFirstParam(String path) throws Exception {
        Iterator<NgxEntry> it = parse(path).getEntries().iterator();
        return it.next();
    }

    public void debug(NgxConfig parsedConfig) {
        NgxDumper ngxDumper = new NgxDumper(parsedConfig);
        ngxDumper.dump(System.out);
    }
}
