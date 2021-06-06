package me.rookie.plugin;

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.plugins.AppPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LauncherPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        boolean transform = target.getPlugins().hasPlugin(AppPlugin.class);
        if (transform) {
            println("===========  LauncherPlugin apply()  ============");
            AppExtension appExtension = target.getExtensions().getByType(AppExtension.class);
            LauncherTransform launcherTransform = new LauncherTransform();
            appExtension.registerTransform(launcherTransform);
        }
    }
}