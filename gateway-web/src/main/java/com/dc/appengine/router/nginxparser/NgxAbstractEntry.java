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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class NgxAbstractEntry implements NgxEntry {
    private Collection<NgxToken> tokens = new ArrayList<NgxToken>();

    /**
     * @param rawValues
     */
    public NgxAbstractEntry(String... rawValues) {
        for (String val : rawValues) {
            tokens.add(new NgxToken(val));
        }
    }

    public Collection<NgxToken> getTokens() {
        return tokens;
    }

    /**
     * @param token
     */
    public void addValue(NgxToken token) {
        tokens.add(token);
    }

    /**
     * @param value
     */
    public void addValue(String value) {
        addValue(new NgxToken(value));
    }

    /* (non-Javadoc)
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (NgxToken value : tokens) {
            builder.append(value).append(" ");
        }
        String s = builder.toString();
        return s.substring(0, s.length()-1);
    }

    /**
     * @return
     */
    public String getName() {
        if (getTokens().isEmpty())
            return null;

        return getTokens().iterator().next().toString();
    }

    /**
     * @return
     */
    public List<String> getValues() {
        ArrayList<String> values = new ArrayList<String>();
        if (getTokens().size() < 2)
            return values;

        Iterator<NgxToken> it = getTokens().iterator();
        it.next();
        while(it.hasNext()) {
            values.add(it.next().toString());
        }
        return values;
    }

    /**
     * @return
     */
    public String getValue() {
        Iterator<String> iterator = getValues().iterator();
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
              builder.append(' ');
            }
        }
        return builder.toString();
    }
    
	public void setValue(String... newValues) {
        if (getTokens().size() < 2)
            return;

        Iterator<NgxToken> it = getTokens().iterator();
        String key = it.next().toString();
		getTokens().clear();
		
		tokens.add(new NgxToken(key));
		for (String val : newValues) {
            tokens.add(new NgxToken(val));
        }
		
	}
    

}
