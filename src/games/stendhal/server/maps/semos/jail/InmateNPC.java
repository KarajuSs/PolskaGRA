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
package games.stendhal.server.maps.semos.jail;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An elven inmate (original name: Conual). He's just decoration. 
 * 
 * @author hendrik
 */
public class InmateNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Conual") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGreeting("Wypuście mnie! Jestem niewinny!");
				addGoodbye();
			}};
			npc.setPosition(13, 3);
			npc.setEntityClass("militiaelfnpc");
			npc.setDirection(Direction.DOWN);
			npc.setDescription("Oto Conual. On jest skazany na dożywocie. Wydaje się, że naprawdę zrobił coś złego.");
			zone.add(npc);		
	}
}


