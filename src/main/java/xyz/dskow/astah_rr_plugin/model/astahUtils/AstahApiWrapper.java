package xyz.dskow.astah_rr_plugin.model.astahUtils;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ModelFinder;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IViewManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class AstahApiWrapper {

	private AstahApiWrapper() {
	}

	public static IViewManager getViewManager() {
		try {
			return AstahAPI.getAstahAPI().getViewManager();
		} catch (ClassNotFoundException | InvalidUsingException e) {
			throw new RuntimeException(e);
		}
	}

	public static ProjectAccessor getProjectAccessor() {
		try {
			return AstahAPI.getAstahAPI().getProjectAccessor();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static BasicModelEditor getBasicModelEditor() {
		try {
			return AstahAPI.getAstahAPI().getProjectAccessor().getModelEditorFactory().getBasicModelEditor();
		} catch (ClassNotFoundException | InvalidEditingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> findElement(final Class<T> cls, final ModelFinder finder) {
		try {
			final INamedElement[] namedEls = getProjectAccessor().findElements(finder);
			if (namedEls == null) {
				return new ArrayList<>(0);
			}
			return Arrays.stream(namedEls).map(cls::cast).collect(Collectors.toList());
		} catch (ProjectNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T findElementById(final Class<T> cls, final String id) {
		final List<INamedElement> els = findElement(INamedElement.class, (INamedElement ine) -> {
			return ine.getId().equals(id);
		});
		if (els.isEmpty()) {
			return null;
		}
		return cls.cast(els.get(0));
	}
}
