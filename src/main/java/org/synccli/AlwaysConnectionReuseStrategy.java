package org.synccli;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public class AlwaysConnectionReuseStrategy implements ConnectionReuseStrategy {

    public static final AlwaysConnectionReuseStrategy INSTANCE = new AlwaysConnectionReuseStrategy();

    public AlwaysConnectionReuseStrategy() {
        super();
    }

    public boolean keepAlive(final HttpResponse response, final HttpContext context) {
        return true;
    }
}
