package xyz.dskow.astah_rr_plugin.controller;

import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.dskow.astah_rr_plugin.model.Attribute;
import xyz.dskow.astah_rr_plugin.model.CodeViewApp;
import xyz.dskow.astah_rr_plugin.model.Scaffold;
import xyz.dskow.astah_rr_plugin.model.ScaffoldType;

@Slf4j
public class CodeViewController {

	private static final String[] COLUMN_HEADER_NAMES = new String[]{"有効／無効", "名前", "タイプ"};

	public static final int COLUMN_ENABLED = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_TYPE = 2;

	@Setter
	private CodeViewApp codeViewApp;

	@Getter
	private final DefaultTableModel table = new DefaultTableModel() {
		private static final long serialVersionUID = 2793957061432039023L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == COLUMN_ENABLED || column == COLUMN_TYPE;
		}
	};

	@Getter
	private final TableColumnModel columns = new DefaultTableColumnModel();

	@Getter
	private final ListSelectionModel selectionModel = new DefaultListSelectionModel();

	@Setter
	private JLabel className;

	@Setter
	private JTextArea code;

	@Getter
	private final ActionListener scaffoldTypeChangeListener = e -> {
		if (!isValid()) {
			return;
		}
		final int selectedIndex = selectionModel.getMaxSelectionIndex();
		if (selectedIndex < 0) {
			return;
		}
		final JComboBox comboBox = (JComboBox) e.getSource();
		this.codeViewApp.getSelectedItem().getAttribute(selectedIndex).setScaffoldType((ScaffoldType) comboBox.getSelectedItem());
	};

	@Getter
	private final ActionListener enabledChangeListener = e -> {
		if (!isValid()) {
			return;
		}
		final int selectedIndex = selectionModel.getMaxSelectionIndex();
		if (selectedIndex < 0) {
			return;
		}
		final JCheckBox checkBox = (JCheckBox) e.getSource();
		this.codeViewApp.getSelectedItem().getAttribute(selectedIndex).setEnabled(checkBox.isSelected());
	};

	@Getter
	private final ChangeListener selectAllChangeListener = e -> {
		if (!isValid()) {
			return;
		}
		final JCheckBox checkBox = (JCheckBox) e.getSource();
		this.codeViewApp.getSelectedItem().getAttributes().stream().forEach(attr -> {
			attr.setEnabled(checkBox.isSelected());
		});
	};

	private boolean isValid() {
		if (this.codeViewApp == null) {
			return false;
		}
		return this.codeViewApp.getSelectedItem() != null;
	}

	public CodeViewController() {
		log.debug("initialize");
		initControl();
	}

	private void initControl() {
		// init.
		// 表示する値を関連づける
		IntStream.range(0, COLUMN_HEADER_NAMES.length).forEach(i -> {
			final TableColumn column = new TableColumn(i);
			column.setHeaderValue(COLUMN_HEADER_NAMES[i]);
			columns.addColumn(column);
			table.addColumn(COLUMN_HEADER_NAMES[i]);
		});
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void bindModel() {
		this.codeViewApp.addPropertyChangeListener("selectedItem", event -> {
			if (event.getOldValue() != null) {
				className.setText(" ");
				code.setText("");
				// 最後から削除しないとうまく消えない。
				for (int i = table.getRowCount() - 1; i >= 0; --i) {
					table.removeRow(i);
				}
			}
			if (event.getNewValue() != null) {
				final Scaffold newValue = (Scaffold) event.getNewValue();
				className.setText(newValue.getClassName());
				code.setText(newValue.getCode());
				final List<Attribute> attrs = newValue.getAttributes();
				IntStream.range(0, attrs.size()).forEach(i -> {
					table.addRow(this.createRowData(attrs.get(i)));
				});
			}
		});
	}

	public void handleViewActivated() {
		this.codeViewApp.unselect();
		this.codeViewApp.select();
	}

	private static Object[] createRowData(final Attribute attribute) {
		return new Object[]{attribute.isEnabled(), attribute.getDisplayName(), attribute.getScaffoldType()};
	}
}
