package games.stendhal.server.maps.quests.captureflag;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.List;

/**
 * leave a game of CTF (remove the tag uselistener
 * @author hendrik, sjtsp 
 *
 */
public class LeaveCaptureFlagAction implements ChatAction {

	static List<String> ctfItemNames = Arrays.asList(
			"flag",
			"łuk zf",
			"strzała pogrzebania",
			"strzała spowolnienia",
			"strzała przyspieszenia",
			"śnieżka pogrzebania",
			"śnieżka spowolnienia",
			"śnieżka przyspieszenia"
			);
			
	
	/**
	 * drop anything the player is carrying that is ctf-related
	 * 
	 * TODO: probably goes elsewhere - more general support class
	 * @param player player whose items should be dropped
	 */
	public void dropAllCTFItems(Player player) {
		
		// TODO: better to loop over what the player does have, or
		//       just force drop of all 

		int amount;
		
		for (String ctfItemName : ctfItemNames) {

			amount = player.getNumberOfEquipped(ctfItemName);

			if (amount > 0) {
				
				// TODO: if it's a flag, have to force item.unequipped
				//       to change outfit (or, better, fix RPEntity.drop())
				player.drop(ctfItemName, amount);
			}
		}
	}
	
	
	@Override
	public void fire(Player player, Sentence sentence, EventRaiser npc) {
		player.removeUseListener();

		dropAllCTFItems(player);
	}
}
