package com.example.project_1;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import model.BuyModel;
import model.ChatModel;
import model.ShareModel;

import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class WriteActivity extends AppCompatActivity {

    //인스턴스 선언
    private FirebaseAuth firebaseAuth;
    String uid;
    EditText et_title;
    EditText et_description;
    Spinner category;
    Button btn_exit;
    Button btn_save;
    NumberPicker targetNum;
    DatabaseReference ref_share;
    DatabaseReference ref_buy;
    long shareMaxNum=0;
    long buyMaxNum=0;

    private long shareCount;
    private  long buyCount;

    //채팅방 인스턴스
    private FirebaseDatabase database;
    String roomNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        //인스턴스 초기화
        firebaseAuth = FirebaseAuth.getInstance();

        et_title = findViewById(R.id.write_title);
        et_description = findViewById(R.id.description);
        btn_exit = findViewById(R.id.back_button);
        btn_save = findViewById(R.id.btn_save);
        category = (Spinner) findViewById(R.id.category);
        targetNum = (NumberPicker) findViewById(R.id.targetNoP);

        // 목표인원수 제한 설정 및 설정값 가져오기
        targetNum.setMinValue(1);
        targetNum.setMaxValue(6);

        //'나눔' 게시글 자동번호 생성
//        ref_share = FirebaseDatabase.getInstance().getReference().child("Share");
//        ref_share.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    shareMaxNum = dataSnapshot.getChildrenCount();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        // Share에서 가장 마지막 글 번호 가져오기
        ref_share = FirebaseDatabase.getInstance().getReference().child("Share");
        ref_share.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot latestItem : dataSnapshot.getChildren()) {
                    ShareModel shareModel = latestItem.getValue(ShareModel.class);

                    shareCount = Long.parseLong(shareModel.id.substring(1));
                    shareCount++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //'구매' 게시글 자동번호 생성
//        ref_buy = FirebaseDatabase.getInstance().getReference().child("Buy");
//        ref_buy.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    buyMaxNum = dataSnapshot.getChildrenCount();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
        ref_buy = FirebaseDatabase.getInstance().getReference().child("Buy");
        ref_buy.orderByChild("id").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot latestItem : dataSnapshot.getChildren()) {
                    BuyModel buyModel = latestItem.getValue(BuyModel.class);

                    buyCount = Long.parseLong(buyModel.id.substring(1));
                    buyCount++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // 게시글 유형 선택시
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //'나눔' 선택시
                if(position == 1) {
                    targetNum.setEnabled(false);
                    btn_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            uid = firebaseAuth.getCurrentUser().getUid();
                            ShareModel shareModel = new ShareModel();

                            shareModel.idNum = Long.toString(shareCount);
                            shareModel.id = setShareId(shareCount);
                            shareModel.title = et_title.getText().toString();
                            shareModel.host = uid;
                            shareModel.description = et_description.getText().toString();
                            ref_share.child(shareModel.id).setValue(shareModel);
//                            ref_share.child(String.valueOf(shareMaxNum + 1)).setValue(shareModel);

                            //채팅방 생성
                            ChatModel chatModel = new ChatModel();
                            chatModel.host = uid;
                            chatModel.roomId = shareModel.id;
//                            database.getInstance().getReference("Share").child(roomNumber).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    ShareModel shareModel = dataSnapshot.getValue(ShareModel.class);
//                                    uid = shareModel.host;
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });

                            chatModel.users.put(uid, true);

                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(chatModel.roomId).setValue(chatModel);

                            finish();
                        }
                    });

                //'구매' 선택시
                } else {
                    targetNum.setEnabled(true);
                    btn_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            uid = firebaseAuth.getCurrentUser().getUid();
                            BuyModel buyModel = new BuyModel();

                            buyModel.id = setBuyId(buyCount);
                            buyModel.idNum = Long.toString(buyCount);
                            buyModel.title = et_title.getText().toString();
                            buyModel.host = uid;
                            buyModel.description = et_description.getText().toString();
                            buyModel.currentNOP = "" +0;

                            buyModel.targetNOP = targetNum.getValue() + "";
                            ref_buy.child(buyModel.id).setValue(buyModel);
//                            ref_buy.child(String.valueOf(buyMaxNum + 1)).setValue(buyModel);

                            finish();
                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private String setShareId(long num) {
        String id = "S" + num;
        return id;
    }

    private String setBuyId(long num) {
        String id = "B" + num;
        return id;
    }
}
