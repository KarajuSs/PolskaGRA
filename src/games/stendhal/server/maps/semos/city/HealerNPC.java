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
package games.stendhal.server.maps.semos.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A young lady (original name: Carmen) who heals players without charge. 
 */
public class HealerNPC implements ZoneConfigurator {
	
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Carmen") {
			@Override
			public void createDialog() {
				addGreeting("Halo jak mogę ci #pomóc");
				addJob("Moja niezwykła moc pomaga mi uleczyć rany. Sprzedaję także lecznicze mikstury i antidotum.");
				addHelp("Mogę Cię za darmo uleczyć. Powiedz tylko #ulecz lub możesz wziąć przygotowane przeze mnie lekarstwo ze sobą na podróż. Jeśli jesteś ciekawy jaka jest całą moja #oferta, zapytaj mnie.");
				addEmotionReply("hugs", "hugs");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 46));
				nodes.add(new Node(18, 46));
				setPath(new FixedPath(nodes, true));
			}
		};
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("healing")));
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(5, 46);
		npc.setDescription("Widzisz przyjazną Carmen. Wygląda ona na kogoś, kogo możesz poprosić o pomoc.");
		npc.setEntityClass("welcomernpc");
		npc.setSounds(Arrays.asList("giggle-1", "giggle-2"));
		zone.add(npc);
	}

}