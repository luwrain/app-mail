/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings> implements MonoApp
{
    static final String
	LOG_COMPONENT = "mail",
	HOOK_ORGANIZE_SUMMARY = "mail.summary.organize",
	HOOK_REPLY = "mail.reply";

    private Hooks hooks = null;
    private MailStoring storing = null;
    private Conv conv = null;
    private MainLayout mainLayout = null;
    private StartingLayout startingLayout = null;

    public App()
    {
	super(Strings.NAME, Strings.class, "luwrain.mail");
    }

    @Override protected AreaLayout onAppInit()
    {
	this.hooks = new Hooks(getLuwrain());
	/*
	this.storing = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
	if (storing == null)
	    return null;
	*/
	this.conv = new Conv(this);
	//	this.mainLayout = new MainLayout(this);
	this.startingLayout = new StartingLayout(this);
	setAppName(getStrings().appName());
	return startingLayout.getAreaLayout();
    }

    boolean fetchIncomingBkg()
    {
	getLuwrain().runWorker(org.luwrain.pim.workers.Pop3.NAME);
	return true;
    }

    void layout(AreaLayout layout)
    {
	getLayout().setBasicLayout(layout);
    }

    private Layouts layouts()
    {
	return new Layouts(){
	    @Override public void messageMode()
	    {
	    }
	};
    }


    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    MailStoring getMailStoring() { return this.storing; }
    MailStoring getStoring() { return this.storing; }
    Hooks getHooks() { return this.hooks; }
    Conv getConv() { return conv; }

    interface Layouts
    {
	void messageMode();
    }
}
