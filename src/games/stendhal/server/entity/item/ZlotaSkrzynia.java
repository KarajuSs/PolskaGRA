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
 * A present which can be unwrapped.
 * 
 * @author kymara
 */
public class ZlotaSkrzynia extends Box {

	private static final String[] ITEMS = { "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "wielki eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", 
											"gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "srebrny sztylet", "rękawice cieni", "tarcza płytowa", "tarcza płytowa", "tarcza płytowa", "tarcza płytowa", "tarcza płytowa", "tarcza płytowa", "zwój tatrzański", "zwój tatrzański", 
											"zwój tatrzański", "zwój tatrzański", "zwój tatrzański", "zwój semos", "zwój semos", "zwój semos", "zwój semos", "zwój semos", "skóra zielonego smoka", "skóra niebieskiego smoka", "skóra czarnego smoka", "skóra czerwonego smoka", "zbroja cieni", "skóra złotego smoka", "skóra arktycznego smoka", 
											"maczuga", "maczuga", "maczuga", "maczuga", "maczuga", "korale", "korale", "pas zbójecki", "pas zbójecki", "skórzane wzmocnione rękawice", "skórzane wzmocnione rękawice", "skórzane wzmocnione rękawice", "skórzane wzmocnione rękawice", "czarne spodnie", "złote spodnie", "złote spodnie", 
											"bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "bukłak z wodą", "jabłko niezgody", "jabłko niezgody", "jabłko niezgody", "pizza", "pizza", "pizza", "pizza", "kanapka", "kanapka", "kanapka", "kanapka",
											"money", "money", "money", "money", "money", "money", "money", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir", "sztylet mroku" };

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

		setContent(ITEMS[Rand.rand(ITEMS.length)]);
	}

	/**
	 * Sets content.
	 * @param type of item to be produced.
	 */
	public void setContent(final String type) {
		setInfoString(type);
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

		final String itemName = getInfoString();
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		player.sendPrivateText("Gratulacje dostałeś " 
				+ Grammar.a_noun(ItemTools.itemNameToDisplayName(itemName)));

		player.equipOrPutOnGround(item);
		player.notifyWorldAboutChanges();

		return true;
	}

}
