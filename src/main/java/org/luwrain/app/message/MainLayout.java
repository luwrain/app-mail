/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.message;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.MailMessage;
import org.luwrain.io.json.*;
import org.luwrain.app.base.*;
import org.luwrain .util.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final MessageArea messageArea;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	final Settings.PersonalInfo sett = Settings.createPersonalInfo(app.getLuwrain().getRegistry());
	final List<String> text = new ArrayList();
	text.addAll(Arrays.asList(app.messageContent.getTextAsArray()));
	text.add("");
	text.addAll(Arrays.asList(TextUtils.splitLinesAnySeparator(sett.getSignature(""))));
	final MessageArea.Params params = new MessageArea.Params();
	params.context = getControlContext();
	params.text = text.toArray(new String[text.size()]);
	this.messageArea = new MessageArea(params){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			    return actSend();
			}
		    return super.onSystemEvent(event);
		}
	    };
	setAreaLayout(messageArea, actions(
					   action("sent", app.getStrings().actionSend(), MainLayout.this::actSend)
					   ));
    }

    private boolean actSend()
    {
	if (app.send(getMailMessage(), true))
	{
	    app.getLuwrain().runWorker(org.luwrain.pim.workers.Smtp.NAME);
	    app.closeApp();
	}
	return true;
    }

    private boolean actEditTo()
    {
	final String res = app.getConv().editTo();
	if (res != null)
	    messageArea.setTo(res);
	return true;
    }

    private boolean actEditCc(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final String res = app.getConv().editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    private boolean actAttachFile(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final File file = app.getConv().attachment();
	if (file == null)
	    return true;
	area.addAttachment(file);
	return true;
    }

    private boolean actDeleteAttachment(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final int index = area.getHotPointY();
	if (area.getItemTypeOnLine(index) != MessageArea.Type.STATIC)
	    return false;
	final Object obj = area.getItemObj(index);
	if (obj == null || !(obj instanceof Attachment))
	    return false;
	final Attachment a = (Attachment)obj;
	if (!app.getConv().confirmAttachmentDeleting(a.file))
	    return true;
	area.removeAttachment(index);
	app.getLuwrain().message("Прикрепление " + a.file.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }

    private boolean isReadyForSending(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	if (area.getTo().trim().isEmpty())
	{
	    app.getLuwrain().message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);//FIXME:
	    area.focusTo();
	    return false;
	}
	if (area.getSubject().trim().isEmpty())
	{
	    app.getLuwrain().message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    area.focusSubject();
	    return false;
	}
	return true;
    }


        private MailMessage getMailMessage()
    {
	final MailMessage msg = new MailMessage();
	msg.setTo(App.splitAddrs(messageArea.getTo()));
	msg.setCc(App.splitAddrs(messageArea.getCc()));
	msg.setSubject(messageArea.getSubject());
	msg.setText(messageArea.getText());
	final List<String> attachments = new LinkedList();
	for(File f: messageArea.getAttachmentFiles())
	    attachments.add(f.getAbsolutePath());
	msg.setAttachments(attachments.toArray(new String[attachments.size()]));
	return msg;
    }
}
