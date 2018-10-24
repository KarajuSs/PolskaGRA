/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ChangePlayerOutfitAndPreserveTempAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerIsWearingOutfitCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.SystemPropertyCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Balloon for Bobby
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Bobby (the boy in fado city)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Mine town weeks must be on for the quest to work</li>
 * <li>If you have a balloon, Bobby asks you if he can have it</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>200 XP</li>
 * <li>50 Karma</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Infinite, but only valid during mine town weeks </li>
 * </ul>
 */

public class BalloonForBobby extends AbstractQuest {

	public static final String QUEST_SLOT = "balloon_bobby";
	// List of outfits which are balloons
	private static final Outfit[] balloonList = new Outfit[4];

	private final String NPCName = "Bobby";

	@Override
	public void addToWorld() {
		fillQuestInfo("Balonik Bobbiego",
				"Młody chłopiec Bobby w Fado wpatruje się w niebo, szukając balonów. On je kocha i chce mieć jednego dla siebie.",
				true);
		prepareBalloonList();
		prepareRequestQuestStep();
		prepareGreetWithBalloonStep();
		prepareAttendingWithBalloonStep();
		prepareQuestItemQuestionStep();
	}

	// Load the different outfits into the list
	public void prepareBalloonList() {
		for (int i = 0; i < 4; i++) {
			balloonList[i] = new Outfit(i+1, null, null, null, null);
		}
	}

	private void prepareRequestQuestStep() {
		SpeakerNPC npc = npcs.get(NPCName);

		// Player asks Bobby for "quest".
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Czy mógłbyś zdobyć dla mnie #'balonik'? Nim zaczną się dni miasta "
						+ ", bo wtedy sam będę mógł zdobyć :)",
				null);

		// Player asks for quest after quest is started.
		npc.add(ConversationStates.ANY,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks"
						+ ", bo wtedy sam będę mógł zdobyć :)",
				null);

		// Player agrees to get a balloon.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Yay!",
				new SetQuestAction(QUEST_SLOT, 0, "start"));

		// Player refuses to get a balloon.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Aww. :'(",
				new SetQuestAction(QUEST_SLOT, 0, "rejected"));

		// Player asks about "balloon".
		npc.add(ConversationStates.ANY,
				Arrays.asList("balloon", "balonik"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Pewnego dnia będę miał wystarczająco dużo baloników, żeby odlecieć!",
				null);
	}

	// If the player has a balloon (and it is mine town weeks),
	// ask if Bobby can have it
	private void prepareGreetWithBalloonStep() {

		// get a reference to Bobby
		SpeakerNPC npc = npcs.get(NPCName);

		// Add conditions for all 4 different kinds of balloons
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new OrCondition(
								new PlayerIsWearingOutfitCondition(balloonList[0]),
								new PlayerIsWearingOutfitCondition(balloonList[1]),
								new PlayerIsWearingOutfitCondition(balloonList[2]),
								new PlayerIsWearingOutfitCondition(balloonList[3]))),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Cześć czy ten balonik jest dla mnie?",
				null);
	}

	// If the player has a balloon but refused to give it to booby
	// after him greeting, he now has another chance.
	// (Unless it's not mine town week)
	private void prepareAttendingWithBalloonStep() {

		SpeakerNPC npc = npcs.get(NPCName);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new OrCondition(
								new PlayerIsWearingOutfitCondition(balloonList[0]),
								new PlayerIsWearingOutfitCondition(balloonList[1]),
								new PlayerIsWearingOutfitCondition(balloonList[2]),
								new PlayerIsWearingOutfitCondition(balloonList[3]))),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Czy ten balonik jest dla mnie?",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new NotCondition(
								new OrCondition(
										new PlayerIsWearingOutfitCondition(balloonList[0]),
										new PlayerIsWearingOutfitCondition(balloonList[1]),
										new PlayerIsWearingOutfitCondition(balloonList[2]),
										new PlayerIsWearingOutfitCondition(balloonList[3])))),
				ConversationStates.ATTENDING,
				"Nie masz dla mnie balonika :(",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new SystemPropertyCondition("stendhal.minetown"),
				ConversationStates.ATTENDING,
				"Chmury powiedziały mi, że dni miasta wciąż trwają -"
				+ " Mogę sam zdobyć balonik."
				+ " Wróć, gdy skończą się dni miasta :)",
				null);
	}

	// Let player decide if he wants to give the balloon to bobby
	private void prepareQuestItemQuestionStep() {

		SpeakerNPC npc = npcs.get(NPCName);

		// The player has a balloon but wants to keep it to himself
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new PlaySoundAction("pout-1"),
						new NPCEmoteAction("dąsy.", false))
				);


		// Rewards to give to the player if he gives Bobby the balloon
		// NOTE: Also changes the players outfit to get rid of the balloon
		List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new ChangePlayerOutfitAndPreserveTempAction(balloonList[0], false));
		reward.add(new ChangePlayerOutfitAndPreserveTempAction(balloonList[1], false));
		reward.add(new ChangePlayerOutfitAndPreserveTempAction(balloonList[2], false));
		reward.add(new ChangePlayerOutfitAndPreserveTempAction(balloonList[3], false));
		reward.add(new IncreaseXPAction(200));
		reward.add(new IncreaseKarmaAction(50));
		reward.add(new SetQuestAction(QUEST_SLOT,0,"done"));
		reward.add(new IncrementQuestAction(QUEST_SLOT,1,1));

		// The player has a balloon and gives it to Bobby
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Hurra! Leć baloniku! Leć!",
				new MultipleActions(reward));

	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			final List<String> questInfo = Arrays.asList(player.getQuest(QUEST_SLOT).split(";"));
			final String questState = questInfo.get(0);
			int completedCount = 0;
 			if (questInfo.size() > 1) {
				completedCount = Integer.parseInt(questInfo.get(1));
			}
 			if (questState.equals("rejected")) {
				res.add("Nienawidzę balonów.");
			} else if (questState.equals("start")) {
				res.add("Kocham baloniki! Pomogę chłopcowi o imieniu " + NPCName + ", aby zdobył chociaż jeden.");
			} else if (questState.equals("done")) {
				String balloon = "balonik";
				if (completedCount > 1 || completedCount > 20 && completedCount < 25) {
					balloon = balloon + "i";
				} else {
					balloon = balloon + "ów";
				}
 				res.add("Znalazłem i dałem ładne " + Integer.toString(completedCount) + " " + balloon + " dla " + NPCName + ".");
			}
		}
 		return res;
	}

	@Override
	public String getName() {
		return "BalloonForBobby";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return NPCName;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return true;
	}
}