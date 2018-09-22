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
package games.stendhal.server.maps.krakow.blacksmith;

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
 * Build a NPC
 *
 * @author KarajuSs
 */
public class SamsonNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Samson") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 89));
				nodes.add(new Node(17, 90));
				nodes.add(new Node(20, 90));
				nodes.add(new Node(20, 89));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w mej kuźni, wojowniku!");
				addJob("Specjalizuje się w przetapianiu miedzi, ale również możesz o ode mnie otrzymać dobry sprzęt!");
				addOffer("Sprzedaję: toporek 25, topór jednoręczny 35, topór 50, pyrlik 90, pordzewiała kosa 210, misa do płukania złota 270.");
				// 3x copper ore + 2x wood = 1x copper
				// czas: 7 min na szt.
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellsamson")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Oto Samson. Jest miejscowym kowalem i specjalizuje się w przetapianiu miedzi. Dodatkowo prowadzi też sprzedaż potrzebnych do pracy narzędzi.");
		npc.setEntityClass("blacksmithnpc");
		npc.setPosition(20, 89);
		npc.initHP(100);
		zone.add(npc);
	}
}
