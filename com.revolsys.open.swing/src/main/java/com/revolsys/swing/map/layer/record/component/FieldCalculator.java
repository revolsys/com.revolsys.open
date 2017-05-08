package com.revolsys.swing.map.layer.record.component;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.awt.WebColors;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class FieldCalculator extends AbstractUpdateField implements DocumentListener {
  private static final long serialVersionUID = 1L;

  public static void addMenuItem(final MenuFactory headerMenu) {
    final EnableCheck enableCheck = newEnableCheck();
    headerMenu.addMenuItem("field", "Field Calculator", "calculator_edit", enableCheck,
      FieldCalculator::showDialog);
  }

  private static void showDialog() {
    final FieldCalculator dialog = new FieldCalculator();
    dialog.setVisible(true);
  }

  private JTextArea expressionField;

  private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

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
        final List<Object> values = id.getValues();
        if (values.size() == 1) {
          return values.get(0);
        }
        return values;
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
    super("Set Field Value");

  }

  public void addTextButton(final ToolBar toolBar, final String label, final String text) {
    final Runnable action = () -> {
      final int newLocation = this.expressionField.getCaretPosition();
      int location = newLocation;
      final Document document = this.expressionField.getDocument();

      boolean needsWhitespaceNext = false;
      try {
        if (location < document.getLength() - 1
          && !Character.isWhitespace(document.getText(location + 1, 1).charAt(0))) {
          needsWhitespaceNext = true;
        }
        if (location > 0 && !Character.isWhitespace(document.getText(location - 1, 1).charAt(0))) {
          this.expressionField.insert(" ", location);
          location++;
        }
        this.expressionField.insert(text, location);
        if (needsWhitespaceNext) {
          this.expressionField.insert(" ", newLocation);
        }
      } catch (final BadLocationException e) {
        this.expressionField.append(text);
      }
    };
    toolBar.addButton("text", label, action);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    validateExpression();
  }

  @Override
  protected JPanel initFieldPanel() {
    final FieldDefinition fieldDefinition = this.getFieldDefinition();
    final String title = "<html>Enter the new value for <b style='color:#32CD32'>"
      + getRecordCountString() + "</b> records</html>";

    final JPanel fieldPanel = new JPanel(new VerticalLayout());
    fieldPanel.setBorder(BorderFactory.createTitledBorder(title));
    final String fieldTitle = fieldDefinition.getTitle();
    fieldPanel.add(SwingUtil.newLabel(fieldTitle));

    this.expressionField = new TextArea("script", 5, 60);
    fieldPanel.add(new JScrollPane(this.expressionField));
    this.expressionField.getDocument().addDocumentListener(this);

    this.errorsField = new TextArea("errors", 5, 60);
    this.errorsField.setEditable(false);
    this.errorsField.setForeground(WebColors.Red);
    fieldPanel.add(new JScrollPane(this.errorsField));

    final ToolBar toolBar = new ToolBar();
    fieldPanel.add(toolBar);

    final AbstractRecordLayer layer = getLayer();

    final List<String> fieldNames = layer.getFieldNames();
    final ComboBox<String> fieldNamesField = ComboBox.newComboBox("fieldNames", fieldNames,
      (final Object name) -> {
        return layer.getFieldTitle((String)name);
      });
    toolBar.addComponent("fieldName", fieldNamesField);
    toolBar.add(fieldNamesField);

    final Runnable addFieldAction = () -> {
      final int pos = this.expressionField.getCaretPosition();
      final String fieldName = fieldNamesField.getFieldValue();
      this.expressionField.insert(fieldName, pos);
    };
    toolBar.addButton("fieldName", "Add field name", "add", addFieldAction);

    for (final String text : Arrays.asList("+", "-", "*", "/")) {
      addTextButton(toolBar, text, text);
    }
    final String fieldName = fieldDefinition.getName();
    addTextButton(toolBar, "if", "if (expression) {\nnewValue;\n} else {\n" + fieldName + ";\n}");
    addTextButton(toolBar, "Code ID", "codeId(" + fieldName + ", value)");
    addTextButton(toolBar, "Code Value", "codeValue(" + fieldName + ", id)");
    return fieldPanel;
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
  protected void updateRecord(final LayerRecord record) {
    final Bindings bindings = newBindings();
    bindings.putAll(record);
    bindings.put("record", new ArrayRecord(record));
    try {
      final String fieldName = getFieldDefinition().getName();
      final Object value = this.script.eval(bindings);
      record.setValue(fieldName, value);
    } catch (final ScriptException e) {
      System.out.println(e.getMessage());
    }
  }

  public void validateExpression() {
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
        this.expressionField.setForeground(WebColors.Black);
        this.errorsField.setText(null);
      } catch (final Throwable e) {
        String errorMessage = e.getMessage();
        if (!Property.hasValue(errorMessage)) {
          errorMessage = "null pointer";
        }
        this.errorsField.setText(errorMessage);
        valid = false;
        this.script = null;
        this.expressionField.setForeground(WebColors.Red);
      }
    }
    setFormValid(valid);
  }
}
