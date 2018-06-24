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
package games.stendhal.server.maps.gdansk.forest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class Skille100NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone);
	}

	private void buildMineArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Altharis") {

			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(87, 84));
				nodes.add(new Node(89, 84));
				nodes.add(new Node(89, 85));
				nodes.add(new Node(97, 85));
				nodes.add(new Node(97, 90));
				nodes.add(new Node(107, 90));
				nodes.add(new Node(107, 96));
				nodes.add(new Node(103, 96));
				nodes.add(new Node(103, 88));
				setPath(new FixedPath(nodes, true));
			}
			
			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Witaj! Jestem tutaj aby #nauczyć Cię czegoś o walce z potworami!";

						if (player.getLevel() < 100) {
							reply += " Jeszcze nie jesteś godzien! Osiągnij 100 poziom!";
						} else {
							reply += " Jesteś godzień przyjąć moje nauki.";
						}
						raiser.say(reply);
					}
				});

				addReply("nauczyć",
						"Gdy osiągniesz 100 poziom nauczę Cię lepiej walczyć z potworami.");
				addGoodbye();
			}
		};


		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("AltharisReward")
						&& (player.getLevel() >= 100)) {
					player.setQuest("AltharisReward", "done");

					player.setAtkXP(180000 + player.getAtkXP());
					player.setDefXP(360000 + player.getDefXP());
					player.addXP(20000);

					player.incAtkXP();
					player.incDefXP();
				}

				if (!player.hasQuest("AltharisFirstChat")) {
					player.setQuest("AltharisFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
				
			}
			
		});

		npc.setEntityClass("blackwizardpriestnpc");
		npc.setPosition(87, 84);
		npc.initHP(85);
		zone.add(npc);
	}
}
