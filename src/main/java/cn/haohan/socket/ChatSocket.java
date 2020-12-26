package cn.haohan.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatSocket {

    private Thread writeListern;
    private boolean writeListernRunable = true;
    private boolean isRunable = true;
    private Socket socket;
    private int maxByteSize = 256;
    private Charset charset = StandardCharsets.UTF_8;
    private final String serverHost = "localhost";
    private final int port = 9999;


    private void startRun(){
        Socket socket = new Socket();
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost,port);
            socket.connect(inetSocketAddress);
            if(socket.isConnected()){
                this.socket = socket;
                while(isRunable){
                    handleRead();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(){
        if(socket.isInputShutdown() || socket.isClosed()){
            log("client exit");
            isRunable = false;
            return;
        }
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[maxByteSize];
            int readCount = -1;
            while((readCount = inputStream.read(bytes)) != -1){
                String msg = new String(bytes,0,readCount,charset);
                log("receive: %s",msg);
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private void handleWrite(String msg){
        if(socket != null && socket.isConnected()){
            try{
                OutputStream outputStream = socket.getOutputStream();
                byte[] bytes = msg.getBytes("UTF-8");
                outputStream.write(bytes);
                if(msg.equals("88")){
                    //客户端主动退出
                    socket.close();

                    //写线程退出
                    this.writeListernRunable = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startToWrite(){
        Thread thread = new Thread(){
            @Override
            public void run(){
                Scanner scanner = new Scanner(System.in);
                String inputMsg = null;
                while(writeListernRunable){
                    if((inputMsg = scanner.nextLine()) != null){
                        handleWrite(inputMsg);
                    }
                }
            }
        };
        thread.setName("write-thread");
        this.writeListern = thread;
        thread.start();
    }

    public static void main(String[] args){

        final ChatSocket chatSocket = new ChatSocket();
        chatSocket.startToWrite();
        chatSocket.startRun();
    }

    private void log(String msg,Object... obj){
        System.out.println(String.format(msg,obj));
    }

}
