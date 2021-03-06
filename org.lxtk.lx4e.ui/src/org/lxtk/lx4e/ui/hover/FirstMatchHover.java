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
package org.lxtk.lx4e.ui.hover;

import java.util.Objects;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.lxtk.lx4e.util.DefaultWordFinder;

/**
 * Checks the given hovers in sequence, and uses the first one that fits.
 */
public class FirstMatchHover
    implements ITextHover, ITextHoverExtension, ITextHoverExtension2
{
    private final ITextHover[] hovers;
    private ITextHover currentHover;

    /**
     * Constructor.
     *
     * @param hovers the hovers to combine (not <code>null</code>)
     */
    public FirstMatchHover(ITextHover... hovers)
    {
        this.hovers = Objects.requireNonNull(hovers);
    }

    @Override
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        return DefaultWordFinder.INSTANCE.findWord(textViewer.getDocument(), offset);
    }

    @Override
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        currentHover = null;
        for (ITextHover hover : hovers)
        {
            if (hover == null)
                continue;

            @SuppressWarnings("deprecation")
            String hoverInfo = hover.getHoverInfo(textViewer, hoverRegion);
            if (hoverInfo != null && !hoverInfo.trim().isEmpty())
            {
                currentHover = hover;
                return hoverInfo;
            }
        }
        return null;
    }

    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
    {
        currentHover = null;
        for (ITextHover hover : hovers)
        {
            if (hover == null)
                continue;

            if (hover instanceof ITextHoverExtension2)
            {
                Object hoverInfo =
                    ((ITextHoverExtension2)hover).getHoverInfo2(textViewer, hoverRegion);
                if (hoverInfo != null)
                {
                    currentHover = hover;
                    return hoverInfo;
                }
            }
            else
            {
                @SuppressWarnings("deprecation")
                String hoverInfo = hover.getHoverInfo(textViewer, hoverRegion);
                if (hoverInfo != null && !hoverInfo.trim().isEmpty())
                {
                    currentHover = hover;
                    return hoverInfo;
                }
            }
        }
        return null;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator()
    {
        if (currentHover instanceof ITextHoverExtension)
            return ((ITextHoverExtension)currentHover).getHoverControlCreator();

        return null;
    }
}
