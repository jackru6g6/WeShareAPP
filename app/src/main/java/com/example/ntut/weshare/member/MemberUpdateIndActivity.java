package com.example.ntut.weshare.member;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ntut.weshare.Common;
import com.example.ntut.weshare.MainActivity;
import com.example.ntut.weshare.R;

import java.io.ByteArrayOutputStream;
import java.util.List;


public class MemberUpdateIndActivity extends AppCompatActivity {
    private final static String TAG = "UserUpdateActivity";

    private ImageView ivUser;
    private byte[] image = null;
    private EditText etNewPassword1;
    private EditText etNewPassword2;
    private EditText etName;
    private EditText etNumber;
    private EditText etEmail;
    private EditText etAddress;

    String action = "userUpdate";
    List<User> userOld = null;

    private static final int REQUEST_PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_uadate_ind_activity);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 從偏好設定檔中取得登入狀態來決定是否顯示「登出」

        SharedPreferences pref = getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
        String user = pref.getString("user", "");
        if (user == "") {
            Toast.makeText(this, "請先註冊登入WeShare~",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
//            Toast.makeText(this, user,
//                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void findViews() {
        ivUser = (ImageView) findViewById(R.id.ivUser);
        etNewPassword1 = (EditText) findViewById(R.id.etNewPassword1);
        etNewPassword2 = (EditText) findViewById(R.id.etNewPassword2);
        etName = (EditText) findViewById(R.id.etName);
        etNumber = (EditText) findViewById(R.id.etNumber);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etAddress = (EditText) findViewById(R.id.etAddress);

        showAllSpots();
    }


    private void showAllSpots() {
        if (Common.networkConnected(this)) {//檢查網路
            String url = Common.URL + "UserServlet";
            SharedPreferences pref = getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
            String account = pref.getString("user", "");
//            Toast.makeText(this, account, Toast.LENGTH_LONG).show();
//            List<User> user = null;
            int imageSize = 400;
            Bitmap bitmap = null;
            try {
                // passing null and calling get() means not to run FindImageByIdTask.onPostExecute()
//            bitmap = new UserGetImageTask(null).execute(url, account, imageSize).get();
                userOld = new UserGetAllTask().execute(url, account).get();//.get()要請UserGetAllTask()的執行結果回傳給我，會等他抓完資料(doInBackground的回傳結果)才會往下執行
                bitmap = new UserGetImageTask().execute(url, account, imageSize).get();//.execute(網址, 圖片id, 這邊做縮圖imageSize，在server端縮圖完再傳過來)
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (userOld == null) {
                Common.showToast(this, "不可能");
            } else {
                //rvSpots.setAdapter(new SpotsRecyclerViewAdapter(getActivity(), spots));//畫面RecyclerView(畫面,資料)，getActivity()取的他所依附的頁面(主頁面)

                ivUser.setImageBitmap(bitmap);

                ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out2);
                image = out2.toByteArray();

                etName.setText(userOld.get(0).getName());
                etNumber.setText(userOld.get(0).getTal());
                etEmail.setText(userOld.get(0).getEmail());
                etAddress.setText(userOld.get(0).getAddress());
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }
    }

    private void cleanPreferences() {
        SharedPreferences pref = getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        pref.edit()
                .putString("user", "")
                .putString("password", "")
                .putBoolean("login", false)
                .apply();
    }

    public void onPickPictureClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    Uri uri = intent.getData();
                    String[] columns = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, columns, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String imagePath = cursor.getString(0);
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        ivUser.setImageBitmap(bitmap);
                        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out2);
                        image = out2.toByteArray();
                    }
                    break;
            }
        }
    }


    public void onUpdateClick(View view) {
        SharedPreferences pref = getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
        String account = pref.getString("user", "");
        String oldPassword = pref.getString("password", "");
        String NewPW = etNewPassword1.getText().toString().trim();
        String NewPW2 = etNewPassword2.getText().toString().trim();
        if (NewPW.length() > 0 || NewPW2.length() > 0) {
            if (NewPW.length() <= 0) {
                Toast.makeText(this, R.string.msg_NewPasswordNull,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (NewPW2.length() <= 0) {
                Toast.makeText(this, R.string.msg_checkPasswordNull,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!NewPW.equals(NewPW2)) {
                Toast.makeText(this, R.string.msg_PWNotSame,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            NewPW = oldPassword;
        }

        //依選取項目顯示不同訊息
        if (userOld.get(0).getIdType() == 1) {
            action = "userUpdate";
        }

        String name = etName.getText().toString().trim();
        String tal = etNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        User user = new User(account, NewPW, name, tal, email, address, userOld.get(0).getIdType());//傳送文字資料

        int count = 0;
        String url = Common.URL + "UserServlet";

        if (Common.networkConnected(this)) {//傳送到server端
            String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);//圖片資料
            try {
                count = new UserUpdateTask().execute(url, action, user, imageBase64).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(MemberUpdateIndActivity.this, R.string.msg_updateFail);
            } else {
//                if (changePassword == true) {
                Common.showToast(MemberUpdateIndActivity.this, R.string.msg_updateSuccessAndLogin);
                cleanPreferences();
                Intent updateIntent = new Intent();
                updateIntent.setClass(MemberUpdateIndActivity.this, MemberLoginActivity.class);
                startActivity(updateIntent);
//                } else {
//                    Common.showToast(MemberUpdateIndActivity.this, R.string.msg_updateSuccess);
//                    finish();
//                }
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }

    }


    public void onCancelClick(View view) {
        finish();
    }
}
