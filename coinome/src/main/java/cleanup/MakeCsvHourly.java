package cleanup;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class MakeCsvHourly {

  public static void main(String[] args) {
    File f = new File("abc.csv");
    try {
      String[] fileContent = FileUtils.readFileToString(f).split("\n");
      StringBuilder newFileContent = new StringBuilder(fileContent[0]).append("\n");
      for (int i = 1; i < fileContent.length - 1; i++) {
        try {
          String[] content = fileContent[i].split("T");
          String[] newContent = fileContent[i + 1].split("T");
          if (!(content[0].equals(newContent[0]) &&
              content[1].split(":")[0].equals(newContent[1].split(":")[0]))) {
            newFileContent.append(fileContent[i]).append("\n");
          }
        } catch (Exception e) {
        }
      }
      newFileContent.append(fileContent[fileContent.length-1]).append("\n");
      FileUtils.write(f, newFileContent);
    } catch (IOException e) {
    }

  }
}
