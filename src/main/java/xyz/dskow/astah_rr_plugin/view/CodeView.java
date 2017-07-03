package xyz.dskow.astah_rr_plugin.view;

import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import lombok.extern.slf4j.Slf4j;
import xyz.dskow.astah_rr_plugin.Activator;
import xyz.dskow.astah_rr_plugin.controller.CodeViewController;
import static xyz.dskow.astah_rr_plugin.controller.CodeViewController.COLUMN_ENABLED;
import static xyz.dskow.astah_rr_plugin.controller.CodeViewController.COLUMN_NAME;
import static xyz.dskow.astah_rr_plugin.controller.CodeViewController.COLUMN_TYPE;
import xyz.dskow.astah_rr_plugin.model.ScaffoldType;
import xyz.dskow.astah_rr_plugin.utils.SystemUtils;

@Slf4j
public class CodeView implements IPluginExtraTabView {

	private static final int FONT_SIZE_LARGE = 21;
	private static final int FONT_SIZE_MEDIUM = 15;

	private static final String TABLE_HEADER_FOREGROUND = "TableHeader.foreground";
	private static final String TABLE_HEADER_BACKGROUND = "TableHeader.background";
	private static final String TABLE_FOREGROUND = "Table.foreground";
	private static final String TABLE_BACKGROUND = "Table.background";
	private static final String TABLE_SELECTION_FOREGROUND = "Table.selectionForeground";
	private static final String TABLE_SELECTION_BACKGROUND = "Table.selectionBackground";

	private final JLabel className = new JLabel();

	private final JTextArea code = new JTextArea();

	private final CodeViewController controller = new CodeViewController();

	private Component component;

	public CodeView() {
	}

