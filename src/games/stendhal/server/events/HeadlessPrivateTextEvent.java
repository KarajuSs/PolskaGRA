/***************************************************************************
 *                   (C) Copyright 2018 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import games.stendhal.common.NotificationType;
import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/**
 * Same as PrivateTextEvent, but does not draw on canvas.
 *
 * FIXME: Should be able to be done in PrivateTextEvent with attribute.
 */
public class HeadlessPrivateTextEvent extends RPEvent {

	private static final String TEXT_TYPE = "texttype";
	private static final String CHANNEL = "channel";
	private static final String TEXT = "text";

	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.HEADLESS_PRIVATE_TEXT);
		rpclass.add(DefinitionClass.ATTRIBUTE, TEXT_TYPE, Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, CHANNEL, Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, TEXT, Type.LONG_STRING);
	}

	/**
	 * Creates a new text event.
	 *
	 * @param type NotificationType
	 * @param text Text
	 */
	public HeadlessPrivateTextEvent(final NotificationType type, final String text) {
		super(Events.HEADLESS_PRIVATE_TEXT);
		put(TEXT_TYPE, type.name());
		put(TEXT, text);
	}
}