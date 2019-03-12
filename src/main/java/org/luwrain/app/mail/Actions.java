
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

class Actions
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
	base.updateSummaryMessages(folder);
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
	base.setCurrentMessage(message);
	messageArea.setDocument(base.prepareDocumentForCurrentMessage(), 512);
app.	enableMessageMode(App.Mode.REGULAR);
	app.gotoMessage();
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
	if (!base.deleteInSummary(message, deleteForever))
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
	return base.makeReply(message, wideReply);
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
}
