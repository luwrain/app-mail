
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class SummaryGroup
{
    final String title;
    final StoredMailMessage[] messages;

    SummaryGroup(String title, StoredMailMessage[] messages)
    {
	NullCheck.notNull(title, "title");
	NullCheck.notNullItems(messages, "messages");
	this.title = title;
	this.messages = messages;
    }

    @Override public String toString()
    {
	return title;
    }

    }
