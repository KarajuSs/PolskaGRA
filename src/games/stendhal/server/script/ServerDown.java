/* $Id$ */
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
package games.stendhal.server.script;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * goes into or comes out of server down mode
 *
 * @author hendrik
 */
public class ServerDown extends ScriptImpl {
	private static final String ZONE_NAME = "int_abstract_server_down";

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 1) {
			admin.sendPrivateText("/script ServerDown.class {true|false}");
			return;
		}

		boolean enable = Boolean.parseBoolean(args.get(0));
		if (enable) {
			start();
		} else {
			stop();
		}
	}

	/**
	 * goes into server down mode
	 */
	private void start() {

		// force logins to server down zone
		StendhalRPZone zone = StendhalRPWorld.get().getZone(ZONE_NAME);
		System.setProperty("stendhal.forcezone", zone.getName());

		// remove portal
		for (RPObject object : zone) {
			if (object instanceof Portal && ((Portal) object).getDestinationReference() != null) {
				zone.remove(object);
				break;
			}
		}
		// TODO: add NPC
		createNPC(zone);
	}

	/**
	 * comes out of server down mode
	 */
	private void stop() {
		// don't force login to server down zone anymore
		StendhalRPZone zone = StendhalRPWorld.get().getZone(ZONE_NAME);
		System.getProperties().remove("stendhal.forcezone");

		// add portal in int_abstract_server_down going back to semos town hall
		// Note: This portal is defined in misc.xml, too.
		Portal portal = new Portal();
		portal.setPosition(15, 28);
		portal.setDestination("0_semos_city", "townhall_entrance");
		zone.add(portal);

		zone.remove(NPCList.get().get("Megan"));
	}

	/**
	 * creates an NPC
	 * @param zone
	 */
	private void createNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Megan") {

			@Override
			protected void createPath() {
				setPath(new FixedPath(Arrays.asList(
					new Node(25, 13),
					new Node(26, 13),
					new Node(26, 8),
					new Node(22, 8),
					new Node(22, 6),
					new Node(8, 6),
					new Node(8, 14),
					new Node(12, 14),
					new Node(12, 22),
					new Node(25, 22)), true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Oh witaj nie mam w tym #miejscu zbyt wielu zwiedzających.");
				addJob("Oh nic takiego. Patrzę w przestrzeń i czas.");
				addHelp("Dziękuję za twoją ofertę, ale teraz nie mam nic dla ciebie. Poczekaj i odpocznij");
				addReply(Arrays.asList("place", "miejscu"), "To jest... Pomyśl o tym jak o teatrze. Przestrzeń i czas są poza rzeczywistością.");
				addReply(Arrays.asList("reality", "rzeczywistością", "rzeczywistość"), "Aby zabrać ciebie w miejsce poza rzeczywistością muszę pozbyć się braku wiary.");
				addReply("polskaonline", "Moce, które aktualnie przywracają rzeczywistość PolskaOnLine. Poczekaj i zrelaksuj się.");
				
				addGoodbye();
			}
		};

		// TODO: different sprite
		npc.setEntityClass("timekeepernpc");
		npc.setPosition(25, 21);
		npc.setDirection(Direction.UP);
		npc.initHP(100);
		npc.setDescription("Oto Megan. Pilnuje przestrzeni i czasu.");
		zone.add(npc);
	}
}
