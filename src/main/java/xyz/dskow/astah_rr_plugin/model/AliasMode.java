package xyz.dskow.astah_rr_plugin.model;

public enum AliasMode {
	NAME,
	ALIAS1,
	ALIAS2;

	public static AliasMode parseAliasMode(final String alias) {
		final AliasMode aliasMode;
		switch (alias) {
			case "ALIAS1>NAME":
				aliasMode = ALIAS1;
				break;
			case "ALIAS2>NAME":
				aliasMode = ALIAS2;
				break;
			default:
				aliasMode = NAME;
				break;
		}
		return aliasMode;
	}
}
