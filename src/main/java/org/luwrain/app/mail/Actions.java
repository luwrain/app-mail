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

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class Actions extends Utils
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;
    private final App app;

    Actions(Base base, App app)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(app, "app");
	this.base = base;
	this.luwrain = base.luwrain;
	this.strings = base.strings;
	this.app = app;
    }

    boolean openFolder(StoredMailFolder folder, ListArea summaryArea)
    {
	NullCheck.notNull(folder, "folder");
	NullCheck.notNull(summaryArea, "summaryArea");
	try {
	base.openFolder(folder);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	summaryArea.refresh();
	luwrain.setActiveArea(summaryArea);
	return true;
    }



    Action[] getSummaryAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить", new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	    new Action("delete-message", strings.actionDeleteMessage(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
	};
    }

    boolean onSummaryClick(Base base, TableArea.Model model,
			   int col, int row, Object obj,
			   TableArea summaryArea, 			   ReaderArea messageArea)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(model, "model");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(messageArea, "messageArea");
	final Object o = model.getRow(row);
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	try {
	    if (message.getState() == MailMessage.State.NEW)
	    {
		message.setState(MailMessage.State.READ);
		summaryArea.refresh();
	    }
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	}
	base.openMessage(message);
	try {
	messageArea.setDocument(prepareDocForMsg(base.getOpenedMessage()), 512);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
//	app.gotoMessage();
		    return true;
    }

    boolean onDeleteInSummary(Base base, TableArea summaryArea, boolean deleteForever)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.getSelectedRow();
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	if (!deleteInSummary(message, deleteForever))
	    return true;
	summaryArea.refresh();
	app.clearMessageArea();
	return true;
    }

    boolean onSummaryReply(Base base, TableArea summaryArea, boolean wideReply)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.getSelectedRow();
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)obj;
	return makeReply(message, wideReply);
    }

    /*
    boolean makeForward(StoredMailMessage message)
    {
	if (!base.makeForward(message))
	    luwrain.message("Во время подготовки перенаправленяи произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }
    */



    void onLaunchMailFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--MAIL"});
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
	    if (!base.hasOpenedMessage())
		return false;
	    final StoredMailMessage m = message != null?message:base.getOpenedMessage();
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


}
