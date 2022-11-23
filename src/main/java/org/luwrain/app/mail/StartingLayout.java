/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.controls.WizardArea.Frame;
import org.luwrain.controls.WizardArea.WizardValues;

final class StartingLayout extends LayoutBase
{
    final App app;
    final WizardArea wizardArea;
    final Frame introFrame;

    private String mail = "", passwd = "";

    StartingLayout(App app)
    {
	super(app);
	this.app = app;
	this.wizardArea = new WizardArea(getControlContext()) ;
	this.introFrame = wizardArea.newFrame()
	.addText(app.getStrings().wizardIntro())
	.addInput(app.getStrings().wizardMailAddr(), "")
	.addClickable(app.getStrings().wizardContinue(), this::onContinue);
	wizardArea.show(introFrame);
		setAreaLayout(wizardArea, null);
    }

    private boolean onContinue(WizardValues values)
    {
	final String mail = values.getText(0).trim();
	if (mail.isEmpty())
	{
	    app.message(app.getStrings().wizardMailAddrIsEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	if (!mail.matches(".*@.*\\..*"))
	{
	    app.message(app.getStrings().wizardMailAddrIsInvalid(), Luwrain.MessageType.ERROR);
	    return true;
	}
	return false;
    }
}
