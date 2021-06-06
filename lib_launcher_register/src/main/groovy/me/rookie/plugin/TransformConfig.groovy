package me.rookie.plugin;
/**
 * 自动注册配置文件
 */
public class TransformConfig {
    //待注入jar文件
    private String targetJarFile;
    //待注入clss文件
    private String targetClassName;
    //待注入方法
    private String targetMethod;
    //注入调用的包名
    private String payloadPackageName;
    //注入调用的类名
    private String payloadClsName;
    //注入调用的方法
    private String payloadMethod;
    //注入调用的方法签名
    private String payloadDescriptor;
    //注入调用的类是不是接口
    private boolean payloadIsInterface;
    //ILauncherLoader的实现类
    private List<String> jarAndLauncherLoaderClsName = new ArrayList<>()

    String getTargetJarFile() {
        return targetJarFile
    }

    void setTargetJarFile(String targetJarFile) {
        this.targetJarFile = targetJarFile
    }

    String getTargetClassName() {
        return targetClassName
    }

    void setTargetClassName(String targetClassName) {
        this.targetClassName = targetClassName
    }

    String getTargetMethod() {
        return targetMethod
    }

    void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod
    }

    String getPayloadPackageName() {
        return payloadPackageName
    }

    void setPayloadPackageName(String payloadPackageName) {
        this.payloadPackageName = payloadPackageName
    }

    String getPayloadClsName() {
        return payloadClsName
    }

    void setPayloadClsName(String payloadClsName) {
        this.payloadClsName = payloadClsName
    }

    String getPayloadMethod() {
        return payloadMethod
    }

    void setPayloadMethod(String payloadMethod) {
        this.payloadMethod = payloadMethod
    }

    String getPayloadDescriptor() {
        return payloadDescriptor
    }

    void setPayloadDescriptor(String payloadDescriptor) {
        this.payloadDescriptor = payloadDescriptor
    }

    boolean getPayloadIsInterface() {
        return payloadIsInterface
    }

    void setPayloadIsInterface(boolean payloadIsInterface) {
        this.payloadIsInterface = payloadIsInterface
    }

    public void addTarget(String loaderCls) {
        jarAndLauncherLoaderClsName.add(loaderCls)
    }

    List<String> getJarAndLauncherLoaderClsName() {
        return jarAndLauncherLoaderClsName
    }
}