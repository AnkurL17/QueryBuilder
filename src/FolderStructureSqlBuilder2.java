
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Ankur Luthra
 *
 */
public class FolderStructureSqlBuilder2 {
  int orgID;
  String fileName;
  String filePath;
  int sheetNo = 0;
  JTextArea textArea;
  String entityType[] = {"P", "A"};

  /**
   * @param fileName
   * @param text
   */
  public FolderStructureSqlBuilder2(String filePath, String fileName, String text,
      JTextArea textArea) {
    try {
      this.orgID = Integer.parseInt(text.trim());
      this.filePath = filePath.trim();
      this.fileName = fileName.trim();
      this.textArea = textArea;
      process();
    } catch (NumberFormatException ex) {
      textArea.append("\n" + text + " is not a proper Org ID\nSkipping file " + fileName);
    }
  }

  public void process() {

    int currentIndent = 0;
    int prevIndent = 0;
    String parent_name = null;
    String currentName;
    String prevName = null;
    String query = null;
    Boolean hasError = false;
    Boolean checkFirstCell = true;
    List<String> list = new ArrayList<String>();
    List<String> parentNameList = new ArrayList<String>();
    List<FolderStructure> folderStructure = new ArrayList<FolderStructure>();
    try {
      FileInputStream excelFile = new FileInputStream(new File(filePath));
      Workbook workbook = new XSSFWorkbook(excelFile);
      Sheet datatypeSheet = workbook.getSheetAt(sheetNo);// sheet number in excel
      Iterator<Row> iterator = datatypeSheet.iterator();
      while (iterator.hasNext()) {
        Row currentRow = iterator.next();
        int rowContentValue = 0;
        Iterator<Cell> cellIterator = currentRow.iterator();
        while (cellIterator.hasNext()) {
          Cell currentCell = cellIterator.next();
          CellType cellType = currentCell.getCellTypeEnum();
          if (checkFirstCell && !currentCell.getAddress().toString().equalsIgnoreCase("A1")) {
            textArea.append("\nDocument Structure should start with First Cell (i.e A1)");
            hasError = true;
            checkFirstCell = false;
            break;
          }
          checkFirstCell = false;
          if (cellType != CellType.BLANK) {
            rowContentValue++; // to store if one row has more than one folder
            currentIndent = currentCell.getColumnIndex();
            currentName = currentCell.toString().trim();
            folderStructure.add(new FolderStructure(currentIndent, currentName));
            if ((folderStructure.size() > 1
                && (folderStructure.get((folderStructure.size() - 1)).getIndent()
                    - folderStructure.get((folderStructure.size() - 2)).getIndent()) > 1)
                || rowContentValue > 1) // checking if a column is skipped
            {
              String error =
                  "Document Structure Level Exception :\n Error occured while rendering file at cell "
                      + currentCell.getAddress();
              error +=
                  "\n\nEither one row contains two folder names or You have skipped a column between a folder & sub-folder name.";
              list.add(error);
              // toFile(list);// writing error to file and exit
              // System.err.println(error);
              textArea.append("\n" + error + "\nClick here to check demo");
              hasError = true;
            }
          }
        } // while
      } // while
      workbook.close();
    } catch (IOException e) {
      hasError = true;
      e.printStackTrace();
    }

    if (!hasError) {
      /*
       * System.out.println("Indent\tName");// File View for (FolderStructure fs : folderStructure)
       * { System.out.println(fs); }
       * 
       * // Trace-out System.out.println("--------------------------------------------------");
       * System.out.println("PrevName\tCurrentName\tPrevI\tCurrentI\tList");
       */
      for (int entityNumber = 0; entityNumber < entityType.length; entityNumber++) {
        list.add("-- Queries for entity type " + entityType[entityNumber] + " --");

        for (FolderStructure fs : folderStructure) {
          currentIndent = fs.getIndent();
          currentName = fs.getName();

          if (currentIndent > prevIndent) {
            parentNameList.add(prevName); // Add a folder to stack if indent/column increases
          } else if (currentIndent < prevIndent) {
            for (int jump = 1; jump <= prevIndent - currentIndent; jump++)
              parentNameList.remove(parentNameList.size()
                  - 1);/* Remove folder(s) from stack depending upon decrease in indent/columns */
          }


          if (!parentNameList.isEmpty()) {
            parent_name = parentNameList.get(parentNameList.size() - 1);
            /*
             * System.out.println(parent_name + "\t\t" + currentName + "\t\t" + prevIndent + "\t" +
             * currentIndent + "\t\t" + parentNameList);
             */
          }


          if (currentIndent == 0)// For parent folders.
          {
            query =
                "insert into document_structure (folder_name,org_id,created_ts,updated_ts,entity_type) values (\""
                    + currentName + "\"," + orgID + ",now(),now(),'" + entityType[entityNumber]
                    + "');";
          } else if (currentIndent == 1)// For level 1 sub-folders.
          {
            query =
                "insert into document_structure (folder_name,org_id,created_ts,updated_ts,parent_id,folder_path,entity_type) values "
                    + "(\"" + currentName + "\"," + orgID
                    + ",now(),now(),(select * from(select id from document_structure where folder_name=\""
                    + parent_name + "\" order by id desc limit 1) as prev_id),"
                    + "concat(\"/\",(select * from(select id from document_structure where folder_name=\""
                    + parent_name + "\" order by id desc limit 1) as prev_id)),'"
                    + entityType[entityNumber] + "');";
          } else // For all remaining sub-sub folders.
          {
            query =
                "insert into document_structure (folder_name,org_id,created_ts,updated_ts,parent_id,folder_path,entity_type) values "
                    + "(\"" + currentName + "\"," + orgID
                    + ",now(),now(),(select * from(select id from document_structure where folder_name=\""
                    + parent_name + "\" order by id desc limit 1) as prev_id),"
                    + "concat((select folder_path from(select id,folder_path from document_structure where folder_name=\""
                    + parent_name + "\" order by id desc limit 1) as prev_id)"
                    + ",concat(\"/\",(select * from(select id from document_structure where folder_name=\""
                    + parent_name + "\" order by id desc limit 1) as prev_id))),'"
                    + entityType[entityNumber] + "');";
          }
          list.add(query);
          prevIndent = currentIndent;
          prevName = currentName;
        }
        list.add(
            "insert into document_structure (folder_name,org_id,created_ts,updated_ts,uncategorized_ind,entity_type) values (\"Uncategorized\","
                + orgID + ",now(),now(),1,'" + entityType[entityNumber] + "');");// Adding an
                                                                                 // uncategorized
                                                                                 // folder to the
                                                                                 // final
        // list.
        list.add("");
        list.add("");
        list.add("");
      }
      toFile(list);
    }

  }

  // To create .sql file either with queries or error description.
  void toFile(List<String> list) {
    try {
      String date = new Date().toString();
      date = date.replaceAll("\\:", "_");
      checkOrCreateDir();
      String createdFileName = "SQL\\" + fileName + "_" + date + ".sql";
      FileOutputStream fos = new FileOutputStream(createdFileName);
      PrintWriter pw = new PrintWriter(fos);
      for (String output : list) {
        pw.println(output);
      }
      pw.close();
      // System.out.println("Queries developed successfully.");
      textArea.append("\nQueries created for both Project and Asset successfully at : \n"
          + createdFileName + " \nUse as per requirement.");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   */
  private void checkOrCreateDir() {
    File theDir = new File("SQL");

    // if the directory does not exist, create it
    if (!theDir.exists()) {
      textArea.append("\ncreating directory SQL\n");
      boolean result = false;

      try {
        theDir.mkdir();
        result = true;
      } catch (SecurityException se) {
        // handle it
      }
      if (result) {
        textArea.append("\nDIR created\n");
      }
    }

  }

}
