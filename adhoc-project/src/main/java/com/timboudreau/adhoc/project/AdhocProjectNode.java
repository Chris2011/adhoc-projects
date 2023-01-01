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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.ProjectInformation;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tim Boudreau
 */
public class AdhocProjectNode extends FilterNode implements PropertyChangeListener {
    private final AdhocProject prj;

    public AdhocProjectNode(Node node, AdhocProject project)
            throws DataObjectNotFoundException {
        super(node,
                new FilterNode.Children(node),
                new ProxyLookup(
                        new Lookup[]{
                            Lookups.singleton(project),
                            node.getLookup()
                        }));
        this.prj = project;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-closed.png", false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-open.png", false);
    }

    @Override
    public String getShortDescription() {
        return "Ad-Hoc Project in " + prj.getProjectDirectory().getPath();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce != null) {
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(pce.getPropertyName())) {
                fireDisplayNameChange(pce.getOldValue() + "", pce.getNewValue() + "");
            }
        }
    }
}
