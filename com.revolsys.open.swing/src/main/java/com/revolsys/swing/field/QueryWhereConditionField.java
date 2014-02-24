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
import java.util.Map;

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
import org.springframework.util.StringUtils;

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
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.And;
import com.revolsys.gis.data.query.Between;
import com.revolsys.gis.data.query.Cast;
import com.revolsys.gis.data.query.CollectionValue;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.ILike;
import com.revolsys.gis.data.query.In;
import com.revolsys.gis.data.query.IsNotNull;
import com.revolsys.gis.data.query.IsNull;
import com.revolsys.gis.data.query.Not;
import com.revolsys.gis.data.query.Or;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.data.query.functions.Function;
import com.revolsys.spring.SpelUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.component.AttributeTitleStringConveter;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.CollectionUtil;

public class QueryWhereConditionField extends ValueField implements
  MouseListener, CaretListener, ItemListener {

  private static final ImageIcon ICON = SilkIconLoader.getIcon("add");

  private static final long serialVersionUID = 1L;

  public static JComponent createSearchField(final Attribute attribute,
    final CodeTable codeTable) {
    if (attribute == null) {
      return new TextField(20);
    } else {
      final String name = attribute.getName();
      final Class<?> typeClass = attribute.getTypeClass();
      final String searchFieldFactory = attribute.getProperty("searchFieldFactory");
      final DataObjectMetaData metaData = attribute.getMetaData();
      if (metaData == null) {
        return new TextField(20);
      } else {
        JComponent field;
        if (attribute.equals(metaData.getIdAttribute())) {
          field = new TextField(20);
        } else if (StringUtils.hasText(searchFieldFactory)) {
          final Map<String, Object> searchFieldFactoryParameters = attribute.getProperty("searchFieldFactoryParameters");
          field = SpelUtil.getValue(searchFieldFactory, attribute,
            searchFieldFactoryParameters);
        } else {
          if (codeTable == null) {
            if (Number.class.isAssignableFrom(typeClass)
              || String.class.isAssignableFrom(typeClass)) {
              field = new TextField(20);
            } else {
              field = SwingUtil.createField(typeClass, name, null);
            }
          } else {
            field = SwingUtil.createComboBox(codeTable, false, 30);
          }
        }
        return field;
      }
    }
  }

  private JComponent binaryConditionField;

  private final ComboBox binaryConditionOperator;

  private final BasePanel binaryConditionPanel;

  private final BasePanel inConditionPanel;

  private JComponent inConditionField;

  private final ComboBox fieldNamesList;

  private final PropertyChangeListener listener;

  private final DataObjectMetaData metaData;

  private final ComboBox rightUnaryConditionOperator;

  private final Color selectionColor;

  private final String sqlPrefix;

  private final JTextArea statusLabel;

  private boolean valid;

  private boolean validating;

  private final TextPane whereTextField;

  private CodeTable codeTable;

  private final BasePanel likePanel;

  private final TextField likeConditionField;

  private final Condition originalFilter;

  public QueryWhereConditionField(final AbstractDataObjectLayer layer,
    final PropertyChangeListener listener, final Condition filter) {
    this(layer, listener, filter, null);
  }

  public QueryWhereConditionField(final AbstractDataObjectLayer layer,
    final PropertyChangeListener listener, final Condition filter,
    final String query) {
    super(new BorderLayout());
    setTitle("Advanced Search");
    this.originalFilter = filter;
    this.listener = listener;
    metaData = layer.getMetaData();
    final List<Attribute> attributes = metaData.getAttributes();

    fieldNamesList = new ComboBox(new AttributeTitleStringConveter(layer),
      false, attributes);
    fieldNamesList.addItemListener(this);
    fieldNamesList.addMouseListener(this);

    final BasePanel fieldNamePanel = new BasePanel(fieldNamesList);
    GroupLayoutUtil.makeColumns(fieldNamePanel, 1, false);

    binaryConditionOperator = new ComboBox("=", "<>", "<", "<=", ">", ">=");
    final JButton binaryConditionAddButton = InvokeMethodAction.createButton(
      "", "Add Binary Condition", ICON, this, "actionAddBinaryCondition");
    binaryConditionPanel = new BasePanel(binaryConditionOperator,
      binaryConditionAddButton);
    setBinaryConditionField(null);

    rightUnaryConditionOperator = new ComboBox("IS NULL", "IS NOT NULL");
    final JButton rightUnaryConditionAddButton = InvokeMethodAction.createButton(
      "", "Add Unary Condition", ICON, this, "actionAddRightUnaryCondition");
    final BasePanel rightUnaryConditionPanel = new BasePanel(
      rightUnaryConditionOperator, rightUnaryConditionAddButton);
    GroupLayoutUtil.makeColumns(rightUnaryConditionPanel, false);

    final JButton likeConditionAddButton = InvokeMethodAction.createButton("",
      "Add Unary Condition", ICON, this, "actionAddLikeCondition");
    likeConditionField = new TextField(20);
    likePanel = new BasePanel(SwingUtil.createLabel("LIKE"), new JLabel(" '%"),
      likeConditionField, new JLabel("%' "), likeConditionAddButton);
    GroupLayoutUtil.makeColumns(likePanel, false);

    final JButton inConditionAddButton = InvokeMethodAction.createButton("",
      "Add Unary Condition", ICON, this, "actionAddInCondition");
    inConditionField = new TextField(20);
    inConditionPanel = new BasePanel(SwingUtil.createLabel("IN"),
      inConditionField, inConditionAddButton);
    setInConditionField(null);

    final BasePanel operatorPanel = new BasePanel(new VerticalLayout(),
      binaryConditionPanel, rightUnaryConditionPanel, likePanel,
      inConditionPanel);

    final BasePanel fieldConditions = new BasePanel(fieldNamePanel,
      operatorPanel);
    GroupLayoutUtil.makeColumns(fieldConditions, 2, false);

    final ToolBar buttonsPanel = new ToolBar();
    buttonsPanel.setBorderPainted(true);
    buttonsPanel.addButton("relational", "AND", this, "insertText", "AND")
      .setBorderPainted(true);
    buttonsPanel.addButton("relational", "OR", this, "insertText", "OR")
      .setBorderPainted(true);
    buttonsPanel.addButton("relational", "NOT", this, "insertText", "NOT")
      .setBorderPainted(true);
    buttonsPanel.addButton("grouping", "( )", this, "insertText", "( )")
      .setBorderPainted(true);
    buttonsPanel.addButton("math", "+", this, "insertText", "+")
      .setBorderPainted(true);
    buttonsPanel.addButton("math", "-", this, "insertText", "-")
      .setBorderPainted(true);
    buttonsPanel.addButton("math", "*", this, "insertText", "*")
      .setBorderPainted(true);
    buttonsPanel.addButton("math", "/", this, "insertText", "/")
      .setBorderPainted(true);

    final BasePanel widgetPanel = new BasePanel(new VerticalLayout(5),
      fieldConditions, buttonsPanel);

    whereTextField = new TextPane(5, 20);
    whereTextField.setFont(new Font("Monospaced", Font.PLAIN, 11));
    selectionColor = whereTextField.getSelectionColor();
    whereTextField.addCaretListener(this);

    final JScrollPane whereScroll = new JScrollPane(whereTextField);
    // whereTextField.setContentType("text/sql"); // Requires the above scroll
    // pane

    sqlPrefix = "SELECT * FROM "
      + metaData.getPath().substring(1).replace('/', '.') + " WHERE";

    final JPanel filterTextPanel = new JPanel(new BorderLayout());
    filterTextPanel.setOpaque(false);
    filterTextPanel.add(new JLabel(sqlPrefix), BorderLayout.NORTH);
    filterTextPanel.add(whereScroll, BorderLayout.CENTER);

    final ToolBar statusToolBar = new ToolBar();
    statusToolBar.setOpaque(false);
    final JButton verifyButton = statusToolBar.addButton("default", "Verify",
      this, "verifyCondition");
    verifyButton.setBorderPainted(true);

    final JPanel queryPanel = new JPanel(new BorderLayout());
    SwingUtil.setTitledBorder(queryPanel, "Query");
    queryPanel.setOpaque(false);
    queryPanel.add(widgetPanel, BorderLayout.NORTH);
    queryPanel.add(filterTextPanel, BorderLayout.CENTER);
    queryPanel.add(statusToolBar, BorderLayout.SOUTH);
    queryPanel.setPreferredSize(new Dimension(500, 400));

    statusLabel = new TextArea();
    statusLabel.setEditable(false);
    final JScrollPane statusPane = new JScrollPane(statusLabel);
    statusPane.setPreferredSize(new Dimension(500, 100));

    final JPanel statusPanel = new JPanel(new BorderLayout());
    SwingUtil.setTitledBorder(statusPanel, "Messages");
    statusPanel.setOpaque(false);
    statusPanel.add(statusPane, BorderLayout.CENTER);

    final JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      queryPanel, statusPanel);
    topBottom.setResizeWeight(0.7);

    add(topBottom, BorderLayout.CENTER);

    setPreferredSize(new Dimension(800, 500));

    setFieldValue(filter);
    if (filter != null) {
      whereTextField.setText(filter.toFormattedString());
    }
    if (StringUtils.hasText(query)) {
      whereTextField.setText(query);
    }
    final String searchField = layer.getProperty("searchField");
    final Attribute searchAttribute = metaData.getAttribute(searchField);
    if (searchAttribute == null) {
      fieldNamesList.setSelectedIndex(0);
    } else {
      fieldNamesList.setSelectedItem(searchAttribute);
    }
  }

  public QueryWhereConditionField(final AbstractDataObjectLayer layer,
    final PropertyChangeListener listener, final String query) {
    this(layer, listener, null, query);
  }

  public void actionAddBinaryCondition() {
    final Attribute attribute = (Attribute)fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final String operator = (String)binaryConditionOperator.getSelectedItem();
      if (StringUtils.hasText(operator)) {
        Object fieldValue = ((Field)binaryConditionField).getFieldValue();
        if (fieldValue != null) {
          final int position = whereTextField.getCaretPosition();
          Class<?> attributeClass = attribute.getTypeClass();
          if (codeTable == null) {
            try {
              fieldValue = StringConverterRegistry.toObject(attributeClass,
                fieldValue);
            } catch (final Throwable e) {
              setInvalidMessage(fieldValue + " is not a valid "
                + attribute.getType());
              return;
            }
          } else {
            final List<Object> values = codeTable.getValues(fieldValue);
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

            final Document document = whereTextField.getDocument();
            final StringBuffer text = new StringBuffer();
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
              LoggerFactory.getLogger(getClass()).error(
                "Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  public void actionAddInCondition() {
    final Attribute attribute = (Attribute)fieldNamesList.getSelectedItem();
    if (attribute != null) {
      Object fieldValue = ((Field)inConditionField).getFieldValue();
      if (fieldValue != null) {
        int position = whereTextField.getCaretPosition();
        Class<?> attributeClass = attribute.getTypeClass();
        if (fieldValue != null) {
          if (codeTable == null) {
            try {
              fieldValue = StringConverterRegistry.toObject(attributeClass,
                fieldValue);
            } catch (final Throwable e) {
              setInvalidMessage(fieldValue + " is not a valid "
                + attribute.getType());
              return;
            }
          } else {
            fieldValue = codeTable.getValue(fieldValue);
            if (fieldValue != null) {
              attributeClass = fieldValue.getClass();
            }
          }
          if (fieldValue != null) {
            final StringBuffer text = new StringBuffer();
            try {
              final Document document = whereTextField.getDocument();
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
              LoggerFactory.getLogger(getClass()).error(
                "Error inserting text: " + text, e);

            }
          }
        }
      }
    }
  }

  public void actionAddLikeCondition() {
    final Attribute attribute = (Attribute)fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final Object fieldValue = ((Field)likeConditionField).getFieldValue();
      if (fieldValue != null) {
        if (codeTable == null) {
          final int position = whereTextField.getCaretPosition();
          final Class<?> attributeClass = attribute.getTypeClass();
          if (fieldValue != null) {
            final String valueString = StringConverterRegistry.toString(
              attributeClass, fieldValue);

            final Document document = whereTextField.getDocument();
            final StringBuffer text = new StringBuffer();
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
              LoggerFactory.getLogger(getClass()).error(
                "Error inserting text: " + text, e);
            }
          }
        }
      }
    }
  }

  public void actionAddRightUnaryCondition() {
    final Attribute attribute = (Attribute)fieldNamesList.getSelectedItem();
    if (attribute != null) {
      final String operator = (String)rightUnaryConditionOperator.getSelectedItem();
      if (StringUtils.hasText(operator)) {
        final int position = whereTextField.getCaretPosition();

        final Document document = whereTextField.getDocument();
        final StringBuffer text = new StringBuffer();
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
          LoggerFactory.getLogger(getClass()).error(
            "Error inserting text: " + text, e);
        }
      }
    }
  }

  public void appendValue(final StringBuffer text, final Class<?> type,
    final Object value) {
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
    if (!validating) {
      whereTextField.setSelectionColor(selectionColor);
      repaint();
    }
  }

  public void insertText(final String operator) {
    if (StringUtils.hasText(operator)) {
      int position = whereTextField.getCaretPosition();
      String previousText;
      try {
        previousText = whereTextField.getText(0, position);
      } catch (final BadLocationException e) {
        previousText = "";
      }
      if (!StringUtils.hasText(previousText)
        || !previousText.matches(".*"
          + operator.replaceAll("\\(", "\\\\(")
            .replaceAll("\\)", "\\\\)")
            .replaceAll("\\*", "\\\\*")
            .replaceAll("\\+", "\\\\+") + "\\s*$")) {
        final Document document = whereTextField.getDocument();
        try {
          if (StringUtils.hasText(previousText)
            && !previousText.substring(previousText.length() - 1).matches(
              "\\s$")) {
            document.insertString(position++, " ", null);
          }
          document.insertString(position, operator + " ", null);
        } catch (final BadLocationException e) {
        }
      }
    }
    whereTextField.requestFocusInWindow();
  }

  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == fieldNamesList) {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        final Attribute attribute = (Attribute)event.getItem();
        final String name = attribute.getName();
        codeTable = metaData.getCodeTableByColumn(name);
        final JComponent binaryConditionField = createSearchField(attribute,
          codeTable);
        if (binaryConditionField instanceof DataStoreQueryTextField) {
          final DataStoreQueryTextField queryField = (DataStoreQueryTextField)binaryConditionField;
          queryField.setBelow(true);
        }
        final JComponent inConditionField = createSearchField(attribute,
          codeTable);
        if (inConditionField instanceof DataStoreQueryTextField) {
          final DataStoreQueryTextField queryField = (DataStoreQueryTextField)inConditionField;
          queryField.setBelow(true);
        }

        if (codeTable == null) {
          if (binaryConditionField instanceof DateField) {
            likePanel.setVisible(false);
          } else {
            likePanel.setVisible(true);
          }
        } else {
          likePanel.setVisible(false);
        }
        setBinaryConditionField(binaryConditionField);
        setInConditionField(inConditionField);
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getSource() == fieldNamesList) {
      if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
        final String fieldName = (String)fieldNamesList.getSelectedItem();
        if (StringUtils.hasText(fieldName)) {
          int position = whereTextField.getCaretPosition();
          String previousText;
          try {
            previousText = whereTextField.getText(0, position);
          } catch (final BadLocationException e) {
            previousText = "";
          }
          if (!StringUtils.hasText(previousText)
            || !previousText.matches(".*\"?" + fieldName + "\"?\\s*$")) {
            final Document document = whereTextField.getDocument();
            try {
              if (StringUtils.hasText(previousText)
                && !previousText.substring(previousText.length() - 1).matches(
                  "\\s$")) {
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
      whereTextField.requestFocusInWindow();
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
    listener.propertyChange(new PropertyChangeEvent(this, "filter",
      originalFilter, condition));
  }

  @Override
  public void save(final JDialog dialog) {
    verifyCondition();
    if (valid) {
      super.save(dialog);
    } else {
      JOptionPane.showMessageDialog(
        this,
        "<html><p>Cannot save the advanced query as the SQL is valid.<p></p>Fix the SQL or use the cancel button on the Advanced Search window to cancel the changes.<p></html>",
        "SQL Invalid", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setBinaryConditionField(JComponent field) {
    if (binaryConditionField != null) {
      binaryConditionPanel.remove(1);
    }
    if (field == null) {
      field = new TextField(20);
    }
    binaryConditionPanel.add(field, 1);
    GroupLayoutUtil.makeColumns(binaryConditionPanel, false);
    this.binaryConditionField = field;
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Condition) {
      super.setFieldValue(value);
    }
  }

  private void setInConditionField(JComponent field) {
    if (inConditionField != null) {
      inConditionPanel.remove(1);
    }
    if (field == null) {
      field = new TextField(20);
    }

    inConditionPanel.add(field, 1);
    GroupLayoutUtil.makeColumns(inConditionPanel, false);
    this.inConditionField = field;
  }

  protected void setInvalidMessage(final int offset, final String message) {
    if (offset >= 0) {
      whereTextField.setSelectionColor(WebColors.Pink);
      whereTextField.setSelectionStart(offset);
      final Document document = whereTextField.getDocument();
      whereTextField.setSelectionEnd(document.getLength());
      whereTextField.requestFocusInWindow();
    }
    setInvalidMessage(message);
  }

  protected void setInvalidMessage(final String message) {
    statusLabel.setForeground(WebColors.Red);
    statusLabel.append(message + "\n");
    valid = false;
    statusLabel.setCaretPosition(0);
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
        setInvalidMessage("Between operator must use a column name not: "
          + leftValueNode);
        return null;
      }

      if (!(betweenExpressionStart instanceof NumericConstantNode)) {
        setInvalidMessage("Between min value must be a number not: "
          + betweenExpressionStart);
        return null;
      }
      if (!(betweenExpressionEnd instanceof NumericConstantNode)) {
        setInvalidMessage("Between max value must be a number not: "
          + betweenExpressionEnd);
        return null;
      }
      final Column column = toQueryValue(leftValueNode);
      final Value min = toQueryValue(betweenExpressionStart);
      final Value max = toQueryValue(betweenExpressionEnd);
      final Attribute attribute = metaData.getAttribute(column.getName());
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
        setInvalidMessage("Binary logical operator " + operator
          + " not supported.");
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
              setInvalidMessage("Values can't be null for " + operator
                + " use IS NULL or IS NOT NULL instead.");
            } else {
              final Column column = (Column)leftCondition;

              final String name = column.getName();
              final Attribute attribute = metaData.getAttribute(name);
              final CodeTable codeTable = metaData.getCodeTableByColumn(name);
              if (codeTable == null || attribute == metaData.getIdAttribute()) {
                final Class<?> typeClass = attribute.getTypeClass();
                try {
                  final Object convertedValue = StringConverterRegistry.toObject(
                    typeClass, value);
                  if (convertedValue == null
                    || !typeClass.isAssignableFrom(typeClass)) {
                    setInvalidMessage(name + " requires a "
                      + attribute.getType() + " not the value " + value);
                    return null;
                  } else {
                    rightCondition = new Value(attribute, convertedValue);
                  }
                } catch (final Throwable t) {
                  setInvalidMessage(name + " requires a " + attribute.getType()
                    + " not the value " + value);
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
                  setInvalidMessage(name
                    + " requires a valid code value that exists not " + value);
                } else {
                  rightCondition = new Value(attribute, id);
                }
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition,
            operator, rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator,
            rightCondition);
          return (V)binaryCondition;
        }

      } else {
        setInvalidMessage("Unsupported binary operator " + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      final Attribute attribute = metaData.getAttribute(columnName);
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
      setInvalidMessage("Unsupported expression" + expression.getClass() + " "
        + expression);
    }
    return null;
  }

  public void verifyCondition() {
    validating = true;
    valid = true;
    statusLabel.setText("");
    try {
      final String whereClause = whereTextField.getText();
      if (StringUtils.hasText(whereClause)) {
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
              if (valid) {
                setFieldValue(condition);
                statusLabel.setForeground(WebColors.DarkGreen);
                statusLabel.setText("Valid");
              }
            }
          }
        } catch (final SQLParserException e) {
          final int offset = e.getErrorPosition();
          setInvalidMessage(offset - sqlPrefix.length(), "Error parsing SQL: "
            + e.getMessage());
        } catch (final StandardException e) {
          LoggerFactory.getLogger(getClass()).error(
            "Error parsing SQL: " + whereClause, e);
        }
      } else {
        statusLabel.setForeground(WebColors.DarkGreen);
        statusLabel.setText("Valid");
      }
    } finally {
      if (!valid) {
        setFieldValue(null);
      }
      validating = false;
    }
  }
}
