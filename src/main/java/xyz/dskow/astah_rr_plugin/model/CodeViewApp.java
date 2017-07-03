package xyz.dskow.astah_rr_plugin.model;

import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectEditUnit;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.view.IEntitySelectionEvent;
import com.change_vision.jude.api.inf.view.IEntitySelectionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static lombok.AccessLevel.PRIVATE;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static xyz.dskow.astah_rr_plugin.model.AliasMode.NAME;
import static xyz.dskow.astah_rr_plugin.model.AliasMode.parseAliasMode;
import xyz.dskow.astah_rr_plugin.model.Attribute.AstahAttribute;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper.findElement;
import static xyz.dskow.astah_rr_plugin.model.astahUtils.AstahHelper.removeElements;

@Slf4j
public class CodeViewApp extends ModelBase implements IEntitySelectionListener, ProjectEventListener {

	@Getter
	private Scaffold selectedItem;
	@Getter(PRIVATE)
	private AliasMode aliasMode = NAME;

	private final Map<String, AstahAttribute> prevAttributes = new HashMap<>(0);

	public CodeViewApp() {
		super();
	}

	private void setSelectedItem(final Scaffold item) {
		final Scaffold oldValue = this.selectedItem;
		if (isSameObject(oldValue, item)) {
			return;
		}
		this.selectedItem = item;
		getPropertyChangeSupport().firePropertyChange("selectedItem", oldValue, this.selectedItem);
	}

	private boolean setAliasMode(final String alias) {
		final AliasMode oldValue = this.aliasMode;
		final AliasMode newValue = parseAliasMode(alias);
		if (oldValue == newValue) {
			return false;
		}
		this.aliasMode = newValue;
		return true;
	}

	@Override
	public void entitySelectionChanged(IEntitySelectionEvent iese) {
		select();
	}

	@Override
	public void projectOpened(ProjectEvent pe) {
		isUpdateAliasName(null);
	}

	@Override
	public void projectClosed(ProjectEvent pe) {
		// 何もしない
	}

	@Override
	public void projectChanged(ProjectEvent pe) {
		final ProjectEditUnit[] units = pe.getProjectEditUnit();
		if (units == null || units.length == 0) {
			return;
		}
		// TaggedValueの変更を保持する。※処理A※で差分を判断するため。
		final Map<String, AstahAttribute> modifyAttrs = new HashMap<>(0);
		final Map<String, AstahAttribute> removeAttrs = new HashMap<>(0);
		try {
			this.putFilteredAttributes(units, modifyAttrs, removeAttrs);
			final String selectedItemId = getSelectedItem() == null ? null : getSelectedItem().getObjectId();
			// 別名表示が変更された場合は設定する。
			if (this.isUpdateAliasName(selectedItemId)) {
				return;
			}
			// 選択しているものがあり、削除対象であれば選択解除する。
			if (this.isRemovedSelectedClass(units, selectedItemId)) {
				return;
			}
			// ※処理A※
			// 属性のタグ付き値にScaffoldTypeがあると、それを優先して表示するので、
			// IClassの属性の型が変更された場合は、その属性が保持しているScaffoldTypeのタグ付き値をクリアする。
			this.updateAttributes(modifyAttrs);
			// 選択されているクラス、もしくはそのクラスの属性に変更がある場合、更新する。
			this.updateClass(units, selectedItemId);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			// 前回のタグ付き値をすべて把握しておかないと、※処理A※ での差分を判断することができない。
			modifyAttrs.values().stream().forEach(attr -> prevAttributes.put(attr.getId(), attr));
			removeAttrs.values().stream().forEach(attr -> prevAttributes.remove(attr.getId()));
		}
	}

	public void select() {
		if (!isValidPresentation()) {
			this.setSelectedItem(null);
			return;
		}
		final IPresentation[] ps = AstahApiWrapper.getViewManager().getDiagramViewManager().getSelectedPresentations();
		{
			final Optional<IClass> cls = Arrays.stream(ps)
					.filter(p -> p.getModel() != null && p.getModel() instanceof IClass)
					.map(p -> (IClass) p.getModel())
					.findAny();
			if (cls.isPresent()) {
				this.setSelectedItem(buildScaffold(cls.get(), this.getAliasMode()));
				return;
			}
		}
		{
			final Optional<IClass> cls = Arrays.stream(ps)
					.filter(p -> p.getModel() != null
					&& (p.getModel() instanceof IAttribute || p.getModel() instanceof IOperation))
					.map(p -> (IClass) p.getModel().getOwner())
					.findAny();
			if (cls.isPresent()) {
				this.setSelectedItem(buildScaffold(cls.get(), this.getAliasMode()));
				return;
			}
		}
		this.setSelectedItem(null);
	}

	public void unselect() {
		this.setSelectedItem(null);
	}

