package ch.cyberduck;

/*
 *  ch.cyberduck.Preferences.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

/**
 * Holding all application preferences. Default values get overwritten when loading
 * the <code>Cyberduck.PREFERENCES_FILE</code>.
 * Singleton class.
 */
public class Preferences extends Properties {
    private static Preferences current = null;
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /**
        * Use #instance instead.
     */
    private Preferences() {
        super();
    }

    /**
     * @return The singleton instance of me.
     */
    public static Preferences instance() {
        if(current == null) {
            Cyberduck.PREFS_DIRECTORY.mkdir();
            current = new Preferences();
            current.setDefaults();
            current.load();

            //initialize SSL
            String strVendor = System.getProperty("java.vendor");
            String strVersion = System.getProperty("java.version");
            //Assumes a system version string of the form:
            //[major].[minor].[release]��(eg. 1.2.2)
            Double dVersion = new Double(strVersion.substring(0, 3));
            //If we are running in a MS environment, use the MS stream handler.
            if(-1 < strVendor.indexOf("Microsoft")) {
                try {
                    Class clsFactory = Class.forName("com.ms.net.wininet.WininetStreamHandlerFactory");
                    if (null != clsFactory)
                        URL.setURLStreamHandlerFactory((URLStreamHandlerFactory)clsFactory.newInstance());
                }
                catch(Exception cfe) { //InstantiationException and ClassNotFoundException
                    System.err.println("WARNING: Unable to load the Microsoft SSL stream handler.");
                }
                //If the stream handler factory has
                //already been successfully set
                //make sure our flag is set and eat the error
                /*
                 catch(Error err) {
                     m_bStreamHandlerSet = true;
                 }
                 */
            }
            //If we are in a normal Java environment,
            //try to use the JSSE handler.
            //NOTE:��JSSE requires 1.2 or better
            else if( 1.2 <= dVersion.doubleValue() ) {
                System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
                try {
                    //if we have the JSSE provider available, /and it has not already been set, add it as a new provide to the Security class.
                    Class clsFactory = Class.forName("com.sun.net.ssl.internal.ssl.Provider");
                    if( (null != clsFactory) && (null == Security.getProvider("SunJSSE")) )
                        Security.addProvider((Provider)clsFactory.newInstance());
                }
                catch(Exception cfe) {
                    System.err.println("WARNING: Unable to load the JSSE SSL stream handler.");
                }
            }
        }
        return current;
    }
    
    public void setProperty(String property, boolean v) {
        Cyberduck.DEBUG("[Preferences] setProperty(" + property + ", " + v + ")");
        String value = "false";
        if (v) {
            value = "true";
        }
        this.put(property, value);
    }

    /*
    public Object setProperty(String property, String value) {
//        Cyberduck.DEBUG("[Preferences] setProperty(" + property + ", " + value + ")");
        this.put(property, value);
        return null;
    }
     */
    
    public void setProperty(String property, int v) {
        Cyberduck.DEBUG("[Preferences] setProperty(" + property + ", " + v + ")");
        String value = String.valueOf(v);
        this.put(property, value);
    }

