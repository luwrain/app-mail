

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

*/
