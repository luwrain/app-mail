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
import org.luwrain.network.*;

final class Base extends Utils
{
    final Luwrain luwrain;
        final Strings strings;
        final MailStoring storing;
        private final FoldersModelSource foldersModelSource;
    private final TreeArea.Model foldersModel;
    private final App app;
    private StoredMailFolder folder = null;
    private Object[] summaryItems = new Object[0];
    private StoredMailMessage currentMessage;

    Base(App app, Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.app = app;
	this.luwrain = luwrain;
	this.strings = strings;
	this.storing = org.luwrain.pim.Connections.getMailStoring(luwrain, true);
	this.foldersModelSource = new FoldersModelSource();
	this.foldersModel = new CachedTreeModel(foldersModelSource);
    }

        void updateSummaryMessages(StoredMailFolder folder) throws PimException
    {
	NullCheck.notNull(folder, "folder");
	this.folder = folder;
	final StoredMailMessage[] allMessages = storing.getMessages().load(folder);
	final LinkedList<StoredMailMessage> res = new LinkedList<StoredMailMessage>();
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

    boolean makeReply(StoredMailMessage message, boolean wideReply)
    {
	/*
	NullCheck.notNull(message, "message");
	Log.debug("mail", "starting making a reply");
	try {
	    String subject = message.getSubject();
	    if (!subject.toLowerCase().startsWith("re: "))
		subject = "Re: " + subject;
	    final byte[] bytes = message.getRawMessage();
	    final String from = message.getFrom();
	    if (from == null || from.trim().isEmpty())
		return false;
	    final String replyToBase;
	    try {
		replyToBase = getReplyTo(bytes);
	    }
	    catch(IOException e)
	    {
		luwrain.crash(e);
		return false;
	    }
	    final String replyTo = !replyToBase.trim().isEmpty()?replyToBase:from;
	    final StringBuilder newBody = new StringBuilder();
	    //newBody.append(strings.replyFirstLine(MailUtils.extractNameFromAddr(from), message.getSentDate()));
	    newBody.append("\n");
	    newBody.append("\n");
	    if (!message.getText().isEmpty())
		for(String s: message.getText().split("\n", -1))
		    newBody.append(">" + s + "\n");
	    if (wideReply)
	    {
		final MailUtils utils;
		try {
		    utils = new MailUtils(bytes);
		}
		catch (IOException e)
		{
		    luwrain.crash(e);
		    return false;
		}
		luwrain.launchApp("message", new String[]{
			replyTo,
			utils.getWideReplyCc(getCcExcludeAddrs(), true),
			subject,
			newBody.toString()
		    });
	    } else 
	    {
		luwrain.launchApp("message", new String[]{
			replyTo,
			subject,
			newBody.toString()
		    });
	    }
	    return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
	*/
	return false;
    }

    boolean makeForward(StoredMailMessage message)
    {
	NullCheck.notNull(message, "message");
	try {
	    if (currentMessage == null)
		return false;
	    final StoredMailMessage m = message != null?message:currentMessage;
	    String subject = m.getSubject();
	    if (!subject.toLowerCase().startsWith("fwd: "))
		subject = "Fwd: " + subject;
	    final byte[] bytes = m.getRawMessage();
	    final String from = m.getFrom();
	    final StringBuilder newBody = new StringBuilder();
	    newBody.append("=== Пересылаемое сообщение ===\n");
	    newBody.append("ОТ: ");
	    newBody.append(m.getFrom());
	    newBody.append("\n");
	    newBody.append("Кому: ");
	    if (m.getTo().length > 0)
	    {
		final String[] values = m.getTo();
		newBody.append(values[0]);
		for(int i = 1;i < values.length;++i)
		    newBody.append("," + values[i]);
	    }
	    newBody.append("\n");
	    if (m.getCc().length > 0)
	    {
		newBody.append("Копия: ");
		final String[] values = m.getCc();
		newBody.append(values[0]);
		for(int i = 1;i < values.length;++i)
		    newBody.append("," + values[i]);
	    }
	    newBody.append("\n");
	    newBody.append("Тема: " + m.getSubject() + "\n");
	    newBody.append("Дата: " + m.getSentDate() + "\n");
	    newBody.append("\n");
	    for(String s: m.getText().split("\n"))
		newBody.append(s + "\n");
	    newBody.append("=== Конец пересылаемого сообщения ===");
	    luwrain.launchApp("message", new String[]{
		    "",
		    subject,
		    newBody.toString()
		});
	    return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean onFolderUniRefQuery(UniRefAreaQuery query, StoredMailFolder folder)
    {
	NullCheck.notNull(query, "query");
	NullCheck.notNull(folder, "folder");
	try {
	    final String uniRef = storing.getFolders().getUniRef(folder);
	    if (uniRef == null || uniRef.trim().isEmpty())
		return false;
	    query.answer(uniRef);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
	return true;
    }

    boolean saveAttachment(String fileName)
    {
	/*
	  if (currentMessage == null)
	    return false;
	File destFile = new File(luwrain.launchContext().userHomeDirAsFile(), fileName);
	destFile = Popups.file(luwrain, "Сохранение прикрепления", "Введите имя файла для сохранения прикрепления:", destFile, 0, 0);
	if (destFile == null)
	    return false;
	if (destFile.isDirectory())
	    destFile = new File(destFile, fileName);
	final org.luwrain.util.MailEssentialJavamail util = new org.luwrain.util.MailEssentialJavamail();
	try {
	    if (!util.saveAttachment(currentMessage.getRawMail(), fileName, destFile))
	    {
		luwrain.message("Целостность почтового сообщения нарушена, сохранение прикрепления невозможно", Luwrain.MESSAGE_ERROR);
		return false;
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время сохранения прикрепления произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	luwrain.message("Файл " + destFile.getAbsolutePath() + " успешно сохранён", Luwrain.MESSAGE_OK);
	*/
	return true;
    }

    private String[] getCcExcludeAddrs()
    {
	final org.luwrain.core.Settings.PersonalInfo sett = org.luwrain.core.Settings.createPersonalInfo(luwrain.getRegistry());
	final String addr = sett.getDefaultMailAddress("");
	if (addr.trim().isEmpty())
	    return new String[0];
	return new String[]{addr};
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
