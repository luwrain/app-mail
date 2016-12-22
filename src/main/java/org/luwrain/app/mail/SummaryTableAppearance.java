
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

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
	    line = message.getState().toString() + " " + Utils.getDisplayedAddress(message.getFrom()) + ":" + message.getSubject() + " " + luwrain.i18n().getPastTimeBrief(message.getSentDate());
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

    @Override public String getCellText(TableArea.Model model,
					int col,
					int row)
    {
	if (model == null)
	    return "#NO MODEL#";
	final Object cell = model.getCell(col, row);
	return cell != null?cell.toString():"";
    }

    @Override public String getRowPrefix(TableArea.Model model, int index)
    {
	return "  ";//FIXME:
    }

    @Override public int getColWidth(TableArea.Model model, int  colIndex)
    {
	return 10;
    }
}
