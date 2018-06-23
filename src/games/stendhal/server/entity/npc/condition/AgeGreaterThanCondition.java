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
package games.stendhal.server.entity.npc.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Is the player's age greater than the specified age?
 */
@Dev(category=Category.STATS, label="Age?")
public class AgeGreaterThanCondition implements ChatCondition {

	private final int age;

	/**
	 * Creates a new AgeGreaterThanCondition.
	 *
	 * @param age
	 *            age
	 */
	public AgeGreaterThanCondition(final int age) {
		this.age = age;
	}

	/**
	 * @return true if players age greater than age in condition
	 */
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		return (player.getAge() > age);
	}

	@Override
	public String toString() {
		return "age > " + age + " ";
	}

	@Override
	public int hashCode() {
		return 43591 * age;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof AgeGreaterThanCondition)) {
			return false;
		}
		return age == ((AgeGreaterThanCondition) obj).age;
	}

}
