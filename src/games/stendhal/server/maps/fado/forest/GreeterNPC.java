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
package games.stendhal.server.maps.fado.forest;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Greeter NPC.
 *
 * @author kymara
 */
public class GreeterNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Orchiwald") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 12));
				nodes.add(new Node(40, 12));
				nodes.add(new Node(40, 28));
				nodes.add(new Node(58, 28));
				nodes.add(new Node(58, 91));
				nodes.add(new Node(99, 91));
				nodes.add(new Node(99, 76));
				nodes.add(new Node(36, 76));
				nodes.add(new Node(36, 37));
				nodes.add(new Node(3, 37));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w skromnym domku albino elfów.");
				addJob("Przechodziłem tędy, wiesz albino elfy często wędrują. Prowadzimy #koczowniczy tryb życia.");
				addReply(Arrays.asList("koczowniczy", "normadic"), "Nie mamy stałego domu podróżujemy pomiędzy lasami i dolinami. Kiedy znajdujemy miejsce to osiedlamy się w nim. Lubimy to miejsce, ponieważ niedaleko są zabytkowe #kamienie.");
				addReply(Arrays.asList("kamienie", "stones"), "Posiadają właściwości mistyczne. Lubimy być w pobliżu nich, gdy zmienia się pora roku.");
				addHelp("Mógłbym sprzedać Ci zaczarowane zwoje, abyś mógł wrócić do Fado. Mam dojście do tanich zwojów.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("fadoscrolls")) {

					@Override
					public int getUnitPrice(final String item) {
						// Player gets 20 % rebate
						return (int) (0.80f * priceCalculator.calculatePrice(item, null));
					}
				});
				addQuest("Hojna oferta, ale niczego nie potrzebuję dziękuję.");
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto Orchiwald albino elf.");
		npc.setEntityClass("albinoelf2npc");
		npc.setPosition(3, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}
