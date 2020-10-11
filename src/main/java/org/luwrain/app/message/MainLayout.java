

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
    private final FormArea formArea;
    private final MutableLinesImpl lines;
    private int attachmentCounter = 0;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	final MessageContent msg = null;
		this.lines = new MutableLinesImpl(msg.textLines());
		this.formArea = new FormArea(new DefaultControlContext(app.getLuwrain())){
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

    /*
    MailMessage constructMailMessage() throws org.luwrain.pim.PimException
    {
	final MailMessage msg = new MailMessage();
	msg.setTo(Base.splitAddrs(formArea.getEnteredText(TO_NAME)));
	msg.setCc(Utils.splitAddrs(formArea.getEnteredText(CC_NAME)));
	msg.setSubject(formArea.getEnteredText(SUBJECT_NAME));
	msg.setText(getText());
	final List<String> attachments = new LinkedList();
	for(File f: getAttachmentFiles())
	    attachments.add(f.getAbsolutePath());
	msg.setAttachments(attachments.toArray(new String[attachments.size()]));
	return msg;
    }

    private MultilineEdit.Params createEditParams()
    {
	final MultilineEdit.Params params = formArea.createMultilineEditParams(new DefaultControlContext(app.getLuwrain()), lines);
	final MultilineEditCorrector corrector = (MultilineEditCorrector)params.model;
	params.model = new DirectScriptMultilineEditCorrector(params.context, corrector, HOOKS_PREFIX);
	return params;
    }
    */

AreaLayout getLayout()
{
    return new AreaLayout(formArea);
}
}

/*
			    if (actions.onSend(messageArea, true))
			    {
				base.luwrain.runWorker(org.luwrain.pim.workers.Smtp.NAME);
				closeApp();
			    }
			    return true;
			}
			if (ActionEvent.isAction(event, "choose-to"))
			    return actions.onEditTo(messageArea);
			if (ActionEvent.isAction(event, "choose-cc"))
			    return actions.onEditCc(messageArea);
			if (ActionEvent.isAction(event, "attach-file"))
			    return actions.onAttachFile(messageArea);
			if (ActionEvent.isAction(event, "delete-attachment"))
			    return actions.onDeleteAttachment(messageArea);
			return false;
		    case OK:
			if (actions.onSend( messageArea, false))
			{
			    base.luwrain.runWorker(org.luwrain.pim.workers.Smtp.NAME);
			    closeApp();
			}
			return true;

    /*
    //Returns true, if the message is successfully saved in the pending queue, the sending worker will be launched
    boolean onSend(MessageArea area, boolean useAnotherAccount)
    {
	NullCheck.notNull(area, "area");
	if (!isReadyForSending(area))
	    return false;
	try {
	    final MailAccount account;
	    final MailAccount defaultAccount = base.mailStoring.getAccounts().getDefault(MailAccount.Type.SMTP);
	    if (useAnotherAccount)
	    {
		account = conv.accountToSend();
		if (account == null)
		    return false;
	    }else
	    {
		if (defaultAccount == null)
		{
		    if (!conv.confirmLaunchingAccountWizard())
			return false;
		    /*FIXME:
		    if (!(new org.luwrain.pim.wizards.Mail(luwrain).start()))
			return false;
		    /
		    account = base.mailStoring.getAccounts().getDefault(MailAccount.Type.SMTP);
		} else
		    account = defaultAccount;
	    }
	    return base.send(account, area.constructMailMessage());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean onEditTo(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editTo();
	if (res != null)
	    area.setTo(res);
	return true;
    }

    boolean onEditCc(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    boolean onAttachFile(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final File file = conv.attachment();
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
	if (!conv.confirmAttachmentDeleting(a.file))
	    return true;
	area.removeAttachment(index);
	luwrain.message("Прикрепление " + a.file.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }

        private boolean isReadyForSending(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	if (area.getTo().trim().isEmpty())
	{
	    luwrain.message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);//FIXME:
	    area.focusTo();
	    return false;
	}
	if (area.getSubject().trim().isEmpty())
	{
	    luwrain.message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    area.focusSubject();
	    return false;
	}
	return true;
    }
*/
