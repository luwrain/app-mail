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

package org.luwrain.app.message;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings>
{
    private MainLayout mainLayout = null;

    private final MessageContent startingMessage = new MessageContent();

    public App()
    {
	super(Strings.NAME, Strings.class);
    }

    public App(String to, String cc, String subject, String text)
    {
	super(Strings.NAME, Strings.class);
	NullCheck.notNull(to, "to");
	NullCheck.notNull(cc, "cc");
	NullCheck.notNull(subject, "subject");
	NullCheck.notNull(text, "text");
	/*
	startingMessage.to = to;
	startingMessage.cc = cc;
	startingMessage.subject = subject;
	startingMessage.text = text;
	*/
    }

    @Override protected boolean onAppInit()
    {
	this.mainLayout = new MainLayout(this);
	return true;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

}
