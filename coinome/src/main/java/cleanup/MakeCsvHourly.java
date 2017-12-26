package cleanup;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MakeCsvHourly {
    public static void main(String[] args) {
        File f = new File("abc.csv");
        try {
            String[] fileContent = FileUtils.readFileToString(f).split("\n");
            System.out.println(fileContent);
            StringBuilder newFileContent = new StringBuilder(fileContent[0]).append("\n");
            for (int i = 1; i < fileContent.length - 1; i++) {
                try {
                    String[] content = fileContent[i].split(" ");
                    String[] newContent = fileContent[i + 1].split(" ");
                    if (!(content[2].equals(newContent[2]) &&
                            content[3].split(":")[0].equals(newContent[3].split(":")[0]))) {
                        newFileContent.append(fileContent[i]).append("\n");
                    }
                } catch (Exception e) {
//                    System.out.println(e);
                }
            }
            System.out.println(newFileContent);
        } catch (IOException e) {
//            e.printStackTrace();
        }

    }
}
