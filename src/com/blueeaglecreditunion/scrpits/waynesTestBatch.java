package com.blueeaglecreditunion.scrpits;

import com.corelationinc.script.*;
import com.ibm.db2.jcc.*;
import java.io.*;
import java.util.ArrayList;
import java.sql.*;
import java.util.Calendar;


public class waynesTestBatch {
    Script script = null;

    private waynesTestBatch(Script wsTest){
        this.script = wsTest;
    }

    public static void runScript(Script script) throws Exception{
        waynesTestBatch wTB = new waynesTestBatch(script);

        wTB.run();
    }

    private void run() throws Exception{
        Connection conn = this.script.openDatabaseConnection();
        Report report = this.script.openReport("Report A", Report.Format.txt);
        String postingDate = script.retrievePostingDateString(conn);
        PrintStream pS = new PrintStream(report.getBufferedOutputStream());
        XMLSerialize xml = new XMLSerialize();
        xml.setXMLWriter(pS);
        xml.putStartDocument();
        xml.putBatchQuery(postingDate);
        xml.putSequence();
        ArrayList<member> memInfo = getNewMembers(conn);
        addNotes(memInfo, xml);
        xml.put(); //End sequence
        xml.putEndDocument();

    }

    public ArrayList<member> getNewMembers(Connection conn) throws Exception{
        ArrayList<member> mem = new ArrayList<>();
        String sql = queries.queryString();
        PreparedStatement pS = conn.prepareStatement(sql);
        ResultSet rS = pS.executeQuery();

        while (rS.next()){
            String name = rS.getString(1);
            String acct = rS.getString(2);
            java.sql.Date DoB = rS.getDate(3);
            int age = rS.getInt(4);
            String address = rS.getString(5);
            String email = rS.getString(6);
            String phone = rS.getString(7);
            java.sql.Date memDate = rS.getDate(8);
            Serial personSerial = Serial.get(rS, 9);

            member member = new member(name, acct, DoB, age, address, email, phone, memDate, personSerial);
            mem.add(member);
        }

        pS.close();
        return mem;
    }

    public static Date addDays(java.sql.Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return new java.sql.Date(c.getTimeInMillis());
    }


    public void addNotes(ArrayList<member> Member, XMLSerialize xml) throws Exception{

        for(member m : Member){
            java.sql.Date experationDate = addDays(m.getMemberDate(), 30);
            xml.putTransaction();
            xml.putStep();
            xml.put("record");
            {
                xml.putOption("operation", "I");
                xml.put("tableName", "PE_NOTE");
                xml.put("targetParentSerial", m.getPersonSerial());
                xml.putOption("includeTableMetadata", "N");
                xml.putOption("includeColumnMetadata", "N");
                xml.putOption("includeRowDescriptions", "Y");

                xml.put("field"); // <field>
                {
                    xml.put("columnName", "TYPE_SERIAL");
                    xml.putOption("operation", "S");
                    xml.put("newContents", "1801");
                }
                xml.put(); // </field>
                xml.put("field"); // <field>
                {
                    xml.put("columnName", "EXPIRATION_DATE");
                    xml.putOption("operation", "S");
                    xml.put("newContents", experationDate.toString());
                }
                xml.put(); // </field>
                xml.put("field"); // <field>
                {
                    xml.put("columnName", "EXPLANATION");
                    xml.putOption("operation", "S");
                    xml.put("newContents", "Welcome to Blue Eagle!");
                }
                xml.put(); // </field>
            }
            xml.put(); //closing record
            xml.put(); //closing step
            xml.put(); //closing transaction
        }
    }


    public static void main(String[] args) throws Throwable {
        String javaClass = "-javaClass=" + "com.blueeaglecreditunion.scrpits.waynesTestBatch"; // class path name of the batch script you want to run
        String javaMethod = "-javaMethod=" + "runScript"; // method to call in the script class
        String database = "-database=" + "D0062T04"; // database to read from XX is the client number and YYY is the env ex: D0035T00
        String databaseHome = "U:/Desktop/Test"; // can set this if you need to read in a file into your program
        String jdbcDriver = "-jdbcDriver=" + "com.ibm.db2.jcc.DB2Driver"; // DB2 driverCoachella2017
        String jdbcURLPrefix = "-jdbcURLPrefix=" + myPassword.getDatabaseName(); // DB2 URL connection to your DB
        String userName = "-userName=" + myPassword.getUsername(); // aix username
        String password = "-password=" + myPassword.getPassword(); // aix password
        String passwordStdInFlag = "-passwordStdInFlag=" + "";
        String userHome = "-userHome=" + "U:/Desktop/Test"; // location for the output folders
        String defaultThreadQueueServerCount = "-defaultThreadQueueServerCount=" + "1";
        String javaClassPath = "-javaClassPath=" + "C:/Users/CORETRAINING/Documents/NetBeansProjects/Batch Scripting Repo/dist/BatchScripting.jar";
        String resultPathName = "-resultPathName=" + "U:/Desktop/Test/OutputReport.xml";  // default output report
        String terminatePathName = "-terminatePathName=" + "";
        String arg = "-arg="; // can pass arguments csv file names, params etc.. ex: test_csv_file.csv

        args = new String[]{
                        javaClass, javaMethod, database, databaseHome, jdbcDriver,
                        jdbcURLPrefix, userName, password, passwordStdInFlag, userHome,
                        defaultThreadQueueServerCount, javaClassPath, resultPathName, terminatePathName, arg
                };


        // main call to run your batch script
        com.corelationinc.script.Script.main(args);
    }

}
