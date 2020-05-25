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
package org.lxtk.lx4e.ui.references;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.lsp4j.Position;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.lxtk.LanguageOperationTarget;
import org.lxtk.Workspace;
import org.lxtk.lx4e.DocumentUtil;
import org.lxtk.lx4e.ui.DefaultEditorHelper;
import org.lxtk.lx4e.ui.EditorHelper;
import org.lxtk.lx4e.util.DefaultWordFinder;

/**
 * Partial implementation of a handler that creates and runs a {@link
 * ReferenceSearchQuery}.
 */
public abstract class AbstractFindReferencesHandler
    extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ReferenceSearchQuery query = createSearchQuery(HandlerUtil.getActiveEditor(event));
        if (query != null)
            NewSearchUI.runQueryInBackground(query);
        return null;
    }

    @Override
    public void setEnabled(Object evaluationContext)
    {
        boolean enabled = false;
        if (evaluationContext instanceof IEvaluationContext)
        {
            Object editor =
                ((IEvaluationContext)evaluationContext).getVariable(ISources.ACTIVE_EDITOR_NAME);
            ReferenceSearchQuery query = createSearchQuery(editor);
            if (query != null && query.canRun())
                enabled = true;
        }
        setBaseEnabled(enabled);
    }

    private ReferenceSearchQuery createSearchQuery(Object context)
    {
        EditorHelper editorHelper = getEditorHelper();
        ITextEditor editor = editorHelper.getTextEditor(context);
        if (editor == null)
            return null;
        ITextSelection selection = editorHelper.getTextSelection(editor);
        if (selection == null)
            return null;
        IDocument document = editorHelper.getDocument(editor);
        if (document == null)
            return null;
        String wordAtPosition = wordAt(document, selection.getOffset());
        if (wordAtPosition == null || wordAtPosition.isEmpty())
            return null;
        Position position;
        try
        {
            position = DocumentUtil.toPosition(document, selection.getOffset());
        }
        catch (BadLocationException e)
        {
            return null;
        }
        LanguageOperationTarget target = getLanguageOperationTarget(editor);
        if (target == null)
            return null;
        return createSearchQuery(target, position, wordAtPosition);

    }

    /**
     * Returns the corresponding {@link LanguageOperationTarget}
     * for the given editor.
     *
     * @param editor never <code>null</code>
     * @return the corresponding <code>LanguageOperationTarget</code>,
     *  or <code>null</code> if none
     */
    protected abstract LanguageOperationTarget getLanguageOperationTarget(IEditorPart editor);

    /**
     * Returns the {@link Workspace} for this handler.
     *
     * @return the workspace (not <code>null</code>)
     */
    protected abstract Workspace getWorkspace();

    /**
     * Creates and returns a {@link ReferenceSearchQuery} with the given
     * parameters.
     *
     * @param target the {@link LanguageOperationTarget} for the search query
     *  (never <code>null</code>)
     * @param position the target text document position (never <code>null</code>)
     * @param wordAtPosition never <code>null</code>, never empty
     * @return the created search query (not <code>null</code>)
     */
    protected ReferenceSearchQuery createSearchQuery(LanguageOperationTarget target,
        Position position, String wordAtPosition)
    {
        return new ReferenceSearchQuery(target, position, wordAtPosition, getWorkspace(), true);
    }

    /**
     * Returns the {@link EditorHelper} for this handler.
     *
     * @return the editor helper (not <code>null</code>)
     */
    protected EditorHelper getEditorHelper()
    {
        return DefaultEditorHelper.INSTANCE;
    }

    /**
     * Returns the region of the word enclosing the given document offset.
     *
     * @param document never <code>null</code>
     * @param offset 0-based
     * @return the corresponding word region, or <code>null</code> if none
     */
    protected IRegion findWord(IDocument document, int offset)
    {
        return DefaultWordFinder.INSTANCE.findWord(document, offset);
    }

    private String wordAt(IDocument document, int offset)
    {
        IRegion wordRegion = findWord(document, offset);
        if (wordRegion == null)
            return null;
        try
        {
            return document.get(wordRegion.getOffset(), wordRegion.getLength());
        }
        catch (BadLocationException e)
        {
            return null;
        }
    }
}
