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
package games.stendhal.server.maps.wofol.blacksmith;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ProducerBehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
/**
 * Configure Wofol Blacksmith (-1_semos_mine_nw).
 *
 * @author kymara
 */
public class BlacksmithNPC implements ZoneConfigurator {

	private static Logger logger = Logger.getLogger(BlacksmithNPC.class);

	private static final String QUEST_SLOT = "alrak_make_bobbin";

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBlacksmith(zone);
	}

	private void buildBlacksmith(final StendhalRPZone zone) {
		final SpeakerNPC dwarf = new SpeakerNPC("Alrak") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 8));
				nodes.add(new Node(22, 7));
				nodes.add(new Node(17, 7));
				nodes.add(new Node(17, 2));
				nodes.add(new Node(8, 2));
				nodes.add(new Node(8, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Jak się tutaj dostałeś? Zazwyczaj spotykam tylko #koboldy.");
				addJob("Jam jest kowalem. Niegdyś żyłem wśród górskich krasnali, ale pozostawiłem wspomnienia o tych czasach daleko za sobą i rzekłbym na szczęście!");
				addHelp("Doszły mnie plotki o straszliwych stworach żyjących gdzieś w głębi kopalni i o wściekłych czartach, które są w ich mocy. Na twoim miejscu nie wetknąłbym nawet palca w te czeluście. Jest tam wyjątkowo niebezpiecznie.");
				addOffer("#Wrvil to imię właściciela sklepu, nie moje. Wciąż wyrabiam okazjonalnie #'szpulki do maszyny'. Jeżeli potrzebujesz powiedz #zrób.");
				addReply(Arrays.asList("kobolds", "koboldy"), "Wiesz, te włochate stwory. Jakoś nie miałem okazji rozmawiać z nimi, z wyjątkiem jednego o imieniu #Wrvil.");
				addReply("Wrvil", "On prowadzi handel nieopodal. Niegdyś pomagałem mu w tworzeniu nowych broni, alem już z sił opadł całkiem.");
				addGoodbye();

				addReply(Arrays.asList("bobbin", "szpulki do maszyny", "szpulki", "szpulka"), "Poproś mnie o wykonanie szpulki dla Ciebie o ile masz żelazo ze sobą i trochę money. Powiedz #zrób jeżeli się zdecydujesz. Jestem trochę zapominalski i gdy powrócisz to przypomnij mi mówiąc 'przypomnij'.");

				/* @author kymara */

				// bobbin from iron
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("żelazo", Integer.valueOf(1));
				requiredResources.put("money", Integer.valueOf(100));

				// make sure alrak tells player to remind him to get bobbin back by overriding transactAgreedDeal
				// override giveProduct so that he doesn't say 'welcome back', which is a greeting,
				// in the middle of an active conversation.
				class SpecialProducerBehaviour extends ProducerBehaviour { 
					SpecialProducerBehaviour(final List<String> productionActivity,
                        final String productName, final Map<String, Integer> requiredResourcesPerItem,
											 final int productionTimePerItem) {
						super(QUEST_SLOT, productionActivity, productName,
							  requiredResourcesPerItem, productionTimePerItem, false);
					}

					/**
					 * Tries to take all the resources required to produce the agreed amount of
					 * the product from the player. If this is possible, initiates an order.
					 * 
					 * @param npc
					 *            the involved NPC
					 * @param player
					 *            the involved player
					 */
					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();

						if (getMaximalAmount(player) < amount) {
							// The player tried to cheat us by placing the resource
							// onto the ground after saying "yes"
							npc.say("Hej! Już skończyłem! Nie próbuj mnie oszukać...");
							return false;
						} else {
							for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerItem().entrySet()) {
								final int amountToDrop = amount * entry.getValue();
								player.drop(entry.getKey(), amountToDrop);
							}
							final long timeNow = new Date().getTime();
							player.setQuest(QUEST_SLOT, amount + ";" + getProductName() + ";" + timeNow);
							npc.say("Dobrze zrobię dla Ciebie  "
                                    + getProductionActivity()
									+ " "
									+ amount
									+ " "
									+ getProductName()
									+ ", ale zajmie mi to trochę czasu. Wróć za "
									+ getApproximateRemainingTime(player) + ". "
									+ "Aha i NAJWAŻNIEJSZE - Jestem trochę zapominalski i dlatego MUSISZ mi przypomnieć, mówiąc #przypomnij, abym dał Tobie szpulka do maszyny, gdy wrócisz!");
							return true;
						}
					}
					
					/**
					 * This method is called when the player returns to pick up the finished
					 * product. It checks if the NPC is already done with the order. If that is
					 * the case, the player is given the product. Otherwise, the NPC asks the
					 * player to come back later.
					 * 
					 * @param npc
					 *            The producing NPC
					 * @param player
					 *            The player who wants to fetch the product
					 */
					@Override
						public void giveProduct(final EventRaiser npc, final Player player) {
						final String orderString = player.getQuest(QUEST_SLOT);
						final String[] order = orderString.split(";");
						final int numberOfProductItems = Integer.parseInt(order[0]);
						if (!isOrderReady(player)) {
							npc.say("Wciąż pracuję nad "
									+ getProductName()
									+ " dla Ciebie. Proszę wróć za "
									+ getApproximateRemainingTime(player) + ", aby odebrać. Nie zapomnij mi przypomnieć, mówiąc #przypomnij ... ");
						} else {
							final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(getProductName());

							products.setQuantity(numberOfProductItems);

							if (isProductBound()) {
								products.setBoundTo(player.getName());
							}

							player.equipOrPutOnGround(products);
							npc.say("Skończyłem! Oto "
									+ Grammar.quantityplnoun(numberOfProductItems,
															 getProductName(), "") + ".");
							player.setQuest(QUEST_SLOT, "done");
							// give some XP as a little bonus for industrious workers
							player.addXP(numberOfProductItems);
							player.notifyWorldAboutChanges();
						}
					}
				}
				
				final ProducerBehaviour behaviour = new SpecialProducerBehaviour(Arrays.asList("make", "zrób"), "szpulka do maszyny",
				        requiredResources, 10 * 60);

				// we are not using producer adder at all here because that uses Conversations states IDLE and saying 'hi' heavily.
				// we can't do that here because Pequod uses that all the time in his fishing quest. so player is going to have to #remind
				// him if he wants his oil back!

				add(
				ConversationStates.ATTENDING,
				Arrays.asList("make", "zrób"),
				new QuestNotActiveCondition(behaviour.getQuestSlot()),
				ConversationStates.ATTENDING, null,
				new ProducerBehaviourAction(behaviour) {
					@Override
					public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser npc) {
						// Find out how much items we shall produce.
						if (res.getAmount() > 1000) {
							logger.warn("Decreasing very large amount of "
									+ res.getAmount()
									+ " " + res.getChosenItemName()
									+ " to 1 for player "
									+ player.getName() + " talking to "
									+ npc.getName() + " saying " + sentence);
							res.setAmount(1);
						}

						if (behaviour.askForResources(res, npc, player)) {
							currentBehavRes = res;
							npc.setCurrentState(ConversationStates.PRODUCTION_OFFERED);
						}
					}
				});

		add(ConversationStates.PRODUCTION_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
						final EventRaiser npc) {
							behaviour.transactAgreedDeal(currentBehavRes, npc, player);

							currentBehavRes = null;
						}
				});

		add(ConversationStates.PRODUCTION_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Dobrze, nie ma problemu.", null);

		add(ConversationStates.ATTENDING,
				Arrays.asList("remind", "przypomnij"),
				new QuestActiveCondition(behaviour.getQuestSlot()),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						behaviour.giveProduct(npc, player);
					}
				});
			
			}
				

		    //remaining behaviour defined in maps.quests.ObsidianKnife
			};

		dwarf.setDescription("Oto Alrak, kowal krasnali i pustelnik.");
		dwarf.setEntityClass("dwarfnpc");
		dwarf.setPosition(22, 8);
		dwarf.initHP(100);
		zone.add(dwarf);
	}
}
