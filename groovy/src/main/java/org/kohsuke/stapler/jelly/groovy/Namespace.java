/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.GroovyObjectSupport;
import groovy.xml.QName;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.SAXException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Kohsuke Kawaguchi
 */
public class Namespace extends GroovyObjectSupport {
    private final JellyBuilder builder;
    private final String nsUri;
    private final String prefix;

    // note that the mapping from nsUri to TagLibrary
    // may change depending on the scope, so we can't cache TagLibrary

    Namespace(JellyBuilder builder, String nsUri, String prefix) {
        this.builder = builder;
        this.nsUri = nsUri;
        this.prefix = prefix==null ? "" : prefix;
    }

    public Object invokeMethod(String localName, Object args) {
        builder.doInvokeMethod(new QName(nsUri,localName,prefix),args);
        return null;
    }

    public void startPrefixMapping(XMLOutput output) throws SAXException {
        output.startPrefixMapping(prefix,nsUri);
    }

    public void endPrefixMapping(XMLOutput output) throws SAXException {
        output.endPrefixMapping(prefix);
    }

    /**
     * Creates a type-safe invoker for calling taglibs.
     */
    public <T extends TypedTagLibrary> T createInvoker(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass()==Object.class)
                    return method.invoke(this,args);

                // invoke methods
                builder.doInvokeMethod(new QName(nsUri,method.getName(),prefix),args);
                return null;
            }
        }));
    }
}
