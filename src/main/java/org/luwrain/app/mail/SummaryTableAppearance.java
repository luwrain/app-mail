
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class SummaryTableAppearance implements TableAppearance
{
    private Luwrain luwrain;
    private Strings strings;

    SummaryTableAppearance(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
    }

    @Override public void introduceRow(TableModel model,
				       int index,
				       int flags)
    {
	if (model == null || index >= model.getRowCount())
	    return;
	final Object obj = model.getRow(index);
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return;
	final StoredMailMessage message = (StoredMailMessage)obj;
	String line = "";
	try {
	    line = Base.getDisplaiedAddress(message.getFrom()) + " " + message.getSubject() + " " + strings.passedTimeBrief(message.getSentDate());
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    luwrain.say("#StorageError!#");
	    return;
	}
	luwrain.say(line);
    }

    @Override public int getInitialHotPointX(TableModel model)
    {
	return 2;
    }

    @Override public String getCellText(TableModel model,
					int col,
					int row)
    {
	if (model == null)
	    return "#NO MODEL#";
	final Object cell = model.getCell(col, row);
	return cell != null?cell.toString():"";
    }

    @Override public String getRowPrefix(TableModel model, int index)
    {
	return "  ";//FIXME:
    }

    @Override public int getColWidth(TableModel model, int  colIndex)
    {
	return 10;
    }
}
