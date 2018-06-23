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
package games.stendhal.server.maps.semos.storage;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HousewifeNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosStorageArea(zone);
	}

	private void buildSemosStorageArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Eonna") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12)); 
				nodes.add(new Node(6, 12));
				nodes.add(new Node(6, 13));
				nodes.add(new Node(15, 13));
				nodes.add(new Node(15, 9));
				nodes.add(new Node(10, 9));
				nodes.add(new Node(10, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody bohaterze.");
				addJob("Jestem gospodynią domową.");
				addHelp("Uwielbiam pomagać dla Landera. Jego kanapki są wspaniałe! Czy wiesz, że szuka pomocnika?");
				addGoodbye();
			}
		};

		npc.setEntityClass("welcomernpc");
		npc.setDescription("Oto Eonna. Jest wspaniałą gospodynią domową, która panicznie boi się szczurów!");
		npc.setPosition(4, 13);
		npc.initHP(100);
		zone.add(npc);
	}
}
