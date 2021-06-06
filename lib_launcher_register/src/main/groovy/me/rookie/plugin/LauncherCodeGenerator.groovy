package me.rookie.plugin;

import org.apache.commons.compress.utils.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class LauncherCodeGenerator {

    private TransformConfig mTransformConfig;

    LauncherCodeGenerator(TransformConfig transformConfig) {
        mTransformConfig = transformConfig
    }

    public void inject() {
        if (mTransformConfig.getTargetJarFile() != null && mTransformConfig.getTargetJarFile().endsWith(".jar"))
            injectCodeToJarFile(mTransformConfig.getTargetJarFile());
    }


    private void injectCodeToJarFile(String jarPath) {
        File jarFile = new File(jarPath)
        File optFile = new File(jarFile.getParent(), jarFile.getName() + ".opt");
        if (optFile.exists()) {
            optFile.delete();
        }

        JarFile srcFile = new JarFile(jarFile);
        Enumeration<JarEntry> entryEnumeration = srcFile.entries();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optFile));
        while (entryEnumeration.hasMoreElements()) {
            JarEntry jarEntry = entryEnumeration.nextElement();
            String name = jarEntry.getName();
            ZipEntry zipEntry = new ZipEntry(name);
            jarOutputStream.putNextEntry(zipEntry);
            InputStream inputStream = srcFile.getInputStream(zipEntry);
            if (name.equals(mTransformConfig.getTargetClassName())) {
                System.out.println("Inject code to class >>" + name);
                jarOutputStream.write(injectCodeToClass(inputStream));
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream));
            }
            inputStream.close();
            jarOutputStream.closeEntry();
        }
        jarOutputStream.close();
        srcFile.close();
        if (jarFile.exists()) {
            jarFile.delete();
        }
        optFile.renameTo(jarFile);
    }


    private byte[] injectCodeToClass(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassVisitor classVisitor = new MyClassVistor(Opcodes.ASM5, classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    class MyClassVistor extends ClassVisitor {

        public MyClassVistor(int api) {
            super(api);
        }

        public MyClassVistor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals(mTransformConfig.getTargetMethod())) {
                methodVisitor = new MethodVistor(Opcodes.ASM5, methodVisitor);
            }
            return methodVisitor;
        }
    }

    class MethodVistor extends MethodVisitor {

        public MethodVistor(int api) {
            super(api);
        }

        public MethodVistor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                List<String> payload = mTransformConfig.getJarAndLauncherLoaderClsName();
                payload.each {
                    name ->
                        name = name.replace("/", ".");
                        mv.visitLdcInsn(name);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                mTransformConfig.getTargetClassName().replace(".class",""),
                                mTransformConfig.getPayloadMethod(),
                                "(Ljava/lang/String;)V",
                                false);
                }

            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }
}
