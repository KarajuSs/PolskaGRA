/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.portal;

import java.awt.Point;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusType;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;

/**
 * A portal which teleports the player to another portal if used.
 */
public class Portal extends Entity implements UseListener {
	private static final String RPCLASS_NAME = "portal";

	/**
	 * The hidden flags attribute name.
	 */
	protected static final String ATTR_HIDDEN = "hidden";
	protected static final String ATTR_USE = "use";
	
	/** Attribute for player face direction. */
	protected static final String ATTR_FACE = "face";
	
	/** Attribute for player positioning. */
	protected static final String ATTR_OFFSET = "offset";

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(Portal.class);

	private boolean isDestinationSet;

	private Object identifier;

	private String destinationZone;

	private Object destinationReference;
	
	/** Direction player should face after teleport when portal is used as destination. */
	private Direction face;

	/** If "true", will not emit logger error about missing destination */
	private boolean ignoreNoDestination = false;

	public static void generateRPClass() {
		try {
				if (!RPClass.hasRPClass(RPCLASS_NAME)){
					final RPClass portal = new RPClass(RPCLASS_NAME);
					portal.isA("entity");
					portal.addAttribute(ATTR_USE, Type.FLAG);
					portal.addAttribute(ATTR_HIDDEN, Type.FLAG);
					portal.addAttribute(ATTR_FACE, Type.STRING);
					portal.addAttribute(ATTR_OFFSET, Type.INT);
				}
		} catch (final SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Creates a new portal.
	 */
	public Portal() {
		setRPClass(RPCLASS_NAME);
		put("type", RPCLASS_NAME);

		isDestinationSet = false;
	}

	public Portal(final RPObject object) {
		super(object);
		setRPClass(RPCLASS_NAME);
		put("type", RPCLASS_NAME);
		isDestinationSet = false;
	}

	/**
	 * Set the portal reference to identify this specific portal with-in a zone.
	 * This value is opaque and requires a working equals(), but typically uses
	 * a String or Integer.
	 *
	 * @param reference
	 *            A reference tag.
	 */
	public void setIdentifier(final Object reference) {
		this.identifier = reference;
	}

	/**
	 * Sets flag to suppress error about missing destination.
	 */
	public void setIgnoreNoDestination(final boolean ignoreNoDestination) {
		this.ignoreNoDestination = ignoreNoDestination;
	}

	/**
	 * gets the identifier of this portal.
	 *
	 * @return identifier
	 */
	public Object getIdentifier() {
		return identifier;
	}

	/**
	 * Set the destination portal zone and reference. The reference should match
	 * the same type/value as that passed to setReference() in the corresponding
	 * portal.
	 *
	 * @param zone
	 *            The target zone.
	 * @param reference
	 *            A reference tag.
	 */
	public void setDestination(final String zone, final Object reference) {
		this.destinationReference = reference;
		this.destinationZone = zone;
		this.isDestinationSet = true;
	}

	public Object getDestinationReference() {
		return destinationReference;
	}

	public String getDestinationZone() {
		return destinationZone;
	}

	/**
	 * Determine if this portal is hidden from players.
	 *
	 * @return <code>true</code> if hidden.
	 */
	@Override
	public boolean isHidden() {
		return has(ATTR_HIDDEN);
	}

	public boolean loaded() {
		return isDestinationSet;
	}

	public void logic() {
	    // Sub-classes can implement this
	}

	@Override
	public String toString() {
		final StringBuilder sbuf = new StringBuilder();
		sbuf.append("Portal");

		final StendhalRPZone zone = getZone();

		if (zone != null) {
			sbuf.append(" at ");
			sbuf.append(zone.getName());
		}

		sbuf.append('[');
		sbuf.append(getX());
		sbuf.append(',');
		sbuf.append(getY());
		sbuf.append(']');

		if (isHidden()) {
			sbuf.append(", hidden");
		}

		return sbuf.toString();
	}

	/**
	 * Use the portal.
	 *
	 * @param player
	 *            the Player who wants to use this portal
	 * @return <code>true</code> if the portal worked, <code>false</code>
	 *         otherwise.
	 */
	protected boolean usePortal(final Player player) {
		if (!player.isZoneChangeAllowed()) {
			player.sendPrivateText("Z jakiegoś powodu nie możesz teraz przejść.");
			return false;
		}

		if (!nextTo(player) && has("use")) {
			player.sendPrivateText("Musisz podejść bliżej, aby użyć kryształu.");
			return false;
		}

		if (!nextTo(player)) {
			// Too far to use the portal from here, but walk to it
			// The pathfinder will do the rest of the work and make the player pass through the portal
			// Check that mouse movement is allowed first
			if (!player.getZone().isMoveToAllowed()) {
				player.sendPrivateText("Nie jest możliwe posługiwanie się myszką. Użyj klawiatury.");
			} else if (player.hasStatus(StatusType.POISONED)) {
				player.sendPrivateText("Trucizna zdezorientowała Ciebie i nie możesz normalnie się poruszać. Możesz iść wstecz i nie możesz zaplanować trasy.");
			} else {
				final List<Node> path = Path.searchPath(player, this.getX(), this.getY());
				player.setPath(new FixedPath(path, false));
			}
			return false;
		}

		if (getDestinationZone() == null) {
			if (!ignoreNoDestination) {
				// This portal is incomplete
				logger.error(this + " has no destination.");
			}
			return false;
		}

		final StendhalRPZone destZone = SingletonRepository.getRPWorld().getZone(
				getDestinationZone());

		if (destZone == null) {
			logger.error(this + " has invalid destination zone: "
					+ getDestinationZone());
			return false;
		}

		final Portal dest = destZone.getPortal(getDestinationReference());

		if (dest == null) {
			// This portal is incomplete
			logger.error(this + " has invalid destination identitifer: "
					+ getDestinationReference());
			return false;
		}

		int destX = dest.getX();
		int destY = dest.getY();

		// Offset positioning of player in relation to the destination portal.
		if (dest.hasOffset()) {
			final int pos = dest.getOffset();
			switch (pos) {
				case 0:
					destX = destX - 1;
					destY = destY - 1;
					break;
				case 1:
					destY = destY - 1;
					break;
				case 2:
					destX = destX + 1;
					destY = destY - 1;
					break;
				case 3:
					destX = destX - 1;
					break;
				case 4:
					destX = destX + 1;
					break;
				case 5:
					destX = destX - 1;
					destY = destY + 1;
					break;
				case 6:
					destY = destY + 1;
					break;
				case 7:
					destX = destX + 1;
					destY = destY + 1;
					break;
				default:
					logger.debug("Invalid destination portal offset positioning: " + Integer.toString(pos));
			}
		}

		if (player.teleport(destZone, destX, destY, null, null)) {
			/* Allow player to continue movement after teleport via portal
			 * without the need to release and press direction again.
			 * 
			 * FIXME: Cannot predict which side of portal player will end up.
			 */
			//if (!player.has(MOVE_CONTINUOUS)) {
			//	player.stop();
			//}
			player.forceStop();

			dest.onUsedBackwards(player);
		}
		return true;
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (user instanceof Player) {
			return usePortal((Player) user);
		} else {
			logger.error("user is no instance of Player but: " + user, new Throwable());
			return false;
		}
	}

	/**
	 * if this portal is the destination of another portal used.
	 *
	 * @param user
	 *            the player who used the other portal teleporting to us
	 */
	public void onUsedBackwards(final RPEntity user) {
		if (hasFaceDirection()) {
			user.setDirection(getFaceDirection());
		}
	}

	/**
	 * Sub-classes can override for actions to be taken if entity was pushed onto portal.
	 */
	@SuppressWarnings("unused")
	public void onPushedOntoFrom(final RPEntity pushed, final RPEntity pusher, final Point prevPos) {
		// implementing classes can override
	}

	/**
	 * Sets the direction attribute for the portal which determines the direction the
	 * player should face when this portal is used as a destination.
	 * 
	 * @param dir
	 * 						<code>Direction</code> player should face.
	 */
	public final void setFaceDirection(final Direction dir) {
		logger.debug("Setting portal direction: " + dir.toString());
		face = dir;
	}
	
	/**
	 * Setup direction player should face after using portal as a
	 * destination. <code>dir</code> can be one of "north", "east",
	 * "south", "west", "up", "right", "down" or "left".
	 * 
	 * @param dir
	 * 						<code>String</code> representation of direction to face.
	 */
	public void setFaceDirection(String dir) {
		// Convert to lowercase.
		dir = dir.toLowerCase();
		
		logger.debug("Portal face attribute: " + dir);
		switch(dir) {
			case "north":
			case "up":
				face = Direction.UP;
				break;
			case "south":
			case "down":
				face = Direction.DOWN;
				break;
			case "east":
			case "right":
				face = Direction.RIGHT;
				break;
			case "west":
			case "left":
				face = Direction.LEFT;
				break;
			default:
					logger.warn("Not a valid direction: " + dir);
		}
	}
	
	/**
	 * Get the direction player should face when portal is used as a destination.
	 * 
	 * @return
	 * 						<code>Direction</code> player should face.
	 */
	public final Direction getFaceDirection() {
		return face;
	}
	
	/**
	 * Check if the portal has defined a direction for player to face when
	 * portal is used as a destination.
	 * 
	 * @return
	 * 						<code>true</code> if portal's direction attribute is set.
	 */
	public final boolean hasFaceDirection() {
		return (face != null);
	}
	
	/**
	 * Gets offset positioning value when used as a destination. Valid values
	 * are 0-7.
	 *
	 * @return
	 *			<code>Integer</code> value of offset.
	 */
	public final int getOffset() {
		return getInt("offset");
	}

	/**
	 * Checks if portal has an offset positioning when used as a destination.
	 *
	 * @return
	 * 			<code>true<code> if "offset" attribute set.
	 */
	public final boolean hasOffset() {
		return has("offset");
	}
}
