

package org.luwrain.app.message;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.MailMessage;

import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final MessageArea messageArea;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.messageArea = new MessageArea(createParams()){
		final Actions actions = actions();
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
    }

    MailMessage getMailMessage()
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

    private boolean actSend()
    {
	if (app.onSend(getMailMessage(), true))
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

    boolean onAttachFile(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final File file = app.getConv().attachment();
	if (file == null)
	    return true;
	area.addAttachment(file);
	return true;
    }

    boolean onDeleteAttachment(MessageArea area)
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

    private MessageArea.Params createParams()
    {
	final MessageArea.Params params = new MessageArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	return params;
    }

AreaLayout getLayout()
{
    return new AreaLayout(messageArea);
}
}
