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
package org.lxtk.lx4e.ui.codeaction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.lxtk.CommandService;
import org.lxtk.LanguageOperationTarget;
import org.lxtk.lx4e.diagnostics.DiagnosticMarkers;
import org.lxtk.lx4e.internal.ui.Activator;
import org.lxtk.lx4e.refactoring.WorkspaceEditChangeFactory;

import com.google.gson.Gson;

/**
 * TODO JavaDoc
 */
public abstract class AbstractMarkerResolutionGenerator
    implements IMarkerResolutionGenerator2
{
    private static final IMarkerResolution[] NO_RESOLUTIONS =
        new IMarkerResolution[0];

    private Gson gson;

    @Override
    public boolean hasResolutions(IMarker marker)
    {
        if (marker.getAttribute(getDiagnosticAttributeName(), null) == null)
            return false;
        LanguageOperationTarget target = getLanguageOperationTarget(marker);
        if (target == null)
            return false;
        return CodeActions.hasCodeActionProvider(target);
    }

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker)
    {
        Diagnostic diagnostic = getDiagnostic(marker,
            getDiagnosticAttributeName());
        if (diagnostic == null)
            return NO_RESOLUTIONS;
        LanguageOperationTarget target = getLanguageOperationTarget(marker);
        if (target == null)
            return NO_RESOLUTIONS;
        CompletableFuture<List<Either<Command, CodeAction>>> future =
            CodeActions.getCodeActions(target, diagnostic.getRange(),
                new CodeActionContext(Collections.singletonList(diagnostic),
                    Collections.singletonList(CodeActionKind.QuickFix)));
        List<Either<Command, CodeAction>> result = null;
        try
        {
            result = future.get(getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (CancellationException | InterruptedException e)
        {
        }
        catch (ExecutionException e)
        {
            Activator.logError(e);
        }
        catch (TimeoutException e)
        {
            Activator.logWarning(e);
        }
        if (result == null || result.isEmpty())
            return NO_RESOLUTIONS;
        List<IMarkerResolution> resolutions = new ArrayList<>(result.size());
        for (Either<Command, CodeAction> item : result)
        {
            if (item.isLeft())
                resolutions.add(newMarkerResolution(item.getLeft()));
            else if (item.isRight())
                resolutions.add(newMarkerResolution(item.getRight()));
        }
        return resolutions.toArray(NO_RESOLUTIONS);
    }

    /**
     * TODO JavaDoc
     *
     * @param marker never <code>null</code>
     * @return the corresponding {@link LanguageOperationTarget},
     *  or <code>null</code> if none
     */
    protected abstract LanguageOperationTarget getLanguageOperationTarget(
        IMarker marker);

    /**
     * TODO JavaDoc
     *
     * @return the command service (not <code>null</code>)
     */
    protected abstract CommandService getCommandService();

    /**
     * TODO JavaDoc
     *
     * @return a {@link WorkspaceEditChangeFactory} (not <code>null</code>)
     */
    protected abstract WorkspaceEditChangeFactory getWorkspaceEditChangeFactory();

    /**
     * TODO JavaDoc
     *
     * @param command never <code>null</code>
     * @return a marker resolution based on the command (not <code>null</code>)
     */
    protected IMarkerResolution newMarkerResolution(Command command)
    {
        return new CommandMarkerResolution(command);
    }

    /**
     * TODO JavaDoc
     *
     * @param codeAction never <code>null</code>
     * @return a marker resolution based on the code action (not <code>null</code>)
     */
    protected IMarkerResolution newMarkerResolution(CodeAction codeAction)
    {
        return new CodeActionMarkerResolution(codeAction);
    }

    /**
     * TODO JavaDoc
     *
     * @return the diagnostic attribute name (not <code>null</code>)
     */
    protected String getDiagnosticAttributeName()
    {
        return DiagnosticMarkers.DIAGNOSTIC_ATTRIBUTE;
    }

    /**
     * TODO JavaDoc
     *
     * @param marker never <code>null</code>
     * @param diagnosticAttributeName never <code>null</code>
     * @return the diagnostic, or <code>null</code> if none
     */
    protected Diagnostic getDiagnostic(IMarker marker,
        String diagnosticAttributeName)
    {
        String value = marker.getAttribute(diagnosticAttributeName, null);
        if (value == null)
            return null;
        if (gson == null)
            gson = new Gson();
        return gson.fromJson(value, Diagnostic.class);
    }

    /**
     * TODO JavaDoc
     *
     * @return a positive duration
     */
    protected Duration getTimeout()
    {
        return Duration.ofSeconds(2);
    }

    /**
     * TODO JavaDoc
     */
    protected class CommandMarkerResolution
        implements IMarkerResolution
    {
        protected final Command command;

        /**
         * TODO JavaDoc
         *
         * @param command not <code>null</code>
         */
        public CommandMarkerResolution(Command command)
        {
            this.command = Objects.requireNonNull(command);
        }

        @Override
        public String getLabel()
        {
            return command.getTitle();
        }

        @Override
        public void run(IMarker marker)
        {
            CodeActions.execute(command, getLabel(), getCommandService());
        }
    }

    /**
     * TODO JavaDoc
     */
    protected class CodeActionMarkerResolution
        implements IMarkerResolution
    {
        protected final CodeAction codeAction;

        /**
         * TODO JavaDoc
         *
         * @param codeAction not <code>null</code>
         */
        public CodeActionMarkerResolution(CodeAction codeAction)
        {
            this.codeAction = Objects.requireNonNull(codeAction);
        }

        @Override
        public String getLabel()
        {
            return codeAction.getTitle();
        }

        @Override
        public void run(IMarker marker)
        {
            CodeActions.execute(codeAction, getLabel(),
                getWorkspaceEditChangeFactory(), getCommandService());
        }
    }
}
