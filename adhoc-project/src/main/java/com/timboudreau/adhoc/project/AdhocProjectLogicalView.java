/*
 * The MIT License
 *
 * Copyright 2023 Chris.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.timboudreau.adhoc.project;

import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris
 */
public class AdhocProjectLogicalView implements LogicalViewProvider {

    private final AdhocProject prj;

    public AdhocProjectLogicalView(AdhocProject project) {
        prj = project;
    }

    @Override
    public Node createLogicalView() {
        try {
            //Obtain the project directory's node:
            FileObject projectDirectory = prj.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            //Decorate the project directory's node:
            return new AdhocProjectNode(nodeOfProjectFolder, prj);
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            //Fallback-the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }

//    @Override
//    public Node createLogicalView() {
//        FileObject projectDirectory = prj.getProjectDirectory();
//        DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
//        Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
//
//        // Create a list of nodes for the project directory's children
//        List<Node> nodeList = new ArrayList<>();
//        for (FileObject fileObject : prj.getProjectDirectory().getChildren()) {
//            try {
//                nodeList.add(DataObject.find(fileObject).getNodeDelegate());
//            } catch (DataObjectNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//        // Sort the list of nodes using a comparator
//        Collections.sort(nodeList, new NodeComparator());
//        // Convert the sorted list to an array and add it to the Children.Array
//        Node[] nodes = new Node[nodeList.size()];
//        if (!nodeList.isEmpty()) {
//            nodes = nodeList.toArray(nodes);
//            Children.Array children = new Children.Array();
//            children.add(nodes);
//
//            try {
//                // Create and return the root node for the logical view
//                if (nodeOfProjectFolder == null || prj == null) {
////                    return Node.EMPTY;
//                    return new AbstractNode(Children.LEAF);
//                } else {
//                    return new AdhocProjectNode(nodeOfProjectFolder, prj);
//                }
//
//            } catch (DataObjectNotFoundException ex) {
////            Exceptions.printStackTrace(ex);
//                return new AbstractNode(children);
//            }
//        } else {
//            // Return an empty node if the project directory is empty
//            return Node.EMPTY;
////            return new AbstractNode(Children.LEAF);
//        }
//    }
//    @Override
//    public Node createLogicalView() {
//        FileObject projectDirectory = prj.getProjectDirectory();
//        DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
//        Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
//
//        // Create a list of nodes for the project directory's children
//        List<Node> nodeList = new ArrayList<>();
//        for (FileObject fileObject : prj.getProjectDirectory().getChildren()) {
//            try {
//                nodeList.add(DataObject.find(fileObject).getNodeDelegate());
//            } catch (DataObjectNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//        // Sort the list of nodes using a comparator
//        Collections.sort(nodeList, new NodeComparator());
//        // Convert the sorted list to an array and add it to the Children.Array
//        Node[] nodes = new Node[nodeList.size()];
//        nodes = nodeList.toArray(nodes);
//        if (nodes.length > 0) {
//            try {
//                Children.Array children = new Children.Array();
//                children.add(nodes);
//                // Create and return the root node for the logical view
//                return new AdhocProjectNode(nodeOfProjectFolder, prj);
//            } catch (DataObjectNotFoundException ex) {
//                return new AbstractNode(Children.LEAF);
//            }
//        } else {
//            return new AbstractNode(Children.LEAF);
//        }
//    }
    @Override
    public Node findPath(Node node, Object target
    ) {
        if (target instanceof Node) {
            target = ((Node) target).getLookup().lookup(DataObject.class
            );
        }
        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }

        return null;
    }

//    private static class NodeComparator implements Comparator<Node> {
//
//        @Override
//        public int compare(Node n1, Node n2) {
//            // Get the DataObjects associated with the nodes
//            DataObject do1 = n1.getLookup().lookup(DataObject.class);
//            DataObject do2 = n2.getLookup().lookup(DataObject.class);
//
//            // Get the FileObjects associated with the DataObjects
//            FileObject fo1 = do1.getPrimaryFile();
//            FileObject fo2 = do2.getPrimaryFile();
//
//            // Check if one of the FileObjects is a folder and the other is a file
//            boolean isFolder1 = fo1.isFolder();
//            boolean isFolder2 = fo2.isFolder();
//            if (isFolder1 != isFolder2) {
//                // If one is a folder and the other is a file, sort the folder first
//                return isFolder1 ? -1 : 1;
//            } else {
//                // If both are folders or both are files, sort by name
//                String name1 = n1.getDisplayName();
//                String name2 = n2.getDisplayName();
//                return name1.compareToIgnoreCase(name2);
//            }
//        }
//    }

}
