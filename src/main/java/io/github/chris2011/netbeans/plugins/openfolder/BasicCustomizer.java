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
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Exceptions;

/**
 *
 * @author Christian Lenz
 */
public class BasicCustomizer extends JPanel implements FocusListener, ActionListener {
    private final OpenFolder prj;

    public BasicCustomizer(OpenFolder prj) {
        this.prj = prj;
        initComponents();
        jTextField1.setText(ProjectUtils.getInformation(prj).getDisplayName());
        jTextField1.addFocusListener(this);
    }

    /** Called by ProjectCustomizer when OK or Apply is clicked. */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Apply encoding change first (before rename, so prefs migration carries it over)
        Charset ch = (Charset) jComboBox1.getSelectedItem();
        if (ch != null) {
            prj.setEncoding(ch);
        }

        // Apply name change after the dialog has fully closed.
        // Rename closes the old project and opens a new one, which must happen
        // after the customizer dialog lifecycle is complete.
        String newName = jTextField1.getText();
        if (newName != null) {
            newName = newName.trim();
        }
        final String nameToSet = newName;
        SwingUtilities.invokeLater(() -> {
            try {
                prj.rename(nameToSet);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BasicCustomizer.class, "BasicCustomizer.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(BasicCustomizer.class, "BasicCustomizer.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BasicCustomizer.class, "BasicCustomizer.jLabel2.text")); // NOI18N

        jComboBox1.setModel(ProjectCustomizer.encodingModel(prj.getEncoding().name()));
        jComboBox1.setRenderer(ProjectCustomizer.encodingRenderer());
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encodingChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(207, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void encodingChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodingChanged
        Charset ch = (Charset) jComboBox1.getSelectedItem();
        prj.setEncoding(ch);
    }//GEN-LAST:event_encodingChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void focusGained(FocusEvent fe) {
        jTextField1.selectAll();
    }

    @Override
    public void focusLost(FocusEvent fe) {
    }
}
