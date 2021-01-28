package com.example.mypdfdoclisttext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.mypdfdoclisttext.utils.CallOtherOpeanFile;
import com.example.mypdfdoclisttext.utils.ProgressDialog;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefan on 2021/1/28
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    public ProgressDialog dialog;
    private ListView mListview;
    private Context context;
    private List<AddFileInfo> list = new ArrayList<AddFileInfo>();
    private String filePath = Environment.getExternalStorageDirectory().toString() + File.separator;
    private static Adapter adapter;
    private ACache aCache;
    private String fileDate = "";

    // 要申请的权限
    private String[] permissions = {Permission.MANAGE_EXTERNAL_STORAGE, Permission.CAMERA};


    /**
     * 开始提交请求权限
     */
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //取消严格模式  FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
//        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 检查该权限是否已经获取
//            int i = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]);
//            int l = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[1]);
//            int m = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[2]);
//            int n = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[3]);
//            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
//            if (i != PackageManager.PERMISSION_GRANTED || l != PackageManager.PERMISSION_GRANTED || m != PackageManager.PERMISSION_GRANTED ||
//                    n != PackageManager.PERMISSION_GRANTED) {
//                // 如果没有授予该权限，就去提示用户请求
//                startRequestPermission();
//            }
//        }

//        SoulPermission.getInstance().checkAndRequestPermissions(
//                Permissions.build(permissions), new CheckRequestPermissionsListener() {
//                    @Override
//                    public void onAllPermissionOk(Permission[] allPermissions) {
//                        mListview = findViewById(R.id.listview);
//                        context = MainActivity.this;
//                        aCache = ACache.get(context);
//                        onLoad();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(Permission[] refusedPermissions) {
//                        // 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
//                        Toast.makeText(MainActivity.this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.parse("package:" + getPackageName()));
//                        startActivity(intent);
//                    }
//                });


        if (XXPermissions.isGrantedPermission(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
            mListview = findViewById(R.id.listview);
            context = MainActivity.this;
            aCache = ACache.get(context);
            onLoad();
        } else {
            getPermissions();
        }
    }

    //请求权限
    private void getPermissions() {
        XXPermissions.with(this)
                // 申请安装包权限
                //.permission(Permission.REQUEST_INSTALL_PACKAGES)
                // 申请悬浮窗权限
                //.permission(Permission.SYSTEM_ALERT_WINDOW)
                // 申请通知栏权限
                //.permission(Permission.NOTIFICATION_SERVICE)
                // 申请系统设置权限
                //.permission(Permission.WRITE_SETTINGS)
                // 申请单个权限
                //.permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                .permission(permissions)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            T.showShort("获取存储和相机权限成功");
                            mListview = findViewById(R.id.listview);
                            context = MainActivity.this;
                            aCache = ACache.get(context);
                            onLoad();
                        } else {
                            T.showShort("获取部分权限成功，但部分权限未正常授予");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            T.showShort("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                            T.showShort("获取存储和相机权限失败");
                            // 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
                            //Toast.makeText(MainActivity.this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    }
                });
    }

    public void onLoad() {
        adapter = new Adapter(MainActivity.this);
        //暂不用缓存
        // String string = aCache.getAsString("file");
        String string = null;
        if (string == null) {
            showProgress();
            new MyThread().start();
        } else {
            String[] str = string.split(",");

            for (int i = 0; i < str.length; i++) {
                Log.i("file", str[i]);
                File f = new File(str[i]);
                if (f.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(f);
                        String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date(f.lastModified()));
                        AddFileInfo info = new AddFileInfo(f.getName(), Long.valueOf(fis.available()), time, false, f.getAbsolutePath());
                        fileDate += f.getAbsolutePath() + ",";
                        list.add(info);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }
        mListview.setOnItemClickListener(onItemClickListener);
        mListview.setAdapter(adapter);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //  startActivity(OpenFile.openFile(list.get(position).getPath()));
            //跳转pdfView  打开pdf文件
//            Intent intent=new Intent(MainActivity.this,WebViewActivity.class);
//            intent.putExtra("path",list.get(position).getPath());
//            startActivity(intent);
            String path = list.get(position).getPath();
            File file = FileUtils.getFile(path);
            CallOtherOpeanFile.openFile(MainActivity.this, file);
        }
    };


    public class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                doSearch(filePath);
                Thread.sleep(2000);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = 1;
                handler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                dismissProgress();
                adapter.notifyDataSetChanged();
              //  aCache.put("file", fileDate.substring(0, (fileDate.length() - 1)), 600);
            }
        }
    };


    /****
     *计算文件大小
     * @param length
     * @return
     */
    public static String ShowLongFileSzie(Long length) {
        if (length >= 1048576) {
            return (length / 1048576) + "MB";
        } else if (length >= 1024) {
            return (length / 1024) + "KB";
        } else if (length < 1024) {
            return length + "B";
        } else {
            return "0KB";
        }
    }


    /****
     * 递归算法获取本地文件
     * @param path
     */
    private void doSearch(String path) {
        File file = new File(path);

        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {

                    if (f.isDirectory()) {
                        doSearch(f.getPath());
                    } else {
                        if (
                                f.getName().endsWith(".ppt")
                                        // || f.getName().endsWith(".pptx")
                                        || f.getName().endsWith(".docx")
                                        // || f.getName().endsWith(".xls")
                                        || f.getName().endsWith(".doc")
                                        || f.getName().endsWith(".pdf")
                                        || f.getName().endsWith(" .txt")//加空格 区别其他系统txt
                        ) {
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(f);
                                String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date(f.lastModified()));
                                AddFileInfo info = new AddFileInfo(f.getName(), Long.valueOf(fis.available()), time, false, f.getAbsolutePath());
                                list.add(info);
                                fileDate += f.getAbsolutePath() + ",";
                                Log.i("url", f.getAbsolutePath() + "--" + f.getName() + "---" + fis.available() + "--");
                                System.out.println("文件名称：" + f.getName());
                                System.out.println("文件是否存在：" + f.exists());
                                System.out.println("文件的相对路径：" + f.getPath());
                                System.out.println("文件的绝对路径：" + f.getAbsolutePath());
                                System.out.println("文件可以读取：" + f.canRead());
                                System.out.println("文件可以写入：" + f.canWrite());
                                System.out.println("文件上级路径：" + f.getParent());
                                System.out.println("文件大小：" + f.length() + "B");
                                System.out.println("文件最后修改时间：" + new Date(f.lastModified()));
                                System.out.println("是否是文件类型：" + f.isFile());
                                System.out.println("是否是文件夹类型：" + f.isDirectory());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    class Adapter extends BaseAdapter {
        private int[] img_word = new int[]{R.mipmap.doc, R.mipmap.pdf, R.mipmap.txt};
        private LayoutInflater inflater;

        public Adapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.item_mytask_file_listview, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AddFileInfo info = (AddFileInfo) getItem(position);
            if (info.getName().endsWith(".doc") || info.getName().endsWith(".docx")) {
                holder.iv_img.setImageResource(img_word[0]);
            } else if (info.getName().endsWith(".pdf")) {
                holder.iv_img.setImageResource(img_word[1]);
            } else {
                holder.iv_img.setImageResource(img_word[2]);
            }
            holder.tv_name.setText(info.getName());
            holder.size.setText(ShowLongFileSzie(info.getSize()));
            holder.time.setText(info.getTime());
            return convertView;
        }


    }

    class ViewHolder {

        private ImageView iv_img;
        private TextView tv_name;
        private TextView size;
        private TextView time;

        public ViewHolder(View view) {
            iv_img = view.findViewById(R.id.item_file_img);
            tv_name = view.findViewById(R.id.item_file_name);
            size = view.findViewById(R.id.item_file_size);
            time = view.findViewById(R.id.item_file_time);
        }

    }


    /***
     * 启动
     */
    public void showProgress() {
        if (dialog == null) {
            dialog = new ProgressDialog(MainActivity.this);
        }
        dialog.showMessage("正在加载");
    }

    /***
     * 关闭
     */
    public void dismissProgress() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //参数 requestCode是我们在申请权限的时候使用的唯一的申请码
    //String[] permission则是权限列表，一般用不到
    //int[] grantResults 是用户的操作响应，包含这权限是够请求成功
    //由于在权限申请的时候，我们就申请了一个权限，所以此处的数组的长度都是1
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGrantedPermission(this, permissions[0]) &&
                    XXPermissions.isGrantedPermission(this, permissions[1]) &&
                    XXPermissions.isGrantedPermission(this, permissions[2])) {
                // toast("用户已经在权限设置页授予了录音和日历权限");
            } else {
                // toast("用户没有在权限设置页授予权限");
            }
        }
    }


    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}
