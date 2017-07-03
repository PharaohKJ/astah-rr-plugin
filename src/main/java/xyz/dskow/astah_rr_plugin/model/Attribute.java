package xyz.dskow.astah_rr_plugin.model;

import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import java.util.Arrays;
import static java.util.Locale.ENGLISH;
import java.util.Optional;
import static lombok.AccessLevel.PACKAGE;
import lombok.Getter;
import static xyz.dskow.astah_rr_plugin.model.Attribute.TAGGEDVALUE_KEY_SCAFFOLD_TYPE;
import static xyz.dskow.astah_rr_plugin.model.ScaffoldType.REFERENCES;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper.findElementById;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahHelper.findTaggedValue;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahHelper.getName;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahHelper.putTaggedValue;

public class Attribute {

	static final String TAGGEDVALUE_KEY_ENABLED = "jude.astah-rr-plugin.enabled";
	static final String TAGGEDVALUE_KEY_SCAFFOLD_TYPE = "jude.astah-rr-plugin.scaffoldType";

	@Getter
	private final String objectId;
	private final AliasMode aliasMode;

	@Getter(PACKAGE)
	private ScaffoldType type;

	void setType(final String type) {
		this.type = convertScaffoldType(type);
	}

	Attribute(final String id) {
		this(id, AliasMode.NAME);
	}

	public Attribute(final String id, final AliasMode aliasMode) {
		this.objectId = id;
		this.aliasMode = aliasMode;
	}

	public void setEnabled(final boolean enabled) {
		putTaggedValue(getIAttribute(), TAGGEDVALUE_KEY_ENABLED, Boolean.toString(enabled));
	}

	public void setScaffoldType(final ScaffoldType type) {
		putTaggedValue(getIAttribute(), TAGGEDVALUE_KEY_SCAFFOLD_TYPE, type.name());
	}

	public String getDisplayName() {
		final IAttribute attr = getIAttribute();
		final ScaffoldType theType = this.getScaffoldType();
		final String name;
		if (theType == REFERENCES) {
			name = getName(attr.getType(), aliasMode);
		} else {
			name = getName(attr, aliasMode);
		}
		return name;
	}

	public ScaffoldType getScaffoldType() {
		final IAttribute attr = getIAttribute();
		final ITaggedValue tv = findTaggedValue(attr, TAGGEDVALUE_KEY_SCAFFOLD_TYPE);
		if (tv == null) {
			return convertScaffoldType(attr.getTypeExpression());
		}
		return ScaffoldType.valueOf(tv.getValue());
	}

	public boolean isEnabled() {
		final IAttribute attr = getIAttribute();
		final ITaggedValue tv = findTaggedValue(attr, TAGGEDVALUE_KEY_ENABLED);
		if (tv == null) {
			return true;
		}
		return Boolean.parseBoolean(tv.getValue());
	}

	public Attribute deepCopy() {
		return new Attribute(this.getObjectId(), this.aliasMode);
	}

	private IAttribute getIAttribute() {
		return findElementById(IAttribute.class, this.getObjectId());
	}

	private static ScaffoldType convertScaffoldType(final String typeExpression) {
		final ScaffoldType type;
		switch (typeExpression.toLowerCase(ENGLISH)) {
			case "int":
			case "long":
				type = ScaffoldType.INTEGER;
				break;
			case "string":
				type = ScaffoldType.TEXT;
				break;
			case "boolean":
			case "bool":
				type = ScaffoldType.BOOLEAN;
				break;
			case "float":
			case "double":
				type = ScaffoldType.FLOAT;
				break;
			case "bigdecimal":
			case "decimal":
				type = ScaffoldType.DECIMAL;
				break;
			case "date":
				type = ScaffoldType.DATE;
				break;
			case "datetime":
				type = ScaffoldType.DATETIME;
				break;
			case "time":
				type = ScaffoldType.TIME;
				break;
			case "timestamp":
				type = ScaffoldType.TIMESTAMP;
				break;
			default:
				type = ScaffoldType.REFERENCES;
				break;
		}
		return type;
	}

	public static class AstahAttribute {

		public AstahAttribute(final IAttribute attr) {
			this.id = attr.getId();
			this.name = attr.getName();
			this.typeExpression = attr.getTypeExpression();
			final Optional<ITaggedValue> targetTv = Arrays.stream(attr.getTaggedValues())
					.filter(tv -> TAGGEDVALUE_KEY_SCAFFOLD_TYPE.equals(tv.getKey()))
					.findAny();
			if (targetTv.isPresent()) {
				scaffoldTaggedValueId = targetTv.get().getId();
			} else {
				scaffoldTaggedValueId = "";
			}
		}

		@Getter
		private final String id;
		@Getter
		private final String name;
		@Getter
		private final String typeExpression;
		@Getter
		private final String scaffoldTaggedValueId;
	}
}
