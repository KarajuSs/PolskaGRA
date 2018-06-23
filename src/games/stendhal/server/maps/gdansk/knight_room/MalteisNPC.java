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
package games.stendhal.server.maps.pol.gdansk.knight_room;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MalteisNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Malteis") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 4));
				nodes.add(new Node(4, 4));
				nodes.add(new Node(4, 14));
				nodes.add(new Node(19, 14));
				nodes.add(new Node(19, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addOffer("Nie mam nic do zaoferowania, lecz możesz mi w czymś #pomóc.");
				addHelp("My, rycerze potrzebujemy pomocy w pewnej sprawie. Jeżeli chcesz się dowiedzieć więcej powiedz mi #zadanie.");
				addGoodbye("Dowidzenia. Niech światłość Ci sprzyja.");
			}
		};

		npc.setEntityClass("malteisnpc");
		npc.setPosition(11, 4);
		npc.initHP(100);
		npc.setDescription("Oto Malteis.");
		zone.add(npc);
	}
}
