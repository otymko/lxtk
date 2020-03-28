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

import static org.lxtk.lx4e.internal.examples.typescript.TypeScriptPreferenceConstants.EDITOR_MATCHING_BRACKETS;
import static org.lxtk.lx4e.internal.examples.typescript.TypeScriptPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationCharacterPairMatcher;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.lxtk.LanguageOperationTarget;
import org.lxtk.lx4e.internal.examples.typescript.Activator;
import org.lxtk.lx4e.internal.examples.typescript.TypeScriptOperationTargetProvider;
import org.lxtk.lx4e.internal.examples.typescript.TypeScriptSourceFileDocumentProvider;
import org.lxtk.lx4e.internal.examples.typescript.TypeScriptSourceViewerConfiguration;
import org.lxtk.lx4e.ui.highlight.Highlighter;

/**
 * TODO JavaDoc
 */
public class TypeScriptEditor
    extends AbstractDecoratedTextEditor
{
    private IContentOutlinePage outlinePage;
    private Highlighter highlighter;
    private final Object reconcilerLock = new Object();

    @Override
    protected void initializeEditor()
    {
        IPreferenceStore preferenceStore = new ChainedPreferenceStore(
            new IPreferenceStore[] {
                Activator.getDefault().getPreferenceStore(),
                EditorsUI.getPreferenceStore() });
        setPreferenceStore(preferenceStore);

        TypeScriptSourceFileDocumentProvider documentProvider =
            Activator.getDefault().getDocumentProvider();
        setDocumentProvider(documentProvider);
        setSourceViewerConfiguration(new TypeScriptSourceViewerConfiguration(
            preferenceStore, this, documentProvider));
        setEditorContextMenuId("#ExampleTypeScriptEditorContext"); //$NON-NLS-1$
        setRulerContextMenuId("#ExampleTypeScriptRulerContext"); //$NON-NLS-1$
    }

    @Override
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] {
            "org.lxtk.lx4e.examples.typescript.editor.scope" }); //$NON-NLS-1$
    }

    @Override
    protected void configureSourceViewerDecorationSupport(
        SourceViewerDecorationSupport decorationSupport)
    {
        decorationSupport.setCharacterPairMatcher(
            new LanguageConfigurationCharacterPairMatcher());
        decorationSupport.setMatchingCharacterPainterPreferenceKeys(
            EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);
        super.configureSourceViewerDecorationSupport(decorationSupport);
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        highlighter = new Highlighter(getSourceViewer(), getSelectionProvider(),
            this::getLanguageOperationTarget);
        highlighter.install();
    }

    @Override
    public void dispose()
    {
        if (highlighter != null)
        {
            highlighter.uninstall();
            highlighter = null;
        }
        super.dispose();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            if (outlinePage == null)
                outlinePage = new TypeScriptOutlinePage(this);
            return adapter.cast(outlinePage);
        }
        return super.getAdapter(adapter);
    }

    void outlinePageClosed()
    {
        if (outlinePage != null)
        {
            outlinePage = null;
            resetHighlightRange();
        }
    }

    public Object getReconcilerLock()
    {
        return reconcilerLock;
    }

    private LanguageOperationTarget getLanguageOperationTarget()
    {
        return TypeScriptOperationTargetProvider.getOperationTarget(this);
    }
}
