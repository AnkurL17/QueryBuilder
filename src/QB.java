

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Panel;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * @author Ankur Luthra
 *
 */
public class QB {

  private JFrame frmCreateDocumentStructure;
  private JTextField textField;
  public JTextArea textArea;
  private JLabel lblDropExcelFile;
  private JPanel panelSouth;
  protected boolean loop;
  private JLabel label;
  private Panel panel_1;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          QB window = new QB();
          window.frmCreateDocumentStructure.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public QB() {
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frmCreateDocumentStructure = new JFrame();
    URL iconURL1 = getClass().getResource("resource/favicon.png");
    final ImageIcon icon1 = new ImageIcon(iconURL1);
    URL iconURL2 = getClass().getResource("resource/favicon2.png");
    final ImageIcon icon2 = new ImageIcon(iconURL2);
    frmCreateDocumentStructure.setIconImage(icon1.getImage());
    frmCreateDocumentStructure.setLocation(new Point(400, 15));
    frmCreateDocumentStructure.setSize(new Dimension(500, 700));
    frmCreateDocumentStructure.setTitle("Create document structure");
    frmCreateDocumentStructure.setResizable(false);
    frmCreateDocumentStructure.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final JPanel panel = new JPanel();
    panel.setBackground(new Color(0, 204, 102));
    frmCreateDocumentStructure.getContentPane().add(panel, BorderLayout.CENTER);
    panel.setLayout(new BorderLayout(0, 0));

    lblDropExcelFile = new JLabel("Drop Excel File(s) Here !");
    lblDropExcelFile.setHorizontalAlignment(SwingConstants.CENTER);
    lblDropExcelFile.setHorizontalTextPosition(SwingConstants.CENTER);
    lblDropExcelFile.setAlignmentX(Component.CENTER_ALIGNMENT);
    lblDropExcelFile.setBorder(BorderFactory.createDashedBorder(Color.red));
    lblDropExcelFile.setDropTarget(new DropTarget() {
      /*
       * (non-Javadoc)
       * 
       * @see java.awt.dnd.DropTarget#drop(java.awt.dnd.DropTargetDropEvent)
       */
      public synchronized void drop(DropTargetDropEvent evt) {
        try {
          evt.acceptDrop(DnDConstants.ACTION_COPY);
          List<File> droppedFiles =
              (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          panel.setBackground(Color.red);
          panel_1.setBackground(Color.yellow);
          lblDropExcelFile.setText("Processing dropped files.");
          frmCreateDocumentStructure.setIconImage(icon2.getImage());
          for (File file : droppedFiles) {
            loop = true;
            textArea.append("\n-------------------------------------------------------------\n");
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();
            String[] split = fileName.split("\\.");
            String extension = split[split.length - 1].trim();
            if (!(extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx"))) {
              textArea.append(
                  "\nOnly Excel files with .xls or .xlsx extension will be parsed.\nSkipping file : "
                      + fileName + "\n");
            } else {
              do {
                textField.setText(JOptionPane
                    .showInputDialog("Enter Org ID for " + split[split.length - 2].trim() + " : "));
              } while (textField.getText().isEmpty()
                  && JOptionPane.showConfirmDialog(frmCreateDocumentStructure,
                      "Leaving Org ID blank will skip SQL creation.\nWould you like to fill it again ?",
                      "Org ID is empty", JOptionPane.YES_NO_OPTION) == 0);
              if (textField.getText().isEmpty())
                textArea.append("\nOrg ID left blank.\nSkipping file : " + fileName + "\n");
              else {
                textArea.append(
                    "Creating queries for " + fileName + " with Org ID " + textField.getText());
                new FolderStructureSqlBuilder2(filePath, fileName, textField.getText(), textArea);
              }
            }
          }
          frmCreateDocumentStructure.setIconImage(icon1.getImage());
          panel.setBackground(new Color(0, 204, 102));
          panel_1.setBackground(new Color(30, 144, 255));
          lblDropExcelFile.setText("Drop Excel File(s) Here !");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

    panel.add(lblDropExcelFile, BorderLayout.CENTER);
    JPanel panelNorth = new JPanel();
    panelNorth.setVisible(false);
    panel.add(panelNorth, BorderLayout.NORTH);

    JLabel lblOrgId = new JLabel("Org ID");
    panelNorth.add(lblOrgId);

    textField = new JTextField();
    panelNorth.add(textField);
    textField.setColumns(10);
    textField.setEnabled(false);

    panelSouth = new JPanel();
    panelSouth.setBackground(new Color(30, 144, 255));
    panelSouth.setPreferredSize(new Dimension(100, 300));
    panel.add(panelSouth, BorderLayout.SOUTH);
    panelSouth.setLayout(new BorderLayout(0, 0));

    JScrollPane scrollPane = new JScrollPane();
    panelSouth.add(scrollPane, BorderLayout.CENTER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    textArea = new JTextArea();
    textArea.setColumns(40);
    textArea.setRows(15);
    scrollPane.setViewportView(textArea);
    textArea.setEditable(false);

    panel_1 = new Panel();
    panelSouth.add(panel_1, BorderLayout.PAGE_END);

    label = new JLabel("");
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        lblDropExcelFile.setIcon(new ImageIcon(QB.class.getResource("/resource/excel.png")));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        lblDropExcelFile.setIcon(null);
      }
    });
    label.setHorizontalTextPosition(SwingConstants.RIGHT);
    panel_1.add(label);

    label.setPreferredSize(new Dimension(32, 32));
    label.setMaximumSize(new Dimension(32, 32));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setVerticalAlignment(SwingConstants.BOTTOM);
    label.setToolTipText("Demo File");
    label.setIcon(new ImageIcon(QB.class.getResource("/resource/excelicon.jpg")));

  }

}
