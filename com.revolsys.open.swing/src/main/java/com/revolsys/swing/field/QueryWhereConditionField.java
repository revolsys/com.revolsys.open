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
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

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
import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.And;
import com.revolsys.data.query.Between;
import com.revolsys.data.query.Cast;
import com.revolsys.data.query.CollectionValue;
import com.revolsys.data.query.Column;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.ILike;
import com.revolsys.data.query.In;
import com.revolsys.data.query.IsNotNull;
import com.revolsys.data.query.IsNull;
import com.revolsys.data.query.Not;
import com.revolsys.data.query.Or;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.Value;
import com.revolsys.data.query.functions.Function;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.component.AttributeTitleStringConveter;
import com.revolsys.swing.map.layer.record.component.RecordLayerFields;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class QueryWhereConditionField extends ValueField
  implements MouseListener, CaretListener, ItemListener {

  private static final ImageIcon ICON = Icons.getIcon("add");

  private static final long serialVersionUID = 1L;

  public static JComponent createSearchField(final AbstractRecordLayer layer,
    final FieldDefinition fieldDefinition, final CodeTable codeTable) {
    if (fieldDefinition == null) {
      return new TextField(20);
    } else {
      final String fieldName = fieldDefinition.getName();
      return RecordLayerFields.createCompactField(layer, fieldName, true);
    }
  }

  private JComponent binaryConditionField;

  private final ComboBox binaryConditionOperator;

  private final BasePanel binaryConditionPanel;

  private CodeTable codeTable;

  private final ComboBox fieldNamesList;

  private JComponent inConditionField;

  private final BasePanel inConditionPanel;

  private final AbstractRecordLayer layer;

  private final TextField likeConditionField;

  private final BasePanel likePanel;

  private final PropertyChangeListener listener;

  private final Condition originalFilter;

  private final RecordDefinition recordDefinition;

  private final ComboBox rightUnaryConditionOperator;

  private final Color selectionColor;

  private final String sqlPrefix;

  private final JTextArea statusLabel;

  private boolean valid;

  private boolean validating;

  private final TextPane whereTextField;

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
    final List<FieldDefinition> attributes = this.recordDefinition.getFields();

    this.fieldNamesList = new ComboBox(new AttributeTitleStringConveter(layer), false, attributes);
    this.fieldNamesList.addItemListener(this);
    this.fieldNamesList.addMouseListener(this);

    final BasePanel fieldNamePanel = new BasePanel(this.fieldNamesList);
    GroupLayoutUtil.makeColumns(fieldNamePanel, 1, false);

    this.binaryConditionOperator = new ComboBox("=", "<>", "<", "<=", ">", ">=");
    final JButton binaryConditionAddButton = InvokeMethodAction.createButton("",
      "Add Binary Condition", ICON, this, "actionAddBinaryCondition");
    this.binaryConditionPanel = new BasePanel(this.binaryConditionOperator,
      binaryConditionAddButton);
    setBinaryConditionField(null);

    this.rightUnaryConditionOperator = new ComboBox("IS NULL", "IS NOT NULL");
    final JButton rightUnaryConditionAddButton = InvokeMethodAction.createButton("",
      "Add Unary Condition", ICON, this, "actionAddRightUnaryCondition");
    final BasePanel rightUnaryConditionPanel = new BasePanel(this.rightUnaryConditionOperator,
      rightUnaryConditionAddButton);
    GroupLayoutUtil.makeColumns(rightUnaryConditionPanel, false);

    final JButton likeConditionAddButton = InvokeMethodAction.createButton("",
      "Add Unary Condition", ICON, this, "actionAddLikeCondition");
    this.likeConditionField = new TextField(20);
    this.likePanel = new BasePanel(SwingUtil.createLabel("LIKE"), new JLabel(" '%"),
      this.likeConditionField, new JLabel("%' "), likeConditionAddButton);
    GroupLayoutUtil.makeColumns(this.likePanel, false);

    final JButton inConditionAddButton = InvokeMethodAction.createButton("", "Add Unary Condition",
      ICON, this, "actionAddInCondition");
    this.inConditionField = new TextField(20);
    this.inConditionPanel = new BasePanel(SwingUtil.createLabel("IN"), this.inConditionField,
      inConditionAddButton);
    setInConditionField(null);

    final BasePanel operatorPanel = new BasePanel(new VerticalLayout(), this.binaryConditionPanel,
      rightUnaryConditionPanel, this.likePanel, this.inConditionPanel);

    final BasePanel fieldConditions = new BasePanel(fieldNamePanel, operatorPanel);
    GroupLayoutUtil.makeColumns(fieldConditions, 2, false);

    final ToolBar buttonsPanel = new ToolBar();
    buttonsPanel.setBorderPainted(true);
    buttonsPanel.addButton("relational", "AND", this, "insertText", "AND").setBorderPainted(true);
    buttonsPanel.addButton("relational", "OR", this, "insertText", "OR").setBorderPainted(true);
    buttonsPanel.addButton("relational", "NOT", this, "insertText", "NOT").setBorderPainted(true);
    buttonsPanel.addButton("grouping", "( )", this, "insertText", "( )").setBorderPainted(true);
    buttonsPanel.addButton("math", "+", this, "insertText", "+").setBorderPainted(true);
    buttonsPanel.addButton("math", "-", this, "insertText", "-").setBorderPainted(true);
    buttonsPanel.addButton("math", "*", this, "insertText", "*").setBorderPainted(true);
    buttonsPanel.addButton("math", "/", this, "insertText", "/").setBorderPainted(true);

    final BasePanel widgetPanel = new BasePanel(new VerticalLayout(5), fieldConditions,
      buttonsPanel);

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
    final JButton verifyButton = statusToolBar.addButton("default", "Verify", this,
      "verifyCondition");
    verifyButton.setBorderPainted(true);

    final JPanel queryPanel = new JPanel(new BorderLayout());
    SwingUtil.setTitledBorder(queryPanel, "Query");
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
    SwingUtil.setTitledBorder(statusPanel, "Messages");
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
    final FieldDefinition searchAttribute = this.recordDefinition.getField(searchField);
    if (searchAttribute == null) {
      this.fieldNamesList.setSelectedIndex(0);
    } else {
      this.fieldNamesList.setSelectedItem(searchAttribute);
    }
  }

  public QueryWhereConditionField(final AbstractRecordLayer layer,
    final PropertyChangeListener listener, final String query) {
    this(layer, listener, null, query);
  }

  public void actionAddBinaryCondition() {
    final FieldDefinition attribute = (FieldDefinition)this.fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final String operator = (String)this.binaryConditionOperator.getSelectedItem();
      if (Property.hasValue(operator)) {
        Object fieldValue = ((Field)this.binaryConditionField).getFieldValue();
        if (fieldValue != null) {
          final int position = this.whereTextField.getCaretPosition();
          Class<?> attributeClass = attribute.getTypeClass();
          if (this.codeTable == null) {
            try {
              fieldValue = StringConverterRegistry.toObject(attributeClass, fieldValue);
            } catch (final Throwable e) {
              setInvalidMessage(
                "'" + fieldValue + "' is not a valid " + attribute.getType().getValidationName());
              return;
            }
          } else {
            final List<Object> values = this.codeTable
              .getValues(Identifier.create(fieldValue));
            if (values.size() == 1) {
              fieldValue = values.get(0);
            } else {
              fieldValue = CollectionUtil.toString(":", values);
            }
            if (fieldValue != null) {
              attributeClass = fieldValue.getClass();
            }
          }
          if (fieldValue != null) {

            final Document document = this.whereTextField.getDocument();
            final StringBuilder text = new StringBuilder();
            if (position > 0) {
              text.append(" ");
            }
            text.append(attribute.getName());
            text.append(" ");
            text.append(operator);
            text.append(" ");
            appendValue(text, attributeClass, fieldValue);
            text.append(" ");
            try {
              document.insertString(position, text.toString(), null);
            } catch (final BadLocationException e) {
              LoggerFactory.getLogger(getClass()).error("Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  public void actionAddInCondition() {
    final FieldDefinition attribute = (FieldDefinition)this.fieldNamesList.getSelectedItem();
    if (attribute != null) {
      Object fieldValue = ((Field)this.inConditionField).getFieldValue();
      if (fieldValue != null) {
        int position = this.whereTextField.getCaretPosition();
        Class<?> attributeClass = attribute.getTypeClass();
        if (fieldValue != null) {
          if (this.codeTable == null) {
            try {
              fieldValue = StringConverterRegistry.toObject(attributeClass, fieldValue);
            } catch (final Throwable e) {
              setInvalidMessage(
                "'" + fieldValue + "' is not a valid " + attribute.getType().getValidationName());
              return;
            }
          } else {
            fieldValue = this.codeTable.getValue(Identifier.create(fieldValue));
            if (fieldValue != null) {
              attributeClass = fieldValue.getClass();
            }
          }
          if (fieldValue != null) {
            final StringBuilder text = new StringBuilder();
            try {
              final Document document = this.whereTextField.getDocument();
              final String currentText = document.getText(0, position);
              final String endText = attribute.getName() + " IN (.+) $";
              if (currentText.matches(endText)) {
                position -= 2;
                text.append(", ");
                appendValue(text, attributeClass, fieldValue);
              } else {

                if (position > 0) {
                  text.append(" ");
                }
                text.append(attribute.getName());
                text.append(" IN (");
                appendValue(text, attributeClass, fieldValue);
                text.append(") ");
              }
              document.insertString(position, text.toString(), null);
            } catch (final BadLocationException e) {
              LoggerFactory.getLogger(getClass()).error("Error inserting text: " + text, e);

            }
          }
        }
      }
    }
  }

  public void actionAddLikeCondition() {
    final FieldDefinition attribute = (FieldDefinition)this.fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final Object fieldValue = ((Field)this.likeConditionField).getFieldValue();
      if (fieldValue != null) {
        if (this.codeTable == null) {
          final int position = this.whereTextField.getCaretPosition();
          final Class<?> attributeClass = attribute.getTypeClass();
          if (fieldValue != null) {
            final String valueString = StringConverterRegistry.toString(attributeClass, fieldValue);

            final Document document = this.whereTextField.getDocument();
            final StringBuilder text = new StringBuilder();
            if (position > 0) {
              text.append(" ");
            }
            text.append(attribute.getName());
            text.append(" LIKE '%");
            text.append(valueString.replaceAll("'", "''"));
            text.append("%' ");
            try {
              document.insertString(position, text.toString(), null);
            } catch (final BadLocationException e) {
              LoggerFactory.getLogger(getClass()).error("Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  public void actionAddRightUnaryCondition() {
    final FieldDefinition attribute = (FieldDefinition)this.fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final String operator = (String)this.rightUnaryConditionOperator.getSelectedItem();
      if (Property.hasValue(operator)) {
        final int position = this.whereTextField.getCaretPosition();

        final Document document = this.whereTextField.getDocument();
        final StringBuilder text = new StringBuilder();
        if (position > 0) {
          text.append(" ");
        }
        text.append(attribute.getName());
        text.append(" ");
        text.append(operator);
        text.append(" ");

        try {
          document.insertString(position, text.toString(), null);
        } catch (final BadLocationException e) {
          LoggerFactory.getLogger(getClass()).error("Error inserting text: " + text, e);
        }
      }
    }
  }

  public void appendValue(final StringBuilder text, final Class<?> type, final Object value) {
    final String valueString = StringConverterRegistry.toString(type, value);
    if (Date.class.isAssignableFrom(type)) {
      text.append("{d '" + valueString + "'}");
    } else if (Time.class.isAssignableFrom(type)) {
      text.append("{t '" + valueString + "'}");
    } else if (Timestamp.class.isAssignableFrom(type)) {
      text.append("{ts '" + valueString + "'}");
    } else if (java.util.Date.class.isAssignableFrom(type)) {
      text.append("{ts '" + valueString + "'}");
    } else if (Number.class.isAssignableFrom(type)) {
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

  public void insertText(final String operator) {
    if (Property.hasValue(operator)) {
      int position = this.whereTextField.getCaretPosition();
      String previousText;
      try {
        previousText = this.whereTextField.getText(0, position);
      } catch (final BadLocationException e) {
        previousText = "";
      }
      if (!Property.hasValue(previousText) || !previousText.matches(".*"
        + operator.replaceAll("\\(", "\\\\(")
          .replaceAll("\\)", "\\\\)")
          .replaceAll("\\*", "\\\\*")
          .replaceAll("\\+", "\\\\+")
        + "\\s*$")) {
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
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == this.fieldNamesList) {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        final FieldDefinition field = (FieldDefinition)event.getItem();
        final String fieldName = field.getName();
        this.codeTable = this.recordDefinition.getCodeTableByFieldName(fieldName);
        final JComponent binaryConditionField = createSearchField(this.layer, field,
          this.codeTable);
        final JComponent inConditionField = createSearchField(this.layer, field, this.codeTable);

        if (this.codeTable == null) {
          if (binaryConditionField instanceof DateField) {
            this.likePanel.setVisible(false);
          } else {
            this.likePanel.setVisible(true);
          }
        } else {
          this.likePanel.setVisible(false);
        }
        setBinaryConditionField(binaryConditionField);
        setInConditionField(inConditionField);
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getSource() == this.fieldNamesList) {
      if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
        final String fieldName = (String)this.fieldNamesList.getSelectedItem();
        if (Property.hasValue(fieldName)) {
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
      JOptionPane.showMessageDialog(this,
        "<html><p>Cannot save the advanced query as the SQL is valid.<p></p>Fix the SQL or use the cancel button on the Advanced Search window to cancel the changes.<p></html>",
        "SQL Invalid", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setBinaryConditionField(JComponent field) {
    if (this.binaryConditionField != null) {
      this.binaryConditionPanel.remove(1);
    }
    if (field == null) {
      field = new TextField(20);
    }
    this.binaryConditionPanel.add(field, 1);
    GroupLayoutUtil.makeColumns(this.binaryConditionPanel, false);
    this.binaryConditionField = field;
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Condition) {
      super.setFieldValue(value);
    }
  }

  private void setInConditionField(JComponent field) {
    if (this.inConditionField != null) {
      this.inConditionPanel.remove(1);
    }
    if (field == null) {
      field = new TextField(20);
    }

    this.inConditionPanel.add(field, 1);
    GroupLayoutUtil.makeColumns(this.inConditionPanel, false);
    this.inConditionField = field;
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
      final FieldDefinition attribute = this.recordDefinition.getField(column.getName());
      min.convert(attribute);
      max.convert(attribute);
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final Condition leftCondition = toQueryValue(leftValueNode);
      final Condition rightCondition = toQueryValue(rightValueNode);
      if ("AND".equals(operator)) {
        return (V)new And(leftCondition, rightCondition);
      } else if ("OR".equals(operator)) {
        return (V)new Or(leftCondition, rightCondition);
      } else {
        setInvalidMessage("Binary logical operator " + operator + " not supported.");
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
              final FieldDefinition attribute = this.recordDefinition.getField(name);
              final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(name);
              if (codeTable == null || attribute == this.recordDefinition.getIdField()) {
                final Class<?> typeClass = attribute.getTypeClass();
                try {
                  final Object convertedValue = StringConverterRegistry.toObject(typeClass, value);
                  if (convertedValue == null || !typeClass.isAssignableFrom(typeClass)) {
                    setInvalidMessage(name + "='" + value + "' is not a valid "
                      + attribute.getType().getValidationName());
                    return null;
                  } else {
                    rightCondition = new Value(attribute, convertedValue);
                  }
                } catch (final Throwable t) {
                  setInvalidMessage(name + "='" + value + "' is not a valid "
                    + attribute.getType().getValidationName());
                }
              } else {
                Object id;

                if (value instanceof String) {
                  final String string = (String)value;
                  final String[] values = string.split(":");
                  id = codeTable.getId((Object[])values);
                } else {
                  id = codeTable.getId(value);
                }
                if (id == null) {
                  setInvalidMessage(name + "='" + value + "' could not be found in the code table "
                    + codeTable.getName());
                } else {
                  rightCondition = new Value(attribute, id);
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
      final FieldDefinition attribute = this.recordDefinition.getField(columnName);
      if (attribute == null) {
        setInvalidMessage("Invalid column name " + columnName);
      } else {
        return (V)new Column(attribute);
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

      final List<QueryValue> conditions = new ArrayList<QueryValue>();
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
              final Condition condition = toQueryValue(where);
              if (this.valid) {
                setFieldValue(condition);
                this.statusLabel.setForeground(WebColors.DarkGreen);
                this.statusLabel.setText("Valid");
              }
            }
          }
        } catch (final SQLParserException e) {
          final int offset = e.getErrorPosition();
          setInvalidMessage(offset - this.sqlPrefix.length(),
            "Error parsing SQL: " + e.getMessage());
        } catch (final StandardException e) {
          LoggerFactory.getLogger(getClass()).error("Error parsing SQL: " + whereClause, e);
        }
      } else {
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
