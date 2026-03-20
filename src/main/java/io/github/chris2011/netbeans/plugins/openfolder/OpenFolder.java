/*
 * Copyright 2025 Christian Lenz <christian.lenz@gmx.net>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.chris2011.netbeans.plugins.openfolder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.openide.util.ImageUtilities;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Project class for "Open Folder as Project" functionality. Shows a merged icon
 * (folder + small NetBeans logo).
 *
 * @author Christian Lenz
 */
public class OpenFolder implements Project, ActionProvider {

    private final FileObject dir;
    private final ProjectState state;
    private final AuxPropertiesImpl aux = new AuxPropertiesImpl();
    private final EncQueryImpl encodingQuery;
    private final PI info = new PI();
    private final OpenFolderCustomizerProvider customizer = new OpenFolderCustomizerProvider(this);
    private final FileOwnerQueryImpl fileOwnerQuery = new FileOwnerQueryImpl();
    private final ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    private final ProjectActionPerformerImpl actionPerformer = new ProjectActionPerformerImpl();
    private final DeleteOperationImpl deleteOperation = new DeleteOperationImpl();
    private final MoveOperationImpl moveOperation = new MoveOperationImpl();
    private Lookup lookup;

    public static final String CUSTOMIZE_COMMAND = "customize";
    public static final String TYPE_NAME = "io-github-chris2011-netbeans-plugins-openfolder";