    /**
     * setting the default prefs values
     */
    public void setDefaults() {
        Cyberduck.DEBUG("[Preferences] setDefaults()");

        this.setProperty("laf.default", javax.swing.UIManager.getSystemLookAndFeelClassName());
        //this.setProperty("laf.default", javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        this.setProperty("bookmarks.default", "My Bookmarks");
        this.setProperty("interface.multiplewindow", "false");
        this.setProperty("interface.error-dialog", "false");
        //Paths
        this.setProperty("download.path", System.getProperty("user.dir") + "/");

        // font sizes
        String font_small = "10";
        String font_normal = "12";
        if(System.getProperty("java.vendor").indexOf("Microsoft") != -1) {
            font_small = "12";
            font_normal = "12";
        }
        this.setProperty("font.small", font_small);
        this.setProperty("font.normal", font_normal);
        //Sound clips
        this.setProperty("status.sound.stop", "false");
        this.setProperty("status.sound.start", "true");
        this.setProperty("status.sound.complete", "true");
    
        //this.setProperty("files.encode", "true");
        this.setProperty("files.postprocess", "false");
        
        //BookmarkTable properties
        this.setProperty("table.save", "true");
        //BookmarkTable column locations
        /*
        this.setProperty("table.column0.position", "0");
        this.setProperty("table.column1.position", "1");
        this.setProperty("table.column2.position", "2");
        this.setProperty("table.column3.position", "3");
         */
        //BookmarkTable column widths
        this.setProperty("table.column0.width", "12");
        this.setProperty("table.column1.width", "500");
        this.setProperty("table.column2.width", "15");
        this.setProperty("table.column3.width", "150");
        
        //Duplicate files
        this.setProperty("duplicate.ask", "true");
        this.setProperty("duplicate.similar", "false");
        this.setProperty("duplicate.resume", "false");
        this.setProperty("duplicate.overwrite", "false");
        
        //Connection
        //this.setProperty("connection.log", "true");
        this.setProperty("connection.log.file", "cyberduck.connection.log");
        this.setProperty("connection.buffer", "4096");
        this.setProperty("connection.log.speech", "false");
        this.setProperty("connection.protocol.default", "sftp");
//        this.setProperty("connection.transfertype.default", "binary");
        this.setProperty("connection.timeout", "2"); // seconds
        this.setProperty("connection.timeout.default", "2"); // seconds
        this.setProperty("connection.proxy", "false");
        this.setProperty("connection.proxy.host", "proxy");
        this.setProperty("connection.proxy.port", "9999");
        this.setProperty("connection.proxy.authenticate", "false");
        this.setProperty("connection.proxy.username", "user");
        this.setProperty("connection.proxy.password", "pass");
        
        //ftp properties
        this.setProperty("ftp.showHidden", "false");
        this.setProperty("ftp.login.name", System.getProperty("user.name"));
        this.setProperty("ftp.login.anonymous.name", "anonymous");
        this.setProperty("ftp.login.anonymous.pass", "anonymous");
        this.setProperty("ftp.active", "true");
        this.setProperty("ftp.passive", "false");
        this.setProperty("ftp.listing.showType", "true");
        this.setProperty("ftp.listing.showFilenames", "true");
        this.setProperty("ftp.listing.showSize", "true");
        this.setProperty("ftp.listing.showDate", "true");
        this.setProperty("ftp.listing.showOwner", "true");
        this.setProperty("ftp.listing.showAccess", "false");
        
        //frame sizes
        this.setProperty("frame.width", "560");
        this.setProperty("frame.height", "480");
        this.setProperty("frame.x", getXLocation(560));
        this.setProperty("frame.y", getYLocation(480));
        this.setProperty("transcriptdialog.width", "520");
        this.setProperty("transcriptdialog.height", "550");
        this.setProperty("transcriptdialog.x", getXLocation(520));
        this.setProperty("transcriptdialog.y", getYLocation(550));
        this.setProperty("logdialog.width", "500");
        this.setProperty("logdialog.height", "300");
        this.setProperty("logdialog.x", getXLocation(500));
        this.setProperty("logdialog.y", getYLocation(300));
        this.setProperty("preferencesdialog.width", "500");
        this.setProperty("preferencesdialog.height", "485");
        this.setProperty("preferencesdialog.x", getXLocation(500));
        this.setProperty("preferencesdialog.y", getYLocation(485));
        this.setProperty("newconnectiondialog.width", "500");
        this.setProperty("newconnectiondialog.height", "300");
        this.setProperty("newconnectiondialog.x", getXLocation(500));
        this.setProperty("newconnectiondialog.y", getYLocation(300));
        
        //Status messages in detail panel
        //this.setProperty("statuspanel.localpath", "true");
        //this.setProperty("statuspanel.errormessage", "true");
        //this.setProperty("statuspanel.progressmessage", "true");
        //this.setProperty("statuspanel.transcriptmessage", "false");
    }
    
    private String getXLocation(int componentWidth) {
        return new Integer((screenSize.width/2) - (componentWidth/2)).toString();
    }

    private String getYLocation(int componentHeight) {
        return new Integer((screenSize.height/2) - (componentHeight/2)).toString();
    }

    public String getProperty(String property) {
        Cyberduck.DEBUG("[Preferences] getProperty(" + property + ")");
        String value = super.getProperty(property);
        if(value == null)
            throw new IllegalArgumentException("No property with key '" + property.toString() + "'");
        return value;
    }
        
    /**
     * Save preferences into user home
     */
    public void store() {
        Cyberduck.DEBUG("[Preferences] store()");
        try {
            FileOutputStream output = new FileOutputStream(new File(Cyberduck.PREFS_DIRECTORY, Cyberduck.PREFERENCES_FILE));
            this.store(output, "Cyberduck properties - YOU SHOULD NOT EDIT THIS FILE");
            output.close();
        }
        catch(IOException e) {
            System.err.println("[Preferences] Could not save current preferences.\n" + e.getMessage());
        }
    }

    /**
     * Overriding the default values with prefs from the last session.
     */
    public void load() {
        Cyberduck.DEBUG("[Preferences] load()");
        try {
            File prefs = new File(Cyberduck.PREFS_DIRECTORY, Cyberduck.PREFERENCES_FILE);
            if (prefs.exists()) {
                this.load(new FileInputStream(prefs));
            }
            else {
                System.err.println("[Preferences] Could not load current preferences.");
            }
        }
        catch(IOException e) {
            System.err.println("[Preferences] Could not load current preferences.\n" + e.getMessage());
        }
    }

    public void list() {
        this.list(System.out);
    }
}
