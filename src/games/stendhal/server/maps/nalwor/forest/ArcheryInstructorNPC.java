/***************************************************************************
 *                   (C) Copyright 2018 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.nalwor.forest;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.LogoutListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerStatLevelCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ShowItemListEvent;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.TimeUtil;

/**
 * TODO: create JUnit test
 * TODO: require assassins pass (same as used for assassins HQ)
 * TODO: only allow 10 players into training area at a time
 * TODO: notify players of fee before they pay
 */
public class ArcheryInstructorNPC implements ZoneConfigurator,LoginListener,LogoutListener {

	/** logger instance */
	private static Logger logger = Logger.getLogger(ArcheryInstructorNPC.class);

	/** quest/activity identifier */
	private static final String QUEST_SLOT = "archery_range";

	/** cost to use archery range */
	private static final int COST = 10000;

	/** capped range attack level */
	private static final int RATK_LIMIT = 80;

	/** time (in seconds) allowed for training session */
	private static final int TRAIN_TIME = 15 * MathHelper.SECONDS_IN_ONE_MINUTE;

	/** time (in seconds) remaining for this training session */
	private int TIME_REMAINING = 0;

	/** time player must wait to train again */
	private static final int COOLDOWN = 6 * MathHelper.MINUTES_IN_ONE_HOUR;

	/** archery range area */
	private final String archeryZone = "0_nalwor_forest_n";
	private final Rectangle2D archeryArea = new Rectangle(96, 96, 21, 12);

	/** NPC that manages archery area */
	private static final String npcName = "Chester";
	private SpeakerNPC npc;

	/** phrases used in conversations */
	private static final List<String> TRAIN_PHRASES = Arrays.asList("train", "training", "trenuj", "trenowanie", "trenować");
	private static final List<String> FEE_PHRASES = Arrays.asList("fee", "cost", "charge", "opłata", "cena", "koszt", "opłatę");

	/** quest states */
	private static final String STATE_ACTIVE = "training";
	private static final String STATE_DONE = "done";


	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		// set up the login/logout notifiers
		SingletonRepository.getLoginNotifier().addListener(this);
		SingletonRepository.getLogoutNotifier().addListener(this);

