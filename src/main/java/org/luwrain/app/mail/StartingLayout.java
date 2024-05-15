/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>
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

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.mail.persistence.model.*;

import org.luwrain.controls.WizardArea.Frame;
import org.luwrain.controls.WizardArea.WizardValues;

import static org.luwrain.core.DefaultEventResponse.*;

final class StartingLayout extends LayoutBase
{
    final App app;
    final WizardArea wizardArea;
    final Frame introFrame, passwordFrame;

    private String mail = "", passwd = "";
    private Account smtp = null, pop3 = null;

    StartingLayout(App app)
    {
	super(app);
	this.app = app;
	this.wizardArea = new WizardArea(getControlContext()) ;
	this.introFrame = wizardArea.newFrame()
	.addText(app.getStrings().wizardIntro())
	.addInput(app.getStrings().wizardMailAddr(), "")
	.addClickable(app.getStrings().wizardContinue(), this::onMailAddress);
	this.passwordFrame = wizardArea.newFrame()
	.addText(app.getStrings().wizardPasswordIntro())
	.addInput(app.getStrings().wizardPassword(), "")
	.addClickable(app.getStrings().wizardContinue(), this::onPassword);
	wizardArea.show(introFrame);
	setAreaLayout(wizardArea, null);
    }

    private boolean onMailAddress(WizardValues values)
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
	final 	Map<String, Account> accounts = null;//app.getHooks().server(mail);
	if (accounts == null)
	    return false;
	this.smtp = accounts.get("smtp");
	this.pop3 = accounts.get("pop3");
	if (smtp == null || pop3 == null)
	    return false;
	wizardArea.show(passwordFrame);
	app.setEventResponse(text(Sounds.OK, app.getStrings().wizardPasswordAnnouncement()));
	return true;
    }

    private boolean onPassword(WizardValues values)
    {
	final String password = values.getText(0).trim();
	if (password.isEmpty())
	{
	    app.message(app.getStrings().wizardPasswordIsEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	this.smtp.setPasswd(password);
	pop3.setPasswd(password);
//	NullCheck.notNull(app.getStoring(), "storing");
//	NullCheck.notNull(app.getStoring().getAccounts(), "accounts");
//	app.getStoring().getAccounts().save(smtp);
//	app.getStoring().getAccounts().save(pop3);
	app.layouts().main();
	return true;
    }
}
