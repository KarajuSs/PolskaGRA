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
package games.stendhal.server.maps.tatry.kuznice.chapel;

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
import games.stendhal.server.entity.npc.behaviour.adder.FreeHealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

public class RadomilNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Radomil") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 2));
				nodes.add(new Node(14, 2));
				nodes.add(new Node(14, 7));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(6, 8));
				nodes.add(new Node(6, 7));
				nodes.add(new Node(5, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuję się tą kapliczką oraz posiadam niezwykłą moc, która pomaga mi uleczyć rany.");
				addHelp("Mogę Cię #'uleczyć'.");
				new FreeHealerAdder().addHealer(this, 0);
				addOffer("Mogę Cię uleczyć. Powiedz tylko #'ulecz' oraz sprzedaję antidotum, mocne antidotum, eliksir, duży eliksir i wielki eliksir.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("eliksiry")));
				addGoodbye();
			}
		};

		npc.setEntityClass("npcwikary");
		npc.setPosition(5, 2);
		zone.add(npc);
	}
}