
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class MailApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.mail";

    private Luwrain luwrain;
    private final Base base = new Base();
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

    @Override public boolean makeReply(StoredMailMessage message)
    {
	try {
	base.makeReply(message);
	return true;
	}
	catch(Exception e)
			  {
			      e.printStackTrace();
			      luwrain.message("Произошла ошибка при подготовке ответа", Luwrain.MESSAGE_ERROR);
			      return true;
			  }
    }

    @Override public boolean makeForward(StoredMailMessage message)
    {
	return true;
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

    @Override public boolean onFolderUniRefQuery(AreaQuery query)
    {
	if (query == null || !(query instanceof ObjectUniRefQuery))
	    return false;
	final Object selected = foldersArea.selected();
	if (selected == null || !(selected instanceof FolderWrapper))
	    return false;
	return base.onFolderUniRefQuery((ObjectUniRefQuery)query, (FolderWrapper)selected);
    }

    @Override public void showMessage(StoredMailMessage message)
    {
	if (message == null)
	    return;
	messageArea.show(message);
	gotoMessage();
    }

    private void createAreas()
    {
	final Actions actions = this;
	final Strings s = strings;

	final TableClickHandler summaryHandler = new TableClickHandler(){
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
				   base.getFoldersModel(), strings.foldersAreaName()){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.gotoSummary();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.OBJECT_UNIREF:
			return actions.onFolderUniRefQuery(query);
		    default:
			return super.onAreaQuery(query);
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
				    base.getSummaryModel(), base.getSummaryAppearance(),
				    summaryHandler, strings.summaryAreaName()) { //Click handler;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.gotoMessage();
			    return true;
			case KeyboardEvent.BACKSPACE:
			    actions.gotoFolders();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[]{
			new Action("reply", "Ответить"),
			new Action("reply-all", "Ответить всем"),
			new Action("forward", "Переслать"),
		    };
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
