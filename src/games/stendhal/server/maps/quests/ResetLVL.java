package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

public class ResetLVL extends AbstractQuest {
	
	private static final String QUEST_SLOT = "reset_level";
	private static int LEVEL_TO_RESET = 0;
	private static int XP_TO_RESET = 0;

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest("Yerena")) {
			return res;
		}
		res.add("Spotkałem smoka Yerena w jaskini w domku na górze Zakopane.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem cofnięcia się w czasie.");
			return res;
		}
		res.add("Postanowiłem posłuchać smoka Yerena i cofnąć się w czasie.");
		if ("done".equals(questState)) {
			res.add("Yerena cofnęła mój poziom i od teraz muszę na nowo zdobywać doświadczenie!");
		} 
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void offerresetlevel() {
		final SpeakerNPC npc = npcs.get("Yerena");
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.getLevel() == 597) {
							raiser.say("Dzielny wojowniku, czy jesteś gotów cofnąć swój aktualny poziom?");
						} else {
							npc.say("Abym mógł cofnąć Ciebie w czasie to musisz osiągnąć maksymalny poziom! Twój aktualny poziom to: #"
									+ player.getLevel());
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_1,
				"Pamiętaj, że twój poziom zostanie &'zresetowany', ale #'zadania', #'skille' jak i #'punkty życia' już nie! Chcesz tego? (#'tak')",
				new SetQuestAction(QUEST_SLOT, "start"));
		
		// Jeżeli gracz odrzuci 1 ostrzeżenie
		npc.add(ConversationStates.INFORMATION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));
		
		npc.add(
				ConversationStates.INFORMATION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_2,
				"Proszę, zastanów się jeszcze. Czy jesteś tego pewien? (#'tak')",
				null);
		
		// Jeżeli gracz odrzuci 2 ostrzeżenie
		npc.add(ConversationStates.INFORMATION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));

		npc.add(
				ConversationStates.INFORMATION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_3,
				"Cofnięcie się w czasie zpowoduje, że twój aktualny #poziom się &'wyzeruje', ale twoje #umiejętności zostaną takie jakie były wcześniej! Twoje aktualne zdrowie również zostaną bez zmian. Czy jesteś tego pewien? (#'tak')",
				null);
		
		// Jeżeli gracz odrzuci 3 ostrzeżenie
		npc.add(ConversationStates.INFORMATION_3,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}
	
	private void startresetlevel() {
		final SpeakerNPC npc = npcs.get("Yerena");
		
		npc.add(ConversationStates.INFORMATION_3,
				ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING,
				"Proszę bardzo! Cofnąłeś się do momentu kiedy miałeś jeszcze poziom zerowy! Życzę Ci powodzenia na nowej drodze!",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
							player.setQuest(QUEST_SLOT, "done");

							player.setXP(XP_TO_RESET);
							player.setLevel(LEVEL_TO_RESET);
						}
					}
				});
		
		npc.add(new ConversationStates[] { ConversationStates.ATTENDING,
				ConversationStates.INFORMATION_1,
				ConversationStates.INFORMATION_2,
				ConversationStates.INFORMATION_3},
			ConversationPhrases.NO_MESSAGES, new NotCondition(new QuestInStateCondition(QUEST_SLOT, "start")), ConversationStates.IDLE,
			"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
			null);
		
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Smok, który włada czasem",
				"Yerena potrafi cofnąć wojownika w czasie kiedy był jeszcze początkujący.",
				false);
		offerresetlevel();
		startresetlevel();
	}

	@Override
	public String getName() {
		return "ResetLVL";
	}
	
	@Override
	public String getNPCName() {
		return "Yerena";
	}
}