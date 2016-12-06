
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;
import org.luwrain.doctree.control.*;
import org.luwrain.pim.mail.*;

class Actions
{
    private final Luwrain luwrain;
    private final MailApp app;

    Actions(Luwrain luwrain, MailApp app)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	this.luwrain = luwrain;
	this.app = app;
    }


    static Action[] getSummaryAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	};
    }

    boolean onSummaryClick(Base base, TableModel model,
			   int col, int row,
			   DoctreeArea messageArea, Object obj)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(model, "model");
	NullCheck.notNull(messageArea, "messageArea");
	final Object o = model.getRow(row);
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	base.setCurrentMessage(message);
	messageArea.setDocument(base.prepareDocumentForCurrentMessage(), 512);
app.	enableMessageMode(MailApp.Mode.REGULAR);
	app.gotoMessage();
		    return true;

    }
}
