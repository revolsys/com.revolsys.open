package com.revolsys.swing.map.layer.record.component;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.scripting.ScriptEngines;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class FieldCalculator extends AbstractUpdateField implements DocumentListener {
  private static final long serialVersionUID = 1L;

  public static void addMenuItem(final MenuFactory headerMenu) {
    final EnableCheck enableCheck = newEnableCheck();
    headerMenu.addMenuItem("field", "Field Calculator", "calculator:edit", enableCheck,
      FieldCalculator::showDialog);
  }

  private static void showDialog() {
    final FieldCalculator dialog = new FieldCalculator();
    dialog.setVisible(true);
  }

  private JTextArea expressionField;

  private final ScriptEngine scriptEngine = ScriptEngines.JS;

  private final Compilable scriptEngineCompiler = (Compilable)this.scriptEngine;

  private CompiledScript script;

  private final BiFunction<String, Object, Object> codeIdFunction = (fieldName, value) -> {
    final AbstractRecordLayer layer = getLayer();
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
    if (codeTable != null) {
      final Identifier id = codeTable.getIdentifier(value);
      if (id == null) {
        return null;
      } else {
        if (id.isSingle()) {
          return id.getValue(0);
        }
        return id.getValues();
      }
    }
    return value;
  };

  private final BiFunction<String, Object, Object> codeValueFunction = (fieldName, id) -> {
    final AbstractRecordLayer layer = getLayer();
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
    if (codeTable != null) {
      final Object value = codeTable.getValue(id);
      return value;
    }
    return id;
  };

  private TextArea errorsField;

  private FieldCalculator() {
    super("Field Calculator");
  }

  private void addTextButton(final String groupName, final ToolBar toolBar, final String label,
    final String text) {
    final Runnable action = () -> {
      insertText(text);
    };
    toolBar.addButton(groupName, label, action) //
      .setBorderPainted(true);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    validateExpression();
  }

  @Override
  protected String getProgressMonitorNote() {
    return "Calculating " + getFieldDefinition().getName() + " values";
  }

  @Override
  protected void initDialog() {
    setMinimumSize(new Dimension(700, 300));
    super.initDialog();
  }

  @Override
  protected JComponent initErrorsPanel() {
    final Color background = new JPanel().getBackground();
    this.errorsField = new TextArea("errors", 5, 1);
    this.errorsField.setEditable(false);
    this.errorsField.setForeground(WebColors.Red);
    this.errorsField.setBackground(background);
    final JScrollPane errorScroll = new JScrollPane(this.errorsField);
    errorScroll.setBorder(BorderFactory.createTitledBorder("Errors"));
    errorScroll.setBackground(background);
    return errorScroll;

  }

  @Override
  protected JPanel initFieldPanel() {
    final FieldDefinition fieldDefinition = getFieldDefinition();
    final String fieldName = fieldDefinition.getName();

    final JPanel fieldPanel = new JPanel(new VerticalLayout());

    final ToolBar toolBar = new ToolBar();
    fieldPanel.add(toolBar);

    this.expressionField = new TextArea("script", 8, 1);
    fieldPanel.add(new JScrollPane(this.expressionField));
    this.expressionField.getDocument().addDocumentListener(this);

    final AbstractRecordLayer layer = getLayer();
    final List<String> fieldNames = layer.getFieldNames();
    final ComboBox<String> fieldNamesField = ComboBox.newComboBox("fieldNames", fieldNames,
      (final Object name) -> {
        return layer.getFieldTitle((String)name);
      });
    toolBar.addComponent("fieldName", fieldNamesField);
    toolBar.add(fieldNamesField);
    fieldNamesField.setMaximumSize(new Dimension(250, 30));

    final Runnable addFieldAction = () -> {
      final String selectedFieldName = fieldNamesField.getFieldValue();
      insertText(selectedFieldName);
    };
    toolBar.addButton("fieldName", "Add field name", "add", addFieldAction);

    for (final String text : Arrays.asList("+", "-", "*", "/")) {
      addTextButton("operators", toolBar, text, text);
    }
    addTextButton("condition", toolBar, "if",
      "if (expression) {\n  newValue;\n} else {\n  " + fieldName + ";\n}");
    addTextButton("codeTable", toolBar, "Code ID", "codeId('codeFieldName', codeValue)");
    addTextButton("codeTable", toolBar, "Code Value", "codeValue('codeFieldName', codeValue)");

    return fieldPanel;
  }

  private void insertText(final String text) {
    int location = this.expressionField.getCaretPosition();
    final Document document = this.expressionField.getDocument();

    boolean needsWhitespaceNext = false;
    try {
      // Remove selected text
      final int selectionStart = this.expressionField.getSelectionStart();
      final int selectionEnd = this.expressionField.getSelectionEnd();
      final int selectionLength = selectionEnd - selectionStart;
      if (selectionLength > 0) {
        document.remove(selectionStart, selectionLength);
        location = selectionStart;
      }
      if (location > 0 && !Character.isWhitespace(document.getText(location - 1, 1).charAt(0))) {
        this.expressionField.insert(" ", location);
        location++;
      }
      final int newLocation = location + text.length();

      if (location < document.getLength() - 1
        && !Character.isWhitespace(document.getText(location + 1, 1).charAt(0))) {
        needsWhitespaceNext = true;
      }
      this.expressionField.insert(text, location);
      if (needsWhitespaceNext) {
        this.expressionField.insert(" ", newLocation);
      }
    } catch (final BadLocationException e) {
      this.expressionField.append(text);
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    validateExpression();
  }

  protected Bindings newBindings() {
    final Bindings bindings = this.scriptEngine.createBindings();
    bindings.put("codeId", this.codeIdFunction);
    bindings.put("codeValue", this.codeValueFunction);
    return bindings;
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    validateExpression();
  }

  @Override
  public void updateRecord(final LayerRecord record) {
    final Bindings bindings = newBindings();
    bindings.putAll(record);
    bindings.put("record", new ArrayRecord(record));
    try {
      final FieldDefinition fieldDefinition = getFieldDefinition();
      final String fieldName = fieldDefinition.getName();
      Object value = this.script.eval(bindings);
      value = fieldDefinition.toFieldValueException(value);
      final AbstractRecordLayer layer = record.getLayer();
      layer.setRecordValue(record, fieldName, value);
    } catch (final ScriptException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  private void validateExpression() {
    boolean valid = true;
    final String scriptText = this.expressionField.getText();
    if (scriptText.isEmpty()) {
      valid = false;
    } else {
      try {
        this.script = this.scriptEngineCompiler.compile(scriptText);
        final Bindings bindings = newBindings();
        final AbstractRecordLayer layer = getLayer();
        final RecordDefinition recordDefinition = layer.getRecordDefinition();
        final Record record = new ArrayRecord(recordDefinition);
        for (final FieldDefinition field : layer.getFieldDefinitions()) {
          bindings.put(field.getName(), null);
        }
        bindings.put("record", record);
        this.script.eval(bindings);
        this.errorsField.setText(null);
      } catch (final Throwable e) {
        String errorMessage = e.getMessage();
        if (!Property.hasValue(errorMessage)) {
          errorMessage = "null pointer";
        }
        this.errorsField.setText(errorMessage);
        this.errorsField.setCaretPosition(0);
        valid = false;
        this.script = null;
      }
    }
    setFormValid(valid);
  }
}
