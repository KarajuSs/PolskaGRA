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
package games.stendhal.server.maps.nalwor.basement;

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
 * Inside Nalwor Inn basement .
 */

public class ArcheryDealerNPC implements ZoneConfigurator  {

	private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Merenwen") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10,5));
				nodes.add(new Node(16,5));
				nodes.add(new Node(16,6));
				nodes.add(new Node(10,6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Miłe spotkanie nieznajomy.");
				addJob("Skupuję łucznicze wyposażenie dla naszej wioski.");
				addHelp("Nie mogę zaoferować Tobie pomocy. Przykro mi.");
				addOffer("Spójrz na tablicę i sprawdź ceny.");
				addQuest("Nie mam dla Ciebie zadania.");
				addGoodbye("Bądź szczęśliwy. Dowidzenia.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyarcherstuff")), false);			    
			}};
			npc.setPosition(10, 5);
			npc.setDescription("Oto piękna czarodziejka elfów Merenwen. Skupuje przedmioty związane z łucznictwem.");
			npc.setEntityClass("mageelfnpc");
			zone.add(npc);		
   	}
}