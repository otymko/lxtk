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
package org.lxtk;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;

/**
 * Provides locations for all references to the symbol denoted by a given
 * text document position.
 *
 * @see LanguageService
 */
public interface ReferenceProvider
    extends LanguageFeatureProvider<TextDocumentRegistrationOptions>
{
    /**
     * Requests locations for all references to the symbol denoted by the given
     * text document position.
     *
     * @param params not <code>null</code>
     * @return result future (never <code>null</code>)
     */
    CompletableFuture<List<? extends Location>> getReferences(ReferenceParams params);
}
