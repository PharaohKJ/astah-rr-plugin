package xyz.dskow.astah_rr_plugin.utils;

import java.awt.Color;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SystemUtils {

	private SystemUtils() {
	}

	public static boolean isMacLaF() {
		final String name = UIManager.getLookAndFeel().getName();
		log.trace("LAF:{}", name);
		return name.contains("Mac");
	}

	public static Color getColor(final String key) {
		if (isMacLaF()) {
			switch (key) {
				case "TableHeader.background":
					return new Color(246, 246, 246);
				default:
					break;
			}
		}
		return UIManager.getColor(key);
	}
}
