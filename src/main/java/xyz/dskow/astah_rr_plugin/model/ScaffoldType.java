package xyz.dskow.astah_rr_plugin.model;

import static java.util.Locale.ENGLISH;

public enum ScaffoldType {
	INTEGER,
	DECIMAL,
	FLOAT,
	STRING,
	TEXT,
	BINARY,
	DATE,
	DATETIME,
	TIMESTAMP,
	TIME,
	BOOLEAN,
	REFERENCES;

	@Override
	public String toString() {
		return super.toString().toLowerCase(ENGLISH);
	}
}
