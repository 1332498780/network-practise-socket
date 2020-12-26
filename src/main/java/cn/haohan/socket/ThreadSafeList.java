package cn.haohan.socket;

import java.util.ArrayList;
import java.util.List;

public class ThreadSafeList {

    ArrayList<Message> msgs;

    public ThreadSafeList(){
        msgs = new ArrayList<>();
    }

    public synchronized void setMsg(String msg,int port){
        msgs.add( Message.build().msg(msg).port(port) );

        this.notify();
    }

    public synchronized List<Message> getMsgs(){
        while(msgs.isEmpty()){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Message> temp = (ArrayList<Message>) msgs.clone();
        msgs.clear();
        return temp;
    }

    public static class Message {
          int port;
          String msg;

        static Message build(){
            return new Message();
        }
        Message port(int port){
            this.port = port;
            return this;
        }
        Message msg(String msg){
            this.msg = msg;
            return this;
        }

    }
}
