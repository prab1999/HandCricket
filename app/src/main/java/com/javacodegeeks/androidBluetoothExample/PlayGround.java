package com.javacodegeeks.androidBluetoothExample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.javacodegeeks.R;

import java.util.ArrayList;

import static com.javacodegeeks.androidBluetoothExample.MainActivity.MESSAGE_TOAST_FAILED;
import static com.javacodegeeks.androidBluetoothExample.MainActivity.MESSAGE_TOAST_LOST;

public class PlayGround extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothChatService mChatService =null;

    private TextView batsman,bowler,scr,overview,targetview,targetval;
    private LinearLayout targetlabel;
    private ImageView roleimage,batready,bowlready;
    private RadioGroup rg;
    private String role="";
    private String bt="",bw="";
    private String over="",wicket="";
    private double curover=0.0;
    private int curwicket=0,curscore=0,inning=1,target=0;
    private ConstraintLayout finalscore;
    private TextView yourscore,oppscore,win;
    private int uscore,oscore,uwicket,owicket;
    private double uover,oover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_ground);
        Intent i=getIntent();
        role=i.getStringExtra("Role");
        over=i.getStringExtra("over");
        wicket=i.getStringExtra("wicket");
        System.out.println(role==null);
        bowler=(TextView)findViewById(R.id.textView);
        batsman=(TextView)findViewById(R.id.textView2);
        scr=(TextView)findViewById(R.id.textView3);
        overview=(TextView)findViewById(R.id.over);
        rg=(RadioGroup)findViewById(R.id.rg);
        roleimage=(ImageView)findViewById(R.id.roleimage);
        batready=(ImageView)findViewById(R.id.batready);
        bowlready=(ImageView)findViewById(R.id.bowlready);
        mChatService = BluetoothChatService.getInstance();
        mChatService.changeHandler(mHandler);
        overview.setText(curover+"("+over+")");
        targetview=(TextView)findViewById(R.id.target);
        targetlabel=(LinearLayout)findViewById(R.id.targetlabel);
        targetval=(TextView)findViewById(R.id.target);
        finalscore=(ConstraintLayout)findViewById(R.id.finalscore);
        oppscore=(TextView)findViewById(R.id.oppscore);
        yourscore=(TextView)findViewById(R.id.yourscore);
        win=(TextView)findViewById(R.id.win);
        if(role.equals("Bowler")){
            roleimage.setImageResource(R.drawable.bowl);
        }
        else{
            roleimage.setImageResource(R.drawable.batsman);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb=(RadioButton)findViewById(i);
                if((i!=-1)&&rb.isChecked()){
                String message="";


                if(rb!=null){

                    message=rb.getText().toString();
                   }
                    System.out.println("msg "+message);
                if(role.equals("Batsman"))batsman.setText(message);
                else bowler.setText(message);
                sendMessage(message);
                (new Wait()).execute();}

            }
        });
    }

    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            for(int i=0;i<rg.getChildCount();i++){
                ((RadioButton)rg.getChildAt(i)).setEnabled(false);
            }
            System.out.println("sending  ");
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field

        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_WRITE:

                    byte[] writeBuf = (byte[]) msg.obj;

                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if(role.equals("Bowler")){
                        bowlready.setImageResource(R.drawable.green_chat);
                    bw=writeMessage;}
                    else{
                        batready.setImageResource(R.drawable.green_chat);
                        bt=writeMessage;}
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    System.out.println("ffd "+readMessage);
                    if(role.equals("Bowler")){
                        batready.setImageResource(R.drawable.green_chat);
                        bt=readMessage;}
                    else{
                        bowlready.setImageResource(R.drawable.green_chat);
                        bw=readMessage;}
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST_FAILED:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST_LOST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private class Wait extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            while(bt.equals("")||bw.equals("")){
                System.out.print("...");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            int a=Integer.parseInt(bt);
            int b=Integer.parseInt(bw);
            if(role.equals("Batsman"))bowler.setText(bw);
            else {
                batsman.setText(bt);
            }
            if(a==b){
                if(role.equals("Bowler"))
                    Toast.makeText(PlayGround.this,"You Bowled The batsman",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(PlayGround.this,"You are Out",Toast.LENGTH_SHORT).show();
                curwicket=(curwicket+1);


            }
            else{

                curscore+=a;
                if(role.equals("Bowler"))
                    Toast.makeText(PlayGround.this,"You Have given "+a+" runs",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(PlayGround.this,"You Hit "+a+" runs",Toast.LENGTH_SHORT).show();
            }
            curover+=0.1;
            if(curover-(int)curover==0.6)
                curover+=0.4;
            scr.setText(curscore+"/"+curwicket);
            overview.setText(curover+"("+over+")");
            reset();
            if((curover==Double.parseDouble(over)||curwicket==Integer.parseInt(wicket))&&inning==1){
                changeInning();
            }
            else if((curover==Double.parseDouble(over)||curwicket==Integer.parseInt(wicket)||curscore>=target)&&inning==2){
                prepWinner();
            }


        }
    }
    void prepWinner(){
        finalscore.setVisibility(View.VISIBLE);
        yourscore.setText(uscore+"");
        if(role.equals("Batsman")){
            uscore=curscore;
            uover=curover;
            uwicket=curwicket;
            if(curscore>=target){
                win.setText("You Won!");
            }
            else{
                win.setText("You Lost!");
            }
        }
        else{
            oscore=curscore;
            oover=curover;
            owicket=curwicket;
            if(curscore>=target){
                win.setText("You Lost!");
            }
            else{
                win.setText("You Won!");
            }
        }
        yourscore.setText(uscore+"/"+uwicket+"("+uover+")");
        oppscore.setText(oscore+"/"+owicket+"("+oover+")");

    }
    void changeInning(){
        Toast.makeText(this,"Changing Innings",Toast.LENGTH_LONG).show();
        inning=2;
        target=curscore+1;
        targetlabel.setVisibility(View.VISIBLE);
        targetval.setText(target+"");
        if(role.equals("Bowler"))role="Batsman";
        else role="Bowler";
        if(role.equals("Bowler")){
            uscore=target;
            uwicket=curwicket;
            uover=curover;
            roleimage.setImageResource(R.drawable.bowl);
        }
        else{
            oscore=target;
            owicket=curwicket;
            oover=curover;
            roleimage.setImageResource(R.drawable.batsman);
        }
        curscore=0;
        curover=0.0;
        curwicket=0;
        targetview.setVisibility(View.VISIBLE);
        targetlabel.setVisibility(View.VISIBLE);

        scr.setText(curscore+"/"+curwicket);
        overview.setText(curover+"("+over+")");
    }
    void reset(){
        bt="";
        bw="";;
        rg.clearCheck();
        for(int i=0;i<rg.getChildCount();i++){
            ((RadioButton)rg.getChildAt(i)).setEnabled(true);
        }
        batready.setImageResource(R.drawable.red_chat);
        bowlready.setImageResource(R.drawable.red_chat);
    }
}
