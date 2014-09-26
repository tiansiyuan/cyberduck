package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.TranscriptListener;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

/**
 * @version $Id$
 */
public class LoggingHttpRequestExecutor extends HttpRequestExecutor {

    private TranscriptListener listener;

    public LoggingHttpRequestExecutor(final TranscriptListener listener) {
        this.listener = listener;
    }

    @Override
    public void preProcess(final HttpRequest request, final HttpProcessor processor, final HttpContext context)
            throws HttpException, IOException {
        listener.log(true, request.getRequestLine().toString());
        for(Header header : request.getAllHeaders()) {
            listener.log(true, header.toString());
        }
        super.preProcess(request, processor, context);
    }

    @Override
    public void postProcess(final HttpResponse response, final HttpProcessor processor, final HttpContext context)
            throws HttpException, IOException {
        listener.log(false, response.getStatusLine().toString());
        for(Header header : response.getAllHeaders()) {
            listener.log(false, header.toString());
        }
        super.postProcess(response, processor, context);
    }
}
