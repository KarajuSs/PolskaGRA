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
package games.stendhal.server.maps.koscielisko.cave;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

public class Skille50NPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Choros") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Witaj! Jestem tutaj aby #nauczyć Cię czegoś o walce z potworami!";

						if (player.getLevel() < 50) {
							reply += " Jeszcze nie jesteś godzien! Osiągnij 50 poziom!";
						} else {
							reply += " Jesteś godzień przyjąć moje nauki.";
						}
						raiser.say(reply);
					}
				});

				addReply("nauczyć",
						"Gdy osiągniesz 50 poziom nauczę Cię lepiej walczyć z potworami.");
				addGoodbye();
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};


		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("ChorosReward")
						&& (player.getLevel() >= 50)) {
					player.setQuest("ChorosReward", "done");

					player.setAtkXP(45000 + player.getAtkXP());
					player.setDefXP(90000 + player.getDefXP());
					player.addXP(20000);

					player.incAtkXP();
					player.incDefXP();
				}

				if (!player.hasQuest("ChorosFirstChat")) {
					player.setQuest("ChorosFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
				
			}
			
		});

		npc.setEntityClass("blackwizardpriestnpc");
		npc.setDescription("Oto Choros. Jest Wysokim Kapłanem, który może Cię czegoś nauczyć.");
		npc.setPosition(38, 3);
		npc.setDirection(Direction.DOWN);
		npc.initHP(85);
		zone.add(npc);
	}
}
