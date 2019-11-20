/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.lxtk.lx4e.internal.examples.json.editor;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.lxtk.lx4e.internal.examples.json.Activator;
import org.lxtk.lx4e.internal.examples.json.JsonSourceFileDocumentProvider;
import org.lxtk.lx4e.internal.examples.json.JsonSourceViewerConfiguration;

/**
 * TODO JavaDoc
 */
public class JsonEditor
    extends AbstractDecoratedTextEditor
{
    private IContentOutlinePage outlinePage;

    @Override
    protected void initializeEditor()
    {
        super.initializeEditor();
        JsonSourceFileDocumentProvider documentProvider =
            Activator.getDefault().getDocumentProvider();
        setDocumentProvider(documentProvider);
        setSourceViewerConfiguration(new JsonSourceViewerConfiguration(
            getPreferenceStore(), this, documentProvider));
    }

    @Override
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] {
            "org.lxtk.lx4e.examples.json.editor.scope" }); //$NON-NLS-1$
    }

    @Override
    public <T> T getAdapter(Class<T> adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            if (outlinePage == null)
                outlinePage = new JsonOutlinePage(this);
            return adapter.cast(outlinePage);
        }
        return super.getAdapter(adapter);
    }

    /**
     * Informs the editor that its outline page has been closed.
     */
    public void outlinePageClosed()
    {
        if (outlinePage != null)
        {
            outlinePage = null;
            resetHighlightRange();
        }
    }
}