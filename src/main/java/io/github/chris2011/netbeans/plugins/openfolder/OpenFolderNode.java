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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Christian Lenz
 */
public class OpenFolderNode extends FilterNode implements PropertyChangeListener {

    private final OpenFolder prj;
    private final PropertyChangeListener weakListener;

    public OpenFolderNode(Node node, OpenFolder project)
        throws DataObjectNotFoundException {
        super(node,
            new FilterNode.Children(node),
            new ProxyLookup(
                Lookups.singleton(project),
                node.getLookup()
            ));
        this.prj = project;
        ProjectInformation pi = ProjectUtils.getInformation(project);
        this.weakListener = WeakListeners.propertyChange(this, pi);
        pi.addPropertyChangeListener(weakListener);
    }

    @Override
    public String getName() {
        return prj.getProjectDirectory().getName();
    }

    @Override
    public String getDisplayName() {
        return prj.getProjectDirectory().getName();
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        ProjectUtils.getInformation(prj).removePropertyChangeListener(weakListener);
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            ap.invokeAction(ActionProvider.COMMAND_DELETE, prj.getLookup());
        }
    }

    @Override
    public void setName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }

        String oldName = prj.getProjectDirectory().getName();
        if (newName.equals(oldName)) {
            return;
        }

        try {
            prj.rename(newName);
        } catch (IOException ex) {
            org.openide.util.Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("io/github/chris2011/netbeans/plugins/openfolder/open-folder.png", true);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("io/github/chris2011/netbeans/plugins/openfolder/open-folder.png", true);
    }

    @Override
    public String getShortDescription() {
        return prj.getProjectDirectory().getPath() + " is a custom project (Folder opened as project).";
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce != null) {
            String prop = pce.getPropertyName();
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                fireDisplayNameChange(
                    pce.getOldValue() != null ? pce.getOldValue().toString() : null,
                    pce.getNewValue() != null ? pce.getNewValue().toString() : null
                );
            }
            if (ProjectInformation.PROP_NAME.equals(prop)) {
                fireNameChange(
                    pce.getOldValue() != null ? pce.getOldValue().toString() : null,
                    pce.getNewValue() != null ? pce.getNewValue().toString() : null
                );
            }
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        // Start with base class actions (New, Find, Cut, Copy, Paste, etc.)
        List<Action> actions = new ArrayList<>(Arrays.asList(super.getActions(context)));

        // Remove base class Properties - we use project-level one instead
        actions.removeIf(a -> a != null && a.getClass().getName().contains("Properties"));

        // Remove trailing null separators
        while (!actions.isEmpty() && actions.get(actions.size() - 1) == null) {
            actions.remove(actions.size() - 1);
        }

        // Insert Move after Paste (find Paste, insert Move after it)
        int pasteIdx = -1;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a != null && a.getClass().getName().contains("Paste")) {
                pasteIdx = i;
                break;
            }
        }
        if (pasteIdx >= 0) {
            actions.add(pasteIdx + 1, CommonProjectActions.moveProjectAction());
        }

        // Append project-level actions from layer.xml (Find, Close, Properties)
        actions.add(null);
        actions.addAll(Arrays.asList(CommonProjectActions.forType(OpenFolder.TYPE_NAME)));

        return actions.toArray(Action[]::new);
    }
}
