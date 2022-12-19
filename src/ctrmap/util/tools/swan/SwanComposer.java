package ctrmap.util.tools.swan;

import xstandard.fs.FSFile;
import xstandard.gui.ArrayListModel;
import xstandard.gui.DialogUtils;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.PlusMinusButtonSet;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import xstandard.gui.file.XFileDialog;
import xstandard.text.StringEx;
import xstandard.util.ListenableList;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class SwanComposer extends javax.swing.JFrame {

	private final ArrayListModel<SwanSourceFile> sourceFileListModel = new ArrayListModel<>();
	private final ArrayListModel<String> includesListModel = new ArrayListModel<>();
	private final ArrayListModel<SwanTypedef> typedefListModel = new ArrayListModel<>();
	private final ArrayListModel<SwanTypedef> gvarListModel = new ArrayListModel<>();
	private final ArrayListModel<SwanEnum> enumListModel = new ArrayListModel<>();
	private final ArrayListModel<SwanStructure> structListModel = new ArrayListModel<>();
	private final ArrayListModel<SwanMethod> methodsListModel = new ArrayListModel<>();
	private boolean pauseChanges = false;

	private SwanDB db = new SwanDB();

	private SwanEnum currentEnum;
	private SwanStructure currentStruct;

	private int keyboardUsageStreak = 0;

	/**
	 * Creates new form SwanComposer
	 */
	public SwanComposer() {
		initComponents();
		chainMethodAddHint.setVisible(false);
		keyboardUsageComboLabel.setVisible(false);
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				keyboardUsageStreak = 0;
				SwingUtilities.invokeLater((() -> {
					keyboardUsageComboLabel.setVisible(false);
				}));
			}
		}, AWTEvent.MOUSE_EVENT_MASK);

		sourceFileList.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				loadSourceFile();
			}
		});

		btnAddRemoveSourceFile.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				String path = JOptionPane.showInputDialog(SwanComposer.this, "Enter the path for the source file:");
				if (path != null) {
					if (db.findFileByPath(path) != null) {
						DialogUtils.showErrorMessage(SwanComposer.this, "Duplicate source file", "A source file under this path already exists.");
					} else {
						SwanSourceFile f = new SwanSourceFile(path);
						db.sourceFiles.add(f);
						db.sortSrcFiles();
						sourceFileList.setSelectedValue(f, true);
						loadSourceFile();
					}
				}
			}

			@Override
			public void minusClicked() {
				SwanSourceFile file = sourceFileList.getSelectedValue();
				if (file != null) {
					if (DialogUtils.showYesNoDialog(SwanComposer.this, "Are you sure?", "Source file \"" + file + "\" will be permanently removed.")) {
						db.sourceFiles.remove(file);
						FSFile ymlFile = db.getYaml().getFile();
						if (ymlFile != null) {
							FSFile toDelete = ymlFile.getParent().getChild(file.path);
							if (toDelete.exists()) {
								toDelete.delete();
							}
						}
					}
				}
			}
		});

		includesList.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				loadIncludeEntry();
			}
		});

		includeComboBox.addListener((selectedItem) -> {
			if (pauseChanges) {
				return;
			}
			int index = includesList.getSelectedIndex();
			if (index != -1) {
				SwanSourceFile f = sourceFileList.getSelectedValue();
				if (f != null) {
					Object val = includeComboBox.getSelectedItem();
					if (val != null) {
						f.includes.setModify(index, String.valueOf(val));
					}
				}
			}
		});

		btnAddRemoveInclude.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				SwanSourceFile f = sourceFileList.getSelectedValue();
				if (f != null) {
					String newVal = "<not yet assigned>";
					if (!f.includes.contains(newVal)) {
						f.includes.add(newVal);
						includesList.setSelectedValue(newVal, true);
					}
				}
			}

			@Override
			public void minusClicked() {
				String sel = includesList.getSelectedValue();
				if (sel != null) {
					sourceFileList.getSelectedValue().includes.remove(sel);
				}
			}
		});

		setupSimpleTypedefEditor(typedefsList, typedefCppName, btnAddRemoveTypedef, typedefTextArea, () -> {
			SwanSourceFile f = sourceFileList.getSelectedValue();
			return f != null ? f.typedefs : null;
		}, "typedef void Type;");
		setupSimpleTypedefEditor(gvarList, null, btnAddRemoveGVar, gvarDefArea, () -> {
			SwanSourceFile f = sourceFileList.getSelectedValue();
			return f != null ? f.gvars : null;
		}, "void* g_Var;");

		enumList.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				loadEnumEntry();
			}
		});

		enumTextArea.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (currentEnum != null) {
					String oldName = currentEnum.definition.getTypeName();
					currentEnum.definition.content = enumTextArea.getText();
					if (!Objects.equals(oldName, currentEnum.definition.getTypeName())) {
						ListenableList<SwanEnum> enums = sourceFileList.getSelectedValue().enums;
						enums.setModify(enums.indexOf(currentEnum), currentEnum);
					}
				}
			}
		});

		btnAddRemoveEnum.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				SwanSourceFile sf = sourceFileList.getSelectedValue();
				if (sf != null) {
					SwanEnum newEnum = new SwanEnum();
					newEnum.definition = new SwanTypedef("enum Enum {\n};");
					sf.enums.add(newEnum);
					enumList.setSelectedValue(newEnum, true);
				}
			}

			@Override
			public void minusClicked() {
				SwanSourceFile sf = sourceFileList.getSelectedValue();
				if (sf != null) {
					sf.enums.remove(enumList.getSelectedValue());
				}
			}
		});

		enumCppName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (currentEnum != null) {
					String val = ComponentUtils.getDocTextFromField(enumCppName);
					currentEnum.definition.cppName = val == null || val.isEmpty() ? null : val;
				}
			}
		});

		typedefCppName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				SwanTypedef td = typedefsList.getSelectedValue();
				if (td != null) {
					String val = ComponentUtils.getDocTextFromField(typedefCppName);
					td.cppName = val == null || val.isEmpty() ? null : val;
				}
			}
		});

		allFunctionsTextArea.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				saveFunctions();
			}
		});

		structList.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				loadStructEntry();
			}
		});

		structDefArea.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (currentStruct != null) {
					String oldName = currentStruct.definition.getTypeName();
					currentStruct.definition.content = structDefArea.getText();
					if (!Objects.equals(oldName, currentStruct.definition.getTypeName())) {
						ListenableList<SwanStructure> structs = sourceFileList.getSelectedValue().structures;
						structs.setModify(structs.indexOf(currentStruct), currentStruct);
					}
				}
			}
		});

		btnAddRemoveStruct.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				SwanSourceFile sf = sourceFileList.getSelectedValue();
				if (sf != null) {
					SwanStructure newStruct = new SwanStructure();
					newStruct.definition = new SwanTypedef("struct Struct {\n};");
					sf.structures.add(newStruct);
					structList.setSelectedValue(newStruct, true);
				}
			}

			@Override
			public void minusClicked() {
				SwanSourceFile sf = sourceFileList.getSelectedValue();
				if (sf != null) {
					SwanStructure s = structList.getSelectedValue();
					if (s != null) {
						if (DialogUtils.showYesNoDialog(SwanComposer.this, "Are you sure?", "The structure \"" + s + " will be permanently removed from the database.")) {
							sf.structures.remove(structList.getSelectedValue());
						}
					}
				}
			}
		});

		structCppMethodsList.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				loadMethodEntry();
			}
		});

		structCppName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (currentStruct != null) {
					String val = ComponentUtils.getDocTextFromField(structCppName);
					currentStruct.definition.cppName = val == null || val.isEmpty() ? null : val;
				}
			}
		});

		structCppMethodName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				SwanMethod m = structCppMethodsList.getSelectedValue();
				if (m != null) {
					String val = ComponentUtils.getDocTextFromField(structCppMethodName);
					m.cppName = val == null || val.isEmpty() ? null : val;
					currentStruct.methods.setModify(currentStruct.methods.indexOf(m), m);
				}
			}
		});

		structCppMethodParamSwizzle.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				SwanMethod m = structCppMethodsList.getSelectedValue();
				if (m != null) {
					String val = ComponentUtils.getDocTextFromField(structCppMethodParamSwizzle);
					m.paramSwizzle = val == null || val.isEmpty() ? null : val;
				}
			}
		});

		structCppMethodTarget.addListener((selectedItem) -> {
			SwanMethod m = structCppMethodsList.getSelectedValue();
			if (m != null) {
				m.cName = (String) structCppMethodTarget.getSelectedItem();
				setCppMethodTargetArgsPreview();
			}
		});

		structCppMethodIsStatic.addChangeListener((e) -> {
			SwanMethod m = structCppMethodsList.getSelectedValue();
			if (m != null) {
				m.isStatic = structCppMethodIsStatic.isSelected();
			}
		});

		btnAddRemoveStructCppMethod.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				addMethod();
			}

			@Override
			public void minusClicked() {
				SwanMethod m = structCppMethodsList.getSelectedValue();
				if (m != null && currentStruct != null) {
					currentStruct.methods.remove(m);
				}
			}
		});

		macroArea.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				SwanSourceFile f = sourceFileList.getSelectedValue();
				if (f != null) {
					f.macros = macroArea.getText();
				}
			}
		});

		openDB(db);
	}

	private static void setupSimpleTypedefEditor(JList<SwanTypedef> list, JTextField cppNameField, PlusMinusButtonSet buttons, RSyntaxTextArea textArea, Supplier<ListenableList<SwanTypedef>> getListFunc, String defaultDef) {
		list.addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting()) {
				SwanTypedef td = list.getSelectedValue();
				if (td != null) {
					textArea.setText(td.content);
					if (cppNameField != null) {
						cppNameField.setText(td.cppName);
					}
				} else {
					textArea.setText(null);
					if (cppNameField != null) {
						cppNameField.setText(null);
					}
				}
			}
		});

		textArea.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				SwanTypedef td = list.getSelectedValue();
				if (td != null) {
					String oldName = td.getTypeName();
					td.content = textArea.getText().replace("extern ", ""); //not applicable to typedefs, but does no harm
					if (!Objects.equals(oldName, td.getTypeName())) {
						ListenableList<SwanTypedef> typedefs = getListFunc.get();
						typedefs.setModify(typedefs.indexOf(td), td);
					}
				}
			}
		});

		buttons.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				List<SwanTypedef> l = getListFunc.get();
				if (l != null) {
					SwanTypedef newTypedef = new SwanTypedef(defaultDef);
					l.add(newTypedef);
					list.setSelectedValue(newTypedef, true);
				}
			}

			@Override
			public void minusClicked() {
				List<SwanTypedef> l = getListFunc.get();
				if (l != null) {
					l.remove(list.getSelectedValue());
				}
			}
		});
	}

	public void openDB(SwanDB db) {
		this.db = db;
		currentEnum = null;
		currentStruct = null;

		sourceFileListModel.setList(db.sourceFiles);
		includeComboBox.loadValuesListenable(db.sourceFiles);
	}

	private void loadSourceFile() {
		SwanSourceFile file = sourceFileList.getSelectedValue();
		if (file != null) {
			includesListModel.setList(file.includes);
			typedefListModel.setList(file.typedefs);
			gvarListModel.setList(file.gvars);
			structListModel.setList(file.structures);
			enumListModel.setList(file.enums);
			allFunctionsTextArea.setText(getAllFunctionsFromSrcFile(file));
			macroArea.setText(file.macros);
			updateMethodSelection(file.functions);
			try {
				allFunctionsTextArea.setCaretPosition(0);
			} catch (Exception ex) {

			}
		} else {
			includesListModel.setList(new ListenableList<>());
			typedefListModel.setList(new ListenableList<>());
			gvarListModel.setList(new ListenableList<>());
			structListModel.setList(new ListenableList<>());
			enumListModel.setList(new ListenableList<>());
			methodsListModel.setList(new ListenableList<>());
			allFunctionsTextArea.setText(null);
			macroArea.setText(null);
		}
	}

	private void loadIncludeEntry() {
		pauseChanges = true;
		String incValue = includesList.getSelectedValue();
		if (incValue != null && db != null) {
			includeComboBox.setSelectedItem(db.findFileByPath(incValue));
		} else {
			includeComboBox.setSelectedItem(null);
		}
		pauseChanges = false;
	}

	private void loadEnumEntry() {
		currentEnum = null;
		SwanEnum enm = enumList.getSelectedValue();
		if (enm != null) {
			enumTextArea.setText(enm.definition.content);
			enumCppName.setText(enm.definition.cppName);
			enumDefineFlagOps.setSelected(enm.defineFlagOps);
		} else {
			enumTextArea.setText(null);
			enumCppName.setText(null);
			enumDefineFlagOps.setSelected(false);
		}
		currentEnum = enm;
	}

	private void loadStructEntry() {
		currentStruct = null;
		SwanStructure struct = structList.getSelectedValue();
		if (struct != null) {
			structDefArea.setText(struct.definition.content);
			structCppName.setText(struct.definition.cppName);
			methodsListModel.setList(struct.methods);
		} else {
			structDefArea.setText(null);
			structCppName.setText(null);
			methodsListModel.setList(new ListenableList<>());
		}
		currentStruct = struct;
	}

	private void loadMethodEntry() {
		SwanMethod method = structCppMethodsList.getSelectedValue();
		if (method != null) {
			structCppMethodName.setText(method.cppName);
			structCppMethodTarget.setSelectedItem(method.cName);
			setCppMethodTargetArgsPreview();
			structCppMethodIsStatic.setSelected(method.isStatic);
			structCppMethodParamSwizzle.setText(method.paramSwizzle);
		} else {
			structCppMethodName.setText(null);
			structCppMethodTarget.setSelectedItem(null);
			structCppMethodTargetArgs.setText("<N/A>");
			structCppMethodIsStatic.setSelected(false);
			structCppMethodParamSwizzle.setText(null);
		}
	}

	private void setCppMethodTargetArgsPreview() {
		SwanMethod method = structCppMethodsList.getSelectedValue();
		SwanTypedef tgtFunc;
		if (method != null && (tgtFunc = sourceFileList.getSelectedValue().getFuncByName(method.cName)) != null) {
			structCppMethodTargetArgs.setText(tgtFunc.content.substring(tgtFunc.content.indexOf(method.cName) + method.cName.length()).trim());
		} else {
			structCppMethodTargetArgs.setText("<N/A>");
		}
	}

	private void saveFunctions() {
		SwanSourceFile sf = sourceFileList.getSelectedValue();
		if (sf != null) {
			List<SwanTypedef> src = getFunctionsFromText(allFunctionsTextArea.getText());
			Object selMethodBackup = structCppMethodTarget.getSelectedItem();
			sf.functions.clear();
			sf.functions.addAll(src);
			updateMethodSelection(src);
			structCppMethodTarget.setSelectedItem((String) selMethodBackup);
		}
	}

	private void addMethod() {
		if (currentStruct != null) {
			SwanMethod m = new SwanMethod();
			m.cppName = "Method";
			currentStruct.methods.add(m);
			structCppMethodsList.setSelectedValue(m, true);
			structCppMethodName.requestFocus();
			structCppMethodName.setSelectionStart(0);
			structCppMethodName.setSelectionEnd(m.cppName.length());
		}
	}

	private void updateMethodSelection(List<SwanTypedef> src) {
		List<String> names = new ArrayList<>();
		for (SwanTypedef t : src) {
			names.add(t.getTypeName());
		}
		structCppMethodTarget.loadValues(names);
	}

	private static String removeDupedGaps(String str) {
		StringBuilder out = new StringBuilder();
		boolean gap = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (!gap) {
					gap = true;
					out.append(" ");
				}
			} else {
				gap = false;
				out.append(c);
			}
		}
		return out.toString();
	}

	private static List<SwanTypedef> getFunctionsFromText(String text) {
		String[] lines = StringEx.splitOnecharFastNoBlank(text, '\n');
		List<SwanTypedef> funcs = new ArrayList<>();
		for (String line : lines) {
			line = line.replace("extern ", "");
			if (line.endsWith(";")) {
				line = line.substring(0, line.length() - 1);
			}
			funcs.add(new SwanTypedef(removeDupedGaps(line.trim())));
		}
		return funcs;
	}

	private static String getAllFunctionsFromSrcFile(SwanSourceFile srcFile) {
		StringBuilder bld = new StringBuilder();
		int ppIdx = SwanTypedef.getPrettyPrintFirstNameIdx(srcFile.functions, false);
		for (SwanTypedef func : srcFile.functions) {
			String pp = func.prettyPrint(ppIdx, false);
			bld.append(pp);
			if (!(pp.endsWith(";") || pp.endsWith("}"))) {
				bld.append(";");
			}
			bld.append("\n");
		}
		return bld.toString();
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceFileSP = new javax.swing.JScrollPane();
        sourceFileList = new javax.swing.JList<>();
        editors = new javax.swing.JTabbedPane();
        includeEditor = new javax.swing.JPanel();
        includesSP = new javax.swing.JScrollPane();
        includesList = new javax.swing.JList<>();
        includeComboBox = new xstandard.gui.components.combobox.ACComboBox<>();
        btnAddRemoveInclude = new xstandard.gui.components.PlusMinusButtonSet();
        includeListLabel = new javax.swing.JLabel();
        typedefEditor = new javax.swing.JPanel();
        typedefTextArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        typedefsSP = new javax.swing.JScrollPane();
        typedefsList = new javax.swing.JList<>();
        btnAddRemoveTypedef = new xstandard.gui.components.PlusMinusButtonSet();
        typedefListLabel = new javax.swing.JLabel();
        typedefCppNameLabel = new javax.swing.JLabel();
        typedefCppName = new javax.swing.JTextField();
        gvarEditor = new javax.swing.JPanel();
        gvarDefArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        gvarListSP = new javax.swing.JScrollPane();
        gvarList = new javax.swing.JList<>();
        btnAddRemoveGVar = new xstandard.gui.components.PlusMinusButtonSet();
        gvarListLabel = new javax.swing.JLabel();
        enumEditor = new javax.swing.JPanel();
        enumListSP = new javax.swing.JScrollPane();
        enumList = new javax.swing.JList<>();
        btnAddRemoveEnum = new xstandard.gui.components.PlusMinusButtonSet();
        enumTextAreaSP = new org.fife.ui.rtextarea.RTextScrollPane();
        enumTextArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        enumCppNameLabel = new javax.swing.JLabel();
        enumCppName = new javax.swing.JTextField();
        enumListLabel = new javax.swing.JLabel();
        enumDefineFlagOps = new javax.swing.JCheckBox();
        functionsEditor = new javax.swing.JPanel();
        allFunctionsSP = new org.fife.ui.rtextarea.RTextScrollPane();
        allFunctionsTextArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        structEditor = new javax.swing.JPanel();
        structListSP = new javax.swing.JScrollPane();
        structList = new javax.swing.JList<>();
        btnAddRemoveStruct = new xstandard.gui.components.PlusMinusButtonSet();
        structEditorTabs = new javax.swing.JTabbedPane();
        structDefSP = new org.fife.ui.rtextarea.RTextScrollPane();
        structDefArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        structCppEditor = new javax.swing.JPanel();
        structCppNameLabel = new javax.swing.JLabel();
        structCppName = new javax.swing.JTextField();
        structCppMethodsEditor = new javax.swing.JPanel();
        structCppMethodsListSP = new javax.swing.JScrollPane();
        structCppMethodsList = new javax.swing.JList<>();
        btnAddRemoveStructCppMethod = new xstandard.gui.components.PlusMinusButtonSet();
        structCppMethodNameLabel = new javax.swing.JLabel();
        structCppMethodName = new javax.swing.JTextField();
        structCppMethodTargetLabel = new javax.swing.JLabel();
        structCppMethodTarget = new xstandard.gui.components.combobox.ACComboBox<>();
        structCppMethodIsStatic = new javax.swing.JCheckBox();
        methodListLabel = new javax.swing.JLabel();
        chainMethodAdd = new javax.swing.JButton();
        chainMethodAddHint = new javax.swing.JLabel();
        keyboardUsageComboLabel = new javax.swing.JLabel();
        structCppMethodParamSwizzle = new javax.swing.JTextField();
        structCppMethodParamSwizzleLabel = new javax.swing.JLabel();
        structCppMethodTargetArgs = new javax.swing.JLabel();
        structListLabel = new javax.swing.JLabel();
        btnMoveStructDown = new javax.swing.JButton();
        btnMoveStructUp = new javax.swing.JButton();
        macroAreaSP = new org.fife.ui.rtextarea.RTextScrollPane();
        macroArea = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        btnAddRemoveSourceFile = new xstandard.gui.components.PlusMinusButtonSet();
        btnRenameSrcFile = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        btnDBOpen = new javax.swing.JMenuItem();
        btnDBSave = new javax.swing.JMenuItem();
        btnGenSources = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SwanComposer");
        setLocationByPlatform(true);

        sourceFileList.setModel(sourceFileListModel);
        sourceFileSP.setViewportView(sourceFileList);

        includesList.setModel(includesListModel   );
        includesSP.setViewportView(includesList);

        includeComboBox.setACMode(xstandard.gui.components.combobox.ComboBoxExInternal.ACMode.CONTAINS);

        includeListLabel.setText("List");

        javax.swing.GroupLayout includeEditorLayout = new javax.swing.GroupLayout(includeEditor);
        includeEditor.setLayout(includeEditorLayout);
        includeEditorLayout.setHorizontalGroup(
            includeEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(includeEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(includeEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(includeEditorLayout.createSequentialGroup()
                        .addComponent(includeListLabel)
                        .addGap(143, 143, 143)
                        .addComponent(btnAddRemoveInclude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(includeEditorLayout.createSequentialGroup()
                        .addComponent(includesSP, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(includeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(280, Short.MAX_VALUE))
        );
        includeEditorLayout.setVerticalGroup(
            includeEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(includeEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(includeEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddRemoveInclude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(includeListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(includeEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(includeEditorLayout.createSequentialGroup()
                        .addComponent(includeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(includesSP, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                .addContainerGap())
        );

        editors.addTab("Includes", includeEditor);

        typedefTextArea.setColumns(20);
        typedefTextArea.setRows(5);
        typedefTextArea.setSyntaxEditingStyle("text/c");

        typedefsList.setModel(typedefListModel   );
        typedefsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        typedefsSP.setViewportView(typedefsList);

        typedefListLabel.setText("List");

        typedefCppNameLabel.setText("C++ name:");

        javax.swing.GroupLayout typedefEditorLayout = new javax.swing.GroupLayout(typedefEditor);
        typedefEditor.setLayout(typedefEditorLayout);
        typedefEditorLayout.setHorizontalGroup(
            typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typedefEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(typedefEditorLayout.createSequentialGroup()
                        .addComponent(typedefListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddRemoveTypedef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(typedefsSP, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typedefTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addGroup(typedefEditorLayout.createSequentialGroup()
                        .addComponent(typedefCppNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typedefCppName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        typedefEditorLayout.setVerticalGroup(
            typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typedefEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnAddRemoveTypedef, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(typedefListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(typedefCppName)
                        .addComponent(typedefCppNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typedefEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typedefsSP, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                    .addGroup(typedefEditorLayout.createSequentialGroup()
                        .addComponent(typedefTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        editors.addTab("Basic typedefs", typedefEditor);

        gvarDefArea.setColumns(20);
        gvarDefArea.setRows(5);
        gvarDefArea.setSyntaxEditingStyle("text/c");

        gvarList.setModel(gvarListModel    );
        gvarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gvarListSP.setViewportView(gvarList);

        gvarListLabel.setText("List");

        javax.swing.GroupLayout gvarEditorLayout = new javax.swing.GroupLayout(gvarEditor);
        gvarEditor.setLayout(gvarEditorLayout);
        gvarEditorLayout.setHorizontalGroup(
            gvarEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gvarEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gvarEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(gvarEditorLayout.createSequentialGroup()
                        .addComponent(gvarListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddRemoveGVar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(gvarListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gvarDefArea, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                .addContainerGap())
        );
        gvarEditorLayout.setVerticalGroup(
            gvarEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gvarEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gvarEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddRemoveGVar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gvarListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gvarEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gvarEditorLayout.createSequentialGroup()
                        .addComponent(gvarDefArea, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(gvarListSP, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                .addContainerGap())
        );

        editors.addTab("Global variables", gvarEditor);

        enumList.setModel(enumListModel);
        enumList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        enumListSP.setViewportView(enumList);

        enumTextArea.setColumns(20);
        enumTextArea.setRows(5);
        enumTextArea.setSyntaxEditingStyle("text/c");
        enumTextAreaSP.setViewportView(enumTextArea);

        enumCppNameLabel.setText("C++ name:");

        enumListLabel.setText("List");

        enumDefineFlagOps.setText("Define flag operators");
        enumDefineFlagOps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enumDefineFlagOpsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout enumEditorLayout = new javax.swing.GroupLayout(enumEditor);
        enumEditor.setLayout(enumEditorLayout);
        enumEditorLayout.setHorizontalGroup(
            enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enumEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(enumListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(enumEditorLayout.createSequentialGroup()
                        .addComponent(enumListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddRemoveEnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(enumEditorLayout.createSequentialGroup()
                        .addComponent(enumCppNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(enumCppName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(enumDefineFlagOps)
                        .addGap(0, 237, Short.MAX_VALUE))
                    .addComponent(enumTextAreaSP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        enumEditorLayout.setVerticalGroup(
            enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enumEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(enumCppName)
                        .addComponent(enumDefineFlagOps))
                    .addComponent(enumCppNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddRemoveEnum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enumListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(enumEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enumListSP, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                    .addComponent(enumTextAreaSP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        editors.addTab("Enums", enumEditor);

        allFunctionsTextArea.setColumns(20);
        allFunctionsTextArea.setRows(5);
        allFunctionsTextArea.setSyntaxEditingStyle("text/c");
        allFunctionsSP.setViewportView(allFunctionsTextArea);

        javax.swing.GroupLayout functionsEditorLayout = new javax.swing.GroupLayout(functionsEditor);
        functionsEditor.setLayout(functionsEditorLayout);
        functionsEditorLayout.setHorizontalGroup(
            functionsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 851, Short.MAX_VALUE)
            .addGroup(functionsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(functionsEditorLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(allFunctionsSP, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        functionsEditorLayout.setVerticalGroup(
            functionsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 509, Short.MAX_VALUE)
            .addGroup(functionsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(functionsEditorLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(allFunctionsSP, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        editors.addTab("Functions", functionsEditor);

        structList.setModel(structListModel);
        structListSP.setViewportView(structList);

        structDefArea.setColumns(20);
        structDefArea.setRows(5);
        structDefArea.setSyntaxEditingStyle("text/c");
        structDefSP.setViewportView(structDefArea);

        structEditorTabs.addTab("Definition", structDefSP);

        structCppNameLabel.setText("Namespace + name:");

        structCppMethodsEditor.setBorder(javax.swing.BorderFactory.createTitledBorder("Methods"));

        structCppMethodsList.setModel(methodsListModel   );
        structCppMethodsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        structCppMethodsListSP.setViewportView(structCppMethodsList);

        structCppMethodNameLabel.setText("Name:");

        structCppMethodTargetLabel.setText("Mapped C method:");

        structCppMethodTarget.setACMode(xstandard.gui.components.combobox.ComboBoxExInternal.ACMode.CONTAINS);

        structCppMethodIsStatic.setText("Static");

        methodListLabel.setText("List");

        chainMethodAdd.setText("Add another!");
        chainMethodAdd.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        chainMethodAdd.setContentAreaFilled(false);
        chainMethodAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chainMethodAddMouseClicked(evt);
            }
        });
        chainMethodAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chainMethodAddActionPerformed(evt);
            }
        });

        chainMethodAddHint.setForeground(new java.awt.Color(0, 153, 0));
        chainMethodAddHint.setText("(Hint: hit <Tab> and <Enter> for mouse-less!)");

        keyboardUsageComboLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        keyboardUsageComboLabel.setText("hidden");

        structCppMethodParamSwizzleLabel.setText("Parameter swizzle");

        structCppMethodTargetArgs.setText("<N/A>");

        javax.swing.GroupLayout structCppMethodsEditorLayout = new javax.swing.GroupLayout(structCppMethodsEditor);
        structCppMethodsEditor.setLayout(structCppMethodsEditorLayout);
        structCppMethodsEditorLayout.setHorizontalGroup(
            structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                        .addComponent(methodListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddRemoveStructCppMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(structCppMethodsListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(structCppMethodNameLabel, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(structCppMethodTargetLabel, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(structCppMethodTarget, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                        .addComponent(structCppMethodName, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(structCppMethodIsStatic)
                    .addComponent(chainMethodAddHint, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                        .addComponent(chainMethodAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keyboardUsageComboLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(structCppMethodParamSwizzle, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(structCppMethodParamSwizzleLabel, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(structCppMethodTargetArgs, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
                .addContainerGap())
        );
        structCppMethodsEditorLayout.setVerticalGroup(
            structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                        .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(methodListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAddRemoveStructCppMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodsListSP, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                    .addGroup(structCppMethodsEditorLayout.createSequentialGroup()
                        .addComponent(structCppMethodNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodTargetLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodTargetArgs)
                        .addGap(9, 9, 9)
                        .addComponent(structCppMethodIsStatic)
                        .addGap(3, 3, 3)
                        .addGroup(structCppMethodsEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chainMethodAdd)
                            .addComponent(keyboardUsageComboLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chainMethodAddHint)
                        .addGap(32, 32, 32)
                        .addComponent(structCppMethodParamSwizzleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structCppMethodParamSwizzle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout structCppEditorLayout = new javax.swing.GroupLayout(structCppEditor);
        structCppEditor.setLayout(structCppEditorLayout);
        structCppEditorLayout.setHorizontalGroup(
            structCppEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structCppEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(structCppEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(structCppMethodsEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(structCppEditorLayout.createSequentialGroup()
                        .addGroup(structCppEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(structCppNameLabel)
                            .addComponent(structCppName, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        structCppEditorLayout.setVerticalGroup(
            structCppEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structCppEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(structCppNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(structCppName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(structCppMethodsEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        structEditorTabs.addTab("C++", structCppEditor);

        structListLabel.setText("List");

        btnMoveStructDown.setText("▼");
        btnMoveStructDown.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnMoveStructDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveStructDownActionPerformed(evt);
            }
        });

        btnMoveStructUp.setText("▲");
        btnMoveStructUp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnMoveStructUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveStructUpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout structEditorLayout = new javax.swing.GroupLayout(structEditor);
        structEditor.setLayout(structEditorLayout);
        structEditorLayout.setHorizontalGroup(
            structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structEditorLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(structEditorLayout.createSequentialGroup()
                        .addComponent(structListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnMoveStructDown, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMoveStructUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddRemoveStruct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(structListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(structEditorTabs)
                .addContainerGap())
        );
        structEditorLayout.setVerticalGroup(
            structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(structEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(structEditorLayout.createSequentialGroup()
                        .addGroup(structEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAddRemoveStruct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, structEditorLayout.createSequentialGroup()
                                .addComponent(btnMoveStructUp, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(btnMoveStructDown, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(structListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(structListSP))
                    .addComponent(structEditorTabs))
                .addContainerGap())
        );

        editors.addTab("Structs/Classes", structEditor);

        macroArea.setColumns(20);
        macroArea.setRows(5);
        macroArea.setSyntaxEditingStyle("text/c");
        macroAreaSP.setViewportView(macroArea);

        editors.addTab("Macros", macroAreaSP);

        btnRenameSrcFile.setText("Rename");
        btnRenameSrcFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameSrcFileActionPerformed(evt);
            }
        });

        fileMenu.setText("File");

        btnDBOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnDBOpen.setText("Open database");
        btnDBOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDBOpenActionPerformed(evt);
            }
        });
        fileMenu.add(btnDBOpen);

        btnDBSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnDBSave.setText("Save");
        btnDBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDBSaveActionPerformed(evt);
            }
        });
        fileMenu.add(btnDBSave);

        btnGenSources.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnGenSources.setText("Generate sources");
        btnGenSources.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenSourcesActionPerformed(evt);
            }
        });
        fileMenu.add(btnGenSources);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddRemoveSourceFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRenameSrcFile))
                    .addComponent(sourceFileSP, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editors))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editors)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sourceFileSP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddRemoveSourceFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRenameSrcFile, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDBOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDBOpenActionPerformed
		FSFile file = XFileDialog.openFileDialog(SwanDB.EXTENSION_FILTER);
		if (file != null) {
			openDB(new SwanDB(file));
		}
    }//GEN-LAST:event_btnDBOpenActionPerformed

    private void btnDBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDBSaveActionPerformed
		if (db.getYaml().getFile() == null) {
			FSFile destFile = XFileDialog.openSaveFileDialog(SwanDB.EXTENSION_FILTER);
			if (destFile == null) {
				return;
			}
			db.getYaml().writeToFile(destFile);
		}
		db.save();
    }//GEN-LAST:event_btnDBSaveActionPerformed

    private void btnGenSourcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenSourcesActionPerformed
		SwanCodeGen.genFromDB(db);
    }//GEN-LAST:event_btnGenSourcesActionPerformed

	private static class ComboTextMap {

		public final int threshold;
		public final String text;

		public ComboTextMap(int threshold, String text) {
			this.threshold = threshold;
			this.text = text;
		}
	}

	private static final ComboTextMap[] COMBO_TEXTS = new ComboTextMap[]{
		new ComboTextMap(4, "Nice!"),
		new ComboTextMap(8, "Great!"),
		new ComboTextMap(16, "Excellent!"),
		new ComboTextMap(32, "Phenomenal!"),
		new ComboTextMap(64, "Fantabulous!"),
		new ComboTextMap(512, "Wait... what?"),
		new ComboTextMap(1024, "I'm scared..."),
		new ComboTextMap(2048, "Are your wrists okay?"),};

	private Random colorRandom = new Random();

    private void chainMethodAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chainMethodAddActionPerformed
		addMethod();
		keyboardUsageStreak++;

		if (keyboardUsageStreak >= 2) {
			String text = null;
			for (int i = 0; i < COMBO_TEXTS.length; i++) {
				if (keyboardUsageStreak == COMBO_TEXTS[i].threshold) {
					text = COMBO_TEXTS[i].text;
					break;
				}
			}
			if (text == null) {
				text = "COMBO x" + keyboardUsageStreak;
			}
			keyboardUsageComboLabel.setText(text);
			keyboardUsageComboLabel.setForeground(new Color(colorRandom.nextInt()));
			keyboardUsageComboLabel.setVisible(true);
		} else {
			keyboardUsageComboLabel.setVisible(false);
		}
    }//GEN-LAST:event_chainMethodAddActionPerformed

	private Timer chainMethodHintHideTimer;

    private void chainMethodAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chainMethodAddMouseClicked
		chainMethodAddHint.setVisible(true);
		if (chainMethodHintHideTimer != null) {
			chainMethodHintHideTimer.stop();
		}
		chainMethodHintHideTimer = new Timer(4000, (e) -> {
			chainMethodAddHint.setVisible(false);
		});
		chainMethodHintHideTimer.setRepeats(false);
		chainMethodHintHideTimer.start();
    }//GEN-LAST:event_chainMethodAddMouseClicked

    private void btnRenameSrcFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameSrcFileActionPerformed
		SwanSourceFile f = sourceFileList.getSelectedValue();
		if (f != null) {
			String path = JOptionPane.showInputDialog(SwanComposer.this, "Enter the new path for the source file:", f.path);
			if (path != null) {
				if (db.findFileByPath(path) != null) {
					DialogUtils.showErrorMessage(SwanComposer.this, "Duplicate source file", "A source file under this path already exists.");
				} else {
					String oldPath = f.path;
					FSFile root = db.getYaml().getFile().getParent();
					if (root != null) {
						FSFile oldOutLoc = root.getChild(oldPath);
						if (oldOutLoc.exists()) {
							FSFile oldOutParent = oldOutLoc.getParent();
							oldOutLoc.delete();
							if (oldOutParent.getChildCount() == 0) {
								oldOutParent.delete();
							}
						}
					}

					f.path = path;
					for (SwanSourceFile f2 : db.sourceFiles) {
						for (int i = 0; i < f2.includes.size(); i++) {
							if (f2.includes.get(i).equals(oldPath)) {
								f2.includes.set(i, path);
							}
						}
					}
					db.sortSrcFiles();
					sourceFileList.setSelectedValue(f, true);
					loadSourceFile();
				}
			}
		}
    }//GEN-LAST:event_btnRenameSrcFileActionPerformed

    private void enumDefineFlagOpsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enumDefineFlagOpsActionPerformed
		if (currentEnum != null) {
			currentEnum.defineFlagOps = enumDefineFlagOps.isSelected();
		}
    }//GEN-LAST:event_enumDefineFlagOpsActionPerformed

    private void btnMoveStructUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveStructUpActionPerformed
		if (currentStruct != null) {
			SwanSourceFile file = sourceFileList.getSelectedValue();
			if (file != null) {
				SwanStructure s = currentStruct;
				int index = file.structures.indexOf(s);
				if (index != -1 && index > 0) {
					SwanStructure prevElem = file.structures.get(index - 1);
					file.structures.set(index, prevElem);
					file.structures.set(index - 1, s);
					structList.setSelectedIndex(index - 1);
				}
			}
		}
    }//GEN-LAST:event_btnMoveStructUpActionPerformed

    private void btnMoveStructDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveStructDownActionPerformed
        if (currentStruct != null) {
			SwanSourceFile file = sourceFileList.getSelectedValue();
			if (file != null) {
				SwanStructure s = currentStruct;
				int index = file.structures.indexOf(s);
				if (index != -1 && index < file.structures.size() - 1) {
					SwanStructure nextElem = file.structures.get(index + 1);
					file.structures.set(index, nextElem);
					file.structures.set(index + 1, s);
					structList.setSelectedIndex(index + 1);
				}
			}
		}
    }//GEN-LAST:event_btnMoveStructDownActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		ComponentUtils.setSystemNativeLookAndFeel();

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				SwanComposer sc = new SwanComposer();
				sc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				sc.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.fife.ui.rtextarea.RTextScrollPane allFunctionsSP;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea allFunctionsTextArea;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveEnum;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveGVar;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveInclude;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveSourceFile;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveStruct;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveStructCppMethod;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveTypedef;
    private javax.swing.JMenuItem btnDBOpen;
    private javax.swing.JMenuItem btnDBSave;
    private javax.swing.JMenuItem btnGenSources;
    private javax.swing.JButton btnMoveStructDown;
    private javax.swing.JButton btnMoveStructUp;
    private javax.swing.JButton btnRenameSrcFile;
    private javax.swing.JButton chainMethodAdd;
    private javax.swing.JLabel chainMethodAddHint;
    private javax.swing.JTabbedPane editors;
    private javax.swing.JTextField enumCppName;
    private javax.swing.JLabel enumCppNameLabel;
    private javax.swing.JCheckBox enumDefineFlagOps;
    private javax.swing.JPanel enumEditor;
    private javax.swing.JList<SwanEnum> enumList;
    private javax.swing.JLabel enumListLabel;
    private javax.swing.JScrollPane enumListSP;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea enumTextArea;
    private org.fife.ui.rtextarea.RTextScrollPane enumTextAreaSP;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel functionsEditor;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea gvarDefArea;
    private javax.swing.JPanel gvarEditor;
    private javax.swing.JList<SwanTypedef> gvarList;
    private javax.swing.JLabel gvarListLabel;
    private javax.swing.JScrollPane gvarListSP;
    private xstandard.gui.components.combobox.ACComboBox<SwanSourceFile> includeComboBox;
    private javax.swing.JPanel includeEditor;
    private javax.swing.JLabel includeListLabel;
    private javax.swing.JList<String> includesList;
    private javax.swing.JScrollPane includesSP;
    private javax.swing.JLabel keyboardUsageComboLabel;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea macroArea;
    private org.fife.ui.rtextarea.RTextScrollPane macroAreaSP;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel methodListLabel;
    private javax.swing.JList<SwanSourceFile> sourceFileList;
    private javax.swing.JScrollPane sourceFileSP;
    private javax.swing.JPanel structCppEditor;
    private javax.swing.JCheckBox structCppMethodIsStatic;
    private javax.swing.JTextField structCppMethodName;
    private javax.swing.JLabel structCppMethodNameLabel;
    private javax.swing.JTextField structCppMethodParamSwizzle;
    private javax.swing.JLabel structCppMethodParamSwizzleLabel;
    private xstandard.gui.components.combobox.ACComboBox<String> structCppMethodTarget;
    private javax.swing.JLabel structCppMethodTargetArgs;
    private javax.swing.JLabel structCppMethodTargetLabel;
    private javax.swing.JPanel structCppMethodsEditor;
    private javax.swing.JList<SwanMethod> structCppMethodsList;
    private javax.swing.JScrollPane structCppMethodsListSP;
    private javax.swing.JTextField structCppName;
    private javax.swing.JLabel structCppNameLabel;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea structDefArea;
    private org.fife.ui.rtextarea.RTextScrollPane structDefSP;
    private javax.swing.JPanel structEditor;
    private javax.swing.JTabbedPane structEditorTabs;
    private javax.swing.JList<SwanStructure> structList;
    private javax.swing.JLabel structListLabel;
    private javax.swing.JScrollPane structListSP;
    private javax.swing.JTextField typedefCppName;
    private javax.swing.JLabel typedefCppNameLabel;
    private javax.swing.JPanel typedefEditor;
    private javax.swing.JLabel typedefListLabel;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea typedefTextArea;
    private javax.swing.JList<SwanTypedef> typedefsList;
    private javax.swing.JScrollPane typedefsSP;
    // End of variables declaration//GEN-END:variables
}
