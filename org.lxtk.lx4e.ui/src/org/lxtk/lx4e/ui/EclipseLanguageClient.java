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
package org.lxtk.lx4e.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.lxtk.client.AbstractLanguageClient;
import org.lxtk.client.Feature;
import org.lxtk.lx4e.refactoring.WorkspaceEditChangeFactory;
import org.lxtk.lx4e.refactoring.WorkspaceEditRefactoring;
import org.lxtk.util.Log;

/**
 * Default implementation of an Eclipse-based {@link LanguageClient}.
 *
 * @param <S> server interface type
 */
public class EclipseLanguageClient<S extends LanguageServer>
    extends AbstractLanguageClient<S>
{
    private final WorkspaceEditChangeFactory workspaceEditChangeFactory;

    /**
     * Constructor.
     *
     * @param log the client's log (not <code>null</code>)
     * @param diagnosticConsumer the client's diagnostic consumer
     *  (not <code>null</code>)
     * @param workspaceEditChangeFactory the {@link WorkspaceEditChangeFactory}
     *  for the client (not <code>null</code>)
     * @param features the client's features (not <code>null</code>).
     *  Subsequent modifications of the given collection will have no effect
     *  on the constructed instance
     */
    public EclipseLanguageClient(Log log, Consumer<PublishDiagnosticsParams> diagnosticConsumer,
        WorkspaceEditChangeFactory workspaceEditChangeFactory,
        Collection<Feature<? super S>> features)
    {
        super(log, diagnosticConsumer, features);
        this.workspaceEditChangeFactory = Objects.requireNonNull(workspaceEditChangeFactory);
    }

    @Override
    public void fillClientCapabilities(ClientCapabilities capabilities)
    {
        WorkspaceEditCapabilities workspaceEdit = new WorkspaceEditCapabilities();
        workspaceEdit.setDocumentChanges(true);
        // TODO Support resource operations
//        workspaceEdit.setResourceOperations(Arrays.asList(
//            ResourceOperationKind.Create, ResourceOperationKind.Delete,
//            ResourceOperationKind.Rename));

        WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
        workspace.setApplyEdit(true);
        workspace.setWorkspaceEdit(workspaceEdit);

        capabilities.setWorkspace(workspace);
        super.fillClientCapabilities(capabilities);
    }

    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params)
    {
        String label = getEditLabel(params);

        WorkspaceEditRefactoring refactoring =
            new WorkspaceEditRefactoring(label, workspaceEditChangeFactory);
        refactoring.setWorkspaceEdit(params.getEdit());

        PerformRefactoringOperation operation =
            new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);

        CompletableFuture<ApplyWorkspaceEditResponse> future = new CompletableFuture<>();
        WorkspaceJob job = new WorkspaceJob(label)
        {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
            {
                try
                {
                    operation.run(monitor);

                    RefactoringStatus status = operation.getConditionStatus();
                    if (status != null && !status.hasFatalError())
                        status = operation.getValidationStatus();

                    ApplyWorkspaceEditResponse response = new ApplyWorkspaceEditResponse();
                    response.setApplied(status != null && !status.hasFatalError());
                    // TODO Set ApplyWorkspaceEditResponse.failureReason when it becomes available in LSP4J

                    future.complete(response);
                    return Status.OK_STATUS;
                }
                catch (OperationCanceledException e)
                {
                    future.cancel(false);
                    throw e;
                }
                catch (Throwable e)
                {
                    future.completeExceptionally(e);
                    throw e;
                }
            }
        };
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.schedule();
        return future;
    }

    /**
     * Returns the workspace edit label. This label is presented in the
     * user interface, e.g., on an undo stack. If no label is present
     * in the given {@link ApplyWorkspaceEditParams}, a generic label
     * is returned.
     *
     * @param params never <code>null</code>
     * @return the workspace edit label (not <code>null</code>)
     */
    protected String getEditLabel(ApplyWorkspaceEditParams params)
    {
        String label = params.getLabel();
        if (label == null || label.isEmpty())
            label = Messages.EclipseLanguageClient_Edit_label;
        return label;
    }

    @Override
    public void telemetryEvent(Object object)
    {
    }

    @Override
    public void showMessage(MessageParams params)
    {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
        {
            MessageDialog dialog = new MessageDialog(getShell(), getMessageTitle(params), null,
                params.getMessage(), getDialogImageType(params), 0, IDialogConstants.OK_LABEL);
            dialog.open();
        });
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params)
    {
        CompletableFuture<MessageActionItem> future = new CompletableFuture<>();
        PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
        {
            List<MessageActionItem> actions = params.getActions();
            MessageDialog dialog = new MessageDialog(getShell(), getMessageTitle(params), null,
                params.getMessage(), getDialogImageType(params), 0, getDialogButtonLabels(actions));
            int index = dialog.open();
            if (index == SWT.DEFAULT || actions == null || actions.isEmpty())
                future.complete(null);
            else
                future.complete(actions.get(index));
        });
        return future;
    }

    /**
     * Returns the message title. This title is presented in the user interface,
     * e.g., in a message dialog.
     * <p>
     * Default implementation returns a generic title.
     * </p>
     *
     * @param params never <code>null</code>
     * @return the message title (may be <code>null</code>)
     */
    protected String getMessageTitle(MessageParams params)
    {
        ServerInfo serverInfo = getServerInfo();
        return serverInfo == null ? Messages.EclipseLanguageClient_Message_title
            : serverInfo.getName();
    }

    private static int getDialogImageType(MessageParams params)
    {
        switch (params.getType())
        {
        case Error:
            return MessageDialog.ERROR;
        case Warning:
            return MessageDialog.WARNING;
        case Info:
            return MessageDialog.INFORMATION;
        default:
            return MessageDialog.NONE;
        }
    }

    private static String[] getDialogButtonLabels(List<MessageActionItem> actions)
    {
        List<String> labels = new ArrayList<>();
        if (actions == null || actions.isEmpty())
            labels.add(IDialogConstants.OK_LABEL);
        else
            for (MessageActionItem action : actions)
                labels.add(action.getTitle());
        return labels.toArray(new String[labels.size()]);
    }

    private static Shell getShell()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return null;
        return window.getShell();
    }
}
