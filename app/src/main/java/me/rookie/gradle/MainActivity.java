package me.rookie.gradle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import me.rookie.annotation.Launcher;
import me.rookie.annotation.LauncherMeta;
import me.rookie.launcher.LauncherManager;

import com.rookie.transformdemo.R;

import java.util.List;

@Launcher(group = "综合查询",authority = "MID_XXXXX",route = "/main/main",icon = "",label = "测试")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LauncherManager.init(this);
        List<LauncherMeta> launcherMetaList = LauncherManager.getLauncherMetaList();
    }
}