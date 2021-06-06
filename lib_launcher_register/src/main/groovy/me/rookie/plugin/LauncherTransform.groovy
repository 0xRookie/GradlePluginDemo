package me.rookie.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class LauncherTransform extends Transform {

    private TransformConfig mTransformConfig;

    LauncherTransform() {
        mTransformConfig = new TransformConfig()
        mTransformConfig.setTargetMethod("init")
        mTransformConfig.setTargetClassName("me/rookie/launcher/LauncherManager.class")
        mTransformConfig.setPayloadIsInterface(true)
        mTransformConfig.setPayloadPackageName("me/rookie/annotation")
        mTransformConfig.setPayloadClsName("me/rookie/annotation/ILauncherLoader")
        mTransformConfig.setPayloadMethod("loadLauncher")
        mTransformConfig.setPayloadDescriptor("(Ljava/lang/String;)V")
    }

    @Override
    public String getName() {
        return "LauncherRegister";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println("Start to transform launcher!!");
        boolean leftSlash = File.separator.equals("/");

        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            Collection<JarInput> jarInputs = input.getJarInputs();
            for (JarInput jarInput : jarInputs) {
                String name = jarInput.getName();
                String md5Hex = DigestUtils.md5Hex(name);
                if (name.endsWith(".jar")) {
                    name = name.substring(0, name.length() - 4);
                }
                File file = jarInput.getFile();
                File contentLocation = transformInvocation.getOutputProvider().getContentLocation(name + "_" + md5Hex, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                if (shouldProcessPreDexJar(file.getPath())) {
                    scanJar(file, contentLocation);
                }
                FileUtils.copyFile(file, contentLocation)
            }

            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs();
            for (DirectoryInput directoryInput : directoryInputs) {
                File dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                String root = directoryInput.getFile().getAbsolutePath();
                if (!root.endsWith(File.separator)) {
                    root += File.separator;
                }
                directoryInput.getFile().eachFileRecurse {
                    File file ->
                        String path = file.getAbsolutePath().replace(root, "");
                        if (!leftSlash) {
                            path = path.replaceAll("\\\\", "/");
                        }
                        if (file.isFile() && shouldProcessClass(path)) {
                            scanClasses(new FileInputStream(file));
                        }
                }
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
        }
        LauncherCodeGenerator codeGenerator = new LauncherCodeGenerator(mTransformConfig);
        codeGenerator.inject();
    }

    boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(mTransformConfig.getPayloadPackageName());
    }

    private File scanJar(File src, File destFile) throws IOException {
        JarFile jarFile = new JarFile(src);
        Enumeration<JarEntry> entryEnumeration = jarFile.entries();
        while (entryEnumeration.hasMoreElements()) {
            JarEntry jarEntry = entryEnumeration.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.equals(mTransformConfig.getTargetClassName())) {
                mTransformConfig.setTargetJarFile(destFile.getPath())
            } else if (entryName.startsWith(mTransformConfig.getPayloadPackageName())) {
                InputStream is = jarFile.getInputStream(jarEntry);
                scanClasses(is)
                is.close()
            }
        }
        jarFile.close();
        return null;
    }

    private void scanClasses(InputStream is) throws IOException {
        try {
            ClassReader classReader = new ClassReader(is);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ScanClassVistor(Opcodes.ASM5, classWriter);
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    class ScanClassVistor extends ClassVisitor {


        public ScanClassVistor(int api) {
            super(api);
        }

        public ScanClassVistor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            if (mTransformConfig.getPayloadIsInterface()) {
                for (String interfaceN : interfaces) {
                    if (interfaceN.equals(mTransformConfig.getPayloadClsName())) {
                        mTransformConfig.addTarget(name)
                    }
                }
            } else {

            }

        }
    }
}
