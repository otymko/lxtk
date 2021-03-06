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
/**
 * Provides API and implementation for LXTK Core Services, such as
 * Document Service and Language Service.
 * <p>
 * <b>Note:</b> Unless stated otherwise, implementations of the interfaces
 * defined in this package need to be thread-safe.
 * </p>
 * <p>
 * LXTK Core Services are the primary entry points to the LXTK Core Framework.
 * Rather than exposing LSP4J services directly, LXTK provides its own service layer.
 * This layer is somewhat similar to the Extension API of Visual Studio Code, but is tailored
 * specifically to LSP. In particular, LXTK API directly reuses protocol data types of LSP4J.
 * </p>
 * <p>
 * Here are brief descriptions of the provided services.
 * </p>
 * <h2>Document Service</h2>
 * <p>
 * {@link org.lxtk.DocumentService} provides support for document management.
 * Text documents are represented by the {@link org.lxtk.TextDocument} interface.
 * </p>
 * <p>
 * {@link org.lxtk.DefaultDocumentService} provides a general-purpose implementation
 * of the service.
 * </p>
 * <h2>Language Service</h2>
 * <p>
 * {@link org.lxtk.LanguageService} provides support for participating in language-specific
 * editing features, like code completion, code actions, rename, etc. Each feature provider is
 * represented by a corresponding sub-interface of the {@link org.lxtk.LanguageFeatureProvider}
 * interface.
 * </p>
 * <p>
 * {@link org.lxtk.DefaultLanguageService} provides a general-purpose implementation
 * of the service.
 * </p>
 * <h2>Command Service</h2>
 * <p>
 * {@link org.lxtk.CommandService} provides support for command management. A command is a
 * {@link org.lxtk.CommandHandler} with a unique identifier.
 * </p>
 * <p>
 * {@link org.lxtk.DefaultCommandService} provides a general-purpose implementation
 * of the service.
 * </p>
 * <h2>Workspace Service</h2>
 * <p>
 * {@link org.lxtk.WorkspaceService} provides support for workspace management. A workspace contains
 * a collection of {@link org.lxtk.WorkspaceFolder} objects.
 * </p>
 * <p>
 * {@link org.lxtk.DefaultWorkspaceService} provides a general-purpose implementation
 * of the service.
 * </p>
 */
package org.lxtk;
