/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

import java.io.*;
import org.apache.logging.log4j.*;

import org.luwrain.core.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings> implements MonoApp
{
    static final Logger log = LogManager.getLogger();

    private Hooks hooks = null;
    //    private MailStoring storing = null;
    private Data data = null;
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
	this.data = new Data(getStrings(), new File(getLuwrain().getFileProperty(Luwrain.PROP_DIR_USERHOME), ".luwrain-defaults.conf"));
	this.conv = new Conv(this);
	this.mainLayout = new MainLayout(this, data);
	this.startingLayout = new StartingLayout(this);
	setAppName(getStrings().appName());
	if (data.accountDAO.getAll().isEmpty())
	    return startingLayout.getAreaLayout();
	return mainLayout.getAreaLayout();
    }

    Layouts layouts()
    {
	return new Layouts(){
	    @Override public void main()
	    {
		setAreaLayout(mainLayout);
		getLuwrain().announceActiveArea();
	    }
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

//    MailStoring getStoring() { return this.storing; }
    Hooks getHooks() { return this.hooks; }
    Conv getConv() { return conv; }
    public Data getData() { return data; }

    interface Layouts
    {
	void main();
	void messageMode();
    }
}
