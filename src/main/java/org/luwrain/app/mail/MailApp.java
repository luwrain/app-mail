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
import org.luwrain.pim.mail.*;

public class MailApp implements Application, Actions
{
    private static final String STRINGS_NAME = "luwrain.mail";


    private Luwrain luwrain;
    private Base base = new Base();
    private Strings strings;

    private TreeArea foldersArea;
    private TableArea summaryArea;
    private MessageArea messageArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, this, strings))//FIXME:Let user know what happens;
	    return false;
	createAreas();
	return true;
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public void refreshMessages(boolean refreshTableArea)
    {
	//FIXME:
    }

    @Override public void openFolder(StoredMailFolder folder)
    {
	if (!base.openFolder(folder))
	    return;
	summaryArea.refresh();
	gotoSummary();
    }

    @Override public void showMessage(StoredMailMessage message)
    {
	messageArea.show(message);
	gotoMessage();
    }

    @Override public boolean insertMessages()
    {
	if (!base.insertMessages())
	    return false;
	summaryArea.refresh();
	return true;
    }

    private void createAreas()
    {
	final Actions a = this;
	final Strings s = strings;

	final TableClickHandler summaryHandler = new TableClickHandler(){
		private Actions actions = a;
		@Override public boolean onClick(TableModel model,
						 int col,
						 int row,
						 Object obj)
		{
		    if (model == null)
			return false;
		    final Object o = model.getRow(row);
		    if (o == null || !(o instanceof StoredMailMessage))
			return false;
		    actions.showMessage((StoredMailMessage)o);
		    return true;
		}};

	foldersArea = new TreeArea(new DefaultControlEnvironment(luwrain),
				   base.getFoldersModel(),
				   strings.foldersAreaName()){
		private Strings strings = s;
		private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event == null)
			throw new NullPointerException("event may not be null");
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.gotoSummary();
			    return true;
			default:
			    return super.onKeyboardEvent(event);
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    if (event == null)
			throw new NullPointerException("eevent may not be null");
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
		    if (obj == null || !(obj instanceof FolderWrapper))
			return;
		    final FolderWrapper wrapper = (FolderWrapper)obj;
		    actions.openFolder(wrapper.folder());
		}
	    };

	summaryArea = new TableArea(new DefaultControlEnvironment(luwrain),
				    base.getSummaryModel(),
				    base.getSummaryAppearance(),
				    summaryHandler,
				    strings.summaryAreaName()) { //Click handler;
		private Strings strings = s;
		private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.gotoMessage();
			    return true;
			case KeyboardEvent.INSERT:
			    return actions.insertMessages();
			default:
			    return super.onKeyboardEvent(event);
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    if (event == null)
			throw new NullPointerException("event may not be null");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	messageArea = new MessageArea(luwrain, this, strings);
    }

    @Override  public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, messageArea);
    }

    @Override public void gotoFolders()
    {
	luwrain.setActiveArea(foldersArea);
    }

    @Override public void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    @Override public void gotoMessage()
    {
	luwrain.setActiveArea(messageArea);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}
