
package org.luwrain.app.mail;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

final class FoldersModel implements TreeListArea.Model<MailFolder>
{
    static private final int
	DEFAULT_ID = 17;

    final App app;
    FoldersModel(App app) { this.app = app; }

    @Override public boolean getItems(MailFolder obj, TreeListArea.Collector<MailFolder> collector)
    {
	collector.collect(Arrays.asList(app.getStoring().getFolders().load(obj)));
	return true;
    }

    @Override public MailFolder getRoot()
    {
	return app.getStoring().getFolders().getRoot();
    }

    @Override public boolean isLeaf(MailFolder item)
    {
	return !app.getStoring().getFolders().hasSubfolders(item);
    }
}
