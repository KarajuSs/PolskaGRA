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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * Food and drink seller,  Inside Semos Tavern - Level 0 (ground floor)
 * Sells the flask required for Tad's quest IntroducePlayers
 */
public class BarMaidNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMargaret(zone);
	}

	private void buildMargaret(final StendhalRPZone zone) {
		final SpeakerNPC margaret = new SpeakerNPC("Margaret") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 4));
				nodes.add(new Node(18, 4));
				nodes.add(new Node(18, 3));
				nodes.add(new Node(11, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addReply("flaszki", "Jeżeli chciałbyś kupić flaszę to powiedz mi: #buy #flasza lub zapytaj co mogłabym jeszcze #zaoferować.");
				addQuest("Oh miło, że pytasz. Niestety nic dla ciebie nie mam.");
				addJob("Jestem kelnerką w tej oberży. Sprzedajemy #flaszki , importowane i lokalne soki z chmielu oraz dobre jedzenie.");
				addHelp("Oberża ta jest na tyle duża, żeby tu odpocząć i spotkać nowych ludzi! Jeżeli chcesz poznać moją #ofertę, to powiedz mi o tym.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("food&drinks")));

				addGoodbye();
			}
		};

		//coupon for free beer
		
        margaret.add(ConversationStates.ATTENDING,
                (Arrays.asList("coupon", "coupons", "beer coupon", "free beer", "kupon", "kupony", "darmowy sok z chmielu")),
                new PlayerHasItemWithHimCondition("kupon"),
                ConversationStates.ATTENDING,
                "Oh widzę, że znalazłeś jeden z kuponów, które rozdałam jakiś czas temu. Przyjemnego kosztowania soku z chmielu!",
                new MultipleActions(new DropItemAction("kupon"),
                					new EquipItemAction("sok z chmielu"))
                );
        
        margaret.add(ConversationStates.ATTENDING,
        		(Arrays.asList("coupon", "coupons", "beer coupon", "free beer", "kupon", "kupony", "darmowy sok z chmielu")),
                new NotCondition(new PlayerHasItemWithHimCondition("kupon")),
                ConversationStates.ATTENDING,
                "Nie kłam. Nie masz kuponu ze sobą. Dziś trudno prowadzić tawernę. Nie oszukuj mnie!",
                null
                );
        
		margaret.setEntityClass("tavernbarmaidnpc");
		margaret.setDescription("Margaret wygląda na miłą osobę. Nie możesz jej pomóc, ale możesz coś od niej kupić.");
		margaret.setPosition(11, 4);
		margaret.initHP(100);
		margaret.setSounds(Arrays.asList("hiccup-1", "hiccup-2", "hiccup-3"));
		zone.add(margaret);
	}
}