	private void putFilteredAttributes(final ProjectEditUnit[] units, final Map<String, AstahAttribute> updateAttrs, final Map<String, AstahAttribute> removeAttrs) {
		getFilteredAttributeStream(units, unit -> unit.getOperation() == ProjectEditUnit.ADD || unit.getOperation() == ProjectEditUnit.MODIFY)
				.forEach(attr -> {
					updateAttrs.put(attr.getId(), createAstahAttribute(attr));
				});
		getFilteredAttributeStream(units, unit -> unit.getOperation() == ProjectEditUnit.REMOVE)
				.forEach(attr -> {
					removeAttrs.put(attr.getId(), createAstahAttribute(attr));
				});
	}

	private IModel getProject() {
		try {
			return AstahApiWrapper.getProjectAccessor().getProject();
		} catch (ProjectNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isUpdateAliasName(final String selectedItemId) {
		final IModel project = getProject();
		if (project == null) {
			return false;
		}
		// jude.multi_language.indicatory, ALIAS2>NAME
		final Optional<ITaggedValue> aliasTv = Arrays.stream(project.getTaggedValues()).filter(tv -> tv.getKey().equals("jude.multi_language.indicatory")).findAny();
		if (!setAliasMode(aliasTv.isPresent() ? aliasTv.get().getValue() : "")) {
			return false;
		}
		if (selectedItemId == null) {
			return false;
		}
		setSelectedItem(buildScaffold(AstahApiWrapper.findElementById(IClass.class, selectedItemId), getAliasMode()));
		return true;
	}

	private boolean isRemovedSelectedClass(final ProjectEditUnit[] units, final String selectedItemId) {
		final Optional<IElement> element = Arrays.stream(units)
				.filter(unit -> unit.getOperation() == ProjectEditUnit.REMOVE && unit.getEntity() != null && unit.getEntity() instanceof IElement)
				.map(unit -> (IElement) unit.getEntity())
				.filter(c -> c.getId().equals(selectedItemId))
				.findAny();
		if (!element.isPresent()) {
			return false;
		}
		this.setSelectedItem(null);
		return true;
	}

	private void updateAttributes(final Map<String, AstahAttribute> modifyAttrs) {
		if (modifyAttrs.isEmpty()) {
			return;
		}
		final Set<String> removeIds = new HashSet<>(0);
		modifyAttrs.values().forEach(modifyAttr -> {
			prevAttributes.values().stream()
					.filter(prev -> !prev.getScaffoldTaggedValueId().equals("")
					&& prev.getId().equals(modifyAttr.getId()) && !prev.getTypeExpression().equals(modifyAttr.getTypeExpression()))
					.forEach(prev -> {
						removeIds.add(prev.getScaffoldTaggedValueId());
					});
		});
		if (removeIds.isEmpty()) {
			return;
		}
		// ITaggedValueはfindElementsで検索できないので、Attributeから検索している。
		final List<IAttribute> attrs = findElement(IAttribute.class, el -> {
			return el instanceof IAttribute;
		});
		final Map<String, ITaggedValue> tvs = attrs.stream().flatMap(attr -> Arrays.stream(attr.getTaggedValues()))
				.filter(tv -> removeIds.contains(tv.getId()))
				.collect(Collectors.toMap(tv -> tv.getKey(), tv -> tv));
		removeElements(tvs.values());
	}

	private void updateClass(final ProjectEditUnit[] units, final String selectedItemId) {
		final List<IElement> els = Arrays.stream(units)
				.filter(unit -> (unit.getOperation() == ProjectEditUnit.ADD || unit.getOperation() == ProjectEditUnit.MODIFY)
				&& unit.getEntity() != null && unit.getEntity() instanceof IElement)
				.map(unit -> (IElement) unit.getEntity())
				.collect(Collectors.toList());
		IClass cls = null;
		for (IElement el : els) {
			if (el instanceof IClass && el.getId().equals(selectedItemId)) {
				cls = (IClass) el;
				break;
			}
			if (el instanceof IAttribute && el.getOwner() != null && el.getOwner().getId().equals(selectedItemId)) {
				cls = (IClass) el.getOwner();
				break;
			}
		}
		if (cls == null) {
			return;
		}
		this.setSelectedItem(buildScaffold(cls, this.getAliasMode()));
	}

	private static boolean isValidPresentation() {
		final IDiagram diagram = AstahApiWrapper.getViewManager().getDiagramViewManager().getCurrentDiagram();
		if (diagram == null || !(diagram instanceof IClassDiagram)) {
			return false;
		}
		final IPresentation[] ps = AstahApiWrapper.getViewManager().getDiagramViewManager().getSelectedPresentations();
		return !(ps == null || ps.length == 0);
	}

	private static Scaffold buildScaffold(final IClass cls, final AliasMode aliasMode) {
		if (cls == null) {
			return null;
		}
		return new Scaffold(cls.getId(), aliasMode);
	}

	private static Stream<IAttribute> getFilteredAttributeStream(final ProjectEditUnit[] units, final Predicate<ProjectEditUnit> predicate) {
		return Arrays.stream(units)
				.filter(predicate)
				.filter(unit -> unit.getEntity() != null && unit.getEntity() instanceof IAttribute)
				.map(unit -> (IAttribute) unit.getEntity());
	}

	private static AstahAttribute createAstahAttribute(final IAttribute attr) {
		return new AstahAttribute(attr);
	}
}
