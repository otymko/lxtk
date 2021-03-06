/*******************************************************************************
 * Copyright (c) 2019, 2020 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.lxtk.lx4e.internal.examples.typescript.editor;

import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.quickoutline.HandlyOutlinePopup;
import org.eclipse.handly.ui.viewer.DeferredElementTreeContentProvider;
import org.eclipse.handly.ui.viewer.ProblemMarkerLabelDecorator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.lxtk.lx4e.examples.typescript.TypeScriptInputElementProvider;
import org.lxtk.lx4e.ui.LanguageElementLabelProvider;

/**
 * The outline popup of the TypeScript editor.
 */
public class TypeScriptOutlinePopup
    extends HandlyOutlinePopup
{
    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return TypeScriptInputElementProvider.INSTANCE;
    }

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return new DeferredElementTreeContentProvider(getTreeViewer(), null);
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new DecoratingStyledCellLabelProvider(new LanguageElementLabelProvider(),
            new ProblemMarkerLabelDecorator(), null);
    }
}
