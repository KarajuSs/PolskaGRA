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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.LastMinuteScroll;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Rainbow Beans
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Juhas, a dealer in bilet turystyczny
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The NPC sells bilet turystyczny to players above level 200</li>
 * <li>When used, rainbow beans teleport you to a dreamworld full of strange
 * sights, hallucinations and the creatures of your nightmares</li>
 * <li>You can remain there for up to 30 minutes</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>The desert land is very beautiful!</li>
 * <li>XP from creatures you kill there</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>No more than once every 2 days</li>
 * </ul>
 *
 * NOTES:
 * <ul>
 * <li>The area of the dreamworld will be a no teleport zone</li>
 * <li>You can exit via a portal if you want to exit before the 30 minutes is
 * up</li>
 * </ul>
 */
public class BiletTurystyczny extends AbstractQuest {

	private static final int REQUIRED_LEVEL = 100;

	private static final int REQUIRED_MONEY = 5000;

	private static final int REQUIRED_MINUTES = 60 * 24 * 2;

	private static final String QUEST_SLOT = "bilet_turystyczny";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Juhas");

		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.INFORMATION_1,
			"CIII! Jesteś ciekaw jakimi towarami #handluję.", null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),  
			ConversationStates.QUEST_OFFERED,
			"Wróciłeś po kolejny bilet turystyczny?", null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),  
			ConversationStates.ATTENDING, 
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Niestety kolejny bilet dostanę za co najmniej"));

		// player responds to word 'deal' - enough level
		npc.add(ConversationStates.INFORMATION_1, 
			Arrays.asList("deal", "handluję", "umowa", "biletem", "bilety"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT), 
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.QUEST_OFFERED, 
			"Sprzedaję #bilety turystyczne na pustynię. Możesz kupić, ale będzie Cię kosztować "
								+ REQUIRED_MONEY
								+ " money. Chcesz kupić?",
			null);
		
		// player responds to word 'deal' - low level
		npc.add(ConversationStates.INFORMATION_1, 
			Arrays.asList("deal", "handluję", "umowa", "biletem", "bilety"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT), 
					new LevelLessThanCondition(REQUIRED_LEVEL)),
			ConversationStates.ATTENDING, 
			"Nie jesteś gotowy na taką podróż. Wróć, gdy podrośniesz!",
			null);

		// player wants to take the beans but hasn't the money
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY)),
			ConversationStates.ATTENDING, 
			"Nie masz wystarczająco dużo pieniędzy. Wróć, gdy będziesz miał.",
			null);

		// player wants to take the beans
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, 
				new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY),
				ConversationStates.ATTENDING, 
				"W porządku oto twój bilet turystyczny. Gdy użyjesz to wrócisz za około 3 godziny. Jeśli będziesz chciał wcześniej wrócić to skorzystaj tam stan na herbie Zakopanego, który zabierze Ciebie z powrotem.",
				new MultipleActions(
						new DropItemAction("money", REQUIRED_MONEY),
						new EquipItemAction("bilet turystyczny", 1, true),
						// this is still complicated and could probably be split out further
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								if (player.hasQuest(QUEST_SLOT)) {
									final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
									if (tokens.length == 4) {
										// we stored an old time taken or set it to -1 (never taken), either way, remember this.
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;" + tokens[3]);
									} else {
										// it must have started with "done" (old quest slot status was done;timestamp), but now we store when the beans were taken. 
										// And they haven't taken beans since
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;-1");
								
									}
								} else {
									// first time they bought beans here
									player.setQuest(QUEST_SLOT, "bought;"
											+ System.currentTimeMillis() + ";taken;-1");
								
								}
							}
						}));

		// player is not willing to experiment
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"To nie jest dla każdego. Jeżeli chciałbyś coś to mów.",
			null);

		// player says 'deal' or asks about beans when NPC is ATTENDING, not
		// just in information state (like if they said no then changed mind and
		// are trying to get him to deal again)
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "ticket", "bilet turystyczny", "yes", "tak"),
			new LevelGreaterThanCondition(REQUIRED_LEVEL-1),
			ConversationStates.ATTENDING,
			"Już mówiliśmy o tym! Spróbuj innym razem.",
			null);
			
		// player says 'deal' or asks about beans when NPC is ATTENDING, not
		// just in information state (like if they said no then changed mind and
		// are trying to get him to deal again)
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "ticket", "bilet turystyczny", "yes", "tak"),
			new LevelLessThanCondition(REQUIRED_LEVEL),
			ConversationStates.ATTENDING, 
			"Nie jesteś wystarczająco przygotowany na taką podróż. Nie masz szans!",
			null);
	}

	@Override
	public void addToWorld() {
		/* login notifier to teleport away players logging into the dream world.
		 * there is a note in TimedTeleportScroll that it should be done there or its subclass.
		 */
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
				LastMinuteScroll scroll = (LastMinuteScroll) SingletonRepository.getEntityManager().getItem("bilet turystyczny");
				scroll.teleportBack(player);
			}

		});
		fillQuestInfo(
				"Bilet Turystyczny",
				"Bilet turystyczny wysyła na wycieczkę do obcej krainy pokrytej piaskiem.",
				false);
		step_1();

	}
	@Override
	public String getName() {
		return "BiletTurystyczny";
	}
	
	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		if(!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		String[] tokens = player.getQuest(QUEST_SLOT).split(";");
		if (tokens.length < 4) {
			return false;
		}
		return MathHelper.parseLongDefault(tokens[3],-1)>0;
	}
	
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();	
	}
	@Override
	public String getNPCName() {
		return "Juhas";
	}
	
	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
