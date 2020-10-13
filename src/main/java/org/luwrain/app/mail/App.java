/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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

final class App extends AppBase<Strings> implements MonoApp
{
    static final String LOG_COMPONENT = "mail";

    private Hooks hooks = null;
    private MailStoring storing = null;
    private MainLayout mainLayout = null;

    App()
    {
	super(Strings.NAME, Strings.class, "luwrain.mail");
    }

    @Override protected boolean onAppInit()
    {
	this.hooks = new Hooks(getLuwrain());
	this.storing = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
	if (storing == null)
	    return false;
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return true;
    }

                boolean onInputEvent(Area area, InputEvent event, Runnable closing)
    {
	NullCheck.notNull(area, "area");
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case ESCAPE:
		if (closing != null)
		    closing.run(); else
		    closeApp();
		return true;
	    }
	return super.onInputEvent(area, event);
    }

    @Override public boolean onInputEvent(Area area, InputEvent event)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(event, "event");
	return onInputEvent(area, event, null);
    }

    MailStoring getMailStoring()
    {
	return this.storing;
    }

    boolean fetchIncomingBkg()
    {
	getLuwrain().runWorker(org.luwrain.pim.workers.Pop3.NAME);
	return true;
    }

    void layout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
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

    MailStoring getStoring()
    {
	return this.storing;
    }

    Hooks getHooks()
    {
	return this.hooks;
    }

    @Override  protected AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    interface Layouts
    {
	void messageMode();
    }
}
