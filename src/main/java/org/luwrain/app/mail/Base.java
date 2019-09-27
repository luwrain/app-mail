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
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.util.*;

final class Base extends Utils
{
    static final String LOG_COMPONENT = "mail";

    final Luwrain luwrain;
    final Strings strings;
    final MailStoring storing;
    private final FoldersModelSource foldersModelSource;
    private final TreeArea.Model foldersModel;
    private StoredMailFolder folder = null;
    private Object[] summaryItems = new Object[0];
    private StoredMailMessage currentMessage;

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

        void updateSummaryMessages(StoredMailFolder folder) throws PimException
    {
	NullCheck.notNull(folder, "folder");
	this.folder = folder;
	final StoredMailMessage[] allMessages = storing.getMessages().load(folder);
	final List<StoredMailMessage> res = new LinkedList();
	for(StoredMailMessage m: allMessages)
	    if (m.getState() != MailMessage.State.DELETED)
		res.add(m);
	final Hooks hooks = new Hooks(luwrain);
	this.summaryItems = hooks.organizeSummary(res.toArray(new StoredMailMessage[res.size()]));
	Log.debug("proba", "loaded " + summaryItems.length + " items");
    }


    void setCurrentMessage(StoredMailMessage message)
    {
	this.currentMessage = message;
    }

    StoredMailMessage getCurrentMessage()
    {
	return currentMessage;
    }

    boolean hasCurrentMessage()
    {
	return currentMessage != null;
    }

    Document prepareDocumentForCurrentMessage()
    {
	final NodeBuilder builder = new NodeBuilder();
	try {
	    builder.addParagraph("ОТ: " + currentMessage.getFrom());
	builder.addParagraph("Кому: " + listToString(currentMessage.getTo()));
	builder.addParagraph("Копия: " + listToString(currentMessage.getCc()));
	builder.addParagraph("Тема: " + currentMessage.getSubject());
	builder.addParagraph("Время: " + currentMessage.getSentDate());
	builder.addParagraph("Тип данных: " + currentMessage.getMimeContentType());
	//nodes.add(NodeFactory.newEmptyLine());
	//	    attachments = message.getAttachments();
	for(String line: splitLines(currentMessage.getText()))
	    if (!line.isEmpty())
		builder.addParagraph(line); else
	builder.addEmptyLine();
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
	final Node root = builder.newRoot(); 
	final Document doc = new Document(root);
doc.setProperty("url", "http://localhost");
doc.commit();
	return doc;
    }

    boolean deleteInSummary(StoredMailMessage message, boolean deleteForever)
    {
	/*
	NullCheck.notNull(message, "message");
	if (currentFolder == null)
	    return false;
	try {
	    if (deleteForever)
		storing.getMessages().delete(message); else
		message.setState(MailMessage.State.DELETED);
	    updateSummaryModel();
	}
	catch(PimException e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время удаления сообщения произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	*/
	return true;
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
	    final StoredMailFolder folder = (StoredMailFolder)obj;
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
