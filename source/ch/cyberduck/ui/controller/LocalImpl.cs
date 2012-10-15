﻿// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Collections;
using Microsoft.Win32;
using ch.cyberduck.core;
using java.util;
using org.apache.commons.io;
using org.apache.log4j;
using File = java.io.File;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LocalImpl : Local
    {
        protected const int ErrorAccessDenied = 5;
        protected const int ErrorFileNotFound = 2;
        private static readonly Logger Log = Logger.getLogger(typeof (LocalImpl).FullName);

        private Attributes info;

        public LocalImpl(string parent, string name) : base(parent, name)
        {
            ;
        }

        public LocalImpl(Local parent, string name) : base(parent, name)
        {
            ;
        }

        public LocalImpl(string path) : base(path)
        {
            ;
        }

        public LocalImpl(File path) : base(path)
        {
            ;
        }


        public override char getPathDelimiter()
        {
            return '\\';
        }

        public override bool isRoot()
        {
            return getAbsolute().Equals(Directory.GetDirectoryRoot(getAbsolute()));
        }

        public override bool exists()
        {
            if (System.IO.File.Exists(getAbsolute()))
            {
                return true;
            }
            return Directory.Exists(getAbsolute());
        }

        public override void writeUnixPermission(Permission p, bool b)
        {
            ;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>True if application was found to open the file with</returns>
        public override bool open()
        {
            if (Log.isDebugEnabled())
            {
                Log.debug("open():" + getAbsolute());
            }
            return Utils.StartProcess(getAbsolute());
        }

        public override void bounce()
        {
            ;
        }

        public static string kind(string extension)
        {
            Shell32.SHFILEINFO shinfo = new Shell32.SHFILEINFO();
            IntPtr hSuccess = Shell32.SHGetFileInfo(extension, 0, ref shinfo, (uint) Marshal.SizeOf(shinfo),
                                                    Shell32.SHGFI_TYPENAME | Shell32.SHGFI_USEFILEATTRIBUTES);
            if (hSuccess != IntPtr.Zero)
            {
                return Convert.ToString(shinfo.szTypeName.Trim());
            }
            return null;
        }

        public override string kind()
        {
            // Native file type mapping
            String kind = LocalImpl.kind(getExtension());
            if (string.IsNullOrEmpty(kind))
            {
                return base.kind();
            }
            return kind;
        }

        public override Attributes attributes()
        {
            if (null == info)
            {
                info = new FileInfoAttributes(this);
            }
            return info;
        }

        /// <summary>
        /// Delete to trash is not supported yet.
        /// </summary>
        /// <see cref="http://social.msdn.microsoft.com/forums/en-US/netfxbcl/thread/f2411a7f-34b6-4f30-a25f-9d456fe1c47b/"/>
        /// <see cref="http://stackoverflow.com/questions/222463/is-it-possible-with-java-to-delete-to-the-recycle-bin"/>
        public override void trash()
        {
            delete();
        }

        public override bool reveal()
        {
            if (exists())
            {
                //select first file downloaded. We could just open the containing folder alternatively.
                return Utils.StartProcess("explorer.exe", "/select, " + getAbsolute());
            }
            return false;
        }

        public override void setPath(string parent, string name)
        {
            string p = MakeValidPath(parent);
            string n = MakeValidFilename(name);
            base.setPath(p, n);
        }

        protected override void setPath(string filename)
        {
            string parent = Path.Combine(FilenameUtils.getPrefix(filename),
                                         MakeValidPath(FilenameUtils.getPath(filename)));
            string name = MakeValidFilename(FilenameUtils.getName(filename));
            base.setPath(parent + name);
        }

        private string MakeValidPath(string path)
        {
            if (Utils.IsNotBlank(path))
            {
                path = FilenameUtils.separatorsToSystem(path);
                string prefix = FilenameUtils.getPrefix(path);
                if (!path.EndsWith(Path.DirectorySeparatorChar.ToString()))
                {
                    path = path + Path.DirectorySeparatorChar;
                }
                path = FilenameUtils.getPath(path);

                StringBuilder sb = new StringBuilder();
                if (Utils.IsNotBlank(prefix))
                {
                    sb.Append(prefix);
                }
                path = FilenameUtils.separatorsToSystem(path);
                string[] parts = path.Split(Path.DirectorySeparatorChar);
                foreach (string part in parts)
                {
                    string cleanpart = part;
                    foreach (char c in Path.GetInvalidFileNameChars())
                    {
                        cleanpart = cleanpart.Replace(c.ToString(), "_");
                    }
                    sb.Append(cleanpart);
                    if (!parts[parts.Length - 1].Equals(part))
                    {
                        sb.Append(Path.DirectorySeparatorChar);
                    }
                }
                return sb.ToString();
            }
            return path;
        }

        private string MakeValidFilename(string name)
        {
            if (Utils.IsNotBlank(name))
            {
                foreach (char c in Path.GetInvalidFileNameChars())
                {
                    name = name.Replace(c.ToString(), "_");
                }
            }
            return name;
        }

        public static void Register()
        {
            LocalFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : LocalFactory
        {
            protected override Local create(Local parent, string name)
            {
                return new LocalImpl(parent, name);
            }

            protected override Local create(string parent, string name)
            {
                return new LocalImpl(parent, name);
            }

            protected override Local create(string path)
            {
                return new LocalImpl(path);
            }

            protected override Local create(File path)
            {
                return new LocalImpl(path);
            }

            protected override object create()
            {
                return new LocalImpl(Environment.GetEnvironmentVariable("HOME"));
            }
        }

        private class FileInfoAttributes : LocalAttributes
        {
            private Local file;

            public FileInfoAttributes(Local l) : base(l)
            {
                file = l;
            }
        }
    }
}