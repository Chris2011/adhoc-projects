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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.openide.util.ImageUtilities;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders(value = {
    @ServiceProvider(service = ProjectFactory.class),
    @ServiceProvider(service = ProjectFactory2.class)
})
public class OpenFolderFactory implements ProjectFactory, ProjectFactory2 {

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return check(projectDirectory);
    }

    public static boolean check(FileObject fo) {
        if (fo != null && fo.isFolder()) {
            FileObject dotNB = fo.getFileObject(".netbeans");
            return dotNB != null && dotNB.isFolder();
        }
        return false;
    }

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (!isProject(projectDirectory)) {
            return null;
        }
        return new OpenFolder(projectDirectory, state);
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }

    @Override
    public Result isProject2(FileObject fo) {
        if (isProject(fo)) {
            return new ProjectManager.Result(ImageUtilities.loadImageIcon("io/github/chris2011/netbeans/plugins/openfolder/open-folder.png", true));
        }
        return null;
    }
}
