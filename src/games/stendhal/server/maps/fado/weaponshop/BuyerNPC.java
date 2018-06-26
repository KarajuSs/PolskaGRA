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
package games.stendhal.server.maps.fado.weaponshop;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to buy previously un bought weapons.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Yorphin Baos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 5));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(7, 3));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(4, 5));
				nodes.add(new Node(4, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w moim sklepie.");
				addJob("#Handluję bronią. Sprzedaję tylko tutejszym z Fado, ale mogę od Ciebie kupić.");
				addHelp("#Pohandluję rzadką bronią. Tutaj znajdziesz Ognira, który wyrabia pierścionki, a także skupuje dziwne kamienie.");
				addOffer("Spójrz na tablicę na ścianie, aby zobaczyć co skupuję.");
				addQuest("Dziękuję, ale niczego nie potrzebuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrare2")), false); 
				addGoodbye("Do zobaczenie wkrótce.");
			}
		};

		npc.setDescription("Oto Yorphin Baos właściciel sklepu.");
		npc.setEntityClass("weaponsellernpc");
		npc.setPosition(4, 5);
		npc.initHP(100);
		zone.add(npc);
	}
}
