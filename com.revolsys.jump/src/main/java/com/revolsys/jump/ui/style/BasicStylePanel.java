/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.revolsys.jump.ui.style;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.SpringLayout.Constraints;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TransparencyPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.FillPatternFactory;
import com.vividsolutions.jump.workbench.ui.style.AbstractPalettePanel;
import com.vividsolutions.jump.workbench.ui.style.GridPalettePanel;
import com.vividsolutions.jump.workbench.ui.style.ListPalettePanel;

@SuppressWarnings("serial")
public class BasicStylePanel extends JPanel {
  private static final long serialVersionUID = -8308677294373713456L;

  protected static final int SLIDER_TEXT_FIELD_COLUMNS = 3;

  protected static final Dimension SLIDER_DIMENSION = new Dimension(130, 49);

  private Paint[] fillPatterns = new FillPatternFactory().createFillPatterns();

  private JPanel centerPanel = new JPanel();

  private AbstractPalettePanel palettePanel;

  private JCheckBox enabledCheckBox = new JCheckBox();

  private JCheckBox fillCheckBox = new JCheckBox();

  private JCheckBox lineCheckBox = new JCheckBox();

  private TransparencyPanel transparencyPanel = new TransparencyPanel();

  private JLabel transparencyLabel = new JLabel();

  private ColorChooserPanel lineColorChooserPanel = new ColorChooserPanel();

  private ColorChooserPanel fillColorChooserPanel = new ColorChooserPanel();

  private JLabel lineWidthLabel = new JLabel();

  private JCheckBox synchronizeCheckBox = new JCheckBox();

  private JCheckBox linePatternCheckBox = new JCheckBox();

  private JCheckBox fillPatternCheckBox = new JCheckBox();

  private String[] linePatterns = new String[] {
    "1", "3", "5", "5,1", "7", "7,12", "9", "9,2", "15,6", "20,3"
  };

