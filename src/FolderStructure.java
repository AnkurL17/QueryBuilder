
/**
 * @author Ankur Luthra
 *
 */
public class FolderStructure {
  private int indent;
  private String name;
  private String spaces = "";

  public FolderStructure(int currentIndent, String currentName) {
    setIndent(currentIndent);
    setName(currentName);
  }

  public int getIndent() {
    return indent;
  }

  public void setIndent(int indent) {
    this.indent = indent;
    setSpaces(indent);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSpaces() {
    return spaces;
  }

  public void setSpaces(int indent) {
    for (int i = 0; i < indent; i++)
      this.spaces += " ";
  }

  @Override
  public String toString() {
    return indent + "\t" + spaces + name;
  }

}
