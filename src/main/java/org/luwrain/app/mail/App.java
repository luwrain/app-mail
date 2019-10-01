/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

final class App implements Application, MonoApp
{
    private Luwrain luwrain = null;
    private Actions actions = null;
    private ActionLists actionLists = null;
    private Base base = null;
    private Strings strings = null;

    private TreeArea foldersArea = null;
    private ListArea summaryArea = null;
    private ReaderArea messageArea = null;
    private AreaLayoutHelper layout = null;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actions = new Actions(base, this);
	this.actionLists = new ActionLists(base);
	if (base.storing == null)
	    return new InitResult(InitResult.Type.FAILURE);
	createAreas();
	if (base.openDefaultFolder())
	    summaryArea.refresh();
		this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
		    }, new AreaLayout(AreaLayout.LEFT_RIGHT, foldersArea, summaryArea));
			luwrain.runWorker(org.luwrain.pim.workers.Pop3.NAME);
	return new InitResult();
    }

    private void createAreas()
    {
	this.foldersArea = new TreeArea(base.createFoldersTreeParams((area, obj)->{
		    if (obj == null || !(obj instanceof MailFolder))
			return false;
		    final MailFolder folder = (MailFolder)obj;
		    return actions.onOpenFolder(folder, summaryArea);
		})) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.UNIREF_AREA:
			{
			    final Object selected = foldersArea.selected();
			    if (selected == null || !(selected instanceof MailFolder) || !(query instanceof UniRefAreaQuery))
				return false;
			    final UniRefAreaQuery uniRefQuery = (UniRefAreaQuery)query;
			    final MailFolder folder = (MailFolder)selected;
			    try {
				final String uniRef = base.storing.getFolders().getUniRef(folder);
				if (uniRef == null || uniRef.trim().isEmpty())
				    return false;
				uniRefQuery.answer(uniRef);
			    }
			    catch(PimException e)
			    {
				luwrain.crash(e);
				return false;
			    }
			}
		    default:
			return super.onAreaQuery(query);
		    }
		}
	    };

	this.summaryArea = new ListArea(base.createSummaryParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			case BACKSPACE:
			    AreaLayoutHelper.activatePrevArea(luwrain, getAreaLayout(), this);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "reply"))
			    return actions.onSummaryReply(this);
						if (ActionEvent.isAction(event, "delete-message"))
						    return actions.onSummaryDelete(this, false);
			return false;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getSummaryAreaActions();
		}
	    };

	final ReaderArea.Params messageParams = new ReaderArea.Params();
	messageParams.context = new DefaultControlContext(luwrain);
	this.messageArea = new ReaderArea(messageParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			case BACKSPACE:
			    return AreaLayoutHelper.activatePrevArea(luwrain, getAreaLayout(), this);
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
    }

    void saveAttachment(String fileName)
    {
	actions.saveAttachment(fileName);
    }

    void refreshMessages()
    {
	summaryArea.refresh();
    }

    void clearMessageArea()
    {
	base.openMessage(null);//FIXME:closeMessage()
	//	messageArea.show(null);
	//messageArea.setHotPoint(0, 0);
    }

    boolean switchToRawMessage()
    {
	if (!base.hasOpenedMessage())
	    return false;
	return true;
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override  public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
