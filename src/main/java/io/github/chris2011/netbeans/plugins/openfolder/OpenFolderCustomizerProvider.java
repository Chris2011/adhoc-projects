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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.JComponent;
import org.netbeans.modules.editor.indent.project.api.Customizers;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Christian Lenz
 */
public class OpenFolderCustomizerProvider implements CustomizerProvider {

    private final OpenFolder project;

    OpenFolderCustomizerProvider(OpenFolder project) {
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        String path = "Projects/" + OpenFolder.TYPE_NAME + "/Customizer";
        Dialog dlg = ProjectCustomizer.createCustomizerDialog(path, project.getLookup(), "Basic", new AL(), HelpCtx.DEFAULT_HELP);
        dlg.setModal(false);
        dlg.setVisible(true);
    }

    private class AL implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            //do nothing
        }
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = OpenFolder.TYPE_NAME, position = 1000,
        category = "Basic", categoryLabel = "#LBL_CategoryBasic")
    @NbBundle.Messages("LBL_CategoryBasic=Basic")
    public static ProjectCustomizer.CompositeCategoryProvider basic() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            @Override
            public ProjectCustomizer.Category createCategory(Lookup lkp) {
                return ProjectCustomizer.Category.create("Basic", "Basic", ImageUtils.getFolderWithNetBeansLogo());
            }

            @Override
            public JComponent createComponent(ProjectCustomizer.Category ctgr, Lookup lkp) {
                OpenFolder prj = lkp.lookup(OpenFolder.class);
                BasicCustomizer panel = new BasicCustomizer(prj);
                ctgr.setStoreListener(panel);
                return panel;
            }
        };
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = OpenFolder.TYPE_NAME, position = 2000,
        category = "Formatting", categoryLabel = "#LBL_CategoryFormatting")
    @NbBundle.Messages("LBL_CategoryFormatting=Formatting")
    public static ProjectCustomizer.CompositeCategoryProvider formatting() {
        return Customizers.createFormattingCategoryProvider(Collections.emptyMap());
    }

    public JComponent create(ProjectCustomizer.Category ctgr) {
        if (ctgr.getName().equals("Basic")) {
            return new BasicCustomizer(project);
        }

        return null;
    }
}
