
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class RawMessageArea extends NavigationArea
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final App app;
    private StoredMailMessage message;
    private String[] content = new String[0];

    RawMessageArea(Luwrain luwrain, App app,
		Strings strings)
    {
	super(new DefaultControlContext(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.app = app;
	this.strings = strings;
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
	    final String str = new String(message.getRawMessage(), "US-ASCII");
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

    @Override public boolean onInputEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case TAB:
app.gotoFolders();
	    return true;
	    case BACKSPACE:
app.gotoSummary();
	    return true;
	    }
	return super.onInputEvent(event);
    }

    @Override public boolean onSystemEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case CLOSE:
app.closeApp();
	    return true;
	default:
	    return super.onSystemEvent(event);
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
