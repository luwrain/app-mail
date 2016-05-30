
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class RawMessageArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private StoredMailMessage message;
    private String[] content = new String[0];

    RawMessageArea(Luwrain luwrain, Actions actions,
		Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions =  actions;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strings");
    }

    boolean show(StoredMailMessage message)
    {
	if (message == null)
	{
	    this.message = null;
	    content = new String[0];
	luwrain.onAreaNewContent(this);
	    return true;
	}
	try {
	    final String str = new String(message.getRawMail(), "US-ASCII");
	    System.out.println("strlen " + str.length());
	content = str.split("\n");
	this.message = message;
	luwrain.onAreaNewContent(this);
	return true;
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    content = new String[0];
	    this.message = null;
	luwrain.onAreaNewContent(this);
	    return false;
	}
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case F9:
		actions.launchMailFetch();
		return true;
	    case TAB:
	    actions.gotoFolders();
	    return true;
	    case BACKSPACE:
	    actions.gotoSummary();
	    return true;
	    case F5://FIXME:Action
		    return actions.makeReply(null, false);
	    case F6://FIXME:Action
		return actions.makeForward(null);
	    }
	if (event.isSpecial() && event.withShiftOnly())
	    switch(event.getSpecial())
	    {
	    case F5://FIXME:Action
		    return actions.makeReply(null, true);
}
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case CLOSE:
	    actions.closeApp();
	    return true;
	case ACTION:
	    if (ActionEvent.isAction(event, "reply"))
	    {
		actions.makeReply(null, false);
		return true;
	    }
	    if (ActionEvent.isAction(event, "reply-all"))
	    {
		actions.makeReply(null, true);
		return true;
	    }
	    if (ActionEvent.isAction(event, "forward"))
	    {
		actions.makeForward(null);
		return true;
	    }
	    return false;


	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	};
    }

    @Override public String getAreaName()
    {
	return strings.messageAreaName();//FIXME:
    }

    @Override public int getLineCount()
    {
	return content.length > 1?content.length:1;
    }

    @Override public String getLine(int index)
    {
	return index < content.length?content[index]:"";
    }
}
