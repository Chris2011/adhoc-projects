/* Copyright (C) 2013 Tim Boudreau

 Permission is hereby granted, free of charge, to any person obtaining a copy 
 of this software and associated documentation files (the "Software"), to 
 deal in the Software without restriction, including without limitation the 
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 sell copies of the Software, and to permit persons to whom the Software is 
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.timboudreau.adhoc.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class AdhocProject implements Project, ActionProvider {
    private FileObject dir;
    private final AuxPropertiesImpl aux = new AuxPropertiesImpl();
    private final EncQueryImpl encodingQuery;
    public static final String CUSTOMIZE_COMMAND = "customize";
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private final PI info = new PI();
    private final AdhocProjectCustomizerProvider customizer = new AdhocProjectCustomizerProvider(this);
    public static final String TYPE_NAME = "com-timboudreau-adhoc-project";

    public AdhocProject(FileObject dir, ProjectState state) throws IOException {
        this.encodingQuery = new EncQueryImpl();
        this.dir = dir;
    }

    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this,
                aux,
                encodingQuery,
                info,
                customizer,
                new AdhocProjectLogicalView(this));
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
            CUSTOMIZE_COMMAND
        };
    }

    @Override
    public void invokeAction(String string, Lookup lkp) throws IllegalArgumentException {
        switch (string) {
            case CUSTOMIZE_COMMAND:
                customizer.showCustomizer();
                break;
        }
    }

    @Override
    public boolean isActionEnabled(String string, Lookup lkp) throws IllegalArgumentException {
        switch (string) {
            case CUSTOMIZE_COMMAND:
                return true;
        }

        //do nothing
        return false;
    }

    private class PI implements ProjectInformation {

        private final PropertyChangeSupport supp = new PropertyChangeSupport(this);

        @Override
        public String getName() {
            return dir.getName();
        }

        @Override
        public String getDisplayName() {
            try {
                Preferences p = preferences(false);
                if (p == null) {
                    return getName();
                }
                String s = p.get("name", getName());
                return s.trim().isEmpty() ? getName() : s.trim();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            return getName();
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/swing/plaf/resources/hidpi-folder-closed.png", false);
        }

        @Override
        public Project getProject() {
            return AdhocProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
            supp.addPropertyChangeListener(pl);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
            supp.removePropertyChangeListener(pl);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pl) {
        supp.addPropertyChangeListener(pl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pl) {
        supp.removePropertyChangeListener(pl);
    }

    public void setEncoding(Charset charset) {
        try {
            String name = charset == null ? "UTF-8" : charset.name();
            preferences(true).put("charset", name);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Charset getEncoding() {
        try {
            Preferences p = preferences(false);
            if (p != null) {
                String name = p.get("charset", "UTF-8");
                return Charset.forName(name);
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Charset.forName("UTF-8");
    }

    private String prefsNodeName() {
        String delim;
        if (Utilities.isWindows()) {
            delim = "--";
        } else {
            delim = ";;";
        }
        String nodeName = delim + getProjectDirectory().getPath().replace(
                '/', '_').replace('\\', '_').replace(":", "~");
        return nodeName;
    }

    Preferences preferences(boolean create) throws BackingStoreException {
        Preferences prefs = NbPreferences.forModule(AdhocProjectNode.class);
        String n = prefsNodeName();
        if (prefs.nodeExists(n) || create) {
            Preferences forProject = prefs.node(n);
            return forProject;
        }
        return null;
    }

    public Preferences forFile(FileObject fo, String string) {
        try {
            Preferences p = preferences(true);
            return p.node("__formatting").node(string);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            return NbPreferences.forModule(AdhocProject.class);
        }
    }

    public Preferences forDocument(Document dcmnt, String string) {
        return forFile(null, string);
    }

    private class EncQueryImpl extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject fo) {
            return AdhocProject.this.getEncoding();
        }
    }

    private class AuxPropertiesImpl implements AuxiliaryProperties {

        private Preferences p(boolean shared) {
            if (!shared) {
                try {
                    return preferences(true).node("__aux");
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return NbPreferences.forModule(AuxPropertiesImpl.class);
        }

        @Override
        public String get(String string, boolean bln) {
            return p(bln).get(string, null);
        }

        @Override
        public void put(String string, String string1, boolean bln) {
            Preferences prefs = p(bln);
            if (string1 == null) {
                prefs.remove(string);
            } else {
                prefs.put(string, string1);
            }
        }

        @Override
        public Iterable<String> listKeys(boolean bln) {
            try {
                return Arrays.asList(p(bln).childrenNames());
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
                return Collections.emptySet();
            }
        }
    }
}
