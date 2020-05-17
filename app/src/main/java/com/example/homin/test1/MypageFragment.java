package com.example.homin.test1;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;

/**
 * A simple {@link Fragment} subclass.
 */
public class MypageFragment extends Fragment {

    private static final String TAG = "changeImage";
    private static final String TAG1 = "alstjrdl";
    private static final String TAG2 = "mini";


    public static userDataTableAdapter adapter;

    private static Context context;
    private static String key;
    private static DatabaseReference reference;
    private static ImageView imageView, imageView2;
    private TextView textView,textView2, textView5, wichi;

    private RecyclerView recycler;
    private List<UserDataTable> userDataTList;
    private String userName;

    // 공개여부 설정에 필요
    private Switch swich;
    private static String public_gab;
    public boolean Gong_ge;

    // 카메라 권한 필요한 것
    private static final int REQ_CODE_PERMISSION = 1;
    // 프로필 눌르면 팝업
    private PopupWindow mPopupWindow;

    private static Bitmap resizeImg;
    private static String curPhotoPath;

    // 리싸이클러뷰 디테일프래그먼트
    interface EssaySetlectedCallback {
        void onessaySetlected(int position);
    }

    private EssaySetlectedCallback callback;



    public MypageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof EssaySetlectedCallback) {
            callback = (EssaySetlectedCallback) context;
        } else {
            new  RuntimeException ("반드시 ProductSelectedCallback를 구현해야함.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        String downUriToStr = getArguments().getString("downUriToStr");
//        downUri = Uri.parse(downUriToStr);

        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        textView = view.findViewById(R.id.textView);
        textView.setText(DaoImple.getInstance().getLoginId());





        // 프로필 이미지 설정
        imageView = view.findViewById(R.id.imageView);
        imageView.setBackground(new ShapeDrawable(new OvalShape()));
        imageView.setClipToOutline(true);
        getProImg();

        // 프로필 이미지 버튼화 - 프로필 이미지 변경
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "마이 페이지 프로팔 클릭");
                View popupView = getLayoutInflater().inflate(R.layout.popup_permission, null);
                //popupView 에서 (LinearLayout 을 사용) 레이아웃이 둘러싸고 있는 컨텐츠의 크기 만큼 팝업 크기를 지정
                mPopupWindow= new PopupWindow(
                        popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                mPopupWindow.getContentView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPopupWindow.dismiss();
                    }
                });

                mPopupWindow.setFocusable(true); // 외부영역 선택시 종료

                mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                Button popCamera = popupView.findViewById(R.id.btn_popCamera);
                Button popGallery = popupView.findViewById(R.id.btn_popGallery);
                Button popPlusImg = popupView.findViewById(R.id.btn_popPlusImg);
                Button popupBaseImg = popupView.findViewById(R.id.btn_baseImg);
                Log.i(TAG, "팝업버튼 네개 나옴. ");


                //팝업버튼중에  "사진촬영" 버튼 선택시
                popCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "사진촬영 선택");
                        popupclickedCamera();
                        Log.i(TAG, "사진촬영메소드 실행 후");
