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
package games.stendhal.server.maps.kalavan.castle;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a mad scientist NPC who takes your silk glands makes thread, then gives them to another NPC.
 *
 * @author kymara with modifications by tigertoes
 */
public class MadScientist1NPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Vincento Price") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			    protected void createDialog() {
				addHelp("Ha ha ha ha!");
				addOffer("Mówię to tylko dlatego, bo widzę, że potrzebujesz tego. Zrobię #40 szpulek #przędzy jedwabnej dla Ciebie. Powiedz tylko #zrób.");
				addQuest("Ha! Widzę, że potrzebujesz 40 szpulek #przędzy jedwabnej! Powiedz tylko #zrób. Mogę ją zrobić. Jeżeli będę miał na to ochotę ...");
				addJob("Jak to wygląda?");
				addGoodbye("Ta ta!");
				// remaining behaviour defined in maps.quests.MithrilCloak
	 	     }

		};

		npc.setDescription("Oto nieco dziwna osoba. Może nie powinieneś zawracać jej głowy?");
		npc.setEntityClass("madscientistnpc");
		npc.setPosition(18, 84);
		npc.initHP(100);
		zone.add(npc);
	}
}