  private JComboBox linePatternComboBox = new JComboBox(linePatterns) {
    private static final long serialVersionUID = -6894293449186434880L;

    {
      final ValidatingTextField.Cleaner cleaner = new ValidatingTextField.Cleaner() {
        public String clean(final String text) {
          String pattern = "";
          StringTokenizer tokenizer = new StringTokenizer(
            StringUtil.replaceAll(text, ",", " "));

          while (tokenizer.hasMoreTokens()) {
            pattern += (tokenizer.nextToken() + " ");
          }

          return StringUtil.replaceAll(pattern.trim(), " ", ",");
        }
      };

      BasicComboBoxEditor editor = new BasicComboBoxEditor();
      setEditor(editor);
      setEditable(true);
      addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          updateControls();
        }
      });
      ValidatingTextField.installValidationBehavior(
        (JTextField)editor.getEditorComponent(),
        new ValidatingTextField.Validator() {
          public boolean isValid(final String text) {
            try {
              BasicStyle.toArray(cleaner.clean(text), 1);

              return true;
            } catch (Exception e) {
              return false;
            }
          }
        }, cleaner);
      ((JTextField)editor.getEditorComponent()).getDocument()
        .addDocumentListener(new DocumentListener() {
          public void changedUpdate(final DocumentEvent e) {
            updateControls();
          }

          public void insertUpdate(final DocumentEvent e) {
            updateControls();
          }

          public void removeUpdate(final DocumentEvent e) {
            updateControls();
          }
        });
      setRenderer(new ListCellRenderer() {
        private JPanel panel = new JPanel() {
          /**
           * 
           */
          private static final long serialVersionUID = 2617061236647912261L;

          private int lineWidth = 2;

          protected void paintComponent(final Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_BEVEL, 1.0f, BasicStyle.toArray(linePattern,
                lineWidth), 0));
            g2.draw(new Line2D.Double(0, panel.getHeight() / 2.0,
              panel.getWidth(), panel.getHeight() / 2.0));
          }
        };

        private String linePattern;

        public Component getListCellRendererComponent(final JList list,
          final Object value, final int index, final boolean isSelected,
          final boolean cellHasFocus) {
          linePattern = (String)value;
          if (isSelected) {
            panel.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            panel.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
          } else {
            panel.setForeground(UIManager.getColor("ComboBox.foreground"));
            panel.setBackground(UIManager.getColor("ComboBox.background"));
          }

          return panel;
        }
      });
    }
  };

  private JComboBox fillPatternComboBox = new JComboBox(fillPatterns) {

    {
      setMaximumRowCount(24);
      setEditable(false);
      addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          updateControls();
        }
      });
      setRenderer(new ListCellRenderer() {
        private Paint fillPattern;

        private JLabel label = new JLabel(" ");

        private JPanel panel = new JPanel(new BorderLayout()) {

          {
            label.setPreferredSize(new Dimension(150,
              (int)label.getPreferredSize().getHeight()));
            add(label, BorderLayout.CENTER);
          }

          protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            ((Graphics2D)g).setPaint(fillPattern);
            ((Graphics2D)g).fill(new Rectangle2D.Double(0, 0, getWidth(),
              getHeight()));
          }
        };

        public Component getListCellRendererComponent(final JList list,
          final Object value, final int index, final boolean isSelected,
          final boolean cellHasFocus) {
          fillPattern = (Paint)value;
          label.setText(""
            + (1 + CollectionUtil.indexOf(fillPattern, fillPatterns)));
          if (isSelected) {
            label.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            panel.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
          } else {
            label.setForeground(UIManager.getColor("ComboBox.foreground"));
            panel.setBackground(UIManager.getColor("ComboBox.background"));
          }

          return panel;
        }
      });
    }
  };

  private JSlider lineWidthSlider = new JSlider() {

    {
      addChangeListener(new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          updateControls();
        }
      });
    }
  };

  private Blackboard blackboard;

  /**
   * Parameterless constructor for JBuilder GUI designer.
   */
  public BasicStylePanel() {
    this(null, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
  }

  public BasicStylePanel(final Blackboard blackboard,
    final int palettePanelVerticalScrollBarPolicy) {
    this.blackboard = blackboard;
    palettePanel = new ListPalettePanel(palettePanelVerticalScrollBarPolicy);

    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      Assert.shouldNeverReachHere();
    }

    transparencyPanel.getSlider().getModel().addChangeListener(
      new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          updateControls();
        }
      });
    palettePanel.add(new GridPalettePanel.Listener() {
      public void basicStyleChosen(final BasicStyle basicStyle) {
        // Preserve some settings e.g. line and fill patterns, alpha;
        BasicStyle newBasicStyle = getBasicStyle();
        newBasicStyle.setFillColor(basicStyle.getFillColor());
        newBasicStyle.setLineColor(basicStyle.getLineColor());
        newBasicStyle.setLineWidth(basicStyle.getLineWidth());
        newBasicStyle.setLinePattern(basicStyle.getLinePattern());
        newBasicStyle.setRenderingLinePattern(basicStyle.isRenderingLinePattern());
        newBasicStyle.setRenderingFill(basicStyle.isRenderingFill());
        newBasicStyle.setRenderingLine(basicStyle.isRenderingLine());
        setBasicStyle(newBasicStyle);
      }
    });
    updateControls();
  }

  /**
   * Remove extra commas
   */
  private String clean(final String linePattern) {
    String pattern = "";
    StringTokenizer tokenizer = new StringTokenizer(StringUtil.replaceAll(
      linePattern, ",", " "));

    while (tokenizer.hasMoreTokens()) {
      pattern += (tokenizer.nextToken() + " ");
    }

    return StringUtil.replaceAll(pattern.trim(), " ", ",");
  }

  // UT made it protected for testing
  protected void jbInit() throws Exception {
    lineWidthSlider.setPreferredSize(SLIDER_DIMENSION);
    lineWidthSlider.setPaintLabels(true);
    lineWidthSlider.setValue(1);
    lineWidthSlider.setLabelTable(lineWidthSlider.createStandardLabels(10));
    lineWidthSlider.setMajorTickSpacing(5);
    lineWidthSlider.setMaximum(30);
    lineWidthSlider.setMinorTickSpacing(1);
    setLayout(new GridBagLayout());
    linePatternCheckBox.setText(I18N.get("ui.style.BasicStylePanel.line-pattern"));
    fillPatternCheckBox.setText(I18N.get("ui.style.BasicStylePanel.fill-pattern"));
    linePatternCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        linePatternCheckBoxActionPerformed(e);
      }
    });
    fillPatternCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        fillPatternCheckBoxActionPerformed(e);
      }
    });
    add(centerPanel, new GridBagConstraints(0, 0, 1, 2, 0, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2),
      0, 0));
    add(new JPanel(), new GridBagConstraints(3, 0, 1, 1, 1, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2,
        2, 2), 0, 0));
    add(new JLabel(I18N.get("ui.style.BasicStylePanel.presets")),
      new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(0, 30, 0, 0), 0, 0));
    add(palettePanel, new GridBagConstraints(2, 1, 1, 1, 0, 1,
      GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 30,
        0, 0), 0, 0));
    SpringLayout centreLayout = new SpringLayout();
    centerPanel.setLayout(centreLayout);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    fillColorChooserPanel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        fillColorChooserPanelActionPerformed(e);
      }
    });
    lineColorChooserPanel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        lineColorChooserPanelActionPerformed(e);
      }
    });
    synchronizeCheckBox.setText(I18N.get("ui.style.BasicStylePanel.sync-line-colour-with-fill-colour"));
    synchronizeCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        synchronizeCheckBoxActionPerformed(e);
      }
    });
    enabledCheckBox.setText(I18N.get("ui.style.BasicStylePanel.enable"));
    fillCheckBox.setText(I18N.get("ui.style.BasicStylePanel.fill"));
    fillCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        fillCheckBoxActionPerformed(e);
      }
    });
    lineCheckBox.setText(I18N.get("ui.style.BasicStylePanel.line"));
    lineCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        lineCheckBoxActionPerformed(e);
      }
    });
    fillColorChooserPanel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        fillColorChooserPanelActionPerformed(e);
      }
    });
    lineWidthLabel.setText(I18N.get("ui.style.BasicStylePanel.line-width"));
    transparencyLabel.setText(I18N.get("ui.style.BasicStylePanel.transparency"));

    makeCompactGrid(centerPanel, new Component[][] {
      new Component[] {
        enabledCheckBox
      }, new Component[] {
        fillCheckBox, fillColorChooserPanel
      }, new Component[] {
        fillPatternCheckBox, fillPatternComboBox
      }, new Component[] {
        lineCheckBox, lineColorChooserPanel
      }, new Component[] {
        linePatternCheckBox, linePatternComboBox
      }, new Component[] {
        synchronizeCheckBox
      }, new Component[] {
        lineWidthLabel, lineWidthSlider
      }, new Component[] {
        transparencyLabel, transparencyPanel
      }
    }, 5, 5, 5, 5);
  }

  public static void makeCompactGrid(final Container parent,
    final Component[][] components, final int initialX, final int initialY,
    final int xPad, final int yPad) {
    SpringLayout layout;
    try {
      layout = (SpringLayout)parent.getLayout();
    } catch (ClassCastException exc) {
      System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
      return;
    }

    int numColumns = 0;
    for (Component[] row : components) {
      numColumns = Math.max(numColumns, row.length);
    }

    // Align all cells in each column and make them the same width.
    Spring x = Spring.constant(initialX);
    for (int c = 0; c < numColumns; c++) {
      Spring width = Spring.constant(0);
      for (int r = 0; r < components.length; r++) {
        Component[] row = components[r];
        if (c < row.length) {
          Component component = row[c];
          parent.add(component);
          Constraints constraints = layout.getConstraints(component);
          width = Spring.max(width, constraints.getWidth());
        }
      }
      for (int r = 0; r < components.length; r++) {
        Component[] row = components[r];
        if (c < row.length) {
          Component component = row[c];
          Constraints constraints = layout.getConstraints(component);
          constraints.setX(x);
          constraints.setWidth(width);
        }
      }
      x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
    }

    // Align all cells in each row and make them the same height.
    Spring y = Spring.constant(initialY);
    for (int r = 0; r < components.length; r++) {
      Component[] row = components[r];
      Spring height = Spring.constant(0);
      for (int c = 0; c < row.length; c++) {
        Component component = row[c];
        Constraints constraints = layout.getConstraints(component);
        height = Spring.max(height, constraints.getHeight());
      }
      for (int c = 0; c < row.length; c++) {
        Component component = row[c];
        Constraints constraints = layout.getConstraints(component);
        constraints.setY(y);
        constraints.setHeight(height);
      }
      y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
    }

    // Set the parent's size.
    SpringLayout.Constraints pCons = layout.getConstraints(parent);
    pCons.setConstraint(SpringLayout.SOUTH, y);
    pCons.setConstraint(SpringLayout.EAST, x);
  }

  public JSlider getTransparencySlider() {
    return transparencyPanel.getSlider();
  }

  protected void setAlpha(final int alpha) {
    transparencyPanel.getSlider().setValue(255 - alpha);
  }

  protected int getAlpha() {
    return 255 - transparencyPanel.getSlider().getValue();
  }

  public void setBasicStyle(final BasicStyle basicStyle) {
    addCustomFillPatterns();
    enabledCheckBox.setEnabled(basicStyle.isEnabled());
    fillColorChooserPanel.setColor(basicStyle.getFillColor());
    lineColorChooserPanel.setColor(basicStyle.getLineColor());
    setAlpha(basicStyle.getAlpha());
    fillCheckBox.setSelected(basicStyle.isRenderingFill());
    lineCheckBox.setSelected(basicStyle.isRenderingLine());
    lineWidthSlider.setValue(basicStyle.getLineWidth());
    linePatternCheckBox.setSelected(basicStyle.isRenderingLinePattern());
    fillPatternCheckBox.setSelected(basicStyle.isRenderingFillPattern());
    linePatternComboBox.setSelectedItem(basicStyle.getLinePattern());

    // Update fill pattern colors before finding the basic style's current fill
    // pattern in the combobox. [Jon Aquino]
    updateFillPatternColors();

    // Because fillPatternComboBox is not editable, we must use
    // findEquivalentItem,
    // otherwise the combobox gets confused and a stack overflow occurs
    // if the two items are equal but not == . [Jon Aquino]

    Object fill = findEquivalentItem(basicStyle.getFillPattern(),
      fillPatternComboBox);

    if (fill != null) {
      fillPatternComboBox.setSelectedItem(fill);
    }

    updateControls();
  }

  @SuppressWarnings("unchecked")
  private void addCustomFillPatterns() {
    for (Paint fillPattern : ((Collection<Paint>)blackboard.get(
      FillPatternFactory.CUSTOM_FILL_PATTERNS_KEY, new ArrayList<Paint>()))) {
      if (null == findEquivalentItem(fillPattern, fillPatternComboBox)) {
        // Clone it because several BasicStylePanels access the collection of
        // custom fill patterns [Jon Aquino]
        ((DefaultComboBoxModel)fillPatternComboBox.getModel()).addElement(cloneIfBasicFillPattern(fillPattern));
      }
    }
  }

  private Object findEquivalentItem(final Object item, final JComboBox comboBox) {

    if (comboBox == null) {
      return null;
    }

    if (item == null) {
      if (comboBox.getItemCount() > 0) {
        return comboBox.getItemAt(0);
      } else {
        return null;
      }
    }

    for (int i = 0; i < comboBox.getItemCount(); i++) {
      if (item.equals(comboBox.getItemAt(i))) {
        return comboBox.getItemAt(i);
      }
    }

    return null;
  }

  public BasicStyle getBasicStyle() {
    BasicStyle basicStyle = new BasicStyle();
    basicStyle.setEnabled(enabledCheckBox.isEnabled());
    basicStyle.setFillColor(fillColorChooserPanel.getColor());
    basicStyle.setLineColor(lineColorChooserPanel.getColor());
    basicStyle.setAlpha(getAlpha());
    basicStyle.setRenderingFill(fillCheckBox.isSelected());
    basicStyle.setRenderingLine(lineCheckBox.isSelected());
    basicStyle.setRenderingLinePattern(linePatternCheckBox.isSelected());
    basicStyle.setRenderingFillPattern(fillPatternCheckBox.isSelected());
    basicStyle.setLinePattern(clean((String)linePatternComboBox.getEditor()
      .getItem()));
    basicStyle.setFillPattern(cloneIfBasicFillPattern((Paint)fillPatternComboBox.getSelectedItem()));
    basicStyle.setLineWidth(lineWidthSlider.getValue());

    return basicStyle;
  }

  private Paint cloneIfBasicFillPattern(final Paint fillPattern) {
    if (fillPattern instanceof BasicFillPattern) {
      return (Paint)((BasicFillPattern)fillPattern).clone();
    } else {
      return fillPattern;
    }
  }

  protected void setFillColor(final Color newColor) {
    fillColorChooserPanel.setColor(newColor);
    transparencyPanel.setColor(newColor);
  }

  protected void updateControls() {
    linePatternComboBox.setEnabled(linePatternCheckBox.isSelected());
    fillPatternComboBox.setEnabled(fillPatternCheckBox.isSelected());
    lineColorChooserPanel.setEnabled(lineCheckBox.isSelected());
    fillColorChooserPanel.setEnabled(fillCheckBox.isSelected());
    fillColorChooserPanel.setAlpha(getAlpha());
    lineColorChooserPanel.setAlpha(getAlpha());
    palettePanel.setAlpha(getAlpha());
    if (lineCheckBox.isSelected() && !fillCheckBox.isSelected()) {
      transparencyPanel.setColor(lineColorChooserPanel.getColor());
    } else {
      transparencyPanel.setColor(fillColorChooserPanel.getColor());
    }
    updateFillPatternColors();
    fillPatternComboBox.repaint();
  }

  private void updateFillPatternColors() {
    // Iterate through combo box contents rather than fillPatterns field,
    // because
    // the combo box contents = fillPatterns + customFillPatterns [Jon Aquino]
    for (int i = 0; i < fillPatternComboBox.getItemCount(); i++) {
      if (fillPatternComboBox.getItemAt(i) instanceof BasicFillPattern) {
        ((BasicFillPattern)fillPatternComboBox.getItemAt(i)).setColor(GUIUtil.alphaColor(
          fillColorChooserPanel.getColor(), getAlpha()));
      }
    }
  }

  void fillCheckBoxActionPerformed(final ActionEvent e) {
    updateControls();
  }

  void fillColorChooserPanelActionPerformed(final ActionEvent e) {
    if (synchronizeCheckBox.isSelected()) {
      syncLineColor();
    }

    updateControls();
  }

  private void syncLineColor() {
    lineColorChooserPanel.setColor(fillColorChooserPanel.getColor().darker());
  }

  void lineColorChooserPanelActionPerformed(final ActionEvent e) {
    if (synchronizeCheckBox.isSelected()) {
      fillColorChooserPanel.setColor(lineColorChooserPanel.getColor()
        .brighter());
    }

    updateControls();
  }

  void lineCheckBoxActionPerformed(final ActionEvent e) {
    updateControls();
  }

  public void setSynchronizingLineColor(final boolean newSynchronizingLineColor) {
    synchronizeCheckBox.setSelected(newSynchronizingLineColor);
  }

  protected void synchronizeCheckBoxActionPerformed(final ActionEvent e) {
    if (synchronizeCheckBox.isSelected()) {
      syncLineColor();
    }

    updateControls();
  }

  public JCheckBox getSynchronizeCheckBox() {
    return synchronizeCheckBox;
  }

  void linePatternCheckBoxActionPerformed(final ActionEvent e) {
    updateControls();
  }

  void fillPatternCheckBoxActionPerformed(final ActionEvent e) {
    updateControls();
  }
}
