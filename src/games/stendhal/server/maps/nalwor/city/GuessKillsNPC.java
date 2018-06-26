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
package games.stendhal.server.maps.nalwor.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import java.util.Arrays;

import java.util.Map;

/**
 * QUEST: The Guessing Game
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Crearid, an old lady found in Nalwor city</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Crearid asks if you want to play a game</li>
 * <li>She picks a random creature you have killed and asks you to guess how
 * many of those you killed</li>
 * <li>You get three guesses and get rewarded if your guess exactly matches the number
 * or a lower reward if your guess is close to the correct number</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Weekly</li>
 * </ul>
 */
public class GuessKillsNPC implements ZoneConfigurator {

    /**
     * Configure a zone.
     *
     * @param	zone		The zone to be configured.
     * @param	attributes	Configuration attributes.
     */
    @Override
    public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
        buildNPC(zone, attributes);
    }

    /**
     * Creates the NPC and sets default responses, path and other attributes
     *
     * @param zone The zone to add the NPC to.
     * @param attributes
     */
    private void buildNPC(final StendhalRPZone zone, final Map<String, String> attributes) {
        final SpeakerNPC npc = new SpeakerNPC("Crearid") {

            @Override
            protected void createPath() {
                setPath(new FixedPath(Arrays.asList(
                            new Node(29, 84),
                            new Node(22, 84),
                            new Node(17, 79),
                            new Node(18, 69),
                            new Node(22, 63),
                            new Node(29, 63),
                            new Node(29, 72),
                            new Node(39, 72),
                            new Node(39, 84)),
                        true));
            }

            @Override
            protected void createDialog() {
                addGreeting("Pozdrawiam");
                addJob("Jestem tylko starą kobietą obserwującą wszystkich podczas spaceru.");
                addHelp("Nie wiem jak tobie pomóc. Od paru dni lubię #grać w #gry.");
                addOffer("Obawiam się, że nie mam nic do zaoferowania.");
                addReply(ConversationPhrases.QUEST_MESSAGES, "Na razie nic nie potrzebuję, ale okazjonalnie lubię #grać w #gry.");

                //play and games reply is in the quest class: GuessKills

                addGoodbye("Dowidzenia słonko.");
            }
        };

        npc.setEntityClass("granmanpc");
        npc.setDescription("Oto Crearid bardzo spostrzegawcza starsza pani.");
        npc.setPosition(30, 83);
        npc.initHP(100);

        zone.add(npc);
    }
}