		buildNPC(zone);
		initShop(zone);
		initTraining();
		addToQuestSystem();
	}

	private void buildNPC(final StendhalRPZone zone) {
		npc = new SpeakerNPC(npcName) {
			@Override
			protected void createDialog() {
				addGreeting("To jest strzelnica zabójców. Lepiej uważaj na język, jeśli nie chcesz zostać zraniony.");
				addJob("Zarządzam tą tutaj strzelnicą. Należy ona do zabójców, więc nie wtykaj swojego nosa tam, gdzie nie trzeba.");
				addGoodbye("Możesz tutaj wracać kiedy będziesz miał trochę gotówki. Uprzejmość nie jest walutą.");
			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			public void say(final String text) {
				// don't turn toward player
				say(text, false);
			}
		};

		npc.setDescription("Widzisz mężczyznę, który wydaje się być utalentowanym zabójcą.");
		npc.setPosition(120, 100);
		npc.setEntityClass("rangernpc");
		zone.add(npc);
	}

	/**
	 * Adds bow & arrows for sale from NPC.
	 */
	private void initShop(final StendhalRPZone zone) {
		// override the default offer message
		npc.addOffer("Nie patrz się tak na mnie, wszyscy są głupcami!"
				+ " Sprawdź moje ceny łuków oraz strzał na tamtej tablicy."
				+ " Jeśli szukasz specjalnej okazji to jej tutaj nie znajdziesz!"
				+ " Znajdź sobie innego bezmyślnego frajera.");

		// prices are higher than those of other shops
		final Map<String, Integer> shop = new LinkedHashMap<>();
		shop.put("strzała", 4);
		shop.put("drewniany łuk", 600);
		shop.put("długi łuk", 1200);
		shop.put("łuk treningowy", 4500);

		new SellerAdder().addSeller(npc, new SellerBehaviour(shop), false);

		// a sign showing prices of items
		final ShopSign blackboard = new ShopSign("sellarcheryrange", "Sklep łuczniczy dla zabójców", "Sprzedawane są tu łuki i strzały:", true) {
			@Override
			public boolean onUsed(final RPEntity user) {
				List<Item> itemList = generateItemList(shop);
				ShowItemListEvent event = new ShowItemListEvent(this.title, this.caption, itemList);
				user.addEvent(event);
				user.notifyWorldAboutChanges();
				return true;
			}
		};
		blackboard.setEntityClass("blackboard");
		blackboard.setPosition(117, 101);
		zone.add(blackboard);
	}

	/**
	 * Initializes conversation & actions for archery training.
	 */
	private void initTraining() {
		npc.addQuest("Mogę Ci pozwolić tutaj #trenować twoje umiejętności dystansowe za drobną #'opłatą'.");
		npc.addHelp("Mogę Ci pozwolić tutaj #trenować twoje umiejętności dystansowe za drobną #'opłatą'.");
		npc.addReply(FEE_PHRASES, "Koszt #trenowania na tej strzelnicy to: " + Integer.toString(COST) + " money.");

		// player has never trained before
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new PlayerStatLevelCondition("ratk", "lteq", RATK_LIMIT)),
				ConversationStates.QUESTION_1,
				"Widzę, że nigdy wcześniej nie trenowałeś z nami. Czy chciałbyś spróbować?",
				null);

		// player returns after cooldown period is up
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DONE),
						new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN),
						new PlayerStatLevelCondition("ratk", "lt", RATK_LIMIT)),
				ConversationStates.QUESTION_1,
				"Czy chciałbyś z nami ponownie potrenować?",
				null);

		// player returns before cooldown period is up
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN)),
						new PlayerStatLevelCondition("ratk", "lt", RATK_LIMIT)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, COOLDOWN, "Nie możesz jeszcze trenować. Wróc za"));

		// player's RATK level is too high
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new PlayerStatLevelCondition("ratk", "gteq", RATK_LIMIT),
				ConversationStates.ATTENDING,
				"Jesteś już zbyt wyszkolony, by tu trenować. A teraz wynoś się ty leniwcu i walcz z potworami!",
				null);

		// player training state is active
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				"Wynoś się stąd! Zapłaciłeś już za sesję szkoleniową.",
				null);

		// player has enough money to begin training
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", COST),
				ConversationStates.IDLE,
				"Możesz trenować maksymalnie do " + Integer.toString(TRAIN_TIME / MathHelper.SECONDS_IN_ONE_MINUTE) + " minut. Więc dobrze wykorzystaj swój czas.",
				new MultipleActions(
						new DropItemAction("money", COST),
						new SetQuestAction(QUEST_SLOT, STATE_ACTIVE + ";" + Integer.toString(TRAIN_TIME)),
						new ArcheryRangeTimerAction()));

		// player does not have enough money to begin training
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("money", COST)),
				ConversationStates.ATTENDING,
				"Co to ma być? Nie masz nawet wystarczająco dużo pieniędzy na #'opłatę'. Odejdź z tobą!",
				null);

		// player does not want to train
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Aww, szkoda. Czy jest coś jeszcze, co mogę ci pomóc?",
				null);

		/* FIXME: How to get updated remaining time?
		// player asks how much time is left in training session
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("time", "czas"),
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, TRAIN_TIME, "Twój trening zakończy się za około"));
		*/
	}

	/**
	 * Makes visible in inspect command.
	 */
	private void addToQuestSystem() {
		SingletonRepository.getStendhalQuestSystem().loadQuest(new AbstractQuest() {

			@Override
			public List<String> getHistory(Player player) {
				return null;
			}

			@Override
			public String getSlotName() {
				return QUEST_SLOT;
			}

			@Override
			public void addToWorld() {
			}

			@Override
			public String getName() {
				return "ArcheryRange";
			}
		});
	}

	/**
	 * Actions to take when player logs in.
	 */
	@Override
	public void onLoggedIn(final Player player) {
		// don't allow players to login within archery range area boundaries
		if (player.isInArea(archeryZone, archeryArea)) {
			player.teleport(archeryZone, 118, 104, null, null);
		}

		// re-initialize turn notifier if player still has active training session
		final String sessionState = player.getQuest(QUEST_SLOT, 0);
		if (sessionState != null && sessionState.equals(STATE_ACTIVE)) {
			final String sessionTime = player.getQuest(QUEST_SLOT, 1);
			if (sessionTime != null) {
				// set remaining time from quest state
				TIME_REMAINING = Integer.parseInt(sessionTime);

				// re-create the timer/notifier
				new ArcheryRangeTimerAction().fire(player, null, null);
				return;
			}
		}

		// ensure time remaining value is reset to 0 if session info is not found
		if (TIME_REMAINING != 0) {
			logger.debug("Session time remaining was not \"0\" at login. Actual time remaining: " + Integer.toString(TIME_REMAINING));
		}
		TIME_REMAINING = 0;
	}

	/**
	 * Actions to take when player logs out.
	 */
	@Override
	public void onLoggedOut(Player player) {
		final String sessionState = player.getQuest(QUEST_SLOT, 0);
		if (sessionState != null && sessionState.equals(STATE_ACTIVE)) {
			// store training session information
			player.setQuest(QUEST_SLOT, 1, Integer.toString(TIME_REMAINING));
		}

		/* Stop running notifiers that might be left after the player
		 * logged out during archery range training session.
		 */
		SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
	}

	/**
	 * Teleports player out of archery range training area.
	 */
	private void endTrainingSession(final Player player) {
		if (player.get("zoneid").equals(archeryZone)) {
			npc.say("Twój trening się skończył " + player.getName() + ".");
		}
		if (player.isInArea(archeryZone, archeryArea)) {
			player.teleport(archeryZone, 118, 104, null, null);
		}

		player.setQuest(QUEST_SLOT, STATE_DONE + ";" + Long.toString(System.currentTimeMillis()));
	}


	/**
	 * Notifies player of time remaining for training & ends training session.
	 */
	private class Timer implements TurnListener {

		private final WeakReference<Player> timedPlayer;

		protected Timer(final Player player) {
			timedPlayer = new WeakReference<Player>(player);
			if (TIME_REMAINING == 0) {
				// beginning a new session
				TIME_REMAINING = TRAIN_TIME;
			}
		}

		@Override
		public void onTurnReached(int currentTurn) {
			final Player playerTemp = timedPlayer.get();

			if (playerTemp != null) {
				if (TIME_REMAINING > 0) {
					// notify players at 10 minute mark & every minute after 5 minute mark
					if (TIME_REMAINING == 10 * MathHelper.SECONDS_IN_ONE_MINUTE || TIME_REMAINING <= 5 * MathHelper.SECONDS_IN_ONE_MINUTE) {
						npc.say(playerTemp.getName() + ", pozostało Tobie " + TimeUtil.timeUntil(TIME_REMAINING) + ".");
					}
					TIME_REMAINING = TIME_REMAINING - 10 * 6;
					SingletonRepository.getTurnNotifier().notifyInTurns(10 * 3 * 6, this);
				} else {
					endTrainingSession(playerTemp);
				}
			}
		}

		@Override
		public int hashCode() {
			final Player player = timedPlayer.get();

			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((player == null) ? 0 : player.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			final Player player = timedPlayer.get();

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Timer other = (Timer) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (player == null) {
				if (other.timedPlayer.get() != null)
					return false;
			} else if (!player.equals(other.timedPlayer.get()))
				return false;
			return true;
		}

		private ArcheryInstructorNPC getOuterType() {
			return ArcheryInstructorNPC.this;
		}
	}


	/**
	 * Action that notifies
	 */
	private class ArcheryRangeTimerAction implements ChatAction {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			// remove any existing notifiers
			SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));

			// create the new notifier
			SingletonRepository.getTurnNotifier().notifyInTurns(0, new Timer(player));
		}
	}
}