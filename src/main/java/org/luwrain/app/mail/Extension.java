
package org.luwrain.app.mail;

import java.util.*;

import org.luwrain.core.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new Command(){
		@Override public String getName()
		{
		    return "mail";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("mail");
		}
	    }};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{new Shortcut() {
		@Override public String getName()
		{
		    return "mail";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new MailApp()};
		}
	    }};
    }
}
