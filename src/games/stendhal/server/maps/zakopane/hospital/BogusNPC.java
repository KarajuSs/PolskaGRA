/* $Id: BogusNPC.java,v 1.7 2012/09/22 02:28:01 Legolas Exp $ */
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
// Base on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
package games.stendhal.server.maps.zakopane.hospital;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class BogusNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Boguś") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 4));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(4, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj łowco.");
				addJob("Poszukuję skór zwierząt. Może masz jakieś ze sobą, które mógłbyś mi #zaoferować.");
				addHelp("Skupuję skóry zwierząt, jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Wszystko czym handluję znajdziesz w tych książkach.");
				addQuest("O, dziękuję, ale niczego już nie potrzebuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyskin")), false);
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellskin")), false);
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto Boguś wyglądający na uczciwego.");
		npc.setEntityClass("npcjuhasboguslaw");
		npc.setPosition(4, 4);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(9, 5);
		book.setText(" -- Skup -- \n piórko\t 3\n skóra lwa\t 1500\n skóra tygrysa\t 1000\n skóra białego tygrysa\t 250\n skóra zielonego smoka\t 2600\n skóra niebieskiego smoka\t 2600\n"+
		" skóra czerwonego smoka\t 2600\n skóra złotego smoka\t 2800\n skóra czarnego smoka\t 3300\n skóra xenocium\t 450");
		book.setEntityClass("book_red");
		book.setResistance(10);
		zone.add(book);

		final Sign book2 = new Sign();
		book2.setPosition(7, 5);
		book2.setText(" -- Sprzedaż -- \n buteleczka\t 5\n butelka\t 7\n krótki miecz 500\n topór\t 800\n kosa\t 2000");
		book2.setEntityClass("book_blue");
		book2.setResistance(10);
		zone.add(book2);
	}
}
