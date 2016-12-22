
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.network.*;

class SummaryTableAppearance implements TableArea.Appearance
{
    private final Luwrain luwrain;
    private final Strings strings;

    SummaryTableAppearance(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    @Override public void announceRow(TableArea.Model model,
				      int index, int flags)
    {
	NullCheck.notNull(model, "model");
	if (index < 0 || index >= model.getRowCount())
	    return;
	final Object obj = model.getRow(index);
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return;
	final StoredMailMessage message = (StoredMailMessage)obj;
	final String line;
	try {
	    final String prefix;
	    final MailMessage.State state = message.getState();
	    switch(state)
	    {
	    case READ:
		prefix = strings.readPrefix();
		break;
	    case MARKED:
		prefix = strings.markedPrefix();
		break;
	    case DELETED:
		prefix = strings.deletedPrefix();
		break;
	    default:
		prefix = "";
	    }
	    line = prefix + " " + MailUtils.extractNameFromAddr(message.getFrom()) + ":" + message.getSubject() + " " + luwrain.i18n().getPastTimeBrief(message.getSentDate());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return;
	}
	luwrain.playSound(Sounds.LIST_ITEM);
	luwrain.say(line);
    }

    @Override public int getInitialHotPointX(TableArea.Model model)
    {
	return 2;
    }

    @Override public String getCellText(TableArea.Model model, int col, int row)
    {
	NullCheck.notNull(model, "model");
	final Object cell = model.getCell(col, row);
	NullCheck.notNull(cell, "cell");
	return cell.toString();
    }

    @Override public String getRowPrefix(TableArea.Model model, int index)
    {
	return "  ";//FIXME:
    }

    @Override public int getColWidth(TableArea.Model model, int  colIndex)
    {
	return 15;
    }
}
