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
package games.stendhal.server.maps.kalavan.house;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.chest.Chest;

import java.util.Map;

public class CommonChest implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKalavanHouseAreaChest(zone);
	}

	private void buildKalavanHouseAreaChest(final StendhalRPZone zone) {
	    // load the stuff in the house with presents
		final Chest chest = new Chest();
		chest.setPosition(22, 2);
		chest.add(SingletonRepository.getEntityManager().getItem("sok z chmielu"));
		chest.add(SingletonRepository.getEntityManager().getItem("jajo wielkanocne"));
		chest.add(SingletonRepository.getEntityManager().getItem("wielki eliksir"));
		chest.add(SingletonRepository.getEntityManager().getItem("prezent"));
		chest.add(SingletonRepository.getEntityManager().getItem("tarta"));
		zone.add(chest);
	}
}
