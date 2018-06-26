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
package games.stendhal.server.maps.athor.holiday_area;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

public class HusbandNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("John") {
			@Override
			public void say(final String text) {
				// He doesn't move around because he's "lying" on his towel.
				say(text, false);
			}

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
	}
	
			@Override
			public void createDialog() {
				addGreeting("Cześć!");
				addJob("Jestem woźnicą, ale na tej wyspie nie ma powozów!");
				addHelp("Nie próbuj rozmawiać z moją żoną, jest bardzo nieśmiała.");
				addGoodbye("Dowidzenia!");
			}

		};
		npc.setPosition(27, 44);
		npc.setEntityClass("swimmer5npc");
		npc.setDescription ("Oto John na plaży. Wypoczywa na plaży wraz ze swoją żoną Jane.");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);		
	}
}

