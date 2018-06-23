/* $Id$ */
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
package games.stendhal.server.entity.npc.action;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * BehaviourAction handles ProducerBehaviour requests.
 */
@Dev(category=Category.IGNORE)
public abstract class ProducerBehaviourAction extends AbstractBehaviourAction<ProducerBehaviour> {

	public ProducerBehaviourAction(final ProducerBehaviour behaviour) {
		this(behaviour, "produce");
	}

	public ProducerBehaviourAction(final ProducerBehaviour behaviour, String npcAction) {
		super(behaviour, behaviour.getProductionActivity(), npcAction);
	}

	@Override
	public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
		raiser.say(behaviour.getErrormessage(res, npcAction));
	}

	@Override
	public String toString() {
		return "ProducerBehaviourAction";
	}


}
