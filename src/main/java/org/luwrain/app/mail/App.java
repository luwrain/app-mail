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
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.template.*;

final class App extends AppBase<Strings> implements MonoApp
{
    static final String LOG_COMPONENT = "mail";
    
    private Hooks hooks = null;
    private MailStoring storing = null;
    private MainLayout mainLayout = null;

    App()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override protected boolean onAppInit()
    {
	this.hooks = new Hooks(getLuwrain());
	this.storing = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
		//luwrain.runWorker(org.luwrain.pim.workers.Pop3.NAME);
			return true;
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

    @Override  protected AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
