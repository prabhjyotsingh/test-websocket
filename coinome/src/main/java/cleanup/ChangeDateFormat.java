package cleanup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.io.FileUtils;

public class ChangeDateFormat {


  public static void main(String[] args) {

    Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    File f = new File("abc.csv");
    try {
      String[] fileContent = FileUtils.readFileToString(f).split("\n");
      StringBuilder newFileContent = new StringBuilder(fileContent[0]).append("\n");
      for (int i = 1; i < fileContent.length - 1; i++) {
        try {
          String[] content = fileContent[i].split("\t");

          String stringDate = content[0];
          DateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
          Date date = format.parse(stringDate);

          newFileContent.append(gson.toJson(date,Date.class).replaceAll("\"","")).append(fileContent[i].split("2017")[1]).append("\n");

        } catch (Exception e) {
        }
      }
//      System.out.println(newFileContent);
      FileUtils.write(f, newFileContent);
    } catch (IOException e) {
    }

  }
}
