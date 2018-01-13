
package org.luwrain.app.mail;

import org.luwrain.base.*;
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
	    },

	};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new ExtensionObject[]{

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "mail";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new MailApp()};
		}
	    },

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "message";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length == 0)
			return new Application[]{new org.luwrain.app.message.App()};
		    switch(args.length)
		    {
		    case 1:
			return new Application[]{new org.luwrain.app.message.App(args[0], "", "", "")};
		    case 2:
			return new Application[]{new org.luwrain.app.message.App(args[0], "", args[1], "")};
		    case 3:
			return new Application[]{new org.luwrain.app.message.App(args[0], "", args[1], args[2])};
		    case 4:
			return new Application[]{new org.luwrain.app.message.App(args[0], args[1], args[2], args[3])};
		    }
		    return new Application[]{new org.luwrain.app.message.App()};
		}
	    }

	    

	};
    }
}
