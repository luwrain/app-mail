
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
