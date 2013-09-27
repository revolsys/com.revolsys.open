package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.toolbar.ToolBar;

public class QueryWhereConditionField extends ValueField implements
  MouseListener {
  private static final long serialVersionUID = 1L;

  private final TextArea whereTextField;

  private final JList fieldNamesList;

  private final String sqlPrefix;

  public QueryWhereConditionField(final DataObjectLayer layer) {
    super(new BorderLayout());
    setTitle("Advanced Filter");
    final DataObjectMetaData metaData = layer.getMetaData();
    final List<String> attributeNames = metaData.getAttributeNames();

    fieldNamesList = new JList(new Vector<String>(attributeNames));
    fieldNamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    final JPanel fieldNamesPanel = new JPanel(new BorderLayout());
    fieldNamesPanel.add(new JScrollPane(fieldNamesList), BorderLayout.CENTER);
    SwingUtil.setTitledBorder(fieldNamesPanel, "Field Names");
    fieldNamesList.addMouseListener(this);

    final JPanel buttonsPanel = new JPanel();
    SwingUtil.setTitledBorder(buttonsPanel, "Operators");
    for (final String operator : Arrays.asList("=", "<>", "<", "<=", ">", ">=",
      "AND", "OR", "NOT", "NULL", "LIKE", "BETWEEN", "(", ")", "()", "%", "\"",
      "'")) {
      addButton(buttonsPanel, operator);
    }

    whereTextField = new TextArea(5, 20);
    final JPanel filterTextPanel = new JPanel(new BorderLayout());
    SwingUtil.setTitledBorder(filterTextPanel, "Query");
    sqlPrefix = "SELECT * FROM "
      + metaData.getPath().substring(1).replace('/', '.') + " WHERE";
    filterTextPanel.add(new JLabel(sqlPrefix), BorderLayout.NORTH);
    filterTextPanel.add(new JScrollPane(whereTextField), BorderLayout.CENTER);

    final ToolBar toolBar = new ToolBar();
    toolBar.setOpaque(false);

    toolBar.addButton("default", "Verify", this, "verifyCondition");
    filterTextPanel.add(toolBar, BorderLayout.SOUTH);

    GroupLayoutUtil.makeColumns(buttonsPanel, 6, true);
    final JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      true, buttonsPanel, filterTextPanel);
    topBottom.setDividerLocation(130);

    final JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      true, fieldNamesPanel, topBottom);
    leftRight.setDividerLocation(250);
    add(leftRight, BorderLayout.CENTER);

    setPreferredSize(new Dimension(1000, Math.max(650,
      Math.min(attributeNames.size() * 15, 650))));
  }

  private void addButton(final JPanel buttonsPanel, final String operator) {
    final JButton button = InvokeMethodAction.createButton(operator, this,
      "insertText", operator);
    button.setMinimumSize(new Dimension(100, 22));
    buttonsPanel.add(button);
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
        || !previousText.matches(".*" + operator + "\\s*$")) {
        if (StringUtils.hasText(previousText)
          && !previousText.substring(previousText.length() - 1).matches("\\s$")) {
          whereTextField.insert(" ", position++);
        }
        whereTextField.insert(operator + " ", position);
      }
    }
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
            if (StringUtils.hasText(previousText)
              && !previousText.substring(previousText.length() - 1).matches(
                "\\s$")) {
              whereTextField.insert(" ", position++);
            }
            whereTextField.insert("\"" + fieldName + "\" ", position);
          }
        }
      }
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
  public void setFieldValue(final Object value) {
    if (value instanceof Condition) {
      final Condition condition = (Condition)value;
      whereTextField.setText(condition.toString());
      super.setFieldValue(value);
    }
  }

  public void verifyCondition() {
    final String whereClause = whereTextField.getText();
    if (StringUtils.hasText(whereClause)) {
      final String sql = sqlPrefix + " " + whereClause;
      try {
        final Statement statement = new CCJSqlParserManager().parse(new StringReader(
          sql));
        System.out.println(statement);
      } catch (final JSQLParserException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ParseException) {
          final ParseException parseException = (ParseException)cause;
          System.out.println(parseException.currentToken);
          final Set<String> tokenSet = new LinkedHashSet<String>();
          for (final int[] indexes : parseException.expectedTokenSequences) {
            final StringBuffer tokens = new StringBuffer();

            for (final int index : indexes) {
              tokens.append(parseException.tokenImage[index]);
            }
            tokenSet.add(tokens.toString());
          }
          System.out.println(tokenSet);
        } else {
          cause.printStackTrace();
        }
      }
    }
  }
}
