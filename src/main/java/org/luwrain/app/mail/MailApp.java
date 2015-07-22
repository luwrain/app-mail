/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

public class MailApp implements Application, Actions
{
    private Luwrain luwrain;
    private Base base = new Base();
    private Strings strings;

    private TreeArea foldersArea;
    private TableArea summaryArea;
    private MessageArea messageArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = "";//FIXME:Langs.requestStringConstructor("mail-reader");
	if (o == null)
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	base.init(luwrain, strings);
	createFoldersArea();
	createSummaryArea();
	createMessageArea();
	return true;
    }

    @Override public String getAppName()
    {
	return "mail";
    }

    public void openFolder(Object folder)
    {
	/*
	if (folder == null || !base.isStoredMailGroup(folder))
	    return;
	*/
	if (base.openFolder(folder, summaryArea))
	    gotoSummary(); else
	    luwrain.message(strings.errorOpeningFolder());
    }

    private void createFoldersArea()
    {
	final Actions a = this;
	final Strings s = strings;
	foldersArea = new TreeArea(new DefaultControlEnvironment(luwrain),
				   base.getFoldersModel(),
				   strings.foldersAreaName()){
		private Strings strings = s;
		private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isCommand() &&
			!event.isModified() &&
			event.getCommand() == KeyboardEvent.TAB)
		    {
			actions.gotoSummary();
			return true;
		    }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public void onClick(Object obj)
		{
		    if (obj != null)
			actions.openFolder(obj);
		}
	    };
    }

    private void createSummaryArea()
    {
	final Actions a = this;
	final Strings s = strings;
	summaryArea = new TableArea(new DefaultControlEnvironment(luwrain),
				    base.getSummaryModel(),
				    strings.summaryAreaName(),
				    base.getSummaryAppearance(),
				    null) { //Click handler;
		private Strings strings = s;
		private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isCommand() &&
			!event.isModified() &&
			event.getCommand() == KeyboardEvent.TAB)
		    {
			actions.gotoMessage();
			return true;
		    }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public boolean onClick(TableModel model,
					      int col,
					      int row,
					      Object cell)
		{
		    //FIXME:
		    return false;
		}
	    };
    }

    private void createMessageArea()
    {
	messageArea = new MessageArea(luwrain, this, strings);
    }

    public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, messageArea);
    }

    public void gotoFolders()
    {
	luwrain.setActiveArea(foldersArea);
    }

    public void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    public void gotoMessage()
    {
	luwrain.setActiveArea(messageArea);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}
