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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Action to open any folder as a NetBeans project.
 *
 * @author Christian Lenz
 */
@NbBundle.Messages("ACT_Open=Open Folder as Project")
@ActionID(id = "io.github.chris2011.netbeans.plugins.openfolder.OpenFolderAsProjectAction", category = "File")
@ActionRegistration(displayName = "#ACT_Open", lazy = false)
@ActionReference(position = 300, path = "Menu/File")
public class OpenFolderAsProjectAction extends AbstractAction {

    public OpenFolderAsProjectAction() {
        super(Bundle.ACT_Open());

        putValue(SMALL_ICON, ImageUtils.getFolderWithNetBeansLogo());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 1) Dialog to select folder
        File folder = new FileChooserBuilder(OpenFolderAsProjectAction.class)
            .setDirectoriesOnly(true)
            .setTitle(Bundle.ACT_Open())
            .showOpenDialog();

        if (folder == null) {
            return;
        }

        FileObject fo = FileUtil.toFileObject(folder);
        if (fo == null) {
            return;
        }

        // 2) Create .netbeans folder if not present
        try {
            FileObject dotNB = fo.getFileObject(".netbeans");
            if (dotNB == null) {
                dotNB = fo.createFolder(".netbeans");
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }

        // 3) Clear NetBeans cache if needed
        ProjectManager.getDefault().clearNonProjectCache();

        // 4) Find and open the project
        RequestProcessor.getDefault().post(() -> {
            Project p;
            try {
                p = ProjectManager.getDefault().findProject(fo);
                if (p != null) {
                    OpenProjects.getDefault().open(new Project[]{p}, false);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
