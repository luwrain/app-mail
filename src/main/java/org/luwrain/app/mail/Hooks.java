
package org.luwrain.app.mail;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.script.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.script.*;

final class Hooks
{
    private final Luwrain luwrain;

    Hooks(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    Object[] organizeSummary(StoredMailMessage[] messages)
    {
	NullCheck.notNullItems(messages, "messages");
	final MessageHookObject[] hookObjs = new MessageHookObject[messages.length];
	for(int i = 0;i < messages.length;i++)
	    hookObjs[i] = new MessageHookObject(messages[i]);
	final Object[] args = new Object[]{ScriptUtils.createReadOnlyArray(hookObjs)};
	final AtomicReference res = new AtomicReference();
	luwrain.xRunHooks("luwrain.mail.summary.organize", (hook)->{
		final Object result = hook.run(args);
		if (result == null)
		return Luwrain.HookResult.CONTINUE;
		res.set(result);
		return Luwrain.HookResult.BREAK;
	    });
	if (res.get() == null)
	    return new Object[0];
	final List array = ScriptUtils.getArray(res.get());
	if (array == null)
	    return new Object[0];
	return array.toArray(new Object[array.size()]);
    }

}
