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
    private final Layouts layouts;

    Actions(Base base, Layouts layouts)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(layouts, "layouts");
	this.base = base;
	this.luwrain = base.luwrain;
	this.strings = base.strings;
	this.layouts = layouts;
    }

    boolean onOpenFolder(MailFolder folder, ListArea summaryArea)
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
	summaryArea.reset(false);
	luwrain.setActiveArea(summaryArea);
	return true;
    }

    boolean onSummaryClick(Object obj, ListArea summaryArea, ReaderArea messageArea)
    {
	NullCheck.notNull(obj, "obj");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(messageArea, "messageArea");
	if (!(obj instanceof SummaryItem))
	    return false;
	final MailMessage message = ((SummaryItem)obj).message;
	if (message == null)
	    return false;
	try {
	    if (message.getState() == MailMessage.State.NEW)
	    {
		message.setState(MailMessage.State.READ);
		summaryArea.refresh();
	    }
	    	    	base.openMessage(message);
			messageArea.setDocument(createDocForMessage(message), 128);
	layouts.messageMode();
	return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
    }

    boolean onSummaryDelete(ListArea summaryArea, boolean deleteForever)
    {
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.selected();
	if (o == null || !(o instanceof SummaryItem))
	    return false;
	final SummaryItem item = (SummaryItem)o;
	if (item.message == null)
	    return false;
	try {
	    if (deleteForever)
		base.storing.getMessages().delete(item.message); else
		item.message.setState(MailMessage.State.DELETED);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	return true;
    }

    boolean onSummaryReply(ListArea summaryArea)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof SummaryItem))
	    return false;
	final SummaryItem summaryItem = (SummaryItem)obj;
	if (summaryItem.message == null)
	    return false;
	return base.hooks.makeReply(summaryItem.message);
    }

    void onLaunchMailFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--MAIL"});
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
}
