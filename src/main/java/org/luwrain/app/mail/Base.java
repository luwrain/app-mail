/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.controls.reader.*;

final class Base extends Utils
{
    static final String LOG_COMPONENT = "mail";

    final Luwrain luwrain;
    final Strings strings;
    final MailStoring storing;
    final Hooks hooks;
    private final FoldersModelSource foldersModelSource;
    private final TreeArea.Model foldersModel;
    private MailFolder openedFolder = null;
    private SummaryItem[] summaryItems = new SummaryItem[0];
    private MailMessage openedMessage;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.storing = org.luwrain.pim.Connections.getMailStoring(luwrain, true);
	if (storing != null)
	    Log.debug(LOG_COMPONENT, "mail storing prepared"); else
	    Log.debug(LOG_COMPONENT, "no mail storing");
	this.foldersModelSource = new FoldersModelSource();
	this.foldersModel = new CachedTreeModel(foldersModelSource);
	this.hooks = new Hooks(luwrain);
    }

    void openFolder(MailFolder folder) throws PimException
    {
	NullCheck.notNull(folder, "folder");
	this.openedFolder = folder;
	final MailMessage[] messages = storing.getMessages().loadNoDeleted(folder);
	this.summaryItems = hooks.organizeSummary(messages);
    }

    void openMessage(MailMessage message)
    {
	this.openedMessage = message;
    }

    MailMessage getOpenedMessage()
    {
	return this.openedMessage;
    }

    boolean hasOpenedMessage()
    {
	return this.openedMessage != null;
    }

    boolean openDefaultFolder()
    {
	/*
	final StoredMailFolder folder;
	final org.luwrain.pim.Settings.MailFolders sett = org.luwrain.pim.Settings.createMailFolders(luwrain.getRegistry());
	final String uniRef = sett.getFolderInbox("");
	if (uniRef.isEmpty())
	    return false;
	try {
	    folder = storing.getFolders().loadByUniRef(uniRef);
	    if (folder == null)
		return false;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
	return openFolder(folder);
	*/
	return false;
    }

    TreeArea.Params createFoldersTreeParams(TreeArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = foldersModel;
	params.name = strings.foldersAreaName();
	params.clickHandler = clickHandler;
	return params;
    }

    ListArea.Params createSummaryParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = strings.summaryAreaName();
	params.model = new SummaryListModel();
	params.clickHandler = clickHandler;
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (!(item instanceof SummaryItem))
			return false;
		    final SummaryItem summaryItem = (SummaryItem)item;
		    return summaryItem.type == SummaryItem.Type.SECTION;
		}
	    };
	params.transition = new ListUtils.DoubleLevelTransition(params.model){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (!(item instanceof SummaryItem))
			return false;
		    final SummaryItem summaryItem = (SummaryItem)item;
		    return summaryItem.type == SummaryItem.Type.SECTION;
		}
	    };
	return params;
    }

    ReaderArea.Params createMessageReaderParams()
    {
	final ReaderArea.Params params = new ReaderArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = strings.messageAreaName();
	return params;
    }

    private final class SummaryListModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return summaryItems.length;
	}
	@Override public Object getItem(int index)
	{
	    return summaryItems[index];
	}
	@Override public void refresh()
	{
	}
    }

    private class FoldersModelSource implements org.luwrain.controls.CachedTreeModelSource
    {
	@Override public Object getRoot()
	{
	    try {
		return storing.getFolders().getRoot();
	    }
	    catch (PimException e)
	    {
		luwrain.crash(e);
		return null;
	    }
	}
	@Override public Object[] getChildObjs(Object obj)
	{
	    NullCheck.notNull(obj, "obj");
	    final MailFolder folder = (MailFolder)obj;
	    try {
		return storing.getFolders().load(folder);
	    }
	    catch(PimException e)
	    {
		luwrain.crash(e);
		return new Object[0];
	    }
	}
    }
}
