package cn.haohan.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimitiveServerSocket {

    private boolean isRunable = true;
    private Map<Integer,Socket> sockets = new HashMap<>();
    private int maxByteSize = 256;
    private Charset charset = StandardCharsets.UTF_8;
    private final int port = 9999;
    private ThreadSafeList msgList = new ThreadSafeList();
    private Thread forwardThread;
    private boolean forwardRunable = true;


    public void startServer(){

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            while (isRunable){
                final Socket socket = serverSocket.accept();
                log("client [%s] connected",((InetSocketAddress)socket.getRemoteSocketAddress()).getHostString());
                sockets.put(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort(),socket);
                handleRead(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(final Socket socket){
        Thread inputThread = new Thread(){
            @Override
            public void run(){
                try{
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[maxByteSize];
                    int readCount = -1;
                    int port = ((InetSocketAddress)socket.getRemoteSocketAddress()).getPort();
                    while((readCount = inputStream.read(bytes))!= -1){
                        String msg = new String(bytes,0,readCount,charset);
                        if(msg.equals("88")){
                            log("client [%d] exit",port);
                        }else{
                            log("received client [%d] send msg: %s",port,msg);
                        }
                        //转发
                        msgList.setMsg(msg,port);
//                        handleForward(bytes,socket.getPort());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        inputThread.start();
    }

    private void handleWrite(Socket socket,String msg){
        try{
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(msg.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startForwardServer(){
        if(forwardThread == null){
            forwardThread = new Thread(){
                @Override
                public void run(){
                    while(forwardRunable){
                        List<ThreadSafeList.Message> msgs = msgList.getMsgs();
                        for(ThreadSafeList.Message msg : msgs){
                            for(Map.Entry<Integer,Socket> entry:sockets.entrySet()){
                                if(msg.port != entry.getKey()){
                                    try {
                                        log("forward msg [%s] to [%d]",msg.port,entry.getKey());
                                        entry.getValue().getOutputStream().write(msg.msg.getBytes(charset));
                                    } catch (IOException e) {
                                        log("client with %d failed!",msg.port);
                                    }
                                }
                            }
                        }
                    }
                }
            };
        }
        forwardThread.start();
    }

    public static void main(String[] args){
        PrimitiveServerSocket main = new PrimitiveServerSocket();
        main.startForwardServer();
        main.startServer();
    }

    private void log(String msg,Object... obj){
        System.out.println(String.format(msg,obj));
    }

}
