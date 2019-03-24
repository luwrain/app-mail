
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.mail.*;

final class App implements Application, MonoApp
{
    enum Mode {
	REGULAR,
	RAW,
    };

    private Luwrain luwrain;
    private Actions actions = null;
    private Base base = null;
    private Strings strings;

    private Mode mode = Mode.REGULAR;
    private TreeArea foldersArea;
    private ListArea summaryArea;
    private ReaderArea messageArea;
    private RawMessageArea rawMessageArea;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(this, luwrain, strings);
	this.actions = new Actions(base, this);
	if (base.storing == null)
	    return new InitResult(InitResult.Type.FAILURE);
	createAreas();
	if (base.openDefaultFolder())
	    summaryArea.refresh();
	return new InitResult();
    }

    void saveAttachment(String fileName)
    {
	base.saveAttachment(fileName);
    }

void refreshMessages()
    {
	summaryArea.refresh();
    }


    private boolean onFolderUniRefQuery(AreaQuery query)
    {
	if (query == null || !(query instanceof UniRefAreaQuery))
	    return false;
	final Object selected = foldersArea.selected();
	if (selected == null || !(selected instanceof StoredMailFolder))
	    return false;
	return base.onFolderUniRefQuery((UniRefAreaQuery)query, (StoredMailFolder)selected);
    }

    void clearMessageArea()
    {
	base.setCurrentMessage(null);
	//	messageArea.show(null);
	//messageArea.setHotPoint(0, 0);
	rawMessageArea.show(null);
	rawMessageArea.setHotPoint(0, 0);
	enableMessageMode(Mode.REGULAR);
    }

    boolean switchToRawMessage()
    {
	if (!base.hasCurrentMessage())
	    return false;
	enableMessageMode(Mode.RAW);
	gotoMessage();
	return true;
    }

    private void createAreas()
    {
	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.context = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getFoldersModel(); 
	treeParams.name = strings.foldersAreaName();
	this.foldersArea = new TreeArea(treeParams) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    gotoSummary();
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
			return onFolderUniRefQuery(query);
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override public void onClick(Object obj)
		{
		    if (obj == null || !(obj instanceof StoredMailFolder))
			return;
		    final StoredMailFolder folder = (StoredMailFolder)obj;
actions.openFolder(folder, summaryArea);
		}
	    };

	final ListArea.Params summaryParams = new ListArea.Params();
	summaryParams.context = new DefaultControlEnvironment(luwrain);
	summaryParams.name = strings.summaryAreaName();
	summaryParams.model = base.getSummaryModel();
	summaryParams.appearance = new ListUtils.DoubleLevelAppearance(summaryParams.context){
			@Override public boolean isSectionItem(Object item)
		{
		    return false;
		}
	    };
	this.summaryArea = new ListArea(summaryParams) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    gotoMessage();
			    return true;
			case BACKSPACE:
			    gotoFolders();
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
			return onSummaryAreaAction(event);
		    default:
			return super.onSystemEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getSummaryAreaActions();
		}
	    };

	final ReaderArea.Params messageParams = new ReaderArea.Params();

	messageParams.context = new DefaultControlContext(luwrain);

messageArea = new ReaderArea(messageParams){

	@Override public boolean onInputEvent(KeyboardEvent event)
	{
	    NullCheck.notNull(event, "event");
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case TAB:
		    gotoFolders();
		    return true;
		case BACKSPACE:
		    gotoSummary();
		    return true;
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

	rawMessageArea = new RawMessageArea(luwrain, this, strings);

	summaryArea.setListClickHandler(				    (area,index,obj)->{return false;});
    }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	/*
	if (ActionEvent.isAction(event, "delete-message"))
	    return actions.onDeleteInSummary(base, summaryArea, false);
	*/
	/*
	if (ActionEvent.isAction(event, "delete-message-forever"))
	    return actions.onDeleteInSummary(base, summaryArea, false);
	*/
	/*
	if (ActionEvent.isAction(event, "reply"))
	    return actions.onSummaryReply(base, summaryArea, false);
	*/
	/*
	if (ActionEvent.isAction(event, "reply-all"))
	    return actions.onSummaryReply(base, summaryArea, true);
	*/

	    //	if (ActionEvent.isAction(event, "forward"))
	return false;
    }

    void gotoFolders()
    {
	luwrain.setActiveArea(foldersArea);
    }

    void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

void gotoMessage()
    {
	switch(mode)
	{
	case REGULAR:
	    luwrain.setActiveArea(messageArea);
	    return;
	case RAW:
	    luwrain.setActiveArea(rawMessageArea);
	    return;
	}
    }

    void enableMessageMode(Mode mode)
    {
	if (this.mode == mode)
	    return;
	this.mode = mode;
	luwrain.onNewAreaLayout();
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
	switch(mode)
	{
	case REGULAR:
	    return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, messageArea);
	case RAW:
	    return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, rawMessageArea);
	}
	return null;
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
