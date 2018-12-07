package com.bai.imagecycle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageCycleView IC;
    private Button bu_1,bu_2;
    private ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IC = findViewById(R.id.IC);

        list = new ArrayList<>();
        list.add("http://seopic.699pic.com/photo/50035/0520.jpg_wh1200.jpg");
        list.add("https://img.pc841.com/2018/0815/20180815101229911.jpg");
        list.add("http://s1.sinaimg.cn/large/001vhiLJzy7dNoP6PHl0b");

        IC.setImageResources(list,R.layout.layout,listener);
    }

    private ImageCycleView.ImageCycleViewListener listener = new ImageCycleView.ImageCycleViewListener(){

        @Override
        public void displayImage(String imageURL, TempRoundImage imageView) {
            Picasso.with(MainActivity.this).load(imageURL).into(imageView);
        }

        @Override
        public void onImageClick(int position, View imageView) {
            Toast.makeText(MainActivity.this,"第"+position+"张图",Toast.LENGTH_SHORT).show();
        }

        @Override
        public View onView(int layoutId) {
            View view = LayoutInflater.from(MainActivity.this).inflate(layoutId,null);
            bu_1 = view.findViewById(R.id.bu_1);
            bu_2 = view.findViewById(R.id.bu_2);
            bu_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this,"button1", Toast.LENGTH_SHORT).show();
                }
            });
            bu_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this,"button2", Toast.LENGTH_SHORT).show();
                }
            });
            return  view;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (IC!=null)
            IC.startImageCycle();
    }

    @Override
    protected void onPause() {
        if (IC!=null)
            IC.pushImageCycle();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (IC!=null)
            IC.pushImageCycle();
        super.onDestroy();
    }
}
