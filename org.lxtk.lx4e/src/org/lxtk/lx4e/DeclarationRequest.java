/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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
package org.lxtk.lx4e;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.lxtk.DeclarationProvider;

/**
 * Requests declaration locations for the symbol denoted by the given text document position.
 */
public class DeclarationRequest
    extends LanguageFeatureRequest<DeclarationProvider, DeclarationParams,
        Either<List<? extends Location>, List<? extends LocationLink>>>
{
    @Override
    protected Future<Either<List<? extends Location>, List<? extends LocationLink>>> send(
        DeclarationProvider provider, DeclarationParams params)
    {
        setTitle(MessageFormat.format(Messages.DeclarationRequest_title, params));
        return provider.getDeclaration(params);
    }
}
