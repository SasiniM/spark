package org.apache.spark.sql.fastbit;

import gov.lbl.fastbit.FastBit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author sasini
 */
public class FastBitClient {

    private int noOfColumns;
    private FastBit fastbitclient = null;

    public FastBitClient() {

        fastbitclient = new FastBit("");

    }

    //-------------------------------------------------------------------------
    //Create Binary files for each column
    //-------------------------------------------------------------------------

    private void createBinaryFiles(String baseTable, String indexTable, String tableDetails[], String dataTypes[]){

        String dataSource = "", columnString = "";
        for (int i=0; i<tableDetails.length; i++){
            if (dataTypes[i]=="StringType"){
                columnString += (tableDetails[i]+ ":text" );
            }
            else
                columnString += (tableDetails[i]+ ":" + dataTypes[i] );
            if (i != tableDetails.length-1){
                columnString += ",";
            }
        }

        File file = new File("spark-warehouse/" + baseTable);
        File dataFiles[] = file.listFiles();
        for (File dataFile : dataFiles) {
            if(!dataFile.isHidden()) {
                dataSource = dataFile.getName();
            }
        }
        String ardeaCommand = "/home/sasini/Documents/FastBit/fastbit-2.0.3/examples/ardea -d spark-warehouse/" +
                indexTable + " -m \"" + columnString + "\" -t " +
                "spark-warehouse/" + baseTable +
                "/" + dataSource;
/*/home/sasini/Documents/FastBit/fastbit-2.0.3/examples/ardea -d bin/spark-warehouse/truck_mileage_index
                -m "truckid:key, driverid:key, rdate:key, miles:LongType, gas:LongType, mpg:DoubleType"
                -t bin/spark-warehouse/truck_mileage/truck_mileage.csv*/

        try {
            Process process =Runtime.getRuntime().exec(ardeaCommand);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = br.readLine();
            while (line !=null){
                System.out.println(line);
                line =br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------
    //Create Indexes
    //-------------------------------------------------------------------------

    public void createIndex(String indexTable,
                            String baseTable,
                            String indexHandlerClass,
                            String columnNames[],
                            String tableDetails[],
                            String dataTypes[]){

        createBinaryFiles(baseTable, indexTable, tableDetails, dataTypes);
            noOfColumns = columnNames.length;

            for (int i=0; i < noOfColumns; i++) {
                fastbitclient.build_index("spark-warehouse/" + indexTable,
                        columnNames[i],
                        "index=" + indexHandlerClass);
            }
    }

    //-----------------------------------------------------------------------
    //Count Query
    //-----------------------------------------------------------------------

    public  void countQuery(String selectClause, String dataDir, String whereClause){

/*        String selectColumns = "";
        for (int i=0; i<selectClause.length; i++){
                selectColumns += (selectClause[i]);
            if (i != selectClause.length-1){
                selectColumns += ",";
            }
        }*/

        dataDir = "truck_mileage_index";
        whereClause = "miles>15000";

        String[] countQuery = new String[]{"/home/sasini/Documents/fastbit-2.0.3/examples/ibis",
                "-d",
                "/home/sasini/Documents/fastbit-2.0.3/"+dataDir,
                "-q",
                "select "+selectClause+" where " +whereClause,
                "-v",
                "6"};

        try {
            Process p = Runtime.getRuntime().exec(countQuery);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str =br.readLine();
            while (str !=null) {
                System.out.println(str);
                str = br.readLine();
            }
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            str =br.readLine();
            while (str !=null) {
                System.out.println(str);
                str = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
        //FastBit.QueryHandle handle = fastbitclient.build_query("miles", "truck_mileage_index", "miles>=15000");
        //int count = fastbitclient.get_result_size(handle);
}
