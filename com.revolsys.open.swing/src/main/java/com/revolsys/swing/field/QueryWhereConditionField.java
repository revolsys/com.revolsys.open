package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.springframework.util.StringUtils;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BetweenOperatorNode;
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
import com.akiban.sql.unparser.NodeToString;
import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.BetweenCondition;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Cast;
import com.revolsys.gis.data.query.CollectionCondition;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.Function;
import com.revolsys.gis.data.query.MultipleCondition;
import com.revolsys.gis.data.query.Value;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.table.AttributeFilterPanel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.CollectionUtil;

public class QueryWhereConditionField extends ValueField implements
  MouseListener, CaretListener {
  private static final Map<String, Map<String, String>> BUTTON_OPERATORS = new LinkedHashMap<String, Map<String, String>>();

  private static final long serialVersionUID = 1L;

  private static final List<String> SUPPORTED_BINARY_OPERATORS = Arrays.asList(
    "AND", "OR", "+", "-", "/", "*", "=", "<>", "<", "<=", ">", ">=", "LIKE");

  static {
    addButton("equality", "=");
    addButton("equality", "<>");
    addButton("equality", "<");
    addButton("equality", "<=");
    addButton("equality", ">");
    addButton("equality", ">=");
    addButton("relational", "AND");
    addButton("relational", "OR");
    addButton("relational", "NOT");
    addButton("grouping", "( )");
    addButton("comparison", "IS NULL");
    addButton("comparison", "IS NOT NULL");
    addButton("comparison", "BETWEEN ", "BETWEEN value1 AND value2");
    addButton("comparison", "IN", "IN (value1, value2)");
    addButton("comparison", "LIKE", "LIKE '%value%'");
    addButton("math", "+");
    addButton("math", "-");
    addButton("math", "*");
    addButton("math", "/");

  }

  private static void addButton(final String group, final String operator) {
    addButton(group, operator, operator);
  }

  private static void addButton(final String group, final String label,
    final String operator) {
    CollectionUtil.addToMap(BUTTON_OPERATORS, group, label, operator);
  }

  private final JList fieldNamesList;

  private final DataObjectMetaData metaData;

  private final Color selectionColor;

  private final String sqlPrefix;

  private final JTextArea statusLabel;

  private boolean valid;

  private boolean validating;

  private final TextPane whereTextField;

  private final AttributeFilterPanel filterPanel;

  public QueryWhereConditionField(final AttributeFilterPanel filterPanel) {
    super(new BorderLayout());
    setTitle("Advanced Filter");
    final DataObjectLayer layer = filterPanel.getLayer();
    this.filterPanel = filterPanel;
    metaData = layer.getMetaData();
    final List<String> attributeNames = metaData.getAttributeNames();

    fieldNamesList = new JList(new Vector<String>(attributeNames));
    fieldNamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    final JPanel fieldNamesPanel = new JPanel(new BorderLayout());
    fieldNamesPanel.add(new JScrollPane(fieldNamesList), BorderLayout.CENTER);
    SwingUtil.setTitledBorder(fieldNamesPanel, "Field Names");
    fieldNamesList.addMouseListener(this);
    fieldNamesPanel.setMinimumSize(new Dimension(300, 100));

    final ToolBar buttonsPanel = new ToolBar();
    buttonsPanel.setOpaque(false);
    buttonsPanel.setBorderPainted(true);
    for (final Entry<String, Map<String, String>> group : BUTTON_OPERATORS.entrySet()) {
      final String groupName = group.getKey();
      final Map<String, String> buttonDefinitions = group.getValue();
      for (final Entry<String, String> buttonDefinition : buttonDefinitions.entrySet()) {
        final String title = buttonDefinition.getKey();
        final String text = buttonDefinition.getValue();
        final JButton button = buttonsPanel.addButton(groupName, title, this,
          "insertText", text);
        button.setBorderPainted(true);
      }
    }

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
    buttonsPanel.setBorderPainted(true);
    final JButton verifyButton = statusToolBar.addButton("default", "Verify",
      this, "verifyCondition");
    verifyButton.setBorderPainted(true);

    final JPanel queryPanel = new JPanel(new BorderLayout());
    SwingUtil.setTitledBorder(queryPanel, "Query");
    queryPanel.setOpaque(false);
    queryPanel.add(buttonsPanel, BorderLayout.NORTH);
    queryPanel.add(filterTextPanel, BorderLayout.CENTER);
    queryPanel.add(statusToolBar, BorderLayout.SOUTH);
    queryPanel.setPreferredSize(new Dimension(500, 400));

    statusLabel = new TextArea();
    statusLabel.setEditable(false);
    final JScrollPane statusPanel = new JScrollPane(statusLabel);
    statusPanel.setPreferredSize(new Dimension(500, 200));

    final JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      queryPanel, statusPanel);
    topBottom.setResizeWeight(0.7);

    final JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      true, fieldNamesPanel, topBottom);
    add(leftRight, BorderLayout.CENTER);

    setPreferredSize(new Dimension(1000, Math.max(650,
      Math.min(attributeNames.size() * 15, 650))));

    final Condition searchCondition = filterPanel.getSearchCondition();
    setFieldValue(searchCondition);
    if (searchCondition != null) {
      whereTextField.setText(searchCondition.toFormattedString());
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
  public void mouseClicked(final MouseEvent event) {
    if (event.getSource() == fieldNamesList) {
      if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
        final String fieldName = (String)fieldNamesList.getSelectedValue();
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
    filterPanel.setSearchCondition(condition);
  }

  @Override
  public void save(final JDialog dialog) {
    verifyCondition();
    if (valid) {
      super.save(dialog);
    } else {
      JOptionPane.showMessageDialog(
        this,
        "<html><p>Cannot save the advanced query as the SQL is valid.<p></p>Fix the SQL or use the cancel button on the Advanced Filter window to cancel the changes.<p></html>",
        "SQL Invalid", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Condition) {
      super.setFieldValue(value);
    }
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
  private <V extends Condition> V toCondition(final ValueNode expression) {
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
      final Column column = toCondition(leftValueNode);
      final Value min = toCondition(betweenExpressionStart);
      final Value max = toCondition(betweenExpressionEnd);
      final Attribute attribute = metaData.getAttribute(column.getName());
      min.convert(attribute);
      max.convert(attribute);
      return (V)new BetweenCondition(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final Condition leftCondition = toCondition(leftValueNode);
      final Condition rightCondition = toCondition(rightValueNode);
      return (V)new MultipleCondition(operator, leftCondition, rightCondition);
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final Condition leftCondition = toCondition(leftValueNode);
        Condition rightCondition = toCondition(rightValueNode);

        if (leftCondition instanceof Column) {
          if (rightCondition instanceof Value) {
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              setInvalidMessage("Values can't be null for " + operator
                + " use IS NULL or IS NOT NULL instead.");
            } else {
              final Column column = (Column)leftCondition;
              final Attribute attribute = metaData.getAttribute(column.getName());
              final Class<?> typeClass = attribute.getTypeClass();
              try {
                final Object convertedValue = StringConverterRegistry.toObject(
                  typeClass, value);
                if (convertedValue == null
                  || !typeClass.isAssignableFrom(typeClass)) {
                  setInvalidMessage(column.getName() + " requires a "
                    + attribute.getType() + " not the value " + value);
                  return null;
                } else {
                  rightCondition = new Value(convertedValue);
                }
              } catch (final Throwable t) {
                setInvalidMessage(column.getName() + " requires a "
                  + attribute.getType() + " not the value " + value);
              }
            }
          }
        }
        final BinaryCondition binaryCondition = new BinaryCondition(
          leftCondition, operator, rightCondition);
        return (V)binaryCondition;
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
        final String attributeName = attribute.getName();
        return (V)new Column(attributeName);
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final Condition leftCondition = toCondition(leftValueNode);
      final Condition rightCondition = toCondition(rightValueNode);
      return (V)Conditions.like(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toCondition(operand);
      return (V)Conditions.not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final Condition leftCondition = toCondition(leftOperand);

      final List<Condition> conditions = new ArrayList<Condition>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final Condition itemCondition = toCondition(itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)Conditions.in(leftCondition,
        new CollectionCondition(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final Condition condition = toCondition(operand);
      if (isNullNode.getNodeType() == NodeTypes.IS_NOT_NULL_NODE) {
        return (V)Conditions.isNotNull(condition);
      } else {
        return (V)Conditions.isNull(condition);
      }
      // } else if (expression instanceof Parenthesis) {
      // final Parenthesis parenthesis = (Parenthesis)expression;
      // final ValueNode parenthesisValueNode = parenthesis.getExpression();
      // final Condition condition = toCondition(parenthesisExpression);
      // final ParenthesisCondition parenthesisCondition = new
      // ParenthesisCondition(
      // condition);
      // if (parenthesis.isNot()) {
      // return (V)Conditions.not(parenthesisCondition);
      // } else {
      // return (V)parenthesisCondition;
      // }
    } else if (expression instanceof RowConstructorNode) {
      final RowConstructorNode rowConstructorNode = (RowConstructorNode)expression;
      final ValueNodeList values = rowConstructorNode.getNodeList();
      final ValueNode valueNode = values.get(0);
      return (V)toCondition(valueNode);
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
      final Condition condition = toCondition(operand);
      return (V)new Function(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final Condition condition = toCondition(operand);
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
        final String sql = sqlPrefix + "\n" + whereClause;
        try {
          final StatementNode statement = new SQLParser().parseStatement(sql);
          if (statement instanceof CursorNode) {
            final CursorNode selectStatement = (CursorNode)statement;
            final ResultSetNode resultSetNode = selectStatement.getResultSetNode();
            if (resultSetNode instanceof SelectNode) {
              final SelectNode selectNode = (SelectNode)resultSetNode;
              final ValueNode where = selectNode.getWhereClause();
              final Condition condition = toCondition(where);
              if (valid) {
                setFieldValue(condition);
                statusLabel.setForeground(WebColors.DarkGreen);
                statusLabel.setText("Valid");
                try {
                  System.out.println(new NodeToString().toString(where));
                } catch (final StandardException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                System.out.println(condition);
              }
            }
          }
        } catch (final SQLParserException e) {
          final int offset = e.getErrorPosition();
          setInvalidMessage(offset - sqlPrefix.length(), "Error parsing SQL: "
            + e.getMessage());

        } catch (final StandardException e) {
          e.printStackTrace();
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