    public OpenFolder(FileObject dir, ProjectState state) throws IOException {
        this.dir = dir;
        this.state = state;
        this.encodingQuery = new EncQueryImpl();
    }

    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(
                this,
                aux,
                encodingQuery,
                info,
                customizer,
                new OpenFolderLogicalView(this),
                fileOwnerQuery,
                projectConfig,
                actionPerformer,
                deleteOperation,
                moveOperation
            );
        }
        return lookup;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
            CUSTOMIZE_COMMAND,
            COMMAND_DELETE,
            COMMAND_MOVE,
            COMMAND_RENAME
        };
    }

    @Override
    public void invokeAction(String cmd, Lookup lkp) throws IllegalArgumentException {
        switch (cmd) {
            case CUSTOMIZE_COMMAND:
                customizer.showCustomizer();
                break;
            case COMMAND_DELETE:
                DefaultProjectOperations.performDefaultDeleteOperation(this);
                break;
            case COMMAND_MOVE:
                DefaultProjectOperations.performDefaultMoveOperation(this);
                break;
            case COMMAND_RENAME:
                DefaultProjectOperations.performDefaultRenameOperation(this, null);
                break;
        }
    }

    @Override
    public boolean isActionEnabled(String cmd, Lookup lkp) throws IllegalArgumentException {
        switch (cmd) {
            case CUSTOMIZE_COMMAND:
            case COMMAND_DELETE:
            case COMMAND_COPY:
            case COMMAND_MOVE:
            case COMMAND_RENAME:
                return true;
            default:
                return false;
        }
    }

    private class PI implements ProjectInformation {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        @Override
        public String getName() {
            return dir.getName();
        }

        @Override
        public String getDisplayName() {
            return dir.getName();
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("io/github/chris2011/netbeans/plugins/openfolder/open-folder.png", true);
        }

        @Override
        public Project getProject() {
            return OpenFolder.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
            pcs.addPropertyChangeListener(pl);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
            pcs.removePropertyChangeListener(pl);
        }

        public void fireDisplayNameChange(String oldName, String newName) {
            pcs.firePropertyChange(PROP_DISPLAY_NAME, oldName, newName);
        }

        public void fireNameChange(String oldName, String newName) {
            pcs.firePropertyChange(PROP_NAME, oldName, newName);
        }
    }

    // --------------------------------------
    // PropertyChange/Preferences
    // --------------------------------------
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        info.pcs.addPropertyChangeListener(pl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pl) {
        info.pcs.removePropertyChangeListener(pl);
    }

    public void rename(String newName) throws IOException {
        String oldName = dir.getName();
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
            // Save old prefs node before rename
            String oldPrefsNodeName = prefsNodeName();

            // Check if this is the main project
            Project mainProject = OpenProjects.getDefault().getMainProject();
            boolean wasMain = mainProject != null
                && getProjectDirectory().equals(mainProject.getProjectDirectory());

            // Save all documents, then close this project (like NetBeans doMoveProject does)
            LifecycleManager.getDefault().saveAll();
            OpenProjects.getDefault().close(new Project[]{this});

            // Mark this project as deleted so ProjectManager stops tracking it
            state.notifyDeleted();

            // Rename the directory
            FileObject parent = dir.getParent();
            FileLock lock = dir.lock();
            try {
                dir.rename(lock, newName, null);
            } finally {
                lock.releaseLock();
            }

            // Migrate preferences to new path
            migratePreferences(oldPrefsNodeName);

            // Clear cache and find the project at the new location
            ProjectManager.getDefault().clearNonProjectCache();
            FileObject newDir = parent.getFileObject(newName);
            if (newDir == null) {
                newDir = dir;
            }
            Project nue = ProjectManager.getDefault().findProject(newDir);

            // Open the new project
            if (nue != null) {
                OpenProjects.getDefault().open(new Project[]{nue}, false);
                if (wasMain) {
                    OpenProjects.getDefault().setMainProject(nue);
                }
            }
        }
    }

    private void migratePreferences(String oldNodeName) {
        try {
            Preferences root = NbPreferences.forModule(OpenFolderNode.class);
            if (root.nodeExists(oldNodeName)) {
                Preferences oldNode = root.node(oldNodeName);
                Preferences newNode = root.node(prefsNodeName());
                for (String key : oldNode.keys()) {
                    newNode.put(key, oldNode.get(key, ""));
                }

                // Migrate child nodes (e.g. __aux)
                for (String child : oldNode.childrenNames()) {
                    Preferences oldChild = oldNode.node(child);
                    Preferences newChild = newNode.node(child);
                    for (String key : oldChild.keys()) {
                        newChild.put(key, oldChild.get(key, ""));
                    }
                }
                oldNode.removeNode();
                newNode.flush();
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setEncoding(Charset charset) {
        try {
            String name = (charset == null ? "UTF-8" : charset.name());
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
        String delim = Utilities.isWindows() ? "--" : ";;";
        String path = getProjectDirectory().getPath()
            .replace('/', '_')
            .replace('\\', '_')
            .replace(":", "~");
        return delim + path;
    }

    private String prefsNodeNameForPath(String absolutePath) {
        String delim = Utilities.isWindows() ? "--" : ";;";
        String path = absolutePath
            .replace('/', '_')
            .replace('\\', '_')
            .replace(":", "~");
        return delim + path;
    }

    Preferences preferences(boolean create) throws BackingStoreException {
        Preferences prefs = NbPreferences.forModule(OpenFolderNode.class);
        String n = prefsNodeName();
        if (prefs.nodeExists(n) || create) {
            return prefs.node(n);
        }
        return null;
    }

    // --------------------------------------
    // FileEncodingQuery
    // --------------------------------------
    private class EncQueryImpl extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject fo) {
            return OpenFolder.this.getEncoding();
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
        public String get(String key, boolean shared) {
            return p(shared).get(key, null);
        }

        @Override
        public void put(String key, String value, boolean shared) {
            Preferences prefs = p(shared);
            if (value == null) {
                prefs.remove(key);
            } else {
                prefs.put(key, value);
            }
        }

        @Override
        public Iterable<String> listKeys(boolean shared) {
            try {
                return Arrays.asList(p(shared).childrenNames());
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
                return Collections.emptySet();
            }
        }
    }

    private class FileOwnerQueryImpl implements FileOwnerQueryImplementation {

        @Override
        public Project getOwner(FileObject file) {
            if (FileUtil.isParentOf(dir, file)) {
                return OpenFolder.this;
            }
            return null;
        }

        @Override
        public Project getOwner(java.net.URI uri) {
            return null;
        }
    }

    private class ProjectConfigImpl implements ProjectConfiguration {

        @Override
        public String getDisplayName() {
            return "Default";
        }
    }

    private class ProjectActionPerformerImpl implements ProjectActionPerformer {

        @Override
        public void perform(Project project) {
            // Default implementation for project action performer
        }

        @Override
        public boolean enable(Project project) {
            return true;
        }
    }

    private class DeleteOperationImpl implements DeleteOperationImplementation, DataFilesProviderImplementation {

        @Override
        public void notifyDeleting() {
            // Implementation for delete notification
        }

        @Override
        public void notifyDeleted() {
            state.notifyDeleted();
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<FileObject> getDataFiles() {
            return Arrays.asList(dir.getChildren());
        }
    }

    private class MoveOperationImpl implements MoveOrRenameOperationImplementation, DataFilesProviderImplementation {

        @Override
        public void notifyMoving() {
        }

        @Override
        public void notifyMoved(Project original, File originalPath, String nueName) {
            if (original != null) {
                // We are the NEW project instance after move.
                // Migrate preferences from old path to new path.
                String oldNodeName = prefsNodeNameForPath(originalPath.getAbsolutePath());
                migratePreferences(oldNodeName);
            } else {
                // We are the OLD project being discarded
                state.notifyDeleted();
            }
        }

        @Override
        public void notifyRenaming() {
        }

        @Override
        public void notifyRenamed(String nueName) {
            // Called for rename-only (no folder rename).
            // For OpenFolder, project name = folder name, so this is a no-op.
            // Folder rename goes through doMoveProject instead.
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return Collections.emptyList();
        }

        @Override
        public List<FileObject> getDataFiles() {
            return Collections.emptyList();
        }
    }
}
