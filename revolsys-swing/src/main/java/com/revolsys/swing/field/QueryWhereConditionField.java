package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BetweenOperatorNode;
import com.akiban.sql.parser.BinaryArithmeticOperatorNode;
import com.akiban.sql.parser.BinaryLogicalOperatorNode;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.CastNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.InListOperatorNode;
import com.akiban.sql.parser.IsNullNode;
import com.akiban.sql.parser.LikeEscapeOperatorNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NotNode;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.RowConstructorNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserException;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.SimpleStringOperatorNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.UserTypeConstantNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Between;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.In;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.Function;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class QueryWhereConditionField extends ValueField
  implements MouseListener, CaretListener, ItemListener, PropertyChangeListener, DocumentListener {

  private static final long serialVersionUID = 1L;

  private JComponent searchField;

  private final JPanel searchFieldPanel;

  private CodeTable codeTable;

  private final ComboBox<FieldDefinition> fieldNamesList;

  private final AbstractRecordLayer layer;

  private final PropertyChangeListener listener;

  private final Condition originalFilter;

  private final RecordDefinition recordDefinition;

  private final Color selectionColor;

  private final String sqlPrefix;

  private final JTextArea statusLabel;

  private boolean valid;

  private boolean validating;

  private final TextPane whereTextField;

  private boolean hasSearchText;

  private boolean fieldComparable;

  private boolean likeEnabled;

  public QueryWhereConditionField(final AbstractRecordLayer layer,
    final PropertyChangeListener listener, final Condition filter) {
    this(layer, listener, filter, null);
  }

  public QueryWhereConditionField(final AbstractRecordLayer layer,
    final PropertyChangeListener listener, final Condition filter, final String query) {
    super(new BorderLayout());
    setTitle("Advanced Search");
    this.layer = layer;
    this.originalFilter = filter;
    this.listener = listener;
    this.recordDefinition = layer.getRecordDefinition();
    final List<FieldDefinition> fieldDefinitions = this.recordDefinition.getFields();

    this.fieldNamesList = ComboBox.newComboBox("fieldNames", fieldDefinitions,
      (final Object field) -> {
        return ((FieldDefinition)field).getTitle();
      });
    this.fieldNamesList.setShowErrorIcon(false);
    this.fieldNamesList.addItemListener(this);
    this.fieldNamesList.addMouseListener(this);

    this.searchFieldPanel = new JPanel();
    this.searchFieldPanel.setOpaque(false);

    final ObjectPropertyEnableCheck hasSearchText = new ObjectPropertyEnableCheck(this,
      "hasSearchText", true);
    final EnableCheck fieldCompareEnabled = hasSearchText
      .and(new ObjectPropertyEnableCheck(this, "fieldComparable"));

    final ToolBar operatorToolBar = new ToolBar();
    operatorToolBar.setOpaque(false);
    operatorToolBar.setBorderPainted(true);
    for (final String binaryOperator : Arrays.asList("=", "<>")) {
      final Runnable runnable = () -> actionAddBinaryCondition(binaryOperator);
      operatorToolBar.addButton("binary", binaryOperator, hasSearchText, runnable)
        .setBorderPainted(true);
    }
    for (final String binaryOperator : Arrays.asList("<", "<=", ">", ">=")) {
      final Runnable runnable = () -> actionAddBinaryCondition(binaryOperator);
      operatorToolBar.addButton("binary", binaryOperator, fieldCompareEnabled, runnable)
        .setBorderPainted(true);
    }
    operatorToolBar
      .addButton("like", "LIKE", hasSearchText.and(this, "likeEnabled"),
        this::actionAddLikeCondition)
      .setBorderPainted(true);

    for (final String rightUnaryOperator : Arrays.asList("IS NULL", "IS NOT NULL")) {
      final Runnable runnable = () -> actionAddRightUnaryCondition(rightUnaryOperator);
      operatorToolBar.addButton("rightUnary", rightUnaryOperator, runnable).setBorderPainted(true);
    }
    operatorToolBar.addButton("in", "IN", hasSearchText, this::actionAddInCondition)
      .setBorderPainted(true);

    final ToolBar buttonsToolBar = new ToolBar();
    buttonsToolBar.setOpaque(false);
    buttonsToolBar.setBorderPainted(true);
    buttonsToolBar.addButton("relational", "AND", () -> insertText("AND")).setBorderPainted(true);
    buttonsToolBar.addButton("relational", "OR", () -> insertText("OR")).setBorderPainted(true);
    buttonsToolBar.addButton("relational", "NOT", () -> insertText("NOT")).setBorderPainted(true);
    buttonsToolBar.addButton("grouping", "( )", () -> insertText("( )")).setBorderPainted(true);
    buttonsToolBar.addButton("math", "+", () -> insertText("+")).setBorderPainted(true);
    buttonsToolBar.addButton("math", "-", () -> insertText("-")).setBorderPainted(true);
    buttonsToolBar.addButton("math", "*", () -> insertText("*")).setBorderPainted(true);
    buttonsToolBar.addButton("math", "/", () -> insertText("/")).setBorderPainted(true);

    final BasePanel widgetPanel = new BasePanel(new VerticalLayout(5), //
      GroupLayouts.panelColumns(this.fieldNamesList), //
      operatorToolBar, //
      this.searchFieldPanel, //
      buttonsToolBar);

    this.whereTextField = new TextPane(5, 20);
    this.whereTextField.setFont(new Font("Monospaced", Font.PLAIN, 11));
    this.selectionColor = this.whereTextField.getSelectionColor();
    this.whereTextField.addCaretListener(this);

    final JScrollPane whereScroll = new JScrollPane(this.whereTextField);
    // whereTextField.setContentType("text/sql"); // Requires the above scroll
    // pane

    this.sqlPrefix = "SELECT * FROM "
      + this.recordDefinition.getPath().substring(1).replace('/', '.') + " WHERE";

    final JPanel filterTextPanel = new JPanel(new BorderLayout());
    filterTextPanel.setOpaque(false);
    filterTextPanel.add(new JLabel(this.sqlPrefix), BorderLayout.NORTH);
    filterTextPanel.add(whereScroll, BorderLayout.CENTER);

    final ToolBar statusToolBar = new ToolBar();
    statusToolBar.setOpaque(false);
    final JButton verifyButton = statusToolBar.addButton("default", "Verify",
      this::verifyCondition);
    verifyButton.setBorderPainted(true);

    final JPanel queryPanel = new JPanel(new BorderLayout());
    Borders.titled(queryPanel, "Query");
    queryPanel.setOpaque(false);
    queryPanel.add(widgetPanel, BorderLayout.NORTH);
    queryPanel.add(filterTextPanel, BorderLayout.CENTER);
    queryPanel.add(statusToolBar, BorderLayout.SOUTH);
    queryPanel.setPreferredSize(new Dimension(500, 400));

    this.statusLabel = new TextArea();
    this.statusLabel.setEditable(false);
    final JScrollPane statusPane = new JScrollPane(this.statusLabel);
    statusPane.setPreferredSize(new Dimension(500, 100));

    final JPanel statusPanel = new JPanel(new BorderLayout());
    Borders.titled(statusPanel, "Messages");
    statusPanel.setOpaque(false);
    statusPanel.add(statusPane, BorderLayout.CENTER);

    final JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel, statusPanel);
    topBottom.setResizeWeight(0.7);

    add(topBottom, BorderLayout.CENTER);

    setPreferredSize(new Dimension(800, 500));

    setFieldValue(filter);
    if (filter != null) {
      this.whereTextField.setText(filter.toFormattedString());
    }
    if (Property.hasValue(query)) {
      this.whereTextField.setText(query);
    }
    final String searchField = layer.getProperty("searchField");
    FieldDefinition searchFieldDefinition = this.recordDefinition.getField(searchField);
    if (searchFieldDefinition == null) {
      searchFieldDefinition = fieldDefinitions.get(0);
    }
    this.fieldNamesList.setSelectedItem(searchFieldDefinition);
    setSearchField(searchFieldDefinition);
  }

  public QueryWhereConditionField(final AbstractRecordLayer layer,
    final PropertyChangeListener listener, final String query) {
    this(layer, listener, null, query);
  }

  private void actionAddBinaryCondition(final String operator) {
    final FieldDefinition fieldDefinition = this.fieldNamesList.getSelectedItem();
    if (fieldDefinition != null) {
      Object fieldValue = ((Field)this.searchField).getFieldValue();
      if (fieldValue != null) {
        final int position = this.whereTextField.getCaretPosition();
        DataType fieldType = fieldDefinition.getDataType();
        if (this.codeTable == null) {
          try {
            fieldValue = fieldDefinition.toFieldValue(fieldValue);
          } catch (final Throwable e) {
            setInvalidMessage("'" + fieldValue + "' is not a valid "
              + fieldDefinition.getDataType().getValidationName());
            return;
          }
        } else {
          final List<Object> values = this.codeTable
            .getValues(Identifier.newIdentifier(fieldValue));
          if (values.size() == 1) {
            fieldValue = values.get(0);
          } else {
            fieldValue = Strings.toString(":", values);
          }
          if (fieldValue != null) {
            fieldType = DataTypes.STRING;
          }
        }
        if (fieldValue != null) {

          final Document document = this.whereTextField.getDocument();
          final StringBuilder text = new StringBuilder();
          if (position > 0) {
            text.append(" ");
          }
          text.append(fieldDefinition.getName());
          text.append(" ");
          text.append(operator);
          text.append(" ");
          appendValue(text, fieldType, fieldValue);
          text.append(" ");
          try {
            document.insertString(position, text.toString(), null);
          } catch (final BadLocationException e) {
            Logs.error(this, "Error inserting text: " + text, e);
          }
        }
      }
    }
  }

  private void actionAddInCondition() {
    final FieldDefinition fieldDefinition = this.fieldNamesList.getSelectedItem();
    if (fieldDefinition != null) {
      Object fieldValue = ((Field)this.searchField).getFieldValue();
      if (Property.hasValue(fieldValue)) {
        int position = this.whereTextField.getCaretPosition();
        DataType fieldType = fieldDefinition.getDataType();
        if (fieldValue != null) {
          if (this.codeTable == null) {
            try {
              fieldValue = fieldDefinition.toFieldValue(fieldValue);
            } catch (final Throwable e) {
              setInvalidMessage(
                "'" + fieldValue + "' is not a valid " + fieldType.getValidationName());
              return;
            }
          } else {
            fieldValue = this.codeTable.getValue(fieldValue);
            if (fieldValue != null) {
              fieldType = DataTypes.STRING;
            }
          }
          if (fieldValue != null) {
            final StringBuilder text = new StringBuilder();
            try {
              final Document document = this.whereTextField.getDocument();
              final String currentText = document.getText(0, position);
              final Matcher matcher = Pattern.compile("(?:.+ )?" + fieldDefinition.getName()
                + " IN \\((?:.+,\\s*)*'?((?:[^']|'')*)'?\\) $").matcher(currentText);
              if (matcher.matches()) {
                final String previousValue = matcher.group(1).replace("''", "'");

                if (!DataType.equal(fieldValue, previousValue)) {
                  position -= 2;
                  text.append(", ");
                  appendValue(text, fieldType, fieldValue);
                }
              } else {
                if (position > 0) {
                  text.append(" ");
                }
                text.append(fieldDefinition.getName());
                text.append(" IN (");
                appendValue(text, fieldType, fieldValue);
                text.append(") ");
              }
              document.insertString(position, text.toString(), null);
            } catch (final BadLocationException e) {
              Logs.error(this, "Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  private void actionAddLikeCondition() {
    final FieldDefinition fieldDefinition = this.fieldNamesList.getSelectedItem();
    if (fieldDefinition != null) {
      final Object fieldValue = ((Field)this.searchField).getFieldValue();
      if (fieldValue != null) {
        if (this.codeTable == null) {
          final int position = this.whereTextField.getCaretPosition();
          if (fieldValue != null) {
            final String valueString = fieldDefinition.toString(fieldValue);

            final Document document = this.whereTextField.getDocument();
            final StringBuilder text = new StringBuilder();
            if (position > 0) {
              text.append(" ");
            }
            text.append(fieldDefinition.getName());
            text.append(" LIKE '%");
            text.append(valueString.replaceAll("'", "''"));
            text.append("%' ");
            try {
              document.insertString(position, text.toString(), null);
            } catch (final BadLocationException e) {
              Logs.error(this, "Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  private void actionAddRightUnaryCondition(final String operator) {
    final FieldDefinition fieldDefinition = this.fieldNamesList.getSelectedItem();
    if (fieldDefinition != null) {
      final int position = this.whereTextField.getCaretPosition();

      final Document document = this.whereTextField.getDocument();
      final StringBuilder text = new StringBuilder();
      if (position > 0) {
        text.append(" ");
      }
      text.append(fieldDefinition.getName());
      text.append(" ");
      text.append(operator);
      text.append(" ");

      try {
        document.insertString(position, text.toString(), null);
      } catch (final BadLocationException e) {
        Logs.error(this, "Error inserting text: " + text, e);
      }
    }
  }

  public void appendValue(final StringBuilder text, final DataType type, final Object value) {
    final String valueString = type.toString(value);
    if (DataTypes.SQL_DATE == type) {
      text.append("{d '" + valueString + "'}");
    } else if (DataTypes.TIME == type) {
      text.append("{t '" + valueString + "'}");
    } else if (DataTypes.TIMESTAMP == type || DataTypes.DATE == type) {
      text.append("{ts '" + valueString + "'}");
    } else if (DataTypes.DATE_TIME == type) {
      text.append("{ts '" + valueString + "'}");
    } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
      text.append(valueString);
    } else {
      text.append("'");
      text.append(valueString.replaceAll("'", "''"));
      text.append("'");
    }
  }

  @Override
  public void caretUpdate(final CaretEvent e) {
    if (!this.validating) {
      this.whereTextField.setSelectionColor(this.selectionColor);
      repaint();
    }
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    updateHasSearchText();
  }

  public void insertText(final String operator) {
    if (Property.hasValue(operator)) {
      int position = this.whereTextField.getCaretPosition();
      String previousText;
      try {
        previousText = this.whereTextField.getText(0, position);
      } catch (final BadLocationException e) {
        previousText = "";
      }
      if (!Property.hasValue(previousText)
        || !previousText.matches(".*" + operator.replaceAll("\\(", "\\\\(")
          .replaceAll("\\)", "\\\\)")
          .replaceAll("\\*", "\\\\*")
          .replaceAll("\\+", "\\\\+") + "\\s*$")) {
        final Document document = this.whereTextField.getDocument();
        try {
          if (Property.hasValue(previousText)
            && !previousText.substring(previousText.length() - 1).matches("\\s$")) {
            document.insertString(position++, " ", null);
          }
          document.insertString(position, operator + " ", null);
        } catch (final BadLocationException e) {
        }
      }
    }
    this.whereTextField.requestFocusInWindow();
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    updateHasSearchText();
  }

  public boolean isFieldComparable() {
    return this.fieldComparable;
  }

  public boolean isHasSearchText() {
    return this.hasSearchText;
  }

  public boolean isLikeEnabled() {
    return this.likeEnabled;
  }

  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == this.fieldNamesList) {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        final FieldDefinition fieldDefinition = (FieldDefinition)event.getItem();
        setSearchField(fieldDefinition);
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getSource() == this.fieldNamesList) {
      if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
        final FieldDefinition fieldDefinition = this.fieldNamesList.getSelectedItem();
        if (Property.hasValue(fieldDefinition)) {
          final String fieldName = fieldDefinition.getName();
          int position = this.whereTextField.getCaretPosition();
          String previousText;
          try {
            previousText = this.whereTextField.getText(0, position);
          } catch (final BadLocationException e) {
            previousText = "";
          }
          if (!Property.hasValue(previousText)
            || !previousText.matches(".*\"?" + fieldName + "\"?\\s*$")) {
            final Document document = this.whereTextField.getDocument();
            try {
              if (Property.hasValue(previousText)
                && !previousText.substring(previousText.length() - 1).matches("\\s$")) {
                document.insertString(position++, " ", null);
              }

              if (fieldName.matches("[A-Z][_A-Z1-9]*")) {
                document.insertString(position, fieldName + " ", null);
              } else {
                document.insertString(position, "\"" + fieldName + "\" ", null);
              }

            } catch (final BadLocationException e) {
            }
          }
        }
      }
      this.whereTextField.requestFocusInWindow();
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.searchField) {
      updateHasSearchText();
    }
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    updateHasSearchText();
  }

  @Override
  public void save() {
    super.save();
    final Condition condition = getFieldValue();
    this.listener
      .propertyChange(new PropertyChangeEvent(this, "filter", this.originalFilter, condition));
  }

  @Override
  public void save(final JDialog dialog) {
    verifyCondition();
    if (this.valid) {
      super.save(dialog);
    } else {
      Dialogs.showMessageDialog(
        "<html><p>Cannot save the advanced query as the SQL is valid.<p></p>Fix the SQL or use the cancel button on the Advanced Search window to cancel the changes.<p></html>",
        "SQL Invalid", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public boolean setFieldValue(final Object value) {
    if (value instanceof Condition) {
      return super.setFieldValue(value);
    } else if (Property.hasValue(value)) {
      return false;
    } else {
      return super.setFieldValue(value);
    }
  }

  public void setHasSearchText(final boolean hasSearchText) {
    final boolean oldValue = this.hasSearchText;
    this.hasSearchText = hasSearchText;
    firePropertyChange("hasSearchText", oldValue, hasSearchText);
  }

  protected void setInvalidMessage(final int offset, final String message) {
    if (offset >= 0) {
      this.whereTextField.setSelectionColor(WebColors.Pink);
      this.whereTextField.setSelectionStart(offset);
      final Document document = this.whereTextField.getDocument();
      this.whereTextField.setSelectionEnd(document.getLength());
      this.whereTextField.requestFocusInWindow();
    }
    setInvalidMessage(message);
  }

  protected void setInvalidMessage(final String message) {
    this.statusLabel.setForeground(WebColors.Red);
    this.statusLabel.append(message + "\n");
    this.valid = false;
    this.statusLabel.setCaretPosition(0);
  }

  private void setSearchField(final FieldDefinition fieldDefinition) {
    if (this.searchField != null) {
      Property.removeListener(this.searchField, this);
      if (this.searchField instanceof JTextComponent) {
        final JTextComponent textField = (JTextComponent)this.searchField;
        textField.getDocument().removeDocumentListener(this);
      }
    }
    this.searchFieldPanel.removeAll();
    final String fieldName = fieldDefinition.getName();
    this.codeTable = this.recordDefinition.getCodeTableByFieldName(fieldName);
    this.searchField = this.layer.newSearchField(fieldDefinition, this.codeTable);

    final boolean oldFieldComparable = this.fieldComparable;
    this.fieldComparable = false;
    final boolean oldLikeEnabled = this.likeEnabled;
    this.likeEnabled = false;
    if (this.codeTable == null) {
      final Class<?> fieldClass = fieldDefinition.getTypeClass();
      if (Number.class.isAssignableFrom(fieldClass) || Date.class.isAssignableFrom(fieldClass)) {
        this.fieldComparable = true;
      } else if (String.class.isAssignableFrom(fieldClass)) {
        this.likeEnabled = true;
      }
    }
    firePropertyChange("fieldComparable", oldFieldComparable, this.fieldComparable);
    firePropertyChange("likeEnabled", oldLikeEnabled, this.likeEnabled);

    this.searchFieldPanel.add(this.searchField);
    setHasSearchText(((Field)this.searchField).isHasValidValue());
    Property.addListener(this.searchField, this);
    if (this.searchField instanceof JTextComponent) {
      final JTextComponent textField = (JTextComponent)this.searchField;
      textField.getDocument().addDocumentListener(this);
    }
    GroupLayouts.makeColumns(this.searchFieldPanel, false);
  }

  @SuppressWarnings("unchecked")
  private <V extends QueryValue> V toQueryValue(final ValueNode expression) {
    if (expression instanceof BetweenOperatorNode) {
      final BetweenOperatorNode betweenExpression = (BetweenOperatorNode)expression;
      final ValueNode leftValueNode = betweenExpression.getLeftOperand();
      final ValueNodeList rightOperandList = betweenExpression.getRightOperandList();
      final ValueNode betweenExpressionStart = rightOperandList.get(0);
      final ValueNode betweenExpressionEnd = rightOperandList.get(1);
      if (!(leftValueNode instanceof ColumnReference)) {
        setInvalidMessage("Between operator must use a column name not: " + leftValueNode);
        return null;
      }

      if (!(betweenExpressionStart instanceof NumericConstantNode)) {
        setInvalidMessage("Between min value must be a number not: " + betweenExpressionStart);
        return null;
      }
      if (!(betweenExpressionEnd instanceof NumericConstantNode)) {
        setInvalidMessage("Between max value must be a number not: " + betweenExpressionEnd);
        return null;
      }
      final Column column = toQueryValue(leftValueNode);
      final Value min = toQueryValue(betweenExpressionStart);
      final Value max = toQueryValue(betweenExpressionEnd);
      final FieldDefinition fieldDefinition = this.recordDefinition.getField(column.getName());
      min.convert(fieldDefinition);
      max.convert(fieldDefinition);
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final QueryValue leftValue = toQueryValue(leftValueNode);
      if (leftValue instanceof Condition) {
        final Condition leftCondition = (Condition)leftValue;
        final QueryValue rightValue = toQueryValue(rightValueNode);
        if (rightValue instanceof Condition) {
          final Condition rightCondition = (Condition)rightValue;
          if ("AND".equals(operator)) {
            return (V)new And(leftCondition, rightCondition);
          } else if ("OR".equals(operator)) {
            return (V)new Or(leftCondition, rightCondition);
          } else {
            setInvalidMessage("Binary logical operator " + operator + " not supported.");
            return null;
          }
        } else {
          setInvalidMessage("Right side of " + operator
            + " must be a condition (e.g. column_name = 'value') not: " + rightValue);
          return null;
        }
      } else {
        setInvalidMessage("Left side of " + operator
          + " must be a condition (e.g. column_name = 'value') not: " + leftValue);
        return null;
      }
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (QueryValue.SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final QueryValue leftCondition = toQueryValue(leftValueNode);
        QueryValue rightCondition = toQueryValue(rightValueNode);

        if (leftCondition instanceof Column) {
          if (rightCondition instanceof Value) {
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              setInvalidMessage(
                "Values can't be null for " + operator + " use IS NULL or IS NOT NULL instead.");
            } else {
              final Column column = (Column)leftCondition;

              final String name = column.getName();
              final FieldDefinition fieldDefinition = this.recordDefinition.getField(name);
              final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(name);
              if (codeTable == null || fieldDefinition == this.recordDefinition.getIdField()) {
                try {
                  final Object convertedValue = fieldDefinition.toFieldValueException(value);
                  if (convertedValue == null) {
                    setInvalidMessage("Values can't be null for " + operator
                      + " use IS NULL or IS NOT NULL instead.");
                    return null;
                  } else {
                    rightCondition = new Value(fieldDefinition, convertedValue);
                  }
                } catch (final Throwable t) {
                  setInvalidMessage(name + "='" + value + "' is not a valid "
                    + fieldDefinition.getDataType().getValidationName());
                }
              } else {
                Object id;

                if (value instanceof String) {
                  final String string = (String)value;
                  final String[] values = string.split(":");
                  id = codeTable.getIdentifier((Object[])values);
                } else {
                  id = codeTable.getIdentifier(value);
                }
                if (id == null) {
                  setInvalidMessage(name + "='" + value + "' could not be found in the code table "
                    + codeTable.getName());
                } else {
                  rightCondition = new Value(fieldDefinition, id);
                }
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition, operator,
            rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator, rightCondition);
          return (V)binaryCondition;
        }

      } else {
        setInvalidMessage("Unsupported binary operator " + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      final FieldDefinition fieldDefinition = this.recordDefinition.getField(columnName);
      if (fieldDefinition == null) {
        setInvalidMessage("Invalid field name " + columnName);
      } else {
        return (V)new Column(fieldDefinition);
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(leftValueNode);
      final QueryValue rightCondition = toQueryValue(rightValueNode);
      return (V)new ILike(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toQueryValue(operand);
      return (V)new Not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(leftOperand);

      final List<QueryValue> conditions = new ArrayList<>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final QueryValue itemCondition = toQueryValue(itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)new In(leftCondition, new CollectionValue(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final QueryValue value = toQueryValue(operand);
      if (isNullNode.getNodeType() == NodeTypes.IS_NOT_NULL_NODE) {
        return (V)new IsNotNull(value);
      } else {
        return (V)new IsNull(value);
      }
      // } else if (expression instanceof Parenthesis) {
      // final Parenthesis parenthesis = (Parenthesis)expression;
      // final ValueNode parenthesisValueNode = parenthesis.getExpression();
      // final Condition condition = toCondition(parenthesisExpression);
      // final ParenthesisCondition parenthesisCondition = new
      // ParenthesisCondition(
      // condition);
      // if (parenthesis.isNot()) {
      // return (V)Q.not(parenthesisCondition);
      // } else {
      // return (V)parenthesisCondition;
      // }
    } else if (expression instanceof RowConstructorNode) {
      final RowConstructorNode rowConstructorNode = (RowConstructorNode)expression;
      final ValueNodeList values = rowConstructorNode.getNodeList();
      final ValueNode valueNode = values.get(0);
      return (V)toQueryValue(valueNode);
    } else if (expression instanceof UserTypeConstantNode) {
      final UserTypeConstantNode constant = (UserTypeConstantNode)expression;
      final Object objectValue = constant.getObjectValue();
      return (V)new Value(objectValue);
    } else if (expression instanceof ConstantNode) {
      final ConstantNode constant = (ConstantNode)expression;
      final Object value = constant.getValue();
      return (V)new Value(value);
    } else if (expression instanceof SimpleStringOperatorNode) {
      final SimpleStringOperatorNode operatorNode = (SimpleStringOperatorNode)expression;
      final String functionName = operatorNode.getMethodName().toUpperCase();
      final ValueNode operand = operatorNode.getOperand();
      final QueryValue condition = toQueryValue(operand);
      return (V)new Function(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final QueryValue condition = toQueryValue(operand);
      return (V)new Cast(condition, typeName);
    } else if (expression == null) {
      return null;
    } else {
      setInvalidMessage("Unsupported expression" + expression.getClass() + " " + expression);
    }
    return null;
  }

  protected void updateHasSearchText() {
    final boolean fieldValid = ((Field)this.searchField).isHasValidValue();
    setHasSearchText(fieldValid);
  }

  public void verifyCondition() {
    this.validating = true;
    this.valid = true;
    this.statusLabel.setText("");
    try {
      final String whereClause = this.whereTextField.getText();
      if (Property.hasValue(whereClause)) {
        final String sql = "SELECT * FROM X WHERE " + "\n" + whereClause;
        try {
          final StatementNode statement = new SQLParser().parseStatement(sql);
          if (statement instanceof CursorNode) {
            final CursorNode selectStatement = (CursorNode)statement;
            final ResultSetNode resultSetNode = selectStatement.getResultSetNode();
            if (resultSetNode instanceof SelectNode) {
              final SelectNode selectNode = (SelectNode)resultSetNode;
              final ValueNode where = selectNode.getWhereClause();
              final QueryValue queryValue = toQueryValue(where);
              if (queryValue instanceof Condition) {
                final Condition condition = (Condition)queryValue;
                if (this.valid) {
                  setFieldValue(condition);
                  this.statusLabel.setForeground(WebColors.DarkGreen);
                  this.statusLabel.setText("Valid");
                }
              }
            }
          }
        } catch (final SQLParserException e) {
          final int offset = e.getErrorPosition();
          setInvalidMessage(offset - this.sqlPrefix.length(),
            "Error parsing SQL: " + e.getMessage());
        } catch (final StandardException e) {
          Logs.error(this, "Error parsing SQL: " + whereClause, e);
        }
      } else {
        setFieldValue(Condition.ALL);
        this.statusLabel.setForeground(WebColors.DarkGreen);
        this.statusLabel.setText("Valid");
      }
    } finally {
      if (!this.valid) {
        setFieldValue(null);
      }
      this.validating = false;
    }
  }
}
