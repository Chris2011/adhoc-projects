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
import org.openide.util.ImageUtilities;

public final class ImageUtils {

    private static final String FOLDER_ICON_PATH = "org/netbeans/swing/plaf/resources/hidpi-folder-closed.png";
    private static final String NB_ICON_PATH = "org/netbeans/core/startup/frame.gif";

    // Cache, damit wir nicht jedes Mal neu berechnen
    private static Image cachedIcon = null;

    private ImageUtils() {
        // Utility-Klasse, kein public Konstruktor
    }

    public static synchronized Image getFolderWithNetBeansLogo() {
        if (cachedIcon == null) {
            cachedIcon = createMergedImage();
        }

        return cachedIcon;
    }

    private static Image createMergedImage() {
        Image base = ImageUtilities.loadImage(FOLDER_ICON_PATH, true);
        int wBase = base.getWidth(null);
        int hBase = base.getHeight(null);

        Image overlayOriginal = ImageUtilities.loadImage(NB_ICON_PATH, true);

        int overlayW = 12;
        int overlayH = 12;
        Image overlayScaled = overlayOriginal.getScaledInstance(overlayW, overlayH, Image.SCALE_SMOOTH);

        int x = wBase - overlayW;
        int y = hBase - overlayH;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        Image merged = ImageUtilities.mergeImages(base, overlayScaled, x, y);

        return merged;
    }
}
