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
package games.stendhal.server.entity.item;

import games.stendhal.common.ItemTools;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Zlota skrzynia
 *
 * @author KarajuSs
 */
public class ZlotaSkrzynia extends Box {

	private static final String[] items = { "money", "wielki eliksir", "gigantyczny eliksir",
			"miecz lodowy", "rękawice cieni", "tarcza płytowa", "skóra czerwonego smoka", "skóra zielonego smoka",
			"skóra niebieskiego smoka", "skóra czarnego smoka", "korale", "zbroja cieni", "skórzane wzmocnione rękawice",
			"złote spodnie", "skóra złotego smoka", "skóra arktycznego smoka", "sztylet mroku", "pas zbójecki",
			"czarne spodnie", "spodnie z mithrilu", "ciupaga" };

	/**
	 * Creates a new present.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public ZlotaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public ZlotaSkrzynia(final ZlotaSkrzynia item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();

		final String itemName = items[Rand.rand(items.length)];
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		if (itemName.equals(itemName)) {
			/*
			 * Bound powerful items.
			 */
			item.setBoundTo(player.getName());
		}

		player.equipOrPutOnGround(item);
		player.incObtainedForItem(item.getName(), item.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Gratulacje! Ze skrzynki otrzymałeś &'"
				+ Grammar.a_noun(ItemTools.itemNameToDisplayName(itemName) + "'!"));

		return true;
	}

}
