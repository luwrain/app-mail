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
import java.util.concurrent.atomic.*;
import org.apache.logging.log4j.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.mail.persistence.model.*;

import org.luwrain.controls.WizardArea.Frame;
import org.luwrain.controls.WizardArea.WizardValues;
import org.luwrain.io.json.PopularMailServer;

import static org.luwrain.core.DefaultEventResponse.*;
import static org.luwrain.pim.mail.PopularServers.*;

final class StartingLayout extends LayoutBase
{
    static private final Logger log = LogManager.getLogger();

    final App app;
    final WizardArea wizardArea;
    final Frame introFrame, passwordFrame;

    private String mail = "", passwd = "";
    private PopularMailServer preset = null;

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
	for(var s: getPopularMailServers())
	{
	    var found = new AtomicBoolean(false);
	    s.getSuffixes().forEach(suff -> found.set(found.get() || mail.toLowerCase().endsWith(suff)));
	    if (found.get())
	    {
		this.mail = mail.trim();
		this.preset = s;
			wizardArea.show(passwordFrame);
				app.setEventResponse(text(Sounds.OK, app.getStrings().wizardPasswordAnnouncement()));
		return true;
	    }
	}
	return false;
    }

    private boolean onPassword(WizardValues values)
    {
	final String password = values.getText(0).trim();
	if (password.isEmpty())
	{
	    app.message(app.getStrings().wizardPasswordIsEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	if (preset != null)
	{
	    var a = new Account();
	    //SMTP
	    a.setType(Account.Type.SMTP);
	    a.setName(preset.getSmtp().getHost() + " для " + mail);
	    a.setHost(preset.getSmtp().getHost());
	    a.setPort(preset.getSmtp().getPort());
	    a.setLogin(mail);
	    a.setPasswd(password);
	    a.setTls(preset.getSmtp().isTls());
	    a.setSsl(preset.getSmtp().isSsl());
	    a.setEnabled(true);
	    a.setDefaultAccount(true);
	    a.setLeaveMessages(false);
	    log.trace("Adding account " + a.toString());
	    app.getData().accountDAO.add(a);
	    	    a = new Account();
	    //POP3
	    a.setType(Account.Type.POP3);
	    a.setName(preset.getPop3().getHost() + " для " + mail);
	    a.setHost(preset.getPop3().getHost());
	    a.setPort(preset.getPop3().getPort());
	    a.setLogin(mail);
	    a.setPasswd(password);
	    a.setTls(preset.getPop3().isTls());
	    a.setSsl(preset.getPop3().isSsl());
	    a.setEnabled(true);
	    a.setDefaultAccount(false);
	    a.setLeaveMessages(true);
	    log.trace("Adding account " + a.toString());
	    app.getData().accountDAO.add(a);
	}
	/*
	this.smtp.setPasswd(password);
	pop3.setPasswd(password);
//	NullCheck.notNull(app.getStoring(), "storing");
//	NullCheck.notNull(app.getStoring().getAccounts(), "accounts");
//	app.getStoring().getAccounts().save(smtp);
//	app.getStoring().getAccounts().save(pop3);
	app.layouts().main();
	*/
	return true;
    }
}