	@Override
	public String getTitle() {
		return "Scaffold View";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Component getComponent() {
		if (component == null) {
			component = createComponent();
			controller.handleViewActivated();
		}
		return component;
	}

	@Override
	public void addSelectionListener(ISelectionListener il) {
	}

	@Override
	public void activated() {
		this.controller.handleViewActivated();
	}

	@Override
	public void deactivated() {
	}

	private Component createComponent() {
		// 描画コンポーネントを初期化。
		initClassNameComponent();
		initCodeComponent();
		final JTable table = createTable();
		// レイアウトする。
		final JPanel top = new JPanel();
		top.setBackground(Color.WHITE);
		{
			final BoxLayout layout = new BoxLayout(top, BoxLayout.Y_AXIS);
			top.setLayout(layout);
			top.add(className);
			top.add(wrapScrollPane(table));
		}
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setTopComponent(top);
		splitPane.setBottomComponent(wrapScrollPane(code));
		splitPane.setDividerLocation(0.5d);
		// view、コントローラー、モデルを紐付ける。
		this.bind();
		// viewの検証。
		splitPane.validate();
		return splitPane;
	}

	private void initClassNameComponent() {
		this.className.setText(" ");
		this.className.setBackground(Color.WHITE);
		this.className.setAlignmentX(0f);
		final Font f = this.className.getFont();
		this.className.setFont(new Font(f.getFontName(), f.getStyle(), FONT_SIZE_LARGE));
	}

	private void initCodeComponent() {
		this.code.setEditable(false);
		this.code.setWrapStyleWord(true);
		this.code.setLineWrap(true);
		final Font f = this.code.getFont();
		this.code.setFont(new Font(f.getFontName(), f.getStyle(), FONT_SIZE_MEDIUM));
	}

	private JTable createTable() {
		final TableColumnModel columns = controller.getColumns();
		final JTable table = new JTable(controller.getTable(), columns, controller.getSelectionModel());
		final JTableHeader tableHeader = new JTableHeader(controller.getColumns());
		// テーブルの選択色を保持。
		final JCheckBox selectAllCheckBox = new JCheckBox();
		selectAllCheckBox.setOpaque(true);
		selectAllCheckBox.addChangeListener(controller.getSelectAllChangeListener());
		tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = -347184672129699492L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				final Component c;
				if (column == COLUMN_ENABLED) {
					c = selectAllCheckBox;
				} else {
					c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
				c.setForeground(SystemUtils.getColor(TABLE_HEADER_FOREGROUND));
				c.setBackground(SystemUtils.getColor(TABLE_HEADER_BACKGROUND));
				return c;
			}
		});
		tableHeader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				final int column = table.getColumnModel().getColumnIndexAtX(e.getX());
				if (column != COLUMN_ENABLED) {
					return;
				}
				selectAllCheckBox.setSelected(!selectAllCheckBox.isSelected());
			}
		});
		table.setTableHeader(tableHeader);
		// テーブル描画設定。
		table.setRowHeight(FONT_SIZE_MEDIUM + 8);
		// セル用描画設定。
		// フォントの設定。
		final Font f = table.getFont();
		final Font newFont = new Font(f.getFontName(), f.getStyle(), FONT_SIZE_MEDIUM);
		table.setFont(newFont);
		// 
		IntStream.range(0, columns.getColumnCount()).forEach(i -> {
			final TableColumn column = columns.getColumn(i);
			switch (i) {
				case COLUMN_ENABLED:
					// 表示用。
					column.setCellRenderer(createEnabledColumnCellRenderer(newFont));
					final JCheckBox checkBox = new JCheckBox();
					checkBox.addActionListener(controller.getEnabledChangeListener());
					column.setCellEditor(new DefaultCellEditor(checkBox));
					break;
				case COLUMN_NAME:
					// 表示用。
					column.setCellRenderer(createNameColumnCellRenderer(newFont));
					break;
				case COLUMN_TYPE:
					// 表示用。
					column.setCellRenderer(createScaffoldTypeColumnCellRenderer(newFont));
					// 編集用。
					final JComboBox<ScaffoldType> comboBox = new JComboBox<>();
					comboBox.setEditable(false);
					comboBox.setFont(new Font(f.getFontName(), f.getStyle(), FONT_SIZE_MEDIUM));
					Arrays.stream(ScaffoldType.values()).forEach(type -> {
						comboBox.addItem(type);
					});
					comboBox.addActionListener(controller.getScaffoldTypeChangeListener());
					column.setCellEditor(new DefaultCellEditor(comboBox));
					break;
				default:
					break;
			}
		});
		return table;
	}

	private JLabel createTableCellLabel(final Font font) {
		final JLabel label = new JLabel();
		label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		label.setFont(font);
		label.setOpaque(true);
		return label;
	}

	private TableCellRenderer createEnabledColumnCellRenderer(final Font font) {
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setOpaque(true);
		return (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columnIndex) -> {
			setCellRendererColor(checkBox, isSelected);
			checkBox.setSelected((boolean) value);
			return checkBox;
		};
	}

	private TableCellRenderer createNameColumnCellRenderer(final Font font) {
		final JLabel label = this.createTableCellLabel(font);
		return (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columnIndex) -> {
			setCellRendererColor(label, isSelected);
			label.setText((String) value);
			return label;
		};
	}

	private TableCellRenderer createScaffoldTypeColumnCellRenderer(final Font font) {
		final JLabel label = this.createTableCellLabel(font);
		return (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columnIndex) -> {
			setCellRendererColor(label, isSelected);
			label.setText(((ScaffoldType) value).toString());
			return label;
		};
	}

	private void setCellRendererColor(final Component c, final boolean isSelected) {
		if (isSelected) {
			c.setForeground(SystemUtils.getColor(TABLE_SELECTION_FOREGROUND));
			c.setBackground(SystemUtils.getColor(TABLE_SELECTION_BACKGROUND));
		} else {
			c.setForeground(SystemUtils.getColor(TABLE_FOREGROUND));
			c.setBackground(SystemUtils.getColor(TABLE_BACKGROUND));
		}
	}

	private JScrollPane wrapScrollPane(final JComponent component) {
		final JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setAlignmentX(0f);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	private void bind() {
		controller.setCodeViewApp(Activator.getApp());
		controller.setClassName(className);
		controller.setCode(code);
		controller.bindModel();
	}
}
