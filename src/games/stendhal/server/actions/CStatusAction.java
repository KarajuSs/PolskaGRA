/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.CID;
import static games.stendhal.common.constants.Actions.CSTATUS;
import static games.stendhal.common.constants.Actions.ID;
import games.stendhal.common.NotificationType;
import games.stendhal.common.Version;
import games.stendhal.server.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

import marauroa.common.game.RPAction;

/**
 * handles CID actions.
 */
public class CStatusAction implements ActionListener {

	/** Key is ID, value contains list of names */
	public static final Map<String, String> idList = new HashMap<String, String>();

	/** Key is name, value is ID */
	public static final Map<String, String> nameList = new HashMap<String, String>();

	/** registers the action */
	public static void register() {
		CommandCenter.register(CID, new CStatusAction());
		CommandCenter.register(CSTATUS, new CStatusAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String pName = player.getName();
		if (action.has(CID) || action.has(ID)) {

			String cid = action.get(CID);
			if (cid == null) {
				cid = action.get(ID);
			}

			//add to idList
			if (idList.containsKey(cid)) {
				if(!idList.get(cid).contains("," + pName + ",")) {
					final String tempid = idList.get(cid) + pName + ",";
					idList.put(cid, tempid);
				}
			} else {
				idList.put(cid, "," + pName + ",");
			}

			//add to nameList
			nameList.put(pName, cid);
		}

		if (action.has("version")) {
			String clientVersion = action.get("version");
			player.setClientVersion(clientVersion);
			String serverVersion = Version.VERSION;
			if (!Version.checkCompatibility(serverVersion, clientVersion)) {
				if (serverVersion.compareTo(clientVersion) < 0) {
					player.sendPrivateText(NotificationType.ERROR,
						"Mogą wystąpić problemy z kompatybilnością ponieważ serwer jest przestażały.");
				} else {
					player.sendPrivateText(NotificationType.ERROR,
						"Twój klient może funkcjonować nieprawidłowo,\nponieważ serwer jest w wersji "
								+ serverVersion
								+ ", a twój klient jest w wersji "
								+ clientVersion
								+ ".\nMożesz pobrać najnowszą wersję z http://polskagra.org ");
				}
			}
		}

	}
}
