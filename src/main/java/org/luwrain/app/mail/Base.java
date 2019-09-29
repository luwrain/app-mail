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

final class Base extends Utils
{
    static final String LOG_COMPONENT = "mail";

    final Luwrain luwrain;
    final Strings strings;
    final MailStoring storing;
    private final FoldersModelSource foldersModelSource;
    private final TreeArea.Model foldersModel;
    private MailFolder openedFolder = null;
    private Object[] summaryItems = new Object[0];
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
    }

        void openFolder(MailFolder folder) throws PimException
    {
	NullCheck.notNull(folder, "folder");
	this.openedFolder = folder;
	final MailMessage[] allMessages = storing.getMessages().load(folder);
	final List<MailMessage> res = new LinkedList();
	for(MailMessage m: allMessages)
	    if (m.getState() != MailMessage.State.DELETED)
		res.add(m);
	final Hooks hooks = new Hooks(luwrain);
	this.summaryItems = hooks.organizeSummary(res.toArray(new MailMessage[res.size()]));
	Log.debug(LOG_COMPONENT, "loaded " + summaryItems.length + " items");
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

    TreeArea.Params createFoldersTreeParams()
    {
	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = foldersModel;
	params.name = strings.foldersAreaName();
	return params;
    }

    ListArea.Params createSummaryParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = strings.summaryAreaName();
	params.model = new SummaryListModel();
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    return false;
		}
	    };
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
