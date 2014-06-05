package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.serializer.TransferDictionary;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class CopyTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        CopyTransfer t = new CopyTransfer(new Host(new SFTPProtocol(), "t"),
                new Host(new FTPProtocol(), "t"), Collections.<Path, Path>singletonMap(test, new Path("d", EnumSet.of(Path.Type.file))));
        t.addSize(4L);
        t.addTransferred(3L);
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.roots, serialized.getRoots());
        assertEquals(t.files, ((CopyTransfer) serialized).files);
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testAction() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        CopyTransfer t = new CopyTransfer(new Host(new SFTPProtocol(), "t"),
                new Host(new FTPProtocol(), "t"), Collections.<Path, Path>singletonMap(test, new Path("d", EnumSet.of(Path.Type.file))));
        assertEquals(TransferAction.overwrite, t.action(new SFTPSession(new Host(new SFTPProtocol(), "t")), false, true, new DisabledTransferPrompt()));
    }

    @Test
    public void testList() throws Exception {
        Transfer t = new CopyTransfer(new Host("s"), new Host("t"), Collections.singletonMap(
                new Path("/s", EnumSet.of(Path.Type.directory)),
                new Path("/t", EnumSet.of(Path.Type.directory))));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("/s/c", EnumSet.of(Path.Type.file)));
                return children;
            }
        };
        assertEquals(Collections.<TransferItem>singletonList(new TransferItem(new Path("/s/c", EnumSet.of(Path.Type.file)))),
                t.list(session, new Path("/s", EnumSet.of(Path.Type.directory)), null, new DisabledListProgressListener())
        );
    }
}
