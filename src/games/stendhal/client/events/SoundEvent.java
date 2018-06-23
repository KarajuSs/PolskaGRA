/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2014 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.sound.facade.AudibleArea;
import games.stendhal.client.sound.facade.AudibleCircleArea;
import games.stendhal.client.sound.facade.InfiniteAudibleArea;
import games.stendhal.client.sound.facade.SoundFileType;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.math.Algebra;
import games.stendhal.common.math.Numeric;

/**
 * Plays a sound.
 *
 * @author hendrik
 */
class SoundEvent extends Event<Entity> {
	/**
	 * Executes the event.
	 */
	@Override
	public void execute() {
		SoundLayer layer = SoundLayer.AMBIENT_SOUND;
		int idx = event.getInt("layer");
		if (idx < SoundLayer.values().length) {
			layer = SoundLayer.values()[idx];
		}
		float volume = 1.0f;
		if (event.has("volume")) {
			volume = Numeric.intToFloat(event.getInt("volume"), 100.0f);
		}
		String soundName = event.get("sound");
		AudibleArea area;
		if (event.has("radius")) {
			int radius = event.getInt("radius");
			area = new AudibleCircleArea(Algebra.vecf((float) entity.getX(), (float) entity.getY()), radius / 4.0f, radius);
		} else {
			area = new InfiniteAudibleArea();
		}

		SoundGroup group = ClientSingletonRepository.getSound().getGroup(layer.groupName);
		group.loadSound(soundName, soundName + ".ogg", SoundFileType.OGG, false);
		group.play(soundName, volume, 0, area, null, false, true);
	}
}
