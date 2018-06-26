/***************************************************************************
 *                     (C) Copyright 2017 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.wall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Soldiers on the wall
 *
 * @author snowflake
 * @author hendrik
 */
public class WallSoldier1NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAdosWallSoldier(zone);
	}

	/**
	 * Creatures a soldier on the city wall
	 *
	 * @param zone StendhalRPZone
	 */
	private void buildAdosWallSoldier(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Galercus") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(79,  1));
				path.add(new Node(79, 17));
				path.add(new Node(76, 17));
				path.add(new Node(76,  1));
				setPath(new FixedPath(path, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Aktualnie jestem na służbie. Nie mam czasu na jakiekolwiek rozmowy.");
				addJob("Myślałem, że to jest oczywiste. Jestem strażnikiem i chronię mury tego miasta.");
				addHelp("Czy ja wyglądam jak przewodnik? Lepiej pójdź porozmawiać z Juliusem, on bardzo lubi pomagać innym ludziom. Strzeże głownego wejścia do miasta Ados, powinieneś go tam znaleźć.");
				// addQuest("I don't have any tasks for you. But I heard that Vicendus is always looking for volunteers to do his dirty work.");
				addGoodbye("Miłego dnia.");
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(79,  1);
		npc.initHP(100);
		npc.setDescription("Oto Galercus, żołnierz, który strzeże mury miasta Ados.");
		zone.add(npc);
	}
}
