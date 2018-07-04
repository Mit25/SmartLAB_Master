package com.example.student.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mit25 on 6/6/18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder>{

    HashMap<String,JSONObject> list;
    Context context;
    HashMap<String,DataInputStream> allDIN;
    HashMap<String,DataOutputStream> allDOUT;

    public RecyclerAdapter(HashMap<String,JSONObject> list, Context context, HashMap<String,DataInputStream> allDIN, HashMap<String,DataOutputStream> allDOUT) {
        this.list = list;
        this.context = context;
        this.allDIN=allDIN;
        this.allDOUT=allDOUT;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview,parent,false);
        MyHolder myHolder = new MyHolder(view,context,list,allDIN,allDOUT);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position){
        Iterator it=list.entrySet().iterator();
        int cnt=0;
        if(!it.hasNext())
            return;
        HashMap.Entry pair=(HashMap.Entry)it.next();

        while(cnt<position && it.hasNext()){
            pair=(HashMap.Entry) it.next();
            cnt++;
        }
        holder.id.setText(pair.getKey().toString());
        holder.data.setText(pair.getValue().toString());
    }

    @Override
    public int getItemCount() {
        int arr = 0;
        try{
            if(list.size()==0){
                arr = 0;
            }
            else{
                arr=list.size();
            }
        }catch (Exception e){
        }
        return arr;
    }


    public static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView id,data;
        HashMap<String,JSONObject> l;
        Context ctx;
        HashMap<String,DataInputStream> allDIN;
        HashMap<String,DataOutputStream> allDOUT;

        public MyHolder(View itemView,Context ctx,HashMap<String,JSONObject> list,HashMap<String,DataInputStream> allDIN, HashMap<String,DataOutputStream> allDOUT) {
            super(itemView);
            this.ctx = ctx;
            this.l = list;
            itemView.setOnClickListener(this);
            id = (TextView) itemView.findViewById(R.id.dID);
            data = (TextView) itemView.findViewById(R.id.data);
            this.allDIN=allDIN;
            this.allDOUT=allDOUT;
        }

        @Override
        public void onClick(View view) {
            int positon = getAdapterPosition();
            Iterator it = allDIN.entrySet().iterator();
            Iterator it1 = allDOUT.entrySet().iterator();
            int cnt=0;
            HashMap.Entry pair=null,pair1=null;
            while (cnt<=positon && it.hasNext() && it1.hasNext()) {
                pair= (HashMap.Entry)it.next();
                pair1= (HashMap.Entry)it1.next();
                cnt++;
            }
            if(pair!=null && pair1!=null) {
                String name=(String)pair.getKey();
                DataInputStream din = (DataInputStream) pair.getValue();
                final DataOutputStream dout=(DataOutputStream) pair1.getValue();

                LayoutInflater li = LayoutInflater.from(ctx);
                View pv = li.inflate(R.layout.activity_client, null);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this.ctx);
                builder1.setView(pv);

                final CheckBox cpu=pv.findViewById(R.id.cpu);
                final CheckBox memory=pv.findViewById(R.id.memory);
                final CheckBox battery=pv.findViewById(R.id.battery);
                final CheckBox bw=pv.findViewById(R.id.bw);
                final CheckBox proxy=pv.findViewById(R.id.proxy);
                final CheckBox gyro=pv.findViewById(R.id.gyro);
                final CheckBox file=pv.findViewById(R.id.file);

                builder1.setCancelable(true);
                builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String msg="";
                            if(cpu.isChecked()) msg+="T"; else msg+="F";
                            if(memory.isChecked()) msg+="T"; else msg+="F";
                            if(battery.isChecked()) msg+="T"; else msg+="F";
                            if(bw.isChecked()) msg+="T"; else msg+="F";
                            if(proxy.isChecked()) msg+="T"; else msg+="F";
                            if(gyro.isChecked()) msg+="T"; else msg+="F";
                            if(file.isChecked()) msg+="T"; else msg+="F";
                            try {
                                dout.writeUTF(msg);
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

                builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        }
    }

}