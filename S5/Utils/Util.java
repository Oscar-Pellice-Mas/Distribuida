package S5.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Scanner;

public class Util {
    public static void writeUpdate(String name){
        File myObj = new File(".\\S5\\updateLog\\"+name+".txt");

        int version;
        try {
            myObj.createNewFile();
            Scanner myReader = new Scanner(myObj);
            String data = "";
            if (myReader.hasNextLine()){
                while (myReader.hasNextLine()) {
                    data =  myReader.nextLine();
                }
                version = Integer.parseInt(data.split(" ")[0].split("v")[1]) + 1;
            }else{
                version=0;
            }
            myReader.close();

            FileWriter myWriter = new FileWriter(".\\S5\\updateLog\\"+name+".txt", true);
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            myWriter.write("v"+version+" Update realitzada a "+ ts.toString() +"\n");
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
