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
package games.stendhal.server.maps.krakow.tavern;

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
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds an NPC
 *
 * @author KarajuSs
 */
public class MerchandNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Merchand") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(x, y)); // CHWILOWO BRAK ROZPLANOWANIA
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, chciałbyś zobaczyć moje towary?");
				addJob("Zajmuję się handlem.");
				addOffer("Spójrz na moje książki, które położyłem na stoliku.");
				// kupno: płaszcz - 10; studded legs - 20; sztylecik - 25; katana - 50
				// sprzedaz: chain legs - 100; studded boots - 120; leather scale armor - 150; chain boots - 180; unicorn shield - 200; skull shield - 225; scimitar - 250; viking helmet - 300
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellmerchand")), false);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buymerchand")), false);
				addGoodbye("Dowidzenia. Mam nadzieję, że się jeszcze spotkamy!");
			}
		};

		npc.setDescription("Oto Merchand. Jest wędrownym handlarzem, który najczęściej lubi się zatrzymywać na dłużej w pokoiku gospody na Krakowskim Rynku.");
		npc.setEntityClass(""); // npchandlarz
		npc.setPosition(x, y); // CHWILOWO BRAK ROZPLANOWANIA
		npc.initHP(100);
		zone.add(npc);
	}
}
