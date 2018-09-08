/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.marketplace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds an NPC
 *
 * @author KarajuSs
 */
public class LajkonikNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

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
		final SpeakerNPC npc = new SpeakerNPC("Lajkonik") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(88, 44));
				nodes.add(new Node(84, 44));
				nodes.add(new Node(84, 52));
				nodes.add(new Node(84, 52));
				nodes.add(new Node(88, 52));
				nodes.add(new Node(88, 48));
				nodes.add(new Node(101, 48));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jeżeli chcesz mogę ci opowiedzieć #'historię' z roku #'1287'.");
				addOffer("Mam do zaoferowania #'długi łuk' oraz #'wzmocnione drewniane strzały'.");
				// ceny: długi łuk - 1200; wzmocniona drewniana strzała - 4/5/6
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("selllajkonik")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Oto Lajkonik. Jest jednym z symboli miasta Krakowa. Jeżeli go spytasz opowie ci historię wydarzenia z 1287 roku.");
		npc.setEntityClass("noimagenpc"); // npclajkonik
		npc.setPosition(88, 45);
		npc.initHP(100);
		zone.add(npc);
	}
}
