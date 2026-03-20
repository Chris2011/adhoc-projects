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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

public class OpenFolderLogicalView implements LogicalViewProvider {

    private final OpenFolder project;

    public OpenFolderLogicalView(OpenFolder project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        FileObject projDir = project.getProjectDirectory();
        DataFolder projDataObject = DataFolder.findFolder(projDir);

        try {
            return new OpenFolderNode(projDataObject.getNodeDelegate(), project);
        } catch (DataObjectNotFoundException e) {
            // Fallback to simple FilterNode
            return new FilterNode(
                projDataObject.getNodeDelegate(),
                projDataObject.createNodeChildren(DataFilter.ALL)
            );
        }
    }

    @Override
    public Node findPath(Node root, Object target) {
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;

            Set<Node> visitedNodes
                = Collections.newSetFromMap(new IdentityHashMap<>());
            return findNodeForFileObject(root, fo, visitedNodes);
        }
        return null;
    }

    private Node findNodeForFileObject(Node currentNode, FileObject fo, Set<Node> visited) {
        // Abort if node was already visited (avoid infinite loops)
        if (!visited.add(currentNode)) {
            return null;
        }

        DataObject dob = currentNode.getLookup().lookup(DataObject.class);
        if (dob != null && fo.equals(dob.getPrimaryFile())) {
            return currentNode;
        }

        try {
            if (dob != null) {
                FileObject nodeFileObject = dob.getPrimaryFile();

                // Skip folders that cannot contain the target file
                if (nodeFileObject.isFolder() && !FileUtil.isParentOf(nodeFileObject, fo)) {
                    return null;
                }
            }

            Node[] children = currentNode.getChildren().getNodes(true);
            for (Node child : children) {
                Node result = findNodeForFileObject(child, fo, visited);

                if (result != null) {
                    return result;
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
