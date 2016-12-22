
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.util.*;
import org.luwrain.network.*;

class Utils 
{
    static String getDisplayedAddress(String addr)
    {
	NullCheck.notNull(addr, "addr");
	if (addr.trim().isEmpty())
	    return addr;
	try {
	    final javax.mail.internet.InternetAddress inetAddr = new javax.mail.internet.InternetAddress(addr, false);
	    final String personal = inetAddr.getPersonal();
	    if (personal == null || personal.trim().isEmpty())
		return addr;
	    //	System.out.println(personal);
	    return personal;
	}
	catch (javax.mail.internet.AddressException e)
	{
	    e.printStackTrace();
	    return addr;
	}
    }

    static String getReplyTo(byte[] bytes) throws PimException, java.io.IOException
    {
	final MailUtils utils = new MailUtils(bytes);
	final String[] res = utils.getReplyTo(true);
	if (res == null || res.length < 1)
	    return "";
	return res[0];
    }
}
