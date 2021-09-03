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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tim Boudreau
 */
public class AdhocProjectNode extends FilterNode implements LogicalViewProvider, PropertyChangeListener {
    private final AdhocProject prj;

    public AdhocProjectNode(AdhocProject prj) throws DataObjectNotFoundException {
        this(prj, DataObject.find(prj.getProjectDirectory()));
    }

    AdhocProjectNode(AdhocProject prj, DataObject dob) throws DataObjectNotFoundException {
        super(dob.getNodeDelegate(), new Children.Array(), new ProxyLookup(Lookups.fixed(prj), dob.getLookup()));
        this.prj = prj;

        List<Node> nodeList = new ArrayList<>();

        for (FileObject fileObject : this.prj.getProjectDirectory().getChildren()) {
            try {
                nodeList.add(DataObject.find(fileObject).getNodeDelegate());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Node[] nodes = new Node[nodeList.size()];
        nodes = nodeList.toArray(nodes);

        getChildren().add(nodes);
        prj.addPropertyChangeListener(WeakListeners.propertyChange(this, prj));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.deleteProjectAction(),
            null,
            CommonProjectActions.setAsMainProjectAction(),
            CommonProjectActions.closeProjectAction(),
            null,
            CommonProjectActions.customizeProjectAction(),};
    }

    @Override
    public String getDisplayName() {
        return prj.getDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("com/timboudreau/adhoc/project/adhoc.png", false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Node createLogicalView() {
        return this;
    }

    @Override
    public String getShortDescription() {
        return "Ad-Hoc Project in " + prj.getProjectDirectory().getPath();
    }

    @Override
    public Node findPath(Node root, Object target) {
        if (target instanceof Node) {
            target = ((Node) target).getLookup().lookup(DataObject.class);
        }
        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }
//        if (target instanceof FileObject) {
//            FileObject t = (FileObject) target;
//            return recurseFindChild(Collections.<Node>singleton(sources), t, 0);
//        }
        return null;
    }

//    private Node recurseFindChild(Iterable<Node> folders, FileObject target, int depth) {
//        FileObject par = target.getParent();
//        List<Node> next = new ArrayList<>();
//        for (Node fld : folders) {
//            DataObject dob = fld.getLookup().lookup(DataObject.class);
//            if (dob != null) {
//                if (par.equals(dob.getPrimaryFile())) {
//                    for (Node nn : fld.getChildren().getNodes(true)) {
//                        DataObject d1 = nn.getLookup().lookup(DataObject.class);
//                        if (d1 != null && d1.getPrimaryFile().equals(target)) {
//                            return nn;
//                        } else if (d1 instanceof DataFolder) {
//                            next.add(nn);
//                        }
//                    }
//                } else {
//                    for (Node nn : fld.getChildren().getNodes(true)) {
//                        DataFolder nx = nn.getLookup().lookup(DataFolder.class);
//                        if (nx != null) {
//                            next.add(nn);
//                        }
//                    }
//                }
//            }
//        }
//        if (!next.isEmpty() && depth < 10) {
//            return recurseFindChild(next, target, depth + 1);
//        }
//        return null;
//    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce != null) {
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(pce.getPropertyName())) {
                fireDisplayNameChange(pce.getOldValue() + "", pce.getNewValue() + "");
            }
        }
    }

    public static class ProjectFilesNodeFactory extends FilterNode.Children {
        public ProjectFilesNodeFactory(AdhocProject prj, Node or) {
            super(or);
        }

        @Override
        public Node[] createNodes(Node key) {
            List<Node> nodes = new ArrayList<>();

            DataObject dob = key.getLookup().lookup(DataObject.class);

            if (dob != null) {
                try {
                    FileObject fo = dob.getPrimaryFile();

                    nodes.add(DataObject.find(fo).getNodeDelegate());

//                if (!VisibilityQuery.getDefault().isVisible(fo)) {
//                    return new Node[0];
//                }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return (Node[]) nodes.toArray();
        }
    }
}
