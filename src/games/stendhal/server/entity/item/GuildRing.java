/***************************************************************************
 *                      (C) Copyright 2018 - Marauroa                      *
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

import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.ItemLogger;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

import java.util.Map;
import java.util.StringTokenizer;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SlotOwner;

import org.apache.log4j.Logger;

/**
 * A special ring that allows the owner to teleport to his or her spouse. The
 * spouse's name is engraved into the ring. Technically, the name is stored in
 * the item's infostring.
 *
 * Guild rings should always be bound to the owner.
 *
 * @author daniel
 */
public class GuildRing extends Item {
	/** The cooling period of players of same level in seconds */

	private static final Logger logger = Logger.getLogger(GuildRing.class);

	/**
	 * Creates a new guild ring.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public GuildRing(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		setPersistent(true);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public GuildRing(final GuildRing item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		RPObject base = getBaseContainer();

		if ((user instanceof Player) && user.nextTo((Entity) base)) {
			return teleportToGuild((Player) user);
		}
		return false;
	}

	/**
	 * Teleports the given player to his/her spouse, but only if the spouse is
	 * also wearing the wedding ring.
	 *
	 * @param player
	 *            The ring's owner.
	 */
	private boolean teleportToGuild(final Player player) {
		// init as guild ring
		StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_zakopane_s");
		int x = 30;
		int y = 30;

		/*
		 * Guild ring have a destination which is stored in the infostring,
		 * existing of a zone name and x and y coordinates
		 */
		final String infostring = getInfoString();

		if (infostring != null) {
			final StringTokenizer st = new StringTokenizer(infostring);
			if (st.countTokens() == 3) {
				// check destination
				final String zoneName = st.nextToken();
				final StendhalRPZone temp = SingletonRepository.getRPWorld().getZone(zoneName);
				if (temp == null) {
					// invalid zone (the ring may have been marked in an
					// old version and the zone was removed)
					player.sendPrivateText("Z dziwnych powodów pierścień nie przeniósł mnie tam gdzie chciałem.");
					logger.warn("ring to unknown zone " + infostring
							+ " teleported " + player.getName()
							+ " to Zakopane instead");
				} else {
					final StendhalRPZone sourceZone = player.getZone();
					zone = temp;
					x = Integer.parseInt(st.nextToken());
					y = Integer.parseInt(st.nextToken());
					if ((!zone.isTeleportOutAllowed(x, y)) || (!sourceZone.isTeleportOutAllowed(player.getX(), player.getY()))) {
						player.sendPrivateText("Silna antymagiczna aura blokuje działanie pierścienia!");
						return false;
					}
				}
			}
			// we use the player as teleporter (last parameter) to give feedback
			// if something goes wrong.
			return player.teleport(zone, x, y, null, player);
		} else {
			return false;
		}
	}

	// Check if there are more rings in the slot where this ring was added
	@Override
	public void setContainer(final SlotOwner container, final RPSlot slot) {
		GuildRing oldRing = null;
		// only bound rings destroy others
		if ((slot != null) && (getBoundTo() != null)) {
			for (final RPObject object : slot) {
				if ((object instanceof GuildRing)
						&& (!getID().equals(object.getID()))) {
					final GuildRing ring = (GuildRing) object;
					if (getBoundTo().equals(ring.getBoundTo())) {
						oldRing = (GuildRing) object;
						break;
					}
				}
			}
		}

		if (oldRing != null) {
			// The player is cheating with multiple rings. Explode the
			// old ring, and use up the energy of this one
			destroyRing(container, oldRing, slot);
		}

		super.setContainer(container, slot);
	}

	/**
	 * Destroy a guild ring.
	 * To be used when a ring is put in a same slot with another.
	 *
	 * @param container
	 * @param ring the ring to be destroyed
	 */
	private void destroyRing(SlotOwner container, final GuildRing ring, final RPSlot slot) {
		// The players need to be told first, while the ring still
		// exist in the world
		informNearbyPlayers(ring);

		RPEntity player = null;
		if (container instanceof RPEntity) {
			player = (RPEntity) container;
		}

		new ItemLogger().destroy(player, slot, ring, "another ring");
		ring.removeFromWorld();
		logger.info("Destroyed a guild ring: " + ring);
	}

	/**
	 * Give a nice message to nearby players when rings get destroyed.
	 */
	private void informNearbyPlayers(final GuildRing ring) {
		try {
			final Entity container = (Entity) ring.getBaseContainer();
			final StendhalRPZone zone = getZone();

			if (zone != null) {
				for (final Player player : zone.getPlayers()) {
					if (player.nextTo(container)) {
						player.sendPrivateText(NotificationType.SCENE_SETTING,
						"Błyska światło, gdy guild ring zaczyna się rozpadać w zetknięciu magii.");
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e);
		}
	}
}
