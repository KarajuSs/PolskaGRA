/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

public class ZagadkiBogumila extends AbstractQuest {
	private static final String QUEST_SLOT = "zagadki_bogumila";
	private Questions questions;

	private static final int REQUIRED_WAIT_DAYS = 1;

	private static final int xpreward = 5000;
	private static final int skillsreward = 2000;

	private static class Questions {
		private static Logger logger = Logger.getLogger(Questions.class);

		private static final String QUESTIONS_XML = "/data/conf/riddle_bogumil.xml";

		Map<String, Collection<String>> questionMap;

		public Questions() {
			questionMap = new HashMap<String, Collection<String>>();
			new QuestionLoader().load(questionMap);
		}

		/**
		 * Check if an answer mathces the riddle.
		 * 
		 * @param riddle The riddle to be answered
		 * @param answer The answer given by the player
		 * @return <code>true</code> iff the answer is correct
		 */
		public boolean matches(String question, Sentence sentence) {
			final Sentence answer = sentence.parseAsMatchingSource();

			for (String correct : questionMap.get(question)) {
				final Sentence expected = ConversationParser.parse(correct, new SimilarExprMatcher());
				if (answer.matchesFull(expected)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Get a random riddle.
		 * 
		 * @return A random ridde
		 */
		String getQuestion() {
			return Rand.rand(questionMap.keySet());
		}

		/**
		 * Loader for the riddles xml format.
		 */
		private static class QuestionLoader extends DefaultHandler {
			Map<String, Collection<String>> questions;
			String currentKey;
			String currentAnswer;

			public void load(Map<String, Collection<String>> questions) {
				this.questions = questions;

				InputStream in = getClass().getResourceAsStream(QUESTIONS_XML);

				if (in == null) {
					logger.error("Failed to load " + QUESTIONS_XML);
					return;
				}

				SAXParser parser;

				// Use the default (non-validating) parser
				final SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					parser = factory.newSAXParser();
					parser.parse(in, this);
				} catch (final Exception e) {
					logger.error(e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}

			private void addAnswer(String question, String answer) {
				Collection<String> answers = questions.get(question);
				if (answers == null) {
					answers = new LinkedList<String>();
					questions.put(question, answers);
				}
				answers.add(answer);
			}

			@Override
			public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) {
				if (qName.equals("entry")) {
					final String key = attrs.getValue("key");
					if (key == null) {
						logger.warn("An entry without a key");
					} else {
						currentKey = key;
					}
				} else if (!(qName.equals("questions") || qName.equals("comment"))) {
					currentKey = null;
					logger.warn("Unknown XML element: " + qName);
				}
			}

			@Override
			public void endElement(final String uri, final String lName, final String qName) {
				if (qName.equals("entry")) {
					if ((currentKey != null) && (currentAnswer != null)) {
						addAnswer(currentKey, currentAnswer);
					} else {
						logger.error("Error reading questions, Key=" + currentKey + " " + " Answer=" + currentAnswer);
					}
				} else {
					currentKey = null;
					currentAnswer = null;
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				if (currentKey != null) {
					currentAnswer = new String(ch, start, length);
				} else {
					currentAnswer = null;
				}
			}
		}
	}

	public ZagadkiBogumila() {
		questions = new Questions();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void setQuestion() {
		final SpeakerNPC npc = npcs.get("Bogumil");

		// player has no unsolved riddle active
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zagadki", "zagadka", "riddles", "riddle"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String question = questions.getQuestion();
						npc.say("Oto zagadka: " + question);
						player.setQuest(QUEST_SLOT, question);
					}
				});
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zagadki", "zagadka", "riddles", "riddle"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "done;"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.getQuest(QUEST_SLOT).startsWith("done;")) {
						final String[] waittokens = player.getQuest(QUEST_SLOT).split(";");
						final long waitdelay = REQUIRED_WAIT_DAYS;
						final long waittimeRemaining = (Long.parseLong(waittokens[1]) + waitdelay) - System.currentTimeMillis();
						if (waittimeRemaining > 0L) {
						npc.say("Zagadkę możesz rozwiązać tylko raz dziennie. Wróc za "
							+ TimeUtil.approxTimeUntil((int) (waittimeRemaining / 1000L))
							+ ".");
						}
					} else {
						npc.setCurrentState(ConversationStates.QUESTION_1);
						final String question = questions.getQuestion();
						npc.say("Oto zagadka: " + question);
						player.setQuest(QUEST_SLOT, question);
					}
				}
			});

		// player already was set a riddle he couldn't solve
		final String question = questions.getQuestion();
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zagadki", "zagadka", "riddles", "riddle"), 
				new QuestStateStartsWithCondition(QUEST_SLOT, question),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String question = player.getQuest(QUEST_SLOT);
					npc.say("Poznałeś już odpowiedź na mą wcześniejszą zagadkę? Oto ona dla przypomnienia: " + question);
				}
		});

		npc.add(ConversationStates.QUESTION_1, "", null,
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String question = player.getQuest(QUEST_SLOT);
					final String triggerText = sentence.getTriggerExpression().getNormalized();

					if (questions.matches(question, sentence)) {
						player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis());
						int oldXp = player.getXP();
						player.addXP(xpreward);
						int oldAtkXp = player.getAtkXP();
						player.addatk_xp(skillsreward);
						int oldDefXp = player.getDefXP();
						player.adddef_xp(skillsreward);
						int xpDiff = player.getXP() - oldXp;
						int atk_xpDiff = player.getAtkXP() - oldAtkXp;
						int def_xpDiff = player.getDefXP() - oldDefXp;
						if (xpDiff > 0 && atk_xpDiff > 0 && def_xpDiff > 0) {
							npc.say("Bardzo dobrze! Rozwiązałeś poprawnie zagadkę i otrzymałeś #"
									+ xpreward + " punktów doświadczenia oraz #"
									+ skillsreward + " punktów siły jak i obrony. "
									+ "Jeżeli chcesz ponownie rozwiązać jakąś zagadkę wróć do mnie za &'24 godziny'.");
						}
						player.notifyWorldAboutChanges();
						npc.setCurrentState(ConversationStates.IDLE);
					} else if (ConversationPhrases.GOODBYE_MESSAGES.contains(triggerText)) {
						npc.say("Wróć jak odgadniesz mą zagadkę!");
						npc.setCurrentState(ConversationStates.IDLE);
					} else if (triggerText.equals("riddles") || triggerText.equals("riddle") || triggerText.equals("zagadki") || triggerText.equals("zagadka")) {
						npc.say("Już ci zadałem przecież zagadkę, musisz ją tylko rozwiązać. Powtórzę jeszcze raz: " + question);
					} else {
						npc.say("Źle! Spróbuj ponownie!");
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Rozwiąż Test na Drwala",
				"Musisz zdać test na drwala, aby zdobyć umiejętność ścinania drzew.",
				false);
		setQuestion();
	}

	@Override
	public String getName() {
		return "ZagadkiBogumila";
	}

	@Override
	public int getMinLevel() {
		return 50;
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
		return "Bogumil";
	}
}