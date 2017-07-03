package xyz.dskow.astah_rr_plugin.model.astahUtils;

import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import xyz.dskow.astah_rr_plugin.model.AliasMode;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper.findElementById;

public final class AstahHelper {

	private AstahHelper() {
	}

	public static void putTaggedValue(final IElement element, final String key, final String value) {
		final BasicModelEditor editor = AstahApiWrapper.getBasicModelEditor();
		final ITaggedValue tv = findTaggedValue(element, key);
		try {
			TransactionManager.beginTransaction();
			if (tv == null) {
				final ITaggedValue newTv = editor.createTaggedValue(element, "temp." + key, value);
				newTv.setKey(key);
			} else {
				tv.setValue(value);
			}
			TransactionManager.endTransaction();
		} catch (InvalidEditingException e) {
			TransactionManager.abortTransaction();
			throw new RuntimeException(e);
		}
	}

	public static ITaggedValue findTaggedValue(final IElement element, final String key) {
		final ITaggedValue[] tvs = element.getTaggedValues();
		if (tvs == null) {
			return null;
		}
		final Optional<ITaggedValue> taggedValue = Arrays.stream(tvs).filter(tv -> tv.getKey().equals(key)).findAny();
		return taggedValue.isPresent() ? taggedValue.get() : null;
	}

	public static void removeElementById(final String id) {
		final List<String> list = new ArrayList<>(1);
		list.add(id);
		removeElementsById(list);
	}

	public static void removeElementsById(final Collection<String> ids) {
		final BasicModelEditor editor = AstahApiWrapper.getBasicModelEditor();
		try {
			TransactionManager.beginTransaction();
			for (String id : ids) {
				final IElement el = findElementById(IElement.class, id);
				if (el != null) {
					editor.delete(el);
				}
			}
			TransactionManager.endTransaction();
		} catch (InvalidEditingException e) {
			TransactionManager.abortTransaction();
			throw new RuntimeException(e);
		}
	}

	public static void removeElements(final Collection<? extends IElement> elements) {
		final BasicModelEditor editor = AstahApiWrapper.getBasicModelEditor();
		try {
			TransactionManager.beginTransaction();
			for (IElement el : elements) {
				editor.delete(el);
			}
			TransactionManager.endTransaction();
		} catch (InvalidEditingException e) {
			TransactionManager.abortTransaction();
			throw new RuntimeException(e);
		}
	}

	public static String getName(final INamedElement named, final AliasMode aliasMode) {
		final String name;
		switch (aliasMode) {
			case NAME:
				name = named.getName();
				break;
			case ALIAS1:
				name = "".equals(named.getAlias1()) ? String.format("[%s]", named.getName()) : named.getAlias1();
				break;
			case ALIAS2:
				name = "".equals(named.getAlias2()) ? String.format("[%s]", named.getName()) : named.getAlias2();
				break;
			default:
				name = named.getName();
				break;
		}
		return name;
	}
}
