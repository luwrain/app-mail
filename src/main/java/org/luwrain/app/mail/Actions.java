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

    boolean onSummaryClick(ListArea summaryArea)
    {
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.selected();
	if (o == null || !(o instanceof MailMessage))
	    return false;
	final MailMessage message = (MailMessage)o;
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
		    return true;
    }

    boolean onSummaryDelete(Base base, TableArea summaryArea, boolean deleteForever)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.getSelectedRow();
	if (o == null || !(o instanceof MailMessage))
	    return false;
	final MailMessage message = (MailMessage)o;
	if (!deleteInSummary(message, deleteForever))
	    return true;
	summaryArea.refresh();
	app.clearMessageArea();
	return true;
    }

    boolean onSummaryReply(ListArea summaryArea)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof MailMessage))
	    return false;
	final MailMessage message = (MailMessage)obj;
	return base.hooks.makeReply(message);
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

        boolean deleteInSummary(MailMessage message, boolean deleteForever)
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
