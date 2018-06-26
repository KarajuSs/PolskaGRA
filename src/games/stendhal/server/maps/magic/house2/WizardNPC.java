/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.magic.house2;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Builds a wizard npc, an expert in textiles.
 *
 * @author kymara 
 */
public class WizardNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Whiggins") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			    protected void createDialog() {
				addGreeting("Gorąco zapraszam");
				addHelp("Jeżeli potrzebujesz zwoi to Erodel Bmud posiada szeroki asortyment.");
				addOffer("Nic tutaj nie sprzedaję.");
				addJob("Utrzymuje ten dom i obserwuje wróżki.");
				addGoodbye("Do następnego razu.");
				// remaining behaviour defined in maps.quests.MithrilCloak
	 	     }
		    
		};

		npc.setDescription("Oto Whiggins. Wygląda na spokojnego i szczęśliwego.");
		npc.setEntityClass("mithrilforgernpc");
		npc.setPosition(14, 14);
		npc.initHP(100);
		zone.add(npc);
	}
}