//                        ((MainActivity)getActivity()).selectPhoto();
                        mPopupWindow.dismiss();
                    }
                });


                //팝업버튼중에  "앨범에서 사진선택" 버튼 선택시
                popGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "앨범에서 사진선택 클릭");
                        ((MapsActivity)getActivity()).clickedProImgBotton();
                        Log.i(TAG, "앨범 메소드 실행 후");
                        mPopupWindow.dismiss();

                    }
                });// end popGallery

                //팝업버튼중에  "확대해서보기" 버튼 선택시
                popPlusImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "확대해서보기 클릭");
                        //TODO: 이미지 확대보기 클릭시 사진이 크게 나와야함
                        mPopupWindow.dismiss();
                        key = DaoImple.getInstance().getKey();
                        Log.i(TAG, "누구 key: " + key);
                        String curProImgUrl = DaoImple.getInstance().getContact().getPictureUrl();
                        Log.i(TAG, "curProImgUrl: " + curProImgUrl);
                        Log.i(TAG, "imageView.getDrawable(): " + imageView.getDrawable());

                        //TODO:
                        if (curProImgUrl == null) { // Firebase에 저장된 파일이 있을 때
                            Log.i(TAG, "if문 안속 curProImgUrl값 :"+curProImgUrl);

                            Toast.makeText(getContext(), "저장된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                            mPopupWindow.dismiss();

                        } else { // 없을 때
                            Log.i(TAG, "else 안속 curProImgUrl값 :"+curProImgUrl);
                            View popupView = getLayoutInflater().inflate(R.layout.popup_plusimage, null);
                            //popupView 에서 (LinearLayout 을 사용) 레이아웃이 둘러싸고 있는 컨텐츠의 크기 만큼 팝업 크기를 지정
                            mPopupWindow= new PopupWindow(
                                    popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            mPopupWindow.getContentView().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mPopupWindow.dismiss();
                                }
                            }); // click
                            Log.i(TAG, "if값에서 나옴 :");
                            ImageView plusImage = popupView.findViewById(R.id.popup_PlusImageVeiw);
                            mPopupWindow.setFocusable(true); // 외부영역 선택시 종료
                            mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                            Log.i(TAG, "마지막줄 curProImgUrl , plusImage:"+ curProImgUrl +", "+plusImage);
                            Glide.with(getContext()).load(curProImgUrl).into(plusImage);

                        }



                    }
                }); //end  popPlusImg



                //팝업버튼중에  "기본이미지" 버튼 선택시
                popupBaseImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteProImgAlert();
                        mPopupWindow.dismiss();
                    }
                }); // popupbaseImg

            }
        });
        userDataTList = new ArrayList<>();

            recycler = view.findViewById(R.id.essay_listview);
            recycler.setHasFixedSize(true);

            recycler.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new userDataTableAdapter();
            recycler.setAdapter(adapter);






        return view;


    }

    private void deleteProImgAlert() {
        String urlCheck = DaoImple.getInstance().getContact().getPictureUrl();
        if (urlCheck != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("프로필 이미지 삭제");
            alert.setMessage("기본 이미지로 변경하시겠습니까?");

            alert.setNegativeButton("확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // storage
                            FirebaseStorage storage = FirebaseStorage.getInstance();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference dataRef = database.getReference();

                            StorageReference storageRef = storage.getReferenceFromUrl("gs://test33-32739.appspot.com/");
                            StorageReference pathRef = storageRef.child(key + "/").child("profileImage/curProImg.jpg");

                            pathRef.delete();
                            DaoImple.getInstance().getContact().setPictureUrl(null);

                            Contact contact = DaoImple.getInstance().getContact();

                            dataRef.child("Contact").child(key).setValue(contact);
                            imageView.setImageResource(R.drawable.what);
                        }
                    });

            alert.setPositiveButton("취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            alert.show();
        } else {
            Toast.makeText(context, "이미 기본 이미지로 설정되어 있습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void getProImg() {
        key = DaoImple.getInstance().getKey();
//        Log.i(TAG, "key: " + key);
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://test33-32739.appspot.com/");
//        StorageReference pathRef = storageRef.child(key + "/").child("profileImage/curProImg.jpg");

        String curProImgUrl = DaoImple.getInstance().getContact().getPictureUrl();
//        Log.i(TAG, "curProImgUrl: " + curProImgUrl);
//        Log.i(TAG, "imageView.getDrawable(): " + imageView.getDrawable());

        // 프로필 이미지 이슈 해결되면 아래 주석 풀기
        if (curProImgUrl != null) { // Firebase에 저장된 파일이 있을 때
            Glide.with(this).load(curProImgUrl).into(imageView);

        } else {
            imageView.setImageResource(R.drawable.what);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // 공개여부
        wichi = getView().findViewById(R.id.wichi);
        textView5 = getView().findViewById(R.id.textNot_recycle_write);
        textView2= getView().findViewById(R.id.textNot_recycle);
        imageView2 = getView().findViewById(R.id.imageNot_recycle);
        swich = getView().findViewById(R.id.switchBtn);
        key = DaoImple.getInstance().getKey();
        Gong_ge = DaoImple.getInstance().getContact().isPublic();
        Log.i(TAG1,"맨처음 Gong_ge 값은:  "+ Gong_ge);
        if (Gong_ge == true ) {
            swich.setChecked(true);
            Log.i(TAG1,"if가 ture 일때 Gong_ge 값:  "+ Gong_ge);
        } else {
            swich.setChecked(false);

            Log.i(TAG1,"if가 false 일때 Gong_ge 값:  "+ Gong_ge);
        }

        boolean checkPublic = DaoImple.getInstance().getContact().isPublic();
        if (checkPublic == true) {
            wichi.setTextColor(RED);
            wichi.setText("위치 공유중");
        }

        swich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {

                if ( ischecked == true ) {

                    wichi.setTextColor(RED);
                    wichi.setText("위치 공유중");
                    textView5.setTextColor(RED);
//                    textView5.setText("내가 쓴 글 목록");
                    reference.child("Contact").child(key).child("public").setValue(true);
                    Log.i(TAG1,"스위치 ture 일때 Gong_ge 값:  "+ ischecked);
                    Toast.makeText(MypageFragment.context, "내정보 공개", Toast.LENGTH_SHORT).show();
                } else {
                    wichi.setText("");
                    textView5.setTextColor(GRAY);
//                    textView5.setText("내가 쓴 글 목록");
                    reference.child("Contact").child(key).child("public").setValue(false);
                    Log.i(TAG1,"스위치 false 일때 Gong_ge 값:  "+ ischecked);
                    Toast.makeText(MypageFragment.context, "내정보 비공개", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 리사이클러 뷰

        if (userDataTList.size()== 0) {
            key = DaoImple.getInstance().getKey();
            Log.i(TAG, "Key값 : " + key);

            reference = FirebaseDatabase.getInstance().getReference();
            reference.child("userData").child(key).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Object obj = dataSnapshot.getValue();
                    Log.i(TAG, "온차일드에디드 안 속 obj :" + obj);
//                Log.i(TAG, "dataSnapshot.getValue(): " + dataSnapshot.getValue());
//                UserData data = dataSnapshot.getValue(UserData.class);
                    String snapshot = String.valueOf(obj);
                    Log.i(TAG, "온스타트_온차이드 String snapshot" + snapshot);
                    UserDataTable data = dataSnapshot.getValue(UserDataTable.class);

                    Log.i(TAG, "온스타트_온차이드 UserDtaTable data" + data);
//                Log.i(TAG, "line302) userDtaTlist.add(data) :" + userDataTList.add(data));
                    userDataTList.add(data);
                    Log.i(TAG, "온스타트_온차이드....여기까지만 나와도 행복하것다...");
//                Log.i(TAG, "data.getName(): " + data.getTitle());
                    int size = userDataTList.size();

                    Log.i(TAG, "온차일드 userDataTList.size() " + size);
//                Log.i(TAG, "userDataTList.size(): " + size);
//                Log.i("ggg","데이터 받아옴");
                    if (userDataTList != null){
                        textView2.setVisibility(View.GONE);
                        imageView2.setVisibility(View.GONE);
                        textView5.setText("내가 쓴 글 목록");
                    }



                    if (!userDataTList.isEmpty()) {

                    }

                    adapter.notifyDataSetChanged();


                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    // Adapter 클래스
    class userDataTableAdapter extends RecyclerView.Adapter<userDataTableAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i(TAG, "onCreateViewHolder");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View itemView = inflater.inflate(R.layout.essay, parent, false);
            ViewHolder holder = new ViewHolder(itemView);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            UserDataTable userData = userDataTList.get((userDataTList.size()-1)-position);

                holder.textTitle.setText(userData.getTitle());  // 글제목
                //TODO: 글 내용살짝 보기
                String content = userData.getContent();
                Log.i(TAG, "onBindViewHolder: " + userData.getContent());
                String cutcontent = "";
                if (userData.getContent().length() > 30) {
                    cutcontent = content.substring(0, 28);
                    cutcontent += "...";
                } else {
                    cutcontent = userData.getContent();
                }
                holder.tectContent.setText(cutcontent); // 저장 글 짤라서 보이기!

                /**위도,경도를 주고값으로 가져오는 코드 */
                Double lat = userData.getLocation().get(0);
                Double lng = userData.getLocation().get(1);
                String address = getAddress(getContext(), lat, lng);
                holder.textLocation.setText(address);

                String dateFormat = userData.getData();
                String najjanaom = DaoImple.getInstance().getDateFormat(dateFormat);
                holder.textDate.setText(najjanaom); // 글 날짜
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DaoImple.getInstance().setMyPageUserData(userDataTList.get((userDataTList.size() - 1) - position));
                        callback.onessaySetlected((userDataTList.size() - 1) - position);
                    }
                });

        }//end onBindViewHolder()

        @Override
        public int getItemCount() {
            int size = userDataTList.size();
            Log.i(TAG, "userDataTList.size(): " + size);
            return userDataTList.size();

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle;
            TextView textLocation;
            TextView tectContent;
            TextView textDate;

            public ViewHolder(View itemView) {
                super(itemView);
                Log.i(TAG, "ViewHolder");
                textTitle = itemView.findViewById(R.id.textTitle);
                tectContent = itemView.findViewById(R.id.textContent);
                textLocation = itemView.findViewById(R.id.textLocation);
                textDate = itemView.findViewById(R.id.textDate);
            }
        }
    }

// 위도, 경도를 주소값으로 가져오는 메소드!!!!
public static String getAddress(Context context,double lat, double lng) {
    String nowAddress ="현재 위치를 확인 할 수 없습니다.";
    Geocoder geocoder = new Geocoder(context,Locale.KOREA);
    List <Address> address;
    try {
        if (geocoder != null) {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geocoder.getFromLocation(lat, lng, 1);

            if (address != null && address.size() > 0) {
                // 주소 받아오기
                String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                nowAddress  = currentLocationAddress;

            }
        }

    } catch (IOException e) {
        Toast.makeText(context, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();

        e.printStackTrace();
    }
    return nowAddress;
}

    // 프로필 이미지 '사진촬영'했을 때 찍은 사진 저장되는 폴더 및 파일명 세팅
    public static File createImageFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory() + "/MyApp/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgName = timeStamp + ".jpg";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/MyApp/" + imgName);
        curPhotoPath = storageDir.getAbsolutePath();

        return storageDir;

    } // end createImageFile()

    // 프로필 사진 선택시 리사이즈해서 storage에 업로드
    public static void resizeImg(Uri getUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dataRef = database.getReference();

        String filename = "curProImg_resize.png";
        key = DaoImple.getInstance().getKey();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://test33-32739.appspot.com/").child(key + "/").child("profileImage/" + filename);

        try {
            Bitmap orgImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), getUri);


            int imgHeight = orgImage.getHeight();
            int imgWidth = orgImage.getWidth();
            float aspectRatio = orgImage.getWidth() / (float) orgImage.getHeight();
            if (imgHeight >= imgWidth) {
                imgWidth = 100;
                imgHeight = Math.round(imgWidth / aspectRatio);
                resizeImg = Bitmap.createScaledBitmap(orgImage, imgWidth, imgHeight, true);
            } else {
                imgHeight = 100;
                imgWidth = Math.round(imgHeight * aspectRatio);
                resizeImg = Bitmap.createScaledBitmap(orgImage, imgWidth, imgHeight, true);
            }

            resizeImg = PersonItemRenderer.getCircleBitmap(resizeImg);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            resizeImg.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), resizeImg, "Title", null);
            Uri bitmapUri = Uri.parse(path);

            storageRef.putFile(bitmapUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downThisUri = taskSnapshot.getDownloadUrl();
                    String downUriToStr = downThisUri.toString();

                    DaoImple.getInstance().getContact().setResizePictureUrl(downUriToStr);

                    Contact contact = DaoImple.getInstance().getContact();

                    dataRef.child("Contact").child(key).setValue(contact);

                    String str = "Pictures";
                    setDirEmpty(str);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 리사이즈하면서 기기에 저장된 100x100 이미지 삭제
    public static void setDirEmpty(String dirName) {
        String path = Environment.getExternalStorageDirectory() + File.separator + dirName;

        File dir = new File(path);
        File[] childFileList = dir.listFiles();

        if (dir.exists()) {
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    setDirEmpty(childFile.getAbsolutePath());    //하위 디렉토리
                } else {
                    childFile.delete();    //하위 파일
                }
            }
            dir.delete();
        }
    } // end setDirEmpty()



    // Firebase에 사진 업로드하는 메소드
    public static void uploadFile(Uri filePath) {

        // storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dataRef = database.getReference();

        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("프로필 사진 업데이트 중...");
            progressDialog.show();

            // 파일명 지정
            String filename = "curProImg.jpg";
            key = DaoImple.getInstance().getKey();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://test33-32739.appspot.com/").child(key + "/").child("profileImage/" + filename);

            storageRef.putFile(filePath)
                    // 성공했을 때
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "프로필 업데이트 완료!", Toast.LENGTH_SHORT).show();

                            Uri downUri = taskSnapshot.getDownloadUrl();
                            Log.i(TAG, "다운로드uri: " + downUri);
                            String downUriToStr = downUri.toString();

                            DaoImple.getInstance().getContact().setPictureUrl(downUriToStr);

                            Log.i(TAG,key);

                            Contact contact = DaoImple.getInstance().getContact();
                            Log.i(TAG, "contact: " + contact.getPictureUrl());

                            dataRef.child("Contact").child(key).setValue(contact);

                            // 변경된 프로필 이미지 ImageView에 바로 적용
                            Glide.with(context).load(downUri).into(imageView);

                        }
                    })
                    // 실패했을 때
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "업데이트 실패..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    // 진행중..
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else {
            Toast.makeText(context, "사진을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        } // end if

    } // end uploadFile()


    public void popupclickedCamera() {
        //TODO: 카메라 권한요청 코드 (1)
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (hasPermissions(permissions)) {

            Log.i(TAG, "권한이 요청되있나 확인.");
            // 필요한 권한들이 허용된 경우 메소드 실행
            ((MapsActivity) getActivity()).popupCameraInCameraMethod();
        } else {
            // 필요한 권한들이 허용되어 있지 않는 경우
            // 사용자에게 권한 요청 다이얼로그를 보여줌
            // -> 사용자가 거부/허용을 선택
            // -> 사용자의 선택 결과는 onRequestPermissionResult() 메소드로 전달됨
            Log.i(TAG, "권한 요청이 확인되지 않았을경우");

            if (shouldShowRequestPermissionRationale(permissions[0]) != true) {
                Log.i(TAG, "너 나오는데 왜 체크박스가 안뜨니?1");
                this.shouldShowRequestPermissionRationale(permissions[0]);
            } else if (shouldShowRequestPermissionRationale(permissions[1]) != true) {
                Log.i(TAG, "너 나오는데 왜 체크박스가 안뜨니?2");
                this.shouldShowRequestPermissionRationale(permissions[1]);
            }
            this.requestPermissions(permissions, REQ_CODE_PERMISSION);

            Log.i(TAG, "설정한 request코드를 보내줍니다!");
        }
    }

    //TODO 옮기는 과정 (2)
    private boolean hasPermissions(String[] permissions) {
        boolean result = true;
        for (String p : permissions) {

            //TODO 권한 획득하기 전 권한 유효성 체크 - 현재 앱이 특정 권한을 갖고 있는지 확인 가능
            if (PermissionChecker.checkSelfPermission(getContext(), p) != PackageManager.PERMISSION_GRANTED) {
                result = false;
                break;
            }
        }
        return  result;
    }

    //TODO 옮기는 과정 (3)
    // 안드로이드7.0 버전 이상부터 카메라 권한 허가 요청 코드가 필요.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO: 사용권한 요청
        if (requestCode == REQ_CODE_PERMISSION) {
            // 사용자가 camera와 (READ&Write) 권한을 모두 허용한 경우에만
            // popupCameraInCameraMethod() 호출
            if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "너 나옴2? 요청");
                ((MapsActivity)getActivity()).popupCameraInCameraMethod();
            } else {
                Toast.makeText(context, "권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }



}
