package com.example.student.chatapp;

import android.app.DownloadManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    static final int SocketServerPORT = 8080;  // Port should be fetched dynamically in real systems.// NSD Manager and service registration code
    private String SERVICE_NAME = "Ravi";
    private String SERVICE_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;
    ServerSocket serverSocket;
    String Message="";
    String Pc_ip="";
    pcServer server;
    int Pcport;
    EditText editText;
    int id=0;
    String[] Messages ;
    HashMap<String,JSONObject> hashMap; // Hashmap to store user name with their data
    JSONArray jsonArray;
    RecyclerView recycle;
    RecyclerAdapter recyclerAdapter;
    EditText url;
    Button connect;
    HashMap<String,DataInputStream> allDIN;
    HashMap<String,DataOutputStream> allDOUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonArray=new JSONArray();
        Messages= new String[4];
        hashMap=new HashMap<String, JSONObject>();
        allDIN=new HashMap<String, DataInputStream>();
        allDOUT=new HashMap<String, DataOutputStream>();

        url=findViewById(R.id.port);

        for(int i=0;i<4;i++)
            Messages[i]="";


        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        Server server = new Server();
        server.start();

        recycle = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerAdapter = new RecyclerAdapter(hashMap,this,allDIN,allDOUT);
        RecyclerView.LayoutManager recyce = new GridLayoutManager(this,1);
        recycle.setLayoutManager(recyce);
        recycle.setItemAnimator(new DefaultItemAnimator());
        recycle.setAdapter(recyclerAdapter);
    }

    public void Done(View V)
    {
        EditText editText =(EditText)findViewById(R.id.sname);
        SERVICE_NAME=editText.getText().toString();
        TextView tv = (TextView) findViewById(R.id.textView1);


        tv.setText("Service Name: "+SERVICE_NAME);
        registerService(SocketServerPORT);

    }
    public void ConnectToPc(View view)
    {
        /*EditText editText = (EditText) findViewById(R.id.ip);
        String ip= editText.getText().toString();
        editText = (EditText) findViewById(R.id.port);
        String port = editText.getText().toString();

        Pc_ip=ip;
        Pcport=Integer.parseInt(port);
        String IPADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";


       Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(Pc_ip);


        if(matcher.matches())
        {

                // connecting with remote pc server

                server = new pcServer(Pc_ip,Pcport);
                server.start();
            view.setEnabled(false);



        }
        else
        {
            Toast.makeText(this,"Please provide a valid ip Address",Toast.LENGTH_LONG).show();
        }*/

    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager.registerService(serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                mRegistrationListener);
    }

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            SERVICE_NAME = mServiceName;
            Toast.makeText(MainActivity.this, "Successfully registered",
                    Toast.LENGTH_LONG).show();
            Log.d("NsdserviceOnRegister", "Registered name : " + mServiceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {

            Toast.makeText(MainActivity.this, "Registration failed",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // NsdManager.unregisterService() called and passed in this listener.
            Log.d("NsdserviceOnUnregister",
                    "Service Unregistered : " + serviceInfo.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                           int errorCode) {
            //Fail
        }
    };

    private class clientHandler extends Thread
    {
        // Thread for clients connection
        String Id;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        String message="";

        public clientHandler(String id,DataInputStream inputStream,DataOutputStream outputStream)
        {
            this.Id=id;
            dataInputStream=inputStream;
            dataOutputStream=outputStream;

        }
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    message=dataInputStream.readUTF();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject jsonObject = new JSONObject(message);
                                hashMap.put(Id,jsonObject);
                                recyclerAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e){
                                Log.d("Exception","JSON Parsing");
                            }
                        }
                    });
                }
                catch (final IOException e)
                {
                    // if user is not available remove user from hashmap
                    hashMap.remove(Id);
                    break;
                }
            }
        }

        Map<String,String> parseJSONObject(String id,JSONObject jsonObject) throws Exception{
            Map<String,String> m=new HashMap<String,String>();
            m.put("id",id);
            m.put("cpu",jsonObject.getString("cpu"));
            m.put("memory",jsonObject.getString("memory"));
            m.put("name",jsonObject.getString("name"));
            m.put("battery",jsonObject.getString("battery"));
            m.put("Bandwidth",jsonObject.getString("Bandwidth"));
            m.put("GyroX",jsonObject.getString("GyroX"));
            m.put("GyroY",jsonObject.getString("GyroY"));
            m.put("GyroZ",jsonObject.getString("GyroZ"));
            m.put("Proxy",jsonObject.getString("Proxy"));
            return m;
        }
    }

    private class Server extends Thread {
        @Override
        public void run()

        {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Server Hasbeen started",Toast.LENGTH_LONG).show();
                }
            });
                serverSocket = new ServerSocket(SocketServerPORT);
                Socket socket;
                while(true) {

                    socket= serverSocket.accept();
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOuputStream = new DataOutputStream(socket.getOutputStream());
                    final String name = dataInputStream.readUTF();
                    if(!hashMap.containsKey(name))
                    {
                        dataOuputStream.writeUTF("1");
                        clientHandler ch = new clientHandler(name, dataInputStream, dataOuputStream);
                        allDOUT.put(name,ch.dataOutputStream);
                        allDIN.put(name,ch.dataInputStream);
                        ch.start();  // whenever a new client come start a new thread

                    }
                    else
                    {
                        dataOuputStream.writeUTF("0");
                        socket.close();
                    }
                    id++;
                }

            } catch (IOException e) {

            }

        }
    }

private class pcServer extends Thread {

    String Pc_ip;
    int PcPort;

    public pcServer(String ip ,int port)
    {
        Pc_ip=ip;
        Pcport=port;
    }
    @Override
    public void run() {

        try {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this," Connecting to "+Pc_ip,Toast.LENGTH_LONG).show();
                }
            });
            Socket socket = new Socket(Pc_ip,Pcport);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Conneced to server",Toast.LENGTH_LONG).show();
                }
            });
            while(true)
            {   jsonArray= new JSONArray();
                Iterator iterator = hashMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                HashMap.Entry pair= (HashMap.Entry)iterator.next();
                    JSONObject j1 =(JSONObject) pair.getValue();
                    jsonArray.put(j1);  // creating json array

                }
                final JSONObject mainobj = new JSONObject();
                mainobj.put("Data",jsonArray);
                dos.writeUTF(mainobj.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(R.id.textView);
                        textView.setText(mainobj.toString());
                    }
                });
                dos.flush();

                Thread.sleep(1000);

           }

        }
        catch (final Exception e)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textView = (TextView)findViewById(R.id.textView);
                    textView.setText(""+e);
                }
            });
        }


    }
    }
}
