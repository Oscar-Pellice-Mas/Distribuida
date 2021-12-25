package S5.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {



    public static void main(String[] args) throws IOException {
        Socket connection;
        PrintWriter outS      = null;
        BufferedReader inS    = null;
        //Leemos el archivo y parseamos
        TransactionList lista = new TransactionList(args[0]);
        //Parseo finalizado. Hora de enviar
        //Conectamos con el server
        connection = new Socket("127.0.0.1", 8000);
        inS=new BufferedReader(new InputStreamReader(connection.getInputStream()));
        outS =  new PrintWriter(connection.getOutputStream(), true);
        //Conect
        outS.println("client-");
        for (int i = 0; i < lista.getTransactions().size(); i++) {

        }
    }
}

class TransactionList{

    private ArrayList<Transaction> transactions;


    public TransactionList(String filename) {
        transactions = new ArrayList<Transaction>();
        int t = 0;

        try{
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                transactions.add(new Transaction());
                String data = myReader.nextLine();
                String[] buffer;
                String writeBuffer = "";
                System.out.println(data);

                buffer = data.split(",");
                if (buffer[0].split("<").length <= 1){
                    transactions.get(t).setLayer(1);
                    System.out.println("En la capa 0");
                }else{
                    transactions.get(t).setLayer(Integer.parseInt(buffer[0].split("<")[1].split(">")[0]));
                    System.out.println("en la capa" + transactions.get(t));
                }

                for (int i = 1; i < buffer.length ; i++){
                    if(buffer[i].charAt(1)=='r'){
                        transactions.get(t).reads.add(Integer.parseInt(buffer[i].split("[(]")[1].split("[)]")[0]));
                    }else if (!buffer[i].equals(" c")){
                        if(buffer[i].charAt(1)=='w'){
                            writeBuffer = buffer[i].split("[(]")[1];
                            writeBuffer = writeBuffer.concat("-");
                        }else{
                            writeBuffer = writeBuffer.concat(buffer[i].split("[)]")[0]);
                            transactions.get(t).writes.add(writeBuffer);
                        }
                    }
                }
                t++;
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    class Transaction{
        private ArrayList<Integer> reads;
        private ArrayList<String>  writes;
        private int                layer;

        public Transaction() {
            reads = new ArrayList<Integer>();
            writes = new ArrayList<String>();
        }

        public ArrayList<Integer> getReads() {
            return reads;
        }

        public void setReads(ArrayList<Integer> reads) {
            this.reads = reads;
        }

        public ArrayList<String> getWrites() {
            return writes;
        }

        public void setWrites(ArrayList<String> writes) {
            this.writes = writes;
        }

        public int getLayer() {
            return layer;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }
    }
}
