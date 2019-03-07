
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

class Group
{
    final StoredMailFolder folder;

    Group(StoredMailFolder folder)
    {
	NullCheck.notNull(folder, "folder");
	this.folder = folder;
    }
}
