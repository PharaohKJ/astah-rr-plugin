package xyz.dskow.astah_rr_plugin.model;

import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper.findElementById;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahHelper.getName;

@ToString
public class Scaffold {

	@Getter
	private final String objectId;

	private final AliasMode aliasMode;

	@Getter
	@Setter
	private String code;

	public Scaffold(final String id, final AliasMode aliasMode) {
		this.objectId = id;
		this.aliasMode = aliasMode;
		setGenerateCode();
	}

	public List<Attribute> getAttributes() {
		final IClass cls = findElementById(IClass.class, this.objectId);
		final IAttribute[] attrs = cls.getAttributes();
		if (attrs == null || attrs.length == 0) {
			return new ArrayList<>(0);
		}
		return Arrays.stream(attrs)
				.map(attr -> new Attribute(attr.getId(), this.aliasMode))
				.collect(Collectors.toList());
	}

	public Attribute getAttribute(final int index) {
		return getAttributes().get(index);
	}

	public String getClassName() {
		final IClass cls = findElementById(IClass.class, this.objectId);
		return getName(cls, aliasMode);
	}

	private void setGenerateCode() {
		// TODO
		final String theCode;
		if (this.getAttributes().isEmpty()) {
			theCode = "";
		} else {
			final StringBuilder sb = new StringBuilder();
			sb.append("rails generate scaffold ");
			sb.append(this.getClassName());
			this.getAttributes().stream()
					.filter(Attribute::isEnabled)
					.forEach(attr -> {
						sb.append(" ");
						sb.append(attr.getDisplayName());
						sb.append(":");
						sb.append(attr.getScaffoldType().toString());
					});
			theCode = sb.toString();
		}
		this.setCode(theCode);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.code);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Scaffold other = (Scaffold) obj;
		return Objects.equals(this.code, other.getCode());
	}
}
